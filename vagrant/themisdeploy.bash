####################################README#################################################
# This batch file takes care of downloading, building and deploying all themis components #
###########################################################################################

if [ ! -d "/repository/" ]
then
	sudo mkdir /repository/
fi

if [ ! -d "/data/backmeup-service/" ]
then
	sudo mkdir -p /data/backmeup-service/plugins
fi

if [ ! -d "/data/backmeup-worker/" ]
then
	sudo mkdir -p /data/backmeup-worker/plugins
fi

if [ ! -d "/data/backmeup-worker/" ]
then
	sudo mkdir -p /data/backmeup-storage/
fi

if [ ! -d "/data/thumbnails/" ]
then
	sudo mkdir -p /data/thumbnails/
fi

if [ ! -d "/data/index-core/" ]
then
	sudo mkdir -p /data/index-core/
fi

git clone --branch v2ThemisFinal --depth 1 https://github.com/backmeup/backmeup-service.git /repository/backmeup/backmeup-service
git clone --branch v2ThemisFinal --depth 1 https://github.com/backmeup/backmeup-ui.git /repository/backmeup/backmeup-ui
git clone --branch v2ThemisFinal --depth 1 https://github.com/backmeup/backmeup-storage.git /repository/backmeup/backmeup-storage
git clone --branch v2ThemisFinal --depth 1 https://github.com/backmeup/backmeup-indexer.git /repository/backmeup/backmeup-indexer
git clone --branch v2ThemisFinal --depth 1 https://github.com/backmeup/backmeup-keyserver.git /repository/backmeup/backmeup-keyserver
git clone --branch v2ThemisFinal --depth 1 https://github.com/backmeup/backmeup-plugins.git /repository/backmeup/backmeup-plugins
git clone --branch v2ThemisFinal --depth 1 https://github.com/backmeup/backmeup-worker.git /repository/backmeup/backmeup-worker


sudo cp -f /vagrant/mvn_template_settings.xml /etc/maven/settings.xml
sudo cp -f /vagrant/tomcat_template_tomcat-users.xml /etc/tomcat7/tomcat-users.xml
sudo service tomcat7 restart 

#-------------------KEYSERVER CONFIGURATION --------------------------------------------
# change keyserver config settings
cd /repository/backmeup/backmeup-keyserver/
sudo git checkout .
sudo sed -i "s?org.backmeup.keyserver.core.db.derby.DerbyDatabaseImpl?org.backmeup.keyserver.core.db.sql.SQLDatabaseImpl?g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
sudo sed -i "s?org.apache.derby.jdbc.EmbeddedDriver?org.postgresql.Driver?g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
#sed -i "s|jdbc:derby:keyserver;create=true|jdbc:postgresql://themis-keysrv01.x/db_keysrv?user=dbu_keysrv\&password=pwdreplace|g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
sudo sed -i "s?jdbc:derby:keyserver;create=true?jdbc:postgresql://localhost/db_keysrv\?user=dbu_keysrv\&password=dbu_keysrv?g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
sudo sed -i "s?REPLACE-SERVICE?7HwZXJzYWTlIHT3678TGXSSW7SERVICE?g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
sudo sed -i "s?REPLACE-WORKER?7HwZXJzYWTlIHT3678TGXSSW78WORKER?g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
sudo sed -i "s?REPLACE-INDEXER?7HwZXJzYWTlIHT3678TGXSSW7INDEXER?g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
sudo sed -i "s?REPLACE-STORAGE?7HwZXJzYWTlIHT3678TGXSSW7STORAGE?g" backmeup-keyserver-core/src/main/resources/backmeup-keyserver.properties
# move postgres properties to default properties
sudo mv backmeup-keyserver-core/src/main/resources/backmeup-keyserver-sql-postgres.properties backmeup-keyserver-core/src/main/resources/backmeup-keyserver-sql.properties

#-------------------SERVICE CONFIGURATION --------------------------------------------
# change bmu service config settings
cd /repository/backmeup/backmeup-service/
sudo git checkout .
# replace callback url in "backmeup.properties"
#sed -i "s?backmeup.callbackUrl = ###REPLACE_ME###?backmeup.callbackUrl = http://themis-dev01.backmeup.at/page/create_backup_oAuthHandler.html?g" #backmeup-service-rest/src/main/resources/backmeup.properties
#sed -i "s?backmeup.keyserver.baseUrl = http://localhost:8080/backmeup-keyserver-rest?backmeup.keyserver.baseUrl = http://themis-keysrv01:8080/backmeup-keyserver-rest?g" #backmeup-service-rest/src/main/resources/backmeup.properties
sudo sed -i "s?backmeup.service.appSecret = REPLACE-SERVICE?backmeup.service.appSecret = 7HwZXJzYWTlIHT3678TGXSSW7SERVICE?g" backmeup-service-rest/src/main/resources/backmeup.properties

#-------------------INDEXER CONFIGURATION--------------------------------------------
# change indexer pre-build config settings
# replace callback url in "backmeup-indexer_linux.properties"
cd /repository/backmeup/backmeup-indexer
sudo git checkout .
#sed -i "s?backmeup.keyserver.baseUrl = http://localhost:8080/backmeup-keyserver-rest?backmeup.keyserver.baseUrl = http://themis-keysrv01:8080/backmeup-keyserver-rest?g" #backmeup-indexer-core/src/main/resources/backmeup-indexer_linux.properties
sudo sed -i "s?backmeup.indexer.appSecret = REPLACE-INDEXER?backmeup.indexer.appSecret = 7HwZXJzYWTlIHT3678TGXSSW7INDEXER?g" backmeup-indexer-core/src/main/resources/backmeup-indexer_linux.properties
#sed -i "s?backmeup.storage.baseUrl = http://localhost:8080/backmeup-storage-service?backmeup.storage.baseUrl = http://themis-storage01.backmeup.at:8080/backmeup-storage-service?g" #backmeup-indexer-core/src/main/resources/backmeup-indexer_linux.properties
#sed -i "s?backmeup.indexer.rest.host = localhost?backmeup.indexer.rest.host = themis-dev01.backmeup.at?g" backmeup-indexer-client/src/main/resources/backmeup-index-client.properties

#-------------------STORAGE CONFIGURATION--------------------------------------------
# replace properties in "backmeup-storage.properties"
cd /repository/backmeup/backmeup-storage
sudo git checkout .
#sed -i "s?backmeup.service.path = http://localhost:8080/backmeup-service-rest?backmeup.service.path = http://themis-dev01:8080/backmeup-service-rest?g" #backmeup-storage-service/src/main/resources-test/backmeup-storage.properties
#sed -i "s?backmeup.keyserver.baseUrl = http://localhost:8080/backmeup-keyserver-rest?backmeup.keyserver.baseUrl = http://themis-keysrv01:8080/backmeup-keyserver-rest?g" #backmeup-storage-service/src/main/resources-test/backmeup-storage.properties
sudo sed -i "s?backmeup.storage.appSecret = REPLACE-STORAGE?backmeup.storage.appSecret = 7HwZXJzYWTlIHT3678TGXSSW7STORAGE?g" backmeup-storage-service/src/main/resources-test/backmeup-storage.properties

#-------------------WORKER CONFIGURATION--------------------------------------------
# replace properties in "backmeup-worker.properties"
cd /repository/backmeup/backmeup-worker
sudo git checkout .
#sed -i "s?backmeup.keyserver.baseUrl = http://localhost:8080/backmeup-keyserver-rest?backmeup.keyserver.baseUrl = http://themis-keysrv01:8080/backmeup-keyserver-rest?g" #backmeup-worker-app-servlet/src/main/resources/backmeup-worker.properties
sudo sed -i "s?backmeup.worker.appSecret = REPLACE-WORKER?backmeup.worker.appSecret = HwZXJzYWTlIHT3678TGXSSW78WORKER?g" backmeup-worker-app-servlet/src/main/resources/backmeup-worker.properties

#-------------------PLUGIN CONFIGURATION--------------------------------------------
cd /repository/backmeup/backmeup-plugins
sudo git checkout .
# replace credentials in "facebook.properties"
sudo sed -i "s?app.key = enterkeyhere?app.key = 508411825969983?g" backmeup-facebook-plugin/src/main/resources/facebook.properties
sudo sed -i "s?app.secret = entersecretkeyhere?app.secret = ###ADDAPPSECRETHERE###?g" backmeup-facebook-plugin/src/main/resources/facebook.properties

# replace credentials in "dropbox.properties"
sudo sed -i "s?app.key = bitte ersetzen?app.key = 4pheai3cd0btkd5?g" backmeup-dropbox-plugin/src/main/resources/dropbox.properties
sudo sed -i "s?app.secret = bitte ersetzen?app.secret = ###ADDAPPSECRETHERE###?g" backmeup-dropbox-plugin/src/main/resources/dropbox.properties

# replace credentials in "storage.properties"
#sed -i "s?storage.url = http://localhost:8080/backmeup-storage-service/?storage.url = http://themis-storage01.backmeup.at:8080/backmeup-storage-service/?g" #backmeup-storage-plugin/src/main/resources/storage.properties

#-------------------UI CONFIGURATION--------------------------------------------
cd /repository/backmeup/backmeup-ui
sudo git checkout .
# replace host and port in "plugin.WebServiceClient.json"
sudo sed -i "s?themis-dev01.backmeup.at?localhost?g" app.extern.gtn.themis/www_debug/js/plugin/plugin.WebServiceClient.json
sudo sed -i "s? 80,? 8080,?g" app.extern.gtn.themis/www_debug/js/plugin/plugin.WebServiceClient.json


####################### NOTES REGARDING DEPLOYMENT ############################
# Note Themis dependency chains have changed during the project so for the initial compilation
# a workaround is required to provide all required 3rd party and themis jars in maven m2 repo:
# A) Backmeup Keyserver - DONE
# B) Backmeup Service 
#  ->Service Core missing indexer-client, indexer-model
# C) Backmeup Indexer
#  -> Indexer Core missing storage-client
# D) Backmeup Storage 
#  -> storage client ok 
#  -> storage service fails missing indexer-client
# E) Backmeup Indexer - DONE
# F) Backmeup Service - DONE
# G) Backmeup Storage - DONE
# H) Backmeup Worker - DONE
# I) Backmeup Plugins - DONE
# J) Backmeup UI - DONE
##############################################################################
#------------------------ DEPLOYMET ---------------------------------
#Note: use -DintegrationTests target to deploy on local tomcat
#A) Keyserver Deployment
cd /repository/backmeup/backmeup-keyserver/
sudo mvn clean install -DskipTests -DintegrationTests

#-------------------------
#B) Service
cd /repository/backmeup/backmeup-service/
sudo mvn clean install -DskipTests

#C) Indexer
cd /repository/backmeup/backmeup-indexer
sudo mvn clean install -DskipTests

#D) Storage
cd /repository/backmeup/backmeup-storage
sudo mvn clean install -DskipTests

#-------------------------
#E) Indexer Deployment
cd /repository/backmeup/backmeup-indexer
# if folder exist delete it (build will recrate the folder)
if [ -d "autodeploy" ]
then
 sudo rm -r autodeploy
fi

#Trigger the build - use integrationTests target to deploy on local tomcat
sudo mvn clean install -DskipTests -DintegrationTests

#Indexer post-build deployment steps (osgi bundle related)
cd autodeploy
# remove the old plugins from the osgi bundle pickup folders
for file in *.jar
do
  sudo rm -f /data/backmeup-service/plugins/$file
  sudo rm -f /data/backmeup-worker/plugins/$file
done
# wait until the backmeup service has undeploeyd all plugins
sleep 3
# copy the new build plugins to the osgi server
#rsync -v *.jar root@themis-dev01:/data/backmeup-service/plugins/
sudo cp -f *.jar /data/backmeup-service/plugins/
sudo cp -f *.jar /data/backmeup-worker/plugins/
# change the owner and group of the files to tomcat7
for file in *.jar
do
  sudo chown tomcat7:tomcat7 /data/backmeup-service/plugins/$file
  sudo chown tomcat7:tomcat7 /data/backmeup-worker/plugins/$file
done

#-------------------------
#F) Service Deployment
cd /repository/backmeup/backmeup-service
# if folder exist delete it (build will recrate the folder)
if [ -d "autodeploy" ]
then
 sudo rm -r autodeploy
fi

#Trigger the build - use integrationTests target to deploy on local tomcat
sudo mvn clean install -DskipTests -DintegrationTests

#Service post-build deployment steps (osgi bundle related)
cd autodeploy
# remove the old plugins from the osgi bundle pickup folders
for file in *.jar
do
  sudo rm -f /data/backmeup-service/plugins/$file
  sudo rm -f /data/backmeup-worker/plugins/$file
done
# wait until the backmeup service has undeploeyd all plugins
sleep 3
# copy the new build plugins to the osgi server
#rsync -v *.jar root@themis-dev01:/data/backmeup-service/plugins/
sudo cp -f *.jar /data/backmeup-service/plugins/
sudo cp -f *.jar /data/backmeup-worker/plugins/
# change the owner and group of the files to tomcat7
for file in *.jar
do
  sudo chown tomcat7:tomcat7 /data/backmeup-service/plugins/$file
  sudo chown tomcat7:tomcat7 /data/backmeup-worker/plugins/$file
done

#-------------------------
#G) Storage Deployment
cd /repository/backmeup/backmeup-storage
# if folder exist delete it (build will recrate the folder)
if [ -d "autodeploy" ]
then
 sudo rm -r autodeploy
fi

#Trigger the build - use integrationTests target to deploy on local tomcat
sudo mvn clean install -DskipTests -DintegrationTests

#Storage post-build deployment steps (osgi bundle related)
cd autodeploy
# remove the old plugins from the osgi bundle pickup folders
for file in *.jar
do
  sudo rm -f /data/backmeup-service/plugins/$file
  sudo rm -f /data/backmeup-worker/plugins/$file
done
# wait until the backmeup service has undeploeyd all plugins
sleep 3
# copy the new build plugins to the osgi server
#rsync -v *.jar root@themis-dev01:/data/backmeup-service/plugins/
sudo cp -f *.jar /data/backmeup-service/plugins/
sudo cp -f *.jar /data/backmeup-worker/plugins/
# change the owner and group of the files to tomcat7
for file in *.jar
do
  sudo chown tomcat7:tomcat7 /data/backmeup-service/plugins/$file
  sudo chown tomcat7:tomcat7 /data/backmeup-worker/plugins/$file
done

#-------------------------
#H) Worker Deployment
cd /repository/backmeup/backmeup-worker
#Trigger the build - use integrationTests target to deploy on local tomcat
sudo mvn clean install -DskipTests -DintegrationTests

#-------------------------
#I) Plugin Deployment
cd /repository/backmeup/backmeup-plugins
# if folder exist delete it (build will recrate the folder)
if [ -d "autodeploy" ]
then
 sudo rm -r autodeploy
fi

#Trigger the build - use integrationTests target to deploy on local tomcat
sudo mvn clean install -DskipTests -DintegrationTests

#plugins post-build deployment steps (osgi bundle related)
cd autodeploy
# remove the old plugins from the osgi bundle pickup folders
for file in *.jar
do
  sudo rm -f /data/backmeup-service/plugins/$file
  sudo rm -f /data/backmeup-worker/plugins/$file
done
# wait until the backmeup service has undeploeyd all plugins
sleep 3
# copy the new build plugins to the osgi server
#rsync -v *.jar root@themis-dev01:/data/backmeup-service/plugins/
sudo cp -f *.jar /data/backmeup-service/plugins/
sudo cp -f *.jar /data/backmeup-worker/plugins/
# change the owner and group of the files to tomcat7
for file in *.jar
do
  sudo chown tomcat7:tomcat7 /data/backmeup-service/plugins/$file
  sudo chown tomcat7:tomcat7 /data/backmeup-worker/plugins/$file
done

#-------------------------
#H) UI Deployment
cd /repository/backmeup/backmeup-ui
#Trigger the build - use integrationTests target to deploy on local tomcat
sudo rm -rf /var/lib/tomcat7/webapps/ROOT*
sudo mvn clean install -DskipTests -DintegrationTests