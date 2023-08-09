public abstract class UserDictionaryToolsList extends Activity
    implements View.OnClickListener, OnTouchListener, OnFocusChangeListener {
    protected String  mListViewName;
    protected String  mEditViewName;
    protected String  mPackageName;
    private final int MENU_ITEM_ADD = 0;
    private final int MENU_ITEM_EDIT = 1;
    private final int MENU_ITEM_DELETE = 2;
    private final int MENU_ITEM_INIT = 3;
    private final int DIALOG_CONTROL_DELETE_CONFIRM = 0;
    private final int DIALOG_CONTROL_INIT_CONFIRM = 1;
    private final int WORD_TEXT_SIZE = 16;
    private final int UNFOCUS_BACKGROUND_COLOR = 0xFF242424;
    private final int FOCUS_BACKGROUND_COLOR = 0xFFFF8500;
    private final int MIN_WORD_COUNT = 0;
    private final int MAX_WORD_COUNT = 100;
    private final int MAX_LIST_WORD_COUNT = 100;
    private final int DOUBLE_TAP_TIME = 300;
    private Menu mMenu;
    private TableLayout mTableLayout;
    private static View sFocusingView = null;
    private static View sFocusingPairView = null;
    private Intent mIntent;
    private int mWordCount = 0;
    private boolean mAddMenuEnabled;
    private boolean mEditMenuEnabled;
    private boolean mDeleteMenuEnabled;
    private boolean mInitMenuEnabled;
    private boolean mInitializedMenu = false;
    private boolean mSelectedWords;
    private int mSelectedViewID = -1;
    private static int sBeforeSelectedViewID = -1;
    private static long sJustBeforeActionTime = -1;
    private ArrayList<WnnWord> mWordList = null;
    private WnnWord[] mSortData;
    private boolean mInit = false;
    private Button mLeftButton = null;
    private Button mRightButton = null;
    protected abstract boolean sendEventToIME(OpenWnnEvent ev);
    protected abstract Comparator<WnnWord> getComparator();
    protected abstract void  headerCreate();
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.user_dictionary_tools_list);
        mTableLayout = (TableLayout)findViewById(R.id.user_dictionary_tools_table);
        Button b = (Button)findViewById(R.id.user_dictionary_left_button);
        b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int pos = mWordCount - MAX_LIST_WORD_COUNT;
                    if (0 <= pos) {
                        mWordCount = pos;
                        updateWordList();
                        mTableLayout.findViewById(1).requestFocus();
                    }
                }
            });
        mLeftButton = b;
        b = (Button)findViewById(R.id.user_dictionary_right_button);
        b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int pos = mWordCount + MAX_LIST_WORD_COUNT;
                    if (pos < mWordList.size()) {
                        mWordCount = pos;
                        updateWordList();
                        mTableLayout.findViewById(1).requestFocus();
                    }
                }
            });
        mRightButton = b;
    }
    @Override protected void onStart() {
        super.onStart();
        sBeforeSelectedViewID = -1;
        sJustBeforeActionTime = -1;
        mWordList = getWords();
        headerCreate();
        final TextView leftText = (TextView) findViewById(R.id.user_dictionary_tools_list_title_words_count);
        leftText.setText(mWordList.size() + "/" + MAX_WORD_COUNT);
        updateWordList();
    }
    private TableLayout.LayoutParams tableCreateParam(int w, int h) {
        return new TableLayout.LayoutParams(w, h);
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        setOptionsMenuEnabled();
        menu.add(0, MENU_ITEM_ADD, 0, R.string.user_dictionary_add)
            .setIcon(android.R.drawable.ic_menu_add)
            .setEnabled(mAddMenuEnabled);
        menu.add(0, MENU_ITEM_EDIT, 0, R.string.user_dictionary_edit)
            .setIcon(android.R.drawable.ic_menu_edit)
            .setEnabled(mEditMenuEnabled);
        menu.add(0, MENU_ITEM_DELETE, 0, R.string.user_dictionary_delete)
            .setIcon(android.R.drawable.ic_menu_delete)
            .setEnabled(mDeleteMenuEnabled);
        menu.add(1, MENU_ITEM_INIT, 0, R.string.user_dictionary_init)
            .setIcon(android.R.drawable.ic_menu_delete)
            .setEnabled(mInitMenuEnabled);
        mMenu = menu;
        mInitializedMenu = true;
        return super.onCreateOptionsMenu(menu);
    }
    private void setOptionsMenuEnabled() {
        if (mWordList.size() >= MAX_WORD_COUNT) {
            mAddMenuEnabled = false;
        } else {
            mAddMenuEnabled = true;
        }
        if (mWordList.size() <= MIN_WORD_COUNT) {
            mEditMenuEnabled = false;
            mDeleteMenuEnabled = false;
        } else {
            mEditMenuEnabled = true;
            if (mSelectedWords) {
                mDeleteMenuEnabled = true;
            } else {
                mDeleteMenuEnabled = false;
            }
        }
        mInitMenuEnabled = true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret;
        switch (item.getItemId()) {
        case MENU_ITEM_ADD:
            wordAdd();
            ret = true;
            break;
        case MENU_ITEM_EDIT:
            wordEdit(sFocusingView, sFocusingPairView);
            ret = true;
            break;
        case MENU_ITEM_DELETE:
            showDialog(DIALOG_CONTROL_DELETE_CONFIRM);
            ret = true;
            break;
        case MENU_ITEM_INIT:
            showDialog(DIALOG_CONTROL_INIT_CONFIRM);
            ret = true;
            break;
        default:
            ret = false;
        }
        return ret;
    }
    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
            return true;
        }
         return super.onKeyUp(keyCode, event);
    }
    @Override protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_CONTROL_DELETE_CONFIRM:
            return new AlertDialog.Builder(UserDictionaryToolsList.this)
                .setMessage(R.string.user_dictionary_delete_confirm)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, mDialogDeleteWords)
                .setCancelable(true)
                .create();
        case DIALOG_CONTROL_INIT_CONFIRM:
            return new AlertDialog.Builder(UserDictionaryToolsList.this)
                .setMessage(R.string.dialog_clear_user_dictionary_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, mDialogInitWords)
                .setCancelable(true)
                .create();
        default:
            Log.e("OpenWnn", "onCreateDialog : Invaled Get DialogID. ID=" + id);
            break;
        }
        return super.onCreateDialog(id);
    }
    private DialogInterface.OnClickListener mDialogDeleteWords =
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {
                CharSequence focusString = ((TextView)sFocusingView).getText();
                CharSequence focusPairString = ((TextView)sFocusingPairView).getText();
                WnnWord wnnWordSearch = new WnnWord();
                if (mSelectedViewID > MAX_WORD_COUNT) {
                    wnnWordSearch.stroke = focusPairString.toString();
                    wnnWordSearch.candidate = focusString.toString();
                } else {
                    wnnWordSearch.stroke = focusString.toString();
                    wnnWordSearch.candidate = focusPairString.toString();
                }
                boolean deleted = deleteWord(wnnWordSearch);
                if (deleted) {
                    Toast.makeText(getApplicationContext(),
                                   R.string.user_dictionary_delete_complete,
                                   Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                                   R.string.user_dictionary_delete_fail,
                                   Toast.LENGTH_LONG).show();
                    return;
                }
                mWordList = getWords();
                int size = mWordList.size();
                if (size <= mWordCount) {
                    int newPos = (mWordCount - MAX_LIST_WORD_COUNT);
                    mWordCount = (0 <= newPos) ? newPos : 0;
                }
                updateWordList();
                TextView leftText = (TextView) findViewById(R.id.user_dictionary_tools_list_title_words_count);
                leftText.setText(size + "/" + MAX_WORD_COUNT);
                if (mInitializedMenu) {
                    onCreateOptionsMenu(mMenu);
                }
            }
        };
    private DialogInterface.OnClickListener mDialogInitWords =
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {
                OpenWnnEvent ev = new OpenWnnEvent(OpenWnnEvent.INITIALIZE_USER_DICTIONARY, new WnnWord());
                sendEventToIME(ev);
                Toast.makeText(getApplicationContext(), R.string.dialog_clear_user_dictionary_done,
                               Toast.LENGTH_LONG).show();
                mWordList = new ArrayList<WnnWord>();
                mWordCount = 0;
                updateWordList();
                TextView leftText = (TextView) findViewById(R.id.user_dictionary_tools_list_title_words_count);
                leftText.setText(mWordList.size() + "/" + MAX_WORD_COUNT);
                if (mInitializedMenu) {
                    onCreateOptionsMenu(mMenu);
                }
            }
        };
    public void onClick(View arg0) {
    }
    public boolean onTouch(View v, MotionEvent e) {
        mSelectedViewID = ((TextView)v).getId();
        switch (e.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if (sBeforeSelectedViewID != ((TextView)v).getId()) {
                sBeforeSelectedViewID = ((TextView)v).getId();
            } else {
                if ((e.getDownTime() - sJustBeforeActionTime) < DOUBLE_TAP_TIME) {
                    sFocusingView = v;
                    sFocusingPairView = ((UserDictionaryToolsListFocus)v).getPairView();
                    wordEdit(sFocusingView, sFocusingPairView);
                }
            }
            sJustBeforeActionTime = e.getDownTime();
            break;
        }
        return false;
    }
    public void onFocusChange(View v, boolean hasFocus) {
        mSelectedViewID = ((TextView)v).getId();
        sFocusingView = v;
        sFocusingPairView = ((UserDictionaryToolsListFocus)v).getPairView();
        if (hasFocus) {
            ((TextView)v).setTextColor(Color.BLACK);
            v.setBackgroundColor(FOCUS_BACKGROUND_COLOR);
            ((TextView)sFocusingPairView).setTextColor(Color.BLACK);
            sFocusingPairView.setBackgroundColor(FOCUS_BACKGROUND_COLOR);
            mSelectedWords = true;
        } else {
            mSelectedWords = false;
            ((TextView)v).setTextColor(Color.LTGRAY);
            v.setBackgroundColor(UNFOCUS_BACKGROUND_COLOR);
            ((TextView)sFocusingPairView).setTextColor(Color.LTGRAY);
            sFocusingPairView.setBackgroundColor(UNFOCUS_BACKGROUND_COLOR);
        }
        if (mInitializedMenu) {
            onCreateOptionsMenu(mMenu);
        }
    }
    public void wordAdd() {
        screenTransition(Intent.ACTION_INSERT, mEditViewName);
    }
    public void wordEdit(View focusView, View focusPairView) {
        if (mSelectedViewID > MAX_WORD_COUNT) {
            createUserDictionaryToolsEdit(focusPairView, focusView);
        } else {
            createUserDictionaryToolsEdit(focusView, focusPairView);
        }
        screenTransition(Intent.ACTION_EDIT, mEditViewName);
    }
    protected abstract UserDictionaryToolsEdit createUserDictionaryToolsEdit(View focusView, View focusPairView);
    public boolean deleteWord(WnnWord searchword) {
        OpenWnnEvent event = new OpenWnnEvent(OpenWnnEvent.LIST_WORDS_IN_USER_DICTIONARY,
                                              WnnEngine.DICTIONARY_TYPE_USER,
                                              searchword);
        boolean deleted = false;
        sendEventToIME(event);
        for( int i=0; i < MAX_WORD_COUNT; i++) {
            WnnWord getword = new WnnWord();
            event = new OpenWnnEvent(OpenWnnEvent.GET_WORD,
                                     getword);
            sendEventToIME(event);
            getword = event.word;
            int len = getword.candidate.length();
            if (len == 0) {
                break;
            }
            if (searchword.candidate.equals(getword.candidate)) {
                WnnWord delword = new WnnWord();
                delword.stroke = searchword.stroke;
                delword.candidate = searchword.candidate;
                delword.id = i;
                event = new OpenWnnEvent(OpenWnnEvent.DELETE_WORD,
                                         delword);
                deleted = sendEventToIME(event);
                break;
            }
        }
        if (mInitializedMenu) {
            onCreateOptionsMenu(mMenu);
        }
        return deleted;
    }
    private void screenTransition(String action, String classname) {
        if (action.equals("")) {
            mIntent = new Intent();
        } else {
            mIntent = new Intent(action);
        }
        mIntent.setClassName(mPackageName, classname);
        startActivity(mIntent);
        finish();
    }
    private ArrayList<WnnWord> getWords() {
        WnnWord word = new WnnWord();
        OpenWnnEvent event = new OpenWnnEvent(OpenWnnEvent.LIST_WORDS_IN_USER_DICTIONARY,
                                              WnnEngine.DICTIONARY_TYPE_USER,
                                              word);
        sendEventToIME(event);
        ArrayList<WnnWord> list = new ArrayList<WnnWord>();
        for (int i = 0; i < MAX_WORD_COUNT; i++) {
            event = new OpenWnnEvent(OpenWnnEvent.GET_WORD, word);
            if (!sendEventToIME(event)) {
                break;
            }
            list.add(event.word);
        }
        compareTo(list);
        return list;
    }
    protected void compareTo(ArrayList<WnnWord> array) {
        mSortData = new WnnWord[array.size()];
        array.toArray(mSortData);
        Arrays.sort(mSortData, getComparator());   
    }
    private void updateWordList() {
        if (!mInit) {
            mInit = true;
            mSelectedViewID = 1;
            Window window = getWindow();
            WindowManager windowManager = window.getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            int system_width = display.getWidth();
            for (int i = 1; i <= MAX_LIST_WORD_COUNT; i++) {
                TableRow row = new TableRow(this);
                UserDictionaryToolsListFocus stroke = new UserDictionaryToolsListFocus(this);
                stroke.setId(i);
                stroke.setWidth(system_width/2);
                stroke.setTextSize(WORD_TEXT_SIZE);
                stroke.setTextColor(Color.LTGRAY);
                stroke.setBackgroundColor(UNFOCUS_BACKGROUND_COLOR);
                stroke.setSingleLine();
                stroke.setPadding(1,0,1,1);
                stroke.setEllipsize(TextUtils.TruncateAt.END);
                stroke.setClickable(true);
                stroke.setFocusable(true);
                stroke.setFocusableInTouchMode(true);
                stroke.setOnTouchListener(this);
                stroke.setOnFocusChangeListener(this);
                UserDictionaryToolsListFocus candidate = new UserDictionaryToolsListFocus(this);
                candidate.setId(i+MAX_WORD_COUNT);
                candidate.setWidth(system_width/2);
                candidate.setTextSize(WORD_TEXT_SIZE);
                candidate.setTextColor(Color.LTGRAY);
                candidate.setBackgroundColor(UNFOCUS_BACKGROUND_COLOR);
                candidate.setSingleLine();
                candidate.setPadding(1,0,1,1);
                candidate.setEllipsize(TextUtils.TruncateAt.END);
                candidate.setClickable(true);
                candidate.setFocusable(true);
                candidate.setFocusableInTouchMode(true);
                candidate.setOnTouchListener(this);
                candidate.setOnFocusChangeListener(this);
                stroke.setPairView(candidate);
                candidate.setPairView(stroke);
                row.addView(stroke);
                row.addView(candidate);
                mTableLayout.addView(row, tableCreateParam(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
        int size = mWordList.size();
        int start = mWordCount;
        TextView t = (TextView)findViewById(R.id.user_dictionary_position_indicator);
        if (size <= MAX_LIST_WORD_COUNT) {
            ((View)mLeftButton.getParent()).setVisibility(View.GONE);
        } else {
            ((View)mLeftButton.getParent()).setVisibility(View.VISIBLE);
            int last = (start + MAX_LIST_WORD_COUNT);
            t.setText((start + 1) + " - " + Math.min(last, size));
            mLeftButton.setEnabled(start != 0);
            mRightButton.setEnabled(last < size);
        }
        int selectedId = mSelectedViewID - ((MAX_WORD_COUNT < mSelectedViewID) ? MAX_WORD_COUNT : 0);
        for (int i = 0; i < MAX_LIST_WORD_COUNT; i++) {
            if ((size - 1) < (start + i)) {
                if ((0 < i) && (selectedId == (i + 1))) {
                    mTableLayout.findViewById(i).requestFocus();
                }
                ((View)(mTableLayout.findViewById(i + 1)).getParent()).setVisibility(View.GONE);
                continue;
            }
            WnnWord wnnWordGet;
            wnnWordGet = mSortData[start + i];
            int len_stroke = wnnWordGet.stroke.length();
            int len_candidate = wnnWordGet.candidate.length();
            if (len_stroke == 0 || len_candidate == 0) {
                break;
            }
            if (selectedId == i + 1) {
                mTableLayout.findViewById(i + 1).requestFocus();
            }
            TextView text = (TextView)mTableLayout.findViewById(i + 1);
            text.setText(wnnWordGet.stroke);
            text = (TextView)mTableLayout.findViewById(i + 1 + MAX_WORD_COUNT);
            text.setText(wnnWordGet.candidate);
            ((View)text.getParent()).setVisibility(View.VISIBLE);
        }
        mTableLayout.requestLayout();
    }
}
