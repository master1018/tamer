public class SettingsController {
    private static final String SETTINGS_FILENAME = "androidtool.cfg"; 
    private final Properties mProperties = new Properties();
    private ISettingsPage mSettingsPage;
    private final UpdaterData mUpdaterData;
    public SettingsController(UpdaterData updaterData) {
        mUpdaterData = updaterData;
    }
    public boolean getForceHttp() {
        return Boolean.parseBoolean(mProperties.getProperty(ISettingsPage.KEY_FORCE_HTTP));
    }
    public boolean getAskBeforeAdbRestart() {
        String value = mProperties.getProperty(ISettingsPage.KEY_ASK_ADB_RESTART);
        if (value == null) {
            return true;
        }
        return Boolean.parseBoolean(value);
    }
    public boolean getShowUpdateOnly() {
        String value = mProperties.getProperty(ISettingsPage.KEY_SHOW_UPDATE_ONLY);
        if (value == null) {
            return true;
        }
        return Boolean.parseBoolean(value);
    }
    public void setShowUpdateOnly(boolean enabled) {
        setSetting(ISettingsPage.KEY_SHOW_UPDATE_ONLY, enabled);
    }
    public int getMonitorDensity() {
        String value = mProperties.getProperty(ISettingsPage.KEY_MONITOR_DENSITY, null);
        if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    public void setMonitorDensity(int density) {
        mProperties.setProperty(ISettingsPage.KEY_MONITOR_DENSITY, Integer.toString(density));
    }
    private void setSetting(String key, boolean value) {
        mProperties.setProperty(key, Boolean.toString(value));
    }
    public void setSettingsPage(ISettingsPage settingsPage) {
        mSettingsPage = settingsPage;
        mSettingsPage.loadSettings(mProperties);
        settingsPage.setOnSettingsChanged(new ISettingsPage.SettingsChangedCallback() {
            public void onSettingsChanged(ISettingsPage page) {
                SettingsController.this.onSettingsChanged();
            }
        });
    }
    public void loadSettings() {
        FileInputStream fis = null;
        String path = null;
        try {
            String folder = AndroidLocation.getFolder();
            File f = new File(folder, SETTINGS_FILENAME);
            path = f.getPath();
            if (f.exists()) {
                fis = new FileInputStream(f);
                mProperties.load(fis);
                setShowUpdateOnly(getShowUpdateOnly());
                setSetting(ISettingsPage.KEY_ASK_ADB_RESTART, getAskBeforeAdbRestart());
            }
        } catch (Exception e) {
            ISdkLog log = mUpdaterData.getSdkLog();
            if (log != null) {
                log.error(e, "Failed to load settings from .android folder. Path is '%1$s'.", path);
            }
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
    }
    public void saveSettings() {
        FileOutputStream fos = null;
        String path = null;
        try {
            String folder = AndroidLocation.getFolder();
            File f = new File(folder, SETTINGS_FILENAME);
            path = f.getPath();
            fos = new FileOutputStream(f);
            mProperties.store( fos, "## Settings for Android Tool");  
        } catch (Exception e) {
            ISdkLog log = mUpdaterData.getSdkLog();
            if (log != null) {
                log.error(e, "Failed to save settings at '%1$s'", path);
            }
            String reason = null;
            if (e instanceof FileNotFoundException) {
                reason = "File not found";
            } else if (e instanceof AndroidLocationException) {
                reason = ".android folder not found, please define ANDROID_SDK_HOME";
            } else if (e.getMessage() != null) {
                reason = String.format("%1$s: %2$s", e.getClass().getSimpleName(), e.getMessage());
            } else {
                reason = e.getClass().getName();
            }
            MessageDialog.openInformation(mUpdaterData.getWindowShell(),
                    "SDK Manager Settings",
                    String.format(
                        "The Android SDK and AVD Manager failed to save its settings (%1$s) at %2$s",
                        reason, path));
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }
    private void onSettingsChanged() {
        if (mSettingsPage == null) {
            return;
        }
        String oldHttpsSetting = mProperties.getProperty(ISettingsPage.KEY_FORCE_HTTP,
                Boolean.FALSE.toString());
        mSettingsPage.retrieveSettings(mProperties);
        applySettings();
        saveSettings();
        String newHttpsSetting = mProperties.getProperty(ISettingsPage.KEY_FORCE_HTTP,
                Boolean.FALSE.toString());
        if (!newHttpsSetting.equals(oldHttpsSetting)) {
            mUpdaterData.refreshSources(false );
        }
    }
    public void applySettings() {
        Properties props = System.getProperties();
        props.setProperty(ISettingsPage.KEY_HTTP_PROXY_HOST,
                mProperties.getProperty(ISettingsPage.KEY_HTTP_PROXY_HOST, "")); 
        props.setProperty(ISettingsPage.KEY_HTTP_PROXY_PORT,
                mProperties.getProperty(ISettingsPage.KEY_HTTP_PROXY_PORT, ""));   
    }
}
