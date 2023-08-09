public class LanguageSettings extends PreferenceActivity {
    private static final String KEY_PHONE_LANGUAGE = "phone_language";
    private boolean mHaveHardKeyboard;
    private List<InputMethodInfo> mInputMethodProperties;
    private List<CheckBoxPreference> mCheckboxes;
    private Preference mLanguagePref;
    final TextUtils.SimpleStringSplitter mStringColonSplitter
            = new TextUtils.SimpleStringSplitter(':');
    private String mLastInputMethodId;
    private String mLastTickedInputMethodId;
    static public String getInputMethodIdFromKey(String key) {
        return key;
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.language_settings);
        if (getAssets().getLocales().length == 1) {
            getPreferenceScreen().
                removePreference(findPreference("language_category"));
        } else {
            mLanguagePref = findPreference(KEY_PHONE_LANGUAGE);
        }
        Configuration config = getResources().getConfiguration();
        if (config.keyboard != Configuration.KEYBOARD_QWERTY) {
            getPreferenceScreen().removePreference(
                    getPreferenceScreen().findPreference("hardkeyboard_category"));
        } else {
            mHaveHardKeyboard = true;
        }
        mCheckboxes = new ArrayList<CheckBoxPreference>();
        onCreateIMM();
    }
    private boolean isSystemIme(InputMethodInfo property) {
        return (property.getServiceInfo().applicationInfo.flags
                & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
    private void onCreateIMM() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodProperties = imm.getInputMethodList();
        mLastInputMethodId = Settings.Secure.getString(getContentResolver(),
            Settings.Secure.DEFAULT_INPUT_METHOD);
        PreferenceGroup textCategory = (PreferenceGroup) findPreference("text_category");
        int N = (mInputMethodProperties == null ? 0 : mInputMethodProperties
                .size());
        for (int i = 0; i < N; ++i) {
            InputMethodInfo property = mInputMethodProperties.get(i);
            String prefKey = property.getId();
            CharSequence label = property.loadLabel(getPackageManager());
            boolean systemIME = isSystemIme(property);
            if (mHaveHardKeyboard || (N > 1 && !systemIME)) {
                CheckBoxPreference chkbxPref = new CheckBoxPreference(this);
                chkbxPref.setKey(prefKey);
                chkbxPref.setTitle(label);
                textCategory.addPreference(chkbxPref);
                mCheckboxes.add(chkbxPref);
            }
            if (null != property.getSettingsActivity()) {
                PreferenceScreen prefScreen = new PreferenceScreen(this, null);
                String settingsActivity = property.getSettingsActivity();
                if (settingsActivity.lastIndexOf("/") < 0) {
                    settingsActivity = property.getPackageName() + "/" + settingsActivity;
                }
                prefScreen.setKey(settingsActivity);
                prefScreen.setTitle(label);
                if (N == 1) {
                    prefScreen.setSummary(getString(R.string.onscreen_keyboard_settings_summary));
                } else {
                    CharSequence settingsLabel = getResources().getString(
                            R.string.input_methods_settings_label_format, label);
                    prefScreen.setSummary(settingsLabel);
                }
                textCategory.addPreference(prefScreen);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        final HashSet<String> enabled = new HashSet<String>();
        String enabledStr = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_INPUT_METHODS);
        if (enabledStr != null) {
            final TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
            splitter.setString(enabledStr);
            while (splitter.hasNext()) {
                enabled.add(splitter.next());
            }
        }
        int N = mInputMethodProperties.size();
        for (int i = 0; i < N; ++i) {
            final String id = mInputMethodProperties.get(i).getId();
            CheckBoxPreference pref = (CheckBoxPreference) findPreference(mInputMethodProperties
                    .get(i).getId());
            if (pref != null) {
                pref.setChecked(enabled.contains(id));
            }
        }
        mLastTickedInputMethodId = null;
        if (mLanguagePref != null) {
            Configuration conf = getResources().getConfiguration();
            String locale = conf.locale.getDisplayName(conf.locale);
            if (locale != null && locale.length() > 1) {
                locale = Character.toUpperCase(locale.charAt(0)) + locale.substring(1);
                mLanguagePref.setSummary(locale);
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        StringBuilder builder = new StringBuilder(256);
        StringBuilder disabledSysImes = new StringBuilder(256);
        int firstEnabled = -1;
        int N = mInputMethodProperties.size();
        for (int i = 0; i < N; ++i) {
            final InputMethodInfo property = mInputMethodProperties.get(i);
            final String id = property.getId();
            CheckBoxPreference pref = (CheckBoxPreference) findPreference(id);
            boolean hasIt = id.equals(mLastInputMethodId);
            boolean systemIme = isSystemIme(property); 
            if (((N == 1 || systemIme) && !mHaveHardKeyboard) 
                    || (pref != null && pref.isChecked())) {
                if (builder.length() > 0) builder.append(':');
                builder.append(id);
                if (firstEnabled < 0) {
                    firstEnabled = i;
                }
            } else if (hasIt) {
                mLastInputMethodId = mLastTickedInputMethodId;
            }
            if (pref != null && !pref.isChecked() && systemIme && mHaveHardKeyboard) {
                if (disabledSysImes.length() > 0) disabledSysImes.append(":");
                disabledSysImes.append(id);
            }
        }
        if (null == mLastInputMethodId || "".equals(mLastInputMethodId)) {
            if (firstEnabled >= 0) {
                mLastInputMethodId = mInputMethodProperties.get(firstEnabled).getId();
            } else {
                mLastInputMethodId = null;
            }
        }
        Settings.Secure.putString(getContentResolver(),
            Settings.Secure.ENABLED_INPUT_METHODS, builder.toString());
        Settings.Secure.putString(getContentResolver(),
                Settings.Secure.DISABLED_SYSTEM_INPUT_METHODS, disabledSysImes.toString());
        Settings.Secure.putString(getContentResolver(),
            Settings.Secure.DEFAULT_INPUT_METHOD,
            mLastInputMethodId != null ? mLastInputMethodId : "");
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (Utils.isMonkeyRunning()) {
            return false;
        }
        if (preference instanceof CheckBoxPreference) {
            final CheckBoxPreference chkPref = (CheckBoxPreference) preference;
            final String id = getInputMethodIdFromKey(chkPref.getKey());
            if (chkPref.isChecked()) {
                InputMethodInfo selImi = null;
                final int N = mInputMethodProperties.size();
                for (int i=0; i<N; i++) {
                    InputMethodInfo imi = mInputMethodProperties.get(i);
                    if (id.equals(imi.getId())) {
                        selImi = imi;
                        if (isSystemIme(imi)) {
                            mLastTickedInputMethodId = id;
                            return super.onPreferenceTreeClick(preferenceScreen, preference);
                        }
                    }
                }
                chkPref.setChecked(false);
                if (selImi == null) {
                    return super.onPreferenceTreeClick(preferenceScreen, preference);
                }
                AlertDialog d = (new AlertDialog.Builder(this))
                        .setTitle(android.R.string.dialog_alert_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(getString(R.string.ime_security_warning,
                                selImi.getServiceInfo().applicationInfo.loadLabel(
                                        getPackageManager())))
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        chkPref.setChecked(true);
                                        mLastTickedInputMethodId = id;
                                    }
                        })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                        })
                        .create();
                d.show();
            } else if (id.equals(mLastTickedInputMethodId)) {
                mLastTickedInputMethodId = null;
            }
        } else if (preference instanceof PreferenceScreen) {
            if (preference.getIntent() == null) {
                PreferenceScreen pref = (PreferenceScreen) preference;
                String activityName = pref.getKey();
                String packageName = activityName.substring(0, activityName
                        .lastIndexOf("."));
                int slash = activityName.indexOf("/");
                if (slash > 0) {
                    packageName = activityName.substring(0, slash);
                    activityName = activityName.substring(slash + 1);
                }
                if (activityName.length() > 0) {
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.setClassName(packageName, activityName);
                    startActivity(i);
                }
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
