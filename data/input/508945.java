public class UsageStats extends Activity implements OnItemSelectedListener {
    private static final String TAG="UsageStatsActivity";
    private static final boolean localLOGV = true;
    private Spinner mTypeSpinner;
    private ListView mListView;
    private IUsageStats mUsageStatsService;
    private LayoutInflater mInflater;
    private UsageStatsAdapter mAdapter;
    private PackageManager mPm;
    public static class AppNameComparator implements Comparator<PkgUsageStats> {
        Map<String, CharSequence> mAppLabelList;
        AppNameComparator(Map<String, CharSequence> appList) {
            mAppLabelList = appList;
        }
        public final int compare(PkgUsageStats a, PkgUsageStats b) {
            String alabel = mAppLabelList.get(a.packageName).toString();
            String blabel = mAppLabelList.get(b.packageName).toString();
            return alabel.compareTo(blabel);
        }
    }
    public static class LaunchCountComparator implements Comparator<PkgUsageStats> {
        public final int compare(PkgUsageStats a, PkgUsageStats b) {
            return b.launchCount - a.launchCount;
        }
    }
    public static class UsageTimeComparator implements Comparator<PkgUsageStats> {
        public final int compare(PkgUsageStats a, PkgUsageStats b) {
            long ret = a.usageTime-b.usageTime;
            if (ret == 0) {
                return 0;
            }
            if (ret < 0) {
                return 1;
            }
            return -1;
        }
    }
    static class AppViewHolder {
        TextView pkgName;
        TextView launchCount;
        TextView usageTime;
    }
    class UsageStatsAdapter extends BaseAdapter {
        private static final int _DISPLAY_ORDER_USAGE_TIME = 0;
        private static final int _DISPLAY_ORDER_LAUNCH_COUNT = 1;
        private static final int _DISPLAY_ORDER_APP_NAME = 2;
        private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
        private List<PkgUsageStats> mUsageStats;
        private LaunchCountComparator mLaunchCountComparator;
        private UsageTimeComparator mUsageTimeComparator;
        private AppNameComparator mAppLabelComparator;
        private HashMap<String, CharSequence> mAppLabelMap;
        UsageStatsAdapter() {
            mUsageStats = new ArrayList<PkgUsageStats>();
            mAppLabelMap = new HashMap<String, CharSequence>();
            PkgUsageStats[] stats;
            try {
                stats = mUsageStatsService.getAllPkgUsageStats();
            } catch (RemoteException e) {
                Log.e(TAG, "Failed initializing usage stats service");
                return;
            }
           if (stats == null) {
               return;
           }
           for (PkgUsageStats ps : stats) {
               mUsageStats.add(ps);
               CharSequence label;
               try {
                   ApplicationInfo appInfo = mPm.getApplicationInfo(ps.packageName, 0);
                   label = appInfo.loadLabel(mPm);
                } catch (NameNotFoundException e) {
                    label = ps.packageName;
                }
                mAppLabelMap.put(ps.packageName, label);
           }
           mLaunchCountComparator = new LaunchCountComparator();
           mUsageTimeComparator = new UsageTimeComparator();
           mAppLabelComparator = new AppNameComparator(mAppLabelMap);
           sortList();
        }
        public int getCount() {
            return mUsageStats.size();
        }
        public Object getItem(int position) {
            return mUsageStats.get(position);
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            AppViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.usage_stats_item, null);
                holder = new AppViewHolder();
                holder.pkgName = (TextView) convertView.findViewById(R.id.package_name);
                holder.launchCount = (TextView) convertView.findViewById(R.id.launch_count);
                holder.usageTime = (TextView) convertView.findViewById(R.id.usage_time);
                convertView.setTag(holder);
            } else {
                holder = (AppViewHolder) convertView.getTag();
            }
            PkgUsageStats pkgStats = mUsageStats.get(position);
            if (pkgStats != null) {
                CharSequence label = mAppLabelMap.get(pkgStats.packageName);
                holder.pkgName.setText(label);
                holder.launchCount.setText(String.valueOf(pkgStats.launchCount));
                holder.usageTime.setText(String.valueOf(pkgStats.usageTime)+" ms");
            } else {
                Log.w(TAG, "No usage stats info for package:" + position);
            }
            return convertView;
        }
        void sortList(int sortOrder) {
            if (mDisplayOrder == sortOrder) {
                return;
            }
            mDisplayOrder= sortOrder;
            sortList();
        }
        private void sortList() {
            if (mDisplayOrder == _DISPLAY_ORDER_USAGE_TIME) {
                if (localLOGV) Log.i(TAG, "Sorting by usage time");
                Collections.sort(mUsageStats, mUsageTimeComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_LAUNCH_COUNT) {
                if (localLOGV) Log.i(TAG, "Sorting launch count");
                Collections.sort(mUsageStats, mLaunchCountComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_APP_NAME) {
                if (localLOGV) Log.i(TAG, "Sorting by application name");
                Collections.sort(mUsageStats, mAppLabelComparator);
            }
            notifyDataSetChanged();
        }
    }
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mUsageStatsService = IUsageStats.Stub.asInterface(ServiceManager.getService("usagestats"));
        if (mUsageStatsService == null) {
            Log.e(TAG, "Failed to retrieve usagestats service");
            return;
        }
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPm = getPackageManager();
        setContentView(R.layout.usage_stats);
        mTypeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        mTypeSpinner.setOnItemSelectedListener(this);
        mListView = (ListView) findViewById(R.id.pkg_list);
        mAdapter = new UsageStatsAdapter();
        mListView.setAdapter(mAdapter);
    }
    public void onItemSelected(AdapterView<?> parent, View view, int position,
            long id) {
        mAdapter.sortList(position);
    }
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
