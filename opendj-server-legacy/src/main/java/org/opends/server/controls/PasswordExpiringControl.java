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
 *      Copyright 2006-2008 Sun Microsystems, Inc.
 *      Portions Copyright 2014-2015 ForgeRock AS
 */
package org.opends.server.controls;
import org.forgerock.i18n.LocalizableMessage;



import org.forgerock.opendj.io.ASN1Writer;
import org.opends.server.types.*;
import org.forgerock.opendj.ldap.ResultCode;
import org.forgerock.opendj.ldap.ByteString;
import org.forgerock.i18n.slf4j.LocalizedLogger;
import static org.opends.messages.ProtocolMessages.*;
import static org.opends.server.util.ServerConstants.*;
import static org.opends.server.util.StaticUtils.*;

import java.io.IOException;


/**
 * This class implements the Netscape password expiring control, which serves as
 * a warning to clients that the user's password is about to expire. The only
 * element contained in the control value is a string representation of the
 * number of seconds until expiration.
 */
public class PasswordExpiringControl
       extends Control
{
  /**
   * ControlDecoder implementation to decode this control from a ByteString.
   */
  private static final class Decoder
      implements ControlDecoder<PasswordExpiringControl>
  {
    /** {@inheritDoc} */
    public PasswordExpiringControl decode(boolean isCritical, ByteString value)
        throws DirectoryException
    {
      if (value == null)
      {
        LocalizableMessage message = ERR_PWEXPIRING_NO_CONTROL_VALUE.get();
        throw new DirectoryException(ResultCode.PROTOCOL_ERROR, message);
      }

      int secondsUntilExpiration;
      try
      {
        secondsUntilExpiration =
            Integer.parseInt(value.toString());
      }
      catch (Exception e)
      {
        logger.traceException(e);

        LocalizableMessage message = ERR_PWEXPIRING_CANNOT_DECODE_SECONDS_UNTIL_EXPIRATION.
            get(getExceptionMessage(e));
        throw new DirectoryException(ResultCode.PROTOCOL_ERROR, message);
      }


      return new PasswordExpiringControl(isCritical,
          secondsUntilExpiration);
    }

    public String getOID()
    {
      return OID_NS_PASSWORD_EXPIRING;
    }

  }

  /**
   * The Control Decoder that can be used to decode this control.
   */
  public static final ControlDecoder<PasswordExpiringControl> DECODER =
    new Decoder();
  private static final LocalizedLogger logger = LocalizedLogger.getLoggerForThisClass();




  /** The length of time in seconds until the password actually expires. */
  private int secondsUntilExpiration;



  /**
   * Creates a new instance of the password expiring control with the provided
   * information.
   *
   * @param  secondsUntilExpiration  The length of time in seconds until the
   *                                 password actually expires.
   */
  public PasswordExpiringControl(int secondsUntilExpiration)
  {
    this(false, secondsUntilExpiration);
  }



  /**
   * Creates a new instance of the password expiring control with the provided
   * information.
   *
   * @param  isCritical              Indicates whether support for this control
   *                                 should be considered a critical part of the
   *                                 client processing.
   * @param  secondsUntilExpiration  The length of time in seconds until the
   *                                 password actually expires.
   */
  public PasswordExpiringControl(boolean isCritical, int secondsUntilExpiration)
  {
    super(OID_NS_PASSWORD_EXPIRING, isCritical);


    this.secondsUntilExpiration = secondsUntilExpiration;
  }


  /**
   * Writes this control's value to an ASN.1 writer. The value (if any) must be
   * written as an ASN1OctetString.
   *
   * @param writer The ASN.1 output stream to write to.
   * @throws IOException If a problem occurs while writing to the stream.
   */
  @Override
  public void writeValue(ASN1Writer writer) throws IOException {
    writer.writeOctetString(String.valueOf(secondsUntilExpiration));
  }



  /**
   * Retrieves the length of time in seconds until the password actually
   * expires.
   *
   * @return  The length of time in seconds until the password actually expires.
   */
  public int getSecondsUntilExpiration()
  {
    return secondsUntilExpiration;
  }



  /**
   * Appends a string representation of this password expiring control to the
   * provided buffer.
   *
   * @param  buffer  The buffer to which the information should be appended.
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("PasswordExpiringControl(secondsUntilExpiration=");
    buffer.append(secondsUntilExpiration);
    buffer.append(")");
  }
}

