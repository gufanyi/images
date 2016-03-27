
一：安装jdk
rpm -ivh jdk-7u71-linux-x64.rpm;

二：修改./etc/profile中的内容

1.编辑/etc/profile文件，在文件中追加
export JAVA_HOME=/usr/java/jdk1.7.0_71
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar

2.直接通过命令行执行
echo "" >> /etc/profile;
echo 'export JAVA_HOME=/usr/java/jdk1.7.0_71' >> /etc/profile;
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/profile;
echo 'export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar' >> /etc/profile;
echo "" >> /etc/profile;
source /etc/profile;