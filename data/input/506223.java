public class RecognitionManagerService extends Binder {
    final static String TAG = "RecognitionManagerService";
    final Context mContext;
    final MyPackageMonitor mMonitor;
    class MyPackageMonitor extends PackageMonitor {
        public void onSomePackagesChanged() {
            ComponentName comp = getCurRecognizer();
            if (comp == null) {
                if (anyPackagesAppearing()) {
                    comp = findAvailRecognizer(null);
                    if (comp != null) {
                        setCurRecognizer(comp);
                    }
                }
                return;
            }
            int change = isPackageDisappearing(comp.getPackageName()); 
            if (change == PACKAGE_PERMANENT_CHANGE
                    || change == PACKAGE_TEMPORARY_CHANGE) {
                setCurRecognizer(findAvailRecognizer(null));
            } else if (isPackageModified(comp.getPackageName())) {
                setCurRecognizer(findAvailRecognizer(comp.getPackageName()));
            }
        }
    }
    RecognitionManagerService(Context context) {
        mContext = context;
        mMonitor = new MyPackageMonitor();
        mMonitor.register(context, true);
    }
    public void systemReady() {
        ComponentName comp = getCurRecognizer();
        if (comp != null) {
            try {
                mContext.getPackageManager().getServiceInfo(comp, 0);
            } catch (NameNotFoundException e) {
                setCurRecognizer(null);
            }
        } else {
            comp = findAvailRecognizer(null);
            if (comp != null) {
                setCurRecognizer(comp);
            }
        }
    }
    ComponentName findAvailRecognizer(String prefPackage) {
        List<ResolveInfo> available =
                mContext.getPackageManager().queryIntentServices(
                        new Intent(RecognitionService.SERVICE_INTERFACE), 0);
        int numAvailable = available.size();
        if (numAvailable == 0) {
            Slog.w(TAG, "no available voice recognition services found");
            return null;
        } else {
            if (prefPackage != null) {
                for (int i=0; i<numAvailable; i++) {
                    ServiceInfo serviceInfo = available.get(i).serviceInfo;
                    if (prefPackage.equals(serviceInfo.packageName)) {
                        return new ComponentName(serviceInfo.packageName, serviceInfo.name);
                    }
                }
            }
            if (numAvailable > 1) {
                Slog.w(TAG, "more than one voice recognition service found, picking first");
            }
            ServiceInfo serviceInfo = available.get(0).serviceInfo;
            return new ComponentName(serviceInfo.packageName, serviceInfo.name);
        }
    }
    ComponentName getCurRecognizer() {
        String curRecognizer = Settings.Secure.getString(
                mContext.getContentResolver(),
                Settings.Secure.VOICE_RECOGNITION_SERVICE);
        if (TextUtils.isEmpty(curRecognizer)) {
            return null;
        }
        return ComponentName.unflattenFromString(curRecognizer);
    }
    void setCurRecognizer(ComponentName comp) {
        Settings.Secure.putString(mContext.getContentResolver(),
                Settings.Secure.VOICE_RECOGNITION_SERVICE,
                comp != null ? comp.flattenToShortString() : "");
    }
}
