public class PurgeableBitmap extends GraphicsActivity {
    private PurgeableBitmapView mView;
    private final RefreshHandler mRedrawHandler = new RefreshHandler();
    class RefreshHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int index = mView.update(this);
            if (index > 0) {
                showAlertDialog(getDialogMessage(true, index));
            } else if (index < 0){
                mView.invalidate();
                showAlertDialog(getDialogMessage(false, -index));
            } else {
              mView.invalidate();
            }
        }
        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new PurgeableBitmapView(this,  detectIfPurgeableRequest());
        mRedrawHandler.sleep(0);
        setContentView(mView);
    }
    private boolean detectIfPurgeableRequest() {
        PackageManager pm = getPackageManager();
        CharSequence labelSeq = null;
        try {
          ActivityInfo info = pm.getActivityInfo(this.getComponentName(),
              PackageManager.GET_META_DATA);
          labelSeq = info.loadLabel(pm);
        } catch (NameNotFoundException e) {
          e.printStackTrace();
          return false;
        }
        String[] components = labelSeq.toString().split("/");
        if (components[components.length - 1].equals("Purgeable")) {
            return true;
        } else {
            return false;
        }
    }
    private String getDialogMessage(boolean isOutOfMemory, int index) {
         StringBuilder sb = new StringBuilder();
         if (isOutOfMemory) {
             sb.append("Out of memery occurs when the ");
             sb.append(index);
             sb.append("th Bitmap is decoded.");
         } else {
             sb.append("Complete decoding ")
               .append(index)
               .append(" bitmaps without running out of memory.");
         }
         return sb.toString();
    }
    private void showAlertDialog(String message) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage(message)
             .setCancelable(false)
             .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                                 }
             });
      AlertDialog alert = builder.create();
      alert.show();
    }
}
