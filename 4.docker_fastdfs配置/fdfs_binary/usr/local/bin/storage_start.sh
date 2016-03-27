#!/bin/bash
cd /etc/service
nohup runsv storage > /mnt/disk1/logs/runit_storage.log 2>&1 &


