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
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.management.NotCompliantMBeanException;

import org.opends.server.core.DirectoryServer;
import static org.opends.server.util.DynamicConstants.*;
import org.opends.server.util.DynamicConstants.*;

import com.sleepycat.je.JEVersion;

import wt.jmx.core.mbeans.*;
import wt.jmx.core.MBeanRegistry;
import wt.jmx.core.SelfAwareMBean;
import wt.log4j.LogR;

/** This MBean provides operations to display and set properties,
 *  and other utility functions.
 *
 *    <BR><BR><B>Supported API: </B>true
 *    <BR><BR><B>Extendable: </B>false
 */
public final class  DSUtility
  extends SelfAwareMBean
  implements DSUtilityMBean
{
  private static final String  CLASSNAME = DSUtility.class.getName();

  private static final String serverVersion = DirectoryServer.getVersionString();
  private static final String buildID = BUILD_ID;
  private static String currentDirectory = System.getProperty ("user.dir");
  public static String installDir;
  public static String instanceDir;
  private static final String operatingSystem = System.getProperty("os.name") + " " +
                                   System.getProperty("os.version") + " " +
                                   System.getProperty("os.arch");
  private static String jvmArchitecture;
  public static String systemName;
  private static final String fileSeparator = File.separator;

  private static String availableProcessors;
  private static String maxAvailableMemory;
  private static String currentUsedMemory;
  private static String currentFreeMemory;

  private static String [] DSInfo;

  private static String [] sysProps;

  private static String [] envVars;

  static
  {
    try
    {
      final BeanInfo  beanInfo = Introspector.getBeanInfo( DSUtility.class );
      beanInfo.getBeanDescriptor().setValue( "persistenceDelegate",
        new DefaultPersistenceDelegate()
        {
          @Override
          protected final void  initialize( final Class<?> type, final Object oldInstance, final Object newInstance, final Encoder out )
          {
            super.initialize( type, oldInstance, newInstance, out );
            initializeDSInfo ();
            updateSysProps ();
            updateEnvVars ();
          }
        }
      );
    }
    catch ( Exception e )
    {
      LogR.getLogger( CLASSNAME ).error( "Failed to set up JavaBeans persistence", e );
    }
  }

  public static final String  OBJECT_NAME_POSTFIX[][] = new String[][] { { MBeanRegistry.SUB_SYSTEM_PROP_KEY, "DSUtility" } };

  private static final ResourceBundle  RESOURCE = ResourceBundle.getBundle( mbeansResource.class.getName() );

  private static final String  STRING_CLASS_NAME = String.class.getName();

  public static DSUtility  newDSUtility()
  {
     try
    {
      return ( new DSUtility() );
    }
    catch ( NotCompliantMBeanException e )
    {
      throw new IllegalArgumentException( e );
    }
  }

  public  DSUtility()
    throws NotCompliantMBeanException
  {
    super( DSUtilityMBean.class );
    initializeDSInfo ();
    updateSysProps ();
    updateEnvVars ();
  }

  private static void initializeDSInfo ()
  {

      // Install Directory
     installDir = DirectoryServer.getServerRoot();
     try
     {
         installDir = new File(installDir).getCanonicalPath();
     }
     catch (Exception e) {}
     if ( installDir == null)
     {
         installDir = "Unknown installation directory";
     }

     // Instance Directory
     instanceDir = DirectoryServer.getInstanceRoot();
     try
     {
         instanceDir = new File(instanceDir).getCanonicalPath();
     }
     catch (Exception e) {}
     if ( instanceDir == null)
     {
         instanceDir = "Unknown instance directory";
     }

     // JVM Architecture
     String sunOsArchDataModel = System.getProperty("sun.arch.data.model");
     if ((sunOsArchDataModel != null) &&
         (! sunOsArchDataModel.toLowerCase().equals("unknown")))
     {
       jvmArchitecture = sunOsArchDataModel + "-bit";
     }
     else{
       jvmArchitecture = "unknown";
     }

     // System Name
     try {
       systemName = InetAddress.getLocalHost().getCanonicalHostName();
     }
     catch (Exception e) {
       systemName = "Unknown";
     }

     updateDSInfo();
  }

  private static void updateDSInfo()
  {
     availableProcessors =
             "" + Runtime.getRuntime().availableProcessors();
     maxAvailableMemory =
             "" + Runtime.getRuntime().maxMemory();
     currentUsedMemory =
             "" + Runtime.getRuntime().totalMemory();
     currentFreeMemory =
             "" + Runtime.getRuntime().freeMemory();

     DSInfo = generateDSInfo();
  }

  private static String [] generateDSInfo()
  {
     Vector <String> v = new Vector<String>();

     v.addElement( serverVersion );
     v.addElement( "Build ID: \t" + buildID );
     v.addElement( "JAVA Version: \t" + System.getProperty("java.version") );
     v.addElement( "JAVA Vendor: \t" + System.getProperty("java.vendor") );
     v.addElement( "JVM Version: \t" + System.getProperty("java.vm.version") );
     v.addElement( "JRM Vendor: \t" + System.getProperty("java.vm.vendor") );
     v.addElement( "JAVA Home: \t" + System.getProperty("java.home") );
     v.addElement( "Class Path: \t" + System.getProperty("java.class.path") );
     v.addElement( "JE Version: \t" + JEVersion.CURRENT_VERSION.toString() );
     v.addElement( "Current Directory: \t" + currentDirectory );
     v.addElement( "Installation Directory: \t" + installDir );
     v.addElement( "Instance Directory: \t" + instanceDir );
     v.addElement( "Operating System: \t" + operatingSystem );
     v.addElement( "JVM Architecture: \t" + jvmArchitecture );
     v.addElement( "System Name: \t" + systemName );

     v.addElement( "Available Processors: \t" + availableProcessors);
     v.addElement( "Max Available Memory: \t" + maxAvailableMemory);
     v.addElement( "Currently Used Memory: \t" + currentUsedMemory);
     v.addElement( "Currently Free Memory: \t" + currentFreeMemory);

     String [] rtnArray = v.toArray(new String[v.size()]);
     return rtnArray;
  }

  public String [] displayDSInfo()
  {
      updateDSInfo ();
      return DSInfo;
  }

  public String [] getDSInfo()
  {
      updateDSInfo ();
      return DSInfo;
  }

  private static void updateSysProps ()
  {
     Properties props;
     Vector <String> v = new Vector<String>();

     props = System.getProperties();
     Set <String> propNames =  props.stringPropertyNames();

     for ( String key : propNames )
     {
         String value = props.getProperty (key);
         String pair = key + " = " + value;
         v.addElement ( pair );
     }
     sysProps = v.toArray(new String[v.size()]);
  }

  public String[]  displaySystemProperties()
  {
     updateSysProps ();
     return sysProps;
  }

  public String[]  getSystemProperties()
  {
     updateSysProps ();
     return sysProps;
  }

  private static void updateEnvVars ()
  {
     Map<String,String> env;
     Vector <String> v = new Vector<String>();

     env = System.getenv();
     for (Iterator <String> i = env.keySet().iterator(); i.hasNext() ; )
     {
         String key = i.next();
         String value = System.getenv (key);
         String pair = key + " = " + value;
         v.addElement ( pair );
     }
     envVars = v.toArray(new String[v.size()]);
  }

  public String[]  displayEnvVariables()
  {
     updateEnvVars ();
     return envVars;
  }

  public String[]  getEnvVariables()
  {
     updateEnvVars ();
     return envVars;
  }

  public String getServerVersion()
  {
    return( serverVersion );
  }

  public String getBuildID()
  {
    return( buildID );
  }


  public String getInstallDir()
  {
    return( installDir );
  }


  public String getInstanceDir()
  {
    return( instanceDir );
  }


  public String getSystemName()
  {
    return( systemName );
  }


  public String getOperatingSystem()
  {
    return( operatingSystem );
  }


  public String getJVMArchitecture()
  {
    return( jvmArchitecture );
  }

  public String getFileSeparator()
  {
    return( fileSeparator );
  }

  public void  setSystemProperty( String propertyName, String propertyValue )
  {
     if ( propertyName != null && propertyName.length() != 0
             && propertyValue != null)
     {
        System.setProperty( propertyName, propertyValue);
     }
  }

  public void  unsetSystemProperty( String propertyName )
  {
     Properties props;

     if (propertyName != null && propertyName.length() != 0)
     {
        props = System.getProperties();
        props.remove( propertyName);
        System.setProperties (props);
     }
  }

  @Override
  public String[][]  getObjectNameSuffix()
  {
    return ( OBJECT_NAME_POSTFIX );
  }

  public String[]  getSystemProperty( String propertyName )
  {
     if ( propertyName != null && propertyName.length() != 0)
     {
	String [] rtnArray = {System.getProperty( propertyName )};
        return (rtnArray);
     }
	return displaySystemProperties();
  }

}
