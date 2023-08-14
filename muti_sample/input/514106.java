public abstract class ListScenario extends Activity {
    private ListView mListView;
    private TextView mHeaderTextView;
    private int mNumItems;
    protected boolean mItemsFocusable;
    private int mStartingSelectionPosition;
    private double mItemScreenSizeFactor;
    private Map<Integer, Double> mOverrideItemScreenSizeFactors = Maps.newHashMap();
    private int mScreenHeight;
    private boolean mIncludeHeader;
    private Set<Integer> mUnselectableItems = new HashSet<Integer>();
    private boolean mStackFromBottom;
    private int mClickedPosition = -1;
    private int mLongClickedPosition = -1;
    private int mConvertMisses = 0;
    private int mHeaderViewCount;
    private boolean mHeadersFocusable;
    private int mFooterViewCount;
    private LinearLayout mLinearLayout;
    public ListView getListView() {
        return mListView;
    }
    protected int getScreenHeight() {
        return mScreenHeight;
    }
    private boolean isItemAtPositionSelectable(int position) {
        return !mUnselectableItems.contains(position);
    }
    public static class Params {
        private int mNumItems = 4;
        private boolean mItemsFocusable = false;
        private int mStartingSelectionPosition = 0;
        private double mItemScreenSizeFactor = 1 / 5;
        private Double mFadingEdgeScreenSizeFactor = null;
        private Map<Integer, Double> mOverrideItemScreenSizeFactors = Maps.newHashMap();
        private List<Integer> mUnselectableItems = new ArrayList<Integer>(8);
        private boolean mIncludeHeader = false;
        private boolean mStackFromBottom = false;
        public boolean mMustFillScreen = true;
        private int mHeaderViewCount;
        private boolean mHeaderFocusable = false;
        private int mFooterViewCount;
        private boolean mConnectAdapter = true;
        public Params setNumItems(int numItems) {
            mNumItems = numItems;
            return this;
        }
        public Params setItemsFocusable(boolean itemsFocusable) {
            mItemsFocusable = itemsFocusable;
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
        public Params setPositionUnselectable(int position) {
            mUnselectableItems.add(position);
            return this;
        }
        public Params setPositionsUnselectable(int ...positions) {
            for (int pos : positions) {
                setPositionUnselectable(pos);
            }
            return this;
        }
        public Params includeHeaderAboveList(boolean includeHeader) {
            mIncludeHeader = includeHeader;
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
        public Params setFadingEdgeScreenSizeFactor(double fadingEdgeScreenSizeFactor) {
            mFadingEdgeScreenSizeFactor = fadingEdgeScreenSizeFactor;
            return this;
        }
        public Params setHeaderViewCount(int headerViewCount) {
            mHeaderViewCount = headerViewCount;
            return this;
        }
        public Params setHeaderFocusable(boolean headerFocusable) {
            mHeaderFocusable = headerFocusable;
            return this;
        }
        public Params setFooterViewCount(int footerViewCount) {
            mFooterViewCount = footerViewCount;
            return this;
        }
        public Params setConnectAdapter(boolean connectAdapter) {
            mConnectAdapter = connectAdapter;
            return this;
        }
    }
    protected abstract void init(Params params);
    protected void positionSelected(int positon) {
    }
    protected void nothingSelected() {
    }
    protected void positionClicked(int position) {
        setClickedPosition(position);
    }
    protected void positionLongClicked(int position) {
        setLongClickedPosition(position);
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mScreenHeight = getWindowManager().getDefaultDisplay().getHeight();
        final Params params = createParams();
        init(params);
        readAndValidateParams(params);
        mListView = createListView();
        mListView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mListView.setDrawSelectorOnTop(false);
        for (int i=0; i<mHeaderViewCount; i++) {
            TextView header = mHeadersFocusable ?
                    new EditText(this) :
                    new TextView(this);
            header.setText("Header: " + i);
            mListView.addHeaderView(header);
        }
        for (int i=0; i<mFooterViewCount; i++) {
            TextView header = new TextView(this);
            header.setText("Footer: " + i);
            mListView.addFooterView(header);
        }
        if (params.mConnectAdapter) {
            setAdapter(mListView);
        }
        mListView.setItemsCanFocus(mItemsFocusable);
        if (mStartingSelectionPosition >= 0) {
            mListView.setSelection(mStartingSelectionPosition);
        }
        mListView.setPadding(0, 0, 0, 0);
        mListView.setStackFromBottom(mStackFromBottom);
        mListView.setDivider(null);
        mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent, View v, int position, long id) {
                positionSelected(position);
            }
            public void onNothingSelected(AdapterView parent) {
                nothingSelected();
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                positionClicked(position);
            }
        });
        if (params.mFadingEdgeScreenSizeFactor != null) {
            mListView.setFadingEdgeLength((int) (params.mFadingEdgeScreenSizeFactor * mScreenHeight));            
        } else {
            mListView.setFadingEdgeLength((int) ((64.0 / 480) * mScreenHeight));
        }
        if (mIncludeHeader) {
            mLinearLayout = new LinearLayout(this);
            mHeaderTextView = new TextView(this);
            mHeaderTextView.setText("hi");
            mHeaderTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            mLinearLayout.addView(mHeaderTextView);
            mLinearLayout.setOrientation(LinearLayout.VERTICAL);
            mLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mListView.setLayoutParams((new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f)));
            mLinearLayout.addView(mListView);
            setContentView(mLinearLayout);
        } else {
            mLinearLayout = new LinearLayout(this);
            mLinearLayout.setOrientation(LinearLayout.VERTICAL);
            mLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mListView.setLayoutParams((new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f)));
            mLinearLayout.addView(mListView);
            setContentView(mLinearLayout);
        }
    }
    protected LinearLayout getListViewContainer() {
        return mLinearLayout;
    }
    public void enableLongPress() {
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView parent, View v, int position, long id) {
                positionLongClicked(position);
                return true;
            }
        });
    }
    protected ListView createListView() {
        return new ListView(this);
    }
    protected Params createParams() {
        return new Params();
    }
    protected void setAdapter(ListView listView) {
        listView.setAdapter(new MyAdapter());
    }
    protected void readAndValidateParams(Params params) {
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
                throw new IllegalArgumentException("list items must combine to be at least " +
                        "the height of the screen.  this is not the case with " + params.mNumItems
                        + " items and " + params.mItemScreenSizeFactor + " screen factor and " +
                        "screen height of " + mScreenHeight);
            }
        }
        mNumItems = params.mNumItems;
        mItemsFocusable = params.mItemsFocusable;
        mStartingSelectionPosition = params.mStartingSelectionPosition;
        mItemScreenSizeFactor = params.mItemScreenSizeFactor;
        mOverrideItemScreenSizeFactors.putAll(params.mOverrideItemScreenSizeFactors);
        mUnselectableItems.addAll(params.mUnselectableItems);
        mIncludeHeader = params.mIncludeHeader;
        mStackFromBottom = params.mStackFromBottom;
        mHeaderViewCount = params.mHeaderViewCount;
        mHeadersFocusable = params.mHeaderFocusable;
        mFooterViewCount = params.mFooterViewCount;
    }
    public final String getValueAtPosition(int position) {
        return isItemAtPositionSelectable(position)
                ?
                "position " + position:
                "------- " + position;
    }
    public int getHeightForPosition(int position) {
        int desiredHeight = (int) (mScreenHeight * mItemScreenSizeFactor);
        if (mOverrideItemScreenSizeFactors.containsKey(position)) {
            desiredHeight = (int) (mScreenHeight * mOverrideItemScreenSizeFactors.get(position));
        }
        return desiredHeight;
    }
    public final String getHeaderValue() {
        if (!mIncludeHeader) {
            throw new IllegalArgumentException("no header above list");
        }
        return mHeaderTextView.getText().toString();
    }
    protected final void setHeaderValue(String value) {
        if (!mIncludeHeader) {
            throw new IllegalArgumentException("no header above list");
        }
        mHeaderTextView.setText(value);        
    }
    protected View createView(int position, ViewGroup parent, int desiredHeight) {
        return ListItemFactory.text(position, parent.getContext(), getValueAtPosition(position),
                desiredHeight);
    }
    public View convertView(int position, View convertView, ViewGroup parent) {
        return ListItemFactory.convertText(convertView, getValueAtPosition(position), position);
    }
    public void setClickedPosition(int clickedPosition) {
        mClickedPosition = clickedPosition;
    }
    public int getClickedPosition() {
        return mClickedPosition;
    }
    public void setLongClickedPosition(int longClickedPosition) {
        mLongClickedPosition = longClickedPosition;
    }
    public int getLongClickedPosition() {
        return mLongClickedPosition;
    }
    public void requestRectangleOnScreen(int childIndex, final Rect rect) {
        final View child = getListView().getChildAt(childIndex);
        child.post(new Runnable() {
            public void run() {
                child.requestRectangleOnScreen(rect);
            }
        });
    }
    public int getItemViewType(int position) {
        return 0;
    }
    public int getViewTypeCount() {
        return 1;
    }
    public int getConvertMisses() {
        return mConvertMisses;
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
        @Override
        public boolean areAllItemsEnabled() {
            return mUnselectableItems.isEmpty();
        }
        @Override
        public boolean isEnabled(int position) {
            return isItemAtPositionSelectable(position);
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            View result = null;
            if (position >= mNumItems || position < 0) {
                throw new IllegalStateException("position out of range for adapter!");
            }
            if (convertView != null) {
                result = convertView(position, convertView, parent);
                if (result == null) {
                    mConvertMisses++;
                }
            }
            if (result == null) {
                int desiredHeight = getHeightForPosition(position);
                result = createView(position, parent, desiredHeight);
            }
            return result;
        }
        @Override
        public int getItemViewType(int position) {
            return ListScenario.this.getItemViewType(position);
        }
        @Override
        public int getViewTypeCount() {
            return ListScenario.this.getViewTypeCount();
        }
    }
}
