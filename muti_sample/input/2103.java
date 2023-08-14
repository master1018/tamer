class WindowsPreferencesFactory implements PreferencesFactory  {
    public Preferences userRoot() {
        return WindowsPreferences.userRoot;
    }
    public Preferences systemRoot() {
        return WindowsPreferences.systemRoot;
    }
}
