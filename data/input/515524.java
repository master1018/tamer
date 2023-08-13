public class OneTimeInitializer extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            initialize(context);
        }
    }
    private void initialize(Context context) {
        if (Config.LOGD) {
            Log.d(Email.LOG_TAG, "OneTimeInitializer: initializing...");
        }
        final Preferences pref = Preferences.getPreferences(context);
        int progress = pref.getOneTimeInitializationProgress();
        if (progress < 1) {
            progress = 1;
            if (VendorPolicyLoader.getInstance(context).useAlternateExchangeStrings()) {
                setComponentEnabled(context, EasAuthenticatorServiceAlternate.class, true);
                setComponentEnabled(context, EasAuthenticatorService.class, false);
            }
            ExchangeUtils.enableEasCalendarSync(context);
        }
        pref.setOneTimeInitializationProgress(progress);
        setComponentEnabled(context, getClass(), false);
    }
    private void setComponentEnabled(Context context, Class<?> clazz, boolean enabled) {
        final ComponentName c = new ComponentName(context, clazz.getName());
        context.getPackageManager().setComponentEnabledSetting(c,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
