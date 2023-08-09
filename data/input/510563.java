public class BluetoothOppLauncherActivity extends Activity {
    private static final String TAG = "BluetoothLauncherActivity";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_SEND) || action.equals(Intent.ACTION_SEND_MULTIPLE)) {
            if (action.equals(Intent.ACTION_SEND)) {
                String type = intent.getType();
                Uri stream = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
                CharSequence extra_text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
                if (stream != null && type != null) {
                    if (V) Log.v(TAG, "Get ACTION_SEND intent: Uri = " + stream + "; mimetype = "
                                + type);
                    BluetoothOppManager.getInstance(this).saveSendingFileInfo(type,
                            stream.toString());
                } else if (extra_text != null && type != null) {
                    if (V) Log.v(TAG, "Get ACTION_SEND intent with Extra_text = "
                                + extra_text.toString() + "; mimetype = " + type);
                    Uri fileUri = creatFileForSharedContent(this, extra_text);
                    if (fileUri != null) {
                        BluetoothOppManager.getInstance(this).saveSendingFileInfo(type,
                                fileUri.toString());
                    }
                } else {
                    Log.e(TAG, "type is null; or sending file URI is null");
                    finish();
                    return;
                }
            } else if (action.equals(Intent.ACTION_SEND_MULTIPLE)) {
                ArrayList<Uri> uris = new ArrayList<Uri>();
                String mimeType = intent.getType();
                uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (mimeType != null && uris != null) {
                    if (V) Log.v(TAG, "Get ACTION_SHARE_MULTIPLE intent: uris " + uris + "\n Type= "
                                + mimeType);
                    BluetoothOppManager.getInstance(this).saveSendingFileInfo(mimeType, uris);
                } else {
                    Log.e(TAG, "type is null; or sending files URIs are null");
                    finish();
                    return;
                }
            }
            if (isAirplaneModeOn()) {
                Intent in = new Intent(this, BluetoothOppBtErrorActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                in.putExtra("title", this.getString(R.string.airplane_error_title));
                in.putExtra("content", this.getString(R.string.airplane_error_msg));
                this.startActivity(in);
                finish();
                return;
            }
            if (!BluetoothOppManager.getInstance(this).isEnabled()) {
                if (V) Log.v(TAG, "Prepare Enable BT!! ");
                Intent in = new Intent(this, BluetoothOppBtEnableActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(in);
            } else {
                if (V) Log.v(TAG, "BT already enabled!! ");
                Intent in1 = new Intent(BluetoothDevicePicker.ACTION_LAUNCH);
                in1.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                in1.putExtra(BluetoothDevicePicker.EXTRA_NEED_AUTH, false);
                in1.putExtra(BluetoothDevicePicker.EXTRA_FILTER_TYPE,
                        BluetoothDevicePicker.FILTER_TYPE_TRANSFER);
                in1.putExtra(BluetoothDevicePicker.EXTRA_LAUNCH_PACKAGE,
                        Constants.THIS_PACKAGE_NAME);
                in1.putExtra(BluetoothDevicePicker.EXTRA_LAUNCH_CLASS,
                        BluetoothOppReceiver.class.getName());
                this.startActivity(in1);
            }
        } else if (action.equals(Constants.ACTION_OPEN)) {
            Uri uri = getIntent().getData();
            if (V) Log.v(TAG, "Get ACTION_OPEN intent: Uri = " + uri);
            Intent intent1 = new Intent();
            intent1.setAction(action);
            intent1.setClassName(Constants.THIS_PACKAGE_NAME, BluetoothOppReceiver.class.getName());
            intent1.setData(uri);
            this.sendBroadcast(intent1);
        }
        finish();
    }
    private final boolean isAirplaneModeOn() {
        return Settings.System.getInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON,
                0) == 1;
    }
    private Uri creatFileForSharedContent(Context context, CharSequence shareContent) {
        if (shareContent == null) {
            return null;
        }
        Uri fileUri = null;
        FileOutputStream outStream = null;
        try {
            String fileName = getString(R.string.bluetooth_share_file_name) + ".html";
            context.deleteFile(fileName);
            String uri = shareContent.toString();
            String content = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html;"
                + " charset=UTF-8\"/></head><body>" + "<a href=\"" + uri + "\">" + uri + "</a></p>"
                + "</body></html>";
            byte[] byteBuff = content.getBytes();
            outStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            if (outStream != null) {
                outStream.write(byteBuff, 0, byteBuff.length);
                fileUri = Uri.fromFile(new File(context.getFilesDir(), fileName));
                if (fileUri != null) {
                    if (D) Log.d(TAG, "Created one file for shared content: "
                            + fileUri.toString());
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.toString());
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileUri;
    }
}
