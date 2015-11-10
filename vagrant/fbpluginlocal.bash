#Bash Script to provide testdata and enable modified FB plugin
#the provided Facebook Plugin does not download but builds its backup from a given local src
#the expected structure: xml directory and files as downloaded by the FB Plugin 
#plugins expected data lookup location: /data/testdata/facebook_backup_input/facebook_backup_testdata

sudo mkdir -p /data/testdata/facebook_backup_input    
sudo cp -r /vagrant/facebook_backup_testdata/ /data/testdata/facebook_backup_input/
sudo chown -R tomcat7:tomcat7 /data/testdata
sudo rm -rf /data/testdata/facebook_backup_input/facebook_backup_testdata/html

#copy the provided/modified facebook plugin (no oauth, no download, data from local src) into osgi plugin dirs
sudo rm -rf /data/backmeup-service/plugins/facebook-2.0.0-SNAPSHOT.jar
sudo rm -rf /data/backmeup-worker/plugins/facebook-2.0.0-SNAPSHOT.jar
# wait until the backmeup service has undeployed the plugins
sleep 3
sudo cp /vagrant/facebook-2.0.0-SNAPSHOT.jar /data/backmeup-service/plugins/
sudo cp /vagrant/facebook-2.0.0-SNAPSHOT.jar /data/backmeup-worker/plugins/
 

