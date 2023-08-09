public class ListOfInternalSelectionViews extends Activity {
    private ListView mListView;
    public static final String BUNDLE_PARAM_NUM_ITEMS = "com.google.test.numItems";
    public static final String BUNDLE_PARAM_NUM_ROWS_PER_ITEM = "com.google.test.numRowsPerItem";
    public static final String BUNDLE_PARAM_ITEM_SCREEN_HEIGHT_FACTOR = "com.google.test.itemScreenHeightFactor";
    private int mScreenHeight;
    private int mNumItems = 5;
    private int mNumRowsPerItem = 4;
    private double mItemScreenSizeFactor = 5 / 4;
    public ListView getListView() {
        return mListView;
    }
    public double getItemScreenSizeFactor() {
        return mItemScreenSizeFactor;
    }
    public int getNumRowsPerItem() {
        return mNumRowsPerItem;
    }
    public int getNumItems() {
        return mNumItems;
    }
    public String getLabelForPosition(int position) {
        return "position " + position;
    }
    public InternalSelectionView getSelectedView() {
        return (InternalSelectionView) getListView().getSelectedView();
    }
    public int getScreenHeight() {
        return mScreenHeight;
    }
    public static Bundle getBundleFor(int numItems, int numRowsPerItem, double itemScreenHeightFactor) {
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_PARAM_NUM_ITEMS, numItems);
        bundle.putInt(BUNDLE_PARAM_NUM_ROWS_PER_ITEM, numRowsPerItem);
        bundle.putDouble(BUNDLE_PARAM_ITEM_SCREEN_HEIGHT_FACTOR, itemScreenHeightFactor);
        return bundle;
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mScreenHeight = getWindowManager().getDefaultDisplay().getHeight();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            initFromBundle(extras);
        }
        mListView = new ListView(this);
        mListView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mListView.setDrawSelectorOnTop(false);
        mListView.setAdapter(new MyAdapter());
        mListView.setItemsCanFocus(true);
        setContentView(mListView);
    }
    private void initFromBundle(Bundle icicle) {
        int numItems = icicle.getInt(BUNDLE_PARAM_NUM_ITEMS, -1);
        if (numItems != -1) {
            mNumItems = numItems;
        }
        int numRowsPerItem = icicle.getInt(BUNDLE_PARAM_NUM_ROWS_PER_ITEM, -1);
        if (numRowsPerItem != -1) {
            mNumRowsPerItem = numRowsPerItem;
        }
        double screenHeightFactor = icicle.getDouble(BUNDLE_PARAM_ITEM_SCREEN_HEIGHT_FACTOR, -1.0);
        if (screenHeightFactor > 0) {
            mItemScreenSizeFactor = screenHeightFactor;
        }
    }
    private class MyAdapter extends BaseAdapter {
        public int getCount() {
            return mNumItems;
        }
        public Object getItem(int position) {
            return getLabelForPosition(position);
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            InternalSelectionView item =
                    new InternalSelectionView(
                            parent.getContext(),
                            mNumRowsPerItem,
                            getLabelForPosition(position));
            item.setDesiredHeight((int) (mScreenHeight * mItemScreenSizeFactor));
            return item;
        }
    }
}
