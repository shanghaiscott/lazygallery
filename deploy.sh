#!/bin/sh
PREFIX=/gm
WEBAPPS=${PREFIX}/web/common/webapps
JSPWIKIS="JSPWiki-2.8 PublicWiki-2.8"
DIST="dist/LazyGallery.jar"

for JSPWIKI in $JSPWIKIS; do
  cp $DIST ${WEBAPPS}/${JSPWIKI}/WEB-INF/lib
  chown -R apache:apache ${WEBAPPS}/${JSPWIKI}/WEB-INF/lib
done

# restart all tomcat's because too many contexts need to be touched
${PREFIX}/etc/init.d/tomcat-vhost restart
