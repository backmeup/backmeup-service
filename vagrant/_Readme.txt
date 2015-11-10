------------------------------------------
Setup Themis using VAGRANT 
------------------------------------------
===========================
0) About: Vagrant ist eine freie- und Open-Source Ruby-Anwendung zum Erstellen und Verwalten von virtuellen Maschinen. Vagrant ermöglicht einfaches Deployment insbesondere in der Software- und Webentwicklung und dient als Wrapper zwischen Virtualisierungssoftware wie VirtualBox, VMware und Hyper-V und Software-Configuration-Management-Anwendungen beziehungsweise Systemkonfigurationswerkzeugen wie Chef, Saltstack und Puppet.

===========================
1) Install Vagrant & VirtualBox
Vagrant installer for your platform http://www.vagrantup.com/downloads [Tested with Vagrant 1.7.4]
Virtual Box: https://www.virtualbox.org/ [Tested with VirtualBox 5.0.6]

===========================
2) Run Themis Vagrant
The vagrant files are located within https://github.com/backmeup/backmeup-service/tree/master/vagrant 
Checkout this folder from github.
Open a shell and call ‘vagrant up’. This downloads a blank ubutnu 64-bit VM image and executes the setup steps which are required to provision a Themis VM.
This includes, downloading and installing all dependencies, 3rd party jars and building + configuring the Themis components.
After the setup is completed you can access the system on your host OS by calling:
localhost:8080

------------------------------------------------------
The provisioning steps provided within dependenciessetup.bash 
* elasticsearch
* tomcat7
* postgres
* openjdk
* maven
* git
* rabbitmq
* apache2
with the following configurations applied
* tomcat7 user with sudo access (NOPASSWD)
* database and database users setup

------------------------------------------------------
The provisioning steps provided within truecryptcompile.bash 
* checkout the truecrypt sources and build it, as there is no headless installer available

------------------------------------------------------
The provisioning steps provided within themisdeploy.bash 
* create all required directories
* change access rights and owner
* git clone of all Themis components (v2ThemisFinal tag)
* replace maven settings.xml, tomcat-users.xml and tomcat7 config files
* configure all Themis components
* deploy Themis components to Tomcat
* install tika.deb package
Note: does not deploy facebook and dropbox plugin due to missing externally reachable callbackURL on VM

------------------------------------------------------
The provisioning steps provided within themiscleanup.bash
Resets Themis as if it were a fresh installation
* Drop the database data
* Cleanup leftovers and created data on file system

------------------------------------------------------
The provisioning steps provided within fbpluginlocal.bash
Provides a modified version of the facebook plugin which can be used for demo purposes
* Removes OAuth
* Downloads the data provided within /vagrant/facebook_backup_testdata when calling the plugin
** expects the /xmldata as originally created by the Facebook Plugin

===========================
3)  Connecting to the VM
Call ‘vagrant ssh’  or in Windows use putty to connect to 127.0.0.1:2222
User: vagrant PW: vagrant
sudo su root
To terminate the VM call ‘vagrant halt’ to shutdown or ‘vagrant destroy’ to delete the VM

===========================
A) Notes
* the directory /vagrant gets mounted on the VM, it contains the vagrant .bash files etc. = shared directories between your OS and the VM use the 
config.vm.synced_folder "../data", "/vagrant_data" property

* At any time you can just open VirtualBox and check on the status of your VM.

* In order to keep the same VM around and not lose its state, use the vagrant suspend and vagrant resume commands. 
This will make your VM survive a host machine reboot with its state intact.

* call vagrant provision --provision-with themis_cleanup.bash themisdeploy.bash 
to reset your VM to a fresh instance of Themis on your WM
