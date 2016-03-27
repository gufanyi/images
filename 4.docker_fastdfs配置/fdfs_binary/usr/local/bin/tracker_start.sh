#!/bin/bash
cd /etc/service
nohup runsv tracker > /mnt/disk1/logs/runit_tracker.log 2>&1 &