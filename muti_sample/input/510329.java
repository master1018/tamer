public class AppCacheTest extends AndroidTestCase {
    private static final boolean localLOGV = false;
    public static final String TAG="AppCacheTest";
    public final long MAX_WAIT_TIME=60*1000;
    public final long WAIT_TIME_INCR=10*1000;
    private static final int THRESHOLD=5;
    private static final int ACTUAL_THRESHOLD=10;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if(localLOGV) Log.i(TAG, "Cleaning up cache directory first");
        cleanUpCacheDirectory();
    }
    void cleanUpDirectory(File pDir, String dirName) {
       File testDir = new File(pDir,  dirName);
       if(!testDir.exists()) {
           return;
       }
        String fList[] = testDir.list();
        for(int i = 0; i < fList.length; i++) {
            File file = new File(testDir, fList[i]);
            if(file.isDirectory()) {
                cleanUpDirectory(testDir, fList[i]);
            } else {
                file.delete();
            }
        }
        testDir.delete();
    }
    void cleanUpCacheDirectory() {
        File testDir = mContext.getCacheDir();
        if(!testDir.exists()) {
            return;
        }
         String fList[] = testDir.list();
         if(fList == null) {
             testDir.delete();
             return;
         }
         for(int i = 0; i < fList.length; i++) {
             File file = new File(testDir, fList[i]);
             if(file.isDirectory()) {
                 cleanUpDirectory(testDir, fList[i]);
             } else {
                 file.delete();
             }
         }
     }
    @SmallTest
    public void testDeleteAllCacheFiles() {
        String testName="testDeleteAllCacheFiles";
        cleanUpCacheDirectory();
    }
    void failStr(String errMsg) {
        Log.w(TAG, "errMsg="+errMsg);
        fail(errMsg);
    }
    void failStr(Exception e) {
        Log.w(TAG, "e.getMessage="+e.getMessage());
        Log.w(TAG, "e="+e);
    }
    long getFreeStorageBlks(StatFs st) {
        st.restat("/data");
        return st.getFreeBlocks();
    }
    long getFreeStorageSize(StatFs st) {
        st.restat("/data");
        return (st.getFreeBlocks()*st.getBlockSize());
    }
    @LargeTest
    public void testFreeApplicationCacheAllFiles() throws Exception {
        boolean TRACKING = true;
        StatFs st = new StatFs("/data");
        long blks1 = getFreeStorageBlks(st);
        long availableMem = getFreeStorageSize(st);
        File cacheDir = mContext.getCacheDir();
        assertNotNull(cacheDir);
        createTestFiles1(cacheDir, "testtmpdir", 5);
        long blks2 = getFreeStorageBlks(st);
        if(localLOGV || TRACKING) Log.i(TAG, "blk1="+blks1+", blks2="+blks2);
        invokePMFreeApplicationCache(availableMem);
        long blks3 = getFreeStorageBlks(st);
        if(localLOGV || TRACKING) Log.i(TAG, "blks3="+blks3);
        verifyTestFiles1(cacheDir, "testtmpdir", 5);
    }
    public void testFreeApplicationCacheSomeFiles() throws Exception {
        StatFs st = new StatFs("/data");
        long blks1 = getFreeStorageBlks(st);
        File cacheDir = mContext.getCacheDir();
        assertNotNull(cacheDir);
        createTestFiles1(cacheDir, "testtmpdir", 5);
        long blks2 = getFreeStorageBlks(st);
        Log.i(TAG, "blk1="+blks1+", blks2="+blks2);
        long diff = (blks1-blks2-2);
        assertTrue(invokePMFreeApplicationCache(diff*st.getBlockSize()));    
        long blks3 = getFreeStorageBlks(st);
        if(!((blks3 <= blks1) && (blks3 >= blks2))) {
            failStr("Expected "+(blks1-blks2)+" number of blocks to be freed but freed only "
                    +(blks1-blks3));
        }
    }
    public void openOutFileInAppFilesDir(File pFile, String pFileOut) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pFile);
        } catch (FileNotFoundException e1) {
            failStr("Error when opening file "+e1);
            return;
        }
        try {
            fos.write(pFileOut.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            failStr(e.getMessage());
        } catch (IOException e) {
            failStr(e.getMessage());
        } 
        int count = pFileOut.getBytes().length;
        byte[] buffer = new byte[count];
        try {
            FileInputStream fis = new FileInputStream(pFile);
            fis.read(buffer, 0, count);
            fis.close();
        } catch (FileNotFoundException e) {
            failStr("Failed when verifing output opening file "+e.getMessage());
        } catch (IOException e) {
            failStr("Failed when verifying output, reading from written file "+e);
        }
        String str = new String(buffer);
        assertEquals(str, pFileOut);
    } 
    @MediumTest
    public void testAppFilesCreateFile() {
        String fileName = "testFile1.txt";
        String fileOut = "abcdefghijklmnopqrstuvwxyz";
        Context con = super.getContext();
        try {
            FileOutputStream fos = con.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.close();
        } catch (FileNotFoundException e) {
            failStr(e);
        } catch (IOException e) {
            failStr(e);
        }
    }
    @SmallTest
    public void testAppCacheCreateFile() {
        String fileName = "testFile1.txt";
        String fileOut = "abcdefghijklmnopqrstuvwxyz";
        Context con = super.getContext();
        File file = new File(con.getCacheDir(), fileName);
        openOutFileInAppFilesDir(file, fileOut);
        cleanUpCacheDirectory();
    }
    @MediumTest
    public void testAppCreateCacheFiles() {
        File cacheDir = mContext.getCacheDir();
        String testDirName = "testtmp";
        File testTmpDir = new File(cacheDir, testDirName);
        testTmpDir.mkdir();
        int numDirs = 3;
        File fileArr[] = new File[numDirs];
        for(int i = 0; i < numDirs; i++) {
            fileArr[i] = new File(testTmpDir, "dir"+(i+1));
            fileArr[i].mkdir();
        }
        byte buffer[] = getBuffer();
        Log.i(TAG, "Size of bufer="+buffer.length);
        for(int i = 0; i < numDirs; i++) {
            for(int j = 1; j <= (i); j++) {
                File file1 = new File(fileArr[i], "testFile"+j+".txt");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file1);
                    for(int k = 1; k < 10; k++) {
                        fos.write(buffer);
                    }
                    Log.i(TAG, "wrote 10K bytes to "+file1);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.i(TAG, "Excetion ="+e);
                    fail("Error when creating outputstream "+e);
                } catch(IOException e) {
                    Log.i(TAG, "Excetion ="+e);
                    fail("Error when writing output "+e);
                }
            }
        }
    }
    byte[] getBuffer() {
        String sbuffer = "a";
        for(int i = 0; i < 10; i++) {
            sbuffer += sbuffer;
        }
        return sbuffer.getBytes();
    }
    long getFileNumBlocks(long fileSize, int blkSize) {
        long ret = fileSize/blkSize;
        if(ret*blkSize < fileSize) {
            ret++;
        }
        return ret;
    }
    public void testAppCacheClear() {
        String dataDir="/data/data";
        StatFs st = new StatFs(dataDir);
        int blkSize = st.getBlockSize();
        int totBlks = st.getBlockCount();
        long availableBlks = st.getFreeBlocks();
        long thresholdBlks = (totBlks*THRESHOLD)/100;
        String testDirName = "testdir";
        File testDir = new File(mContext.getCacheDir(),  testDirName);
        testDir.mkdirs();
        byte[] buffer = getBuffer();
        int i = 1;
        if(localLOGV) Log.i(TAG, "availableBlks="+availableBlks+", thresholdBlks="+thresholdBlks);
        long createdFileBlks = 0;
        int imax = 300;
        while((availableBlks > thresholdBlks) &&(i < imax)) {
            File testFile = new File(testDir, "testFile"+i+".txt");
            if(localLOGV) Log.i(TAG, "Creating "+i+"th test file "+testFile);
            int jmax = i;
            i++;
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(testFile);
            } catch (FileNotFoundException e) {
                Log.i(TAG, "Failed creating test file:"+testFile);
                continue;
            }
            boolean err = false;
            for(int j = 1; j <= jmax;j++) {
                try {
                    fos.write(buffer);
                } catch (IOException e) {
                    Log.i(TAG, "Failed to write to file:"+testFile);
                    err = true;
                }
            }
            try {
                fos.close();
            } catch (IOException e) {
                Log.i(TAG, "Failed closing file:"+testFile);
            }
            if(err) {
                continue;
            }
            createdFileBlks += getFileNumBlocks(testFile.length(), blkSize);
            st.restat(dataDir);
            availableBlks = st.getFreeBlocks();
        }
        st.restat(dataDir);
        long availableBytes = st.getFreeBlocks()*blkSize;
        long shouldFree = (ACTUAL_THRESHOLD-THRESHOLD)*totBlks;
        try {
            Log.i(TAG, "Sleeping for 2 minutes...");
            Thread.sleep(2*60*1000);
        } catch (InterruptedException e) {
            fail("Exception when sleeping "+e);
        }
        boolean removedFlag = false;
        long existingFileBlks = 0;
        for(int k = 1; k <i; k++) {
            File testFile = new File(testDir, "testFile"+k+".txt");
            if(!testFile.exists()) {
                removedFlag = true;
                if(localLOGV) Log.i(TAG, testFile+" removed");
            }  else {
                existingFileBlks += getFileNumBlocks(testFile.length(), blkSize);
            }
        }
        if(localLOGV) Log.i(TAG, "createdFileBlks="+createdFileBlks+
                ", existingFileBlks="+existingFileBlks);
        long fileSize = createdFileBlks-existingFileBlks;
        if(localLOGV) Log.i(TAG, "deletedFileBlks="+fileSize+" shouldFreeBlks="+shouldFree);
        if((fileSize > (shouldFree-blkSize) && (fileSize < (shouldFree+blkSize)))) {
            Log.i(TAG, "passed");
        }
        assertTrue(removedFlag);
    }
    void createTestFiles1(File cacheDir, String testFilePrefix, int numTestFiles) {
        byte buffer[] = getBuffer();
        for(int i = 0; i < numTestFiles; i++) {
            File file1 = new File(cacheDir, testFilePrefix+i+".txt");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file1);
                for(int k = 1; k < 10; k++) {
                    fos.write(buffer);
               }
                fos.close();
            } catch (FileNotFoundException e) {
                Log.i(TAG, "Exception ="+e);
                fail("Error when creating outputstream "+e);
            } catch(IOException e) {
                Log.i(TAG, "Exception ="+e);
                fail("Error when writing output "+e);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                fail("Exception when sleeping "+e);
            }
        }
    }
    void verifyTestFiles1(File cacheDir, String testFilePrefix, int numTestFiles) {
        for(int i = 0; i < numTestFiles; i++) {
            File file1 = new File(cacheDir, testFilePrefix+i+".txt");
            if(file1.exists()) {
                fail("file:"+file1+" should not exist");
            }
        }
    }
    void createTestFiles2(File cacheDir, String rootTestDirName, String subDirPrefix, int numDirs, String testFilePrefix) {
        Context con = super.getContext();
        File testTmpDir = new File(cacheDir, rootTestDirName);
        testTmpDir.mkdir();
        File fileArr[] = new File[numDirs];
        for(int i = 0; i < numDirs; i++) {
            fileArr[i] = new File(testTmpDir, subDirPrefix+(i+1));
            fileArr[i].mkdir();
        }
        byte buffer[] = getBuffer();
        for(int i = 0; i < numDirs; i++) {
            for(int j = 1; j <= (i); j++) {
                File file1 = new File(fileArr[i], testFilePrefix+j+".txt");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file1);
                    for(int k = 1; k < 10; k++) {
                        fos.write(buffer);
                    }
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.i(TAG, "Exception ="+e);
                    fail("Error when creating outputstream "+e);
                } catch(IOException e) {
                    Log.i(TAG, "Exception ="+e);
                    fail("Error when writing output "+e);
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    fail("Exception when sleeping "+e);
                }
            }
        }
    }
    class PackageDataObserver extends IPackageDataObserver.Stub {
        public boolean retValue = false;
        private boolean doneFlag = false;
        public void onRemoveCompleted(String packageName, boolean succeeded)
                throws RemoteException {
            synchronized(this) {
                retValue = succeeded;
                doneFlag = true;
                notifyAll();
            }
        }
        public boolean isDone() {
            return doneFlag;
        }
    }
    IPackageManager getPm() {
        return  IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
    }
    boolean invokePMDeleteAppCacheFiles() throws Exception {
        try {
            String packageName = mContext.getPackageName();
            PackageDataObserver observer = new PackageDataObserver();
            synchronized(observer) {
                getPm().deleteApplicationCacheFiles(packageName, observer);
                long waitTime = 0;
                while(!observer.isDone() || (waitTime > MAX_WAIT_TIME)) {
                    observer.wait(WAIT_TIME_INCR);
                    waitTime += WAIT_TIME_INCR;
                }
                if(!observer.isDone()) {
                    throw new Exception("timed out waiting for PackageDataObserver.onRemoveCompleted");
                }
            }
            return observer.retValue;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get handle for PackageManger Exception: "+e);
            return false;
        } catch (InterruptedException e) {
            Log.w(TAG, "InterruptedException :"+e);
            return false;
        }
    }
    boolean invokePMFreeApplicationCache(long idealStorageSize) throws Exception {
        try {
            String packageName = mContext.getPackageName();
            PackageDataObserver observer = new PackageDataObserver();
            synchronized(observer) {
                getPm().freeStorageAndNotify(idealStorageSize, observer);
                long waitTime = 0;
                while(!observer.isDone() || (waitTime > MAX_WAIT_TIME)) {
                    observer.wait(WAIT_TIME_INCR);
                    waitTime += WAIT_TIME_INCR;
                }
                if(!observer.isDone()) {
                    throw new Exception("timed out waiting for PackageDataObserver.onRemoveCompleted");
                }
            }
            return observer.retValue;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get handle for PackageManger Exception: "+e);
            return false;
        } catch (InterruptedException e) {
            Log.w(TAG, "InterruptedException :"+e);
            return false;
        }
    }
    boolean invokePMFreeStorage(long idealStorageSize, FreeStorageReceiver r, 
            PendingIntent pi) throws Exception {
        try {
            synchronized(r) {
                getPm().freeStorage(idealStorageSize, pi.getIntentSender());
                long waitTime = 0;
                while(!r.isDone() && (waitTime < MAX_WAIT_TIME)) {
                    r.wait(WAIT_TIME_INCR);
                    waitTime += WAIT_TIME_INCR;
                }
                if(!r.isDone()) {
                    throw new Exception("timed out waiting for call back from PendingIntent");
                }
            }
            return r.getResultCode() == 1;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get handle for PackageManger Exception: "+e);
            return false;
        } catch (InterruptedException e) {
            Log.w(TAG, "InterruptedException :"+e);
            return false;
        }
    }
    @LargeTest
    public void testDeleteAppCacheFiles() throws Exception {
        String testName="testDeleteAppCacheFiles";
        File cacheDir = mContext.getCacheDir();
        createTestFiles1(cacheDir, "testtmpdir", 5);
        assertTrue(invokePMDeleteAppCacheFiles());
        verifyTestFiles1(cacheDir, "testtmpdir", 5);
    }
    class PackageStatsObserver extends IPackageStatsObserver.Stub {
        public boolean retValue = false;
        public PackageStats stats;
        private boolean doneFlag = false;
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                throws RemoteException {
            synchronized(this) {
                retValue = succeeded;
                stats = pStats;
                doneFlag = true;
                notifyAll();
            }
        }
        public boolean isDone() {
            return doneFlag;
        }
    }
    public PackageStats invokePMGetPackageSizeInfo() throws Exception {
        try {
            String packageName = mContext.getPackageName();
            PackageStatsObserver observer = new PackageStatsObserver();
            synchronized(observer) {
                getPm().getPackageSizeInfo(packageName, observer);
                long waitTime = 0;
                while((!observer.isDone()) || (waitTime > MAX_WAIT_TIME) ) {
                    observer.wait(WAIT_TIME_INCR);
                    waitTime += WAIT_TIME_INCR;
                }
                if(!observer.isDone()) {
                    throw new Exception("Timed out waiting for PackageStatsObserver.onGetStatsCompleted");
                }
            }
            if(localLOGV) Log.i(TAG, "OBSERVER RET VALUES code="+observer.stats.codeSize+
                    ", data="+observer.stats.dataSize+", cache="+observer.stats.cacheSize);
            return observer.stats;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get handle for PackageManger Exception: "+e);
            return null;
        } catch (InterruptedException e) {
            Log.w(TAG, "InterruptedException :"+e);
            return null;
        }
    }
    @SmallTest
    public void testGetPackageSizeInfo() throws Exception {
        String testName="testGetPackageSizeInfo";
        PackageStats stats = invokePMGetPackageSizeInfo();
        assertTrue(stats!=null);
        if(localLOGV) Log.i(TAG, "code="+stats.codeSize+", data="+stats.dataSize+
                ", cache="+stats.cacheSize);
    }
    @SmallTest
    public void testGetSystemSharedLibraryNames() throws Exception {
        try {
            String[] sharedLibs = getPm().getSystemSharedLibraryNames();
            if (localLOGV) {
                for (String str : sharedLibs) {
                    Log.i(TAG, str);
                }
            }
        } catch (RemoteException e) {
            fail("Failed invoking getSystemSharedLibraryNames with exception:" + e);
        }   
    }
    class FreeStorageReceiver extends BroadcastReceiver {
        public static final String ACTION_FREE = "com.android.unit_tests.testcallback";
        private boolean doneFlag = false;
        public boolean isDone() {
            return doneFlag;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(ACTION_FREE)) {
                if (localLOGV) Log.i(TAG, "Got notification: clear cache succeeded "+getResultCode());
                synchronized (this) {
                    doneFlag = true;
                    notifyAll();
                }
            }
        }
    }
    @SmallTest
    public void testFreeStorage() throws Exception {
        boolean TRACKING = true;
        StatFs st = new StatFs("/data");
        long blks1 = getFreeStorageBlks(st);
        if(localLOGV || TRACKING) Log.i(TAG, "Available free blocks="+blks1);
        long availableMem = getFreeStorageSize(st);
        File cacheDir = mContext.getCacheDir();
        assertNotNull(cacheDir);
        createTestFiles1(cacheDir, "testtmpdir", 5);
        long blks2 = getFreeStorageBlks(st);
        if(localLOGV || TRACKING) Log.i(TAG, "Available blocks after writing test files in application cache="+blks2);
        FreeStorageReceiver receiver = new FreeStorageReceiver();
        mContext.registerReceiver(receiver, new IntentFilter(FreeStorageReceiver.ACTION_FREE));
        PendingIntent pi = PendingIntent.getBroadcast(mContext,
                0,  new Intent(FreeStorageReceiver.ACTION_FREE), 0);
        invokePMFreeStorage(availableMem, receiver, pi);
        long blks3 = getFreeStorageBlks(st);
        if(localLOGV || TRACKING) Log.i(TAG, "Available blocks after freeing cache"+blks3);
        assertEquals(receiver.getResultCode(), 1);
        mContext.unregisterReceiver(receiver);
        verifyTestFiles1(cacheDir, "testtmpdir", 5);
    }
    boolean invokePMClearApplicationUserData() throws Exception {
        try {
            String packageName = mContext.getPackageName();
            PackageDataObserver observer = new PackageDataObserver();
            synchronized(observer) {
                getPm().clearApplicationUserData(packageName, observer);
                long waitTime = 0;
                while(!observer.isDone() || (waitTime > MAX_WAIT_TIME)) {
                    observer.wait(WAIT_TIME_INCR);
                    waitTime += WAIT_TIME_INCR;
                }
                if(!observer.isDone()) {
                    throw new Exception("timed out waiting for PackageDataObserver.onRemoveCompleted");
                }
            }
            return observer.retValue;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get handle for PackageManger Exception: "+e);
            return false;
        } catch (InterruptedException e) {
            Log.w(TAG, "InterruptedException :"+e);
            return false;
        }
    }
    void verifyUserDataCleared(File pDir) {
        if(localLOGV) Log.i(TAG, "Verifying "+pDir);
        if(pDir == null) {
            return;
        }
        String fileList[] = pDir.list();
        if(fileList == null) {
            return;
        }
        int imax = fileList.length;
        for(int i = 0; i < imax; i++) {
            if(localLOGV) Log.i(TAG, "Found entry "+fileList[i]+ "in "+pDir);
            if("lib".equalsIgnoreCase(fileList[i])) {
                if(localLOGV) Log.i(TAG, "Ignoring lib directory");
                continue;
            }
            fail(pDir+" should be empty or contain only lib subdirectory. Found "+fileList[i]);
        }
    }
    File getDataDir() {
        try {
            ApplicationInfo appInfo = getPm().getApplicationInfo(mContext.getPackageName(), 0);
            return new File(appInfo.dataDir);
        } catch (RemoteException e) {
            throw new RuntimeException("Pacakge manager dead", e);
        }
    }
    @LargeTest
    public void testClearApplicationUserDataWithTestData() throws Exception {
        File cacheDir = mContext.getCacheDir();
        createTestFiles1(cacheDir, "testtmpdir", 5);
        if(localLOGV) {
            Log.i(TAG, "Created test data Waiting for 60seconds before continuing");
            Thread.sleep(60*1000);
        }
        assertTrue(invokePMClearApplicationUserData());
        verifyUserDataCleared(getDataDir());
    }
    @SmallTest
    public void testClearApplicationUserDataWithNoTestData() throws Exception {
        assertTrue(invokePMClearApplicationUserData());
        verifyUserDataCleared(getDataDir());
    }
    @LargeTest
    public void testClearApplicationUserDataNoObserver() throws Exception {
        getPm().clearApplicationUserData(mContext.getPackageName(), null);
        Thread.sleep(60*1000);
        verifyUserDataCleared(getDataDir());
    }
}
