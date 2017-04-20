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

import org.opends.server.core.DirectoryServer;

import wt.jmx.core.mbeans.MBeanLoader;
import wt.log4j.LogR;

/** Creates, registers, starts up and provides access to the mbean loader. */
public class  MBeanStartup
{
  /** Lock used when creating and registering the mbean loader. */
  private static final Object  lock = new Object();

  /** The mbean loader. */
  private static volatile MBeanLoader  mbeanLoader;

  public static final String WT_HOME = "wt.home";
  public static final String WDS_INSTALL_ROOT = "wt.windchillds.install.root";
  public static final String WDS_INSTANCE_ROOT = "wt.windchillds.install.root";
  public static final String WDS_LOGS_DIR = "wt.windchillds.logs.dir";
  public static final String WT_MAIL_HOST = "wt.mail.mailhost";
  public static final String MAIL_SMTP_HOST = "mail.smtp.host";
  public static final String MAIL_HOST = "mail.host";
  public static final String MAIL_SMTP_PORT = "mail.smtp.port";
  public static final String MAIL_PORT = "mail.port";
  public static final String WT_SUPPORT_SCN = "wt.support.scn";

  /**
   * Get the mbean loader.
   * @return The mbean loader. May be null.
   */
  public static MBeanLoader  getMBeanLoader()
  {
    return ( mbeanLoader );
  }

  /**
   *  set System Properties that can be later used by the windchill
   *  mbean code.
   */
  private void setSystemProperties ()
  {
    /*
     *  Set the System Property "wt.home" to the server installation root.
     *  This is done so utilities/SystemConfigurationCollector data will be
     *  placed in an appropriate location.  The starting location for the
     *  SystemConfigurationCollector directory is wt.home.
     *
     *  Also set wt.windchillds.home to the server instance root (or installation root) for
     *  later use by the DSLogsDirViewer.
     */
    String installDir = DirectoryServer.getServerRoot();
    String instanceDir = DirectoryServer.getInstanceRoot();
    String baseDir = "";
    String logsDir = "logs";
    try
    {
        installDir = new File(installDir).getCanonicalPath();
    }
    catch (Exception e) {}
    try
    {
        instanceDir = new File(instanceDir).getCanonicalPath();
    }
    catch (Exception e) {}
    if ( instanceDir != null)
    {
        baseDir = instanceDir;
        System.setProperty( WDS_INSTANCE_ROOT, instanceDir);
        if ( installDir == null )
        {
        	System.setProperty( WDS_INSTALL_ROOT, instanceDir);
        }
    }
    if ( installDir != null)
    {
        System.setProperty( WDS_INSTALL_ROOT, installDir);
        if ( instanceDir == null )
        {
        	baseDir = installDir;
        	System.setProperty( WDS_INSTANCE_ROOT, installDir);
        }
    }
    System.setProperty( WT_HOME, baseDir);
    System.setProperty( "wt_home", baseDir);

    if ( !baseDir.isEmpty() )
    {
		logsDir = baseDir + File.separator + logsDir;
    }
    System.setProperty( WDS_LOGS_DIR, logsDir );

    /*
     *  Check the environment for settings for wt.support.scn or
     *  mail host settings: wt.mail.mailhost, or mail.smtp.host or mail.host.
     *
     *  The Submitter Contract Number (SCN) is needed if any support materials
     *  are sent in to PTC support.  This code checks to see if the
     *  environment variable wt.support.scn is set and, if so, copies the
     *  value to the System properties.
     *
     *  The mail host is needed if any support materials are to be sent
     *  via email to PTC support.  This code checks these three environment
     *  variables (in the following order): wt.mail.mailhost,
     *  mail.smtp.host, mail.host.  The first one found set, if any are set,
     *  is copied into the System property wt.mail.mailhost for subsequent
     *  use by the mailing mbeans.
     */
    String SCN;
    if ((SCN = System.getenv(WT_SUPPORT_SCN)) != null)
    {
        System.setProperty (WT_SUPPORT_SCN, SCN);
    }

    String mailHost;
    if ((mailHost = System.getenv(WT_MAIL_HOST)) != null ||
        (mailHost = System.getenv(MAIL_SMTP_HOST)) != null ||
        (mailHost = System.getenv(MAIL_HOST)) != null)
    {
        System.setProperty (WT_MAIL_HOST, mailHost);
    }
    String mailPort;
    if ((mailPort = System.getenv(MAIL_SMTP_PORT)) != null ||
        (mailPort = System.getenv(MAIL_PORT)) != null)
    {
        System.setProperty (MAIL_SMTP_PORT, mailPort);
    }
  }

  /** Create, register and startup the server manager mbean loader. */
  public  MBeanStartup()
  {
    LogR.initProperties();

    setSystemProperties ();

    // the lock and null check are just being super defensive, i.e. there is really no need...
    synchronized ( lock )
    {
    	
      if ( mbeanLoader == null )
        // TO_DO: change first 2 args to be controlled by properties file ...
    	  System.out.println("Checking "+System.getProperty( WDS_INSTALL_ROOT)+File.separator+"config"+File.separator+"WDSMBeanConfig.xml");
    	  String configFile = "WDSMBeanConfig.xml";
    	  
    	  try{
	    	  File file = new File(System.getProperty( WDS_INSTALL_ROOT)+File.separator+"config"+File.separator+"WDSMBeanConfig.xml");
			  if(file.exists()) {
				  configFile = System.getProperty( WDS_INSTALL_ROOT)+"/config/WDSMBeanConfig.xml";
				  System.out.println("File fouund "+configFile);
			  }
    	  }catch(Exception e) {
    		  e.printStackTrace();
    	  }
    	  
        mbeanLoader = MBeanLoader.createAndRegisterMBeanLoader( configFile, 180, null, true );
    }
  }
}
