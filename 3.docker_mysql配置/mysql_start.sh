#!/bin/bash
echo 'get all start container name'
container_name0=`docker ps|grep -v "NAMES"|awk '{print $NF}'`
echo 'split all container name'
container_name0=${container_name0///} 
container_names=("mysql_01" "mysql_02" "mysql_03" "mysql_04")
container_ips=("192.168.2.23" "192.168.2.24" "192.168.2.25" "192.168.2.26")
gate_way="192.168.2.1"


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

app_name="mysql"
docker_image_name="xap/mysql_base:0.1"
container_rundata_path="/mnt/disk1/"${app_name}
host_data_volumne_path="/mnt/disk1/"${app_name}
data_floder="data"
conf_floder="conf"
declare -i array_index
array_index=0

for inner in ${container_names[@]}; do
  echo "create container ${inner}"  
  sudo docker run -v ${host_data_volumne_path}/${data_floder}/${inner}:${container_rundata_path} \
  -d --net=none --name=$inner --privileged=true  $docker_image_name

  echo 'set '${inner}' ip_addr ' ${container_ips[array_index]}
  pipework docker0 ${inner} ${container_ips[array_index]}/24@${gate_way}

  echo 'start ' ${inner}
  sudo docker exec ${inner} /usr/share/mysql/mysql.server start
    
  array_index=$(($array_index+1))
done
