public class BluetoothOppReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothOppReceiver";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, BluetoothOppService.class));
        } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            if (BluetoothAdapter.STATE_ON == intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                if (V) Log.v(TAG, "Received BLUETOOTH_STATE_CHANGED_ACTION, BLUETOOTH_STATE_ON");
                context.startService(new Intent(context, BluetoothOppService.class));
                synchronized (this) {
                    if (BluetoothOppManager.getInstance(context).mSendingFlag) {
                        BluetoothOppManager.getInstance(context).mSendingFlag = false;
                        Intent in1 = new Intent(BluetoothDevicePicker.ACTION_LAUNCH);
                        in1.putExtra(BluetoothDevicePicker.EXTRA_NEED_AUTH, false);
                        in1.putExtra(BluetoothDevicePicker.EXTRA_FILTER_TYPE,
                                BluetoothDevicePicker.FILTER_TYPE_TRANSFER);
                        in1.putExtra(BluetoothDevicePicker.EXTRA_LAUNCH_PACKAGE,
                                Constants.THIS_PACKAGE_NAME);
                        in1.putExtra(BluetoothDevicePicker.EXTRA_LAUNCH_CLASS,
                                BluetoothOppReceiver.class.getName());
                        in1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(in1);
                    }
                }
            }
        } else if (action.equals(BluetoothDevicePicker.ACTION_DEVICE_SELECTED)) {
            BluetoothOppManager mOppManager = BluetoothOppManager.getInstance(context);
            BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (V) Log.v(TAG, "Received BT device selected intent, bt device: " + remoteDevice);
            mOppManager.startTransfer(remoteDevice);
            String deviceName = mOppManager.getDeviceName(remoteDevice);
            String toastMsg;
            int batchSize = mOppManager.getBatchSize();
            if (mOppManager.mMultipleFlag) {
                toastMsg = context.getString(R.string.bt_toast_5, Integer.toString(batchSize),
                        deviceName);
            } else {
                toastMsg = context.getString(R.string.bt_toast_4, deviceName);
            }
            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
        } else if (action.equals(Constants.ACTION_INCOMING_FILE_CONFIRM)) {
            if (V) Log.v(TAG, "Receiver ACTION_INCOMING_FILE_CONFIRM");
            Uri uri = intent.getData();
            Intent in = new Intent(context, BluetoothOppIncomingFileConfirmActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            in.setData(uri);
            context.startActivity(in);
            NotificationManager notMgr = (NotificationManager)context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            if (notMgr != null) {
                notMgr.cancel((int)ContentUris.parseId(intent.getData()));
                if (V) Log.v(TAG, "notMgr.cancel called");
            }
        } else if (action.equals(BluetoothShare.INCOMING_FILE_CONFIRMATION_REQUEST_ACTION)) {
            if (V) Log.v(TAG, "Receiver INCOMING_FILE_NOTIFICATION");
            Toast.makeText(context, context.getString(R.string.incoming_file_toast_msg),
                    Toast.LENGTH_SHORT).show();
        } else if (action.equals(Constants.ACTION_OPEN) || action.equals(Constants.ACTION_LIST)) {
            if (V) {
                if (action.equals(Constants.ACTION_OPEN)) {
                    Log.v(TAG, "Receiver open for " + intent.getData());
                } else {
                    Log.v(TAG, "Receiver list for " + intent.getData());
                }
            }
            BluetoothOppTransferInfo transInfo = new BluetoothOppTransferInfo();
            Uri uri = intent.getData();
            transInfo = BluetoothOppUtility.queryRecord(context, uri);
            if (transInfo == null) {
                Log.e(TAG, "Error: Can not get data from db");
                return;
            }
            if (transInfo.mDirection == BluetoothShare.DIRECTION_INBOUND
                    && BluetoothShare.isStatusSuccess(transInfo.mStatus)) {
                BluetoothOppUtility.openReceivedFile(context, transInfo.mFileName,
                        transInfo.mFileType, transInfo.mTimeStamp, uri);
                BluetoothOppUtility.updateVisibilityToHidden(context, uri);
            } else {
                Intent in = new Intent(context, BluetoothOppTransferActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                in.setData(uri);
                context.startActivity(in);
            }
            NotificationManager notMgr = (NotificationManager)context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            if (notMgr != null) {
                notMgr.cancel((int)ContentUris.parseId(intent.getData()));
                if (V) Log.v(TAG, "notMgr.cancel called");
                }
        } else if (action.equals(Constants.ACTION_OPEN_OUTBOUND_TRANSFER)) {
            if (V) Log.v(TAG, "Received ACTION_OPEN_OUTBOUND_TRANSFER.");
            Intent in = new Intent(context, BluetoothOppTransferHistory.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            in.putExtra("direction", BluetoothShare.DIRECTION_OUTBOUND);
            context.startActivity(in);
        } else if (action.equals(Constants.ACTION_OPEN_INBOUND_TRANSFER)) {
            if (V) Log.v(TAG, "Received ACTION_OPEN_INBOUND_TRANSFER.");
            Intent in = new Intent(context, BluetoothOppTransferHistory.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            in.putExtra("direction", BluetoothShare.DIRECTION_INBOUND);
            context.startActivity(in);
        } else if (action.equals(Constants.ACTION_HIDE)) {
            if (V) Log.v(TAG, "Receiver hide for " + intent.getData());
            Cursor cursor = context.getContentResolver().query(intent.getData(), null, null, null,
                    null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int statusColumn = cursor.getColumnIndexOrThrow(BluetoothShare.STATUS);
                    int status = cursor.getInt(statusColumn);
                    int visibilityColumn = cursor.getColumnIndexOrThrow(BluetoothShare.VISIBILITY);
                    int visibility = cursor.getInt(visibilityColumn);
                    int userConfirmationColumn = cursor
                            .getColumnIndexOrThrow(BluetoothShare.USER_CONFIRMATION);
                    int userConfirmation = cursor.getInt(userConfirmationColumn);
                    if (((userConfirmation == BluetoothShare.USER_CONFIRMATION_PENDING))
                            && visibility == BluetoothShare.VISIBILITY_VISIBLE) {
                        ContentValues values = new ContentValues();
                        values.put(BluetoothShare.VISIBILITY, BluetoothShare.VISIBILITY_HIDDEN);
                        context.getContentResolver().update(intent.getData(), values, null, null);
                        if (V) Log.v(TAG, "Action_hide received and db updated");
                        }
                }
                cursor.close();
            }
        } else if (action.equals(Constants.ACTION_COMPLETE_HIDE)) {
            if (V) Log.v(TAG, "Receiver ACTION_COMPLETE_HIDE");
            ContentValues updateValues = new ContentValues();
            updateValues.put(BluetoothShare.VISIBILITY, BluetoothShare.VISIBILITY_HIDDEN);
            context.getContentResolver().update(BluetoothShare.CONTENT_URI, updateValues,
                    BluetoothOppNotification.WHERE_COMPLETED, null);
        } else if (action.equals(BluetoothShare.TRANSFER_COMPLETED_ACTION)) {
            if (V) Log.v(TAG, "Receiver Transfer Complete Intent for " + intent.getData());
            String toastMsg = null;
            BluetoothOppTransferInfo transInfo = new BluetoothOppTransferInfo();
            transInfo = BluetoothOppUtility.queryRecord(context, intent.getData());
            if (transInfo == null) {
                Log.e(TAG, "Error: Can not get data from db");
                return;
            }
            if (BluetoothShare.isStatusSuccess(transInfo.mStatus)) {
                if (transInfo.mDirection == BluetoothShare.DIRECTION_OUTBOUND) {
                    toastMsg = context.getString(R.string.notification_sent, transInfo.mFileName);
                } else if (transInfo.mDirection == BluetoothShare.DIRECTION_INBOUND) {
                    toastMsg = context.getString(R.string.notification_received,
                            transInfo.mFileName);
                }
            } else if (BluetoothShare.isStatusError(transInfo.mStatus)) {
                if (transInfo.mDirection == BluetoothShare.DIRECTION_OUTBOUND) {
                    toastMsg = context.getString(R.string.notification_sent_fail,
                            transInfo.mFileName);
                } else if (transInfo.mDirection == BluetoothShare.DIRECTION_INBOUND) {
                    toastMsg = context.getString(R.string.download_fail_line1);
                }
            }
            if (V) Log.v(TAG, "Toast msg == " + toastMsg);
            if (toastMsg != null) {
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
