public class AppHwConfigList extends ListActivity {
    private static final String TAG = "AppHwConfigList";
    PackageManager mPm;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPm = getPackageManager();
        mAdapter = new AppListAdapter(this);
        if (mAdapter.getCount() <= 0) {
            finish();
        } else {
            setListAdapter(mAdapter);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        PackageInfo app = mAdapter.appForPosition(position);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(this, AppHwPref.class);
        intent.putExtra("packageName", app.packageName);
        startActivity(intent);
    }
    private final class AppListAdapter extends BaseAdapter {
        public AppListAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            List<ApplicationInfo> appList = mPm.getInstalledApplications(0);
            for (ApplicationInfo app : appList) {
                PackageInfo pkgInfo = null;
                try {
                    pkgInfo = mPm.getPackageInfo(app.packageName, 0);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                if ((pkgInfo != null)) {
                        if(mList == null) {
                             mList = new ArrayList<PackageInfo>();
                         }
                         mList.add(pkgInfo);
                }
            }
            if (mList != null) {
                Collections.sort(mList, sDisplayNameComparator);
            }
        }
        public PackageInfo appForPosition(int position) {
            if (mList == null) {
                return null;
            }
            return mList.get(position);
        }
        public int getCount() {
            return mList != null ? mList.size() : 0;
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(
                        android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }
            bindView(view, mList.get(position));
            return view;
        }
        private final void bindView(View view, PackageInfo info) {
            TextView text = (TextView)view.findViewById(android.R.id.text1);
            text.setText(info != null ? info.applicationInfo.loadLabel(mPm) : "(none)");
        }
        protected final Context mContext;
        protected final LayoutInflater mInflater;
        protected List<PackageInfo> mList;
    }
    private final Comparator sDisplayNameComparator = new Comparator() {
        public final int compare(Object a, Object b) {
            CharSequence  sa = ((PackageInfo) a).applicationInfo.loadLabel(mPm);
            CharSequence  sb = ((PackageInfo) b).applicationInfo.loadLabel(mPm);
            return collator.compare(sa, sb);
        }
        private final Collator   collator = Collator.getInstance();
    };
    private AppListAdapter mAdapter;
}
