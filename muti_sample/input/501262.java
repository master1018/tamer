class AppWidgetService extends IAppWidgetService.Stub
{
    private static final String TAG = "AppWidgetService";
    private static final String SETTINGS_FILENAME = "appwidgets.xml";
    private static final String SETTINGS_TMP_FILENAME = SETTINGS_FILENAME + ".tmp";
    private static final int MIN_UPDATE_PERIOD = 30 * 60 * 1000; 
    static class Provider {
        int uid;
        AppWidgetProviderInfo info;
        ArrayList<AppWidgetId> instances = new ArrayList<AppWidgetId>();
        PendingIntent broadcast;
        boolean zombie; 
        int tag;    
    }
    static class Host {
        int uid;
        int hostId;
        String packageName;
        ArrayList<AppWidgetId> instances = new ArrayList<AppWidgetId>();
        IAppWidgetHost callbacks;
        boolean zombie; 
        int tag;    
    }
    static class AppWidgetId {
        int appWidgetId;
        Provider provider;
        RemoteViews views;
        Host host;
    }
    Context mContext;
    Locale mLocale;
    PackageManager mPackageManager;
    AlarmManager mAlarmManager;
    ArrayList<Provider> mInstalledProviders = new ArrayList<Provider>();
    int mNextAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID + 1;
    final ArrayList<AppWidgetId> mAppWidgetIds = new ArrayList<AppWidgetId>();
    ArrayList<Host> mHosts = new ArrayList<Host>();
    boolean mSafeMode;
    AppWidgetService(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
        mAlarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
    }
    public void systemReady(boolean safeMode) {
        mSafeMode = safeMode;
        loadAppWidgetList();
        loadStateLocked();
        mContext.registerReceiver(mBroadcastReceiver,
                new IntentFilter(Intent.ACTION_BOOT_COMPLETED), null, null);
        mContext.registerReceiver(mBroadcastReceiver,
                new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED), null, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mBroadcastReceiver, filter);
        IntentFilter sdFilter = new IntentFilter();
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        mContext.registerReceiver(mBroadcastReceiver, sdFilter);
    }
    @Override
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump from from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return;
        }
        synchronized (mAppWidgetIds) {
            int N = mInstalledProviders.size();
            pw.println("Providers:");
            for (int i=0; i<N; i++) {
                Provider p = mInstalledProviders.get(i);
                AppWidgetProviderInfo info = p.info;
                pw.print("  ["); pw.print(i); pw.print("] provider ");
                        pw.print(info.provider.flattenToShortString());
                        pw.println(':');
                pw.print("    min=("); pw.print(info.minWidth);
                        pw.print("x"); pw.print(info.minHeight);
                        pw.print(") updatePeriodMillis=");
                        pw.print(info.updatePeriodMillis);
                        pw.print(" initialLayout=#");
                        pw.print(Integer.toHexString(info.initialLayout));
                        pw.print(" zombie="); pw.println(p.zombie);
            }
            N = mAppWidgetIds.size();
            pw.println(" ");
            pw.println("AppWidgetIds:");
            for (int i=0; i<N; i++) {
                AppWidgetId id = mAppWidgetIds.get(i);
                pw.print("  ["); pw.print(i); pw.print("] id=");
                        pw.println(id.appWidgetId);
                pw.print("    hostId=");
                        pw.print(id.host.hostId); pw.print(' ');
                        pw.print(id.host.packageName); pw.print('/');
                        pw.println(id.host.uid);
                if (id.provider != null) {
                    pw.print("    provider=");
                            pw.println(id.provider.info.provider.flattenToShortString());
                }
                if (id.host != null) {
                    pw.print("    host.callbacks="); pw.println(id.host.callbacks);
                }
                if (id.views != null) {
                    pw.print("    views="); pw.println(id.views);
                }
            }
            N = mHosts.size();
            pw.println(" ");
            pw.println("Hosts:");
            for (int i=0; i<N; i++) {
                Host host = mHosts.get(i);
                pw.print("  ["); pw.print(i); pw.print("] hostId=");
                        pw.print(host.hostId); pw.print(' ');
                        pw.print(host.packageName); pw.print('/');
                        pw.print(host.uid); pw.println(':');
                pw.print("    callbacks="); pw.println(host.callbacks);
                pw.print("    instances.size="); pw.print(host.instances.size());
                        pw.print(" zombie="); pw.println(host.zombie);
            }
        }
    }
    public int allocateAppWidgetId(String packageName, int hostId) {
        int callingUid = enforceCallingUid(packageName);
        synchronized (mAppWidgetIds) {
            int appWidgetId = mNextAppWidgetId++;
            Host host = lookupOrAddHostLocked(callingUid, packageName, hostId);
            AppWidgetId id = new AppWidgetId();
            id.appWidgetId = appWidgetId;
            id.host = host;
            host.instances.add(id);
            mAppWidgetIds.add(id);
            saveStateLocked();
            return appWidgetId;
        }
    }
    public void deleteAppWidgetId(int appWidgetId) {
        synchronized (mAppWidgetIds) {
            AppWidgetId id = lookupAppWidgetIdLocked(appWidgetId);
            if (id != null) {
                deleteAppWidgetLocked(id);
                saveStateLocked();
            }
        }
    }
    public void deleteHost(int hostId) {
        synchronized (mAppWidgetIds) {
            int callingUid = getCallingUid();
            Host host = lookupHostLocked(callingUid, hostId);
            if (host != null) {
                deleteHostLocked(host);
                saveStateLocked();
            }
        }
    }
    public void deleteAllHosts() {
        synchronized (mAppWidgetIds) {
            int callingUid = getCallingUid();
            final int N = mHosts.size();
            boolean changed = false;
            for (int i=N-1; i>=0; i--) {
                Host host = mHosts.get(i);
                if (host.uid == callingUid) {
                    deleteHostLocked(host);
                    changed = true;
                }
            }
            if (changed) {
                saveStateLocked();
            }
        }
    }
    void deleteHostLocked(Host host) {
        final int N = host.instances.size();
        for (int i=N-1; i>=0; i--) {
            AppWidgetId id = host.instances.get(i);
            deleteAppWidgetLocked(id);
        }
        host.instances.clear();
        mHosts.remove(host);
        host.callbacks = null;
    }
    void deleteAppWidgetLocked(AppWidgetId id) {
        Host host = id.host;
        host.instances.remove(id);
        pruneHostLocked(host);
        mAppWidgetIds.remove(id);
        Provider p = id.provider;
        if (p != null) {
            p.instances.remove(id);
            if (!p.zombie) {
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_DELETED);
                intent.setComponent(p.info.provider);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id.appWidgetId);
                mContext.sendBroadcast(intent);
                if (p.instances.size() == 0) {
                    cancelBroadcasts(p);
                    intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_DISABLED);
                    intent.setComponent(p.info.provider);
                    mContext.sendBroadcast(intent);
                }
            }
        }
    }
    void cancelBroadcasts(Provider p) {
        if (p.broadcast != null) {
            mAlarmManager.cancel(p.broadcast);
            long token = Binder.clearCallingIdentity();
            try {
                p.broadcast.cancel();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
            p.broadcast = null;
        }
    }
    public void bindAppWidgetId(int appWidgetId, ComponentName provider) {
        mContext.enforceCallingPermission(android.Manifest.permission.BIND_APPWIDGET,
                "bindGagetId appWidgetId=" + appWidgetId + " provider=" + provider);
        synchronized (mAppWidgetIds) {
            AppWidgetId id = lookupAppWidgetIdLocked(appWidgetId);
            if (id == null) {
                throw new IllegalArgumentException("bad appWidgetId");
            }
            if (id.provider != null) {
                throw new IllegalArgumentException("appWidgetId " + appWidgetId + " already bound to "
                        + id.provider.info.provider);
            }
            Provider p = lookupProviderLocked(provider);
            if (p == null) {
                throw new IllegalArgumentException("not a appwidget provider: " + provider);
            }
            if (p.zombie) {
                throw new IllegalArgumentException("can't bind to a 3rd party provider in"
                        + " safe mode: " + provider);
            }
            id.provider = p;
            p.instances.add(id);
            int instancesSize = p.instances.size();
            if (instancesSize == 1) {
                sendEnableIntentLocked(p);
            }
            sendUpdateIntentLocked(p, new int[] { appWidgetId });
            registerForBroadcastsLocked(p, getAppWidgetIds(p));
            saveStateLocked();
        }
    }
    public AppWidgetProviderInfo getAppWidgetInfo(int appWidgetId) {
        synchronized (mAppWidgetIds) {
            AppWidgetId id = lookupAppWidgetIdLocked(appWidgetId);
            if (id != null && id.provider != null && !id.provider.zombie) {
                return id.provider.info;
            }
            return null;
        }
    }
    public RemoteViews getAppWidgetViews(int appWidgetId) {
        synchronized (mAppWidgetIds) {
            AppWidgetId id = lookupAppWidgetIdLocked(appWidgetId);
            if (id != null) {
                return id.views;
            }
            return null;
        }
    }
    public List<AppWidgetProviderInfo> getInstalledProviders() {
        synchronized (mAppWidgetIds) {
            final int N = mInstalledProviders.size();
            ArrayList<AppWidgetProviderInfo> result = new ArrayList<AppWidgetProviderInfo>(N);
            for (int i=0; i<N; i++) {
                Provider p = mInstalledProviders.get(i);
                if (!p.zombie) {
                    result.add(p.info);
                }
            }
            return result;
        }
    }
    public void updateAppWidgetIds(int[] appWidgetIds, RemoteViews views) {
        if (appWidgetIds == null) {
            return;
        }
        if (appWidgetIds.length == 0) {
            return;
        }
        final int N = appWidgetIds.length;
        synchronized (mAppWidgetIds) {
            for (int i=0; i<N; i++) {
                AppWidgetId id = lookupAppWidgetIdLocked(appWidgetIds[i]);
                updateAppWidgetInstanceLocked(id, views);
            }
        }
    }
    public void updateAppWidgetProvider(ComponentName provider, RemoteViews views) {
        synchronized (mAppWidgetIds) {
            Provider p = lookupProviderLocked(provider);
            if (p == null) {
                Slog.w(TAG, "updateAppWidgetProvider: provider doesn't exist: " + provider);
                return;
            }
            ArrayList<AppWidgetId> instances = p.instances;
            final int N = instances.size();
            for (int i=0; i<N; i++) {
                AppWidgetId id = instances.get(i);
                updateAppWidgetInstanceLocked(id, views);
            }
        }
    }
    void updateAppWidgetInstanceLocked(AppWidgetId id, RemoteViews views) {
        if (id != null && id.provider != null && !id.provider.zombie && !id.host.zombie) {
            id.views = views;
            if (id.host.callbacks != null) {
                try {
                    id.host.callbacks.updateAppWidget(id.appWidgetId, views);
                } catch (RemoteException e) {
                    id.host.callbacks = null;
                }
            }
        }
    }
    public int[] startListening(IAppWidgetHost callbacks, String packageName, int hostId,
            List<RemoteViews> updatedViews) {
        int callingUid = enforceCallingUid(packageName);
        synchronized (mAppWidgetIds) {
            Host host = lookupOrAddHostLocked(callingUid, packageName, hostId);
            host.callbacks = callbacks;
            updatedViews.clear();
            ArrayList<AppWidgetId> instances = host.instances;
            int N = instances.size();
            int[] updatedIds = new int[N];
            for (int i=0; i<N; i++) {
                AppWidgetId id = instances.get(i);
                updatedIds[i] = id.appWidgetId;
                updatedViews.add(id.views);
            }
            return updatedIds;
        }
    }
    public void stopListening(int hostId) {
        synchronized (mAppWidgetIds) {
            Host host = lookupHostLocked(getCallingUid(), hostId);
            if (host != null) {
                host.callbacks = null;
                pruneHostLocked(host);
            }
        }
    }
    boolean canAccessAppWidgetId(AppWidgetId id, int callingUid) {
        if (id.host.uid == callingUid) {
            return true;
        }
        if (id.provider != null && id.provider.uid == callingUid) {
            return true;
        }
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.BIND_APPWIDGET)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
   AppWidgetId lookupAppWidgetIdLocked(int appWidgetId) {
        int callingUid = getCallingUid();
        final int N = mAppWidgetIds.size();
        for (int i=0; i<N; i++) {
            AppWidgetId id = mAppWidgetIds.get(i);
            if (id.appWidgetId == appWidgetId && canAccessAppWidgetId(id, callingUid)) {
                return id;
            }
        }
        return null;
    }
    Provider lookupProviderLocked(ComponentName provider) {
        final String className = provider.getClassName();
        final int N = mInstalledProviders.size();
        for (int i=0; i<N; i++) {
            Provider p = mInstalledProviders.get(i);
            if (p.info.provider.equals(provider) || className.equals(p.info.oldName)) {
                return p;
            }
        }
        return null;
    }
    Host lookupHostLocked(int uid, int hostId) {
        final int N = mHosts.size();
        for (int i=0; i<N; i++) {
            Host h = mHosts.get(i);
            if (h.uid == uid && h.hostId == hostId) {
                return h;
            }
        }
        return null;
    }
    Host lookupOrAddHostLocked(int uid, String packageName, int hostId) {
        final int N = mHosts.size();
        for (int i=0; i<N; i++) {
            Host h = mHosts.get(i);
            if (h.hostId == hostId && h.packageName.equals(packageName)) {
                return h;
            }
        }
        Host host = new Host();
        host.packageName = packageName;
        host.uid = uid;
        host.hostId = hostId;
        mHosts.add(host);
        return host;
    }
    void pruneHostLocked(Host host) {
        if (host.instances.size() == 0 && host.callbacks == null) {
            mHosts.remove(host);
        }
    }
    void loadAppWidgetList() {
        PackageManager pm = mPackageManager;
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        List<ResolveInfo> broadcastReceivers = pm.queryBroadcastReceivers(intent,
                PackageManager.GET_META_DATA);
        final int N = broadcastReceivers == null ? 0 : broadcastReceivers.size();
        for (int i=0; i<N; i++) {
            ResolveInfo ri = broadcastReceivers.get(i);
            addProviderLocked(ri);
        }
    }
    boolean addProviderLocked(ResolveInfo ri) {
        Provider p = parseProviderInfoXml(new ComponentName(ri.activityInfo.packageName,
                    ri.activityInfo.name), ri);
        if (p != null) {
            mInstalledProviders.add(p);
            return true;
        } else {
            return false;
        }
    }
    void removeProviderLocked(int index, Provider p) {
        int N = p.instances.size();
        for (int i=0; i<N; i++) {
            AppWidgetId id = p.instances.get(i);
            updateAppWidgetInstanceLocked(id, null);
            cancelBroadcasts(p);
            id.host.instances.remove(id);
            mAppWidgetIds.remove(id);
            id.provider = null;
            pruneHostLocked(id.host);
            id.host = null;
        }
        p.instances.clear();
        mInstalledProviders.remove(index);
        cancelBroadcasts(p);
    }
    void sendEnableIntentLocked(Provider p) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_ENABLED);
        intent.setComponent(p.info.provider);
        mContext.sendBroadcast(intent);
    }
    void sendUpdateIntentLocked(Provider p, int[] appWidgetIds) {
        if (appWidgetIds != null && appWidgetIds.length > 0) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            intent.setComponent(p.info.provider);
            mContext.sendBroadcast(intent);
        }
    }
    void registerForBroadcastsLocked(Provider p, int[] appWidgetIds) {
        if (p.info.updatePeriodMillis > 0) {
            boolean alreadyRegistered = p.broadcast != null;
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            intent.setComponent(p.info.provider);
            long token = Binder.clearCallingIdentity();
            try {
                p.broadcast = PendingIntent.getBroadcast(mContext, 1, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
            if (!alreadyRegistered) {
                long period = p.info.updatePeriodMillis;
                if (period < MIN_UPDATE_PERIOD) {
                    period = MIN_UPDATE_PERIOD;
                }
                mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + period, period, p.broadcast);
            }
        }
    }
    static int[] getAppWidgetIds(Provider p) {
        int instancesSize = p.instances.size();
        int appWidgetIds[] = new int[instancesSize];
        for (int i=0; i<instancesSize; i++) {
            appWidgetIds[i] = p.instances.get(i).appWidgetId;
        }
        return appWidgetIds;
    }
    public int[] getAppWidgetIds(ComponentName provider) {
        synchronized (mAppWidgetIds) {
            Provider p = lookupProviderLocked(provider);
            if (p != null && getCallingUid() == p.uid) {
                return getAppWidgetIds(p);                
            } else {
                return new int[0];
            }
        }
    }
    private Provider parseProviderInfoXml(ComponentName component, ResolveInfo ri) {
        Provider p = null;
        ActivityInfo activityInfo = ri.activityInfo;
        XmlResourceParser parser = null;
        try {
            parser = activityInfo.loadXmlMetaData(mPackageManager,
                    AppWidgetManager.META_DATA_APPWIDGET_PROVIDER);
            if (parser == null) {
                Slog.w(TAG, "No " + AppWidgetManager.META_DATA_APPWIDGET_PROVIDER + " meta-data for "
                        + "AppWidget provider '" + component + '\'');
                return null;
            }
            AttributeSet attrs = Xml.asAttributeSet(parser);
            int type;
            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                    && type != XmlPullParser.START_TAG) {
            }
            String nodeName = parser.getName();
            if (!"appwidget-provider".equals(nodeName)) {
                Slog.w(TAG, "Meta-data does not start with appwidget-provider tag for"
                        + " AppWidget provider '" + component + '\'');
                return null;
            }
            p = new Provider();
            AppWidgetProviderInfo info = p.info = new AppWidgetProviderInfo();
            info.oldName = activityInfo.metaData.getString(
                    AppWidgetManager.META_DATA_APPWIDGET_OLD_NAME);
            info.provider = component;
            p.uid = activityInfo.applicationInfo.uid;
            Resources res = mPackageManager.getResourcesForApplication(
                    activityInfo.applicationInfo);
            TypedArray sa = res.obtainAttributes(attrs,
                    com.android.internal.R.styleable.AppWidgetProviderInfo);
            TypedValue value = sa.peekValue(
                    com.android.internal.R.styleable.AppWidgetProviderInfo_minWidth);
            info.minWidth = value != null ? value.data : 0; 
            value = sa.peekValue(com.android.internal.R.styleable.AppWidgetProviderInfo_minHeight);
            info.minHeight = value != null ? value.data : 0;
            info.updatePeriodMillis = sa.getInt(
                    com.android.internal.R.styleable.AppWidgetProviderInfo_updatePeriodMillis, 0);
            info.initialLayout = sa.getResourceId(
                    com.android.internal.R.styleable.AppWidgetProviderInfo_initialLayout, 0);
            String className = sa.getString(
                    com.android.internal.R.styleable.AppWidgetProviderInfo_configure);
            if (className != null) {
                info.configure = new ComponentName(component.getPackageName(), className);
            }
            info.label = activityInfo.loadLabel(mPackageManager).toString();
            info.icon = ri.getIconResource();
            sa.recycle();
        } catch (Exception e) {
            Slog.w(TAG, "XML parsing failed for AppWidget provider '" + component + '\'', e);
            return null;
        } finally {
            if (parser != null) parser.close();
        }
        return p;
    }
    int getUidForPackage(String packageName) throws PackageManager.NameNotFoundException {
        PackageInfo pkgInfo = mPackageManager.getPackageInfo(packageName, 0);
        if (pkgInfo == null || pkgInfo.applicationInfo == null) {
            throw new PackageManager.NameNotFoundException();
        }
        return pkgInfo.applicationInfo.uid;
    }
    int enforceCallingUid(String packageName) throws IllegalArgumentException {
        int callingUid = getCallingUid();
        int packageUid;
        try {
            packageUid = getUidForPackage(packageName);
        } catch (PackageManager.NameNotFoundException ex) {
            throw new IllegalArgumentException("packageName and uid don't match packageName="
                    + packageName);
        }
        if (callingUid != packageUid && Process.supportsProcesses()) {
            throw new IllegalArgumentException("packageName and uid don't match packageName="
                    + packageName);
        }
        return callingUid;
    }
    void sendInitialBroadcasts() {
        synchronized (mAppWidgetIds) {
            final int N = mInstalledProviders.size();
            for (int i=0; i<N; i++) {
                Provider p = mInstalledProviders.get(i);
                if (p.instances.size() > 0) {
                    sendEnableIntentLocked(p);
                    int[] appWidgetIds = getAppWidgetIds(p);
                    sendUpdateIntentLocked(p, appWidgetIds);
                    registerForBroadcastsLocked(p, appWidgetIds);
                }
            }
        }
    }
    void loadStateLocked() {
        File temp = savedStateTempFile();
        File real = savedStateRealFile();
        if (real.exists()) {
            readStateFromFileLocked(real);
            if (temp.exists()) {
                temp.delete();
            }
        } else if (temp.exists()) {
            readStateFromFileLocked(temp);
            temp.renameTo(real);
        }
    }
    void saveStateLocked() {
        File temp = savedStateTempFile();
        File real = savedStateRealFile();
        if (!real.exists()) {
            try {
                real.createNewFile();
            } catch (IOException e) {
            }
        }
        if (temp.exists()) {
            temp.delete();
        }
        if (!writeStateToFileLocked(temp)) {
            Slog.w(TAG, "Failed to persist new settings");
            return;
        }
        real.delete();
        temp.renameTo(real);
    }
    boolean writeStateToFileLocked(File file) {
        FileOutputStream stream = null;
        int N;
        try {
            stream = new FileOutputStream(file, false);
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(stream, "utf-8");
            out.startDocument(null, true);
            out.startTag(null, "gs");
            int providerIndex = 0;
            N = mInstalledProviders.size();
            for (int i=0; i<N; i++) {
                Provider p = mInstalledProviders.get(i);
                if (p.instances.size() > 0) {
                    out.startTag(null, "p");
                    out.attribute(null, "pkg", p.info.provider.getPackageName());
                    out.attribute(null, "cl", p.info.provider.getClassName());
                    out.endTag(null, "h");
                    p.tag = providerIndex;
                    providerIndex++;
                }
            }
            N = mHosts.size();
            for (int i=0; i<N; i++) {
                Host host = mHosts.get(i);
                out.startTag(null, "h");
                out.attribute(null, "pkg", host.packageName);
                out.attribute(null, "id", Integer.toHexString(host.hostId));
                out.endTag(null, "h");
                host.tag = i;
            }
            N = mAppWidgetIds.size();
            for (int i=0; i<N; i++) {
                AppWidgetId id = mAppWidgetIds.get(i);
                out.startTag(null, "g");
                out.attribute(null, "id", Integer.toHexString(id.appWidgetId));
                out.attribute(null, "h", Integer.toHexString(id.host.tag));
                if (id.provider != null) {
                    out.attribute(null, "p", Integer.toHexString(id.provider.tag));
                }
                out.endTag(null, "g");
            }
            out.endTag(null, "gs");
            out.endDocument();
            stream.close();
            return true;
        } catch (IOException e) {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
            }
            if (file.exists()) {
                file.delete();
            }
            return false;
        }
    }
    void readStateFromFileLocked(File file) {
        FileInputStream stream = null;
        boolean success = false;
        try {
            stream = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            int type;
            int providerIndex = 0;
            HashMap<Integer,Provider> loadedProviders = new HashMap<Integer, Provider>();
            do {
                type = parser.next();
                if (type == XmlPullParser.START_TAG) {
                    String tag = parser.getName();
                    if ("p".equals(tag)) {
                        String pkg = parser.getAttributeValue(null, "pkg");
                        String cl = parser.getAttributeValue(null, "cl");
                        final PackageManager packageManager = mContext.getPackageManager();
                        try {
                            packageManager.getReceiverInfo(new ComponentName(pkg, cl), 0);
                        } catch (PackageManager.NameNotFoundException e) {
                            String[] pkgs = packageManager.currentToCanonicalPackageNames(
                                    new String[] { pkg });
                            pkg = pkgs[0];
                        }
                        Provider p = lookupProviderLocked(new ComponentName(pkg, cl));
                        if (p == null && mSafeMode) {
                            p = new Provider();
                            p.info = new AppWidgetProviderInfo();
                            p.info.provider = new ComponentName(pkg, cl);
                            p.zombie = true;
                            mInstalledProviders.add(p);
                        }
                        if (p != null) {
                            loadedProviders.put(providerIndex, p);
                        }
                        providerIndex++;
                    }
                    else if ("h".equals(tag)) {
                        Host host = new Host();
                        host.packageName = parser.getAttributeValue(null, "pkg");
                        try {
                            host.uid = getUidForPackage(host.packageName);
                        } catch (PackageManager.NameNotFoundException ex) {
                            host.zombie = true;
                        }
                        if (!host.zombie || mSafeMode) {
                            host.hostId = Integer.parseInt(
                                    parser.getAttributeValue(null, "id"), 16);
                            mHosts.add(host);
                        }
                    }
                    else if ("g".equals(tag)) {
                        AppWidgetId id = new AppWidgetId();
                        id.appWidgetId = Integer.parseInt(parser.getAttributeValue(null, "id"), 16);
                        if (id.appWidgetId >= mNextAppWidgetId) {
                            mNextAppWidgetId = id.appWidgetId + 1;
                        }
                        String providerString = parser.getAttributeValue(null, "p");
                        if (providerString != null) {
                            int pIndex = Integer.parseInt(providerString, 16);
                            id.provider = loadedProviders.get(pIndex);
                            if (false) {
                                Slog.d(TAG, "bound appWidgetId=" + id.appWidgetId + " to provider "
                                        + pIndex + " which is " + id.provider);
                            }
                            if (id.provider == null) {
                                continue;
                            }
                        }
                        int hIndex = Integer.parseInt(parser.getAttributeValue(null, "h"), 16);
                        id.host = mHosts.get(hIndex);
                        if (id.host == null) {
                            continue;
                        }
                        if (id.provider != null) {
                            id.provider.instances.add(id);
                        }
                        id.host.instances.add(id);
                        mAppWidgetIds.add(id);
                    }
                }
            } while (type != XmlPullParser.END_DOCUMENT);
            success = true;
        } catch (NullPointerException e) {
            Slog.w(TAG, "failed parsing " + file, e);
        } catch (NumberFormatException e) {
            Slog.w(TAG, "failed parsing " + file, e);
        } catch (XmlPullParserException e) {
            Slog.w(TAG, "failed parsing " + file, e);
        } catch (IOException e) {
            Slog.w(TAG, "failed parsing " + file, e);
        } catch (IndexOutOfBoundsException e) {
            Slog.w(TAG, "failed parsing " + file, e);
        }
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
        }
        if (success) {
            for (int i=mHosts.size()-1; i>=0; i--) {
                pruneHostLocked(mHosts.get(i));
            }
        } else {
            mAppWidgetIds.clear();
            mHosts.clear();
            final int N = mInstalledProviders.size();
            for (int i=0; i<N; i++) {
                mInstalledProviders.get(i).instances.clear();
            }
        }
    }
    File savedStateTempFile() {
        return new File("/data/system/" + SETTINGS_TMP_FILENAME);
    }
    File savedStateRealFile() {
        return new File("/data/system/" + SETTINGS_FILENAME);
    }
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                sendInitialBroadcasts();
            } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                Locale revised = Locale.getDefault();
                if (revised == null || mLocale == null ||
                    !(revised.equals(mLocale))) {
                    mLocale = revised;
                    synchronized (mAppWidgetIds) {
                        int N = mInstalledProviders.size();
                        for (int i=N-1; i>=0; i--) {
                            Provider p = mInstalledProviders.get(i);
                            String pkgName = p.info.provider.getPackageName();
                            updateProvidersForPackageLocked(pkgName);
                        }
                        saveStateLocked();
                    }
                }
            } else {
                boolean added = false;
                String pkgList[] = null;
                if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
                    pkgList = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
                    added = true;
                } if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
                    pkgList = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
                    added = false;
                } else  {
                    Uri uri = intent.getData();
                    if (uri == null) {
                        return;
                    }
                    String pkgName = uri.getSchemeSpecificPart();
                    if (pkgName == null) {
                        return;
                    }
                    pkgList = new String[] { pkgName };
                    added = Intent.ACTION_PACKAGE_ADDED.equals(action);
                }
                if (pkgList == null || pkgList.length == 0) {
                    return;
                }
                if (added) {
                    synchronized (mAppWidgetIds) {
                        Bundle extras = intent.getExtras();
                        if (extras != null && extras.getBoolean(Intent.EXTRA_REPLACING, false)) {
                            for (String pkgName : pkgList) {
                                updateProvidersForPackageLocked(pkgName);
                            }
                        } else {
                            for (String pkgName : pkgList) {
                                addProvidersForPackageLocked(pkgName);
                            }
                        }
                        saveStateLocked();
                    }
                } else {
                    Bundle extras = intent.getExtras();
                    if (extras != null && extras.getBoolean(Intent.EXTRA_REPLACING, false)) {
                    } else {
                        synchronized (mAppWidgetIds) {
                            for (String pkgName : pkgList) {
                                removeProvidersForPackageLocked(pkgName);
                                saveStateLocked();
                            }
                        }
                    }
                }
            }
        }
    };
    void addProvidersForPackageLocked(String pkgName) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setPackage(pkgName);
        List<ResolveInfo> broadcastReceivers = mPackageManager.queryBroadcastReceivers(intent,
                PackageManager.GET_META_DATA);
        final int N = broadcastReceivers == null ? 0 : broadcastReceivers.size();
        for (int i=0; i<N; i++) {
            ResolveInfo ri = broadcastReceivers.get(i);
            ActivityInfo ai = ri.activityInfo;
            if (pkgName.equals(ai.packageName)) {
                addProviderLocked(ri);
            }
        }
    }
    void updateProvidersForPackageLocked(String pkgName) {
        HashSet<String> keep = new HashSet<String>();
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setPackage(pkgName);
        List<ResolveInfo> broadcastReceivers = mPackageManager.queryBroadcastReceivers(intent,
                PackageManager.GET_META_DATA);
        int N = broadcastReceivers == null ? 0 : broadcastReceivers.size();
        for (int i=0; i<N; i++) {
            ResolveInfo ri = broadcastReceivers.get(i);
            ActivityInfo ai = ri.activityInfo;
            if (pkgName.equals(ai.packageName)) {
                ComponentName component = new ComponentName(ai.packageName, ai.name);
                Provider p = lookupProviderLocked(component);
                if (p == null) {
                    if (addProviderLocked(ri)) {
                        keep.add(ai.name);
                    }
                } else {
                    Provider parsed = parseProviderInfoXml(component, ri);
                    if (parsed != null) {
                        keep.add(ai.name);
                        p.info = parsed.info;
                        final int M = p.instances.size();
                        if (M > 0) {
                            int[] appWidgetIds = getAppWidgetIds(p);
                            cancelBroadcasts(p);
                            registerForBroadcastsLocked(p, appWidgetIds);
                            for (int j=0; j<M; j++) {
                                AppWidgetId id = p.instances.get(j);
                                if (id.host != null && id.host.callbacks != null) {
                                    try {
                                        id.host.callbacks.providerChanged(id.appWidgetId, p.info);
                                    } catch (RemoteException ex) {
                                        id.host.callbacks = null;
                                    }
                                }
                            }
                            sendUpdateIntentLocked(p, appWidgetIds);
                        }
                    }
                }
            }
        }
        N = mInstalledProviders.size();
        for (int i=N-1; i>=0; i--) {
            Provider p = mInstalledProviders.get(i);
            if (pkgName.equals(p.info.provider.getPackageName())
                    && !keep.contains(p.info.provider.getClassName())) {
                removeProviderLocked(i, p);
            }
        }
    }
    void removeProvidersForPackageLocked(String pkgName) {
        int N = mInstalledProviders.size();
        for (int i=N-1; i>=0; i--) {
            Provider p = mInstalledProviders.get(i);
            if (pkgName.equals(p.info.provider.getPackageName())) {
                removeProviderLocked(i, p);
            }
        }
        N = mHosts.size();
        for (int i=N-1; i>=0; i--) {
            Host host = mHosts.get(i);
            if (pkgName.equals(host.packageName)) {
                deleteHostLocked(host);
            }
        }
    }
}
