#!/bin/bash

# Change this to your prefered timezone
echo "Set timezone to Europe/Vienna"
timedatectl set-timezone Europe/Vienna
echo " "

#-------------------------------------------------------------------------------
echo "Add Elastic Search repository"
if [ ! -f "/etc/apt/sources.list.d/elasticsearch-1.4.list" ]
then
	wget -qO - https://packages.elastic.co/GPG-KEY-elasticsearch | apt-key add -
	echo "deb http://packages.elasticsearch.org/elasticsearch/1.4/debian stable main" | tee -a /etc/apt/sources.list.d/elasticsearch-1.4.list
fi
echo " "

#-------------------------------------------------------------------------------
echo "Update the system"
apt-get update
apt-get -y dist-upgrade
echo " "

#-------------------------------------------------------------------------------
echo "Install needed software"
apt-get -y install tomcat7 tomcat7-admin postgresql elasticsearch openjdk-7-jre maven git rabbitmq-server apache2

#-------------------------------------------------------------------------------
if [ -z "$(dpkg -l | grep "x-tika")" ]
then
	echo "Download x-tika package. This will take some time."
	#wget --progress=bar:force -O /tmp/x-tika_0.0.16_all.deb https://raw.githubusercontent.com/backmeup/backmeup-indexer/master/resources/tika/x-tika_0.0.16_all.deb
	wget --progress=bar:force -O /tmp/x-tika_0.0.16_all.deb https://github.com/backmeup/backmeup-indexer/blob/master/resources/tika/x-tika_0.0.16_all.deb?raw=true
	dpkg -i /tmp/x-tika_0.0.16_all.deb
fi
echo " "

#-------------------------------------------------------------------------------
echo "Install graphics magic for thumbnail plugin"
if [ ! -f "/etc/apt/sources.list.d/dhor-myway-trusty.list" ] 
then
	sudo add-apt-repository ppa:dhor/myway
	sudo apt-get update
fi
sudo apt-get -y install graphicsmagick
echo " "
	
#-------------------------------------------------------------------------------
echo "Set defaults"
if [ ! -d "/media/themis/" ]
then
	mkdir /media/themis/
fi

if [ ! -f "/etc/sudoers.d/themis" ]
then
	echo "tomcat7 ALL=(ALL:ALL) NOPASSWD: ALL" > /etc/sudoers.d/themis
	chmod 0440 /etc/sudoers.d/themis
fi
echo " "

#-------------------------------------------------------------------------------
echo "Database Stuff"
sudo -u postgres psql -c "CREATE USER dbu_core WITH PASSWORD 'dbu_core';"
sudo -u postgres psql -c "CREATE DATABASE bmucore WITH OWNER dbu_core TEMPLATE template0 ENCODING 'UTF8';"
sudo -u postgres psql -c "CREATE USER dbu_indexcore WITH PASSWORD 'dbu_indexcore';"
sudo -u postgres psql -c "CREATE DATABASE bmuindexcore  WITH OWNER dbu_indexcore TEMPLATE template0 ENCODING 'UTF8';"
sudo -u postgres psql -c "CREATE USER dbu_keysrv WITH PASSWORD 'dbu_keysrv';"
sudo -u postgres psql -c "CREATE DATABASE db_keysrv  WITH OWNER dbu_keysrv TEMPLATE template0 ENCODING 'UTF8';"
echo " "

#-------------------------------------------------------------------------------
# its not possible to auto install truecrypt
# we provide a bash script to build truecrypt from source in truecryptcompile.bash
#echo "Install truecrypt"
#cd /tmp
#wget https://www.grc.com/misc/truecrypt/truecrypt-7.1a-linux-console-x64.tar.gz
#tar -xvf truecrypt-7.1a-linux-console-x64.tar.gz
#./truecrypt-7.1a-setup-console-x64
#echo "done"

