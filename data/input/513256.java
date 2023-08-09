public class AccessibilityManagerService extends IAccessibilityManager.Stub
        implements HandlerCaller.Callback {
    private static final String LOG_TAG = "AccessibilityManagerService";
    private static int sIdCounter = 0;
    private static final int OWN_PROCESS_ID = android.os.Process.myPid();
    private static final int DO_SET_SERVICE_INFO = 10;
    final HandlerCaller mCaller;
    final Context mContext;
    final Object mLock = new Object();
    final List<Service> mServices = new ArrayList<Service>();
    final List<IAccessibilityManagerClient> mClients =
        new ArrayList<IAccessibilityManagerClient>();
    final Map<ComponentName, Service> mComponentNameToServiceMap =
        new HashMap<ComponentName, Service>();
    private final List<ServiceInfo> mInstalledServices = new ArrayList<ServiceInfo>();
    private final Set<ComponentName> mEnabledServices = new HashSet<ComponentName>();
    private final SimpleStringSplitter mStringColonSplitter = new SimpleStringSplitter(':');
    private PackageManager mPackageManager;
    private int mHandledFeedbackTypes = 0;
    private boolean mIsEnabled;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            Service service = (Service) message.obj;
            int eventType = message.arg1;
            synchronized (mLock) {
                notifyEventListenerLocked(service, eventType);
                AccessibilityEvent oldEvent = service.mPendingEvents.get(eventType);
                service.mPendingEvents.remove(eventType);
                tryRecycleLocked(oldEvent);
            }
        }
    };
    AccessibilityManagerService(Context context) {
        mContext = context;
        mPackageManager = mContext.getPackageManager();
        mCaller = new HandlerCaller(context, this);
        registerPackageChangeAndBootCompletedBroadcastReceiver();
        registerSettingsContentObservers();
    }
    private void registerPackageChangeAndBootCompletedBroadcastReceiver() {
        Context context = mContext;
        PackageMonitor monitor = new PackageMonitor() {
            @Override
            public void onSomePackagesChanged() {
                synchronized (mLock) {
                    populateAccessibilityServiceListLocked();
                    manageServicesLocked();
                }
            }
            @Override
            public boolean onHandleForceStop(Intent intent, String[] packages,
                    int uid, boolean doit) {
                synchronized (mLock) {
                    boolean changed = false;
                    Iterator<ComponentName> it = mEnabledServices.iterator();
                    while (it.hasNext()) {
                        ComponentName comp = it.next();
                        String compPkg = comp.getPackageName();
                        for (String pkg : packages) {
                            if (compPkg.equals(pkg)) {
                                if (!doit) {
                                    return true;
                                }
                                it.remove();
                                changed = true;
                            }
                        }
                    }
                    if (changed) {
                        it = mEnabledServices.iterator();
                        StringBuilder str = new StringBuilder();
                        while (it.hasNext()) {
                            if (str.length() > 0) {
                                str.append(':');
                            }
                            str.append(it.next().flattenToShortString());
                        }
                        Settings.Secure.putString(mContext.getContentResolver(),
                                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                                str.toString());
                        manageServicesLocked();
                    }
                    return false;
                }
            }
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED) {
                    synchronized (mLock) {
                        populateAccessibilityServiceListLocked();
                        mIsEnabled = Settings.Secure.getInt(mContext.getContentResolver(),
                                Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1;
                        if (mIsEnabled) {
                            updateClientsLocked();
                        }
                        manageServicesLocked();
                    }
                    return;
                }
                super.onReceive(context, intent);
            }
        };
        monitor.register(context, true);
        IntentFilter bootFiler = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        mContext.registerReceiver(monitor, bootFiler);
    }
    private void registerSettingsContentObservers() {
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri enabledUri = Settings.Secure.getUriFor(Settings.Secure.ACCESSIBILITY_ENABLED);
        contentResolver.registerContentObserver(enabledUri, false,
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    mIsEnabled = Settings.Secure.getInt(mContext.getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1;
                    synchronized (mLock) {
                        if (mIsEnabled) {
                            manageServicesLocked();
                        } else {
                            unbindAllServicesLocked();
                        }
                        updateClientsLocked();
                    }
                }
            });
        Uri providersUri =
            Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        contentResolver.registerContentObserver(providersUri, false,
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    synchronized (mLock) {
                        manageServicesLocked();
                    }
                }
            });
    }
    public void addClient(IAccessibilityManagerClient client) {
        synchronized (mLock) {
            try {
                client.setEnabled(mIsEnabled);
                mClients.add(client);
            } catch (RemoteException re) {
                Slog.w(LOG_TAG, "Dead AccessibilityManagerClient: " + client, re);
            }
        }
    }
    public boolean sendAccessibilityEvent(AccessibilityEvent event) {
        synchronized (mLock) {
            notifyAccessibilityServicesDelayedLocked(event, false);
            notifyAccessibilityServicesDelayedLocked(event, true);
        }
        if (mHandledFeedbackTypes == 0) {
            event.recycle();
        } else {
            mHandledFeedbackTypes = 0;
        }
        return (OWN_PROCESS_ID != Binder.getCallingPid());
    }
    public List<ServiceInfo> getAccessibilityServiceList() {
        synchronized (mLock) {
            return mInstalledServices;
        }
    }
    public void interrupt() {
        synchronized (mLock) {
            for (int i = 0, count = mServices.size(); i < count; i++) {
                Service service = mServices.get(i);
                try {
                    service.mServiceInterface.onInterrupt();
                } catch (RemoteException re) {
                    if (re instanceof DeadObjectException) {
                        Slog.w(LOG_TAG, "Dead " + service.mService + ". Cleaning up.");
                        if (removeDeadServiceLocked(service)) {
                            count--;
                            i--;
                        }
                    } else {
                        Slog.e(LOG_TAG, "Error during sending interrupt request to "
                                + service.mService, re);
                    }
                }
            }
        }
    }
    public void executeMessage(Message message) {
        switch (message.what) {
            case DO_SET_SERVICE_INFO:
                SomeArgs arguments = ((SomeArgs) message.obj);
                AccessibilityServiceInfo info = (AccessibilityServiceInfo) arguments.arg1;
                Service service = (Service) arguments.arg2;
                synchronized (mLock) {
                    service.mEventTypes = info.eventTypes;
                    service.mFeedbackType = info.feedbackType;
                    String[] packageNames = info.packageNames;
                    if (packageNames != null) {
                        service.mPackageNames.addAll(Arrays.asList(packageNames));
                    }
                    service.mNotificationTimeout = info.notificationTimeout;
                    service.mIsDefault = (info.flags & AccessibilityServiceInfo.DEFAULT) != 0;
                }
                return;
            default:
                Slog.w(LOG_TAG, "Unknown message type: " + message.what);
        }
    }
    private void populateAccessibilityServiceListLocked() {
        mInstalledServices.clear();
        List<ResolveInfo> installedServices = mPackageManager.queryIntentServices(
                new Intent(AccessibilityService.SERVICE_INTERFACE), PackageManager.GET_SERVICES);
        for (int i = 0, count = installedServices.size(); i < count; i++) {
            mInstalledServices.add(installedServices.get(i).serviceInfo);
        }
    }
    private void notifyAccessibilityServicesDelayedLocked(AccessibilityEvent event,
            boolean isDefault) {
        try {
            for (int i = 0, count = mServices.size(); i < count; i++) {
                Service service = mServices.get(i);
                if (service.mIsDefault == isDefault) {
                    if (canDispathEventLocked(service, event, mHandledFeedbackTypes)) {
                        mHandledFeedbackTypes |= service.mFeedbackType;
                        notifyAccessibilityServiceDelayedLocked(service, event);
                    }
                }
            }
        } catch (IndexOutOfBoundsException oobe) {
            return;
        }
    }
    private void notifyAccessibilityServiceDelayedLocked(Service service,
            AccessibilityEvent event) {
        synchronized (mLock) {
            int eventType = event.getEventType();
            AccessibilityEvent oldEvent = service.mPendingEvents.get(eventType);
            service.mPendingEvents.put(eventType, event);
            int what = eventType | (service.mId << 16);
            if (oldEvent != null) {
                mHandler.removeMessages(what);
                tryRecycleLocked(oldEvent);
            }
            Message message = mHandler.obtainMessage(what, service);
            message.arg1 = event.getEventType();
            mHandler.sendMessageDelayed(message, service.mNotificationTimeout);
        }
    }
    private void tryRecycleLocked(AccessibilityEvent event) {
        if (event == null) {
            return;
        }
        int eventType = event.getEventType();
        List<Service> services = mServices;
        for (int i = 0, count = services.size(); i < count; i++) {
            Service service = services.get(i);
            if (service.mPendingEvents.get(eventType) == event) {
                return;
            }
        }
        event.recycle();
    }
    private void notifyEventListenerLocked(Service service, int eventType) {
        IEventListener listener = service.mServiceInterface;
        AccessibilityEvent event = service.mPendingEvents.get(eventType);
        try {
            listener.onAccessibilityEvent(event);
            if (Config.DEBUG) {
                Slog.i(LOG_TAG, "Event " + event + " sent to " + listener);
            }
        } catch (RemoteException re) {
            if (re instanceof DeadObjectException) {
                Slog.w(LOG_TAG, "Dead " + service.mService + ". Cleaning up.");
                synchronized (mLock) {
                    removeDeadServiceLocked(service);
                }
            } else {
                Slog.e(LOG_TAG, "Error during sending " + event + " to " + service.mService, re);
            }
        }
    }
    private boolean removeDeadServiceLocked(Service service) {
        mServices.remove(service);
        mHandler.removeMessages(service.mId);
        if (Config.DEBUG) {
            Slog.i(LOG_TAG, "Dead service " + service.mService + " removed");
        }
        if (mServices.isEmpty()) {
            mIsEnabled = false;
            updateClientsLocked();
        }
        return true;
    }
    private boolean canDispathEventLocked(Service service, AccessibilityEvent event,
            int handledFeedbackTypes) {
        if (!service.isConfigured()) {
            return false;
        }
        if (!service.mService.isBinderAlive()) {
            removeDeadServiceLocked(service);
            return false;
        }
        int eventType = event.getEventType();
        if ((service.mEventTypes & eventType) != eventType) {
            return false;
        }
        Set<String> packageNames = service.mPackageNames;
        CharSequence packageName = event.getPackageName();
        if (packageNames.isEmpty() || packageNames.contains(packageName)) {
            int feedbackType = service.mFeedbackType;
            if ((handledFeedbackTypes & feedbackType) != feedbackType
                    || feedbackType == AccessibilityServiceInfo.FEEDBACK_GENERIC) {
                return true;
            }
        }
        return false;
    }
    private void manageServicesLocked() {
        populateEnabledServicesLocked(mEnabledServices);
        updateServicesStateLocked(mInstalledServices, mEnabledServices);
    }
    private void unbindAllServicesLocked() {
        List<Service> services = mServices;
        for (int i = 0, count = services.size(); i < count; i++) {
            Service service = services.get(i);
            service.unbind();
            mComponentNameToServiceMap.remove(service.mComponentName);
        }
        services.clear();
    }
    private void populateEnabledServicesLocked(Set<ComponentName> enabledServices) {
        enabledServices.clear();
        String servicesValue = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (servicesValue != null) {
            TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
            splitter.setString(servicesValue);
            while (splitter.hasNext()) {
                String str = splitter.next();
                if (str == null || str.length() <= 0) {
                    continue;
                }
                ComponentName enabledService = ComponentName.unflattenFromString(str);
                if (enabledService != null) {
                    enabledServices.add(enabledService);
                }
            }
        }
    }
    private void updateServicesStateLocked(List<ServiceInfo> installedServices,
            Set<ComponentName> enabledServices) {
        Map<ComponentName, Service> componentNameToServiceMap = mComponentNameToServiceMap;
        List<Service> services = mServices;
        boolean isEnabled = mIsEnabled;
        for (int i = 0, count = installedServices.size(); i < count; i++) {
            ServiceInfo intalledService = installedServices.get(i);
            ComponentName componentName = new ComponentName(intalledService.packageName,
                    intalledService.name);
            Service service = componentNameToServiceMap.get(componentName);
            if (isEnabled && enabledServices.contains(componentName)) {
                if (service == null) {
                    new Service(componentName).bind();
                }
            } else {
                if (service != null) {
                    service.unbind();
                    componentNameToServiceMap.remove(componentName);
                    services.remove(service);
                }
            }
        }
    }
    private void updateClientsLocked() {
        for (int i = 0, count = mClients.size(); i < count; i++) {
            try {
                mClients.get(i).setEnabled(mIsEnabled);
            } catch (RemoteException re) {
                mClients.remove(i);
                count--;
                i--;
            }
        }
    }
    class Service extends IAccessibilityServiceConnection.Stub implements ServiceConnection {
        int mId = 0;
        IBinder mService;
        IEventListener mServiceInterface;
        int mEventTypes;
        int mFeedbackType;
        Set<String> mPackageNames = new HashSet<String>();
        boolean mIsDefault;
        long mNotificationTimeout;
        boolean mIsActive;
        ComponentName mComponentName;
        Intent mIntent;
        final SparseArray<AccessibilityEvent> mPendingEvents =
            new SparseArray<AccessibilityEvent>();
        Service(ComponentName componentName) {
            mId = sIdCounter++;
            mComponentName = componentName;
            mIntent = new Intent().setComponent(mComponentName);
            mIntent.putExtra(Intent.EXTRA_CLIENT_LABEL,
                    com.android.internal.R.string.accessibility_binding_label);
            mIntent.putExtra(Intent.EXTRA_CLIENT_INTENT, PendingIntent.getActivity(
                    mContext, 0, new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 0));
        }
        public void bind() {
            if (mService == null) {
                mContext.bindService(mIntent, this, Context.BIND_AUTO_CREATE);
            }
        }
        public void unbind() {
            if (mService != null) {
                mContext.unbindService(this);
            }
        }
        public boolean isConfigured() {
            return (mEventTypes != 0 && mFeedbackType != 0);
        }
        public void setServiceInfo(AccessibilityServiceInfo info) {
            mCaller.obtainMessageOO(DO_SET_SERVICE_INFO, info, this).sendToTarget();
        }
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mService = service;
            mServiceInterface = IEventListener.Stub.asInterface(service);
            try {
                mServiceInterface.setConnection(this);
                synchronized (mLock) {
                    if (!mServices.contains(this)) {
                        mServices.add(this);
                        mComponentNameToServiceMap.put(componentName, this);
                    }
                }
            } catch (RemoteException re) {
                Slog.w(LOG_TAG, "Error while setting Controller for service: " + service, re);
            }
        }
        public void onServiceDisconnected(ComponentName componentName) {
            synchronized (mLock) {
                Service service = mComponentNameToServiceMap.remove(componentName);
                mServices.remove(service);
            }
        }
    }
}
