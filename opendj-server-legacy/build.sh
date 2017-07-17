#!/bin/sh
NAME="WindchillDS"
FULLNAME="PTC Windchill Directory Server"
LOWERCASENAME="windchillds"
DOCHOMEPAGE="https://support.ptc.com/appserver/cs/portal/"
DOCWIKIURL="http://support.ptc.com/cs/help/windchill_hc/wc110_hc/"
DOCGUIDEURL="http://support.ptc.com/view?im_dbkey=165655"
VERSIONQUALIFIER="Build_002"
mvn clean
mvn -Dproduct.name="$NAME" -Dproject.name="$FULLNAME" -DlowerCaseProductName="$LOWERCASENAME" -DdocHomepageUrl="$DOCHOMEPAGE" -DdocWikiUrl="$DOCWIKIURL" -DdocGuideRefUrl="$DOCGUIDEURL" -DdocGuideAdminUrl="$DOCGUIDEURL" -DparsedVersion.qualifier="$VERSIONQUALIFIER" -X install
