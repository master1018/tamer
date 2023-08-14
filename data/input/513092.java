class SystemAllowGeolocationOrigins {
    private final static String LAST_READ_ALLOW_GEOLOCATION_ORIGINS =
            "last_read_allow_geolocation_origins";
    private final Context mContext;
    private final SettingObserver mSettingObserver;
    public SystemAllowGeolocationOrigins(Context context) {
        mContext = context;
        mSettingObserver = new SettingObserver();
    }
    public void start() {
        Uri uri = Settings.Secure.getUriFor(Settings.Secure.ALLOWED_GEOLOCATION_ORIGINS);
        mContext.getContentResolver().registerContentObserver(uri, false, mSettingObserver);
        maybeApplySettingAsync();
    }
    public void stop() {
        mContext.getContentResolver().unregisterContentObserver(mSettingObserver);
    }
    void maybeApplySettingAsync() {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                maybeApplySetting();
                return null;
            }
        }.execute();
    }
    private void maybeApplySetting() {
        String newSetting = getSystemSetting();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String lastReadSetting =
                preferences.getString(LAST_READ_ALLOW_GEOLOCATION_ORIGINS, "");
        if (TextUtils.equals(lastReadSetting, newSetting)) {
            return;
        }
        preferences.edit()
                .putString(LAST_READ_ALLOW_GEOLOCATION_ORIGINS, newSetting)
                .commit();
        Set<String> oldOrigins = parseAllowGeolocationOrigins(lastReadSetting);
        Set<String> newOrigins = parseAllowGeolocationOrigins(newSetting);
        Set<String> addedOrigins = setMinus(newOrigins, oldOrigins);
        Set<String> removedOrigins = setMinus(oldOrigins, newOrigins);
        removeOrigins(removedOrigins);
        addOrigins(addedOrigins);
    }
    private static HashSet<String> parseAllowGeolocationOrigins(String setting) {
        HashSet<String> origins = new HashSet<String>();
        if (!TextUtils.isEmpty(setting)) {
            for (String origin : setting.split("\\s+")) {
                if (!TextUtils.isEmpty(origin)) {
                    origins.add(origin);
                }
            }
        }
        return origins;
    }
    private <A> Set<A> setMinus(Set<A> x, Set<A> y) {
        HashSet<A> z = new HashSet<A>(x.size());
        for (A a : x) {
            if (!y.contains(a)) {
                z.add(a);
            }
        }
        return z;
    }
    private String getSystemSetting() {
        String value = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ALLOWED_GEOLOCATION_ORIGINS);
        return value == null ? "" : value;
    }
    private void addOrigins(Set<String> origins) {
        for (String origin : origins) {
            GeolocationPermissions.getInstance().allow(origin);
        }
    }
    private void removeOrigins(Set<String> origins) {
        for (final String origin : origins) {
            GeolocationPermissions.getInstance().getAllowed(origin, new ValueCallback<Boolean>() {
                public void onReceiveValue(Boolean value) {
                    if (value != null && value.booleanValue()) {
                        GeolocationPermissions.getInstance().clear(origin);
                    }
                }
            });
        }
    }
    private class SettingObserver extends ContentObserver {
        SettingObserver() {
            super(new Handler());
        }
        @Override
        public void onChange(boolean selfChange) {
            maybeApplySettingAsync();
        }
    }
}
