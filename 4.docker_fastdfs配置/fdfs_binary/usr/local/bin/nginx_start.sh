#!/bin/bash
cd /etc/service
nohup runsv nginx > /mnt/disk1/logs/runit_nginx.log 2>&1 &



