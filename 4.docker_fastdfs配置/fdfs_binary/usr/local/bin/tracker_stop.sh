#!/bin/bash
cd /etc/service
num=$(ps aux|grep 'runsv tracker'|wc -l)
if [ $num -gt 0 ] ; then
  pid=$(ps -aux|grep 'runsv tracker'|head -1|awk '{print $2}')
  kill -9  $pid  
fi
/usr/local/bin/stop.sh /usr/bin/fdfs_trackerd




