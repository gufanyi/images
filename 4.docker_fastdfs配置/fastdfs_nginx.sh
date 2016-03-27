#!/bin/bash

echo 'download fastdfs-nginx-module'
wget https://github.com/happyfish100/fastdfs-nginx-module/archive/master.zip

echo 'decompression fastdfs-nginx-module'
unzip fastdfs-nginx-module-master.zip

echo 'download nginx 1.6.2 stable version'
wget http://nginx.org/download/nginx-1.6.2.tar.gz

echo 'decompression nginx tar file'
tar -zxvf nginx-1.6.2.tar.gz 

echo 'install pcre and pcre-devel for regex lib'
yum -y install pcre pcre-devel 

echo 'install zlib zlib-devel for compress'
yum -y install zlib zlib-devel 

echo 'install openssl and openssl--devel for security socket'
yum -y install openssl openssl--devel 

echo 'enter nginx-1.6.2'
cd nginx-1.6.2 

echo 'configure need to point add-module fastdfs-nginx-module'
./configure --prefix=/usr/local/nginx   --add-module=/fastdfs-nginx-module-master/src

echo 'compile and install'
make&&make install 

echo 'validate configure is corrent'
/usr/local/nginx/sbin/nginx -t 

echo 'start nginx'
/usr/local/nginx/sbin/nginx

echo 'restart nginx and reload'
/usr/local/nginx/sbin/nginx -s reload 


ln -s /mnt/disk1/fdfs/logs /usr/local/nginx
ln -s /mnt/disk1/fdfs/fdfs /usr/local/nginx
ln -s /mnt/disk1/fdfs/fdfs /etc/fdfs