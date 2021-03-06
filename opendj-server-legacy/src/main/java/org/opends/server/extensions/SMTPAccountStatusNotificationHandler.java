/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at legal-notices/CDDLv1_0.txt
 * or http://forgerock.org/license/CDDLv1.0.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at legal-notices/CDDLv1_0.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2008 Sun Microsystems, Inc.
 *      Portions Copyright 2014-2015 ForgeRock AS
 *      Portions copyright 2015 Edan Idzerda
 */
package org.opends.server.extensions;

import static org.opends.messages.ExtensionMessages.*;
import static org.opends.server.util.StaticUtils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.forgerock.i18n.LocalizableMessage;
import org.forgerock.i18n.LocalizableMessageBuilder;
import org.forgerock.i18n.slf4j.LocalizedLogger;
import org.forgerock.opendj.config.server.ConfigException;
import org.forgerock.opendj.ldap.ByteString;
import org.forgerock.opendj.ldap.ResultCode;
import org.forgerock.util.Utils;
import org.opends.server.admin.server.ConfigurationChangeListener;
import org.opends.server.admin.std.server.AccountStatusNotificationHandlerCfg;
import org.opends.server.admin.std.server.SMTPAccountStatusNotificationHandlerCfg;
import org.opends.server.api.AccountStatusNotificationHandler;
import org.opends.server.core.DirectoryServer;
import org.opends.server.types.AccountStatusNotification;
import org.opends.server.types.AccountStatusNotificationProperty;
import org.opends.server.types.AccountStatusNotificationType;
import org.opends.server.types.Attribute;
import org.opends.server.types.AttributeType;
import org.forgerock.opendj.config.server.ConfigChangeResult;
import org.opends.server.types.Entry;
import org.opends.server.types.InitializationException;
import org.opends.server.util.EMailMessage;

/**
 * This class provides an implementation of an account status notification
 * handler that can send e-mail messages via SMTP to end users and/or
 * administrators whenever an account status notification occurs.  The e-mail
 * messages will be generated from template files, which contain the information
 * to use to create the message body.  The template files may contain plain
 * text, in addition to the following tokens:
 * <UL>
 *   <LI>%%notification-type%% -- Will be replaced with the name of the
 *       account status notification type for the notification.</LI>
 *   <LI>%%notification-message%% -- Will be replaced with the message for the
 *       account status notification.</LI>
 *   <LI>%%notification-user-dn%% -- Will be replaced with the string
 *       representation of the DN for the user that is the target of the
 *       account status notification.</LI>
 *   <LI>%%notification-user-attr:attrname%% -- Will be replaced with the value
 *       of the attribute specified by attrname from the user's entry.  If the
 *       specified attribute has multiple values, then the first value
 *       encountered will be used.  If the specified attribute does not have any
 *       values, then it will be replaced with an emtpy string.</LI>
 *   <LI>%%notification-property:propname%% -- Will be replaced with the value
 *       of the specified notification property from the account status
 *       notification.  If the specified property has multiple values, then the
 *       first value encountered will be used.  If the specified property does
 *       not have any values, then it will be replaced with an emtpy
 *       string.</LI>
 * </UL>
 */
public class SMTPAccountStatusNotificationHandler
       extends AccountStatusNotificationHandler
                    <SMTPAccountStatusNotificationHandlerCfg>
       implements ConfigurationChangeListener
                       <SMTPAccountStatusNotificationHandlerCfg>
{
  private static final LocalizedLogger logger = LocalizedLogger.getLoggerForThisClass();



  /** A mapping between the notification types and the message template. */
  private HashMap<AccountStatusNotificationType,
                  List<NotificationMessageTemplateElement>> templateMap;

  /** A mapping between the notification types and the message subject. */
  private HashMap<AccountStatusNotificationType,String> subjectMap;

  /** The current configuration for this account status notification handler. */
  private SMTPAccountStatusNotificationHandlerCfg currentConfig;



  /**
   * Creates a new, uninitialized instance of this account status notification
   * handler.
   */
  public SMTPAccountStatusNotificationHandler()
  {
    super();
  }



  /** {@inheritDoc} */
  @Override
  public void initializeStatusNotificationHandler(
                   SMTPAccountStatusNotificationHandlerCfg configuration)
         throws ConfigException, InitializationException
  {
    currentConfig = configuration;
    currentConfig.addSMTPChangeListener(this);

    subjectMap  = parseSubjects(configuration);
    templateMap = parseTemplates(configuration);

    // Make sure that the Directory Server is configured with information about
    // one or more mail servers.
    List<Properties> propList = DirectoryServer.getMailServerPropertySets();
    if (propList == null || propList.isEmpty())
    {
      throw new ConfigException(ERR_SMTP_ASNH_NO_MAIL_SERVERS_CONFIGURED.get(configuration.dn()));
    }

    // Make sure that either an explicit recipient list or a set of email
    // address attributes were provided.
    Set<AttributeType> mailAttrs = configuration.getEmailAddressAttributeType();
    Set<String> recipients = configuration.getRecipientAddress();
    if ((mailAttrs == null || mailAttrs.isEmpty()) &&
        (recipients == null || recipients.isEmpty()))
    {
      throw new ConfigException(ERR_SMTP_ASNH_NO_RECIPIENTS.get(configuration.dn()));
    }
  }



  /**
   * Examines the provided configuration and parses the message subject
   * information from it.
   *
   * @param  configuration  The configuration to be examined.
   *
   * @return  A mapping between the account status notification type and the
   *          subject that should be used for messages generated for
   *          notifications with that type.
   *
   * @throws  ConfigException  If a problem occurs while parsing the subject
   *                           configuration.
   */
  private HashMap<AccountStatusNotificationType,String> parseSubjects(
               SMTPAccountStatusNotificationHandlerCfg configuration)
          throws ConfigException
  {
    HashMap<AccountStatusNotificationType,String> map = new HashMap<>();

    for (String s : configuration.getMessageSubject())
    {
      int colonPos = s.indexOf(':');
      if (colonPos < 0)
      {
        throw new ConfigException(ERR_SMTP_ASNH_SUBJECT_NO_COLON.get(s, configuration.dn()));
      }

      String notificationTypeName = s.substring(0, colonPos).trim();
      AccountStatusNotificationType t =
           AccountStatusNotificationType.typeForName(notificationTypeName);
      if (t == null)
      {
        throw new ConfigException(ERR_SMTP_ASNH_SUBJECT_INVALID_NOTIFICATION_TYPE.get(
            s, configuration.dn(), notificationTypeName));
      }
      else if (map.containsKey(t))
      {
        throw new ConfigException(ERR_SMTP_ASNH_SUBJECT_DUPLICATE_TYPE.get(
            configuration.dn(), notificationTypeName));
      }

      map.put(t, s.substring(colonPos+1).trim());
      if (logger.isTraceEnabled())
      {
        logger.trace("Subject for notification type " + t.getName() +
                         ":  " + map.get(t));
      }
    }

    return map;
  }



  /**
   * Examines the provided configuration and parses the message template
   * information from it.
   *
   * @param  configuration  The configuration to be examined.
   *
   * @return  A mapping between the account status notification type and the
   *          template that should be used to generate messages for
   *          notifications with that type.
   *
   * @throws  ConfigException  If a problem occurs while parsing the template
   *                           configuration.
   */
  private HashMap<AccountStatusNotificationType,
                  List<NotificationMessageTemplateElement>> parseTemplates(
               SMTPAccountStatusNotificationHandlerCfg configuration)
          throws ConfigException
  {
    HashMap<AccountStatusNotificationType,
            List<NotificationMessageTemplateElement>> map = new HashMap<>();

    for (String s : configuration.getMessageTemplateFile())
    {
      int colonPos = s.indexOf(':');
      if (colonPos < 0)
      {
        throw new ConfigException(ERR_SMTP_ASNH_TEMPLATE_NO_COLON.get(s, configuration.dn()));
      }

      String notificationTypeName = s.substring(0, colonPos).trim();
      AccountStatusNotificationType t =
           AccountStatusNotificationType.typeForName(notificationTypeName);
      if (t == null)
      {
        throw new ConfigException(ERR_SMTP_ASNH_TEMPLATE_INVALID_NOTIFICATION_TYPE.get(
            s, configuration.dn(), notificationTypeName));
      }
      else if (map.containsKey(t))
      {
        throw new ConfigException(ERR_SMTP_ASNH_TEMPLATE_DUPLICATE_TYPE.get(
            configuration.dn(), notificationTypeName));
      }

      String path = s.substring(colonPos+1).trim();
      File f = new File(path);
      if (! f.isAbsolute() )
      {
        f = new File(DirectoryServer.getInstanceRoot() + File.separator +
            path);
      }
      if (! f.exists())
      {
        throw new ConfigException(ERR_SMTP_ASNH_TEMPLATE_NO_SUCH_FILE.get(
                                       path, configuration.dn()));
      }

      map.put(t, parseTemplateFile(f));
      if (logger.isTraceEnabled())
      {
        logger.trace("Decoded template elment list for type " +
                         t.getName());
      }
    }

    return map;
  }



  /**
   * Parses the specified template file into a list of notification message
   * template elements.
   *
   * @param  f  A reference to the template file to be parsed.
   *
   * @return  A list of notification message template elements parsed from the
   *          specified file.
   *
   * @throws  ConfigException  If error occurs while attempting to parse the
   *                           template file.
   */
  private List<NotificationMessageTemplateElement> parseTemplateFile(File f)
          throws ConfigException
  {
    LinkedList<NotificationMessageTemplateElement> elementList = new LinkedList<>();

    BufferedReader reader = null;
    try
    {
      reader = new BufferedReader(new FileReader(f));
      int lineNumber = 0;
      while (true)
      {
        String line = reader.readLine();
        if (line == null)
        {
          break;
        }

        if (logger.isTraceEnabled())
        {
          logger.trace("Read message template line " + line);
        }

        lineNumber++;
        int startPos = 0;
        while (startPos < line.length())
        {
          int delimPos = line.indexOf("%%", startPos);
          if (delimPos < 0)
          {
            if (logger.isTraceEnabled())
            {
              logger.trace("No more tokens -- adding text " +
                               line.substring(startPos));
            }

            elementList.add(new TextNotificationMessageTemplateElement(
                                     line.substring(startPos)));
            break;
          }
          else
          {
            if (delimPos > startPos)
            {
              if (logger.isTraceEnabled())
              {
                logger.trace("Adding text before token " +
                                 line.substring(startPos));
              }

              elementList.add(new TextNotificationMessageTemplateElement(
                                       line.substring(startPos, delimPos)));
            }

            int closeDelimPos = line.indexOf("%%", delimPos+1);
            if (closeDelimPos < 0)
            {
              // There was an opening %% but not a closing one.
              throw new ConfigException(
                             ERR_SMTP_ASNH_TEMPLATE_UNCLOSED_TOKEN.get(
                                  delimPos, lineNumber));
            }
            else
            {
              String tokenStr = line.substring(delimPos+2, closeDelimPos);
              String lowerTokenStr = toLowerCase(tokenStr);
              if (lowerTokenStr.equals("notification-type"))
              {
                if (logger.isTraceEnabled())
                {
                  logger.trace("Found a notification type token " +
                                   tokenStr);
                }

                elementList.add(
                     new NotificationTypeNotificationMessageTemplateElement());
              }
              else if (lowerTokenStr.equals("notification-message"))
              {
                if (logger.isTraceEnabled())
                {
                  logger.trace("Found a notification message token " +
                                   tokenStr);
                }

                elementList.add(
                  new NotificationMessageNotificationMessageTemplateElement());
              }
              else if (lowerTokenStr.equals("notification-user-dn"))
              {
                if (logger.isTraceEnabled())
                {
                  logger.trace("Found a notification user DN token " +
                                   tokenStr);
                }

                elementList.add(
                     new UserDNNotificationMessageTemplateElement());
              }
              else if (lowerTokenStr.startsWith("notification-user-attr:"))
              {
                String attrName = lowerTokenStr.substring(23);
                AttributeType attrType = DirectoryServer.getAttributeTypeOrNull(attrName);
                if (attrType == null)
                {
                  throw new ConfigException(
                                 ERR_SMTP_ASNH_TEMPLATE_UNDEFINED_ATTR_TYPE.get(
                                      delimPos, lineNumber, attrName));
                }
                else
                {
                  if (logger.isTraceEnabled())
                  {
                    logger.trace("Found a user attribute token for  " +
                                     attrType.getNameOrOID() + " -- " +
                                     tokenStr);
                  }

                  elementList.add(
                       new UserAttributeNotificationMessageTemplateElement(
                                attrType));
                }
              }
              else if (lowerTokenStr.startsWith("notification-property:"))
              {
                String propertyName = lowerTokenStr.substring(22);
                AccountStatusNotificationProperty property =
                     AccountStatusNotificationProperty.forName(propertyName);
                if (property == null)
                {
                  throw new ConfigException(
                                 ERR_SMTP_ASNH_TEMPLATE_UNDEFINED_PROPERTY.get(
                                      delimPos, lineNumber, propertyName));
                }
                else
                {
                  if (logger.isTraceEnabled())
                  {
                    logger.trace("Found a notification property token " +
                                     "for " + propertyName + " -- " + tokenStr);
                  }

                  elementList.add(
                    new NotificationPropertyNotificationMessageTemplateElement(
                          property));
                }
              }
              else
              {
                throw new ConfigException(
                               ERR_SMTP_ASNH_TEMPLATE_UNRECOGNIZED_TOKEN.get(
                                    tokenStr, delimPos, lineNumber));
              }

              startPos = closeDelimPos + 2;
            }
          }
        }


        // We need to put a CRLF at the end of the line, as per the SMTP spec.
        elementList.add(new TextNotificationMessageTemplateElement("\r\n"));
      }

      return elementList;
    }
    catch (Exception e)
    {
      logger.traceException(e);

      throw new ConfigException(ERR_SMTP_ASNH_TEMPLATE_CANNOT_PARSE.get(
          f.getAbsolutePath(), currentConfig.dn(), getExceptionMessage(e)));
    }
    finally
    {
      Utils.closeSilently(reader);
    }
  }



  /** {@inheritDoc} */
  @Override
  public boolean isConfigurationAcceptable(
                      AccountStatusNotificationHandlerCfg
                           configuration,
                      List<LocalizableMessage> unacceptableReasons)
  {
    SMTPAccountStatusNotificationHandlerCfg config =
         (SMTPAccountStatusNotificationHandlerCfg) configuration;
    return isConfigurationChangeAcceptable(config, unacceptableReasons);
  }



  /** {@inheritDoc} */
  @Override
  public void handleStatusNotification(AccountStatusNotification notification)
  {
    SMTPAccountStatusNotificationHandlerCfg config = currentConfig;
    HashMap<AccountStatusNotificationType,String> subjects = subjectMap;
    HashMap<AccountStatusNotificationType,
            List<NotificationMessageTemplateElement>> templates = templateMap;


    // First, see if the notification type is one that we handle.  If not, then
    // return without doing anything.
    AccountStatusNotificationType notificationType =
         notification.getNotificationType();
    List<NotificationMessageTemplateElement> templateElements =
         templates.get(notificationType);
    if (templateElements == null)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("No message template for notification type " +
                         notificationType.getName());
      }

      return;
    }


    // It is a notification that should be handled, so we can start generating
    // the e-mail message.  First, check to see if there are any mail attributes
    // that would cause us to send a message to the end user.
    LinkedList<String> recipients = new LinkedList<>();
    Set<AttributeType> addressAttrs = config.getEmailAddressAttributeType();
    Set<String> recipientAddrs = config.getRecipientAddress();
    if (addressAttrs != null && !addressAttrs.isEmpty())
    {
      Entry userEntry = notification.getUserEntry();
      for (AttributeType t : addressAttrs)
      {
        List<Attribute> attrList = userEntry.getAttribute(t);
        if (attrList != null)
        {
          for (Attribute a : attrList)
          {
            for (ByteString v : a)
            {
              if (logger.isTraceEnabled())
              {
                logger.trace("Adding end user recipient %s from attr %s",
                    v, a.getNameWithOptions());
              }

              recipients.add(v.toString());
            }
          }
        }
      }

      if (recipients.isEmpty())
      {
        if (recipientAddrs == null || recipientAddrs.isEmpty())
        {
          // There are no recipients at all, so there's no point in generating
          // the message.  Return without doing anything.
          if (logger.isTraceEnabled())
          {
            logger.trace("No end user recipients, and no explicit " +
                             "recipients");
          }

          return;
        }
        else
        {
          if (! config.isSendMessageWithoutEndUserAddress())
          {
            // We can't send the message to the end user, and the handler is
            // configured to not send only to administrators, so we shouln't
            // do anything.
            if (logger.isTraceEnabled())
            {
              logger.trace("No end user recipients, and shouldn't send " +
                               "without end user recipients");
            }

            return;
          }
        }
      }
    }


    // Next, add any explicitly-defined recipients.
    if (recipientAddrs != null)
    {
      if (logger.isTraceEnabled())
      {
        for (String s : recipientAddrs)
        {
          logger.trace("Adding explicit recipient " + s);
        }
      }

      recipients.addAll(recipientAddrs);
    }


    // Get the message subject to use.  If none is defined, then use a generic
    // subject.
    String subject = subjects.get(notificationType);
    if (subject == null)
    {
      subject = INFO_SMTP_ASNH_DEFAULT_SUBJECT.get().toString();

      if (logger.isTraceEnabled())
      {
        logger.trace("Using default subject of " + subject);
      }
    }
    else if (logger.isTraceEnabled())
    {
      logger.trace("Using per-type subject of " + subject);
    }



    // Generate the message body.
    LocalizableMessageBuilder messageBody = new LocalizableMessageBuilder();
    for (NotificationMessageTemplateElement e : templateElements)
    {
      e.generateValue(messageBody, notification);
    }


    // Create and send the e-mail message.
    EMailMessage message = new EMailMessage(config.getSenderAddress(),
                                            recipients, subject);
    message.setBody(messageBody);

    if (config.isSendEmailAsHtml())
    {
      message.setBodyMIMEType("text/html");
    }
    if (logger.isTraceEnabled())
    {
      logger.trace("Set message body of " + messageBody);
    }


    try
    {
      message.send();

      if (logger.isTraceEnabled())
      {
        logger.trace("Successfully sent the message");
      }
    }
    catch (Exception e)
    {
      logger.traceException(e);

      logger.error(ERR_SMTP_ASNH_CANNOT_SEND_MESSAGE,
          notificationType.getName(), notification.getUserDN(), getExceptionMessage(e));
    }
  }



  /** {@inheritDoc} */
  @Override
  public boolean isConfigurationChangeAcceptable(
                      SMTPAccountStatusNotificationHandlerCfg configuration,
                      List<LocalizableMessage> unacceptableReasons)
  {
    boolean configAcceptable = true;


    // Make sure that the Directory Server is configured with information about
    // one or more mail servers.
    List<Properties> propList = DirectoryServer.getMailServerPropertySets();
    if (propList == null || propList.isEmpty())
    {
      unacceptableReasons.add(ERR_SMTP_ASNH_NO_MAIL_SERVERS_CONFIGURED.get(configuration.dn()));
      configAcceptable = false;
    }


    // Make sure that either an explicit recipient list or a set of email
    // address attributes were provided.
    Set<AttributeType> mailAttrs = configuration.getEmailAddressAttributeType();
    Set<String> recipients = configuration.getRecipientAddress();
    if ((mailAttrs == null || mailAttrs.isEmpty()) &&
        (recipients == null || recipients.isEmpty()))
    {
      unacceptableReasons.add(ERR_SMTP_ASNH_NO_RECIPIENTS.get(configuration.dn()));
      configAcceptable = false;
    }

    try
    {
      parseSubjects(configuration);
    }
    catch (ConfigException ce)
    {
      logger.traceException(ce);

      unacceptableReasons.add(ce.getMessageObject());
      configAcceptable = false;
    }

    try
    {
      parseTemplates(configuration);
    }
    catch (ConfigException ce)
    {
      logger.traceException(ce);

      unacceptableReasons.add(ce.getMessageObject());
      configAcceptable = false;
    }

    return configAcceptable;
  }



  /** {@inheritDoc} */
  @Override
  public ConfigChangeResult applyConfigurationChange(
              SMTPAccountStatusNotificationHandlerCfg configuration)
  {
    final ConfigChangeResult ccr = new ConfigChangeResult();
    try
    {
      HashMap<AccountStatusNotificationType,String> subjects =
           parseSubjects(configuration);
      HashMap<AccountStatusNotificationType,
              List<NotificationMessageTemplateElement>> templates =
           parseTemplates(configuration);

      currentConfig = configuration;
      subjectMap    = subjects;
      templateMap   = templates;
    }
    catch (ConfigException ce)
    {
      logger.traceException(ce);
      ccr.setResultCode(ResultCode.UNWILLING_TO_PERFORM);
      ccr.addMessage(ce.getMessageObject());
    }
    return ccr;
  }
}
