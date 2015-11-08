#!/bin/bash

echo "Compile truecrypt"

#Compile Truecrypt on Ubuntu: http://ubuntuforums.org/showthread.php?t=812350
#http://jankarres.de/2013/04/raspberry-pi-truecrypt-installieren-und-mounten/
cd /tmp
wget http://prdownloads.sourceforge.net/wxwindows/wxWidgets-2.8.12.tar.gz
wget https://github.com/DrWhax/truecrypt-archive/raw/master/TrueCrypt%207.1a%20Source.tar.gz
sudo apt-get -y install build-essential g++ nasm libfuse-dev wx-common wx2.8-headers libwxbase2.8-dev libwxsvg-dev libwxgtk2.8-0 libwxgtk2.8-dev

mkdir pkcs-header-files
cd pkcs-header-files
wget ftp://ftp.rsasecurity.com/pub/pkcs/pkcs-11/v2-20/*.h
cd ../

tar xf "TrueCrypt 7.1a Source.tar.gz"
tar xf wxWidgets-2.8.12.tar.gz

cd truecrypt-7.1a-source/
sudo make NOGUI=1 WX_ROOT=/tmp/wxWidgets-2.8.12 wxbuild

#build truecrypt
sudo make NOGUI=1 WXSTATIC=1 PKCS11_INC=/tmp/pkcs-header-files

sudo cp Main/truecrypt /usr/bin
sudo chmod +x /usr/bin/truecrypt

echo " "