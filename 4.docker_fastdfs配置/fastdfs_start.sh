#!/bin/bash
container_names=("tracker_01" "tracker_02" "storage_01" "storage_02" "storage_03" "storage_04")
container_trackers_names=("tracker_01" "tracker_02")
container_trackers_ips=("192.168.1.13" "192.168.1.14")
container_storages_names=("storage_01" "storage_02" "storage_03" "storage_04")
container_storage_ips=("192.168.1.15" "192.168.1.16" "192.168.1.17" "192.168.1.18")
gate_way="192.168.1.1"
app_name="fdfs"
docker_image_name="xap/fastdfs_base:0.1"
container_name0=`docker ps|grep -v "NAMES"|awk '{print $NF}'`
echo ${container_name0}
container_name0=${container_name0///} 

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



declare -i array_index
array_index=0;

host_data_volumne_path="/mnt/disk1/"${app_name}
container_rundata_path="/mnt/disk1/"


for inner in ${container_trackers_names[@]}; do
  echo 'create container tracker_01'  
  sudo docker run -v ${host_data_volumne_path}/conf/${inner}:${container_rundata_path}/conf \
  -v ${host_data_volumne_path}/data/${inner}:${container_rundata_path}/data \
  -v ${host_data_volumne_path}/logs/${inner}:${container_rundata_path}/logs \
  -d --net=none --name=$inner --privileged=true  $docker_image_name

  echo 'set '${inner}' ip_addr ' ${container_trackers_ips[array_index]}
  pipework docker0 ${inner} ${container_trackers_ips[array_index]}/24@${gate_way}

  echo 'start ' ${inner}
  sudo docker exec ${inner} /usr/local/bin/tracker_start.sh


    
  array_index=$(($array_index+1))
done


array_index=0;
for inner in ${container_storages_names[@]}; do
    echo "create container ${inner}" 
    sudo docker run -v ${host_data_volumne_path}/conf/${inner}:${container_rundata_path}/conf \
    -v ${host_data_volumne_path}/data/${inner}:${container_rundata_path}/data \
    -v ${host_data_volumne_path}/logs/${inner}:${container_rundata_path}/logs \
    -d  --net=none --name=$inner --privileged=true  $docker_image_name;
   
    echo 'set '${inner}' ip_addr ' ${container_storage_ips[array_index]}
    pipework docker0 ${inner} ${container_storage_ips[array_index]}/24@${gate_way}
    array_index=$(($array_index+1))

    echo 'start ' ${inner}
    docker exec ${inner}  /usr/local/bin/storage_start.sh
    echo 'start nginx'
    docker exec ${inner}  /usr/local/bin/nginx_start.sh
   
done


