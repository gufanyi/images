#!/bin/bash
cd /etc/service
declare -i num
num=$(ps aux|grep 'runsv storage'|wc -l)
if [ $num -gt 0 ] ; then
  pid=$(ps -aux|grep 'runsv storage'|head -1|awk '{print $2}')
  kill -9  $pid  
fi
/usr/local/bin/stop.sh /usr/bin/fdfs_storaged


