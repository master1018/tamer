public final class RingtonePickerActivity extends AlertActivity implements
        AdapterView.OnItemSelectedListener, Runnable, DialogInterface.OnClickListener,
        AlertController.AlertParams.OnPrepareListViewListener {
    private static final String TAG = "RingtonePickerActivity";
    private static final int DELAY_MS_SELECTION_PLAYED = 300;
    private RingtoneManager mRingtoneManager;
    private Cursor mCursor;
    private Handler mHandler;
    private int mSilentPos = -1;
    private int mDefaultRingtonePos = -1;
    private int mClickedPos = -1;
    private int mSampleRingtonePos = -1;
    private boolean mHasSilentItem;
    private Uri mExistingUri;
    private int mStaticItemCount;
    private boolean mHasDefaultItem;
    private Uri mUriForDefaultItem;
    private Ringtone mDefaultRingtone;
    private DialogInterface.OnClickListener mRingtoneClickListener =
            new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            mClickedPos = which;
            playRingtone(which, 0);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        Intent intent = getIntent();
        mHasDefaultItem = intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        mUriForDefaultItem = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI);
        if (mUriForDefaultItem == null) {
            mUriForDefaultItem = Settings.System.DEFAULT_RINGTONE_URI;
        }
        mHasSilentItem = intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        mRingtoneManager = new RingtoneManager(this);
        boolean includeDrm = intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_INCLUDE_DRM,
                true);
        mRingtoneManager.setIncludeDrm(includeDrm);
        int types = intent.getIntExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, -1);
        if (types != -1) {
            mRingtoneManager.setType(types);
        }
        mCursor = mRingtoneManager.getCursor();
        setVolumeControlStream(mRingtoneManager.inferStreamType());
        mExistingUri = intent
                .getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI);
        final AlertController.AlertParams p = mAlertParams;
        p.mCursor = mCursor;
        p.mOnClickListener = mRingtoneClickListener;
        p.mLabelColumn = MediaStore.Audio.Media.TITLE;
        p.mIsSingleChoice = true;
        p.mOnItemSelectedListener = this;
        p.mPositiveButtonText = getString(com.android.internal.R.string.ok);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(com.android.internal.R.string.cancel);
        p.mPositiveButtonListener = this;
        p.mOnPrepareListViewListener = this;
        p.mTitle = intent.getCharSequenceExtra(RingtoneManager.EXTRA_RINGTONE_TITLE);
        if (p.mTitle == null) {
            p.mTitle = getString(com.android.internal.R.string.ringtone_picker_title);
        }
        setupAlert();
    }
    public void onPrepareListView(ListView listView) {
        if (mHasDefaultItem) {
            mDefaultRingtonePos = addDefaultRingtoneItem(listView);
            if (RingtoneManager.isDefault(mExistingUri)) {
                mClickedPos = mDefaultRingtonePos;
            }
        }
        if (mHasSilentItem) {
            mSilentPos = addSilentItem(listView);
            if (mExistingUri == null) {
                mClickedPos = mSilentPos;
            }
        }
        if (mClickedPos == -1) {
            mClickedPos = getListPosition(mRingtoneManager.getRingtonePosition(mExistingUri));
        }
        mAlertParams.mCheckedItem = mClickedPos;
    }
    private int addStaticItem(ListView listView, int textResId) {
        TextView textView = (TextView) getLayoutInflater().inflate(
                com.android.internal.R.layout.select_dialog_singlechoice, listView, false);
        textView.setText(textResId);
        listView.addHeaderView(textView);
        mStaticItemCount++;
        return listView.getHeaderViewsCount() - 1;
    }
    private int addDefaultRingtoneItem(ListView listView) {
        return addStaticItem(listView, com.android.internal.R.string.ringtone_default);
    }
    private int addSilentItem(ListView listView) {
        return addStaticItem(listView, com.android.internal.R.string.ringtone_silent);
    }
    public void onClick(DialogInterface dialog, int which) {
        boolean positiveResult = which == BUTTON1;
        mRingtoneManager.stopPreviousRingtone();
        if (positiveResult) {
            Intent resultIntent = new Intent();
            Uri uri = null;
            if (mClickedPos == mDefaultRingtonePos) {
                uri = mUriForDefaultItem;
            } else if (mClickedPos == mSilentPos) {
                uri = null;
            } else {
                uri = mRingtoneManager.getRingtoneUri(getRingtoneManagerPosition(mClickedPos));
            }
            resultIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, uri);
            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_CANCELED);
        }
        getWindow().getDecorView().post(new Runnable() {
            public void run() {
                mCursor.deactivate();
            }
        });
        finish();
    }
    public void onItemSelected(AdapterView parent, View view, int position, long id) {
        playRingtone(position, DELAY_MS_SELECTION_PLAYED);
    }
    public void onNothingSelected(AdapterView parent) {
    }
    private void playRingtone(int position, int delayMs) {
        mHandler.removeCallbacks(this);
        mSampleRingtonePos = position;
        mHandler.postDelayed(this, delayMs);
    }
    public void run() {
        if (mSampleRingtonePos == mSilentPos) {
            mRingtoneManager.stopPreviousRingtone();
            return;
        }
        if (mDefaultRingtone != null && mDefaultRingtone.isPlaying()) {
            mDefaultRingtone.stop();
            mDefaultRingtone = null;
        }
        Ringtone ringtone;
        if (mSampleRingtonePos == mDefaultRingtonePos) {
            if (mDefaultRingtone == null) {
                mDefaultRingtone = RingtoneManager.getRingtone(this, mUriForDefaultItem);
            }
            ringtone = mDefaultRingtone;
            mRingtoneManager.stopPreviousRingtone();
        } else {
            ringtone = mRingtoneManager.getRingtone(getRingtoneManagerPosition(mSampleRingtonePos));
        }
        if (ringtone != null) {
            ringtone.play();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        stopAnyPlayingRingtone();
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopAnyPlayingRingtone();
    }
    private void stopAnyPlayingRingtone() {
        if (mDefaultRingtone != null && mDefaultRingtone.isPlaying()) {
            mDefaultRingtone.stop();
        }
        if (mRingtoneManager != null) {
            mRingtoneManager.stopPreviousRingtone();
        }
    }
    private int getRingtoneManagerPosition(int listPos) {
        return listPos - mStaticItemCount;
    }
    private int getListPosition(int ringtoneManagerPos) {
        if (ringtoneManagerPos < 0) return ringtoneManagerPos;
        return ringtoneManagerPos + mStaticItemCount;
    }
}
