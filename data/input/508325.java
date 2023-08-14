public class ManageApplications extends TabActivity implements
        OnItemClickListener, DialogInterface.OnCancelListener,
        TabHost.TabContentFactory,
        TabHost.OnTabChangeListener {
    private static final String TAG = "ManageApplications";
    private static final String PREFS_NAME = "ManageAppsInfo.prefs";
    private static final String PREF_DISABLE_CACHE = "disableCache";
    private boolean localLOGV = false;
    private static final boolean DEBUG_SIZE = false;
    private static final boolean DEBUG_TIME = false;
    public static final String APP_PKG_NAME = "pkg";
    public static final String APP_CHG = "chg";
    private static final String ATTR_PKG_NAME="p";
    private static final String ATTR_PKGS="ps";
    private static final String ATTR_STATS="ss";
    private static final String ATTR_SIZE_STRS="fs";
    private static final String ATTR_GET_SIZE_STATUS="passed";
    private static final String ATTR_PKG_STATS="s";
    private static final String ATTR_PKG_SIZE_STR="f";
    private static final int INSTALLED_APP_DETAILS = 1;
    private static final int MENU_OPTIONS_BASE = 0;
    public static final int FILTER_APPS_ALL = MENU_OPTIONS_BASE + 0;
    public static final int FILTER_APPS_RUNNING = MENU_OPTIONS_BASE + 1;
    public static final int FILTER_APPS_THIRD_PARTY = MENU_OPTIONS_BASE + 2;
    public static final int FILTER_APPS_SDCARD = MENU_OPTIONS_BASE + 3;
    public static final int SORT_ORDER_ALPHA = MENU_OPTIONS_BASE + 4;
    public static final int SORT_ORDER_SIZE = MENU_OPTIONS_BASE + 5;
    private int mSortOrder = SORT_ORDER_ALPHA;
    private int mFilterApps = FILTER_APPS_THIRD_PARTY;
    private AppInfoAdapter mAppInfoAdapter;
    private static final int HANDLER_MESSAGE_BASE = 0;
    private static final int INIT_PKG_INFO = HANDLER_MESSAGE_BASE+1;
    private static final int COMPUTE_BULK_SIZE = HANDLER_MESSAGE_BASE+2;
    private static final int REMOVE_PKG = HANDLER_MESSAGE_BASE+3;
    private static final int REORDER_LIST = HANDLER_MESSAGE_BASE+4;
    private static final int ADD_PKG_START = HANDLER_MESSAGE_BASE+5;
    private static final int ADD_PKG_DONE = HANDLER_MESSAGE_BASE+6;
    private static final int REFRESH_LABELS = HANDLER_MESSAGE_BASE+7;
    private static final int REFRESH_DONE = HANDLER_MESSAGE_BASE+8;
    private static final int NEXT_LOAD_STEP = HANDLER_MESSAGE_BASE+9;
    private static final int COMPUTE_END = HANDLER_MESSAGE_BASE+10;
    private static final int REFRESH_ICONS = HANDLER_MESSAGE_BASE+11;
    private PkgSizeObserver mObserver;
    private PackageManager mPm;
    private PackageIntentReceiver mReceiver;
    private boolean mComputeSizesFinished = false;
    private static Drawable mDefaultAppIcon;
    private static final int DLG_BASE = 0;
    private static final int DLG_LOADING = DLG_BASE + 1;
    private CharSequence mInvalidSizeStr;
    private CharSequence mComputingSizeStr;
    private Map<String, Boolean> mAddRemoveMap;
    private LayoutInflater mInflater;
    private static final int SIZE_INVALID = -1;
    private boolean DEBUG_PKG_DELAY = false;
    ResourceLoaderThread mResourceThread;
    private TaskRunner mSizeComputor;
    private String mCurrentPkgName;
    private AppInfoCache mCache = new AppInfoCache();
    private boolean mLoadLabelsFinished = false;
    private boolean mSizesFirst = false;
    private ListView mListView;
    private boolean mJustCreated = true;
    private boolean mFirst = false;
    private long mLoadTimeStart;
    private boolean mSetListViewLater = true;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean status;
            long size;
            String formattedSize;
            ApplicationInfo info;
            Bundle data;
            String pkgName = null;
            AppInfo appInfo;
            data = msg.getData();
            if(data != null) {
                pkgName = data.getString(ATTR_PKG_NAME);
            }
            switch (msg.what) {
            case INIT_PKG_INFO:
                if(localLOGV) Log.i(TAG, "Message INIT_PKG_INFO, justCreated = " + mJustCreated);
                List<ApplicationInfo> newList = null;
                if (!mJustCreated) {
                    if (localLOGV) Log.i(TAG, "List already created");
                    newList = getInstalledApps(FILTER_APPS_ALL);
                    updateAppList(newList);
                }
                initAppList(newList, mFilterApps);
                mHandler.sendEmptyMessage(NEXT_LOAD_STEP);
                break;
            case COMPUTE_BULK_SIZE:
                if(localLOGV) Log.i(TAG, "Message COMPUTE_BULK_PKG_SIZE");
                String[] pkgs = data.getStringArray(ATTR_PKGS);
                long[] sizes = data.getLongArray(ATTR_STATS);
                String[] formatted = data.getStringArray(ATTR_SIZE_STRS);
                if(pkgs == null || sizes == null || formatted == null) {
                     Log.w(TAG, "Ignoring message");
                     break;
                }
                mAppInfoAdapter.bulkUpdateSizes(pkgs, sizes, formatted);
                break;
            case COMPUTE_END:
                mComputeSizesFinished = true;
                mFirst = true;
                mHandler.sendEmptyMessage(NEXT_LOAD_STEP);
                break;
            case REMOVE_PKG:
                if(localLOGV) Log.i(TAG, "Message REMOVE_PKG");
                if(pkgName == null) {
                    Log.w(TAG, "Ignoring message:REMOVE_PKG for null pkgName");
                    break;
                }
                if (!mComputeSizesFinished) {
                    Boolean currB = mAddRemoveMap.get(pkgName);
                    if (currB == null || (currB.equals(Boolean.TRUE))) {
                        mAddRemoveMap.put(pkgName, Boolean.FALSE);
                    }
                    break;
                }
                List<String> pkgList = new ArrayList<String>();
                pkgList.add(pkgName);
                mAppInfoAdapter.removeFromList(pkgList);
                break;
            case REORDER_LIST:
                if(localLOGV) Log.i(TAG, "Message REORDER_LIST");
                int menuOption = msg.arg1;
                if((menuOption == SORT_ORDER_ALPHA) || 
                        (menuOption == SORT_ORDER_SIZE)) {
                    if (menuOption != mSortOrder) {
                        mSortOrder = menuOption;
                        if (localLOGV) Log.i(TAG, "Changing sort order to "+mSortOrder);
                        mAppInfoAdapter.sortList(mSortOrder);
                    }
                } else if(menuOption != mFilterApps) {
                    mFilterApps = menuOption;
                    boolean ret = mAppInfoAdapter.resetAppList(mFilterApps);
                    if(!ret) {
                        mFilterApps = FILTER_APPS_ALL;
                        mHandler.sendEmptyMessage(INIT_PKG_INFO);
                        sendMessageToHandler(REORDER_LIST, menuOption);
                    }
                }
                break;
            case ADD_PKG_START:
                if(localLOGV) Log.i(TAG, "Message ADD_PKG_START");
                if(pkgName == null) {
                    Log.w(TAG, "Ignoring message:ADD_PKG_START for null pkgName");
                    break;
                }
                if (!mComputeSizesFinished || !mLoadLabelsFinished) {
                    Boolean currB = mAddRemoveMap.get(pkgName);
                    if (currB == null || (currB.equals(Boolean.FALSE))) {
                        mAddRemoveMap.put(pkgName, Boolean.TRUE);
                    }
                    break;
                }
                try {
                    info = mPm.getApplicationInfo(pkgName, 0);
                } catch (NameNotFoundException e) {
                    Log.w(TAG, "Couldnt find application info for:"+pkgName);
                    break;
                }
                mObserver.invokeGetSizeInfo(pkgName);
                break;
            case ADD_PKG_DONE:
                if(localLOGV) Log.i(TAG, "Message ADD_PKG_DONE");
                if(pkgName == null) {
                    Log.w(TAG, "Ignoring message:ADD_PKG_START for null pkgName");
                    break;
                }
                status = data.getBoolean(ATTR_GET_SIZE_STATUS);
                if (status) {
                    size = data.getLong(ATTR_PKG_STATS);
                    formattedSize = data.getString(ATTR_PKG_SIZE_STR);
                    if (!mAppInfoAdapter.isInstalled(pkgName)) {
                        mAppInfoAdapter.addToList(pkgName, size, formattedSize);
                    } else {
                        mAppInfoAdapter.updatePackage(pkgName, size, formattedSize);
                    }
                }
                break;
            case REFRESH_LABELS:
                Map<String, CharSequence> labelMap = (Map<String, CharSequence>) msg.obj;
                if (labelMap != null) {
                    mAppInfoAdapter.bulkUpdateLabels(labelMap);
                }
                break;
            case REFRESH_ICONS:
                Map<String, Drawable> iconMap = (Map<String, Drawable>) msg.obj;
                if (iconMap != null) {
                    mAppInfoAdapter.bulkUpdateIcons(iconMap);
                }
                break;
            case REFRESH_DONE:
                mLoadLabelsFinished = true;
                mHandler.sendEmptyMessage(NEXT_LOAD_STEP);
                break;
            case NEXT_LOAD_STEP:
                if (!mCache.isEmpty() && mSetListViewLater) {
                    if (localLOGV) Log.i(TAG, "Using cache to populate list view");
                    initListView();
                    mSetListViewLater = false;
                    mFirst = true;
                }
                if (mComputeSizesFinished && mLoadLabelsFinished) {
                    doneLoadingData();
                    Set<String> keys =  mAddRemoveMap.keySet();
                    for (String key : keys) {
                        if (mAddRemoveMap.get(key) == Boolean.TRUE) {
                            updatePackageList(Intent.ACTION_PACKAGE_ADDED, key);
                        } else {
                            updatePackageList(Intent.ACTION_PACKAGE_REMOVED, key);
                        }
                    }
                    mAddRemoveMap.clear();
                } else if (!mComputeSizesFinished && !mLoadLabelsFinished) {
                    if (mSizesFirst) {
                        initComputeSizes();
                    } else {
                        initResourceThread();
                    }
                } else {
                    if (mSetListViewLater) {
                        if (localLOGV) Log.i(TAG, "Initing list view for very first time");
                        initListView();
                        mSetListViewLater = false;
                    }
                    if (!mComputeSizesFinished) {
                        initComputeSizes();
                    } else if (!mLoadLabelsFinished) {
                        initResourceThread();
                    }
                }
                break;
            default:
                break;
            }
        }
    };
    private void initListView() {
        mAppInfoAdapter.sortBaseList(mSortOrder);
        if (mJustCreated) {
            mJustCreated = false;
            mListView.setAdapter(mAppInfoAdapter);
            dismissLoadingMsg();
        }
    }
   class SizeObserver extends IPackageStatsObserver.Stub {
       private CountDownLatch mCount;
       PackageStats stats;
       boolean succeeded;
       public void invokeGetSize(String packageName, CountDownLatch count) {
           mCount = count;
           mPm.getPackageSizeInfo(packageName, this);
       }
        public void onGetStatsCompleted(PackageStats pStats, boolean pSucceeded) {
            succeeded = pSucceeded;
            stats = pStats;
            mCount.countDown();
        }
    }
    class TaskRunner extends Thread {
        private List<ApplicationInfo> mPkgList;
        private SizeObserver mSizeObserver;
        private static final int END_MSG = COMPUTE_END;
        private static final int SEND_PKG_SIZES = COMPUTE_BULK_SIZE;
        volatile boolean abort = false;
        static final int MSG_PKG_SIZE = 8;
        TaskRunner(List<ApplicationInfo> appList) {
           mPkgList = appList;
           mSizeObserver = new SizeObserver();
           start();
        }
        public void setAbort() {
            abort = true;
        }
        public void run() {
            long startTime;
            if (DEBUG_SIZE || DEBUG_TIME) {
               startTime =  SystemClock.elapsedRealtime();
            }
            int size = mPkgList.size();
            int numMsgs = size / MSG_PKG_SIZE;
            if (size > (numMsgs * MSG_PKG_SIZE)) {
                numMsgs++;
            }
            int endi = 0;
            for (int j = 0; j < size; j += MSG_PKG_SIZE) {
                long sizes[];
                String formatted[];
                String packages[];
                endi += MSG_PKG_SIZE;
                if (endi > size) {
                    endi = size;
                }
                sizes = new long[endi-j];
                formatted = new String[endi-j];
                packages = new String[endi-j];
                for (int i = j; i < endi; i++) {
                    if (abort) {
                        break;
                    }
                    CountDownLatch count = new CountDownLatch(1);
                    String packageName = mPkgList.get(i).packageName;
                    mSizeObserver.invokeGetSize(packageName, count);
                    try {
                        count.await();
                    } catch (InterruptedException e) {
                        Log.i(TAG, "Failed computing size for pkg : "+packageName);
                    }
                    PackageStats pStats = mSizeObserver.stats;
                    boolean succeeded = mSizeObserver.succeeded;
                    long total;
                    if(succeeded && pStats != null) {
                        total = getTotalSize(pStats);
                    } else {
                        total = SIZE_INVALID;
                    }
                    sizes[i-j] = total;
                    formatted[i-j] = getSizeStr(total).toString();
                    packages[i-j] = packageName;
                }
                Bundle data = new Bundle();
                data.putStringArray(ATTR_PKGS, packages);
                data.putLongArray(ATTR_STATS, sizes);
                data.putStringArray(ATTR_SIZE_STRS, formatted);
                Message msg = mHandler.obtainMessage(SEND_PKG_SIZES, data);
                msg.setData(data);
                mHandler.sendMessage(msg);
            }
            if (DEBUG_SIZE || DEBUG_TIME) Log.i(TAG, "Took "+
                    (SystemClock.elapsedRealtime() - startTime)+
                    " ms to compute sizes of all packages ");
            mHandler.sendEmptyMessage(END_MSG);
        }
    }
    private boolean updateAppList(List<ApplicationInfo> newList) {
        if ((newList == null) || mCache.isEmpty()) {
            return false;
        }
        Set<String> existingList = new HashSet<String>();
        boolean ret = false;
        int N = newList.size();
        for (int i = (N-1); i >= 0; i--) {
            ApplicationInfo info = newList.get(i);
            String pkgName = info.packageName;
            AppInfo aInfo = mCache.getEntry(pkgName);
            if (aInfo != null) {
                existingList.add(pkgName);
            } else {
                if (localLOGV) Log.i(TAG, "New pkg :"+pkgName+" installed when paused");
                updatePackageList(Intent.ACTION_PACKAGE_ADDED, pkgName);
                newList.remove(i);
                ret = true;
            }
        }
        List<String> deletedList = null;
        Set<String> staleList = mCache.getPkgList();
        for (String pkgName : staleList) {
            if (!existingList.contains(pkgName)) {
                if (localLOGV) Log.i(TAG, "Pkg :"+pkgName+" deleted when paused");
                if (deletedList == null) {
                    deletedList = new ArrayList<String>();
                    deletedList.add(pkgName);
                }
                ret = true;
            }
        }
        if (deletedList != null) {
            if (localLOGV) Log.i(TAG, "Deleting right away");
            mAppInfoAdapter.removeFromList(deletedList);
        }
        return ret;
    }
    private void doneLoadingData() {
        setProgressBarIndeterminateVisibility(false);
    }
    List<ApplicationInfo> getInstalledApps(int filterOption) {
        List<ApplicationInfo> installedAppList = mPm.getInstalledApplications(
                PackageManager.GET_UNINSTALLED_PACKAGES);
        if (installedAppList == null) {
            return new ArrayList<ApplicationInfo> ();
        }
        if (filterOption == FILTER_APPS_SDCARD) {
            List<ApplicationInfo> appList =new ArrayList<ApplicationInfo> ();
            for (ApplicationInfo appInfo : installedAppList) {
                if ((appInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                    appList.add(appInfo);
                }
            }
            return appList;
        } else if (filterOption == FILTER_APPS_THIRD_PARTY) {
            List<ApplicationInfo> appList =new ArrayList<ApplicationInfo> ();
            for (ApplicationInfo appInfo : installedAppList) {
                boolean flag = false;
                if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    flag = true;
                } else if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    flag = true;
                }
                if (flag) {
                    appList.add(appInfo);
                }
            }
            return appList;
        } else if (filterOption == FILTER_APPS_RUNNING) {
            List<ApplicationInfo> appList =new ArrayList<ApplicationInfo> ();
            List<ActivityManager.RunningAppProcessInfo> procList = getRunningAppProcessesList();
            if ((procList == null) || (procList.size() == 0)) {
                return appList;
            }
            for (ActivityManager.RunningAppProcessInfo appProcInfo : procList) {
                if ((appProcInfo != null)  && (appProcInfo.pkgList != null)){
                    int size = appProcInfo.pkgList.length;
                    for (int i = 0; i < size; i++) {
                        ApplicationInfo appInfo = null;
                        try {
                            appInfo = mPm.getApplicationInfo(appProcInfo.pkgList[i], 
                                    PackageManager.GET_UNINSTALLED_PACKAGES);
                        } catch (NameNotFoundException e) {
                           Log.w(TAG, "Error retrieving ApplicationInfo for pkg:"+appProcInfo.pkgList[i]);
                           continue;
                        }
                        if(appInfo != null) {
                            appList.add(appInfo);
                        }
                    }
                }
            }
            return appList;
        } else {
            return installedAppList;
        }
    }
    private static boolean matchFilter(boolean filter, Map<String, String> filterMap, String pkg) {
        boolean add = true;
        if (filter) {
            if (filterMap == null || !filterMap.containsKey(pkg)) {
                add = false;
            }
        }
        return add;
    }
    List<ApplicationInfo> getFilteredApps(List<ApplicationInfo> pAppList, int filterOption, boolean filter,
            Map<String, String> filterMap) {
        List<ApplicationInfo> retList = new ArrayList<ApplicationInfo>();
        if(pAppList == null) {
            return retList;
        }
        if (filterOption == FILTER_APPS_SDCARD) {
            for (ApplicationInfo appInfo : pAppList) {
                boolean flag = false;
                if ((appInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                    flag = true;
                }
                if (flag) {
                    if (matchFilter(filter, filterMap, appInfo.packageName)) {
                        retList.add(appInfo);
                    }
                }
            }
            return retList;
        } else if (filterOption == FILTER_APPS_THIRD_PARTY) {
            for (ApplicationInfo appInfo : pAppList) {
                boolean flag = false;
                if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    flag = true;
                } else if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    flag = true;
                }
                if (flag) {
                    if (matchFilter(filter, filterMap, appInfo.packageName)) {
                        retList.add(appInfo);
                    }
                }
            }
            return retList;
        } else if (filterOption == FILTER_APPS_RUNNING) {
            List<ActivityManager.RunningAppProcessInfo> procList = getRunningAppProcessesList();
            if ((procList == null) || (procList.size() == 0)) {
                return retList;
            }
            HashMap<String, ActivityManager.RunningAppProcessInfo> runningMap = 
                new HashMap<String, ActivityManager.RunningAppProcessInfo>();
            for (ActivityManager.RunningAppProcessInfo appProcInfo : procList) {
                if ((appProcInfo != null)  && (appProcInfo.pkgList != null)){
                    int size = appProcInfo.pkgList.length;
                    for (int i = 0; i < size; i++) {
                        runningMap.put(appProcInfo.pkgList[i], appProcInfo);
                    }
                }
            }
            for (ApplicationInfo appInfo : pAppList) {
                if (runningMap.get(appInfo.packageName) != null) {
                    if (matchFilter(filter, filterMap, appInfo.packageName)) {
                        retList.add(appInfo);
                    }
                }
            }
            return retList;
        } else {
            for (ApplicationInfo appInfo : pAppList) {
                if (matchFilter(filter, filterMap, appInfo.packageName)) {
                    retList.add(appInfo);
                }
            }
            return retList;
        }
    }
    private List<ActivityManager.RunningAppProcessInfo> getRunningAppProcessesList() {
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        return am.getRunningAppProcesses();
    }
    private void initAppList(List<ApplicationInfo> appList, int filterOption) {
        setProgressBarIndeterminateVisibility(true);
        mComputeSizesFinished = false;
        mLoadLabelsFinished = false;
        mAddRemoveMap = new TreeMap<String, Boolean>();
        mAppInfoAdapter.initMapFromList(appList, filterOption);
    }
    private void initResourceThread() {
        if ((mResourceThread != null) && mResourceThread.isAlive()) {
            mResourceThread.setAbort();
        }
        mResourceThread = new ResourceLoaderThread();
        List<ApplicationInfo> appList = mAppInfoAdapter.getBaseAppList();
        if ((appList != null) && (appList.size()) > 0) {
            mResourceThread.loadAllResources(appList);
        }
    }
    private void initComputeSizes() {
        if (localLOGV) Log.i(TAG, "Initiating compute sizes for first time");
        if ((mSizeComputor != null) && (mSizeComputor.isAlive())) {
            mSizeComputor.setAbort();
        }
        List<ApplicationInfo> appList = mAppInfoAdapter.getBaseAppList();
        if ((appList != null) && (appList.size()) > 0) {
            mSizeComputor = new TaskRunner(appList);
        } else {
            mComputeSizesFinished = true;
        }
    }
    static class AddRemoveInfo {
        String pkgName;
        boolean add;
        public AddRemoveInfo(String pPkgName, boolean pAdd) {
            pkgName = pPkgName;
            add = pAdd;
        }
    }
    class ResourceLoaderThread extends Thread {
        List<ApplicationInfo> mAppList;
        volatile boolean abort = false;
        static final int MSG_PKG_SIZE = 8;
        public void setAbort() {
            abort = true;
        }
        void loadAllResources(List<ApplicationInfo> appList) {
            mAppList = appList;
            start();
        }
        public void run() {
            long start;
            if (DEBUG_TIME) {
                start = SystemClock.elapsedRealtime();
            }
            int imax;
            if(mAppList == null || (imax = mAppList.size()) <= 0) {
                Log.w(TAG, "Empty or null application list");
            } else {
                int size = mAppList.size();
                int numMsgs = size / MSG_PKG_SIZE;
                if (size > (numMsgs * MSG_PKG_SIZE)) {
                    numMsgs++;
                }
                int endi = 0;
                for (int j = 0; j < size; j += MSG_PKG_SIZE) {
                    Map<String, CharSequence> map = new HashMap<String, CharSequence>();
                    endi += MSG_PKG_SIZE;
                    if (endi > size) {
                        endi = size;
                    }
                    for (int i = j; i < endi; i++) {
                        if (abort) {
                            break;
                        }
                        ApplicationInfo appInfo = mAppList.get(i);
                        map.put(appInfo.packageName, appInfo.loadLabel(mPm));
                    }
                    Message msg = mHandler.obtainMessage(REFRESH_LABELS);
                    msg.obj = map;
                    mHandler.sendMessage(msg);
                }
                Message doneMsg = mHandler.obtainMessage(REFRESH_DONE);
                mHandler.sendMessage(doneMsg);
                if (DEBUG_TIME) Log.i(TAG, "Took "+(SystemClock.elapsedRealtime()-start)+
                        " ms to load app labels");
                long startIcons;
                if (DEBUG_TIME) {
                    startIcons = SystemClock.elapsedRealtime();
                }
                Map<String, Drawable> map = new HashMap<String, Drawable>();
                for (int i = (imax-1); i >= 0; i--) {
                    if (abort) {
                        return;
                    }
                    ApplicationInfo appInfo = mAppList.get(i);
                    map.put(appInfo.packageName, appInfo.loadIcon(mPm));
                }
                Message msg = mHandler.obtainMessage(REFRESH_ICONS);
                msg.obj = map;
                mHandler.sendMessage(msg);
                if (DEBUG_TIME) Log.i(TAG, "Took "+(SystemClock.elapsedRealtime()-startIcons)+" ms to load app icons");
            }
            if (DEBUG_TIME) Log.i(TAG, "Took "+(SystemClock.elapsedRealtime()-start)+" ms to load app resources");
        }
    }
    static private class AppInfo {
        public String pkgName;
        int index;
        public CharSequence appName;
        public Drawable appIcon;
        public CharSequence appSize;
        long size;
        public void refreshIcon(Drawable icon) {
            if (icon == null) {
                return;
            }
            appIcon = icon;
        }
        public void refreshLabel(CharSequence label) {
            if (label == null) {
                return;
            }
            appName = label;
        }
        public AppInfo(String pName, int pIndex, CharSequence aName,
                long pSize,
                CharSequence pSizeStr) {
            this(pName, pIndex, aName, mDefaultAppIcon, pSize, pSizeStr);
        }
        public AppInfo(String pName, int pIndex, CharSequence aName, Drawable aIcon,
                long pSize,
                CharSequence pSizeStr) {
            index = pIndex;
            pkgName = pName;
            appName = aName;
            appIcon = aIcon;
            size = pSize;
            appSize = pSizeStr;
        }
        public boolean setSize(long newSize, String formattedSize) {
            if (size != newSize) {
                size = newSize;
                appSize = formattedSize;
                return true;
            }
            return false;
        }
    }
    private long getTotalSize(PackageStats ps) {
        if (ps != null) {
            return ps.cacheSize+ps.codeSize+ps.dataSize;
        }
        return SIZE_INVALID;
    }
    private CharSequence getSizeStr(long size) {
        CharSequence appSize = null;
        if (size == SIZE_INVALID) {
             return mInvalidSizeStr;
        }
        appSize = Formatter.formatFileSize(ManageApplications.this, size);
        return appSize;
    }
    static class AppViewHolder {
        TextView appName;
        ImageView appIcon;
        TextView appSize;
    }
    class AppInfoAdapter extends BaseAdapter implements Filterable {   
        private List<ApplicationInfo> mAppList;
        private List<ApplicationInfo> mAppLocalList;
        private Map<String, String> mFilterMap = new HashMap<String, String>();
        AlphaComparator mAlphaComparator = new AlphaComparator();
        SizeComparator mSizeComparator = new SizeComparator();
        private Filter mAppFilter = new AppFilter();
        final private Object mFilterLock = new Object();
        private Map<String, String> mCurrentFilterMap = null;
        private void generateFilterListLocked(List<ApplicationInfo> list) {
            mAppLocalList = new ArrayList<ApplicationInfo>(list);
            synchronized(mFilterLock) {
                for (ApplicationInfo info : mAppLocalList) {
                    String label = info.packageName;
                    AppInfo aInfo = mCache.getEntry(info.packageName);
                    if ((aInfo != null) && (aInfo.appName != null)) {
                        label = aInfo.appName.toString();
                    }
                    mFilterMap.put(info.packageName, label.toLowerCase());
                }
            }
        }
        private void addFilterListLocked(int newIdx, ApplicationInfo info, CharSequence pLabel) {
            mAppLocalList.add(newIdx, info);
            synchronized (mFilterLock) {
                String label = info.packageName;
                if (pLabel != null) {
                    label = pLabel.toString();
                }
                mFilterMap.put(info.packageName, label.toLowerCase());
            }
        }
        private boolean removeFilterListLocked(String removePkg) {
            int N = mAppLocalList.size();
            int i;
            for (i = (N-1); i >= 0; i--) {
                ApplicationInfo info = mAppLocalList.get(i);
                if (info.packageName.equalsIgnoreCase(removePkg)) {
                    if (localLOGV) Log.i(TAG, "Removing " + removePkg + " from local list");
                    mAppLocalList.remove(i);
                    synchronized (mFilterLock) {
                        mFilterMap.remove(removePkg);
                    }
                    return true;
                }
            }
            return false;
        }
        private void reverseGenerateList() {
            generateFilterListLocked(getFilteredApps(mAppList, mFilterApps, mCurrentFilterMap!= null, mCurrentFilterMap));
            sortListInner(mSortOrder);
        }
        public void initMapFromList(List<ApplicationInfo> pAppList, int filterOption) {
            boolean notify = false;
            List<ApplicationInfo> appList = null;
            if (pAppList == null) {
                appList = mAppList;
            } else {
                mAppList = new ArrayList<ApplicationInfo>(pAppList);
                appList = pAppList;
                notify = true;
            }
            generateFilterListLocked(getFilteredApps(appList, filterOption, mCurrentFilterMap!= null, mCurrentFilterMap));
            int imax = appList.size();
            for (int i = 0; i < imax; i++) {
                ApplicationInfo info  = appList.get(i);
                AppInfo aInfo = mCache.getEntry(info.packageName);
                if(aInfo == null){
                    aInfo = new AppInfo(info.packageName, i, 
                            info.packageName, -1, mComputingSizeStr);
                    if (localLOGV) Log.i(TAG, "Creating entry pkg:"+info.packageName+" to map");
                    mCache.addEntry(aInfo);
                }
            }
            sortListInner(mSortOrder);
            if (notify) {
                notifyDataSetChanged();
            }
        }
        public AppInfoAdapter(Context c, List<ApplicationInfo> appList) {
           mAppList = appList;
        }
        public int getCount() {
            return mAppLocalList.size();
        }
        public Object getItem(int position) {
            return mAppLocalList.get(position);
        }
        public boolean isInstalled(String pkgName) {
            if(pkgName == null) {
                if (localLOGV) Log.w(TAG, "Null pkg name when checking if installed");
                return false;
            }
            for (ApplicationInfo info : mAppList) {
                if (info.packageName.equalsIgnoreCase(pkgName)) {
                    return true;
                }
            }
            return false;
        }
        public ApplicationInfo getApplicationInfo(int position) {
            int imax = mAppLocalList.size();
            if( (position < 0) || (position >= imax)) {
                Log.w(TAG, "Position out of bounds in List Adapter");
                return null;
            }
            return mAppLocalList.get(position);
        }
        public long getItemId(int position) {
            int imax = mAppLocalList.size();
            if( (position < 0) || (position >= imax)) {
                Log.w(TAG, "Position out of bounds in List Adapter");
                return -1;
            }
            AppInfo aInfo = mCache.getEntry(mAppLocalList.get(position).packageName);
            if (aInfo == null) {
                return -1;
            }
            return aInfo.index;
        }
        public List<ApplicationInfo> getBaseAppList() {
            return mAppList;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position >= mAppLocalList.size()) {
                Log.w(TAG, "Invalid view position:"+position+", actual size is:"+mAppLocalList.size());
                return null;
            }
            AppViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.manage_applications_item, null);
                holder = new AppViewHolder();
                holder.appName = (TextView) convertView.findViewById(R.id.app_name);
                holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
                holder.appSize = (TextView) convertView.findViewById(R.id.app_size);
                convertView.setTag(holder);
            } else {
                holder = (AppViewHolder) convertView.getTag();
            }
            ApplicationInfo appInfo = mAppLocalList.get(position);
            AppInfo mInfo = mCache.getEntry(appInfo.packageName);
            if(mInfo != null) {
                if(mInfo.appName != null) {
                    holder.appName.setText(mInfo.appName);
                }
                if(mInfo.appIcon != null) {
                    holder.appIcon.setImageDrawable(mInfo.appIcon);
                }
                if (mInfo.appSize != null) {
                    holder.appSize.setText(mInfo.appSize);
                }
            } else {
                Log.w(TAG, "No info for package:"+appInfo.packageName+" in property map");
            }
            return convertView;
        }
        private void adjustIndex() {
            int imax = mAppLocalList.size();
            for (int i = 0; i < imax; i++) {
                ApplicationInfo info = mAppLocalList.get(i);
                mCache.getEntry(info.packageName).index = i;
            }
        }
        public void sortAppList(List<ApplicationInfo> appList, int sortOrder) {
            Collections.sort(appList, getAppComparator(sortOrder));
        }
        public void sortBaseList(int sortOrder) {
            if (localLOGV) Log.i(TAG, "Sorting base list based on sortOrder = "+sortOrder);
            sortAppList(mAppList, sortOrder);
            generateFilterListLocked(getFilteredApps(mAppList, mFilterApps, mCurrentFilterMap!= null, mCurrentFilterMap));
            adjustIndex();
        }
        private void sortListInner(int sortOrder) {
            sortAppList(mAppLocalList, sortOrder);
            adjustIndex(); 
        }
        public void sortList(int sortOrder) {
            if (localLOGV) Log.i(TAG, "sortOrder = "+sortOrder);
            sortListInner(sortOrder);
            notifyDataSetChanged();
        }
        public boolean resetAppList(int filterOption) {
           generateFilterListLocked(getFilteredApps(mAppList, filterOption, mCurrentFilterMap!= null, mCurrentFilterMap));
           for(ApplicationInfo applicationInfo : mAppLocalList) {
               AppInfo appInfo = mCache.getEntry(applicationInfo.packageName);
               if(appInfo == null) {
                  Log.i(TAG, " Entry does not exist for pkg:  " + applicationInfo.packageName);
               }
           }
           if (mAppLocalList.size() > 0) {
               sortList(mSortOrder);
           } else {
               notifyDataSetChanged();
           }
           return true;
        }
        private Comparator<ApplicationInfo> getAppComparator(int sortOrder) {
            if (sortOrder == SORT_ORDER_ALPHA) {
                return mAlphaComparator;
            }
            return mSizeComparator;
        }
        public void bulkUpdateIcons(Map<String, Drawable> icons) {
            if (icons == null) {
                return;
            }
            Set<String> keys = icons.keySet();
            boolean changed = false;
            for (String key : keys) {
                Drawable ic = icons.get(key);
                if (ic != null) {
                    AppInfo aInfo = mCache.getEntry(key);
                    if (aInfo != null) {
                        aInfo.refreshIcon(ic);
                        changed = true;
                    }
                }
            }
            if (changed) {
                notifyDataSetChanged();
            }
        }
        public void bulkUpdateLabels(Map<String, CharSequence> map) {
            if (map == null) {
                return;
            }
            Set<String> keys = map.keySet();
            boolean changed = false;
            for (String key : keys) {
                CharSequence label = map.get(key);
                AppInfo aInfo = mCache.getEntry(key);
                if (aInfo != null) {
                    aInfo.refreshLabel(label);
                    changed = true;
                }
            }
            if (changed) {
                notifyDataSetChanged();
            }
        }
        private boolean shouldBeInList(int filterOption, ApplicationInfo info) {
            if (filterOption == FILTER_APPS_RUNNING) {
                List<ApplicationInfo> runningList = getInstalledApps(FILTER_APPS_RUNNING);
                for (ApplicationInfo running : runningList) {
                    if (running.packageName.equalsIgnoreCase(info.packageName)) {
                        return true;
                    }
                }
            } else if (filterOption == FILTER_APPS_THIRD_PARTY) {
                if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    return true;
                } else if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    return true;
                }
            } else if (filterOption == FILTER_APPS_SDCARD) {
                if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                    return true;
                }
            } else {
                return true;
            }
            return false;
        }
        public void addToList(String pkgName, long size, String formattedSize) {
            if (pkgName == null) {
                return;
            }
            ApplicationInfo info = null;
            try {
                info = mPm.getApplicationInfo(pkgName, 0);
            } catch (NameNotFoundException e) {
                Log.w(TAG, "Ignoring non-existent package:"+pkgName);
                return;
            }
            if(info == null) {
                Log.i(TAG, "Null ApplicationInfo for package:"+pkgName);
                return;
            }
            mAppList.add(info);
            CharSequence label = info.loadLabel(mPm);
            mCache.addEntry(new AppInfo(pkgName, -1,
                    label, info.loadIcon(mPm), size, formattedSize));
            if (addLocalEntry(info, label)) {
                notifyDataSetChanged();
            }
        }
        private boolean addLocalEntry(ApplicationInfo info, CharSequence label) {
            String pkgName = info.packageName;
            if (shouldBeInList(mFilterApps, info)) {
                int newIdx = Collections.binarySearch(mAppLocalList, info, 
                        getAppComparator(mSortOrder));
                if(newIdx >= 0) {
                    if (localLOGV) Log.i(TAG, "Strange. Package:" + pkgName + " is not new");
                    return false;
                }
                newIdx = -newIdx-1;
                addFilterListLocked(newIdx, info, label);
                adjustIndex();
                return true;
            }
            return false;
        }
        public void updatePackage(String pkgName,
                long size, String formattedSize) {
            ApplicationInfo info = null;
            try {
                info = mPm.getApplicationInfo(pkgName,
                        PackageManager.GET_UNINSTALLED_PACKAGES);
            } catch (NameNotFoundException e) {
                return;
            }
            AppInfo aInfo = mCache.getEntry(pkgName);
            if (aInfo != null) {
                CharSequence label = info.loadLabel(mPm);
                aInfo.refreshLabel(label);
                aInfo.refreshIcon(info.loadIcon(mPm));
                aInfo.setSize(size, formattedSize);
                addLocalEntry(info, label);
                notifyDataSetChanged();
            }
        }
        private void removePkgBase(String pkgName) {
            int imax = mAppList.size();
            for (int i = 0; i < imax; i++) {
                ApplicationInfo app = mAppList.get(i);
                if (app.packageName.equalsIgnoreCase(pkgName)) {
                    if (localLOGV) Log.i(TAG, "Removing pkg: "+pkgName+" from base list");
                    mAppList.remove(i);
                    return;
                }
            }
        }
        public void removeFromList(List<String> pkgNames) {
            if(pkgNames == null) {
                return;
            }
            if(pkgNames.size()  <= 0) {
                return;
            }
            boolean found = false;
            for (String pkg : pkgNames) {
                removePkgBase(pkg);
                if (localLOGV) Log.i(TAG, "Removing " + pkg + " from cache");
                mCache.removeEntry(pkg);
                if (removeFilterListLocked(pkg)) {
                    found = true;
                }
            }
            if (found) {
                adjustIndex();
                if (localLOGV) Log.i(TAG, "adjusting index and notifying list view");
                notifyDataSetChanged();
            }
        }
        public void bulkUpdateSizes(String pkgs[], long sizes[], String formatted[]) {
            if(pkgs == null || sizes == null || formatted == null) {
                return;
            }
            boolean changed = false;
            for (int i = 0; i < pkgs.length; i++) {
                AppInfo entry = mCache.getEntry(pkgs[i]);
                if (entry == null) {
                    if (localLOGV) Log.w(TAG, "Entry for package:"+ pkgs[i] +"doesn't exist in map");
                    continue;
                }
                if (entry.setSize(sizes[i], formatted[i])) {
                    changed = true;
                }
            }
            if (changed) {
                notifyDataSetChanged();
            }
        }
        public Filter getFilter() {
            return mAppFilter;
        }
        private class AppFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence prefix) {
                FilterResults results = new FilterResults();
                if (prefix == null || prefix.length() == 0) {
                    synchronized (mFilterLock) {
                        results.values = new HashMap<String, String>(mFilterMap);
                        results.count = mFilterMap.size();
                    }
                } else {
                    final String prefixString = prefix.toString().toLowerCase();
                    final String spacePrefixString = " " + prefixString;
                    Map<String, String> newMap = new HashMap<String, String>();
                    synchronized (mFilterLock) {
                        Map<String, String> localMap = mFilterMap;
                        Set<String> keys = mFilterMap.keySet();
                        for (String key : keys) {
                            String label = localMap.get(key);
                            if (label == null) continue;
                            label = label.toLowerCase();
                            if (label.startsWith(prefixString)
                                    || label.indexOf(spacePrefixString) != -1) {
                                newMap.put(key, label);
                            }
                        }
                    }
                    results.values = newMap;
                    results.count = newMap.size();
                }
                return results;
            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mCurrentFilterMap = (Map<String, String>) results.values;
                reverseGenerateList();
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    }
    private void clearMessagesInHandler() {
        mHandler.removeMessages(INIT_PKG_INFO);
        mHandler.removeMessages(COMPUTE_BULK_SIZE);
        mHandler.removeMessages(REMOVE_PKG);
        mHandler.removeMessages(REORDER_LIST);
        mHandler.removeMessages(ADD_PKG_START);
        mHandler.removeMessages(ADD_PKG_DONE);
        mHandler.removeMessages(REFRESH_LABELS);
        mHandler.removeMessages(REFRESH_DONE);
        mHandler.removeMessages(NEXT_LOAD_STEP);
        mHandler.removeMessages(COMPUTE_END);
    }
    private void sendMessageToHandler(int msgId, int arg1) {
        Message msg = mHandler.obtainMessage(msgId);
        msg.arg1 = arg1;
        mHandler.sendMessage(msg);
    }
    private void sendMessageToHandler(int msgId, Bundle data) {
        Message msg = mHandler.obtainMessage(msgId);
        msg.setData(data);
        mHandler.sendMessage(msg);
    }
    private void sendMessageToHandler(int msgId) {
        mHandler.sendEmptyMessage(msgId);
    }
    class PkgSizeObserver extends IPackageStatsObserver.Stub {
        String pkgName;
        public void onGetStatsCompleted(PackageStats pStats, boolean pSucceeded) {
            if(DEBUG_PKG_DELAY) {
                try {
                    Thread.sleep(10*1000);
                } catch (InterruptedException e) {
                }
            }
            Bundle data = new Bundle();
            data.putString(ATTR_PKG_NAME, pkgName);
            data.putBoolean(ATTR_GET_SIZE_STATUS, pSucceeded);
            if(pSucceeded && pStats != null) {
                if (localLOGV) Log.i(TAG, "onGetStatsCompleted::"+pkgName+", ("+
                        pStats.cacheSize+","+
                        pStats.codeSize+", "+pStats.dataSize);
                long total = getTotalSize(pStats);
                data.putLong(ATTR_PKG_STATS, total);
                CharSequence sizeStr = getSizeStr(total);
                data.putString(ATTR_PKG_SIZE_STR, sizeStr.toString());
            } else {
                Log.w(TAG, "Invalid package stats from PackageManager");
            }
            Message msg = mHandler.obtainMessage(ADD_PKG_DONE, data);
            msg.setData(data);
            mHandler.sendMessage(msg);
        }
        public void invokeGetSizeInfo(String packageName) {
            if (packageName == null) {
                return;
            }
            pkgName = packageName;
            if(localLOGV) Log.i(TAG, "Invoking getPackageSizeInfo for package:"+
                    packageName);
            mPm.getPackageSizeInfo(packageName, this);
        }
    }
    private class PackageIntentReceiver extends BroadcastReceiver {
         void registerReceiver() {
             IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
             filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
             filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
             filter.addDataScheme("package");
             ManageApplications.this.registerReceiver(this, filter);
             IntentFilter sdFilter = new IntentFilter();
             sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
             sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
             ManageApplications.this.registerReceiver(this, sdFilter);
         }
         @Override
         public void onReceive(Context context, Intent intent) {
             String actionStr = intent.getAction();
             if (Intent.ACTION_PACKAGE_ADDED.equals(actionStr) ||
                     Intent.ACTION_PACKAGE_REMOVED.equals(actionStr)) {
                 Uri data = intent.getData();
                 String pkgName = data.getEncodedSchemeSpecificPart();
                 updatePackageList(actionStr, pkgName);
             } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(actionStr) ||
                     Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(actionStr)) {
                 String pkgList[] = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
                 if (pkgList == null || pkgList.length == 0) {
                     return;
                 }
                 for (String pkgName : pkgList) {
                     updatePackageList(Intent.ACTION_PACKAGE_REMOVED, pkgName);
                     updatePackageList(Intent.ACTION_PACKAGE_ADDED, pkgName);
                 }
             }
         }
    }
    private void updatePackageList(String actionStr, String pkgName) {
        if (Intent.ACTION_PACKAGE_ADDED.equalsIgnoreCase(actionStr)) {
            Bundle data = new Bundle();
            data.putString(ATTR_PKG_NAME, pkgName);
            sendMessageToHandler(ADD_PKG_START, data);
        } else if (Intent.ACTION_PACKAGE_REMOVED.equalsIgnoreCase(actionStr)) {
            Bundle data = new Bundle();
            data.putString(ATTR_PKG_NAME, pkgName);
            sendMessageToHandler(REMOVE_PKG, data);
        }
    }
    static final String TAB_DOWNLOADED = "Downloaded";
    static final String TAB_RUNNING = "Running";
    static final String TAB_ALL = "All";
    static final String TAB_SDCARD = "OnSdCard";
    private View mRootView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(localLOGV) Log.i(TAG, "Activity created");
        long sCreate;
        if (DEBUG_TIME) {
            sCreate = SystemClock.elapsedRealtime();
        }
        Intent intent = getIntent();
        String action = intent.getAction();
        String defaultTabTag = TAB_DOWNLOADED;
        if (action.equals(Intent.ACTION_MANAGE_PACKAGE_STORAGE)) {
            mSortOrder = SORT_ORDER_SIZE;
            mFilterApps = FILTER_APPS_ALL;
            defaultTabTag = TAB_ALL;
            mSizesFirst = true;
        }
        mPm = getPackageManager();
        requestWindowFeature(Window.FEATURE_RIGHT_ICON);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        showLoadingMsg();
        mDefaultAppIcon = Resources.getSystem().getDrawable(
                com.android.internal.R.drawable.sym_def_app_icon);
        mInvalidSizeStr = getText(R.string.invalid_size_value);
        mComputingSizeStr = getText(R.string.computing_size);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = mInflater.inflate(R.layout.compute_sizes, null);
        mReceiver = new PackageIntentReceiver();
        mObserver = new PkgSizeObserver();
        List<ApplicationInfo> appList = getInstalledApps(FILTER_APPS_ALL);
        mAppInfoAdapter = new AppInfoAdapter(this, appList);
        ListView lv = (ListView) mRootView.findViewById(android.R.id.list);
        lv.setOnItemClickListener(this);
        lv.setSaveEnabled(true);
        lv.setItemsCanFocus(true);
        lv.setOnItemClickListener(this);
        lv.setTextFilterEnabled(true);
        mListView = lv;
        if (DEBUG_TIME) {
            Log.i(TAG, "Total time in Activity.create:: " +
                    (SystemClock.elapsedRealtime() - sCreate)+ " ms");
        }
        long sStart;
        if (DEBUG_TIME) {
            sStart = SystemClock.elapsedRealtime();
        }
        mCache.loadCache();
        if (DEBUG_TIME) {
            Log.i(TAG, "Took " + (SystemClock.elapsedRealtime()-sStart) + " ms to init cache");
        }
        final TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec(TAB_DOWNLOADED)
                .setIndicator(getString(R.string.filter_apps_third_party),
                        getResources().getDrawable(R.drawable.ic_tab_download))
                .setContent(this));
        tabHost.addTab(tabHost.newTabSpec(TAB_RUNNING)
                .setIndicator(getString(R.string.filter_apps_running),
                        getResources().getDrawable(R.drawable.ic_tab_running))
                .setContent(this));
        tabHost.addTab(tabHost.newTabSpec(TAB_ALL)
                .setIndicator(getString(R.string.filter_apps_all),
                        getResources().getDrawable(R.drawable.ic_tab_all))
                .setContent(this));
        tabHost.addTab(tabHost.newTabSpec(TAB_SDCARD)
                .setIndicator(getString(R.string.filter_apps_onsdcard),
                        getResources().getDrawable(R.drawable.ic_tab_sdcard))
                .setContent(this));
        tabHost.setCurrentTabByTag(defaultTabTag);
        tabHost.setOnTabChangedListener(this);
    }
    @Override
    protected void onDestroy() {
        mCache.updateCache();
        super.onDestroy();
    }
    @Override
    public Dialog onCreateDialog(int id, Bundle args) {
        if (id == DLG_LOADING) {
            ProgressDialog dlg = new ProgressDialog(this);
            dlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dlg.setMessage(getText(R.string.loading));
            dlg.setIndeterminate(true);        
            dlg.setOnCancelListener(this);
            return dlg;
        }
        return null;
    }
    private void showLoadingMsg() {
        if (DEBUG_TIME) {
            mLoadTimeStart = SystemClock.elapsedRealtime();
        }
        showDialog(DLG_LOADING); 
        if(localLOGV) Log.i(TAG, "Displaying Loading message");
    }
    private void dismissLoadingMsg() {
        if(localLOGV) Log.i(TAG, "Dismissing Loading message");
        dismissDialog(DLG_LOADING);
        if (DEBUG_TIME) Log.i(TAG, "Displayed loading message for "+
                (SystemClock.elapsedRealtime() - mLoadTimeStart) + " ms");
    }
    class AppInfoCache {
        final static boolean FILE_CACHE = true;
        private static final String mFileCacheName="ManageAppsInfo.txt";
        private static final int FILE_BUFFER_SIZE = 1024;
        private static final boolean DEBUG_CACHE = false;
        private static final boolean DEBUG_CACHE_TIME = false;
        private Map<String, AppInfo> mAppPropCache = new HashMap<String, AppInfo>();
        private boolean isEmpty() {
            return (mAppPropCache.size() == 0);
        }
        private AppInfo getEntry(String pkgName) {
            return mAppPropCache.get(pkgName);
        }
        private Set<String> getPkgList() {
            return mAppPropCache.keySet();
        }
        public void addEntry(AppInfo aInfo) {
            if ((aInfo != null) && (aInfo.pkgName != null)) {
                mAppPropCache.put(aInfo.pkgName, aInfo);
            }
        }
        public void removeEntry(String pkgName) {
            if (pkgName != null) {
                mAppPropCache.remove(pkgName);
            }
        }
        private void readFromFile() {
            File cacheFile = new File(getFilesDir(), mFileCacheName);
            if (!cacheFile.exists()) {
                return;
            }
            FileInputStream fis = null;
            boolean err = false;
            try {
                fis = new FileInputStream(cacheFile);
            } catch (FileNotFoundException e) {
                Log.w(TAG, "Error opening file for read operation : " + cacheFile
                        + " with exception " + e);
                return;
            }
            try {
                byte[] byteBuff = new byte[FILE_BUFFER_SIZE];
                byte[] lenBytes = new byte[2];
                mAppPropCache.clear();
                while(fis.available() > 0) {
                    fis.read(lenBytes, 0, 2);
                    int buffLen = (lenBytes[0] << 8) | lenBytes[1];
                    if ((buffLen <= 0) || (buffLen > byteBuff.length)) {
                        err = true;
                        break;
                    }
                    fis.read(byteBuff, 0, buffLen);
                    String buffStr = new String(byteBuff);
                    if (DEBUG_CACHE) {
                        Log.i(TAG, "Read string of len= " + buffLen + " :: " + buffStr + " from file");
                    }
                    String substrs[] = buffStr.split(",");
                    if (substrs.length < 4) {
                        err = true;
                        break;
                    }
                    long size = -1;
                    int idx = -1;
                    try {
                        size = Long.parseLong(substrs[1]);
                    } catch (NumberFormatException e) {
                        err = true;
                        break;
                    }
                    if (DEBUG_CACHE) {
                        Log.i(TAG, "Creating entry(" + substrs[0] + ", " + idx+"," + size + ", " + substrs[2] + ")");
                    }
                    AppInfo aInfo = new AppInfo(substrs[0], idx, substrs[3], size, substrs[2]);
                    mAppPropCache.put(aInfo.pkgName, aInfo);
                }
            } catch (IOException e) {
                Log.w(TAG, "Failed reading from file : " + cacheFile + " with exception : " + e);
                err = true;
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        Log.w(TAG, "Failed to close file " + cacheFile + " with exception : " +e);
                        err = true;
                    }
                }
                if (err) {
                    Log.i(TAG, "Failed to load cache. Not using cache for now.");
                    mAppPropCache.clear();
                }
            }
        }
        boolean writeToFile() {
            File cacheFile = new File(getFilesDir(), mFileCacheName);
            FileOutputStream fos = null;
            try {
                long opStartTime = SystemClock.uptimeMillis();
                fos = new FileOutputStream(cacheFile);
                Set<String> keys = mAppPropCache.keySet();
                byte[] lenBytes = new byte[2];
                for (String key : keys) {
                    AppInfo aInfo = mAppPropCache.get(key);
                    StringBuilder buff = new StringBuilder(aInfo.pkgName);
                    buff.append(",");
                    buff.append(aInfo.size);
                    buff.append(",");
                    buff.append(aInfo.appSize);
                    buff.append(",");
                    buff.append(aInfo.appName);
                    if (DEBUG_CACHE) {
                        Log.i(TAG, "Writing str : " + buff.toString() + " to file of length:" +
                                buff.toString().length());
                    }
                    try {
                        byte[] byteBuff = buff.toString().getBytes();
                        int len = byteBuff.length;
                        if (byteBuff.length >= FILE_BUFFER_SIZE) {
                            len = FILE_BUFFER_SIZE;
                        }
                        lenBytes[1] = (byte) (len & 0x00ff);
                        lenBytes[0] = (byte) ((len & 0x00ff00) >> 8);
                        fos.write(lenBytes, 0, 2);
                        fos.write(byteBuff, 0, len);
                    } catch (IOException e) {
                        Log.w(TAG, "Failed to write to file : " + cacheFile + " with exception : " + e);
                        return false;
                    }
                }
                if (DEBUG_CACHE_TIME) {
                    Log.i(TAG, "Took " + (SystemClock.uptimeMillis() - opStartTime) + " ms to write and process from file");
                }
                return true;
            } catch (FileNotFoundException e) {
                Log.w(TAG, "Error opening file for write operation : " + cacheFile+
                        " with exception : " + e);
                return false;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        Log.w(TAG, "Failed closing file : " + cacheFile + " with exception : " + e);
                        return false;
                    }
                }
            }
        }
        private void loadCache() {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            boolean disable = settings.getBoolean(PREF_DISABLE_CACHE, true);
            if (disable) Log.w(TAG, "Cache has been disabled");
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(PREF_DISABLE_CACHE, true);
            editor.commit();
            if (FILE_CACHE && !disable) {
                readFromFile();
                editor.putBoolean(PREF_DISABLE_CACHE, false);
                editor.commit();
            }
        }
        private void updateCache() {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(PREF_DISABLE_CACHE, true);
            editor.commit();
            if (FILE_CACHE) {
                boolean writeStatus = writeToFile();
                mAppPropCache.clear();
                if (writeStatus) {
                    editor.putBoolean(PREF_DISABLE_CACHE, false);
                    editor.commit();
                }
            }
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        mReceiver.registerReceiver();
        sendMessageToHandler(INIT_PKG_INFO);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mResourceThread != null) {
            mResourceThread.setAbort();
        }
        if (mSizeComputor != null) {
            mSizeComputor.setAbort();
        }
        clearMessagesInHandler();
        unregisterReceiver(mReceiver);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    class SizeComparator implements Comparator<ApplicationInfo> {
        public final int compare(ApplicationInfo a, ApplicationInfo b) {
            AppInfo ainfo = mCache.getEntry(a.packageName);
            AppInfo binfo = mCache.getEntry(b.packageName);
            long atotal = ainfo.size;
            long btotal = binfo.size;
            long ret = atotal - btotal;
            if (ret < 0) {
                return 1;
            }
            if (ret == 0) {
                return 0;
            }
            return -1;
        }
    }
    class AlphaComparator implements Comparator<ApplicationInfo> {
        private final Collator   sCollator = Collator.getInstance();
        public final int compare(ApplicationInfo a, ApplicationInfo b) {
            AppInfo ainfo = mCache.getEntry(a.packageName);
            AppInfo binfo = mCache.getEntry(b.packageName);
            if (ainfo == null || ainfo.appName == null) return -1;
            if (binfo == null || binfo.appName == null) return 1;
            return sCollator.compare(ainfo.appName.toString(), binfo.appName.toString());
        }
    }
    private void startApplicationDetailsActivity() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(this, InstalledAppDetails.class);
        intent.putExtra(APP_PKG_NAME, mCurrentPkgName);
        startActivityForResult(intent, INSTALLED_APP_DETAILS);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, SORT_ORDER_ALPHA, 1, R.string.sort_order_alpha)
                .setIcon(android.R.drawable.ic_menu_sort_alphabetically);
        menu.add(0, SORT_ORDER_SIZE, 2, R.string.sort_order_size)
                .setIcon(android.R.drawable.ic_menu_sort_by_size); 
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mFirst) {
            menu.findItem(SORT_ORDER_ALPHA).setVisible(mSortOrder != SORT_ORDER_ALPHA);
            menu.findItem(SORT_ORDER_SIZE).setVisible(mSortOrder != SORT_ORDER_SIZE);
            return true;
        }
        return false;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        if ((menuId == SORT_ORDER_ALPHA) || (menuId == SORT_ORDER_SIZE)) {
            sendMessageToHandler(REORDER_LIST, menuId);
        }
        return true;
    }
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        ApplicationInfo info = (ApplicationInfo)mAppInfoAdapter.getItem(position);
        mCurrentPkgName = info.packageName;
        startApplicationDetailsActivity();
    }
    public void onCancel(DialogInterface dialog) {
        finish();
    }
    public View createTabContent(String tag) {
        return mRootView;
    }
    public void onTabChanged(String tabId) {
        int newOption;
        if (TAB_DOWNLOADED.equalsIgnoreCase(tabId)) {
            newOption = FILTER_APPS_THIRD_PARTY;
        } else if (TAB_RUNNING.equalsIgnoreCase(tabId)) {
            newOption = FILTER_APPS_RUNNING;
        } else if (TAB_ALL.equalsIgnoreCase(tabId)) {
            newOption = FILTER_APPS_ALL;
        } else if (TAB_SDCARD.equalsIgnoreCase(tabId)) {
            newOption = FILTER_APPS_SDCARD;
        } else {
            return;
        }
        sendMessageToHandler(REORDER_LIST, newOption);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == INSTALLED_APP_DETAILS && mCurrentPkgName != null) {
            try {
                ApplicationInfo info = mPm.getApplicationInfo(mCurrentPkgName,
                        PackageManager.GET_UNINSTALLED_PACKAGES);
            } catch (NameNotFoundException e) {
                Bundle rData = new Bundle();
                rData.putString(ATTR_PKG_NAME, mCurrentPkgName);
                sendMessageToHandler(REMOVE_PKG, rData);
                mCurrentPkgName = null;
            }
        }
    }
}
