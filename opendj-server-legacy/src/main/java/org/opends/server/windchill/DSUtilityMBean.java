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

import javax.management.MBeanOperationInfo;

import wt.jmx.annotations.MBeanOperationImpact;
import wt.jmx.core.mbeans.SelfEmailingMBean;


/** Provides methods to display and set properties and other utility functions.
 *
 *    <BR><BR><B>Supported API: </B>true
 *    <BR><BR><B>Extendable: </B>true
 */
public interface  DSUtilityMBean
  extends SelfEmailingMBean
{

  /** Display all System Property values.
   *  <p>
   *
   *  Display all System Property values.
   *
   *  Note that this operation is considered to have an ACTION_INFO (i.e. "read <i>and</i> write")
   *  impact even though it does not change any server state.  This is due to
   *  the fact that it can be used to view <i>any</i> property readable by the
   *  given process, including properties which may contain sensitive information
   *  such as passwords.  This operation is thus restricted to those users with
   *  the broadest possible access and privileges.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   */
  @MBeanOperationImpact( MBeanOperationInfo.ACTION_INFO )
  public String[]  displaySystemProperties( );

  /** Get all System Property values.
   *  <p>
   *
   *  Get all System Property values.
   *
   *  Note that this operation is considered to have an ACTION_INFO (i.e. "read <i>and</i> write")
   *  impact even though it does not change any server state.  This is due to
   *  the fact that it can be used to view <i>any</i> property readable by the
   *  given process, including properties which may contain sensitive information
   *  such as passwords.  This operation is thus restricted to those users with
   *  the broadest possible access and privileges.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   */
  @MBeanOperationImpact( MBeanOperationInfo.ACTION_INFO )
  public String[]  getSystemProperties( );

  /** Display Environment values.
   *  <p>
   *
   *  Display Environment values.
   *
   *  Note that this operation is considered to have an ACTION_INFO (i.e. "read <i>and</i> write")
   *  impact even though it does not change any server state.  This is due to
   *  the fact that it can be used to view <i>any</i> property readable by the
   *  given process, including properties which may contain sensitive information
   *  such as passwords.  This operation is thus restricted to those users with
   *  the broadest possible access and privileges.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   */
  @MBeanOperationImpact( MBeanOperationInfo.ACTION_INFO )
  public String[]  displayEnvVariables( );

  /** Get Environment values.
   *  <p>
   *
   *  Get Environment values.
   *
   *  Note that this operation is considered to have an ACTION_INFO (i.e. "read <i>and</i> write")
   *  impact even though it does not change any server state.  This is due to
   *  the fact that it can be used to view <i>any</i> property readable by the
   *  given process, including properties which may contain sensitive information
   *  such as passwords.  This operation is thus restricted to those users with
   *  the broadest possible access and privileges.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   */
  @MBeanOperationImpact( MBeanOperationInfo.ACTION_INFO )
  public String[]  getEnvVariables( );

  /** Set System Property value.
   *
   *  <p>
   *
   *  Set the specified System property (propertyName) to the specified value
   *  (propertyValue). Both parameters must be specified and must not be
   *  empty strings.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *  @param propertyName of property to set.
   *  @param propertyValue is the value to set it to.
   */
  @MBeanOperationImpact( MBeanOperationInfo.ACTION )
  public void  setSystemProperty( String propertyName, String propertyValue );

  /** Unset System Property value.
   *
   *  <p>
   *
   *  Unset the specified System property (propertyName).
   *  The propertyName parameter must be specified and must not be
   *  empty.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *  @param propertyName of property to set.
   */
  @MBeanOperationImpact( MBeanOperationInfo.ACTION )
  public void  unsetSystemProperty( String propertyName );

  /** Get System Property value.
   *  <p>
   *
   *  System property value for the specified key (propertyName).
   *  If propertyName is not specified or is the empty string, all
   *  System property values are returned.
   *
   *  Note that this operation is considered to have an ACTION_INFO (i.e. "read <i>and</i> write")
   *  impact even though it does not change any server state.  This is due to
   *  the fact that it can be used to view <i>any</i> property readable by the
   *  given process, including properties which may contain sensitive information
   *  such as passwords.  This operation is thus restricted to those users with
   *  the broadest possible access and privileges.
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   *  @param propertyName of System property to view.
   */
  @MBeanOperationImpact( MBeanOperationInfo.ACTION_INFO )
  public String[]  getSystemProperty( String propertyName );

  /** Display Directory Info.
   *  <p>
   *
   *  Display the Directory Server information summary also available via
   *  the command line start-ds -F.
   *
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   */
  public String[]  displayDSInfo( );

  /** Get Directory Info.
   *  <p>
   *
   *  Directory Server information summary also available via
   *  the command line start-ds -F.
   *
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   */
  public String[] getDSInfo( );

  /** Get Server Version.
   *  <p>
   *
   *  The Directory Server version string.
   *
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   */
  public String getServerVersion( );

  /** Get the Directory Server Build ID.
   *  <p>
   *
   *  The Directory Server Build ID.
   *
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   */
  public String getBuildID( );

  /** Get the Directory Installation Directory.
   *  <p>
   *
   *  The Directory Server installation directory.
   *
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   */
  public String getInstallDir( );

  /** Get Directory Instance Directory.
   *  <p>
   *
   *  The Directory Server Instance file directory.
   *
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   */
  public String getInstanceDir( );

  /** Get System Name.
   *  <p>
   *
   *  The system name.
   *
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   */
  public String getSystemName( );

  /** Get Operating system.
   *  <p>
   *
   *  Operating system info.
   *
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   */
  public String getOperatingSystem( );

  /** Get JVM Architecture string.
   *  <p>
   *
   *  JVM Architecture string.
   *
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   */
  public String getJVMArchitecture( );

  /** Get File Separator.
   *  <p>
   *
   *  File separator -- the file path separator used on this server
   *
   *
   *    <BR><BR><B>Supported API: </B>true
   *
   */
  public String getFileSeparator( );

}
