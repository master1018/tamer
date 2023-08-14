public class Grid1 extends Activity {
    GridView mGrid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadApps(); 
        setContentView(R.layout.grid_1);
        mGrid = (GridView) findViewById(R.id.myGrid);
        mGrid.setAdapter(new AppsAdapter());
    }
    private List<ResolveInfo> mApps;
    private void loadApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
    }
    public class AppsAdapter extends BaseAdapter {
        public AppsAdapter() {
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i;
            if (convertView == null) {
                i = new ImageView(Grid1.this);
                i.setScaleType(ImageView.ScaleType.FIT_CENTER);
                i.setLayoutParams(new GridView.LayoutParams(50, 50));
            } else {
                i = (ImageView) convertView;
            }
            ResolveInfo info = mApps.get(position);
            i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));
            return i;
        }
        public final int getCount() {
            return mApps.size();
        }
        public final Object getItem(int position) {
            return mApps.get(position);
        }
        public final long getItemId(int position) {
            return position;
        }
    }
}
