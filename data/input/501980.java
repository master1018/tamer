public class GridLayoutAnimStubActivity extends Activity {
    private GridView mGridView;
    private static final int GRID_NUM = 9;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gridlayout_anim_controller_layout);
        mGridView = (GridView) findViewById(R.id.gridlayout_anim_gridview);
        mGridView.setAdapter(new MockGridViewAdapter(GRID_NUM));
    }
    public GridView getGridView() {
        return mGridView;
    }
    private class MockGridViewAdapter implements ListAdapter {
        private final int mCount;
        MockGridViewAdapter(int count) {
            mCount = count;
        }
        MockGridViewAdapter() {
            this(1);
        }
        public boolean areAllItemsEnabled() {
            return true;
        }
        public boolean isEnabled(int position) {
            return true;
        }
        public void registerDataSetObserver(DataSetObserver observer) {
        }
        public void unregisterDataSetObserver(DataSetObserver observer) {
        }
        public int getCount() {
            return mCount;
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }
        public boolean hasStableIds() {
            return false;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            if ((convertView != null) && (convertView instanceof ImageView)) {
                ((ImageView) convertView).setImageResource(R.drawable.size_48x48);
                return convertView;
            }
            ImageView newView = new ImageView(GridLayoutAnimStubActivity.this);
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
            newView.setLayoutParams(params);
            newView.setImageResource(R.drawable.size_48x48);
            return newView;
        }
        public int getItemViewType(int position) {
            return 0;
        }
        public int getViewTypeCount() {
            return 1;
        }
        public boolean isEmpty() {
            return false;
        }
    }
}
