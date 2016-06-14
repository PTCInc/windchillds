/* bcwti
 *
 * Copyright (c) 2013 PTC Inc. (PTC). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PTC
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 * ecwti
 */
package org.opends.server.windchill;

import javax.management.NotCompliantMBeanException;
import org.opends.server.util.DynamicConstants.*;
import wt.logs.LogsDirViewer;

/** This MBean provides log viewing operations,
 *
 *    <BR><BR><B>Supported API: </B>true
 *    <BR><BR><B>Extendable: </B>false
 */
public class  DSLogsDirViewer
  extends LogsDirViewer
{
  public static final String WDS_LOGS_DIR = "wt.windchillds.logs.dir";

  public  DSLogsDirViewer()
    throws NotCompliantMBeanException
  {
    super( WDS_LOGS_DIR, null );  // Second argument is a filter; null means include all files in the directory.
  }

}
