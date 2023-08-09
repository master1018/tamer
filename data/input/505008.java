public class BluetoothOppTransferAdapter extends ResourceCursorAdapter {
    private Context mContext;
    public BluetoothOppTransferAdapter(Context context, int layout, Cursor c) {
        super(context, layout, c);
        mContext = context;
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Resources r = context.getResources();
        ImageView iv = (ImageView)view.findViewById(R.id.transfer_icon);
        int status = cursor.getInt(cursor.getColumnIndexOrThrow(BluetoothShare.STATUS));
        int dir = cursor.getInt(cursor.getColumnIndexOrThrow(BluetoothShare.DIRECTION));
        if (BluetoothShare.isStatusError(status)) {
            iv.setImageResource(android.R.drawable.stat_notify_error);
        } else {
            if (dir == BluetoothShare.DIRECTION_OUTBOUND) {
                iv.setImageResource(android.R.drawable.stat_sys_upload_done);
            } else {
                iv.setImageResource(android.R.drawable.stat_sys_download_done);
            }
        }
        TextView tv = (TextView)view.findViewById(R.id.transfer_title);
        String title = cursor.getString(
                cursor.getColumnIndexOrThrow(BluetoothShare.FILENAME_HINT));
        if (title == null) {
            title = mContext.getString(R.string.unknown_file);
        }
        tv.setText(title);
        tv = (TextView)view.findViewById(R.id.targetdevice);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        int destinationColumnId = cursor.getColumnIndexOrThrow(BluetoothShare.DESTINATION);
        BluetoothDevice remoteDevice = adapter.getRemoteDevice(cursor
                .getString(destinationColumnId));
        String deviceName = BluetoothOppManager.getInstance(context).getDeviceName(remoteDevice);
        tv.setText(deviceName);
        long totalBytes = cursor.getLong(cursor.getColumnIndexOrThrow(BluetoothShare.TOTAL_BYTES));
        if (BluetoothShare.isStatusCompleted(status)) {
            tv = (TextView)view.findViewById(R.id.complete_text);
            tv.setVisibility(View.VISIBLE);
            if (BluetoothShare.isStatusError(status)) {
                tv.setText(BluetoothOppUtility.getStatusDescription(mContext, status));
            } else {
                String completeText;
                if (dir == BluetoothShare.DIRECTION_INBOUND) {
                    completeText = r.getString(R.string.download_success, Formatter.formatFileSize(
                            mContext, totalBytes));
                } else {
                    completeText = r.getString(R.string.upload_success, Formatter.formatFileSize(
                            mContext, totalBytes));
                }
                tv.setText(completeText);
            }
            int dateColumnId = cursor.getColumnIndexOrThrow(BluetoothShare.TIMESTAMP);
            long time = cursor.getLong(dateColumnId);
            Date d = new Date(time);
            CharSequence str = DateUtils.isToday(time) ? DateFormat.getTimeFormat(mContext).format(
                    d) : DateFormat.getDateFormat(mContext).format(d);
            tv = (TextView)view.findViewById(R.id.complete_date);
            tv.setVisibility(View.VISIBLE);
            tv.setText(str);
        }
    }
}
