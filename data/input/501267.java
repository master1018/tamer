public class BluetoothOppTransferActivity extends AlertActivity implements
        DialogInterface.OnClickListener {
    private static final String TAG = "BluetoothOppTransferActivity";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    private Uri mUri;
    boolean mIsComplete;
    private BluetoothOppTransferInfo mTransInfo;
    private ProgressBar mProgressTransfer;
    private TextView mPercentView;
    private AlertController.AlertParams mPara;
    private View mView = null;
    private TextView mLine1View, mLine2View, mLine3View, mLine5View;
    private int mWhichDialog;
    private BluetoothAdapter mAdapter;
    public static final int DIALOG_RECEIVE_ONGOING = 0;
    public static final int DIALOG_RECEIVE_COMPLETE_SUCCESS = 1;
    public static final int DIALOG_RECEIVE_COMPLETE_FAIL = 2;
    public static final int DIALOG_SEND_ONGOING = 3;
    public static final int DIALOG_SEND_COMPLETE_SUCCESS = 4;
    public static final int DIALOG_SEND_COMPLETE_FAIL = 5;
    private BluetoothTransferContentObserver mObserver;
    private boolean mNeedUpdateButton = false;
    private class BluetoothTransferContentObserver extends ContentObserver {
        public BluetoothTransferContentObserver() {
            super(new Handler());
        }
        @Override
        public void onChange(boolean selfChange) {
            if (V) Log.v(TAG, "received db changes.");
            mNeedUpdateButton = true;
            updateProgressbar();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mUri = intent.getData();
        mTransInfo = new BluetoothOppTransferInfo();
        mTransInfo = BluetoothOppUtility.queryRecord(this, mUri);
        if (mTransInfo == null) {
            if (V) Log.e(TAG, "Error: Can not get data from db");
            finish();
            return;
        }
        mIsComplete = BluetoothShare.isStatusCompleted(mTransInfo.mStatus);
        displayWhichDialog();
        if (!mIsComplete) {
            mObserver = new BluetoothTransferContentObserver();
            getContentResolver().registerContentObserver(BluetoothShare.CONTENT_URI, true,
                    mObserver);
        }
        if (mWhichDialog != DIALOG_SEND_ONGOING && mWhichDialog != DIALOG_RECEIVE_ONGOING) {
            BluetoothOppUtility.updateVisibilityToHidden(this, mUri);
        }
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        setUpDialog();
    }
    @Override
    protected void onDestroy() {
        if (D) Log.d(TAG, "onDestroy()");
        if (mObserver != null) {
            getContentResolver().unregisterContentObserver(mObserver);
        }
        super.onDestroy();
    }
    private void displayWhichDialog() {
        int direction = mTransInfo.mDirection;
        boolean isSuccess = BluetoothShare.isStatusSuccess(mTransInfo.mStatus);
        boolean isComplete = BluetoothShare.isStatusCompleted(mTransInfo.mStatus);
        if (direction == BluetoothShare.DIRECTION_INBOUND) {
            if (isComplete == true) {
                if (isSuccess == true) {
                    mWhichDialog = DIALOG_RECEIVE_COMPLETE_SUCCESS;
                } else if (isSuccess == false) {
                    mWhichDialog = DIALOG_RECEIVE_COMPLETE_FAIL;
                }
            } else if (isComplete == false) {
                mWhichDialog = DIALOG_RECEIVE_ONGOING;
            }
        } else if (direction == BluetoothShare.DIRECTION_OUTBOUND) {
            if (isComplete == true) {
                if (isSuccess == true) {
                    mWhichDialog = DIALOG_SEND_COMPLETE_SUCCESS;
                } else if (isSuccess == false) {
                    mWhichDialog = DIALOG_SEND_COMPLETE_FAIL;
                }
            } else if (isComplete == false) {
                mWhichDialog = DIALOG_SEND_ONGOING;
            }
        }
        if (V) Log.v(TAG, " WhichDialog/dir/isComplete/failOrSuccess" + mWhichDialog + direction
                    + isComplete + isSuccess);
    }
    private void setUpDialog() {
        mPara = mAlertParams;
        mPara.mIconId = android.R.drawable.ic_dialog_info;
        mPara.mTitle = getString(R.string.download_title);
        if ((mWhichDialog == DIALOG_RECEIVE_ONGOING) || (mWhichDialog == DIALOG_SEND_ONGOING)) {
            mPara.mPositiveButtonText = getString(R.string.download_ok);
            mPara.mPositiveButtonListener = this;
            mPara.mNegativeButtonText = getString(R.string.download_cancel);
            mPara.mNegativeButtonListener = this;
        } else if (mWhichDialog == DIALOG_RECEIVE_COMPLETE_SUCCESS) {
            mPara.mPositiveButtonText = getString(R.string.download_succ_ok);
            mPara.mPositiveButtonListener = this;
        } else if (mWhichDialog == DIALOG_RECEIVE_COMPLETE_FAIL) {
            mPara.mIconId = android.R.drawable.ic_dialog_alert;
            mPara.mPositiveButtonText = getString(R.string.download_fail_ok);
            mPara.mPositiveButtonListener = this;
        } else if (mWhichDialog == DIALOG_SEND_COMPLETE_SUCCESS) {
            mPara.mPositiveButtonText = getString(R.string.upload_succ_ok);
            mPara.mPositiveButtonListener = this;
        } else if (mWhichDialog == DIALOG_SEND_COMPLETE_FAIL) {
            mPara.mIconId = android.R.drawable.ic_dialog_alert;
            mPara.mPositiveButtonText = getString(R.string.upload_fail_ok);
            mPara.mPositiveButtonListener = this;
            mPara.mNegativeButtonText = getString(R.string.upload_fail_cancel);
            mPara.mNegativeButtonListener = this;
        }
        mPara.mView = createView();
        setupAlert();
    }
    private View createView() {
        mView = getLayoutInflater().inflate(R.layout.file_transfer, null);
        mProgressTransfer = (ProgressBar)mView.findViewById(R.id.progress_transfer);
        mPercentView = (TextView)mView.findViewById(R.id.progress_percent);
        customizeViewContent();
        mNeedUpdateButton = false;
        updateProgressbar();
        return mView;
    }
    private void customizeViewContent() {
        String tmp;
        if (mWhichDialog == DIALOG_RECEIVE_ONGOING
                || mWhichDialog == DIALOG_RECEIVE_COMPLETE_SUCCESS) {
            mLine1View = (TextView)mView.findViewById(R.id.line1_view);
            tmp = getString(R.string.download_line1, mTransInfo.mDeviceName);
            mLine1View.setText(tmp);
            mLine2View = (TextView)mView.findViewById(R.id.line2_view);
            tmp = getString(R.string.download_line2, mTransInfo.mFileName);
            mLine2View.setText(tmp);
            mLine3View = (TextView)mView.findViewById(R.id.line3_view);
            tmp = getString(R.string.download_line3, Formatter.formatFileSize(this,
                    mTransInfo.mTotalBytes));
            mLine3View.setText(tmp);
            mLine5View = (TextView)mView.findViewById(R.id.line5_view);
            if (mWhichDialog == DIALOG_RECEIVE_ONGOING) {
                tmp = getString(R.string.download_line5);
            } else if (mWhichDialog == DIALOG_RECEIVE_COMPLETE_SUCCESS) {
                tmp = getString(R.string.download_succ_line5);
            }
            mLine5View.setText(tmp);
        } else if (mWhichDialog == DIALOG_SEND_ONGOING
                || mWhichDialog == DIALOG_SEND_COMPLETE_SUCCESS) {
            mLine1View = (TextView)mView.findViewById(R.id.line1_view);
            tmp = getString(R.string.upload_line1, mTransInfo.mDeviceName);
            mLine1View.setText(tmp);
            mLine2View = (TextView)mView.findViewById(R.id.line2_view);
            tmp = getString(R.string.download_line2, mTransInfo.mFileName);
            mLine2View.setText(tmp);
            mLine3View = (TextView)mView.findViewById(R.id.line3_view);
            tmp = getString(R.string.upload_line3, mTransInfo.mFileType, Formatter.formatFileSize(
                    this, mTransInfo.mTotalBytes));
            mLine3View.setText(tmp);
            mLine5View = (TextView)mView.findViewById(R.id.line5_view);
            if (mWhichDialog == DIALOG_SEND_ONGOING) {
                tmp = getString(R.string.upload_line5);
            } else if (mWhichDialog == DIALOG_SEND_COMPLETE_SUCCESS) {
                tmp = getString(R.string.upload_succ_line5);
            }
            mLine5View.setText(tmp);
        } else if (mWhichDialog == DIALOG_RECEIVE_COMPLETE_FAIL) {
            if (mTransInfo.mStatus == BluetoothShare.STATUS_ERROR_SDCARD_FULL) {
                mLine1View = (TextView)mView.findViewById(R.id.line1_view);
                tmp = getString(R.string.bt_sm_2_1, mTransInfo.mDeviceName);
                mLine1View.setText(tmp);
                mLine2View = (TextView)mView.findViewById(R.id.line2_view);
                tmp = getString(R.string.download_fail_line2, mTransInfo.mFileName);
                mLine2View.setText(tmp);
                mLine3View = (TextView)mView.findViewById(R.id.line3_view);
                tmp = getString(R.string.bt_sm_2_2, Formatter.formatFileSize(this,
                        mTransInfo.mTotalBytes));
                mLine3View.setText(tmp);
            } else {
                mLine1View = (TextView)mView.findViewById(R.id.line1_view);
                tmp = getString(R.string.download_fail_line1);
                mLine1View.setText(tmp);
                mLine2View = (TextView)mView.findViewById(R.id.line2_view);
                tmp = getString(R.string.download_fail_line2, mTransInfo.mFileName);
                mLine2View.setText(tmp);
                mLine3View = (TextView)mView.findViewById(R.id.line3_view);
                tmp = getString(R.string.download_fail_line3, BluetoothOppUtility
                        .getStatusDescription(this, mTransInfo.mStatus));
                mLine3View.setText(tmp);
            }
            mLine5View = (TextView)mView.findViewById(R.id.line5_view);
            mLine5View.setVisibility(View.GONE);
        } else if (mWhichDialog == DIALOG_SEND_COMPLETE_FAIL) {
            mLine1View = (TextView)mView.findViewById(R.id.line1_view);
            tmp = getString(R.string.upload_fail_line1, mTransInfo.mDeviceName);
            mLine1View.setText(tmp);
            mLine2View = (TextView)mView.findViewById(R.id.line2_view);
            tmp = getString(R.string.upload_fail_line1_2, mTransInfo.mFileName);
            mLine2View.setText(tmp);
            mLine3View = (TextView)mView.findViewById(R.id.line3_view);
            tmp = getString(R.string.download_fail_line3, BluetoothOppUtility.getStatusDescription(
                    this, mTransInfo.mStatus));
            mLine3View.setText(tmp);
            mLine5View = (TextView)mView.findViewById(R.id.line5_view);
            mLine5View.setVisibility(View.GONE);
        }
        if (BluetoothShare.isStatusError(mTransInfo.mStatus)) {
            mProgressTransfer.setVisibility(View.GONE);
            mPercentView.setVisibility(View.GONE);
        }
    }
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mWhichDialog == DIALOG_RECEIVE_COMPLETE_SUCCESS) {
                    BluetoothOppUtility.openReceivedFile(this, mTransInfo.mFileName,
                            mTransInfo.mFileType, mTransInfo.mTimeStamp, mUri);
                    BluetoothOppUtility.updateVisibilityToHidden(this, mUri);
                    ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                            .cancel(mTransInfo.mID);
                } else if (mWhichDialog == DIALOG_SEND_COMPLETE_FAIL) {
                    BluetoothOppUtility.updateVisibilityToHidden(this, mUri);
                    ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                            .cancel(mTransInfo.mID);
                    BluetoothOppUtility.retryTransfer(this, mTransInfo);
                    BluetoothDevice remoteDevice = mAdapter.getRemoteDevice(mTransInfo.mDestAddr);
                    Toast.makeText(
                            this,
                            this.getString(R.string.bt_toast_4, BluetoothOppManager.getInstance(
                                    this).getDeviceName(remoteDevice)), Toast.LENGTH_SHORT)
                            .show();
                } else if (mWhichDialog == DIALOG_SEND_COMPLETE_SUCCESS) {
                    BluetoothOppUtility.updateVisibilityToHidden(this, mUri);
                    ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                            .cancel(mTransInfo.mID);
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                if (mWhichDialog == DIALOG_RECEIVE_ONGOING || mWhichDialog == DIALOG_SEND_ONGOING) {
                    this.getContentResolver().delete(mUri, null, null);
                    String msg = "";
                    if (mWhichDialog == DIALOG_RECEIVE_ONGOING) {
                        msg = getString(R.string.bt_toast_3, mTransInfo.mDeviceName);
                    } else if (mWhichDialog == DIALOG_SEND_ONGOING) {
                        msg = getString(R.string.bt_toast_6, mTransInfo.mDeviceName);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                            .cancel(mTransInfo.mID);
                } else if (mWhichDialog == DIALOG_SEND_COMPLETE_FAIL) {
                    BluetoothOppUtility.updateVisibilityToHidden(this, mUri);
                }
                break;
        }
        finish();
    }
    private void updateProgressbar() {
        mTransInfo = BluetoothOppUtility.queryRecord(this, mUri);
        if (mTransInfo == null) {
            if (V) Log.e(TAG, "Error: Can not get data from db");
            return;
        }
        if (mTransInfo.mTotalBytes == 0) {
            mProgressTransfer.setMax(100);
        } else {
            mProgressTransfer.setMax(mTransInfo.mTotalBytes);
        }
        mProgressTransfer.setProgress(mTransInfo.mCurrentBytes);
        mPercentView.setText(BluetoothOppUtility.formatProgressText(mTransInfo.mTotalBytes,
                mTransInfo.mCurrentBytes));
        if (!mIsComplete && BluetoothShare.isStatusCompleted(mTransInfo.mStatus)
                && mNeedUpdateButton) {
            displayWhichDialog();
            updateButton();
            customizeViewContent();
        }
    }
    private void updateButton() {
        if (mWhichDialog == DIALOG_RECEIVE_COMPLETE_SUCCESS) {
            mAlert.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
            mAlert.getButton(DialogInterface.BUTTON_POSITIVE).setText(
                    getString(R.string.download_succ_ok));
        } else if (mWhichDialog == DIALOG_RECEIVE_COMPLETE_FAIL) {
            mAlert.setIcon(android.R.drawable.ic_dialog_alert);
            mAlert.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
            mAlert.getButton(DialogInterface.BUTTON_POSITIVE).setText(
                    getString(R.string.download_fail_ok));
        } else if (mWhichDialog == DIALOG_SEND_COMPLETE_SUCCESS) {
            mAlert.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
            mAlert.getButton(DialogInterface.BUTTON_POSITIVE).setText(
                    getString(R.string.upload_succ_ok));
        } else if (mWhichDialog == DIALOG_SEND_COMPLETE_FAIL) {
            mAlert.setIcon(android.R.drawable.ic_dialog_alert);
            mAlert.getButton(DialogInterface.BUTTON_POSITIVE).setText(
                    getString(R.string.upload_fail_ok));
            mAlert.getButton(DialogInterface.BUTTON_NEGATIVE).setText(
                    getString(R.string.upload_fail_cancel));
        }
    }
}
