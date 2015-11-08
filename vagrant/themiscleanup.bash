#CLEANUP
#-------------------------------------------------------------------------------
sudo service tomcat7 stop

echo "Drop DBs and start all over"
sudo -u postgres psql -c "DROP DATABASE bmucore;"
sudo -u postgres psql -c "CREATE DATABASE bmucore WITH OWNER dbu_core TEMPLATE template0 ENCODING 'UTF8';"
sudo -u postgres psql -c "DROP DATABASE bmuindexcore;"
sudo -u postgres psql -c "CREATE DATABASE bmuindexcore  WITH OWNER dbu_indexcore TEMPLATE template0 ENCODING 'UTF8';"
sudo -u postgres psql -c "DROP DATABASE db_keysrv;"
sudo -u postgres psql -c "CREATE DATABASE db_keysrv  WITH OWNER dbu_keysrv TEMPLATE template0 ENCODING 'UTF8';"
echo " "

echo "cleanup leftovers"
sudo rm -rf /var/log/tomcat7/backmeup*

sudo rm -rf /data/backmeup-service/cache/*
sudo rm -rf /data/backmeup-worker/cache/*
sudo rm -rf /data/backmeup-worker/work/*
sudo rm -rf /data/index-core/datasink/*
sudo rm -rf /data/thumbnails/*
sudo rm -rf /data/backmeup-storage/*

sudo service tomcat7 start