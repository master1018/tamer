public final class SyncService {
    private final static byte[] ID_OKAY = { 'O', 'K', 'A', 'Y' };
    private final static byte[] ID_FAIL = { 'F', 'A', 'I', 'L' };
    private final static byte[] ID_STAT = { 'S', 'T', 'A', 'T' };
    private final static byte[] ID_RECV = { 'R', 'E', 'C', 'V' };
    private final static byte[] ID_DATA = { 'D', 'A', 'T', 'A' };
    private final static byte[] ID_DONE = { 'D', 'O', 'N', 'E' };
    private final static byte[] ID_SEND = { 'S', 'E', 'N', 'D' };
    private final static NullSyncProgresMonitor sNullSyncProgressMonitor =
            new NullSyncProgresMonitor();
    private final static int S_ISOCK = 0xC000; 
    private final static int S_IFLNK = 0xA000; 
    private final static int S_IFREG = 0x8000; 
    private final static int S_IFBLK = 0x6000; 
    private final static int S_IFDIR = 0x4000; 
    private final static int S_IFCHR = 0x2000; 
    private final static int S_IFIFO = 0x1000; 
    private final static int SYNC_DATA_MAX = 64*1024;
    private final static int REMOTE_PATH_MAX_LENGTH = 1024;
    public static final int RESULT_OK = 0;
    public static final int RESULT_CANCELED = 1;
    public static final int RESULT_UNKNOWN_ERROR = 2;
    public static final int RESULT_CONNECTION_ERROR = 3;
    public static final int RESULT_NO_REMOTE_OBJECT = 4;
    public static final int RESULT_TARGET_IS_FILE = 5;
    public static final int RESULT_NO_DIR_TARGET = 6;
    public static final int RESULT_REMOTE_PATH_ENCODING = 7;
    public static final int RESULT_REMOTE_PATH_LENGTH = 8;
    public static final int RESULT_FILE_WRITE_ERROR = 9;
    public static final int RESULT_FILE_READ_ERROR = 10;
    public static final int RESULT_NO_LOCAL_FILE = 11;
    public static final int RESULT_LOCAL_IS_DIRECTORY = 12;
    public static final int RESULT_REMOTE_IS_FILE = 13;
    public static final int RESULT_BUFFER_OVERRUN = 14;
    public static class SyncResult {
        private int mCode;
        private String mMessage;
        SyncResult(int code, String message) {
            mCode = code;
            mMessage = message;
        }
        SyncResult(int code, Exception e) {
            this(code, e.getMessage());
        }
        SyncResult(int code) {
            this(code, errorCodeToString(code));
        }
        public int getCode() {
            return mCode;
        }
        public String getMessage() {
            return mMessage;
        }
    }
    public interface ISyncProgressMonitor {
        public void start(int totalWork);
        public void stop();
        public boolean isCanceled();
        public void startSubTask(String name);
        public void advance(int work);
    }
    private static class NullSyncProgresMonitor implements ISyncProgressMonitor {
        public void advance(int work) {
        }
        public boolean isCanceled() {
            return false;
        }
        public void start(int totalWork) {
        }
        public void startSubTask(String name) {
        }
        public void stop() {
        }
    }
    private InetSocketAddress mAddress;
    private Device mDevice;
    private SocketChannel mChannel;
    private byte[] mBuffer;
    SyncService(InetSocketAddress address, Device device) {
        mAddress = address;
        mDevice = device;
    }
    boolean openSync() throws IOException {
        try {
            mChannel = SocketChannel.open(mAddress);
            mChannel.configureBlocking(false);
            AdbHelper.setDevice(mChannel, mDevice);
            byte[] request = AdbHelper.formAdbRequest("sync:"); 
            AdbHelper.write(mChannel, request, -1, DdmPreferences.getTimeOut());
            AdbResponse resp = AdbHelper.readAdbResponse(mChannel, false );
            if (!resp.ioSuccess || !resp.okay) {
                Log.w("ddms",
                        "Got timeout or unhappy response from ADB sync req: "
                        + resp.message);
                mChannel.close();
                mChannel = null;
                return false;
            }
        } catch (IOException e) {
            if (mChannel != null) {
                try {
                    mChannel.close();
                } catch (IOException e2) {
                }
                mChannel = null;
            }
            throw e;
        }
        return true;
    }
    public void close() {
        if (mChannel != null) {
            try {
                mChannel.close();
            } catch (IOException e) {
            }
            mChannel = null;
        }
    }
    public static ISyncProgressMonitor getNullProgressMonitor() {
        return sNullSyncProgressMonitor;
    }
    private static String errorCodeToString(int code) {
        switch (code) {
            case RESULT_OK:
                return "Success.";
            case RESULT_CANCELED:
                return "Tranfert canceled by the user.";
            case RESULT_UNKNOWN_ERROR:
                return "Unknown Error.";
            case RESULT_CONNECTION_ERROR:
                return "Adb Connection Error.";
            case RESULT_NO_REMOTE_OBJECT:
                return "Remote object doesn't exist!";
            case RESULT_TARGET_IS_FILE:
                return "Target object is a file.";
            case RESULT_NO_DIR_TARGET:
                return "Target directory doesn't exist.";
            case RESULT_REMOTE_PATH_ENCODING:
                return "Remote Path encoding is not supported.";
            case RESULT_REMOTE_PATH_LENGTH:
                return "Remove path is too long.";
            case RESULT_FILE_WRITE_ERROR:
                return "Writing local file failed!";
            case RESULT_FILE_READ_ERROR:
                return "Reading local file failed!";
            case RESULT_NO_LOCAL_FILE:
                return "Local file doesn't exist.";
            case RESULT_LOCAL_IS_DIRECTORY:
                return "Local path is a directory.";
            case RESULT_REMOTE_IS_FILE:
                return "Remote path is a file.";
            case RESULT_BUFFER_OVERRUN:
                return "Receiving too much data.";
        }
        throw new RuntimeException();
    }
    public SyncResult pull(FileEntry[] entries, String localPath, ISyncProgressMonitor monitor) {
        File f = new File(localPath);
        if (f.exists() == false) {
            return new SyncResult(RESULT_NO_DIR_TARGET);
        }
        if (f.isDirectory() == false) {
            return new SyncResult(RESULT_TARGET_IS_FILE);
        }
        FileListingService fls = new FileListingService(mDevice);
        int total = getTotalRemoteFileSize(entries, fls);
        monitor.start(total);
        SyncResult result = doPull(entries, localPath, fls, monitor);
        monitor.stop();
        return result;
    }
    public SyncResult pullFile(FileEntry remote, String localFilename,
            ISyncProgressMonitor monitor) {
        int total = remote.getSizeValue();
        monitor.start(total);
        SyncResult result = doPullFile(remote.getFullPath(), localFilename, monitor);
        monitor.stop();
        return result;
    }
    public SyncResult pullFile(String remoteFilepath, String localFilename,
            ISyncProgressMonitor monitor) {
        monitor.start(0);
        SyncResult result = doPullFile(remoteFilepath, localFilename, monitor);
        monitor.stop();
        return result;
    }
    public SyncResult push(String[] local, FileEntry remote, ISyncProgressMonitor monitor) {
        if (remote.isDirectory() == false) {
            return new SyncResult(RESULT_REMOTE_IS_FILE);
        }
        ArrayList<File> files = new ArrayList<File>();
        for (String path : local) {
            files.add(new File(path));
        }
        File[] fileArray = files.toArray(new File[files.size()]);
        int total = getTotalLocalFileSize(fileArray);
        monitor.start(total);
        SyncResult result = doPush(fileArray, remote.getFullPath(), monitor);
        monitor.stop();
        return result;
    }
    public SyncResult pushFile(String local, String remote, ISyncProgressMonitor monitor) {
        File f = new File(local);
        if (f.exists() == false) {
            return new SyncResult(RESULT_NO_LOCAL_FILE);
        }
        if (f.isDirectory()) {
            return new SyncResult(RESULT_LOCAL_IS_DIRECTORY);
        }
        monitor.start((int)f.length());
        SyncResult result = doPushFile(local, remote, monitor);
        monitor.stop();
        return result;
    }
    private int getTotalRemoteFileSize(FileEntry[] entries, FileListingService fls) {
        int count = 0;
        for (FileEntry e : entries) {
            int type = e.getType();
            if (type == FileListingService.TYPE_DIRECTORY) {
                FileEntry[] children = fls.getChildren(e, false, null);
                count += getTotalRemoteFileSize(children, fls) + 1;
            } else if (type == FileListingService.TYPE_FILE) {
                count += e.getSizeValue();
            }
        }
        return count;
    }
    private int getTotalLocalFileSize(File[] files) {
        int count = 0;
        for (File f : files) {
            if (f.exists()) {
                if (f.isDirectory()) {
                    return getTotalLocalFileSize(f.listFiles()) + 1;
                } else if (f.isFile()) {
                    count += f.length();
                }
            }
        }
        return count;
    }
    private SyncResult doPull(FileEntry[] entries, String localPath,
            FileListingService fileListingService,
            ISyncProgressMonitor monitor) {
        for (FileEntry e : entries) {
            if (monitor.isCanceled() == true) {
                return new SyncResult(RESULT_CANCELED);
            }
            int type = e.getType();
            if (type == FileListingService.TYPE_DIRECTORY) {
                monitor.startSubTask(e.getFullPath());
                String dest = localPath + File.separator + e.getName();
                File d = new File(dest);
                d.mkdir();
                FileEntry[] children = fileListingService.getChildren(e, true, null);
                SyncResult result = doPull(children, dest, fileListingService, monitor);
                if (result.mCode != RESULT_OK) {
                    return result;
                }
                monitor.advance(1);
            } else if (type == FileListingService.TYPE_FILE) {
                monitor.startSubTask(e.getFullPath());
                String dest = localPath + File.separator + e.getName();
                SyncResult result = doPullFile(e.getFullPath(), dest, monitor);
                if (result.mCode != RESULT_OK) {
                    return result;
                }
            }
        }
        return new SyncResult(RESULT_OK);
    }
    private SyncResult doPullFile(String remotePath, String localPath,
            ISyncProgressMonitor monitor) {
        byte[] msg = null;
        byte[] pullResult = new byte[8];
        final int timeOut = DdmPreferences.getTimeOut();
        try {
            byte[] remotePathContent = remotePath.getBytes(AdbHelper.DEFAULT_ENCODING);
            if (remotePathContent.length > REMOTE_PATH_MAX_LENGTH) {
                return new SyncResult(RESULT_REMOTE_PATH_LENGTH);
            }
            msg = createFileReq(ID_RECV, remotePathContent);
            AdbHelper.write(mChannel, msg, -1, timeOut);
            AdbHelper.read(mChannel, pullResult, -1, timeOut);
            if (checkResult(pullResult, ID_DATA) == false &&
                    checkResult(pullResult, ID_DONE) == false) {
                return new SyncResult(RESULT_CONNECTION_ERROR);
            }
        } catch (UnsupportedEncodingException e) {
            return new SyncResult(RESULT_REMOTE_PATH_ENCODING, e);
        } catch (IOException e) {
            return new SyncResult(RESULT_CONNECTION_ERROR, e);
        }
        File f = new File(localPath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            return new SyncResult(RESULT_FILE_WRITE_ERROR, e);
        }
        byte[] data = new byte[SYNC_DATA_MAX];
        while (true) {
            if (monitor.isCanceled() == true) {
                return new SyncResult(RESULT_CANCELED);
            }
            if (checkResult(pullResult, ID_DONE)) {
                break;
            }
            if (checkResult(pullResult, ID_DATA) == false) {
                return new SyncResult(RESULT_CONNECTION_ERROR);
            }
            int length = ArrayHelper.swap32bitFromArray(pullResult, 4);
            if (length > SYNC_DATA_MAX) {
                return new SyncResult(RESULT_BUFFER_OVERRUN);
            }
            try {
                AdbHelper.read(mChannel, data, length, timeOut);
                AdbHelper.read(mChannel, pullResult, -1, timeOut);
            } catch (IOException e) {
                return new SyncResult(RESULT_CONNECTION_ERROR, e);
            }
            try {
                fos.write(data, 0, length);
            } catch (IOException e) {
                return new SyncResult(RESULT_FILE_WRITE_ERROR, e);
            }
            monitor.advance(length);
        }
        try {
            fos.flush();
        } catch (IOException e) {
            return new SyncResult(RESULT_FILE_WRITE_ERROR, e);
        }
        return new SyncResult(RESULT_OK);
    }
    private SyncResult doPush(File[] fileArray, String remotePath, ISyncProgressMonitor monitor) {
        for (File f : fileArray) {
            if (monitor.isCanceled() == true) {
                return new SyncResult(RESULT_CANCELED);
            }
            if (f.exists()) {
                if (f.isDirectory()) {
                    String dest = remotePath + "/" + f.getName(); 
                    monitor.startSubTask(dest);
                    SyncResult result = doPush(f.listFiles(), dest, monitor);
                    if (result.mCode != RESULT_OK) {
                        return result;
                    }
                    monitor.advance(1);
                } else if (f.isFile()) {
                    String remoteFile = remotePath + "/" + f.getName(); 
                    monitor.startSubTask(remoteFile);
                    SyncResult result = doPushFile(f.getAbsolutePath(), remoteFile, monitor);
                    if (result.mCode != RESULT_OK) {
                        return result;
                    }
                }
            }
        }
        return new SyncResult(RESULT_OK);
    }
    private SyncResult doPushFile(String localPath, String remotePath,
            ISyncProgressMonitor monitor) {
        FileInputStream fis = null;
        byte[] msg;
        final int timeOut = DdmPreferences.getTimeOut();
        try {
            byte[] remotePathContent = remotePath.getBytes(AdbHelper.DEFAULT_ENCODING);
            if (remotePathContent.length > REMOTE_PATH_MAX_LENGTH) {
                return new SyncResult(RESULT_REMOTE_PATH_LENGTH);
            }
            File f = new File(localPath);
            if (f.exists() == false) {
                return new SyncResult(RESULT_NO_LOCAL_FILE);
            }
            fis = new FileInputStream(f);
            msg = createSendFileReq(ID_SEND, remotePathContent, 0644);
        } catch (UnsupportedEncodingException e) {
            return new SyncResult(RESULT_REMOTE_PATH_ENCODING, e);
        } catch (FileNotFoundException e) {
            return new SyncResult(RESULT_FILE_READ_ERROR, e);
        }
        try {
            AdbHelper.write(mChannel, msg, -1, timeOut);
        } catch (IOException e) {
            return new SyncResult(RESULT_CONNECTION_ERROR, e);
        }
        if (mBuffer == null) {
            mBuffer = new byte[SYNC_DATA_MAX + 8];
        }
        System.arraycopy(ID_DATA, 0, mBuffer, 0, ID_DATA.length);
        while (true) {
            if (monitor.isCanceled() == true) {
                return new SyncResult(RESULT_CANCELED);
            }
            int readCount = 0;
            try {
                readCount = fis.read(mBuffer, 8, SYNC_DATA_MAX);
            } catch (IOException e) {
                return new SyncResult(RESULT_FILE_READ_ERROR, e);
            }
            if (readCount == -1) {
                break;
            }
            ArrayHelper.swap32bitsToArray(readCount, mBuffer, 4);
            try {
                AdbHelper.write(mChannel, mBuffer, readCount+8, timeOut);
            } catch (IOException e) {
                return new SyncResult(RESULT_CONNECTION_ERROR, e);
            }
            monitor.advance(readCount);
        }
        try {
            fis.close();
        } catch (IOException e) {
            return new SyncResult(RESULT_FILE_READ_ERROR, e);
        }
        try {
            long time = System.currentTimeMillis() / 1000;
            msg = createReq(ID_DONE, (int)time);
            AdbHelper.write(mChannel, msg, -1, timeOut);
            byte[] result = new byte[8];
            AdbHelper.read(mChannel, result, -1 , timeOut);
            if (checkResult(result, ID_OKAY) == false) {
                if (checkResult(result, ID_FAIL)) {
                    int len = ArrayHelper.swap32bitFromArray(result, 4);
                    AdbHelper.read(mChannel, mBuffer, len, timeOut);
                    String message = new String(mBuffer, 0, len);
                    Log.e("ddms", "transfer error: " + message);
                    return new SyncResult(RESULT_UNKNOWN_ERROR, message);
                }
                return new SyncResult(RESULT_UNKNOWN_ERROR);
            }
        } catch (IOException e) {
            return new SyncResult(RESULT_CONNECTION_ERROR, e);
        }
        return new SyncResult(RESULT_OK);
    }
    private Integer readMode(String path) {
        try {
            byte[] msg = createFileReq(ID_STAT, path);
            AdbHelper.write(mChannel, msg, -1 , DdmPreferences.getTimeOut());
            byte[] statResult = new byte[16];
            AdbHelper.read(mChannel, statResult, -1 , DdmPreferences.getTimeOut());
            if (checkResult(statResult, ID_STAT) == false) {
                return null;
            }
            return ArrayHelper.swap32bitFromArray(statResult, 4);
        } catch (IOException e) {
            return null;
        }
    }
    private static byte[] createReq(byte[] command, int value) {
        byte[] array = new byte[8];
        System.arraycopy(command, 0, array, 0, 4);
        ArrayHelper.swap32bitsToArray(value, array, 4);
        return array;
    }
    private static byte[] createFileReq(byte[] command, String path) {
        byte[] pathContent = null;
        try {
            pathContent = path.getBytes(AdbHelper.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return createFileReq(command, pathContent);
    }
    private static byte[] createFileReq(byte[] command, byte[] path) {
        byte[] array = new byte[8 + path.length];
        System.arraycopy(command, 0, array, 0, 4);
        ArrayHelper.swap32bitsToArray(path.length, array, 4);
        System.arraycopy(path, 0, array, 8, path.length);
        return array;
    }
    private static byte[] createSendFileReq(byte[] command, byte[] path, int mode) {
        String modeStr = "," + (mode & 0777); 
        byte[] modeContent = null;
        try {
            modeContent = modeStr.getBytes(AdbHelper.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        byte[] array = new byte[8 + path.length + modeContent.length];
        System.arraycopy(command, 0, array, 0, 4);
        ArrayHelper.swap32bitsToArray(path.length + modeContent.length, array, 4);
        System.arraycopy(path, 0, array, 8, path.length);
        System.arraycopy(modeContent, 0, array, 8 + path.length, modeContent.length);
        return array;
    }
    private static boolean checkResult(byte[] result, byte[] code) {
        if (result[0] != code[0] ||
                result[1] != code[1] ||
                result[2] != code[2] ||
                result[3] != code[3]) {
            return false;
        }
        return true;
    }
    private static int getFileType(int mode) {
        if ((mode & S_ISOCK) == S_ISOCK) {
            return FileListingService.TYPE_SOCKET;
        }
        if ((mode & S_IFLNK) == S_IFLNK) {
            return FileListingService.TYPE_LINK;
        }
        if ((mode & S_IFREG) == S_IFREG) {
            return FileListingService.TYPE_FILE;
        }
        if ((mode & S_IFBLK) == S_IFBLK) {
            return FileListingService.TYPE_BLOCK;
        }
        if ((mode & S_IFDIR) == S_IFDIR) {
            return FileListingService.TYPE_DIRECTORY;
        }
        if ((mode & S_IFCHR) == S_IFCHR) {
            return FileListingService.TYPE_CHARACTER;
        }
        if ((mode & S_IFIFO) == S_IFIFO) {
            return FileListingService.TYPE_FIFO;
        }
        return FileListingService.TYPE_OTHER;
    }
}
