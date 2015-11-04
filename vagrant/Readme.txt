------------------------------------------
Setup Themis using VAGRANT 
------------------------------------------
0) About: Vagrant ist eine freie- und Open-Source Ruby-Anwendung zum Erstellen und Verwalten von virtuellen Maschinen. Vagrant ermöglicht einfaches Deployment insbesondere in der Software- und Webentwicklung und dient als Wrapper zwischen Virtualisierungssoftware wie VirtualBox, VMware und Hyper-V und Software-Configuration-Management-Anwendungen beziehungsweise Systemkonfigurationswerkzeugen wie Chef, Saltstack und Puppet.

1) Install Vagrant & VirtualBox
Vagrant installer for your platform http://www.vagrantup.com/downloads [Tested with Vagrant 1.7.4]
Virtual Box: https://www.virtualbox.org/ [Tested with VirtualBox 5.0.6]

2) Run Themis Vagrant
The vagrant file is located within https://github.com/backmeup/backmeup-service/tree/master/vagrant 
Open a shell and call ‘vagrant up’. This downloads a blank ubutnu 64-bit VM image and executes the setup steps provided within bootstrap.bash 
After this you have a pre-configured VM containing the software dependencies 
* elasticsearch
* tomcat7
* postgres
* openjdk
* maven
* git
* rabbitmq
* apache2
* tika
with the following configurations applied
* tomcat7 user with sudo access (NOPASSWD)
* database and database users setup

3)  Connecting to the VM
Call ‘vagrant ssh’  or in Windows use putty to connect to 127.0.0.1:2222
User: vagrant PW: vagrant
sudo su root
To terminate the VM call ‘vagrant halt’ to shutdown or ‘vagrant destroy’ to delete the VM

A) Notes
* /vagrant gets mounted on the VM, it contains the vagrant bootstrap.bash  etc. to provide shared directories between your OS and the VM use the 
config.vm.synced_folder "../data", "/vagrant_data" property

* At any time you can just open VirtualBox and check on the status of your VM.

* In order to keep the same VM around and not lose its state, use the vagrant suspend and vagrant resume commands. This will make your VM survive a host machine reboot with its state intact.
