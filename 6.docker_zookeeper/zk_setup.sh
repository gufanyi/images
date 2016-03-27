Zookeeper集群搭建

#安装libaio包
rpm -ivh libaio-0.3.107-10.el6.x86_64.rpm
#安装netcat依赖包
echo 'setup netcat dpendens'
yum install libstdc++.i686
echo 'setup netcat' 
rpm -ivh netcat-0.7.1-1.i386.rpm 
echo 'enter Downloads dir'
cd ~/Downloads
echo 'uncompress zookeeper'
tar -zxvf zookeeper-3.4.6.tar.gz
echo 'delete conf dir'
rm -rf conf 
echo  'mkdir storage dir'
mkdir /mnt/disk1/zookeeper/data
mkdir /mnt/disk1/zookeeper/logs
mkdir /mnt/disk1/zookeeper/conf
echo 'link storage dir'
ln -s /mnt/disk1/zookeeper/data /usr/local/zookeeper/
ln -s /mnt/disk1/zookeeper/conf /usr/local/zookeeper/
ls -s /mnt/disk1/zookeeper/logs /usr/local/zookeeper/



#1.zoo.cfg参数说明:

#tickTime=2000:
#tickTime这个时间是作为Zookeeper服务器之间或客户端与服务器之间维持心跳的时间间隔,也就是每个tickTime时间就会发送一个心跳；

# initLimit=10:
# initLimit这个配置项是用来配置Zookeeper接受客户端
# （这里所说的客户端不是用户连接Zookeeper服务器的客户端,而是Zookeeper服务器集群中连接到Leader的Follower 服务器）
# 初始化连接时最长能忍受多少个心跳时间间隔数。
# 当已经超过10个心跳的时间（也就是tickTime）长度后 
# Zookeeper 服务器还没有收到客户端的返回信息,那么表明这个客户端连接失败。总的时间长度就是 10*2000=20 秒；

# syncLimit=5:
# syncLimit这个配置项标识Leader与Follower之间发送消息,请求和应答时间长度,最长不能超过多少个tickTime的时间长度,
# 总的时间长度就是5*2000=10秒；

# dataDir=/mnt/disk1/zookeeper/data
# dataDir顾名思义就是Zookeeper保存数据的目录,默认情况下Zookeeper将写数据的日志文件也保存在这个目录里；

# clientPort=2181
# clientPort这个端口就是客户端连接Zookeeper服务器的端口,Zookeeper会监听这个端口接受客户端的访问请求；

# server.1=localhost:2887:3887
# server.2=localhost:2888:3888
# server.3=localhost:2889:3889
# server.A=B：C：D：
# A是一个数字,表示这个是第几号服务器,B是这个服务器的ip地址
# C第一个端口用来集群成员的信息交换,表示的是这个服务器与集群中的Leader服务器交换信息的端口
# D是在leader挂掉时专门用来进行选举leader所用

# 2、创建ServerID标识

# 除了修改zoo.cfg配置文件,集群模式下还要配置一个文件myid,这个文件在dataDir目录下,
# 这个文件里面就有一个数据就是A的值,在上面配置文件中zoo.cfg中配置的dataDir路径中创建myid文件
# echo '1'> /mnt/disk1/zookeeper/data/myid

# 3、启动zookeeper

# /usr/local/zookeeper/bin/zkServer.sh start


# 4、检测集群是否启动

# echo stat|nc localhost 2181
# #或者
# /usr/local/zookeeper/bin/zkCli.sh


# 5.伪集群部署注意事项:

# 在一台机器上部署了3个server；需要注意的是clientPort这个端口如果在1台机器上部署多个server,那么每台机器都要不同的clientPort.

# 比如 server.1是2181,server.2是2182,server.3是2183

# 最后几行唯一需要注意的地方就是

# server.X 这个数字就是对应 data/myid中的数字。你在3个server的myid文件中分别写入了1,2,3,
# 那么每个server中的zoo.cfg都配 server.1,server.2,server.3就OK了