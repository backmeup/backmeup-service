sudo su

if [ ! -d "/repository/" ]
then
	mkdir /repository/
fi

if [ ! -d "/data/backmeup-service/" ]
then
	mkdir /data/backmeup-service/
fi

if [ ! -d "/data/backmeup-worker/" ]
then
	mkdir /data/backmeup-worker/
fi

if [ ! -d "/data/thumbnails/" ]
then
	mkdir /data/thumbnails/
fi

if [ ! -d "/data/index-core/" ]
then
	mkdir /data/index-core/
fi

git clone --branch v2ThemisFinal --depth 1 https://github.com/backmeup/backmeup-service.git /repository/backmeup/backmeup-service
git clone --branch v2ThemisFinal --depth 1 https://github.com/backmeup/backmeup-ui.git /repository/backmeup/backmeup-ui
git clone --branch v2ThemisFinal --depth 1 https://github.com/backmeup/backmeup-storage.git /repository/backmeup/backmeup-storage
git clone --branch v2ThemisFinal --depth 1 https://github.com/backmeup/backmeup-indexer.git /repository/backmeup/backmeup-indexer
git clone --branch v2ThemisFinal --depth 1 https://github.com/backmeup/backmeup-keyserver.git /repository/backmeup/backmeup-keyserver
git clone --branch v2ThemisFinal --depth 1 https://github.com/backmeup/backmeup-plugins.git /repository/backmeup/backmeup-plugins
git clone --branch v2ThemisFinal --depth 1 https://github.com/backmeup/backmeup-worker.git /repository/backmeup/backmeup-worker


cp -f /vagrant/mvn_template_settings.xml /etc/maven/settings.xml
cp -f /vagrant/tomcat_template_tomcat-users.xml /etc/tomcat7/tomcat-users.xml
 
#-------------------KEYSERVER DEPLOYMENT --------------------------------------------
# change keyserver config settings
cd /repository/backmeup/backmeup-keyserver/
sed -i "s?org.backmeup.keyserver.core.db.derby.DerbyDatabaseImpl?org.backmeup.keyserver.core.db.sql.SQLDatabaseImpl?g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
sed -i "s?org.apache.derby.jdbc.EmbeddedDriver?org.postgresql.Driver?g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
sed -i "s|jdbc:derby:keyserver;create=true|jdbc:postgresql:keyserver?user=keyserver_user&password=keyserver|g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
sed -i "s?REPLACE-SERVICE?p7HwZXJzYWTlIHT3SERVICE?g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
sed -i "s?REPLACE-WORKER?p7HwZXJzYWTlIHT3WORKER?g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
sed -i "s?REPLACE-INDEXER?p7HwZXJzYWTlIHT3INDEXER?g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
sed -i "s?REPLACE-STORAGE?p7HwZXJzYWTlIHT3STORAGE?g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
# move postgres properties to default properties
mv backmeup-keyserver-core/src/main/resources/backmeup-keyserver-sql-postgres.properties backmeup-keyserver-core/src/main/resources/backmeup-keyserver-sql.properties

#Trigger the build - use integrationTests target to deploy on local tomcat
mvn clean install -DskipTests -DintegrationTests

#-------------------INDEXER DEPLOYMENT--------------------------------------------
# change indexer pre-build config settings
# replace callback url in "backmeup-indexer_linux.properties"
cd /repository/backmeup/backmeup-indexer
#sed -i "s?backmeup.keyserver.baseUrl = http://localhost:8080/backmeup-keyserver-rest?backmeup.keyserver.baseUrl = http://themis-keysrv01:8080/backmeup-keyserver-rest?g" #backmeup-indexer-core/src/main/resources/backmeup-indexer_linux.properties
sed -i "s?backmeup.indexer.appSecret = REPLACE-INDEXER?backmeup.indexer.appSecret = 7HwZXJzYWTlIHT3INDEXER?g" backmeup-indexer-core/src/main/resources/backmeup-indexer_linux.properties
#sed -i "s?backmeup.storage.baseUrl = http://localhost:8080/backmeup-storage-service?backmeup.storage.baseUrl = http://themis-storage01.backmeup.at:8080/backmeup-storage-service?g" #backmeup-indexer-core/src/main/resources/backmeup-indexer_linux.properties
#sed -i "s?backmeup.indexer.rest.host = localhost?backmeup.indexer.rest.host = themis-dev01.backmeup.at?g" backmeup-indexer-client/src/main/resources/backmeup-index-client.properties

# if folder exist delete it (build will recrate the folder)
if [ -d "autodeploy" ]
then
 rm -r autodeploy
fi

#Trigger the build - use integrationTests target to deploy on local tomcat
mvn clean install -DskipTests -DintegrationTests

#Indexer post-build deployment steps (mainly osgi related)
cd autodeploy
# remove the old plugins from the osgi bundle pickup folders
for file in *.jar
do
  rm -f /data/backmeup-service/plugins/$file
  rm -f /data/backmeup-worker/plugins/$file
done
# wait until the backmeup service has undeploeyd all plugins
sleep 3
# copy the new build plugins to the osgi server
#rsync -v *.jar root@themis-dev01:/data/backmeup-service/plugins/
cp -f *.jar /data/backmeup-service/plugins/
cp -f *.jar /data/backmeup-worker/plugins/
# change the owner and group of the files to tomcat7
for file in *.jar
do
  chown tomcat7:tomcat7 /data/backmeup-service/plugins/$file
  chown tomcat7:tomcat7 /data/backmeup-worker/plugins/$file
done