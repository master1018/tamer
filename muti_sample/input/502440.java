public class CustomLocaleActivity extends ListActivity {
    private static final String CUSTOM_LOCALES_SEP = " ";
    private static final String CUSTOM_LOCALES = "custom_locales";
    private static final String KEY_CUSTOM = "custom";
    private static final String KEY_NAME = "name";
    private static final String KEY_CODE = "code";
    private static final String TAG = "LocaleSetup";
    private static final boolean DEBUG = true;
    private static final int UPDATE_LIST = 42;
    private static final int MENU_APPLY = 43;
    private static final int MENU_REMOVE = 44;
    private ListView mListView;
    private TextView mCurrentLocaleTextView;
    private SharedPreferences mPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mPrefs = getPreferences(MODE_PRIVATE);
        Button newLocaleButton = (Button) findViewById(R.id.new_locale);
        newLocaleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(CustomLocaleActivity.this, NewLocaleDialog.class);
                startActivityForResult(i, UPDATE_LIST);
            }
        });
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setFocusable(true);
        mListView.setFocusableInTouchMode(true);
        mListView.requestFocus();
        registerForContextMenu(mListView);
        setupLocaleList();
        mCurrentLocaleTextView = (TextView) findViewById(R.id.current_locale);
        displayCurrentLocale();
    }
    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_LIST && resultCode == RESULT_OK && data != null) {
            String locale = data.getExtras().getString(NewLocaleDialog.INTENT_EXTRA_LOCALE);
            if (locale != null && locale.length() > 0) {
                String customLocales = mPrefs.getString(CUSTOM_LOCALES, null);
                if (customLocales == null) {
                    customLocales = locale;
                } else {
                    customLocales += CUSTOM_LOCALES_SEP + locale;
                }
                if (DEBUG) {
                    Log.d(TAG, "add/customLocales: " + customLocales);
                }
                mPrefs.edit().putString(CUSTOM_LOCALES, customLocales).commit();
                Toast.makeText(this, "Added custom locale: " + locale, Toast.LENGTH_SHORT).show();
                setupLocaleList();
                ListAdapter a = mListView.getAdapter();
                for (int i = 0; i < a.getCount(); i++) {
                    Object o = a.getItem(i);
                    if (o instanceof Map<?, ?>) {
                        String code = ((Map<String, String>) o).get(KEY_CODE);
                        if (code != null && code.equals(locale)) {
                            mListView.setSelection(i);
                            break;
                        }
                    }
                }
                if (data.getExtras().getBoolean(NewLocaleDialog.INTENT_EXTRA_SELECT)) {
                    selectLocale(locale);
                }
            }
        }
    }
    private void setupLocaleList() {
        if (DEBUG) {
            Log.d(TAG, "Update locate list");
        }
        ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
        String[] locales = getAssets().getLocales();
        for (String locale : locales) {
            Locale loc = new Locale(locale);
            Map<String, String> map = new HashMap<String, String>(1);
            map.put(KEY_CODE, locale);
            map.put(KEY_NAME, loc.getDisplayName());
            data.add(map);
        }
        locales = null;
        String customLocales = mPrefs.getString(CUSTOM_LOCALES, "");
        if (DEBUG) {
            Log.d(TAG, "customLocales: " + customLocales);
        }
        for (String locale : customLocales.split(CUSTOM_LOCALES_SEP)) {
            if (locale != null && locale.length() > 0) {
                Locale loc = new Locale(locale);
                Map<String, String> map = new HashMap<String, String>(1);
                map.put(KEY_CODE, locale);
                map.put(KEY_NAME, loc.getDisplayName() + " [Custom]");
                map.put(KEY_CUSTOM, "");
                data.add(map);
            }
        }
        Collections.sort(data, new Comparator<Map<String, String>>() {
            public int compare(Map<String, String> lhs, Map<String, String> rhs) {
                return lhs.get(KEY_CODE).compareTo(rhs.get(KEY_CODE));
            }
        });
        mListView.setAdapter(new SimpleAdapter(this, data, R.layout.list_item, new String[] {
                KEY_CODE, KEY_NAME}, new int[] {R.id.locale_code, R.id.locale_name}));
    }
    @SuppressWarnings("unchecked")
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (menuInfo instanceof AdapterContextMenuInfo) {
            int position = ((AdapterContextMenuInfo) menuInfo).position;
            Object o = mListView.getItemAtPosition(position);
            if (o instanceof Map<?, ?>) {
                String locale = ((Map<String, String>) o).get(KEY_CODE);
                String custom = ((Map<String, String>) o).get(KEY_CUSTOM);
                if (custom == null) {
                    menu.setHeaderTitle("System Locale");
                    menu.add(0, MENU_APPLY, 0, "Apply");
                } else {
                    menu.setHeaderTitle("Custom Locale");
                    menu.add(0, MENU_APPLY, 0, "Apply");
                    menu.add(0, MENU_REMOVE, 0, "Remove");
                }
            }
        }
    }
    @SuppressWarnings("unchecked")
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String pendingLocale = null;
        boolean is_custom = false;
        ContextMenuInfo menuInfo = item.getMenuInfo();
        if (menuInfo instanceof AdapterContextMenuInfo) {
            int position = ((AdapterContextMenuInfo) menuInfo).position;
            Object o = mListView.getItemAtPosition(position);
            if (o instanceof Map<?, ?>) {
                pendingLocale = ((Map<String, String>) o).get(KEY_CODE);
                is_custom = ((Map<String, String>) o).get(KEY_CUSTOM) != null;
            }
        }
        if (pendingLocale == null) {
            return super.onContextItemSelected(item);
        }
        if (item.getItemId() == MENU_REMOVE) {
            String customLocales = mPrefs.getString(CUSTOM_LOCALES, "");
            if (DEBUG) {
                Log.d(TAG, "Remove " + pendingLocale + " from custom locales: " + customLocales);
            }
            StringBuilder sb = new StringBuilder();
            for (String locale : customLocales.split(CUSTOM_LOCALES_SEP)) {
                if (locale != null && locale.length() > 0 && !locale.equals(pendingLocale)) {
                    if (sb.length() > 0) {
                        sb.append(CUSTOM_LOCALES_SEP);
                    }
                    sb.append(locale);
                }
            }
            String newLocales = sb.toString();
            if (!newLocales.equals(customLocales)) {
                mPrefs.edit().putString(CUSTOM_LOCALES, customLocales).commit();
                Toast.makeText(this, "Removed custom locale: " + pendingLocale, Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (item.getItemId() == MENU_APPLY) {
            selectLocale(pendingLocale);
        }
        return super.onContextItemSelected(item);
    }
    private void selectLocale(String locale) {
        if (DEBUG) {
            Log.d(TAG, "Select locale " + locale);
        }
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();
            Locale loc = null;
            String[] langCountry = locale.split("_");
            if (langCountry.length == 2) {
                loc = new Locale(langCountry[0], langCountry[1]);
            } else {
                loc = new Locale(locale);
            }
            config.locale = loc;
            config.userSetLocale = true;
            am.updateConfiguration(config);
            Toast.makeText(this, "Select locale: " + locale, Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.e(TAG, "Select locale failed", e);
            }
        }
    }
    private void displayCurrentLocale() {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();
            if (config.locale != null) {
                String text = String.format("%s - %s",
                        config.locale.toString(),
                        config.locale.getDisplayName());
                mCurrentLocaleTextView.setText(text);
            }
        } catch (RemoteException e) {
            if (DEBUG) {
                Log.e(TAG, "get current locale failed", e);
            }
        }
    }
}
