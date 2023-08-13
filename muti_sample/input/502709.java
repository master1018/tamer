public class PackageBrowser extends ListActivity {
    static class MyPackageInfo {
        PackageInfo info;
        String label;
    }
    private PackageListAdapter mAdapter;
    private List<MyPackageInfo> mPackageInfoList = new ArrayList<MyPackageInfo>();
    private Handler mHandler;
    private BroadcastReceiver mRegisteredReceiver;
    public class PackageListAdapter extends ArrayAdapter<MyPackageInfo> {
        public PackageListAdapter(Context context) {
            super(context, R.layout.package_list_item);
            List<PackageInfo> pkgs = context.getPackageManager().getInstalledPackages(0);
            for (int i=0; i<pkgs.size(); i++) {
                MyPackageInfo info = new MyPackageInfo();
                info.info = pkgs.get(i);
                info.label = info.info.applicationInfo.loadLabel(getPackageManager()).toString();
                mPackageInfoList.add(info);
            }
            if (mPackageInfoList != null) {
                Collections.sort(mPackageInfoList, sDisplayNameComparator);
            }
            setSource(mPackageInfoList);
        }
        @Override
        public void bindView(View view, MyPackageInfo info) {
            ImageView icon = (ImageView)view.findViewById(R.id.icon);
            TextView name = (TextView)view.findViewById(R.id.name);
            TextView description = (TextView)view.findViewById(R.id.description);
            icon.setImageDrawable(info.info.applicationInfo.loadIcon(getPackageManager()));
            name.setText(info.label);
            description.setText(info.info.packageName);
        }
    }
    private class ApplicationsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupAdapter();
        }
    }
    private final static Comparator<MyPackageInfo> sDisplayNameComparator
            = new Comparator<MyPackageInfo>() {
        public final int
        compare(MyPackageInfo a, MyPackageInfo b) {
            return collator.compare(a.label, b.label);
        }
        private final Collator   collator = Collator.getInstance();
    };
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setupAdapter();
        mHandler= new Handler();
        registerIntentReceivers();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRegisteredReceiver != null) {
            unregisterReceiver(mRegisteredReceiver);
        }
    }
    private void setupAdapter() {
        mAdapter = new PackageListAdapter(this);
        setListAdapter(mAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Delete package").setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                deletePackage();
                return true;
            }
        });
        return true;
    }
    private void deletePackage() {
        final int curSelection = getSelectedItemPosition();
        if (curSelection >= 0) {
            final MyPackageInfo packageInfo = mAdapter.itemForPosition(curSelection);
            if (packageInfo != null) {
                getPackageManager().deletePackage(packageInfo.info.packageName,
                                                  new IPackageDeleteObserver.Stub() {
                    public void packageDeleted(boolean succeeded) throws RemoteException {
                        if (succeeded) {
                            mPackageInfoList.remove(curSelection);
                            mHandler.post(new Runnable() {
                                    public void run() {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                });
                            final String dataPath = packageInfo.info.applicationInfo.dataDir;
                        } else {
                            mHandler.post(new Runnable() {
                                    public void run() {
                                        new AlertDialog.Builder(PackageBrowser.this)
                                            .setTitle("Oops")
                                            .setMessage("Could not delete package." +
                                                "  Maybe it is in /system/app rather than /data/app?")
                                            .show();
                                    }
                                });
                        }
                    }
                },
                                                  0);
            }
        }
    }
    private void registerIntentReceivers() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        mRegisteredReceiver = new ApplicationsIntentReceiver();
        registerReceiver(mRegisteredReceiver, filter);
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        MyPackageInfo info =
            mAdapter.itemForPosition(position);
        if (info != null) {
            Intent intent = new Intent(
                null, Uri.fromParts("package", info.info.packageName, null));
            intent.setClass(this, PackageSummary.class);
            startActivity(intent);
        }
    }
}
