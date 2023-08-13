public abstract class GridScenario extends Activity {
    private GridView mGridView;
    private int mNumItems;
    private int mStartingSelectionPosition;
    private double mItemScreenSizeFactor;
    private Map<Integer, Double> mOverrideItemScreenSizeFactors = Maps.newHashMap();
    private int mScreenHeight;
    private boolean mStackFromBottom;
    private int mColumnWidth;
    private int mNumColumns;
    private int mStretchMode;
    private int mVerticalSpacing;
    public GridView getGridView() {
        return mGridView;
    }
    protected int getScreenHeight() {
        return mScreenHeight;
    }
    protected int getInitialNumItems() {
        return mNumItems;
    }
    public int getDesiredItemHeight() {
        return (int) (mScreenHeight * mItemScreenSizeFactor);
    }
    public static class Params {
        private int mNumItems = 4;
        private int mStartingSelectionPosition = -1;
        private double mItemScreenSizeFactor = 1 / 5;
        private Map<Integer, Double> mOverrideItemScreenSizeFactors = Maps.newHashMap();
        private boolean mStackFromBottom = false;
        private boolean mMustFillScreen = true;
        private int mColumnWidth = 0;
        private int mNumColumns = GridView.AUTO_FIT;
        private int mStretchMode = GridView.STRETCH_COLUMN_WIDTH;
        private int mVerticalSpacing = 0;
        public Params setNumItems(int numItems) {
            mNumItems = numItems;
            return this;
        }
        public Params setStartingSelectionPosition(int startingSelectionPosition) {
            mStartingSelectionPosition = startingSelectionPosition;
            return this;
        }
        public Params setItemScreenSizeFactor(double itemScreenSizeFactor) {
            mItemScreenSizeFactor = itemScreenSizeFactor;
            return this;
        }
        public Params setPositionScreenSizeFactorOverride(
                int position, double itemScreenSizeFactor) {
            mOverrideItemScreenSizeFactors.put(position, itemScreenSizeFactor);
            return this;
        }
        public Params setStackFromBottom(boolean stackFromBottom) {
            mStackFromBottom = stackFromBottom;
            return this;
        }
        public Params setMustFillScreen(boolean fillScreen) {
            mMustFillScreen = fillScreen;
            return this;
        }
        public Params setColumnWidth(int requestedWidth) {
            mColumnWidth = requestedWidth;
            return this;
        }
        public Params setNumColumns(int numColumns) {
            mNumColumns = numColumns;
            return this;
        }
        public Params setStretchMode(int stretchMode) {
            mStretchMode = stretchMode;
            return this;
        }
        public Params setVerticalSpacing(int verticalSpacing) {
            mVerticalSpacing  = verticalSpacing;
            return this;
        }
    }
    protected abstract void init(Params params);
    protected ListAdapter createAdapter() {
        return new MyAdapter();
    }
    @SuppressWarnings({ "UnusedDeclaration" })
    protected void positionSelected(int positon) {
    }
    protected void nothingSelected() {
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mScreenHeight = getWindowManager().getDefaultDisplay().getHeight();
        final Params params = new Params();
        init(params);
        readAndValidateParams(params);
        mGridView = new GridView(this);
        mGridView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mGridView.setDrawSelectorOnTop(false);
        if (mNumColumns >= GridView.AUTO_FIT) {
            mGridView.setNumColumns(mNumColumns);
        }
        if (mColumnWidth > 0) {
            mGridView.setColumnWidth(mColumnWidth);
        }
        if (mVerticalSpacing > 0) {
            mGridView.setVerticalSpacing(mVerticalSpacing);
        }
        mGridView.setStretchMode(mStretchMode);
        mGridView.setAdapter(createAdapter());
        if (mStartingSelectionPosition >= 0) {
            mGridView.setSelection(mStartingSelectionPosition);
        }
        mGridView.setPadding(10, 10, 10, 10);
        mGridView.setStackFromBottom(mStackFromBottom);
        mGridView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent, View v, int position, long id) {
                positionSelected(position);
            }
            public void onNothingSelected(AdapterView parent) {
                nothingSelected();
            }
        });
        setContentView(mGridView);
    }
    private void readAndValidateParams(Params params) {
        if (params.mMustFillScreen ) {
            double totalFactor = 0.0;
            for (int i = 0; i < params.mNumItems; i++) {
                if (params.mOverrideItemScreenSizeFactors.containsKey(i)) {
                    totalFactor += params.mOverrideItemScreenSizeFactors.get(i);
                } else {
                    totalFactor += params.mItemScreenSizeFactor;
                }
            }
            if (totalFactor < 1.0) {
                throw new IllegalArgumentException("grid items must combine to be at least " +
                        "the height of the screen.  this is not the case with " + params.mNumItems
                        + " items and " + params.mItemScreenSizeFactor + " screen factor and " +
                        "screen height of " + mScreenHeight);
            }
        }
        mNumItems = params.mNumItems;
        mStartingSelectionPosition = params.mStartingSelectionPosition;
        mItemScreenSizeFactor = params.mItemScreenSizeFactor;
        mOverrideItemScreenSizeFactors.putAll(params.mOverrideItemScreenSizeFactors);
        mStackFromBottom = params.mStackFromBottom;
        mColumnWidth = params.mColumnWidth;
        mNumColumns = params.mNumColumns;
        mStretchMode = params.mStretchMode;
        mVerticalSpacing = params.mVerticalSpacing;
    }
    public final String getValueAtPosition(int position) {
        return "postion " + position;
    }
    protected View createView(int position, ViewGroup parent, int desiredHeight) {
        TextView result = new TextView(parent.getContext());
        result.setHeight(desiredHeight);
        result.setText(getValueAtPosition(position));
        final ViewGroup.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        result.setLayoutParams(lp);
        result.setId(position);
        result.setBackgroundColor(0x55ffffff);
        return result;
    }
    private class MyAdapter extends BaseAdapter {
        public int getCount() {
            return mNumItems;
        }
        public Object getItem(int position) {
            return getValueAtPosition(position);
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView != null) {
                ((TextView) convertView).setText(getValueAtPosition(position));
                convertView.setId(position);
                return convertView;
            }
            int desiredHeight = getDesiredItemHeight();
            if (mOverrideItemScreenSizeFactors.containsKey(position)) {
                desiredHeight = (int) (mScreenHeight * mOverrideItemScreenSizeFactors.get(position));
            }
            return createView(position, parent, desiredHeight);
        }
    }
}
