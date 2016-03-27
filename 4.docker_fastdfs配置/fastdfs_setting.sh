#!/bin/bash

host_data_volumn_path="/mnt/disk1/fdfs"
container_data_volumn_path="/mnt/disk1/fdfs"
conf_folder_name="fdfs"
data_folder_name="data"
gate_way="192.168.1.1"
container_trackers_names=("tracker_01" "tracker_02")
container_trackers_ips=("192.168.1.13" "192.168.1.14")

container_storage_group1=("storage_01" "storage_02")
container_storage_group2=("storage_03" "storage_04")
container_storages_names=("storage_01" "storage_02" "storage_03" "storage_04")
container_storage_ips=("192.168.1.15" "192.168.1.16" "192.168.1.17" "192.168.1.18")


declare -i varindex ;
varindex = 0 ;
#sed  '/tracker_server/!d;s/.*=//' storage.conf
# CFG=./config                             
# K=IMAGES_OUTPUT_DIR　
# V=/opt/images_out              
# sed -i "/^$K/c $K=$V" $CFG
#echo|sed 's/^/'"$RANDOM"'.rmvb_/g'
#sed -i 's#base_path=\/[^\/]*$#base_path='"${data_path}"''   ${conf_file} 
#sed 's/\/[^\/]*//'
#sed -i 's/^base_path\/[^\/]*$/base_path=haha/' ${conf_file}
# #base_path modfiy
# typeline=`cat ${conf_file} | awk -v str="base_path=" '/str/{print NR}'`
echo '--------start modify container_trackers_conf  file-------' 
for element in ${container_trackers_names[@]}; do
  
   conf_path=${host_data_volumn_path}/${conf_folder_name}/${element};
   conf_file=${conf_path}/tracker.conf
   data_path=${container_data_volumn_path}
   echo "modify bind_addr=${container_trackers_ips[$varindex]}  to  ${conf_file} "
   # ip address modify
   sed -i 's/bind_addr=[0-9]*.[0-9]*.[0-9]*.[0-9]*/bind_addr='"${container_trackers_ips[$varindex]}"'/'   ${conf_file} 
   
   deletestr=`sed -n '/^base_path.*\/[^\/]*$/p' ${conf_file}`
   echo "delete ${deletestr}  from  ${conf_file} "
   sed -i '/^base_path.*\/[^\/]*$/d'   ${conf_file} 

   echo "add base_path=${data_path} to ${conf_file} "
   sed -i '$a '"base_path=${data_path}"''  ${conf_file} 
   varindex=$(($varindex+1))
done

varindex=0;

echo '--------start modify container_storages_conf  file-------' 
for element in ${container_storages_names[@]}; do
   conf_path=${host_data_volumn_path}/${conf_folder_name}/${element}
   conf_file=${conf_path}/storage.conf
   data_path=${container_data_volumn_path}

   # ip address modify
   echo "modify bind_addr=${container_storage_ips[$varindex]}  to ${conf_file} "
   sed -i 's/bind_addr=[0-9]*.[0-9]*.[0-9]*.[0-9]*/bind_addr='"${container_storage_ips[$varindex]}"'/'   ${conf_file} 

   deletestr=`sed -n '/tracker_server=[0-9]*.[0-9]*.[0-9]*.[0-9]*/p' ${conf_file}`
   echo "delete ${deletestr}  from  ${conf_file} "
   # tracker_server ip address modfiy
   sed -i "/tracker_server=[0-9]*.[0-9]*.[0-9]*.[0-9]*/d"   ${conf_file}


   for inner in ${container_trackers_ips[@]}; do
      tracker_server_str="tracker_server="${inner}":22122"
      echo "add ${tracker_server_str} to ${conf_file} "
      sed -i '$a '"${tracker_server_str}"''  ${conf_file}
   done

   # base_path path modfiy
   deletestr=`sed -n '/^base_path.*\/[^\/]*$/p' ${conf_file}`
   echo "delete ${deletestr}  from  ${conf_file} "
   sed -i '/^base_path.*\/[^\/]*$/d'   ${conf_file} 

   echo "add base_path=${data_path} to ${conf_file} "
   sed -i '$a '"base_path=${data_path}"''  ${conf_file} 
    
   # store_path0 path modify
   deletestr=`sed -n '/^store_path0.*\/[^\/]*$/p' ${conf_file}`
   echo "delete ${deletestr}  from  ${conf_file} "
   sed -i '/^store_path0.*\/[^\/]*$/d'   ${conf_file} 

   echo "add store_path0=${data_path} to ${conf_file} "
   sed -i '$a '"store_path0=${data_path}"'' ${conf_file} 



   varindex=$(($varindex+1))
done

echo '--------start  modify mod_fastdfs.conf  file-------' 

varindex=0;
for element in ${container_storages_names[@]}; do
   conf_path=${host_data_volumn_path}/${conf_folder_name}/${element}
   conf_file=${conf_path}/mod_fastdfs.conf
   data_path=${container_data_volumn_path}

   # tracker_server ip address modfiy
   deletestr=`sed -n '/tracker_server=[0-9]*.[0-9]*.[0-9]*.[0-9]*/p' ${conf_file}`
   echo "delete ${deletestr}  from  ${conf_file} "
   sed -i "/tracker_server=[0-9]*.[0-9]*.[0-9]*.[0-9]*/d"   ${conf_file}

   for inner in ${container_trackers_ips[@]}; do
     tracker_server_str="tracker_server="${inner}":22122"
      echo "add ${tracker_server_str} to ${conf_file} "
      sed -i '$a '"${tracker_server_str}"''  ${conf_file}
   done

   # base_path path modfiy
   deletestr=`sed -n '/^base_path.*\/[^\/]*$/p' ${conf_file}`
   echo "delete ${deletestr}  from  ${conf_file} "
   sed -i '/^base_path.*\/[^\/]*$/d'   ${conf_file} 

   echo "add base_path=${data_path} to ${conf_file} "
   sed -i '$a '"base_path=${data_path}"''  ${conf_file} 
    
   # store_path0 path modify
   deletestr=`sed -n '/^store_path0.*\/[^\/]*$/p' ${conf_file}`
   echo "delete ${deletestr}  from  ${conf_file} "
   sed -i '/^store_path0.*\/[^\/]*$/d'   ${conf_file} 

   echo "add store_path0=${data_path} to ${conf_file} "
   sed -i '$a '"store_path0=${data_path}"'' ${conf_file} 

   varindex=$(($varindex+1))
done









