public abstract class UserDictionaryToolsEdit extends Activity implements View.OnClickListener {
    protected String  mListViewName;
    protected String  mPackageName;
    private static final int STATE_UNKNOWN = 0;
    private static final int STATE_INSERT = 1;
    private static final int STATE_EDIT = 2;
    private static final int MAX_TEXT_SIZE = 20;
    private static final int RETURN_SAME_WORD = -11;
    private static View sFocusingView = null;
    private static View sFocusingPairView = null;
    private EditText mReadEditText;
    private EditText mCandidateEditText;
    private Button mEntryButton;
    private Button mCancelButton;
    private WnnWord mBeforeEditWnnWord;
    private UserDictionaryToolsList mListInstance;
    private static final int DIALOG_CONTROL_WORDS_DUPLICATE = 0;
    private static final int DIALOG_CONTROL_OVER_MAX_TEXT_SIZE = 1;
    private int mRequestState;
    public UserDictionaryToolsEdit() {
        super();
    }
    public UserDictionaryToolsEdit(View focusView, View focusPairView) {
        super();
        sFocusingView = focusView;
        sFocusingPairView = focusPairView;
    }
    protected abstract boolean sendEventToIME(OpenWnnEvent ev);
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.user_dictionary_tools_edit);
        mEntryButton = (Button)findViewById(R.id.addButton);
        mCancelButton = (Button)findViewById(R.id.cancelButton);
        mReadEditText = (EditText)findViewById(R.id.editRead);
        mCandidateEditText = (EditText)findViewById(R.id.editCandidate);
        mEntryButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mRequestState = STATE_UNKNOWN;
        mReadEditText.setSingleLine();
        mCandidateEditText.setSingleLine();
        Intent intent = getIntent();
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_INSERT)) {
            mEntryButton.setEnabled(false);
            mRequestState = STATE_INSERT;
        } else if (action.equals(Intent.ACTION_EDIT)) {
            mEntryButton.setEnabled(true);
            mReadEditText.setText(((TextView)sFocusingView).getText());
            mCandidateEditText.setText(((TextView)sFocusingPairView).getText());
            mRequestState = STATE_EDIT;
            mBeforeEditWnnWord = new WnnWord();
            mBeforeEditWnnWord.stroke = ((TextView)sFocusingView).getText().toString();
            mBeforeEditWnnWord.candidate = ((TextView)sFocusingPairView).getText().toString();
        } else {
            Log.e("OpenWnn", "onCreate() : Invaled Get Intent. ID=" + intent);
            finish();
            return;
        }
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                                  R.layout.user_dictionary_tools_edit_header);
        setAddButtonControl();
    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            screenTransition();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void setAddButtonControl() {
        mReadEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ((mReadEditText.getText().toString().length() != 0) && 
                    (mCandidateEditText.getText().toString().length() != 0)) {
                    mEntryButton.setEnabled(true);
                } else {
                    mEntryButton.setEnabled(false);
                }
            }
        });
        mCandidateEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ((mReadEditText.getText().toString().length() != 0) && 
                    (mCandidateEditText.getText().toString().length() != 0)) {
                    mEntryButton.setEnabled(true);
                } else {
                    mEntryButton.setEnabled(false);
                }
            }
        });
    }
    public void onClick(View v) {
        mEntryButton.setEnabled(false);
        mCancelButton.setEnabled(false);
        switch (v.getId()) {
            case R.id.addButton:
                doSaveAction();
                break;
            case R.id.cancelButton:
                doRevertAction();
                break;
            default:
                Log.e("OpenWnn", "onClick: Get Invalid ButtonID. ID=" + v.getId());
                finish();
                return;
        }
    }
    private void doSaveAction() {
        switch (mRequestState) {
        case STATE_INSERT:
            if (inputDataCheck(mReadEditText) && inputDataCheck(mCandidateEditText)) {
                    String stroke = mReadEditText.getText().toString();
                    String candidate = mCandidateEditText.getText().toString();
                    if (addDictionary(stroke, candidate)) {
                        screenTransition();
                    }
                }
            break;
        case STATE_EDIT:
            if (inputDataCheck(mReadEditText) && inputDataCheck(mCandidateEditText)) {
                deleteDictionary(mBeforeEditWnnWord);
                    String stroke = mReadEditText.getText().toString();
                    String candidate = mCandidateEditText.getText().toString();
                    if (addDictionary(stroke, candidate)) {
                        screenTransition();
                    } else {
                        addDictionary(mBeforeEditWnnWord.stroke, mBeforeEditWnnWord.candidate);
                    }
                }
            break;
        default:
            Log.e("OpenWnn", "doSaveAction: Invalid Add Status. Status=" + mRequestState);
            break;
        }
    }
    private void doRevertAction() {
        screenTransition();
    }
    @Override protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CONTROL_WORDS_DUPLICATE:
                return new AlertDialog.Builder(UserDictionaryToolsEdit.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.user_dictionary_words_duplication_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mEntryButton.setEnabled(true);
                                mCancelButton.setEnabled(true);
                            }
                        })
                        .setCancelable(true)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                mEntryButton.setEnabled(true);
                                mCancelButton.setEnabled(true);
                            }
                        })
                        .create();
            case DIALOG_CONTROL_OVER_MAX_TEXT_SIZE:
                return new AlertDialog.Builder(UserDictionaryToolsEdit.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.user_dictionary_over_max_text_size_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int witchButton) {
                                mEntryButton.setEnabled(true);
                                mCancelButton.setEnabled(true);
                            }
                        })
                        .setCancelable(true)
                        .create();
        }
        return super.onCreateDialog(id);
    }
    private boolean addDictionary(String stroke, String candidate) {
        boolean ret;
        WnnWord wnnWordAdd = new WnnWord();
        wnnWordAdd.stroke = stroke;
        wnnWordAdd.candidate = candidate;
        OpenWnnEvent event = new OpenWnnEvent(OpenWnnEvent.ADD_WORD,
                                  WnnEngine.DICTIONARY_TYPE_USER,
                                  wnnWordAdd);
        ret = sendEventToIME(event);
        if (ret == false) {
            int ret_code = event.errorCode;
            if (ret_code == RETURN_SAME_WORD) {
                showDialog(DIALOG_CONTROL_WORDS_DUPLICATE);
            }
        } else {
            mListInstance = createUserDictionaryToolsList();
        }
        return ret;
    }
    private void deleteDictionary(WnnWord word) {
        mListInstance = createUserDictionaryToolsList();
        boolean deleted = mListInstance.deleteWord(word);
        if (!deleted) {
            Toast.makeText(getApplicationContext(),
                           R.string.user_dictionary_delete_fail,
                           Toast.LENGTH_LONG).show();
        }
    }
    protected abstract UserDictionaryToolsList createUserDictionaryToolsList();
    private boolean inputDataCheck(View v) {
        if ((((TextView)v).getText().length()) > MAX_TEXT_SIZE) {
            showDialog(DIALOG_CONTROL_OVER_MAX_TEXT_SIZE);
            Log.e("OpenWnn", "inputDataCheck() : over max string length.");
            return false;
        }
        return true;
    }
    private void screenTransition() {
        finish();
        Intent intent = new Intent();
        intent.setClassName(mPackageName, mListViewName);
        startActivity(intent);
    }
}
