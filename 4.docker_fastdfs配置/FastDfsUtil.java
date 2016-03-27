package xap.lui.core.util;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.csource.fastdfs.test.TestFastDfs;

import xap.lui.core.common.ContextResourceUtil;
import xap.lui.core.exception.LuiRuntimeException;

public class FastDfsUtil {

	public static String conf_filename = "fdfs_client.conf";
	public static String groupId = "group1";

	static {
		String appPath = ContextResourceUtil.getCurrentAppPath() + ContextResourceUtil.FileSeperator;
		appPath = appPath + ContextResourceUtil.FileSeperator + "WEB-INF" + ContextResourceUtil.FileSeperator;
		conf_filename = appPath + conf_filename;
		try {
			ClientGlobal.init(conf_filename);
		} catch (Throwable e) {
			throw new LuiRuntimeException(e.getMessage());
		}
	}

	public static void main(String args[]) {
		 TestFastDfs test = new TestFastDfs();
	}

	public static String[] upload(String localFileName, NameValuePair[] metas) {
		try {

			TrackerClient tracker = new TrackerClient();
			TrackerServer trackerServer = tracker.getConnection();
			StorageServer storageServer = null;
			StorageClient storageClient = new StorageClient(trackerServer, storageServer);
			String results[] = storageClient.upload_file(localFileName, null, metas);
			return results;

		} catch (Throwable e) {
			throw new LuiRuntimeException(e.getMessage());
		}
	}

	public static String[] upload(byte[] file_buff, String fileExtName, NameValuePair[] metas) {
		try {

			TrackerClient tracker = new TrackerClient();
			TrackerServer trackerServer = tracker.getConnection();
			StorageServer storageServer = null;
			StorageClient storageClient = new StorageClient(trackerServer, storageServer);
			if (fileExtName == null) {
				fileExtName = "txt";
			}
			String results[] = storageClient.upload_file(file_buff, fileExtName, metas);
			return results;

		} catch (Throwable e) {
			throw new LuiRuntimeException(e.getMessage());
		}
	}

	public static byte[] download(String groupName, String remoteFileName) {
		try {
			if (groupName == null) {
				groupName = groupId;
			}
			TrackerClient tracker = new TrackerClient();
			TrackerServer trackerServer = tracker.getConnection();
			StorageServer storageServer = null;
			StorageClient storageClient = new StorageClient(trackerServer, storageServer);
			byte[] b = storageClient.download_file(groupName, remoteFileName);
			return b;
		} catch (Throwable e) {
			throw new LuiRuntimeException(e.getMessage());
		}
	}

	public static FileInfo getFileInfo(String groupName, String remoteFileName) {
		try {
			if (groupName == null) {
				groupName = groupId;
			}
			TrackerClient tracker = new TrackerClient();
			TrackerServer trackerServer = tracker.getConnection();
			StorageServer storageServer = null;
			StorageClient storageClient = new StorageClient(trackerServer, storageServer);
			FileInfo fi = storageClient.get_file_info(groupName, remoteFileName);
			return fi;
		} catch (Throwable e) {
			throw new LuiRuntimeException(e.getMessage());
		}
	}

	public NameValuePair[] getFileMate(String groupName, String remoteFileName) {
		try {
			if (groupName == null) {
				groupName = groupId;
			}
			TrackerClient tracker = new TrackerClient();
			TrackerServer trackerServer = tracker.getConnection();
			StorageServer storageServer = null;

			StorageClient storageClient = new StorageClient(trackerServer, storageServer);
			NameValuePair nvps[] = storageClient.get_metadata(groupName, remoteFileName);
			return nvps;
		} catch (Throwable e) {
			throw new LuiRuntimeException(e.getMessage());
		}
	}

	public static boolean delete(String groupName, String remoteFileName) {
		try {
			if (groupName == null) {
				groupName = groupId;
			}
			TrackerClient tracker = new TrackerClient();
			TrackerServer trackerServer = tracker.getConnection();
			StorageServer storageServer = null;

			StorageClient storageClient = new StorageClient(trackerServer, storageServer);
			int i = storageClient.delete_file(groupName, remoteFileName);
			return i == 0 ? false : true;
		} catch (Throwable e) {
			throw new LuiRuntimeException(e.getMessage());
		}
	}
}