public class StkMenuActivity extends ListActivity {
    private Context mContext;
    private Menu mStkMenu = null;
    private int mState = STATE_MAIN;
    private boolean mAcceptUsersInput = true;
    private TextView mTitleTextView = null;
    private ImageView mTitleIconView = null;
    private ProgressBar mProgressView = null;
    StkAppService appService = StkAppService.getInstance();
    static final int STATE_MAIN = 1;
    static final int STATE_SECONDARY = 2;
    private static final int MSG_ID_TIMEOUT = 1;
    Handler mTimeoutHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case MSG_ID_TIMEOUT:
                mAcceptUsersInput = false;
                sendResponse(StkAppService.RES_ID_TIMEOUT);
                break;
            }
        }
    };
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        StkLog.d(this, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.stk_menu_list);
        mTitleTextView = (TextView) findViewById(R.id.title_text);
        mTitleIconView = (ImageView) findViewById(R.id.title_icon);
        mProgressView = (ProgressBar) findViewById(R.id.progress_bar);
        mContext = getBaseContext();
        initFromIntent(getIntent());
        mAcceptUsersInput = true;
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        StkLog.d(this, "onNewIntent");
        initFromIntent(intent);
        mAcceptUsersInput = true;
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (!mAcceptUsersInput) {
            return;
        }
        Item item = getSelectedItem(position);
        if (item == null) {
            return;
        }
        sendResponse(StkAppService.RES_ID_MENU_SELECTION, item.id, false);
        mAcceptUsersInput = false;
        mProgressView.setVisibility(View.VISIBLE);
        mProgressView.setIndeterminate(true);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!mAcceptUsersInput) {
            return true;
        }
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            switch (mState) {
            case STATE_SECONDARY:
                cancelTimeOut();
                mAcceptUsersInput = false;
                sendResponse(StkAppService.RES_ID_BACKWARD);
                return true;
            case STATE_MAIN:
                break;
            }
            break;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onResume() {
        super.onResume();
        appService.indicateMenuVisibility(true);
        mStkMenu = appService.getMenu();
        if (mStkMenu == null) {
            finish();
            return;
        }
        displayMenu();
        startTimeOut();
        if (!mAcceptUsersInput) {
            mState = STATE_MAIN;
            mAcceptUsersInput = true;
        }
        mProgressView.setIndeterminate(false);
        mProgressView.setVisibility(View.GONE);
    }
    @Override
    public void onPause() {
        super.onPause();
        appService.indicateMenuVisibility(false);
        cancelTimeOut();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        StkLog.d(this, "onDestroy");
    }
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, StkApp.MENU_ID_END_SESSION, 1, R.string.menu_end_session);
        menu.add(0, StkApp.MENU_ID_HELP, 2, R.string.help);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean helpVisible = false;
        boolean mainVisible = false;
        if (mState == STATE_SECONDARY) {
            mainVisible = true;
        }
        if (mStkMenu != null) {
            helpVisible = mStkMenu.helpAvailable;
        }
        menu.findItem(StkApp.MENU_ID_END_SESSION).setVisible(mainVisible);
        menu.findItem(StkApp.MENU_ID_HELP).setVisible(helpVisible);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!mAcceptUsersInput) {
            return true;
        }
        switch (item.getItemId()) {
        case StkApp.MENU_ID_END_SESSION:
            cancelTimeOut();
            mAcceptUsersInput = false;
            sendResponse(StkAppService.RES_ID_END_SESSION);
            return true;
        case StkApp.MENU_ID_HELP:
            cancelTimeOut();
            mAcceptUsersInput = false;
            int position = getSelectedItemPosition();
            Item stkItem = getSelectedItem(position);
            if (stkItem == null) {
                break;
            }
            sendResponse(StkAppService.RES_ID_MENU_SELECTION, stkItem.id, true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("STATE", mState);
        outState.putParcelable("MENU", mStkMenu);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mState = savedInstanceState.getInt("STATE");
        mStkMenu = savedInstanceState.getParcelable("MENU");
    }
    private void cancelTimeOut() {
        mTimeoutHandler.removeMessages(MSG_ID_TIMEOUT);
    }
    private void startTimeOut() {
        if (mState == STATE_SECONDARY) {
            cancelTimeOut();
            mTimeoutHandler.sendMessageDelayed(mTimeoutHandler
                    .obtainMessage(MSG_ID_TIMEOUT), StkApp.UI_TIMEOUT);
        }
    }
    private void displayMenu() {
        if (mStkMenu != null) {
            if (mStkMenu.titleIcon != null) {
                mTitleIconView.setImageBitmap(mStkMenu.titleIcon);
                mTitleIconView.setVisibility(View.VISIBLE);
            } else {
                mTitleIconView.setVisibility(View.GONE);
            }
            if (!mStkMenu.titleIconSelfExplanatory) {
                mTitleTextView.setVisibility(View.VISIBLE);
                if (mStkMenu.title == null) {
                    mTitleTextView.setText(R.string.app_name);
                } else {
                    mTitleTextView.setText(mStkMenu.title);
                }
            } else {
                mTitleTextView.setVisibility(View.INVISIBLE);
            }
            StkMenuAdapter adapter = new StkMenuAdapter(this,
                    mStkMenu.items, mStkMenu.itemsIconSelfExplanatory);
            setListAdapter(adapter);
            setSelection(mStkMenu.defaultItem);
        }
    }
    private void initFromIntent(Intent intent) {
        if (intent != null) {
            mState = intent.getIntExtra("STATE", STATE_MAIN);
        } else {
            finish();
        }
    }
    private Item getSelectedItem(int position) {
        Item item = null;
        if (mStkMenu != null) {
            try {
                item = mStkMenu.items.get(position);
            } catch (IndexOutOfBoundsException e) {
                if (StkApp.DBG) {
                    StkLog.d(this, "Invalid menu");
                }
            } catch (NullPointerException e) {
                if (StkApp.DBG) {
                    StkLog.d(this, "Invalid menu");
                }
            }
        }
        return item;
    }
    private void sendResponse(int resId) {
        sendResponse(resId, 0, false);
    }
    private void sendResponse(int resId, int itemId, boolean help) {
        Bundle args = new Bundle();
        args.putInt(StkAppService.OPCODE, StkAppService.OP_RESPONSE);
        args.putInt(StkAppService.RES_ID, resId);
        args.putInt(StkAppService.MENU_SELECTION, itemId);
        args.putBoolean(StkAppService.HELP, help);
        mContext.startService(new Intent(mContext, StkAppService.class)
                .putExtras(args));
    }
}
