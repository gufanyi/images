#!/bin/bash

echo 'install pcre and pcre-devel for regex lib'
yum -y install pcre pcre-devel 

echo 'install zlib zlib-devel for compress'
yum -y install zlib zlib-devel 

echo 'install openssl and openssl--devel for security socket'
yum -y install openssl openssl--devel 

echo 'download nginx 1.6.2 stable version'
wget http://nginx.org/download/nginx-1.6.2.tar.gz

echo 'decompression nginx tar file'
tar -zxvf nginx-1.6.2.tar.gz 

echo 'enter nginx-1.6.2'
cd nginx-1.6.2 

echo 'configure'
./configure

echo 'compile and install'
make&&make install 

echo 'validate configure is corrent'
/usr/local/nginx/sbin/nginx -t  -c /usr/local/nginx/conf/nginx.conf

echo 'stop nginx'
kill -9 $( ps aux|nginx: master process|awk '{print $2}')

echo 'start nginx'
/usr/local/nginx/sbin/nginx -c /usr/local/nginx/conf/nginx.conf

echo 'restart nginx and reload'
/usr/local/nginx/sbin/nginx -s reload 

