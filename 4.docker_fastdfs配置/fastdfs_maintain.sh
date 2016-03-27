sudo docker run -v /root/machine/fastdfs/tracker_01/conf:/fastdfs_run/conf \
-v /root/machine/fastdfs/tracker_01/data:/fastdfs_run/data \
-v /root/machine/fastdfs/tracker_01/logs:/fastdfs_run/logs \
-d -p 32771:22 -p 32881:22122 -p 32991:8080  --name=tracker_01 --privileged=true  xap/fastdfs_base:0.1;
docker exec tracker_01 /usr/bin/fdfs_trackerd /fastdfs_run/conf/tracker.conf start;
 
 sudo docker run -v /root/machine/fastdfs/tracker_02/conf:/fastdfs_run/conf \
-v /root/machine/fastdfs/tracker_02/data:/fastdfs_run/data \
-v /root/machine/fastdfs/tracker_02/logs:/fastdfs_run/logs \
-d -p 32772:22 -p 32882:22122 -p 32992:8080 --name=tracker_02 --privileged=true  xap/fastdfs_base:0.1;
/usr/bin/fdfs_trackerd /fastdfs_run/conf/tracker.conf start;

sudo docker run -v /root/machine/fastdfs/storage_01/conf:/fastdfs_run/conf \
-v /root/machine/fastdfs/storage_01/data:/fastdfs_run/data \
-v /root/machine/fastdfs/storage_01/logs:/fastdfs_run/logs \
-d -p 32773:22 -p 32883:23000 --name=storage_01 --privileged=true  xap/fastdfs_base:0.1;
/usr/bin/fdfs_storaged /fastdfs_run/conf/storage.conf start;

sudo docker run -v /root/machine/fastdfs/storage_02/conf:/fastdfs_run/conf \
-v /root/machine/fastdfs/storage_02/data:/fastdfs_run/data \
-v /root/machine/fastdfs/storage_02/logs:/fastdfs_run/logs \
-d -p 32774:22 -p 32884:23000 --name=storage_02 --privileged=true  xap/fastdfs_base:0.1;
/usr/bin/fdfs_storaged /fastdfs_run/conf/storage.conf start;

sudo docker run -v /root/machine/fastdfs/storage_03/conf:/fastdfs_run/conf \
-v /root/machine/fastdfs/storage_03/data:/fastdfs_run/data \
-v /root/machine/fastdfs/storage_03/logs:/fastdfs_run/logs \
-d -p 32775:22 -p 32885:23000 --name=storage_03 --privileged=true  xap/fastdfs_base:0.1;
/usr/bin/fdfs_storaged /fastdfs_run/conf/storage.conf start;


docker stop tracker_01;
docker stop tracker_02;
docker stop storage_01;
docker stop storage_02;
docker stop storage_03;

docker rm tracker_01;
docker rm tracker_02;
docker rm storage_01;
docker rm storage_02;
docker rm storage_03;

rm -rf /mnt/disk1/fdfs/data/tracker_01/;
rm -rf /mnt/disk1/fdfs/logs/tracker_01/;
rm -rf /mnt/disk1/fdfs/data/tracker_02/;
rm -rf /mnt/disk1/fdfs/logs/tracker_02/;
rm -rf /mnt/disk1/fdfs/data/storage_01/;
rm -rf /mnt/disk1/fdfs/logs/storage_01/;
rm -rf /mnt/disk1/fdfs/data/storage_02/;
rm -rf /mnt/disk1/fdfs/logs/storage_02/;
rm -rf /mnt/disk1/fdfs/data/storage_03/;
rm -rf /mnt/disk1/fdfs/logs/storage_03/;
rm -rf /mnt/disk1/fdfs/data/storage_04/;
rm -rf /mnt/disk1/fdfs/logs/storage_04/;