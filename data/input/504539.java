public class AppPicker extends ListActivity
{
    @Override
    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        mAdapter = new AppListAdapter(this);
        if (mAdapter.getCount() <= 0) {
            finish();
        } else {
            setListAdapter(mAdapter);
        }
    }
    @Override
    protected void onResume()
    {
        super.onResume();
    }
    @Override
    protected void onStop()
    {
        super.onStop();
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        MyApplicationInfo app = mAdapter.itemForPosition(position);
        Intent intent = new Intent();
        if (app.info != null) intent.setAction(app.info.packageName);
        setResult(RESULT_OK, intent);
        try {
            boolean waitForDebugger = Settings.System.getInt(
                    getContentResolver(), Settings.System.WAIT_FOR_DEBUGGER, 0) != 0;
            ActivityManagerNative.getDefault().setDebugApp(
                    app.info != null ? app.info.packageName : null, waitForDebugger, true);
        } catch (RemoteException ex) {
        }
        finish();
    }
    class MyApplicationInfo {
        ApplicationInfo info;
        String label;
    }
    public class AppListAdapter extends ArrayAdapter<MyApplicationInfo> {
        private List<MyApplicationInfo> mPackageInfoList = new ArrayList<MyApplicationInfo>();
        public AppListAdapter(Context context) {
            super(context, R.layout.package_list_item);
            List<ApplicationInfo> pkgs = context.getPackageManager().getInstalledApplications(0);
            for (int i=0; i<pkgs.size(); i++) {
                MyApplicationInfo info = new MyApplicationInfo();
                info.info = pkgs.get(i);
                info.label = info.info.loadLabel(getPackageManager()).toString();
                mPackageInfoList.add(info);
            }
            Collections.sort(mPackageInfoList, sDisplayNameComparator);
            MyApplicationInfo info = new MyApplicationInfo();
            info.label = "(none)";
            mPackageInfoList.add(0, info);
            setSource(mPackageInfoList);
        }
        @Override
        public void bindView(View view, MyApplicationInfo info) {
            ImageView icon = (ImageView)view.findViewById(R.id.icon);
            TextView name = (TextView)view.findViewById(R.id.name);
            TextView description = (TextView)view.findViewById(R.id.description);
            name.setText(info.label);
            if (info.info != null) {
                icon.setImageDrawable(info.info.loadIcon(getPackageManager()));
                description.setText(info.info.packageName);
            } else {
                icon.setImageDrawable(null);
                description.setText("");
            }
        }
    }
    private final static Comparator<MyApplicationInfo> sDisplayNameComparator
            = new Comparator<MyApplicationInfo>() {
        public final int
        compare(MyApplicationInfo a, MyApplicationInfo b) {
            return collator.compare(a.label, b.label);
        }
        private final Collator   collator = Collator.getInstance();
    };
    private AppListAdapter mAdapter;
}
