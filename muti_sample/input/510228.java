public class VoiceInputOutputSettings extends PreferenceActivity
        implements OnPreferenceChangeListener {
    private static final String TAG = "VoiceInputOutputSettings";
    private static final String KEY_PARENT = "parent";
    private static final String KEY_VOICE_INPUT_CATEGORY = "voice_input_category";
    private static final String KEY_RECOGNIZER = "recognizer";
    private static final String KEY_RECOGNIZER_SETTINGS = "recognizer_settings";
    private PreferenceGroup mParent;
    private PreferenceCategory mVoiceInputCategory;
    private ListPreference mRecognizerPref;
    private PreferenceScreen mSettingsPref;
    private HashMap<String, ResolveInfo> mAvailableRecognizersMap;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.voice_input_output_settings);
        mParent = (PreferenceGroup) findPreference(KEY_PARENT);
        mVoiceInputCategory = (PreferenceCategory) mParent.findPreference(KEY_VOICE_INPUT_CATEGORY);
        mRecognizerPref = (ListPreference) mParent.findPreference(KEY_RECOGNIZER);
        mRecognizerPref.setOnPreferenceChangeListener(this);
        mSettingsPref = (PreferenceScreen) mParent.findPreference(KEY_RECOGNIZER_SETTINGS);
        mAvailableRecognizersMap = new HashMap<String, ResolveInfo>();
        populateOrRemoveRecognizerPreference();
    }
    private void populateOrRemoveRecognizerPreference() {
        List<ResolveInfo> availableRecognitionServices = getPackageManager().queryIntentServices(
                new Intent(RecognitionService.SERVICE_INTERFACE), PackageManager.GET_META_DATA);
        int numAvailable = availableRecognitionServices.size();
        if (numAvailable == 0) {
            removePreference(mVoiceInputCategory);
            removePreference(mRecognizerPref);
            removePreference(mSettingsPref);
        } else if (numAvailable == 1) {
            removePreference(mRecognizerPref);
            ResolveInfo resolveInfo = availableRecognitionServices.get(0);
            String recognizerComponent =
                    new ComponentName(resolveInfo.serviceInfo.packageName,
                            resolveInfo.serviceInfo.name).flattenToShortString();
            mAvailableRecognizersMap.put(recognizerComponent, resolveInfo);
            String currentSetting = Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.VOICE_RECOGNITION_SERVICE);
            updateSettingsLink(currentSetting);
        } else {
            populateRecognizerPreference(availableRecognitionServices);
        }
    }
    private void removePreference(Preference pref) {
        if (pref != null) {
            mParent.removePreference(pref);
        }
    }
    private void populateRecognizerPreference(List<ResolveInfo> recognizers) {
        int size = recognizers.size();
        CharSequence[] entries = new CharSequence[size];
        CharSequence[] values = new CharSequence[size];
        String currentSetting = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.VOICE_RECOGNITION_SERVICE);
        for (int i = 0; i < size; i++) {
            ResolveInfo resolveInfo = recognizers.get(i);
            String recognizerComponent =
                    new ComponentName(resolveInfo.serviceInfo.packageName,
                            resolveInfo.serviceInfo.name).flattenToShortString();
            mAvailableRecognizersMap.put(recognizerComponent, resolveInfo);
            entries[i] = resolveInfo.loadLabel(getPackageManager());
            values[i] = recognizerComponent;
        }
        mRecognizerPref.setEntries(entries);
        mRecognizerPref.setEntryValues(values);
        mRecognizerPref.setDefaultValue(currentSetting);
        mRecognizerPref.setValue(currentSetting);
        updateSettingsLink(currentSetting);
    }
    private void updateSettingsLink(String currentSetting) {
        ResolveInfo currentRecognizer = mAvailableRecognizersMap.get(currentSetting);
        ServiceInfo si = currentRecognizer.serviceInfo;
        XmlResourceParser parser = null;
        String settingsActivity = null;
        try {
            parser = si.loadXmlMetaData(getPackageManager(), RecognitionService.SERVICE_META_DATA);
            if (parser == null) {
                throw new XmlPullParserException("No " + RecognitionService.SERVICE_META_DATA +
                        " meta-data for " + si.packageName);
            }
            Resources res = getPackageManager().getResourcesForApplication(
                    si.applicationInfo);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            int type;
            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                    && type != XmlPullParser.START_TAG) {
            }
            String nodeName = parser.getName();
            if (!"recognition-service".equals(nodeName)) {
                throw new XmlPullParserException(
                        "Meta-data does not start with recognition-service tag");
            }
            TypedArray array = res.obtainAttributes(attrs,
                    com.android.internal.R.styleable.RecognitionService);
            settingsActivity = array.getString(
                    com.android.internal.R.styleable.RecognitionService_settingsActivity);
            array.recycle();
        } catch (XmlPullParserException e) {
            Log.e(TAG, "error parsing recognition service meta-data", e);
        } catch (IOException e) {
            Log.e(TAG, "error parsing recognition service meta-data", e);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "error parsing recognition service meta-data", e);
        } finally {
            if (parser != null) parser.close();
        }
        if (settingsActivity == null) {
            Log.w(TAG, "no recognizer settings available for " + si.packageName);
            mSettingsPref.setIntent(null);
            mParent.removePreference(mSettingsPref);
        } else {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(new ComponentName(si.packageName, settingsActivity));
            mSettingsPref.setIntent(i);
            mRecognizerPref.setSummary(currentRecognizer.loadLabel(getPackageManager()));
        }
    }
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRecognizerPref) {
            String setting = (String) newValue;
            Settings.Secure.putString(
                    getContentResolver(),
                    Settings.Secure.VOICE_RECOGNITION_SERVICE,
                    setting);
            updateSettingsLink(setting);
        }
        return true;
    }
}
