public class CdmaDisplayInfo {
    private static final String LOG_TAG = "CdmaDisplayInfo";
    private static final boolean DBG = (SystemProperties.getInt("ro.debuggable", 0) == 1);
    private static AlertDialog sDisplayInfoDialog = null;
    public static void displayInfoRecord(Context context, String infoMsg) {
        if (DBG) log("displayInfoRecord: infoMsg=" + infoMsg);
        if (sDisplayInfoDialog != null) {
            sDisplayInfoDialog.dismiss();
        }
        sDisplayInfoDialog = new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(context.getText(R.string.network_message))
                .setMessage(infoMsg)
                .setCancelable(true)
                .create();
        sDisplayInfoDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        sDisplayInfoDialog.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        sDisplayInfoDialog.show();
        PhoneApp.getInstance().wakeUpScreen();
    }
    public static void dismissDisplayInfoRecord() {
        if (DBG) log("Dissmissing Display Info Record...");
        if (sDisplayInfoDialog != null) {
            sDisplayInfoDialog.dismiss();
            sDisplayInfoDialog = null;
        }
    }
    private static void log(String msg) {
        Log.d(LOG_TAG, "[CdmaDisplayInfo] " + msg);
    }
}
