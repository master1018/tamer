class FilePreferencesFactoryImpl implements PreferencesFactory {
    private static final Preferences USER_ROOT = new FilePreferencesImpl(true);
    private static final Preferences SYSTEM_ROOT = new FilePreferencesImpl(false);
    public FilePreferencesFactoryImpl() {
        super();
    }
    public Preferences userRoot() {
        return USER_ROOT;
    }
    public Preferences systemRoot() {
        return SYSTEM_ROOT;
    }
}
