public class AutoCompleteTextView extends EditText implements Filter.FilterListener {
    static final boolean DEBUG = false;
    static final String TAG = "AutoCompleteTextView";
    private static final int HINT_VIEW_ID = 0x17;
    private static final int EXPAND_LIST_TIMEOUT = 250;
    private CharSequence mHintText;
    private int mHintResource;
    private ListAdapter mAdapter;
    private Filter mFilter;
    private int mThreshold;
    private PopupWindow mPopup;
    private DropDownListView mDropDownList;
    private int mDropDownVerticalOffset;
    private int mDropDownHorizontalOffset;
    private int mDropDownAnchorId;
    private View mDropDownAnchorView;  
    private int mDropDownWidth;
    private int mDropDownHeight;
    private final Rect mTempRect = new Rect();
    private Drawable mDropDownListHighlight;
    private AdapterView.OnItemClickListener mItemClickListener;
    private AdapterView.OnItemSelectedListener mItemSelectedListener;
    private final DropDownItemClickListener mDropDownItemClickListener =
            new DropDownItemClickListener();
    private boolean mDropDownAlwaysVisible = false;
    private boolean mDropDownDismissedOnCompletion = true;
    private boolean mForceIgnoreOutsideTouch = false;
    private int mLastKeyCode = KeyEvent.KEYCODE_UNKNOWN;
    private boolean mOpenBefore;
    private Validator mValidator = null;
    private boolean mBlockCompletion;
    private ListSelectorHider mHideSelector;
    private Runnable mShowDropDownRunnable;
    private Runnable mResizePopupRunnable = new ResizePopupRunnable();
    private PassThroughClickListener mPassThroughClickListener;
    private PopupDataSetObserver mObserver;
    public AutoCompleteTextView(Context context) {
        this(context, null);
    }
    public AutoCompleteTextView(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.autoCompleteTextViewStyle);
    }
    public AutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPopup = new PopupWindow(context, attrs,
                com.android.internal.R.attr.autoCompleteTextViewStyle);
        mPopup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        TypedArray a =
            context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.AutoCompleteTextView, defStyle, 0);
        mThreshold = a.getInt(
                R.styleable.AutoCompleteTextView_completionThreshold, 2);
        mHintText = a.getText(R.styleable.AutoCompleteTextView_completionHint);
        mDropDownListHighlight = a.getDrawable(
                R.styleable.AutoCompleteTextView_dropDownSelector);
        mDropDownVerticalOffset = (int)
                a.getDimension(R.styleable.AutoCompleteTextView_dropDownVerticalOffset, 0.0f);
        mDropDownHorizontalOffset = (int)
                a.getDimension(R.styleable.AutoCompleteTextView_dropDownHorizontalOffset, 0.0f);
        mDropDownAnchorId = a.getResourceId(R.styleable.AutoCompleteTextView_dropDownAnchor,
                View.NO_ID);
        mDropDownWidth = a.getLayoutDimension(R.styleable.AutoCompleteTextView_dropDownWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mDropDownHeight = a.getLayoutDimension(R.styleable.AutoCompleteTextView_dropDownHeight,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mHintResource = a.getResourceId(R.styleable.AutoCompleteTextView_completionHintView,
                R.layout.simple_dropdown_hint);
        int inputType = getInputType();
        if ((inputType&EditorInfo.TYPE_MASK_CLASS)
                == EditorInfo.TYPE_CLASS_TEXT) {
            inputType |= EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE;
            setRawInputType(inputType);
        }
        a.recycle();
        setFocusable(true);
        addTextChangedListener(new MyWatcher());
        mPassThroughClickListener = new PassThroughClickListener();
        super.setOnClickListener(mPassThroughClickListener);
    }
    @Override
    public void setOnClickListener(OnClickListener listener) {
        mPassThroughClickListener.mWrapped = listener;
    }
    private void onClickImpl() {
        if (mPopup.isShowing()) {
            ensureImeVisible(true);
        }
    }
    public void setCompletionHint(CharSequence hint) {
        mHintText = hint;
    }
    public int getDropDownWidth() {
        return mDropDownWidth;
    }
    public void setDropDownWidth(int width) {
        mDropDownWidth = width;
    }
    public int getDropDownHeight() {
        return mDropDownHeight;
    }
    public void setDropDownHeight(int height) {
        mDropDownHeight = height;
    }
    public int getDropDownAnchor() {
        return mDropDownAnchorId;
    }
    public void setDropDownAnchor(int id) {
        mDropDownAnchorId = id;
        mDropDownAnchorView = null;
    }
    public Drawable getDropDownBackground() {
        return mPopup.getBackground();
    }
    public void setDropDownBackgroundDrawable(Drawable d) {
        mPopup.setBackgroundDrawable(d);
    }
    public void setDropDownBackgroundResource(int id) {
        mPopup.setBackgroundDrawable(getResources().getDrawable(id));
    }
    public void setDropDownVerticalOffset(int offset) {
        mDropDownVerticalOffset = offset;
    }
    public int getDropDownVerticalOffset() {
        return mDropDownVerticalOffset;
    }
    public void setDropDownHorizontalOffset(int offset) {
        mDropDownHorizontalOffset = offset;
    }
    public int getDropDownHorizontalOffset() {
        return mDropDownHorizontalOffset;
    }
    public void setDropDownAnimationStyle(int animationStyle) {
        mPopup.setAnimationStyle(animationStyle);
    }
    public int getDropDownAnimationStyle() {
        return mPopup.getAnimationStyle();
    }
    public boolean isDropDownAlwaysVisible() {
        return mDropDownAlwaysVisible;
    }
    public void setDropDownAlwaysVisible(boolean dropDownAlwaysVisible) {
        mDropDownAlwaysVisible = dropDownAlwaysVisible;
    }
    public boolean isDropDownDismissedOnCompletion() {
        return mDropDownDismissedOnCompletion;
    }
    public void setDropDownDismissedOnCompletion(boolean dropDownDismissedOnCompletion) {
        mDropDownDismissedOnCompletion = dropDownDismissedOnCompletion;
    }
    public int getThreshold() {
        return mThreshold;
    }
    public void setThreshold(int threshold) {
        if (threshold <= 0) {
            threshold = 1;
        }
        mThreshold = threshold;
    }
    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        mItemClickListener = l;
    }
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener l) {
        mItemSelectedListener = l;
    }
    @Deprecated
    public AdapterView.OnItemClickListener getItemClickListener() {
        return mItemClickListener;
    }
    @Deprecated
    public AdapterView.OnItemSelectedListener getItemSelectedListener() {
        return mItemSelectedListener;
    }
    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return mItemClickListener;
    }
    public AdapterView.OnItemSelectedListener getOnItemSelectedListener() {
        return mItemSelectedListener;
    }
    public ListAdapter getAdapter() {
        return mAdapter;
    }
    public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
        if (mObserver == null) {
            mObserver = new PopupDataSetObserver();
        } else if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mFilter = ((Filterable) mAdapter).getFilter();
            adapter.registerDataSetObserver(mObserver);
        } else {
            mFilter = null;
        }
        if (mDropDownList != null) {
            mDropDownList.setAdapter(mAdapter);
        }
    }
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isPopupShowing()
                && !mDropDownAlwaysVisible) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                getKeyDispatcherState().startTracking(event, this);
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                getKeyDispatcherState().handleUpEvent(event);
                if (event.isTracking() && !event.isCanceled()) {
                    dismissDropDown();
                    return true;
                }
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (isPopupShowing() && mDropDownList.getSelectedItemPosition() >= 0) {
            boolean consumed = mDropDownList.onKeyUp(keyCode, event);
            if (consumed) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        performCompletion();
                        return true;
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isPopupShowing()) {
            if (keyCode != KeyEvent.KEYCODE_SPACE
                    && (mDropDownList.getSelectedItemPosition() >= 0
                            || (keyCode != KeyEvent.KEYCODE_ENTER
                                    && keyCode != KeyEvent.KEYCODE_DPAD_CENTER))) {
                int curIndex = mDropDownList.getSelectedItemPosition();
                boolean consumed;
                final boolean below = !mPopup.isAboveAnchor();
                final ListAdapter adapter = mAdapter;
                boolean allEnabled;
                int firstItem = Integer.MAX_VALUE;
                int lastItem = Integer.MIN_VALUE;
                if (adapter != null) {
                    allEnabled = adapter.areAllItemsEnabled();
                    firstItem = allEnabled ? 0 :
                            mDropDownList.lookForSelectablePosition(0, true);
                    lastItem = allEnabled ? adapter.getCount() - 1 :
                            mDropDownList.lookForSelectablePosition(adapter.getCount() - 1, false);                    
                }
                if ((below && keyCode == KeyEvent.KEYCODE_DPAD_UP && curIndex <= firstItem) ||
                        (!below && keyCode == KeyEvent.KEYCODE_DPAD_DOWN && curIndex >= lastItem)) {
                    clearListSelection();
                    mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
                    showDropDown();
                    return true;
                } else {
                    mDropDownList.mListSelectionHidden = false;
                }
                consumed = mDropDownList.onKeyDown(keyCode, event);
                if (DEBUG) Log.v(TAG, "Key down: code=" + keyCode + " list consumed=" + consumed);
                if (consumed) {
                    mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
                    mDropDownList.requestFocusFromTouch();
                    showDropDown();
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                        case KeyEvent.KEYCODE_DPAD_UP:
                            return true;
                    }
                } else {
                    if (below && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (curIndex == lastItem) {
                            return true;
                        }
                    } else if (!below && keyCode == KeyEvent.KEYCODE_DPAD_UP &&
                            curIndex == firstItem) {
                        return true;
                    }
                }
            }
        } else {
            switch(keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                performValidation();
            }
        }
        mLastKeyCode = keyCode;
        boolean handled = super.onKeyDown(keyCode, event);
        mLastKeyCode = KeyEvent.KEYCODE_UNKNOWN;
        if (handled && isPopupShowing() && mDropDownList != null) {
            clearListSelection();
        }
        return handled;
    }
    public boolean enoughToFilter() {
        if (DEBUG) Log.v(TAG, "Enough to filter: len=" + getText().length()
                + " threshold=" + mThreshold);
        return getText().length() >= mThreshold;
    }
    private class MyWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            doAfterTextChanged();
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            doBeforeTextChanged();
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
    void doBeforeTextChanged() {
        if (mBlockCompletion) return;
        mOpenBefore = isPopupShowing();
        if (DEBUG) Log.v(TAG, "before text changed: open=" + mOpenBefore);
    }
    void doAfterTextChanged() {
        if (mBlockCompletion) return;
        if (DEBUG) Log.v(TAG, "after text changed: openBefore=" + mOpenBefore
                + " open=" + isPopupShowing());
        if (mOpenBefore && !isPopupShowing()) {
            return;
        }
        if (enoughToFilter()) {
            if (mFilter != null) {
                performFiltering(getText(), mLastKeyCode);
            }
        } else {
            if (!mDropDownAlwaysVisible) dismissDropDown();
            if (mFilter != null) {
                mFilter.filter(null);
            }
        }
    }
    public boolean isPopupShowing() {
        return mPopup.isShowing();
    }
    protected CharSequence convertSelectionToString(Object selectedItem) {
        return mFilter.convertResultToString(selectedItem);
    }
    public void clearListSelection() {
        final DropDownListView list = mDropDownList;
        if (list != null) {
            list.mListSelectionHidden = true;
            list.hideSelector();
            list.requestLayout();
        }
    }
    public void setListSelection(int position) {
        if (mPopup.isShowing() && (mDropDownList != null)) {
            mDropDownList.mListSelectionHidden = false;
            mDropDownList.setSelection(position);
        }
    }
    public int getListSelection() {
        if (mPopup.isShowing() && (mDropDownList != null)) {
            return mDropDownList.getSelectedItemPosition();
        }
        return ListView.INVALID_POSITION;
    }
    @SuppressWarnings({ "UnusedDeclaration" })
    protected void performFiltering(CharSequence text, int keyCode) {
        mFilter.filter(text, this);
    }
    public void performCompletion() {
        performCompletion(null, -1, -1);
    }
    @Override
    public void onCommitCompletion(CompletionInfo completion) {
        if (isPopupShowing()) {
            mBlockCompletion = true;
            replaceText(completion.getText());
            mBlockCompletion = false;
            if (mItemClickListener != null) {
                final DropDownListView list = mDropDownList;
                mItemClickListener.onItemClick(list, null, completion.getPosition(),
                        completion.getId());
            }
        }
    }
    private void performCompletion(View selectedView, int position, long id) {
        if (isPopupShowing()) {
            Object selectedItem;
            if (position < 0) {
                selectedItem = mDropDownList.getSelectedItem();
            } else {
                selectedItem = mAdapter.getItem(position);
            }
            if (selectedItem == null) {
                Log.w(TAG, "performCompletion: no selected item");
                return;
            }
            mBlockCompletion = true;
            replaceText(convertSelectionToString(selectedItem));
            mBlockCompletion = false;            
            if (mItemClickListener != null) {
                final DropDownListView list = mDropDownList;
                if (selectedView == null || position < 0) {
                    selectedView = list.getSelectedView();
                    position = list.getSelectedItemPosition();
                    id = list.getSelectedItemId();
                }
                mItemClickListener.onItemClick(list, selectedView, position, id);
            }
        }
        if (mDropDownDismissedOnCompletion && !mDropDownAlwaysVisible) {
            dismissDropDown();
        }
    }
    public boolean isPerformingCompletion() {
        return mBlockCompletion;
    }
    public void setText(CharSequence text, boolean filter) {
        if (filter) {
            setText(text);
        } else {
            mBlockCompletion = true;
            setText(text);
            mBlockCompletion = false;
        }
    }
    protected void replaceText(CharSequence text) {
        clearComposingText();
        setText(text);
        Editable spannable = getText();
        Selection.setSelection(spannable, spannable.length());
    }
    public void onFilterComplete(int count) {
        updateDropDownForFilter(count);
    }
    private void updateDropDownForFilter(int count) {
        if (getWindowVisibility() == View.GONE) return;
        if ((count > 0 || mDropDownAlwaysVisible) && enoughToFilter()) {
            if (hasFocus() && hasWindowFocus()) {
                showDropDown();
            }
        } else if (!mDropDownAlwaysVisible) {
            dismissDropDown();
        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus && !mDropDownAlwaysVisible) {
            dismissDropDown();
        }
    }
    @Override
    protected void onDisplayHint(int hint) {
        super.onDisplayHint(hint);
        switch (hint) {
            case INVISIBLE:
                if (!mDropDownAlwaysVisible) {
                    dismissDropDown();
                }
                break;
        }
    }
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (!focused) {
            performValidation();
        }
        if (!focused && !mDropDownAlwaysVisible) {
            dismissDropDown();
        }
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
    @Override
    protected void onDetachedFromWindow() {
        dismissDropDown();
        super.onDetachedFromWindow();
    }
    public void dismissDropDown() {
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null) {
            imm.displayCompletions(this, null);
        }
        mPopup.dismiss();
        mPopup.setContentView(null);
        mDropDownList = null;
    }
    @Override
    protected boolean setFrame(final int l, int t, final int r, int b) {
        boolean result = super.setFrame(l, t, r, b);
        if (mPopup.isShowing()) {
            showDropDown();
        }
        return result;
    }
    private View getDropDownAnchorView() {
        if (mDropDownAnchorView == null && mDropDownAnchorId != View.NO_ID) {
            mDropDownAnchorView = getRootView().findViewById(mDropDownAnchorId);
        }
        return mDropDownAnchorView == null ? this : mDropDownAnchorView;
    }
    public void showDropDownAfterLayout() {
        post(mShowDropDownRunnable);
    }
    public void ensureImeVisible(boolean visible) {
        mPopup.setInputMethodMode(visible
                ? PopupWindow.INPUT_METHOD_NEEDED : PopupWindow.INPUT_METHOD_NOT_NEEDED);
        showDropDown();
    }
    public boolean isInputMethodNotNeeded() {
        return mPopup.getInputMethodMode() == PopupWindow.INPUT_METHOD_NOT_NEEDED;
    }
    public void showDropDown() {
        int height = buildDropDown();
        int widthSpec = 0;
        int heightSpec = 0;
        boolean noInputMethod = isInputMethodNotNeeded();
        if (mPopup.isShowing()) {
            if (mDropDownWidth == ViewGroup.LayoutParams.MATCH_PARENT) {
                widthSpec = -1;
            } else if (mDropDownWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
                widthSpec = getDropDownAnchorView().getWidth();
            } else {
                widthSpec = mDropDownWidth;
            }
            if (mDropDownHeight == ViewGroup.LayoutParams.MATCH_PARENT) {
                heightSpec = noInputMethod ? height : ViewGroup.LayoutParams.MATCH_PARENT;
                if (noInputMethod) {
                    mPopup.setWindowLayoutMode(
                            mDropDownWidth == ViewGroup.LayoutParams.MATCH_PARENT ?
                                    ViewGroup.LayoutParams.MATCH_PARENT : 0, 0);
                } else {
                    mPopup.setWindowLayoutMode(
                            mDropDownWidth == ViewGroup.LayoutParams.MATCH_PARENT ?
                                    ViewGroup.LayoutParams.MATCH_PARENT : 0,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                }
            } else if (mDropDownHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                heightSpec = height;
            } else {
                heightSpec = mDropDownHeight;
            }
            mPopup.setOutsideTouchable(!mForceIgnoreOutsideTouch && !mDropDownAlwaysVisible);
            mPopup.update(getDropDownAnchorView(), mDropDownHorizontalOffset,
                    mDropDownVerticalOffset, widthSpec, heightSpec);
        } else {
            if (mDropDownWidth == ViewGroup.LayoutParams.MATCH_PARENT) {
                widthSpec = ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                if (mDropDownWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    mPopup.setWidth(getDropDownAnchorView().getWidth());
                } else {
                    mPopup.setWidth(mDropDownWidth);
                }
            }
            if (mDropDownHeight == ViewGroup.LayoutParams.MATCH_PARENT) {
                heightSpec = ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                if (mDropDownHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    mPopup.setHeight(height);
                } else {
                    mPopup.setHeight(mDropDownHeight);
                }
            }
            mPopup.setWindowLayoutMode(widthSpec, heightSpec);
            mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
            mPopup.setOutsideTouchable(!mForceIgnoreOutsideTouch && !mDropDownAlwaysVisible);
            mPopup.setTouchInterceptor(new PopupTouchInterceptor());
            mPopup.showAsDropDown(getDropDownAnchorView(),
                    mDropDownHorizontalOffset, mDropDownVerticalOffset);
            mDropDownList.setSelection(ListView.INVALID_POSITION);
            clearListSelection();
            post(mHideSelector);
        }
    }
    public void setForceIgnoreOutsideTouch(boolean forceIgnoreOutsideTouch) {
        mForceIgnoreOutsideTouch = forceIgnoreOutsideTouch;
    }
    private int buildDropDown() {
        ViewGroup dropDownView;
        int otherHeights = 0;
        final ListAdapter adapter = mAdapter;
        if (adapter != null) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null) {
                final int count = Math.min(adapter.getCount(), 20);
                CompletionInfo[] completions = new CompletionInfo[count];
                int realCount = 0;
                for (int i = 0; i < count; i++) {
                    if (adapter.isEnabled(i)) {
                        realCount++;
                        Object item = adapter.getItem(i);
                        long id = adapter.getItemId(i);
                        completions[i] = new CompletionInfo(id, i,
                                convertSelectionToString(item));
                    }
                }
                if (realCount != count) {
                    CompletionInfo[] tmp = new CompletionInfo[realCount];
                    System.arraycopy(completions, 0, tmp, 0, realCount);
                    completions = tmp;
                }
                imm.displayCompletions(this, completions);
            }
        }
        if (mDropDownList == null) {
            Context context = getContext();
            mHideSelector = new ListSelectorHider();
            mShowDropDownRunnable = new Runnable() {
                public void run() {
                    View view = getDropDownAnchorView();
                    if (view != null && view.getWindowToken() != null) {
                        showDropDown();
                    }
                }
            };
            mDropDownList = new DropDownListView(context);
            mDropDownList.setSelector(mDropDownListHighlight);
            mDropDownList.setAdapter(adapter);
            mDropDownList.setVerticalFadingEdgeEnabled(true);
            mDropDownList.setOnItemClickListener(mDropDownItemClickListener);
            mDropDownList.setFocusable(true);
            mDropDownList.setFocusableInTouchMode(true);
            mDropDownList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                        int position, long id) {
                    if (position != -1) {
                        DropDownListView dropDownList = mDropDownList;
                        if (dropDownList != null) {
                            dropDownList.mListSelectionHidden = false;
                        }
                    }
                }
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            mDropDownList.setOnScrollListener(new PopupScrollListener());
            if (mItemSelectedListener != null) {
                mDropDownList.setOnItemSelectedListener(mItemSelectedListener);
            }
            dropDownView = mDropDownList;
            View hintView = getHintView(context);
            if (hintView != null) {
                LinearLayout hintContainer = new LinearLayout(context);
                hintContainer.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f
                );
                hintContainer.addView(dropDownView, hintParams);
                hintContainer.addView(hintView);
                int widthSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST);
                int heightSpec = MeasureSpec.UNSPECIFIED;
                hintView.measure(widthSpec, heightSpec);
                hintParams = (LinearLayout.LayoutParams) hintView.getLayoutParams();
                otherHeights = hintView.getMeasuredHeight() + hintParams.topMargin
                        + hintParams.bottomMargin;
                dropDownView = hintContainer;
            }
            mPopup.setContentView(dropDownView);
        } else {
            dropDownView = (ViewGroup) mPopup.getContentView();
            final View view = dropDownView.findViewById(HINT_VIEW_ID);
            if (view != null) {
                LinearLayout.LayoutParams hintParams =
                        (LinearLayout.LayoutParams) view.getLayoutParams();
                otherHeights = view.getMeasuredHeight() + hintParams.topMargin
                        + hintParams.bottomMargin;
            }
        }
        boolean ignoreBottomDecorations =
                mPopup.getInputMethodMode() == PopupWindow.INPUT_METHOD_NOT_NEEDED;
        final int maxHeight = mPopup.getMaxAvailableHeight(
                getDropDownAnchorView(), mDropDownVerticalOffset, ignoreBottomDecorations);
        int padding = 0;
        Drawable background = mPopup.getBackground();
        if (background != null) {
            background.getPadding(mTempRect);
            padding = mTempRect.top + mTempRect.bottom;
        }
        if (mDropDownAlwaysVisible || mDropDownHeight == ViewGroup.LayoutParams.MATCH_PARENT) {
            return maxHeight + padding;
        }
        final int listContent = mDropDownList.measureHeightOfChildren(MeasureSpec.UNSPECIFIED,
                0, ListView.NO_POSITION, maxHeight - otherHeights, 2);
        if (listContent > 0) otherHeights += padding;
        return listContent + otherHeights;
    }
    private View getHintView(Context context) {
        if (mHintText != null && mHintText.length() > 0) {
            final TextView hintView = (TextView) LayoutInflater.from(context).inflate(
                    mHintResource, null).findViewById(com.android.internal.R.id.text1);
            hintView.setText(mHintText);
            hintView.setId(HINT_VIEW_ID);
            return hintView;
        } else {
            return null;
        }
    }
    public void setValidator(Validator validator) {
        mValidator = validator;
    }
    public Validator getValidator() {
        return mValidator;
    }
    public void performValidation() {
        if (mValidator == null) return;
        CharSequence text = getText();
        if (!TextUtils.isEmpty(text) && !mValidator.isValid(text)) {
            setText(mValidator.fixText(text));
        }
    }
    protected Filter getFilter() {
        return mFilter;
    }
    private class ListSelectorHider implements Runnable {
        public void run() {
            clearListSelection();
        }
    }
    private class ResizePopupRunnable implements Runnable {
        public void run() {
            mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
            showDropDown();
        }
    }
    private class PopupTouchInterceptor implements OnTouchListener {
        public boolean onTouch(View v, MotionEvent event) {
            final int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN &&
                    mPopup != null && mPopup.isShowing()) {
                postDelayed(mResizePopupRunnable, EXPAND_LIST_TIMEOUT);
            } else if (action == MotionEvent.ACTION_UP) {
                removeCallbacks(mResizePopupRunnable);
            }
            return false;
        }
    }
    private class PopupScrollListener implements ListView.OnScrollListener {
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
        }
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == SCROLL_STATE_TOUCH_SCROLL &&
                    !isInputMethodNotNeeded() && mPopup.getContentView() != null) {
                removeCallbacks(mResizePopupRunnable);
                mResizePopupRunnable.run();
            }
        }
    }
    private class DropDownItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            performCompletion(v, position, id);
        }
    }
    private static class DropDownListView extends ListView {
        private boolean mListSelectionHidden;
        public DropDownListView(Context context) {
            super(context, null, com.android.internal.R.attr.dropDownListViewStyle);
        }
        @Override
        View obtainView(int position, boolean[] isScrap) {
            View view = super.obtainView(position, isScrap);
            if (view instanceof TextView) {
                ((TextView) view).setHorizontallyScrolling(true);
            }
            return view;
        }
        @Override
        public boolean isInTouchMode() {
            return mListSelectionHidden || super.isInTouchMode();
        }
        @Override
        public boolean hasWindowFocus() {
            return true;
        }
        @Override
        public boolean isFocused() {
            return true;
        }
        @Override
        public boolean hasFocus() {
            return true;
        }
        protected int[] onCreateDrawableState(int extraSpace) {
            int[] res = super.onCreateDrawableState(extraSpace);
            if (false) {
                StringBuilder sb = new StringBuilder("Created drawable state: [");
                for (int i=0; i<res.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append("0x");
                    sb.append(Integer.toHexString(res[i]));
                }
                sb.append("]");
                Log.i(TAG, sb.toString());
            }
            return res;
        }
    }
    public interface Validator {
        boolean isValid(CharSequence text);
        CharSequence fixText(CharSequence invalidText);
    }
    private class PassThroughClickListener implements OnClickListener {
        private View.OnClickListener mWrapped;
        public void onClick(View v) {
            onClickImpl();
            if (mWrapped != null) mWrapped.onClick(v);
        }
    }
    private class PopupDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            if (isPopupShowing()) {
                showDropDown();
            } else if (mAdapter != null) {
                post(new Runnable() {
                    public void run() {
                        final ListAdapter adapter = mAdapter;
                        if (adapter != null) {
                            updateDropDownForFilter(adapter.getCount());
                        }
                    }
                });
            }
        }
        @Override
        public void onInvalidated() {
            if (!mDropDownAlwaysVisible) {
                dismissDropDown();
            }
        }
    }
}
