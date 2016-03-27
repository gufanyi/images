#!/bin/bash
echo "" >> /etc/profile;
echo 'export JAVA_HOME=/usr/java/jdk1.7.0_71' >> /etc/profile;
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/profile;
echo 'export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar' >> /etc/profile;
echo "" >> /etc/profile;
source /etc/profile;