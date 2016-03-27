#!/bin/bash

echo 'install compile tools'
yum -y groupinstall 'Development Tools'
echo 'install wget'
yum -y install wget

echo 'install libfastcommon'
wget https://github.com/happyfish100/libfastcommon/archive/master.zip
unzip master.zip
cd libfastcommon-master
./make.sh
./make.sh install

echo 'install fastdfs'
wget  https://github.com/happyfish100/fastdfs/archive/V5.05.tar.gz
tar -zxvf V5.05.tar.gz 
cd fastdfs-5.05/
./make.sh
./make.sh install

echo 'setting soft link'
ln -s /mnt/disk1/fdfs/fdfs /etc/fdfs



# mv storage.conf.sample  storage.conf
# mv tracker.conf.sample tracker.conf
#  #/etc/fdfs/tracker.conf
#  base_path=/data/fdfs
# /usr/bin/fdfs_trackerd /etc/fdfs/tracker.conf start
# /etc/fdfs/storage.conf
# /usr/bin/fdfs_storaged /etc/fdfs/storage.conf start