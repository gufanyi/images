#!/bin/bash
cd /etc/service
pid=""
declare -i num
num=$(ps aux|grep 'runsv nginx'|wc -l)
if [ $num -gt 0 ] ; then
  pid=$(ps -aux|grep 'runsv nginx'|head -1|awk '{print $2}')
  kill -9  $pid  
fi
sleep 1
num=`ps -aux | grep 'nginx:'|wc -l`
if [ $num -gt 0 ] ; then
  /usr/local/nginx/sbin/nginx -s stop
fi











