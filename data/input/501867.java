public class SearchDialog extends Dialog implements OnItemClickListener, OnItemSelectedListener {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "SearchDialog";
    private static final boolean DBG_LOG_TIMING = false;
    private static final String INSTANCE_KEY_COMPONENT = "comp";
    private static final String INSTANCE_KEY_APPDATA = "data";
    private static final String INSTANCE_KEY_STORED_APPDATA = "sData";
    private static final String INSTANCE_KEY_USER_QUERY = "uQry";
    private static final String IME_OPTION_NO_MICROPHONE = "nm";
    private static final int SEARCH_PLATE_LEFT_PADDING_GLOBAL = 12;
    private static final int SEARCH_PLATE_LEFT_PADDING_NON_GLOBAL = 7;
    private TextView mBadgeLabel;
    private ImageView mAppIcon;
    private SearchAutoComplete mSearchAutoComplete;
    private Button mGoButton;
    private ImageButton mVoiceButton;
    private View mSearchPlate;
    private Drawable mWorkingSpinner;
    private SearchableInfo mSearchable;
    private ComponentName mLaunchComponent;
    private Bundle mAppSearchData;
    private Context mActivityContext;
    private SearchManager mSearchManager;
    private final Intent mVoiceWebSearchIntent;
    private final Intent mVoiceAppSearchIntent;
    private SuggestionsAdapter mSuggestionsAdapter;
    private static final boolean REWRITE_QUERIES = true;
    private String mUserQuery;
    private String mInitialQuery;
    private final WeakHashMap<String, Drawable.ConstantState> mOutsideDrawablesCache =
            new WeakHashMap<String, Drawable.ConstantState>();
    private int mSearchAutoCompleteImeOptions;
    private BroadcastReceiver mConfChangeListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                onConfigurationChanged();
            }
        }
    };
    public SearchDialog(Context context, SearchManager searchManager) {
        super(context, com.android.internal.R.style.Theme_SearchBar);
        mVoiceWebSearchIntent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
        mVoiceWebSearchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mVoiceWebSearchIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        mVoiceAppSearchIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mVoiceAppSearchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mSearchManager = searchManager;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window theWindow = getWindow();
        WindowManager.LayoutParams lp = theWindow.getAttributes();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        theWindow.setAttributes(lp);
        setCanceledOnTouchOutside(true);        
    }
    private void createContentView() {
        setContentView(com.android.internal.R.layout.search_bar);
        SearchBar searchBar = (SearchBar) findViewById(com.android.internal.R.id.search_bar);
        searchBar.setSearchDialog(this);
        mBadgeLabel = (TextView) findViewById(com.android.internal.R.id.search_badge);
        mSearchAutoComplete = (SearchAutoComplete)
                findViewById(com.android.internal.R.id.search_src_text);
        mAppIcon = (ImageView) findViewById(com.android.internal.R.id.search_app_icon);
        mGoButton = (Button) findViewById(com.android.internal.R.id.search_go_btn);
        mVoiceButton = (ImageButton) findViewById(com.android.internal.R.id.search_voice_btn);
        mSearchPlate = findViewById(com.android.internal.R.id.search_plate);
        mWorkingSpinner = getContext().getResources().
                getDrawable(com.android.internal.R.drawable.search_spinner);
        mSearchAutoComplete.setCompoundDrawablesWithIntrinsicBounds(
                null, null, mWorkingSpinner, null);
        setWorking(false);
        mSearchAutoComplete.addTextChangedListener(mTextWatcher);
        mSearchAutoComplete.setOnKeyListener(mTextKeyListener);
        mSearchAutoComplete.setOnItemClickListener(this);
        mSearchAutoComplete.setOnItemSelectedListener(this);
        mGoButton.setOnClickListener(mGoButtonClickListener);
        mGoButton.setOnKeyListener(mButtonsKeyListener);
        mVoiceButton.setOnClickListener(mVoiceButtonClickListener);
        mVoiceButton.setOnKeyListener(mButtonsKeyListener);
        mBadgeLabel.setVisibility(View.GONE);
        mSearchAutoCompleteImeOptions = mSearchAutoComplete.getImeOptions();
    }
    public boolean show(String initialQuery, boolean selectInitialQuery,
            ComponentName componentName, Bundle appSearchData) {
        boolean success = doShow(initialQuery, selectInitialQuery, componentName, appSearchData);
        if (success) {
            mSearchAutoComplete.showDropDownAfterLayout();
        }
        return success;
    }
    private boolean doShow(String initialQuery, boolean selectInitialQuery,
            ComponentName componentName, Bundle appSearchData) {
        if (!show(componentName, appSearchData)) {
            return false;
        }
        mInitialQuery = initialQuery == null ? "" : initialQuery;
        setUserQuery(initialQuery);
        if (selectInitialQuery) {
            mSearchAutoComplete.selectAll();
        }
        return true;
    }
    private boolean show(ComponentName componentName, Bundle appSearchData) {
        if (DBG) { 
            Log.d(LOG_TAG, "show(" + componentName + ", " 
                    + appSearchData + ")");
        }
        SearchManager searchManager = (SearchManager)
                mContext.getSystemService(Context.SEARCH_SERVICE);
        mSearchable = searchManager.getSearchableInfo(componentName);
        if (mSearchable == null) {
            return false;
        }
        mLaunchComponent = componentName;
        mAppSearchData = appSearchData;
        mActivityContext = mSearchable.getActivityContext(getContext());
        if (!isShowing()) {
            createContentView();
            show();
        }
        updateUI();
        return true;
    }
    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        getContext().registerReceiver(mConfChangeListener, filter);
    }
    @Override
    public void onStop() {
        super.onStop();
        getContext().unregisterReceiver(mConfChangeListener);
        closeSuggestionsAdapter();
        mLaunchComponent = null;
        mAppSearchData = null;
        mSearchable = null;
        mUserQuery = null;
        mInitialQuery = null;
    }
    public void setWorking(boolean working) {
        mWorkingSpinner.setAlpha(working ? 255 : 0);
        mWorkingSpinner.setVisible(working, false);
        mWorkingSpinner.invalidateSelf();
    }
    private void closeSuggestionsAdapter() {
        mSearchAutoComplete.setAdapter((SuggestionsAdapter)null);
        if (mSuggestionsAdapter != null) {
            mSuggestionsAdapter.close();
        }
        mSuggestionsAdapter = null;
    }
    @Override
    public Bundle onSaveInstanceState() {
        if (!isShowing()) return null;
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_KEY_COMPONENT, mLaunchComponent);
        bundle.putBundle(INSTANCE_KEY_APPDATA, mAppSearchData);
        bundle.putString(INSTANCE_KEY_USER_QUERY, mUserQuery);
        return bundle;
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        ComponentName launchComponent = savedInstanceState.getParcelable(INSTANCE_KEY_COMPONENT);
        Bundle appSearchData = savedInstanceState.getBundle(INSTANCE_KEY_APPDATA);
        String userQuery = savedInstanceState.getString(INSTANCE_KEY_USER_QUERY);
        if (!doShow(userQuery, false, launchComponent, appSearchData)) {
            return;
        }
    }
    public void onConfigurationChanged() {
        if (mSearchable != null && isShowing()) {
            updateSearchButton();
            updateSearchAppIcon();
            updateSearchBadge();
            updateQueryHint();
            if (isLandscapeMode(getContext())) {
                mSearchAutoComplete.ensureImeVisible(true);
            }
            mSearchAutoComplete.showDropDownAfterLayout();
        }
    }
    static boolean isLandscapeMode(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }
    private void updateUI() {
        if (mSearchable != null) {
            mDecor.setVisibility(View.VISIBLE);
            updateSearchAutoComplete();
            updateSearchButton();
            updateSearchAppIcon();
            updateSearchBadge();
            updateQueryHint();
            updateVoiceButton(TextUtils.isEmpty(mUserQuery));
            int inputType = mSearchable.getInputType();
            if ((inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT) {
                inputType &= ~InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE;
                if (mSearchable.getSuggestAuthority() != null) {
                    inputType |= InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE;
                }
            }
            mSearchAutoComplete.setInputType(inputType);
            mSearchAutoCompleteImeOptions = mSearchable.getImeOptions();
            mSearchAutoComplete.setImeOptions(mSearchAutoCompleteImeOptions);
            if (mSearchable.getVoiceSearchEnabled()) {
                mSearchAutoComplete.setPrivateImeOptions(IME_OPTION_NO_MICROPHONE);
            } else {
                mSearchAutoComplete.setPrivateImeOptions(null);
            }
        }
    }
    private void updateSearchAutoComplete() {
        closeSuggestionsAdapter();
        mSearchAutoComplete.setDropDownAnimationStyle(0); 
        mSearchAutoComplete.setThreshold(mSearchable.getSuggestThreshold());
        mSearchAutoComplete.setDropDownDismissedOnCompletion(false);
        mSearchAutoComplete.setForceIgnoreOutsideTouch(true);
        if (mSearchable.getSuggestAuthority() != null) {
            mSuggestionsAdapter = new SuggestionsAdapter(getContext(), this, mSearchable, 
                    mOutsideDrawablesCache);
            mSearchAutoComplete.setAdapter(mSuggestionsAdapter);
        }
    }
    private void updateSearchButton() {
        String textLabel = null;
        Drawable iconLabel = null;
        int textId = mSearchable.getSearchButtonText(); 
        if (isBrowserSearch()){
            iconLabel = getContext().getResources()
                    .getDrawable(com.android.internal.R.drawable.ic_btn_search_go);
        } else if (textId != 0) {
            textLabel = mActivityContext.getResources().getString(textId);  
        } else {
            iconLabel = getContext().getResources().
                    getDrawable(com.android.internal.R.drawable.ic_btn_search);
        }
        mGoButton.setText(textLabel);
        mGoButton.setCompoundDrawablesWithIntrinsicBounds(iconLabel, null, null, null);
    }
    private void updateSearchAppIcon() {
        if (isBrowserSearch()) {
            mAppIcon.setImageResource(0);
            mAppIcon.setVisibility(View.GONE);
            mSearchPlate.setPadding(SEARCH_PLATE_LEFT_PADDING_GLOBAL,
                    mSearchPlate.getPaddingTop(),
                    mSearchPlate.getPaddingRight(),
                    mSearchPlate.getPaddingBottom());
        } else {
            PackageManager pm = getContext().getPackageManager();
            Drawable icon;
            try {
                ActivityInfo info = pm.getActivityInfo(mLaunchComponent, 0);
                icon = pm.getApplicationIcon(info.applicationInfo);
                if (DBG) Log.d(LOG_TAG, "Using app-specific icon");
            } catch (NameNotFoundException e) {
                icon = pm.getDefaultActivityIcon();
                Log.w(LOG_TAG, mLaunchComponent + " not found, using generic app icon");
            }
            mAppIcon.setImageDrawable(icon);
            mAppIcon.setVisibility(View.VISIBLE);
            mSearchPlate.setPadding(SEARCH_PLATE_LEFT_PADDING_NON_GLOBAL,
                    mSearchPlate.getPaddingTop(),
                    mSearchPlate.getPaddingRight(),
                    mSearchPlate.getPaddingBottom());
        }
    }
    private void updateSearchBadge() {
        int visibility = View.GONE;
        Drawable icon = null;
        CharSequence text = null;
        if (mSearchable.useBadgeIcon()) {
            icon = mActivityContext.getResources().getDrawable(mSearchable.getIconId());
            visibility = View.VISIBLE;
            if (DBG) Log.d(LOG_TAG, "Using badge icon: " + mSearchable.getIconId());
        } else if (mSearchable.useBadgeLabel()) {
            text = mActivityContext.getResources().getText(mSearchable.getLabelId()).toString();
            visibility = View.VISIBLE;
            if (DBG) Log.d(LOG_TAG, "Using badge label: " + mSearchable.getLabelId());
        }
        mBadgeLabel.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        mBadgeLabel.setText(text);
        mBadgeLabel.setVisibility(visibility);
    }
    private void updateQueryHint() {
        if (isShowing()) {
            String hint = null;
            if (mSearchable != null) {
                int hintId = mSearchable.getHintId();
                if (hintId != 0) {
                    hint = mActivityContext.getString(hintId);
                }
            }
            mSearchAutoComplete.setHint(hint);
        }
    }
    private void updateVoiceButton(boolean empty) {
        int visibility = View.GONE;
        if ((mAppSearchData == null || !mAppSearchData.getBoolean(
                SearchManager.DISABLE_VOICE_SEARCH, false))
                && mSearchable.getVoiceSearchEnabled() && empty) {
            Intent testIntent = null;
            if (mSearchable.getVoiceSearchLaunchWebSearch()) {
                testIntent = mVoiceWebSearchIntent;
            } else if (mSearchable.getVoiceSearchLaunchRecognizer()) {
                testIntent = mVoiceAppSearchIntent;
            }      
            if (testIntent != null) {
                ResolveInfo ri = getContext().getPackageManager().
                        resolveActivity(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
                if (ri != null) {
                    visibility = View.VISIBLE;
                }
            }
        }
        mVoiceButton.setVisibility(visibility);
    }
    void onDataSetChanged() {
        if (mSearchAutoComplete != null && mSuggestionsAdapter != null) {
            mSearchAutoComplete.onFilterComplete(mSuggestionsAdapter.getCount());
        }
    }
    private boolean isBrowserSearch() {
        return mLaunchComponent.flattenToShortString().startsWith("com.android.browser/");
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mSearchAutoComplete.isPopupShowing() && isOutOfBounds(mSearchPlate, event)) {
            if (DBG) Log.d(LOG_TAG, "Pop-up not showing and outside of search plate.");
            cancel();
            return true;
        }
        return super.onTouchEvent(event);
    }
    private boolean isOutOfBounds(View v, MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int slop = ViewConfiguration.get(mContext).getScaledWindowTouchSlop();
        return (x < -slop) || (y < -slop)
                || (x > (v.getWidth()+slop))
                || (y > (v.getHeight()+slop));
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (DBG) Log.d(LOG_TAG, "onKeyDown(" + keyCode + "," + event + ")");
        if (mSearchable == null) {
            return false;
        }
        SearchableInfo.ActionKeyInfo actionKey = mSearchable.findActionKey(keyCode);
        if ((actionKey != null) && (actionKey.getQueryActionMsg() != null)) {
            launchQuerySearch(keyCode, actionKey.getQueryActionMsg());
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int before, int after) { }
        public void onTextChanged(CharSequence s, int start,
                int before, int after) {
            if (DBG_LOG_TIMING) {
                dbgLogTiming("onTextChanged()");
            }
            if (mSearchable == null) {
                return;
            }
            if (!mSearchAutoComplete.isPerformingCompletion()) {
                mUserQuery = s == null ? "" : s.toString();
            }
            updateWidgetState();
            updateVoiceButton(mSearchAutoComplete.isEmpty()
                    || (isBrowserSearch() && mInitialQuery.equals(mUserQuery))
                    || (mAppSearchData != null && mAppSearchData.getBoolean(
                    SearchManager.CONTEXT_IS_VOICE)));
        }
        public void afterTextChanged(Editable s) {
            if (mSearchable == null) {
                return;
            }
            if (mSearchable.autoUrlDetect() && !mSearchAutoComplete.isPerformingCompletion()) {
                int options = (mSearchAutoComplete.getImeOptions() & (~EditorInfo.IME_MASK_ACTION))
                        | EditorInfo.IME_ACTION_GO;
                if (options != mSearchAutoCompleteImeOptions) {
                    mSearchAutoCompleteImeOptions = options;
                    mSearchAutoComplete.setImeOptions(options);
                    mSearchAutoComplete.setInputType(mSearchAutoComplete.getInputType());
                }
            }
        }
    };
    private void updateWidgetState() {
        boolean enabled = !mSearchAutoComplete.isEmpty();
        if (isBrowserSearch()) {
            if (enabled && !mInitialQuery.equals(mUserQuery)) {
                mSearchAutoComplete.setBackgroundResource(
                        com.android.internal.R.drawable.textfield_search);
                mGoButton.setVisibility(View.VISIBLE);
                mGoButton.setEnabled(true);
                mGoButton.setFocusable(true);
            } else {
                mSearchAutoComplete.setBackgroundResource(
                        com.android.internal.R.drawable.textfield_search_empty);
                mGoButton.setVisibility(View.GONE);
            }
        } else {
            mGoButton.setEnabled(enabled);
            mGoButton.setFocusable(enabled);
        }
    }
    View.OnKeyListener mButtonsKeyListener = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (mSearchable == null) {
                return false;
            }
            if (!event.isSystem() && 
                    (keyCode != KeyEvent.KEYCODE_DPAD_UP) &&
                    (keyCode != KeyEvent.KEYCODE_DPAD_LEFT) &&
                    (keyCode != KeyEvent.KEYCODE_DPAD_RIGHT) &&
                    (keyCode != KeyEvent.KEYCODE_DPAD_CENTER)) {
                if (mSearchAutoComplete.requestFocus()) {
                    return mSearchAutoComplete.dispatchKeyEvent(event);
                }
            }
            return false;
        }
    };
    View.OnClickListener mGoButtonClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mSearchable == null) {
                return;
            }
            launchQuerySearch();
        }
    };
    View.OnClickListener mVoiceButtonClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mSearchable == null) {
                return;
            }
            SearchableInfo searchable = mSearchable;
            try {
                if (searchable.getVoiceSearchLaunchWebSearch()) {
                    Intent webSearchIntent = createVoiceWebSearchIntent(mVoiceWebSearchIntent,
                            searchable);
                    getContext().startActivity(webSearchIntent);
                } else if (searchable.getVoiceSearchLaunchRecognizer()) {
                    Intent appSearchIntent = createVoiceAppSearchIntent(mVoiceAppSearchIntent,
                            searchable);
                    getContext().startActivity(appSearchIntent);
                }
            } catch (ActivityNotFoundException e) {
                Log.w(LOG_TAG, "Could not find voice search activity");
            }
            dismiss();
         }
    };
    private Intent createVoiceWebSearchIntent(Intent baseIntent, SearchableInfo searchable) {
        Intent voiceIntent = new Intent(baseIntent);
        ComponentName searchActivity = searchable.getSearchActivity();
        voiceIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                searchActivity == null ? null : searchActivity.flattenToShortString());
        return voiceIntent;
    }
    private Intent createVoiceAppSearchIntent(Intent baseIntent, SearchableInfo searchable) {
        ComponentName searchActivity = searchable.getSearchActivity();
        Intent queryIntent = new Intent(Intent.ACTION_SEARCH);
        queryIntent.setComponent(searchActivity);
        PendingIntent pending = PendingIntent.getActivity(
                getContext(), 0, queryIntent, PendingIntent.FLAG_ONE_SHOT);
        Bundle queryExtras = new Bundle();
        if (mAppSearchData != null) {
            queryExtras.putBundle(SearchManager.APP_DATA, mAppSearchData);
        }
        Intent voiceIntent = new Intent(baseIntent);
        String languageModel = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
        String prompt = null;
        String language = null;
        int maxResults = 1;
        Resources resources = mActivityContext.getResources();
        if (searchable.getVoiceLanguageModeId() != 0) {
            languageModel = resources.getString(searchable.getVoiceLanguageModeId());
        }
        if (searchable.getVoicePromptTextId() != 0) {
            prompt = resources.getString(searchable.getVoicePromptTextId());
        }
        if (searchable.getVoiceLanguageId() != 0) {
            language = resources.getString(searchable.getVoiceLanguageId());
        }
        if (searchable.getVoiceMaxResults() != 0) {
            maxResults = searchable.getVoiceMaxResults();
        }
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxResults);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                searchActivity == null ? null : searchActivity.flattenToShortString());
        voiceIntent.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT, pending);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE, queryExtras);
        return voiceIntent;
    }
    private String fixUrl(String inUrl) {
        if (inUrl.startsWith("http:
            return inUrl;
        if (inUrl.startsWith("http:") || inUrl.startsWith("https:")) {
            if (inUrl.startsWith("http:/") || inUrl.startsWith("https:/")) {
                inUrl = inUrl.replaceFirst("/", "
            } else {
                inUrl = inUrl.replaceFirst(":", ":
            }
        }
        if (inUrl.indexOf(":
            inUrl = "http:
        }
        return inUrl;
    }
    View.OnKeyListener mTextKeyListener = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (mSearchable == null) {
                return false;
            }
            if (DBG_LOG_TIMING) dbgLogTiming("doTextKey()");
            if (DBG) { 
                Log.d(LOG_TAG, "mTextListener.onKey(" + keyCode + "," + event 
                        + "), selection: " + mSearchAutoComplete.getListSelection());
            }
            if (mSearchAutoComplete.isPopupShowing() && 
                    mSearchAutoComplete.getListSelection() != ListView.INVALID_POSITION) {
                return onSuggestionsKey(v, keyCode, event);
            }
            if (!mSearchAutoComplete.isEmpty()) {
                if (keyCode == KeyEvent.KEYCODE_ENTER 
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    v.cancelLongPress();
                    if (mSearchable.autoUrlDetect() &&
                        (mSearchAutoCompleteImeOptions & EditorInfo.IME_MASK_ACTION)
                                == EditorInfo.IME_ACTION_GO) {
                        Uri uri = Uri.parse(fixUrl(mSearchAutoComplete.getText().toString()));
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchIntent(intent);
                    } else {
                        launchQuerySearch();
                    }
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    SearchableInfo.ActionKeyInfo actionKey = mSearchable.findActionKey(keyCode);
                    if ((actionKey != null) && (actionKey.getQueryActionMsg() != null)) {
                        launchQuerySearch(keyCode, actionKey.getQueryActionMsg());
                        return true;
                    }
                }
            }
            return false;
        }
    };
    @Override
    public void hide() {
        if (!isShowing()) return;
        InputMethodManager imm = (InputMethodManager)getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(
                    getWindow().getDecorView().getWindowToken(), 0);
        }
        super.hide();
    }
    private boolean onSuggestionsKey(View v, int keyCode, KeyEvent event) {
        if (mSearchable == null) {
            return false;
        }
        if (mSuggestionsAdapter == null) {
            return false;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (DBG_LOG_TIMING) {
                dbgLogTiming("onSuggestionsKey()");
            }
            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEARCH) {
                int position = mSearchAutoComplete.getListSelection();
                return launchSuggestion(position);
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                int selPoint = (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) ? 
                        0 : mSearchAutoComplete.length();
                mSearchAutoComplete.setSelection(selPoint);
                mSearchAutoComplete.setListSelection(0);
                mSearchAutoComplete.clearListSelection();
                mSearchAutoComplete.ensureImeVisible(true);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP 
                    && 0 == mSearchAutoComplete.getListSelection()) {
                restoreUserQuery();
                return false;
            }
            SearchableInfo.ActionKeyInfo actionKey = mSearchable.findActionKey(keyCode);
            if ((actionKey != null) && 
                    ((actionKey.getSuggestActionMsg() != null) || 
                     (actionKey.getSuggestActionMsgColumn() != null))) {
                int position = mSearchAutoComplete.getListSelection();
                if (position != ListView.INVALID_POSITION) {
                    Cursor c = mSuggestionsAdapter.getCursor();
                    if (c.moveToPosition(position)) {
                        final String actionMsg = getActionKeyMessage(c, actionKey);
                        if (actionMsg != null && (actionMsg.length() > 0)) {
                            return launchSuggestion(position, keyCode, actionMsg);
                        }
                    }
                }
            }
        }
        return false;
    }
    public void launchQuerySearch()  {
        launchQuerySearch(KeyEvent.KEYCODE_UNKNOWN, null);
    }
    protected void launchQuerySearch(int actionKey, String actionMsg)  {
        String query = mSearchAutoComplete.getText().toString();
        String action = Intent.ACTION_SEARCH;
        Intent intent = createIntent(action, null, null, query, null,
                actionKey, actionMsg);
        launchIntent(intent);
    }
    protected boolean launchSuggestion(int position) {
        return launchSuggestion(position, KeyEvent.KEYCODE_UNKNOWN, null);
    }
    protected boolean launchSuggestion(int position, int actionKey, String actionMsg) {
        Cursor c = mSuggestionsAdapter.getCursor();
        if ((c != null) && c.moveToPosition(position)) {
            Intent intent = createIntentFromSuggestion(c, actionKey, actionMsg);
            launchIntent(intent);
            return true;
        }
        return false;
    }
    private void launchIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        Log.d(LOG_TAG, "launching " + intent);
        try {
            Log.i(LOG_TAG, "Starting (as ourselves) " + intent.toURI());
            getContext().startActivity(intent);
            dismiss();
        } catch (RuntimeException ex) {
            Log.e(LOG_TAG, "Failed launch activity: " + intent, ex);
        }
    }
    private void setBrowserApplicationId(Intent intent) {
        Uri data = intent.getData();
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && data != null) {
            String scheme = data.getScheme();
            if (scheme != null && scheme.startsWith("http")) {
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, data.toString());
            }
        }
    }
    public void setListSelection(int index) {
        mSearchAutoComplete.setListSelection(index);
    }
    private Intent createIntentFromSuggestion(Cursor c, int actionKey, String actionMsg) {
        try {
            String action = getColumnString(c, SearchManager.SUGGEST_COLUMN_INTENT_ACTION);
            if (SearchManager.INTENT_ACTION_NONE.equals(action)) {
                return null;
            }
            if (action == null) {
                action = mSearchable.getSuggestIntentAction();
            }
            if (action == null) {
                action = Intent.ACTION_SEARCH;
            }
            String data = getColumnString(c, SearchManager.SUGGEST_COLUMN_INTENT_DATA);
            if (data == null) {
                data = mSearchable.getSuggestIntentData();
            }
            if (data != null) {
                String id = getColumnString(c, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
                if (id != null) {
                    data = data + "/" + Uri.encode(id);
                }
            }
            Uri dataUri = (data == null) ? null : Uri.parse(data);
            String componentName = getColumnString(
                    c, SearchManager.SUGGEST_COLUMN_INTENT_COMPONENT_NAME);
            String query = getColumnString(c, SearchManager.SUGGEST_COLUMN_QUERY);
            String extraData = getColumnString(c, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
            return createIntent(action, dataUri, extraData, query, componentName, actionKey,
                    actionMsg);
        } catch (RuntimeException e ) {
            int rowNum;
            try {                       
                rowNum = c.getPosition();
            } catch (RuntimeException e2 ) {
                rowNum = -1;
            }
            Log.w(LOG_TAG, "Search Suggestions cursor at row " + rowNum + 
                            " returned exception" + e.toString());
            return null;
        }
    }
    private Intent createIntent(String action, Uri data, String extraData, String query,
            String componentName, int actionKey, String actionMsg) {
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (data != null) {
            intent.setData(data);
        }
        intent.putExtra(SearchManager.USER_QUERY, mUserQuery);
        if (query != null) {
            intent.putExtra(SearchManager.QUERY, query);
        }
        if (extraData != null) {
            intent.putExtra(SearchManager.EXTRA_DATA_KEY, extraData);
        }
        if (mAppSearchData != null) {
            intent.putExtra(SearchManager.APP_DATA, mAppSearchData);
        }
        if (actionKey != KeyEvent.KEYCODE_UNKNOWN) {
            intent.putExtra(SearchManager.ACTION_KEY, actionKey);
            intent.putExtra(SearchManager.ACTION_MSG, actionMsg);
        }
        intent.setComponent(mSearchable.getSearchActivity());
        return intent;
    }
    private static String getActionKeyMessage(Cursor c, SearchableInfo.ActionKeyInfo actionKey) {
        String result = null;
        final String column = actionKey.getSuggestActionMsgColumn();
        if (column != null) {
            result = SuggestionsAdapter.getColumnString(c, column);
        }
        if (result == null) {
            result = actionKey.getSuggestActionMsg();
        }
        return result;
    }
    public static class SearchBar extends LinearLayout {
        private SearchDialog mSearchDialog;
        public SearchBar(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        public SearchBar(Context context) {
            super(context);
        }
        public void setSearchDialog(SearchDialog searchDialog) {
            mSearchDialog = searchDialog;
        }
        @Override
        public boolean dispatchKeyEventPreIme(KeyEvent event) {
            if (DBG) Log.d(LOG_TAG, "onKeyPreIme(" + event + ")");
            if (mSearchDialog != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN
                            && event.getRepeatCount() == 0) {
                        state.startTracking(event, this);
                        return true;
                    } else if (event.getAction() == KeyEvent.ACTION_UP
                            && !event.isCanceled() && state.isTracking(event)) {
                        mSearchDialog.onBackPressed();
                        return true;
                    }
                }
            }
            return super.dispatchKeyEventPreIme(event);
        }
    }
    public static class SearchAutoComplete extends AutoCompleteTextView {
        private int mThreshold;
        public SearchAutoComplete(Context context) {
            super(context);
            mThreshold = getThreshold();
        }
        public SearchAutoComplete(Context context, AttributeSet attrs) {
            super(context, attrs);
            mThreshold = getThreshold();
        }
        public SearchAutoComplete(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            mThreshold = getThreshold();
        }
        @Override
        public void setThreshold(int threshold) {
            super.setThreshold(threshold);
            mThreshold = threshold;
        }
        private boolean isEmpty() {
            return TextUtils.getTrimmedLength(getText()) == 0;
        }
        @Override
        protected void replaceText(CharSequence text) {
        }
        @Override
        public void performCompletion() {
        }
        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);
            if (hasWindowFocus) {
                InputMethodManager inputManager = (InputMethodManager)
                        getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(this, 0);
                if (isLandscapeMode(getContext())) {
                    ensureImeVisible(true);
                }
            }
        }
        @Override
        public boolean enoughToFilter() {
            return mThreshold <= 0 || super.enoughToFilter();
        }
    }
    @Override
    public void onBackPressed() {
        InputMethodManager imm = (InputMethodManager)getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isFullscreenMode() &&
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0)) {
            return;
        }
        cancel();
    }
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (DBG) Log.d(LOG_TAG, "onItemClick() position " + position);
        launchSuggestion(position);
    }
     public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         if (DBG) Log.d(LOG_TAG, "onItemSelected() position " + position);
         if (REWRITE_QUERIES) {
             rewriteQueryFromSuggestion(position);
         }
     }
     public void onNothingSelected(AdapterView<?> parent) {
         if (DBG) Log.d(LOG_TAG, "onNothingSelected()");
     }
     private void rewriteQueryFromSuggestion(int position) {
         Cursor c = mSuggestionsAdapter.getCursor();
         if (c == null) {
             return;
         }
         if (c.moveToPosition(position)) {
             CharSequence newQuery = mSuggestionsAdapter.convertToString(c);
             if (newQuery != null) {
                 if (DBG) Log.d(LOG_TAG, "Rewriting query to '" + newQuery + "'");
                 setQuery(newQuery);
             } else {
                 if (DBG) Log.d(LOG_TAG, "Suggestion gives no rewrite, restoring user query.");
                 restoreUserQuery();
             }
         } else {
             Log.w(LOG_TAG, "Bad suggestion position: " + position);
             restoreUserQuery();
         }
     }
     private void restoreUserQuery() {
         if (DBG) Log.d(LOG_TAG, "Restoring query to '" + mUserQuery + "'");
         setQuery(mUserQuery);
     }
     private void setQuery(CharSequence query) {
         mSearchAutoComplete.setText(query, false);
         if (query != null) {
             mSearchAutoComplete.setSelection(query.length());
         }
     }
     private void setUserQuery(String query) {
         if (query == null) {
             query = "";
         }
         mUserQuery = query;
         mSearchAutoComplete.setText(query);
         mSearchAutoComplete.setSelection(query.length());
     }
    private AtomicLong mLastLogTime = new AtomicLong(SystemClock.uptimeMillis());
    private void dbgLogTiming(final String caller) {
        long millis = SystemClock.uptimeMillis();
        long oldTime = mLastLogTime.getAndSet(millis);
        long delta = millis - oldTime;
        final String report = millis + " (+" + delta + ") ticks for Search keystroke in " + caller;
        Log.d(LOG_TAG,report);
    }
}
