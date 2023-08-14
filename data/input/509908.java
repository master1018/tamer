public class TwelveKeyDialer extends Activity implements View.OnClickListener,
        View.OnLongClickListener, View.OnKeyListener,
        AdapterView.OnItemClickListener, TextWatcher {
    private static final String EMPTY_NUMBER = "";
    private static final String TAG = "TwelveKeyDialer";
    private static final int TONE_LENGTH_MS = 150;
    private static final int TONE_RELATIVE_VOLUME = 80;
    private static final int DIAL_TONE_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private EditText mDigits;
    private View mDelete;
    private MenuItem mAddToContactMenuItem;
    private ToneGenerator mToneGenerator;
    private Object mToneGeneratorLock = new Object();
    private Drawable mDigitsBackground;
    private Drawable mDigitsEmptyBackground;
    private View mDialpad;
    private View mVoicemailDialAndDeleteRow;
    private View mVoicemailButton;
    private View mDialButton;
    private ListView mDialpadChooser;
    private DialpadChooserAdapter mDialpadChooserAdapter;
    private MenuItem m2SecPauseMenuItem;
    private MenuItem mWaitMenuItem;
    private static final int MENU_ADD_CONTACTS = 1;
    private static final int MENU_2S_PAUSE = 2;
    private static final int MENU_WAIT = 3;
    CallLogAsync mCallLog = new CallLogAsync();
    private String mLastNumberDialed = EMPTY_NUMBER;
    private boolean mDTMFToneEnabled;
    private HapticFeedback mHaptic = new HapticFeedback();
    static final String ADD_CALL_MODE_KEY = "add_call_mode";
    static final String EXTRA_SEND_EMPTY_FLASH
            = "com.android.phone.extra.SEND_EMPTY_FLASH";
    private boolean mIsAddCallMode;
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if ((state == TelephonyManager.CALL_STATE_IDLE) && dialpadChooserVisible()) {
                    showDialpadChooser(false);
                }
            }
        };
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    public void onTextChanged(CharSequence input, int start, int before, int changeCount) {
    }
    public void afterTextChanged(Editable input) {
        if (SpecialCharSequenceMgr.handleChars(this, input.toString(), mDigits)) {
            mDigits.getText().clear();
        }
        if (!isDigitsEmpty()) {
            mDigits.setBackgroundDrawable(mDigitsBackground);
        } else {
            mDigits.setCursorVisible(false);
            mDigits.setBackgroundDrawable(mDigitsEmptyBackground);
        }
        updateDialAndDeleteButtonEnabledState();
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Resources r = getResources();
        if ((r.getConfiguration().uiMode & Configuration.UI_MODE_TYPE_MASK) ==
                Configuration.UI_MODE_TYPE_CAR) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        setContentView(getContentViewResource());
        mDigitsBackground = r.getDrawable(R.drawable.btn_dial_textfield_active);
        mDigitsEmptyBackground = r.getDrawable(R.drawable.btn_dial_textfield);
        mDigits = (EditText) findViewById(R.id.digits);
        mDigits.setKeyListener(DialerKeyListener.getInstance());
        mDigits.setOnClickListener(this);
        mDigits.setOnKeyListener(this);
        maybeAddNumberFormatting();
        View view = findViewById(R.id.one);
        if (view != null) {
            setupKeypad();
        }
        mVoicemailDialAndDeleteRow = findViewById(R.id.voicemailAndDialAndDelete);
        initVoicemailButton();
        mDialButton = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButton);
        if (r.getBoolean(R.bool.config_show_onscreen_dial_button)) {
            mDialButton.setOnClickListener(this);
        } else {
            mDialButton.setVisibility(View.GONE); 
            mDialButton = null;
        }
        view = mVoicemailDialAndDeleteRow.findViewById(R.id.deleteButton);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        mDelete = view;
        mDialpad = findViewById(R.id.dialpad);  
        if (null == mDialpad) {
            mDigits.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        } else {
            mDigits.setInputType(android.text.InputType.TYPE_NULL);
        }
        mDialpadChooser = (ListView) findViewById(R.id.dialpadChooser);
        mDialpadChooser.setOnItemClickListener(this);
        if (!resolveIntent() && icicle != null) {
            super.onRestoreInstanceState(icicle);
        }
        try {
            mHaptic.init(this, r.getBoolean(R.bool.config_enable_dialer_key_vibration));
        } catch (Resources.NotFoundException nfe) {
             Log.e(TAG, "Vibrate control bool missing.", nfe);
        }
    }
    @Override
    protected void onRestoreInstanceState(Bundle icicle) {
    }
    protected void maybeAddNumberFormatting() {
        mDigits.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
    }
    protected int getContentViewResource() {
        return R.layout.twelve_key_dialer;
    }
    private boolean resolveIntent() {
        boolean ignoreState = false;
        final Intent intent;
        if (isChild()) {
            intent = getParent().getIntent();
            ignoreState = intent.getBooleanExtra(DialtactsActivity.EXTRA_IGNORE_STATE, false);
        } else {
            intent = getIntent();
        }
        mIsAddCallMode = false;
        boolean needToShowDialpadChooser = false;
        final String action = intent.getAction();
        if (Intent.ACTION_DIAL.equals(action) || Intent.ACTION_VIEW.equals(action)) {
            mIsAddCallMode = intent.getBooleanExtra(ADD_CALL_MODE_KEY, false);
            Uri uri = intent.getData();
            if (uri != null) {
                if ("tel".equals(uri.getScheme())) {
                    String data = uri.getSchemeSpecificPart();
                    setFormattedDigits(data);
                } else {
                    String type = intent.getType();
                    if (People.CONTENT_ITEM_TYPE.equals(type)
                            || Phones.CONTENT_ITEM_TYPE.equals(type)) {
                        Cursor c = getContentResolver().query(intent.getData(),
                                new String[] {PhonesColumns.NUMBER}, null, null, null);
                        if (c != null) {
                            if (c.moveToFirst()) {
                                setFormattedDigits(c.getString(0));
                            }
                            c.close();
                        }
                    }
                }
            }
        } else if (Intent.ACTION_MAIN.equals(action)) {
            if (phoneIsInUse()) {
                needToShowDialpadChooser = true;
            }
        }
        showDialpadChooser(needToShowDialpadChooser);
        return ignoreState;
    }
    protected void setFormattedDigits(String data) {
        String dialString = PhoneNumberUtils.extractNetworkPortion(data);
        dialString = PhoneNumberUtils.formatNumber(dialString);
        if (!TextUtils.isEmpty(dialString)) {
            Editable digits = mDigits.getText();
            digits.replace(0, digits.length(), dialString);
            afterTextChanged(digits);
        }
    }
    @Override
    protected void onNewIntent(Intent newIntent) {
        setIntent(newIntent);
        resolveIntent();
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDigits.addTextChangedListener(this);
    }
    private void setupKeypad() {
        View view = findViewById(R.id.one);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        findViewById(R.id.two).setOnClickListener(this);
        findViewById(R.id.three).setOnClickListener(this);
        findViewById(R.id.four).setOnClickListener(this);
        findViewById(R.id.five).setOnClickListener(this);
        findViewById(R.id.six).setOnClickListener(this);
        findViewById(R.id.seven).setOnClickListener(this);
        findViewById(R.id.eight).setOnClickListener(this);
        findViewById(R.id.nine).setOnClickListener(this);
        findViewById(R.id.star).setOnClickListener(this);
        view = findViewById(R.id.zero);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        findViewById(R.id.pound).setOnClickListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        queryLastOutgoingCall();
        mDTMFToneEnabled = Settings.System.getInt(getContentResolver(),
                Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
        mHaptic.checkSystemSetting();
        synchronized(mToneGeneratorLock) {
            if (mToneGenerator == null) {
                try {
                    mToneGenerator = new ToneGenerator(DIAL_TONE_STREAM_TYPE, TONE_RELATIVE_VOLUME);
                    setVolumeControlStream(DIAL_TONE_STREAM_TYPE);
                } catch (RuntimeException e) {
                    Log.w(TAG, "Exception caught while creating local tone generator: " + e);
                    mToneGenerator = null;
                }
            }
        }
        Activity parent = getParent();
        if (parent != null && parent instanceof DialtactsActivity) {
            Uri dialUri = ((DialtactsActivity) parent).getAndClearDialUri();
            if (dialUri != null) {
                resolveIntent();
            }
        }
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        if (phoneIsInUse()) {
            mDigits.setHint(R.string.dialerDialpadHintText);
        } else {
            mDigits.setHint(null);
            showDialpadChooser(false);
        }
        updateDialAndDeleteButtonEnabledState();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            InputMethodManager inputMethodManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mDigits.getWindowToken(), 0);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        synchronized(mToneGeneratorLock) {
            if (mToneGenerator != null) {
                mToneGenerator.release();
                mToneGenerator = null;
            }
        }
        mLastNumberDialed = EMPTY_NUMBER;  
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mAddToContactMenuItem = menu.add(0, MENU_ADD_CONTACTS, 0, R.string.recentCalls_addToContact)
                .setIcon(android.R.drawable.ic_menu_add);
        m2SecPauseMenuItem = menu.add(0, MENU_2S_PAUSE, 0, R.string.add_2sec_pause)
                .setIcon(R.drawable.ic_menu_2sec_pause);
        mWaitMenuItem = menu.add(0, MENU_WAIT, 0, R.string.add_wait)
                .setIcon(R.drawable.ic_menu_wait);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (dialpadChooserVisible()) {
            return false;
        }
        if (isDigitsEmpty()) {
            mAddToContactMenuItem.setVisible(false);
            m2SecPauseMenuItem.setVisible(false);
            mWaitMenuItem.setVisible(false);
        } else {
            CharSequence digits = mDigits.getText();
            Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
            intent.putExtra(Insert.PHONE, digits);
            intent.setType(People.CONTENT_ITEM_TYPE);
            mAddToContactMenuItem.setIntent(intent);
            mAddToContactMenuItem.setVisible(true);
            int selectionStart;
            int selectionEnd;
            String strDigits = digits.toString();
            selectionStart = mDigits.getSelectionStart();
            selectionEnd = mDigits.getSelectionEnd();
            if (selectionStart != -1) {
                if (selectionStart > selectionEnd) {
                    int tmp = selectionStart;
                    selectionStart = selectionEnd;
                    selectionEnd = tmp;
                }
                if (selectionStart != 0) {
                    m2SecPauseMenuItem.setVisible(true);
                    mWaitMenuItem.setVisible(showWait(selectionStart,
                                                      selectionEnd, strDigits));
                } else {
                    m2SecPauseMenuItem.setVisible(false);
                    mWaitMenuItem.setVisible(false);
                }
            } else {
                int strLength = strDigits.length();
                mWaitMenuItem.setVisible(showWait(strLength,
                                                      strLength, strDigits));
            }
        }
        return true;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                long callPressDiff = SystemClock.uptimeMillis() - event.getDownTime();
                if (callPressDiff >= ViewConfiguration.getLongPressTimeout()) {
                    Intent intent = new Intent(Intent.ACTION_VOICE_COMMAND);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                    }
                }
                return true;
            }
            case KeyEvent.KEYCODE_1: {
                long timeDiff = SystemClock.uptimeMillis() - event.getDownTime();
                if (timeDiff >= ViewConfiguration.getLongPressTimeout()) {
                    callVoicemail();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                if (!phoneIsCdma() && mIsAddCallMode && isDigitsEmpty()) {
                    finish();
                }
                dialButtonPressed();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
    private void keyPressed(int keyCode) {
        mHaptic.vibrate();
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        mDigits.onKeyDown(keyCode, event);
    }
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        switch (view.getId()) {
            case R.id.digits:
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    dialButtonPressed();
                    return true;
                }
                break;
        }
        return false;
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.one: {
                playTone(ToneGenerator.TONE_DTMF_1);
                keyPressed(KeyEvent.KEYCODE_1);
                return;
            }
            case R.id.two: {
                playTone(ToneGenerator.TONE_DTMF_2);
                keyPressed(KeyEvent.KEYCODE_2);
                return;
            }
            case R.id.three: {
                playTone(ToneGenerator.TONE_DTMF_3);
                keyPressed(KeyEvent.KEYCODE_3);
                return;
            }
            case R.id.four: {
                playTone(ToneGenerator.TONE_DTMF_4);
                keyPressed(KeyEvent.KEYCODE_4);
                return;
            }
            case R.id.five: {
                playTone(ToneGenerator.TONE_DTMF_5);
                keyPressed(KeyEvent.KEYCODE_5);
                return;
            }
            case R.id.six: {
                playTone(ToneGenerator.TONE_DTMF_6);
                keyPressed(KeyEvent.KEYCODE_6);
                return;
            }
            case R.id.seven: {
                playTone(ToneGenerator.TONE_DTMF_7);
                keyPressed(KeyEvent.KEYCODE_7);
                return;
            }
            case R.id.eight: {
                playTone(ToneGenerator.TONE_DTMF_8);
                keyPressed(KeyEvent.KEYCODE_8);
                return;
            }
            case R.id.nine: {
                playTone(ToneGenerator.TONE_DTMF_9);
                keyPressed(KeyEvent.KEYCODE_9);
                return;
            }
            case R.id.zero: {
                playTone(ToneGenerator.TONE_DTMF_0);
                keyPressed(KeyEvent.KEYCODE_0);
                return;
            }
            case R.id.pound: {
                playTone(ToneGenerator.TONE_DTMF_P);
                keyPressed(KeyEvent.KEYCODE_POUND);
                return;
            }
            case R.id.star: {
                playTone(ToneGenerator.TONE_DTMF_S);
                keyPressed(KeyEvent.KEYCODE_STAR);
                return;
            }
            case R.id.deleteButton: {
                keyPressed(KeyEvent.KEYCODE_DEL);
                return;
            }
            case R.id.dialButton: {
                mHaptic.vibrate();  
                dialButtonPressed();
                return;
            }
            case R.id.voicemailButton: {
                callVoicemail();
                mHaptic.vibrate();
                return;
            }
            case R.id.digits: {
                if (!isDigitsEmpty()) {
                    mDigits.setCursorVisible(true);
                }
                return;
            }
        }
    }
    public boolean onLongClick(View view) {
        final Editable digits = mDigits.getText();
        int id = view.getId();
        switch (id) {
            case R.id.deleteButton: {
                digits.clear();
                mDelete.setPressed(false);
                return true;
            }
            case R.id.one: {
                if (isDigitsEmpty()) {
                    callVoicemail();
                    return true;
                }
                return false;
            }
            case R.id.zero: {
                keyPressed(KeyEvent.KEYCODE_PLUS);
                return true;
            }
        }
        return false;
    }
    void callVoicemail() {
        Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                Uri.fromParts("voicemail", EMPTY_NUMBER, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        mDigits.getText().clear();
        finish();
    }
    void dialButtonPressed() {
        final String number = mDigits.getText().toString();
        boolean sendEmptyFlash = false;
        Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
        if (isDigitsEmpty()) { 
            if (phoneIsCdma() && phoneIsOffhook()) {
                intent.setData(Uri.fromParts("tel", EMPTY_NUMBER, null));
                intent.putExtra(EXTRA_SEND_EMPTY_FLASH, true);
                sendEmptyFlash = true;
            } else if (!TextUtils.isEmpty(mLastNumberDialed)) {
                mDigits.setText(mLastNumberDialed);
                return;
            } else {
                playTone(ToneGenerator.TONE_PROP_NACK);
                return;
            }
        } else {  
            intent.setData(Uri.fromParts("tel", number, null));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        mDigits.getText().clear();
        if (!sendEmptyFlash) {
            finish();
        }
    }
    void playTone(int tone) {
        if (!mDTMFToneEnabled) {
            return;
        }
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
            || (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
            return;
        }
        synchronized(mToneGeneratorLock) {
            if (mToneGenerator == null) {
                Log.w(TAG, "playTone: mToneGenerator == null, tone: "+tone);
                return;
            }
            mToneGenerator.startTone(tone, TONE_LENGTH_MS);
        }
    }
    private void showDialpadChooser(boolean enabled) {
        if (enabled) {
            mDigits.setVisibility(View.GONE);
            if (mDialpad != null) mDialpad.setVisibility(View.GONE);
            mVoicemailDialAndDeleteRow.setVisibility(View.GONE);
            mDialpadChooser.setVisibility(View.VISIBLE);
            if (mDialpadChooserAdapter == null) {
                mDialpadChooserAdapter = new DialpadChooserAdapter(this);
                mDialpadChooser.setAdapter(mDialpadChooserAdapter);
            }
        } else {
            mDigits.setVisibility(View.VISIBLE);
            if (mDialpad != null) mDialpad.setVisibility(View.VISIBLE);
            mVoicemailDialAndDeleteRow.setVisibility(View.VISIBLE);
            mDialpadChooser.setVisibility(View.GONE);
        }
    }
    private boolean dialpadChooserVisible() {
        return mDialpadChooser.getVisibility() == View.VISIBLE;
    }
    private static class DialpadChooserAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        static class ChoiceItem {
            String text;
            Bitmap icon;
            int id;
            public ChoiceItem(String s, Bitmap b, int i) {
                text = s;
                icon = b;
                id = i;
            }
        }
        static final int DIALPAD_CHOICE_USE_DTMF_DIALPAD = 101;
        static final int DIALPAD_CHOICE_RETURN_TO_CALL = 102;
        static final int DIALPAD_CHOICE_ADD_NEW_CALL = 103;
        private static final int NUM_ITEMS = 3;
        private ChoiceItem mChoiceItems[] = new ChoiceItem[NUM_ITEMS];
        public DialpadChooserAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mChoiceItems[0] = new ChoiceItem(
                    context.getString(R.string.dialer_useDtmfDialpad),
                    BitmapFactory.decodeResource(context.getResources(),
                                                 R.drawable.ic_dialer_fork_tt_keypad),
                    DIALPAD_CHOICE_USE_DTMF_DIALPAD);
            mChoiceItems[1] = new ChoiceItem(
                    context.getString(R.string.dialer_returnToInCallScreen),
                    BitmapFactory.decodeResource(context.getResources(),
                                                 R.drawable.ic_dialer_fork_current_call),
                    DIALPAD_CHOICE_RETURN_TO_CALL);
            mChoiceItems[2] = new ChoiceItem(
                    context.getString(R.string.dialer_addAnotherCall),
                    BitmapFactory.decodeResource(context.getResources(),
                                                 R.drawable.ic_dialer_fork_add_call),
                    DIALPAD_CHOICE_ADD_NEW_CALL);
        }
        public int getCount() {
            return NUM_ITEMS;
        }
        public Object getItem(int position) {
            return mChoiceItems[position];
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.dialpad_chooser_list_item, null);
            }
            TextView text = (TextView) convertView.findViewById(R.id.text);
            text.setText(mChoiceItems[position].text);
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            icon.setImageBitmap(mChoiceItems[position].icon);
            return convertView;
        }
    }
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        DialpadChooserAdapter.ChoiceItem item =
                (DialpadChooserAdapter.ChoiceItem) parent.getItemAtPosition(position);
        int itemId = item.id;
        switch (itemId) {
            case DialpadChooserAdapter.DIALPAD_CHOICE_USE_DTMF_DIALPAD:
                returnToInCallScreen(true);
                break;
            case DialpadChooserAdapter.DIALPAD_CHOICE_RETURN_TO_CALL:
                returnToInCallScreen(false);
                break;
            case DialpadChooserAdapter.DIALPAD_CHOICE_ADD_NEW_CALL:
                showDialpadChooser(false);
                break;
            default:
                Log.w(TAG, "onItemClick: unexpected itemId: " + itemId);
                break;
        }
    }
    private void returnToInCallScreen(boolean showDialpad) {
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) phone.showCallScreenWithDialpad(showDialpad);
        } catch (RemoteException e) {
            Log.w(TAG, "phone.showCallScreenWithDialpad() failed", e);
        }
        finish();
    }
    private boolean phoneIsInUse() {
        boolean phoneInUse = false;
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) phoneInUse = !phone.isIdle();
        } catch (RemoteException e) {
            Log.w(TAG, "phone.isIdle() failed", e);
        }
        return phoneInUse;
    }
    private boolean phoneIsCdma() {
        boolean isCdma = false;
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) {
                isCdma = (phone.getActivePhoneType() == TelephonyManager.PHONE_TYPE_CDMA);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "phone.getActivePhoneType() failed", e);
        }
        return isCdma;
    }
    private boolean phoneIsOffhook() {
        boolean phoneOffhook = false;
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) phoneOffhook = phone.isOffhook();
        } catch (RemoteException e) {
            Log.w(TAG, "phone.isOffhook() failed", e);
        }
        return phoneOffhook;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_2S_PAUSE:
                updateDialString(",");
                return true;
            case MENU_WAIT:
                updateDialString(";");
                return true;
        }
        return false;
    }
    private void updateDialString(String newDigits) {
        int selectionStart;
        int selectionEnd;
        int anchor = mDigits.getSelectionStart();
        int point = mDigits.getSelectionEnd();
        selectionStart = Math.min(anchor, point);
        selectionEnd = Math.max(anchor, point);
        Editable digits = mDigits.getText();
        if (selectionStart != -1 ) {
            if (selectionStart == selectionEnd) {
                digits.replace(selectionStart, selectionStart, newDigits);
            } else {
                digits.replace(selectionStart, selectionEnd, newDigits);
                mDigits.setSelection(selectionStart + 1);
            }
        } else {
            int len = mDigits.length();
            digits.replace(len, len, newDigits);
        }
    }
    private void updateDialAndDeleteButtonEnabledState() {
        final boolean digitsNotEmpty = !isDigitsEmpty();
        if (mDialButton != null) {
            if (phoneIsCdma() && phoneIsOffhook()) {
                mDialButton.setEnabled(true);
            } else {
                mDialButton.setEnabled(digitsNotEmpty ||
                                       !TextUtils.isEmpty(mLastNumberDialed));
            }
        }
        mDelete.setEnabled(digitsNotEmpty);
    }
    private void initVoicemailButton() {
        boolean hasVoicemail = false;
        try {
            hasVoicemail = TelephonyManager.getDefault().getVoiceMailNumber() != null;
        } catch (SecurityException se) {
        }
        mVoicemailButton = mVoicemailDialAndDeleteRow.findViewById(R.id.voicemailButton);
        if (hasVoicemail) {
            mVoicemailButton.setOnClickListener(this);
        } else {
            mVoicemailButton.setEnabled(false);
        }
    }
    private boolean showWait(int start, int end, String digits) {
        if (start == end) {
            if (start > digits.length()) return false;
            if (digits.charAt(start-1) == ';') return false;
            if ((digits.length() > start) && (digits.charAt(start) == ';')) return false;
        } else {
            if (start > digits.length() || end > digits.length()) return false;
            if (digits.charAt(start-1) == ';') return false;
        }
        return true;
    }
    private boolean isDigitsEmpty() {
        return mDigits.length() == 0;
    }
    private void queryLastOutgoingCall() {
        mLastNumberDialed = EMPTY_NUMBER;
        CallLogAsync.GetLastOutgoingCallArgs lastCallArgs =
                new CallLogAsync.GetLastOutgoingCallArgs(
                    this,
                    new CallLogAsync.OnLastOutgoingCallComplete() {
                        public void lastOutgoingCall(String number) {
                            mLastNumberDialed = number;
                            updateDialAndDeleteButtonEnabledState();
                        }
                    });
        mCallLog.getLastOutgoingCall(lastCallArgs);
    }
    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData,
            boolean globalSearch) {
        if (globalSearch) {
            super.startSearch(initialQuery, selectInitialQuery, appSearchData, globalSearch);
        } else {
            ContactsSearchManager.startSearch(this, initialQuery);
        }
    }
}
