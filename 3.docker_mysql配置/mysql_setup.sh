一： mysql 快速安装

#关闭防火墙
systemctl stop firewalld.service
systemctl disable firewalld.service

#安装依赖
yum install -y perl-Module-Install.noarch 

#如果是主机安装，这个是需要的，卸载冲突库
rpm -qa | grep mariadb
rpm -e --nodeps mariadb-libs-5.5.35-3.el7.x86_64
yum remove mysql mysql-libs

#如果是容器安装，安装依赖包
rpm -ivh libaio-0.3.107-10.el6.x86_64.rpm

#安装mysql服务端
rpm -vih MySQL-server-5.6.24-1.linux_glibc2.5.x86_64.rpm 

#安装mysql客户端
rpm -vih MySQL-client-5.6.24-1.linux_glibc2.5.x86_64.rpm 

#主机启动mysql
/usr/share/mysql/mysql.server restart|start|stop

#容器启动mysql
/usr/share/mysql/mysql.server restart|start|stop

ln -s /mnt/disk1/cobar/conf /usr/local/cobar
ln -s /mnt/disk1/cobar/logs /usr/local/cobar

二：密码和访问权限设置

1.ssh客户端1 
/usr/bin/mysqld_safe --skip-grant-tables 

2.ssh客户端2 
mysql 
use mysql 
update user set password=password("11") where user="root"; 
flush privileges; 
exit; 
update user set host='%' where user='root' and host='localhost'; 
delete from user where host<>'%'; 
flush privileges; 
exit;  


3.重新启动mysql 
service mysql restart 
/usr/share/mysql/mysql.server restart
mysql -u root -p 
11 
set password=password("11"); 
grant all privileges on *.* to 'root'@'%' identified by '11' with grant option; 
flush privileges; 
exit; 
service mysql restart 

/usr/share/mysql/mysql.server restart



三：my.cnf配置 
1.优化配置 
innodb_buffer_pool_size=4056M 
innodb_log_file_size=1024M 
innodb_log_buffer_size=128M 
max_connections=1000 
2.字符集配置 
[client] 
port=3306 
default-character-set=utf8 
[mysqld] 
port=3306 
character-set-server=utf8 
[mysql] 
no-auto-rehash 
default-character-set=utf8 

四：数据还原
1.数据移动 
mv /var/lib/mysql/mysql_data/beiduoad /var/lib/mysql 
mv /var/lib/mysql/mysql_data/ibdata1 /var/lib/mysql 
2.文件授权 
chown -R mysql:mysql beiduoad 
chown mysql:mysql ibdata1 

service mysqld stop


cp -R /var/lib/mysql /mnt/disk1/
ln -s /mnt/disk1/mysql /var/lib/mysql
chown mysql:mysql /mnt/disk1/mysql -R
ln -s /mnt/disk1/cobar/conf /usr/local/cobar
ln -s /mnt/disk1/cobar/logs /usr/local/cobar



五：常见问题
1.如何mysql的初始化数据库初始化失败，请用下面的语句初始化 
/usr/local/mysql/bin/mysql_install_db --defaults-file=/data1/mysql_3308/my.cnf 
--datadir=/data1/mysql_3308/data 
2.如果建立不了连接，请用下面的语句建立连接 
ln -s /var/lib/mysql/mysql.sock /tmp/mysql.sock 




