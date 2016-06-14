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


import java.beans.BeanInfo;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Introspector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.management.MBeanOperationInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.MBeanServer;

import org.opends.server.core.DirectoryServer;
import org.opends.server.util.DynamicConstants.*;
//import org.opends.server.windchill.MBeanStartup;

import wt.jmx.annotations.MBeanOperationImpact;
import wt.jmx.core.mbeans.*;
import wt.jmx.core.MBeanRegistry;
import wt.jmx.core.MBeanUtilities;
import wt.jmx.core.SelfAwareMBean;
import wt.log4j.LogR;

/** This MBean provides operations to display and set properties,
 *  and other utility functions.
 *
 *    <BR><BR><B>Supported API: </B>true
 *    <BR><BR><B>Extendable: </B>false
 */
public final class DSCollector
  extends SelfAwareMBean
  implements DSCollectorMBean
{
  private static final String  CLASSNAME = DSUtility.class.getName();

  private static String SCN = null;
  private static String mailServer = null;
  private static String mailPort = null;
  private static String collectionPath = null;
  private static String absoluteCollectionPath = null;
  private static String[] pathComponents = {"utilities",
                                            "SystemConfigurationCollector",
                                            "gathered"};

  private static final String KEY_PATH = "path";
  private static final String KEY_WDSPATH = "WDSpath";
  private static final String KEY_MESSAGE = "message";
  private static final String KEY_SUCCESS = "success";
  private static final String KEY_LOCATION = "location";
  private static final String KEY_HOST = "host";

  private static final String PluginTypeLOG = "PTC_Logs";
  private static final String PluginTypeProperty = "PTC_Properties";
  private static final String PluginTypeUtility = "Utilities";
  private static final String MBeanDumpName = "DirectoryServerMBeanDump.xml";
  private static final String LogsDir = File.separator + "logs";
  private static final String ClassesDir = File.separator + "classes";
  private static final String ConfigDir = File.separator + "config";
  private static final String LibDir = File.separator + "lib";
  private static final String SchemaDir = File.separator + "schema";
  private static final String SUCCESS = "Success";

  static
  {
    try
    {
      final BeanInfo  beanInfo = Introspector.getBeanInfo( DSCollector.class );
      beanInfo.getBeanDescriptor().setValue( "persistenceDelegate",
        new DefaultPersistenceDelegate()
        {
          @Override
          protected final void  initialize( final Class<?> type, final Object oldInstance, final Object newInstance, final Encoder out )
          {
            super.initialize( type, oldInstance, newInstance, out );
            initializeClassInfo ();
          }
        }
      );
    }
    catch ( Exception e )
    {
      LogR.getLogger( CLASSNAME ).error( "Failed to set up JavaBeans persistence", e );
    }
  }

  public static final String  OBJECT_NAME_POSTFIX[][] = new String[][] { { MBeanRegistry.SUB_SYSTEM_PROP_KEY, "DSCollector" } };

  private static final ResourceBundle  RESOURCE = ResourceBundle.getBundle( mbeansResource.class.getName() );

  private static final String  STRING_CLASS_NAME = String.class.getName();

  public static DSCollector  newDSCollector()
  {
     try
    {
      return ( new DSCollector() );
    }
    catch ( NotCompliantMBeanException e )
    {
      throw new IllegalArgumentException( e );
    }
  }

  public  DSCollector()
    throws NotCompliantMBeanException
  {
    super( DSCollectorMBean.class );
    initializeClassInfo ();
  }

  private static void initializeClassInfo ()
  {
     String scn;
     if ( (scn = System.getProperty (MBeanStartup.WT_SUPPORT_SCN)) != null )
     {
         SCN = scn;
     }

     String mailserver;
     if ( (mailserver = System.getProperty( MBeanStartup.MAIL_SMTP_HOST)) != null )
     {
         mailServer = mailserver;
     }

     String mailport;
     if ( (mailport = System.getProperty( MBeanStartup.MAIL_SMTP_PORT)) != null )
     {
         mailPort = mailport;
     }

     if ( collectionPath == null )
     {
         buildCollectionPath ( );
     }
     return;
  }

  private static Map<String, Object> buildReturnMap(
          final boolean success,
          final String path,
          final String WDSpath,
          final String message,
          final String location,
          final String host) {
    //set up the status map
    final Map<String, Object> statusMap = new HashMap<String, Object>();
    statusMap.put(KEY_SUCCESS, success);
    if (path != null) {
      statusMap.put(KEY_PATH, path);
    }
    if (WDSpath != null) {
      statusMap.put(KEY_WDSPATH, WDSpath);
    }
    if (message != null) {
      statusMap.put(KEY_MESSAGE, message);
    }
    if (location != null) {
      statusMap.put(KEY_LOCATION, location);
    }
    if (host != null) {
      statusMap.put(KEY_HOST, host);
    }
    return statusMap;
  }

  public String copyDir(String sourcePath, String targetPath, boolean dontOverwrite)
          /* throws IOException */  {
      String rtn = null;
      String rtn2;
      boolean success;
      String copyFileRtn;
      File source = new File( sourcePath );
      File target = new File( targetPath );

      if (!source.exists()) {
          rtn = "copyDir error: source directory does not exist.";
          System.out.println( rtn );
          return(rtn);
      }
      if (source.isDirectory()) {
          if (target.exists() && !target.isDirectory() ){
              rtn = "copyDir error: target: " + targetPath + " exists, but is not a directory.";
              System.out.println (rtn);
              return (rtn);
          }
          if (!target.exists()) {
              target.mkdirs();
          }
          String[] children = source.list();
          for ( String child: children ) {
              rtn2 = copyDir( sourcePath + File.separator + child ,
                       targetPath + File.separator + child , dontOverwrite );
              if( !rtn2.startsWith(SUCCESS) && rtn == null ){
                  rtn = rtn2;
              }
          }
          if( rtn == null ) {
              rtn = SUCCESS + ": copied " + source + " to " + target;
          }
      } else {
         rtn = copyFile( sourcePath, targetPath, dontOverwrite );
              // Capture the last error from copyFile.
//              if( !rtn2.startsWith(SUCCESS) ){
//                  rtn = rtn2;
//              }

      }
      return (rtn);
  }

  public String copyFile(String sourcePath, String targetPath, boolean dontOverwrite)
          /* throws IOException */  {
      String rtn = null;
      File source = new File( sourcePath );
      File target = new File( targetPath );

      if( !source.exists() ) {
          rtn = "copyFile error: source file " + sourcePath + " not found";
          System.out.println (rtn);
          return (rtn);
      }

      if (target.exists()){
          if (dontOverwrite) {
              rtn = "copyFile error: dontOverwrite flag set and destination file exists: " + targetPath;
              System.out.println (rtn);
              return (rtn);
          }
          if (target.isDirectory()) {
              rtn = "copyFile error: destination " + targetPath + " exists and is a directory";
              System.out.println (rtn);
              return (rtn);
          }
      }

      try {
          FileInputStream in = new FileInputStream( source );
          FileOutputStream out = new FileOutputStream( target );

          byte buffer[] = new byte[8192];
          int len;
          while ((len = in.read(buffer)) > 0) {
              out.write (buffer, 0, len );
          }
          in.close();
          out.close();
      } catch (/*FileNotFoundException nfe, */ IOException ioe ){
          System.out.println("copyFile exception: " + ioe.getMessage());
          ioe.printStackTrace();
          rtn = "copyFile exception: " + ioe.getMessage();
      }

      if( rtn == null ) {
          rtn = SUCCESS + ": copied file: " + source + " to " + target;
      }

      return (rtn);
  }

  /** Get SCN.
   *  <p>
   *
   *  SCN - Submitter Contract Number.  Required to send materials to PTC support.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *   @return String: the submitter Contract Number.
   *
   */
   public String getSCN( )
   {
      return SCN;
   }

  /** Set SCN.
   *  <p>
   *
   *  SCN - Submitter Contract Number.  Required to send materials to PTC support.
   *
   *  The SCN is initially set from environment variables or System Properties.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param scn Submitter Contract Number
   */
  public void setSCN( String scn )
  {
      if( scn != null )
      {
          System.setProperty (MBeanStartup.WT_SUPPORT_SCN, scn);
          SCN = scn;
      }

      return;
  }

  /** Get Mail Server.
   *  <p>
   *
   *  Mail Server -- Needed to email materials to PTC support.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @return String: the Mail Server Host.
   */
  public String getMailServer( )
  {
      return (mailServer);
  }

  /** Set Mail Server.
   *
   *  <p>
   *
   *  Mail Server -- Needed to email materials to PTC support.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *  @param mailserver mailserver host.
   */
  public void setMailServer( String mailserver )
  {
      if( mailserver != null )
      {
          System.setProperty( MBeanStartup.MAIL_SMTP_HOST, mailserver);
          mailServer = mailserver;
      }
      return;
  }

  /** Get Mail Server Port number.
   *
   *  <p>
   *
   *  Mail server port number used to send emails with support materials to PTC support.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @return String: Mail Server port
   */
  public String getMailServerPort( )
  {
      return( mailPort );
  }

  /** Set Mail Server Port number.
   *  <p>
   *
   *  Mail server port number used to send emails with support materials to PTC support.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param portNumber of Mail Server.
   */
  public void setMailServerPort( String portNumber )
  {
      if( portNumber != null)
      {
          System.setProperty( MBeanStartup.MAIL_SMTP_PORT, portNumber);
          mailPort = portNumber;
      }
      return;
  }

  /** Get path components.
   *  <p>
   *
   *  Path components -- the relative directory path components to be used for data collection.
   *  The path is returned as a String array comprised of relative
   *  directory names starting at the installation directory of the Windchill
   *  Directory Server.  It is returned this way to avoid including
   *  any path file separators, since Windchill and the Windchill
   *  Directory Server may be on systems of different architecture.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @return String Array of path components for collection directory.
   *
   */
  public String[] getPathComponents( )
  {
      return (pathComponents);
  }

  /** Set path components.
   *  <p>
   *
   *  Path components -- the relative directory path components to be used for data collection
   *  for support.  This path starts under the home directory for the Windchill
   *  Directory Server. Each directory component is passed as a separate
   *  array element to avoid issues with file separators.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param pathcomponents of parent directory for data collection path
   */
  public void setPathComponents( String[] pathcomponents )
  {
      pathComponents = pathcomponents;
      return;
  }

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
   *    @return String giving relative path name of collection directory
   */
  public String getCollectionPath( )
  {
      return ( collectionPath );
  }

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
  public void setCollectionPath( String collectionpath )
  {
      collectionPath = collectionpath;
  }

  /** Get absolute collection path.
   *  <p>
   *
   *  absolute collection path - the absolute (full) directory path to be used for data collection.
   *  The path is returned as a String.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @return String is full path name of data collection directory.
   */
  @MBeanOperationImpact( MBeanOperationInfo.INFO )
  public String getFullCollectionPath( )
  {
      return ( absoluteCollectionPath );
  }

  private static void buildCollectionPath ( )
  {
    String dirPath = "";

    for ( String pathComponent : pathComponents )
    {
      dirPath = dirPath + pathComponent + File.separator;
    }

    collectionPath = dirPath;
    absoluteCollectionPath = DSUtility.installDir + File.separator + dirPath;
  }

  private static String buildDirLocation ( String topicIdentifier )
  {
    buildCollectionPath ();

    String dirLocation = absoluteCollectionPath;

    if (topicIdentifier != null)
    {
        dirLocation = dirLocation + topicIdentifier + File.separator;
    }

    return ( dirLocation );
  }

  private static String buildFileBase (String dirLocation,
                                       String pluginType,
                                       String pathTimeStamp)
  {
    return dirLocation + pluginType + File.separator + pathTimeStamp +
              File.separator;
  }

  /** Collect.
   *  <p>
   *
   *  Collect Directory Data -- Collect the requested directory data.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param topicIdentifier identifies topic or call number
   *    @param maxAgeInDays of logs (if any)
   *    @param minAgeInDays of logs (if any)
   *    @param pathTimeStamp is collection pathTimeStamp and makes each collection unique
   *    @param pluginName is name of plugin type and used in directory naming
   *    @param relativeDirPath is relative directory path of parent directory containing collection data
   *    @return return map in format needed by the customer support SystemConfigurationCollector code.
   */
  public Map<String, Object> collect( String topicIdentifier,
          long maxAgeInDays, long minAgeInDays, String pathTimeStamp,
          String pluginName, String[] relativeDirPath )
  {
      Map<String, Object> results;
      String location;
      String locationAndHost;
      String fileBaseName;

      String rtn = null;

      String host = MBeanUtilities.getJvmId() + "@" + DSUtility.systemName;
//      System.out.println("MBeanUtilities.getJvmId(): " + MBeanUtilities.getJvmId() );

      if ( relativeDirPath != null )
      {
          setPathComponents ( relativeDirPath );
      }
      location = buildDirLocation (topicIdentifier);
      locationAndHost = DSUtility.systemName + ": " + location;

      fileBaseName = buildFileBase (location, pluginName, pathTimeStamp);

      if (pluginName.equalsIgnoreCase(PluginTypeLOG)){
          rtn = collectLogs ( fileBaseName );
      } else if (pluginName.equalsIgnoreCase(PluginTypeProperty)){
          rtn = collectProperties ( fileBaseName );
      } else if (pluginName.equalsIgnoreCase(PluginTypeUtility)){
          rtn = collectMBeanDump ( fileBaseName );
      }
      boolean success = rtn.startsWith(SUCCESS);

      results = buildReturnMap( success, null, fileBaseName,
                                rtn, locationAndHost, host );

      return results;
  }


  /** CollectMBeanDump.
   *  <p>
   *
   *  Collect the dump of all MBean data.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *    @param location gives full file path of directory to contain mbean dump.
   *    @return String: null if successful, Error information if not successful.
   */
  public String collectMBeanDump( String location )
  {
      String result;
      File locationFile = new File( location );

      if( location == null || !locationFile.isAbsolute()) {
          location = absoluteCollectionPath + location;
      }

      Object opParams[] = {location + File.separator + MBeanDumpName, null, false};
      String opSignature[] =
      {
          String.class.getName (),
          String.class.getName (),
          boolean.class.getName ()
      };

      try {
        ObjectName dumper = new ObjectName ("com.ptc:wt.subsystem=Dumper");
        MBeanServer mBeanServer = DirectoryServer.getJMXMBeanServer();
//        System.out.println ("returned from getJMXMBeanServer -- " +
//                            mBeanServer.toString());
        mBeanServer.invoke(dumper, "dumpAllMBeansToFile", opParams, opSignature);
        result = SUCCESS + ": MBean dump written to: " + location + File.separator + MBeanDumpName;
      } catch (Exception e) {
        result = "collectMBeanDump Exception on Dumper invoke: " + e.toString ();
        // Need to also log the error
      }

	  System.out.println (result);
      return result;
  }

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
  public String collectLogs( String location ) {
      String rtn = null;
      File locationFile = new File( location );

      if( location == null || !locationFile.isAbsolute()) {
          location = absoluteCollectionPath + location;
      }

      String logsPath = DSUtility.installDir + LogsDir;

      rtn = copyDir( logsPath, location + LogsDir, false );

      if( rtn.startsWith(SUCCESS) ){
          rtn = SUCCESS + ": all log files collected to: " + location;
      }

	  System.out.println (rtn);

      return (rtn);
  }

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
  public String collectProperties( String location ) {
      String rtn = null;
      String rtn2;
      File locationFile = new File( location );

      if( location == null || !locationFile.isAbsolute()) {
          location = absoluteCollectionPath + location;
      }

      /*
       * Copy WDSMBeanConfig.xml ( in classes directory )
       */
      String classesPath = DSUtility.installDir + ClassesDir;
      rtn2 = copyDir( classesPath, location + ClassesDir, false );
      if( !rtn2.startsWith(SUCCESS) && rtn == null ){
          rtn = rtn2;
      }

      /*
       * Copy selected files in the config directory.
       */
      String configPath = DSUtility.installDir + ConfigDir;
      String configDestPath = location + ConfigDir;
      String configLdifFile = File.separator + "config.ldif";
      String javaPropertiesFile = File.separator + "java.properties";
      String toolsPropertiesFile = File.separator + "tools.properties";
      String wdsMBeanConfigFile = File.separator + "WDSMBeanConfig.xml";

      rtn2 = copyDir( configPath + SchemaDir, configDestPath + SchemaDir, false );
      if( !rtn2.startsWith(SUCCESS) && rtn == null ){
          rtn = rtn2;
      }
      rtn2 = copyFile( configPath + configLdifFile, configDestPath + configLdifFile, false );
      if( !rtn2.startsWith(SUCCESS) && rtn == null ){
          rtn = rtn2;
      }
      rtn2 = copyFile( configPath + javaPropertiesFile, configDestPath + javaPropertiesFile, false );
      if( !rtn2.startsWith(SUCCESS) && rtn == null ){
          rtn = rtn2;
      }
      rtn2 = copyFile( configPath + toolsPropertiesFile, configDestPath + toolsPropertiesFile, false );
      if( !rtn2.startsWith(SUCCESS) && rtn == null ){
          rtn = rtn2;
      }
      rtn2 = copyFile( configPath + wdsMBeanConfigFile, configDestPath + wdsMBeanConfigFile, false );
      if( !rtn2.startsWith(SUCCESS) && rtn == null ){
          rtn = rtn2;
      }

      /*
       * Copy selected files in the lib directory.
       */
      String scriptUtilWinFile = File.separator + "_script-util.bat";
      String scriptUtilUnixFile = File.separator + "_script-util.sh";
      String libPath = DSUtility.installDir + LibDir;

      rtn2 = copyFile( libPath + scriptUtilWinFile, location + scriptUtilWinFile, false );
      if( !rtn2.startsWith(SUCCESS) && rtn == null ){
          rtn = rtn2;
      }
      rtn2 = copyFile( libPath + scriptUtilUnixFile, location + scriptUtilUnixFile, false );
      if( !rtn2.startsWith(SUCCESS) && rtn == null ){
          rtn = rtn2;
      }

      if (rtn == null) {
          rtn = SUCCESS + ": WindchillDS properties files collected to: " + location;
      }

      System.out.println (rtn);
      return (rtn);
  }

  /** Send
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
   *    @param sendComments
   *    @param uniqueId - can be ignored.
   *    @param scn - submitter contract number is required if sending data to support
   *    @param relativeDirPath is relative directory path of parent directory containing collection data
   *    @return return map in format needed by the customer support SystemConfigurationCollector code.
   */
  public Map<String, Object> send( String callNumberString, String topicDir,
          String dirToSend, String sendComments, String uniqueId,
          String scn, String[] relativeDirPath)
  {
      Map<String, Object> results;
      String location;
      String locationAndHost;
      String rtn;
      Long callNumber = 0L;
      String host = MBeanUtilities.getJvmId() + "@" + DSUtility.systemName;

      if ( SCN == null)
      {
          setSCN (scn);
      }

      if ( relativeDirPath != null )
      {
          setPathComponents ( relativeDirPath );
      }
      location = buildDirLocation( topicDir );
      locationAndHost = DSUtility.systemName + ": " + location;

      try {
          callNumber = Long.parseLong (callNumberString.trim());
      } catch ( NumberFormatException nfe ) {
          System.out.println ("NumberFormatException converting call number: " +
                  callNumberString);
      }

      rtn = sendDirectory( location, callNumber, sendComments );
      boolean success = rtn.startsWith(SUCCESS);

      results = buildReturnMap( success, null, location, rtn, locationAndHost, host );

      return results;
  }

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
   *    @return String: null if successful, Error information if not successful.
   */
  public String sendDirectory( String location,
          long callNumber, String description )
  {
      String result;

//	  System.out.println("sendDirectory called, location: " + location + " callNumber: " + callNumber + " description: " + description);

      File locationFile = new File( location );

      if( location == null || !locationFile.isAbsolute()) {
          location = absoluteCollectionPath + location;
          locationFile = new File( location );
      }

      if (!locationFile.exists()) {
		  result = "sendDirectory: Directory to send not found: " + location;
		  System.out.println (result);
		  return (result);
      }

      if (!locationFile.isDirectory()) {
		  result = "sendDirectory: path to send must be a directory: " + location;
		  System.out.println (result);
		  return (result);
      }

      Object opParams[] = {location, callNumber, description};
      String opSignature[] =
      {
          String.class.getName (),
          long.class.getName (),
          String.class.getName ()
      };

      try {
        ObjectName dumper = new ObjectName ("com.ptc:wt.subsystem=Dumper");
        MBeanServer mBeanServer = DirectoryServer.getJMXMBeanServer();
        mBeanServer.invoke(dumper, "sendDirToSupport", opParams, opSignature);
        result = SUCCESS + ": Directory " + location + " sent to support.";
        System.out.println (result);
      } catch (Exception e) {
        result = "sendDirectory Exception: " + e.toString ();
        System.out.println ("sendDirectory: Failure on call to Dumper: " + result );
        e.printStackTrace();
        // Need to also log the error
      }

      return result;
  }

  @Override
  public String[][]  getObjectNameSuffix()
  {
    return ( OBJECT_NAME_POSTFIX );
  }

}
