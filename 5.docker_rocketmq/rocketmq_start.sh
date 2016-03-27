#!/bin/bash
#local_ip=`/sbin/ifconfig | grep 'inet'| grep -Ev '(127|117|211|172|::1|fe)' |awk '{print $2}'|head -n 1`
#echo $local_ip

#container_staus=`docker ps -a|grep -v "STATUS"|awk '{print $NF}'`
#expr index $container_staus  "UP"
#container_staus${container_staus///}

rm -rf /mnt/disk1/rocketmq/data
rm -rf /mnt/disk1/rocketmq/logs

echo 'get all start container name'
container_name0=`docker ps|grep -v "NAMES"|awk '{print $NF}'`
echo 'split all container name'
container_name0=${container_name0///} 


container_names=("namesvr_01" "namesvr_02" "brokersvr_01" "brokersvr_02")
container_ips=("192.168.4.43" "192.168.4.44" "192.168.4.45" "192.168.4.46")
gate_way="192.168.4.1"


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

#if [[ ! -x  "/usr/bin/git" ]]; then
#       echo 'install git'
#       yum install git;
#fi


if [[ ! -x "/usr/local/bin/pipework" ]]; then
  echo 'start install pipework'
   echo 'download pipework from github'
   git clone https://github.com/jpetazzo/pipework;
   echo 'add to path folder'
   cp ~/pipework/pipework /usr/local/bin/
fi


app_name="rocketmq"
docker_image_name="xap/rocketmq_base:0.1"
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

  #sudo docker exec -d  ${inner}   /usr/local/rocketmq/bin/os.sh

  #echo 'start ' ${inner}
  #index=$(awk 'BEGIN{ print index("'"${inner}"'","namesvr") }')

  #if [[ ${index}!=0 ]]; then
      #echo "start namesvr"
      #sudo docker exec -d  ${inner}   /usr/local/rocketmq/bin/mqnamesrv  -c /usr/local/rocketmq/conf/namesvr.p
  #fi
  #index=$(awk 'BEGIN{ print index("'"${inner}"'","brokersvr") }')
  #if [[ ${index}!=0 ]]; then
     #echo "start brokersvr"
     # sudo docker exec -d ${inner}   /usr/local/rocketmq/bin/mqbroker   -c /usr/local/rocketmq/conf/brokersvr.p
  #fi

  array_index=$(($array_index+1))
done



sudo docker exec -d  namesvr_01 /usr/local/rocketmq/bin/mqnamesrv  -c /usr/local/rocketmq/conf/namesvr.p
sudo docker exec -d  namesvr_02 /usr/local/rocketmq/bin/mqnamesrv  -c /usr/local/rocketmq/conf/namesvr.p
sudo docker exec -d  brokersvr_01 /usr/local/rocketmq/bin/mqbroker  -c /usr/local/rocketmq/conf/brokersvr.p
sudo docker exec -d  brokersvr_02 /usr/local/rocketmq/bin/mqbroker  -c /usr/local/rocketmq/conf/brokersvr.p
#sudo docker exec -d  brokersvr_03 /usr/local/rocketmq/bin/mqbroker   -c /usr/local/rocketmq/conf/brokersvr.p