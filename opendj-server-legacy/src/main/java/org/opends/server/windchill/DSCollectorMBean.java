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

import java.io.File;
import javax.management.MBeanOperationInfo;
import java.util.Map;

import wt.jmx.annotations.MBeanOperationImpact;
import wt.jmx.core.mbeans.SelfEmailingMBean;


/** Provides collect, send, and misc controls.  Collects Windchill Directory Server
 *  logs, properties, and MBean data and send it to PTC support via https.
 *
 *    <BR><BR><B>Supported API: </B>true
 *    <BR><BR><B>Extendable: </B>true
 */
public interface  DSCollectorMBean
  extends SelfEmailingMBean
{

  /** Get SCN.
   *  <p>
   *
   *  SCN - Submitter Contract Number.  Required to send materials to PTC support.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *  @return the Submitter Contract Number.
   *
   */
  public String getSCN( );

  /** Set SCN.
   *  <p>
   *
   *  SCN - Submitter Contract Number.  Required to send materials to PTC support..
   *
   *  The SCN is initially set from environment variables or System Properties.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param scn Submitter Contract Number
   */
  public void setSCN( String scn );

  /** Get Mail Server.
   *  <p>
   *
   *  Mail Server -- Needed to email materials to PTC support.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *  @return the Mail Server host
   *
   */
  public String getMailServer( );

  /** Set Mail Server.
   *
   *  <p>
   *
   *  Mail Server -- Needed to email materials to PTC support.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *  @param mailServer mailserver host.
   */
  public void setMailServer( String mailServer );

  /** Get Mail Server Port number.
   *
   *  <p>
   *
   *  Mail server port number used to send emails with support materials to PTC support.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *  @return Mail Server port number
   *
   */
  public String getMailServerPort( );

  /** Set Mail Server Port number.
   *  <p>
   *
   *  Mail server port number used to send emails with support materials to PTC support.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *  @param portNumber of Mail Server.
   */
  public void setMailServerPort( String portNumber );

  /** Get path components.
   *  <p>
   *
   *  Path components -- the relative directory path components to be used
   *  for data collection.  The path is returned as a String array comprised of
   *  relative directory names.  This path is relative to the installation
   *  directory for the Windchill Directory Server. Each directory
   *  component is passed as a separate array element to avoid
   *  issues with file separators.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *  @return array of path components for data collection
   *
   */
  public String[] getPathComponents( );

  /** Set path components.
   *  <p>
   *
   *  Path components -- the relative directory path components to be used
   *  for data collection.  The path is returned as a String array comprised of
   *  relative directory names.  This path is relative to the installation
   *  directory for the Windchill Directory Server. Each directory
   *  component is passed as a separate array element to avoid
   *  issues with file separators.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param pathcomponents of directory for data collection path
   */
  public void setPathComponents( String[] pathcomponents );

  /** Get collection path.
   *  <p>
   *
   *  collection path - the relative directory path to be used for data collection.
   *  The path is relative to the installation directory of the Windchill
   *  Directory Server. Each directory component is passed as a separate
   *  array element to avoid issues with file separators.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *  @return the relative directory path for data collection
   *
   */
  public String getCollectionPath( );

  /** Set collection path.
   *  <p>
   *
   *  collection path - the relative directory path to be used for data collection.
   *  The path is relative to the installation directory of the Windchill
   *  Directory Server. Each directory component is passed as a separate
   *  array element to avoid issues with file separators.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param collectionpath is relative parent directory of data collection area.
   */
  public void setCollectionPath( String collectionpath );

  /** Get absolute collection path.
   *  <p>
   *
   *  absolute collection path - the absolute (full) directory path to be used for
   *  data collection. The path is returned as a String.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *  @return the absolute directory path for data collection
   *
   */
  public String getFullCollectionPath( );

  /** Copy Directory.
   *  <p>
   *
   *  Copies from source to target directories.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param source - source directory path
   *    @param target - target directory path
   *    @param dontOverwrite - If set, don't overwrite existing files.
   *
   *    @return String: null if successful, Error information if not successful
   *
   */
  public String copyDir( String source, String target, boolean dontOverwrite );

  /** Copy File.
   *  <p>
   *
   *  Copies from source to target files.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param source - source file path
   *    @param target - target file path
   *    @param dontOverwrite - If set, don't overwrite existing files.
   *
   *    @return String: null if successful, Error information if not successful
   *
   */
  @MBeanOperationImpact( MBeanOperationInfo.ACTION )
  public String copyFile( String source, String target, boolean dontOverwrite );

  /** Collect.
   *  <p>
   *
   *  Collect Directory Data.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param topicIdentifier identifies topic or call number
   *    @param maxAgeInDays of logs (if any)
   *    @param minAgeInDays of logs (if any)
   *    @param pathTimeStamp is collection pathTimeStamp and makes each collection unique
   *    @param pluginType is name of plugin type and used in directory naming
   *    @param relativeDirPath is relative directory path of parent directory containing collection data
   *
   *    @return a map with success, path, message, and other info to indicate
   *            success or failure and error messages.
   */
  @MBeanOperationImpact( MBeanOperationInfo.ACTION )
  public Map<String, Object> collect( String topicIdentifier, long maxAgeInDays,
          long minAgeInDays, String pathTimeStamp, String pluginType,
          String[] relativeDirPath );

  /** collectMBeanDump.
   *  <p>
   *
   *  Collect the dump of all MBean data.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param location gives full file path of directory to contain mbean dump.
   *
   *    @return String: null if successful, Error information if not successful
   */
  @MBeanOperationImpact( MBeanOperationInfo.ACTION )
  public String collectMBeanDump( String location );

  /** collectLogs.
   *  <p>
   *
   *  Collect all Windchill Directory Server Logs.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param location gives full file path of directory to contain mbean dump.
   *
   *    @return String: null if successful, Error information if not successful
   */
  @MBeanOperationImpact( MBeanOperationInfo.ACTION )
  public String collectLogs( String location );

  /** collectProperties.
   *  <p>
   *
   *  Collect Windchill Directory Server properties files.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param location gives full file path of directory to contain mbean dump.
   *
   *    @return String: null if successful, Error information if not successful
   */
  @MBeanOperationImpact( MBeanOperationInfo.ACTION )
  public String collectProperties( String location );

  /** Send.
   *  <p>
   *
   *  Send the collected data.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param callNumberString required if sending data to support
   *    @param topicDir - directory for topic to be sent
   *    @param dirToSend - directory to send -- This is the plugin type name,
   *             but does not really apply here as we send all directories
   *             at one time
   *    @param sendComments for data being sent to support
   *    @param uniqueId
   *    @param scn - submitter contract number is required if sending data to support
   *    @param relativeDirPath is relative directory path of parent directory containing collection data
   *
   *    @return a map with success, path, message, and other info to indicate
   *            success or failure and error messages.
   *
   */
  @MBeanOperationImpact( MBeanOperationInfo.ACTION )
  public Map<String, Object> send( String callNumberString,
          String topicDir, String dirToSend, String sendComments,
          String uniqueId, String scn, String[] relativeDirPath);

  /** Send Directory.
   *  <p>
   *
   *  Send collected data to support from the directory specified.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param location gives the directory to send to support
   *    @param callNumber required if sending data to support
   *    @param description of data being sent to support
   *
   *    @return - String: null if successful, Error information if not successful.
   */
  @MBeanOperationImpact( MBeanOperationInfo.ACTION )
  public String sendDirectory( String location,
          long callNumber, String description );

}
