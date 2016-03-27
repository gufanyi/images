#!/bin/bash
#local_ip=`/sbin/ifconfig | grep 'inet'| grep -Ev '(127|117|211|172|::1|fe)' |awk '{print $2}'|head -n 1`
#echo $local_ip

#container_staus=`docker ps -a|grep -v "STATUS"|awk '{print $NF}'`
#expr index $container_staus  "UP"
#container_staus${container_staus///}

echo 'get all start container name'
container_name0=`docker ps|grep -v "NAMES"|awk '{print $NF}'`
echo 'split all container name'
container_name0=${container_name0///} 

container_names=("zk_01" "zk_02" "zk_03")
container_ips=("192.168.5.33" "192.168.5.34" "192.168.5.35")
gate_way="192.168.5.1"


for element in ${container_name0[@]}   
do  
  for inner in ${container_names[@]}; do
     if [[ $element == $inner ]]; then
      echo 'stop container' $element 
       docker stop $element 
     fi
  done
done  

container_name1=`docker ps -a|grep -v "NAMES"|awk '{print $NF}'`
container_name1=${container_name1///} 

for element in ${container_name1[@]}   
do  
   for inner in ${container_names[@]}; do
    if [[ $element == $inner ]]; then
      echo 'remove container' $element
      docker rm $element
   fi
   done
done

if [[ ! -x  "/usr/bin/git" ]]; then
       echo 'install git'
       yum install git;
fi





#pipework_path=`which pipework`
#str_index=`awk 'BEGIN{ print index("/usr/local/bin/pipework","no pipework") }'`
#str_index=$(awk 'BEGIN{ print index("/usr/local/bin/pipework","no pipework") }')

if [[ ! -x "/usr/local/bin/pipework" ]]; then
  echo 'start install pipework'
   echo 'download pipework from github'
   git clone https://github.com/jpetazzo/pipework;
   echo 'add to path folder'
   cp ~/pipework/pipework /usr/local/bin/
fi


app_name="zookeeper"
docker_image_name="xap/zookeeper_base:0.1"
container_rundata_path="/mnt/disk1/"${app_name}
host_data_volumne_path="/mnt/disk1/"${app_name}
conf_floder="conf"
logs_floder="logs"
data_floder="data"



for inner in ${container_names[@]}; do
  echo "create container ${inner}"   
  sudo docker run -v ${host_data_volumne_path}/${conf_floder}/${inner}:${container_rundata_path}/${conf_floder} \
  -v ${host_data_volumne_path}/${data_floder}/${inner}:${container_rundata_path}/${data_floder} \
  -v ${host_data_volumne_path}/${logs_floder}/${inner}:${container_rundata_path}/${logs_floder} \
  -e JAVA_HOME=/usr/java/jdk1.7.0_71 \
  -d --net=none --name=$inner --privileged=true  $docker_image_name

  echo 'set '${inner}' ip_addr ' ${container_ips[array_index]}
  pipework docker0 ${inner} ${container_ips[array_index]}/24@${gate_way}

  echo 'start ' ${inner}

  
  sudo docker exec -d ${inner} /usr/local/zookeeper/bin/zkServer.sh  start
    
  array_index=$(($array_index+1))
done



