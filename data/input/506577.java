public class BookmarkPicker extends ListActivity implements SimpleAdapter.ViewBinder {
    private static final String TAG = "BookmarkPicker";
    public static final String EXTRA_TITLE = "com.android.settings.quicklaunch.TITLE";
    public static final String EXTRA_SHORTCUT = "com.android.settings.quicklaunch.SHORTCUT";
    private static final int REQUEST_CREATE_SHORTCUT = 1;
    private static Intent sLaunchIntent;
    private static Intent sShortcutIntent;
    private List<ResolveInfo> mResolveList;
    private static final String KEY_TITLE = "TITLE";
    private static final String KEY_RESOLVE_INFO = "RESOLVE_INFO";
    private static final String sKeys[] = new String[] { KEY_TITLE, KEY_RESOLVE_INFO };
    private static final int sResourceIds[] = new int[] { R.id.title, R.id.icon };
    private SimpleAdapter mMyAdapter;
    private static final int DISPLAY_MODE_LAUNCH = 0;
    private static final int DISPLAY_MODE_SHORTCUT = 1;
    private int mDisplayMode = DISPLAY_MODE_LAUNCH;
    private Handler mUiHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateListAndAdapter();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, DISPLAY_MODE_LAUNCH, 0, R.string.quick_launch_display_mode_applications)
                .setIcon(com.android.internal.R.drawable.ic_menu_archive);
        menu.add(0, DISPLAY_MODE_SHORTCUT, 0, R.string.quick_launch_display_mode_shortcuts)
                .setIcon(com.android.internal.R.drawable.ic_menu_goto);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(DISPLAY_MODE_LAUNCH).setVisible(mDisplayMode != DISPLAY_MODE_LAUNCH);
        menu.findItem(DISPLAY_MODE_SHORTCUT).setVisible(mDisplayMode != DISPLAY_MODE_SHORTCUT);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DISPLAY_MODE_LAUNCH: 
                mDisplayMode = DISPLAY_MODE_LAUNCH;
                break;
            case DISPLAY_MODE_SHORTCUT:
                mDisplayMode = DISPLAY_MODE_SHORTCUT;
                break;
            default:
                return false;
        }
        updateListAndAdapter();
        return true;
    }
    private void ensureIntents() {
        if (sLaunchIntent == null) {
            sLaunchIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            sShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        }
    }
    private void updateListAndAdapter() {
        new Thread("data updater") {
            @Override
            public void run() {
                synchronized (BookmarkPicker.this) {
                    ArrayList<ResolveInfo> newResolveList = new ArrayList<ResolveInfo>();
                    ArrayList<Map<String, ?>> newAdapterList = new ArrayList<Map<String, ?>>();
                    fillResolveList(newResolveList);
                    Collections.sort(newResolveList,
                            new ResolveInfo.DisplayNameComparator(getPackageManager()));
                    fillAdapterList(newAdapterList, newResolveList);
                    updateAdapterToUseNewLists(newAdapterList, newResolveList);
                }
            }
        }.start();  
    }
    private void updateAdapterToUseNewLists(final ArrayList<Map<String, ?>> newAdapterList,
            final ArrayList<ResolveInfo> newResolveList) {
        mUiHandler.post(new Runnable() {
            public void run() {
                mMyAdapter = createResolveAdapter(newAdapterList);
                mResolveList = newResolveList;
                setListAdapter(mMyAdapter);
            }
        });
    }
    private void fillResolveList(List<ResolveInfo> list) {
        ensureIntents();
        PackageManager pm = getPackageManager();
        list.clear();
        if (mDisplayMode == DISPLAY_MODE_LAUNCH) {
            list.addAll(pm.queryIntentActivities(sLaunchIntent, 0));
        } else if (mDisplayMode == DISPLAY_MODE_SHORTCUT) {
            list.addAll(pm.queryIntentActivities(sShortcutIntent, 0)); 
        }
    }
    private SimpleAdapter createResolveAdapter(List<Map<String, ?>> list) {
        SimpleAdapter adapter = new SimpleAdapter(this, list,
                R.layout.bookmark_picker_item, sKeys, sResourceIds);
        adapter.setViewBinder(this);
        return adapter;
    }
    private void fillAdapterList(List<Map<String, ?>> list,
            List<ResolveInfo> resolveList) {
        list.clear();
        int resolveListSize = resolveList.size();
        for (int i = 0; i < resolveListSize; i++) {
            ResolveInfo info = resolveList.get(i);
            Map<String, Object> map = new TreeMap<String, Object>();
            map.put(KEY_TITLE, getResolveInfoTitle(info));
            map.put(KEY_RESOLVE_INFO, info);
            list.add(map);
        }
    }
    private String getResolveInfoTitle(ResolveInfo info) {
        CharSequence label = info.loadLabel(getPackageManager());
        if (label == null) label = info.activityInfo.name;
        return label != null ? label.toString() : null;
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (position >= mResolveList.size()) return;
        ResolveInfo info = mResolveList.get(position);
        switch (mDisplayMode) {
            case DISPLAY_MODE_LAUNCH: 
                Intent intent = getIntentForResolveInfo(info, Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                finish(intent, getResolveInfoTitle(info));
                break;
            case DISPLAY_MODE_SHORTCUT:
                startShortcutActivity(info);
                break;
        }
    }
    private static Intent getIntentForResolveInfo(ResolveInfo info, String action) {
        Intent intent = new Intent(action);
        ActivityInfo ai = info.activityInfo;
        intent.setClassName(ai.packageName, ai.name);
        return intent;
    }
    private void startShortcutActivity(ResolveInfo info) {
        Intent intent = getIntentForResolveInfo(info, Intent.ACTION_CREATE_SHORTCUT);
        startActivityForResult(intent, REQUEST_CREATE_SHORTCUT);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CREATE_SHORTCUT:
                if (data != null) {
                    finish((Intent) data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT),
                            data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
    private void finish(Intent intent, String title) {
        intent.putExtras(getIntent());
        intent.putExtra(EXTRA_TITLE, title);
        setResult(RESULT_OK, intent);
        finish();
    }
    public boolean setViewValue(View view, Object data, String textRepresentation) {
        if (view.getId() == R.id.icon) {
            Drawable icon = ((ResolveInfo) data).loadIcon(getPackageManager());
            if (icon != null) {
                ((ImageView) view).setImageDrawable(icon);
            }
            return true;
        } else {
            return false;
        }
    }
}
