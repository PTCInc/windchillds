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
 *      Portions Copyright 2014 ForgeRock AS
 */

package org.opends.guitools.controlpanel.task;

import org.forgerock.i18n.LocalizableMessage;
import org.opends.server.types.OpenDsException;

/**
 * Exception throw when there is an error updating the configuration online
 * (in general is used as a wrapper when we get a NamingException).
 *
 */
public class OnlineUpdateException extends OpenDsException
{

  private static final long serialVersionUID = 2594845362087209988L;

  /**
   * Creates an exception with a message.
   * @param msg the message.
   */
  public OnlineUpdateException(LocalizableMessage msg)
  {
    super(msg);
  }

  /**
   * Creates an exception with a message and a root cause.
   * @param msg the message.
   * @param rootCause the root cause.
   */
  public OnlineUpdateException(LocalizableMessage msg, Throwable rootCause)
  {
    super(msg, rootCause);
  }
}