#!/bin/bash
docker start mysql_lui
pipework docker0 mysql_lui 192.168.2.2/24@192.168.2.2
docker exec mysql/usr/share/mysql/mysql.server start