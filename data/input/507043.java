public class SpecialCharSequenceMgr {
    private static final String TAG = PhoneApp.LOG_TAG;
    private static final boolean DBG = false;
    private static final String MMI_IMEI_DISPLAY = "*#06#";
    private SpecialCharSequenceMgr() {
    }
    static boolean handleChars(Context context, String input) {
        return handleChars(context, input, null);
    }
    static boolean handleChars(Context context,
                               String input,
                               Activity pukInputActivity) {
        String dialString = PhoneNumberUtils.stripSeparators(input);
        if (handleIMEIDisplay(context, dialString)
            || handlePinEntry(context, dialString, pukInputActivity)
            || handleAdnEntry(context, dialString)
            || handleSecretCode(context, dialString)) {
            return true;
        }
        return false;
    }
    static boolean handleCharsForLockedDevice(Context context,
                                              String input,
                                              Activity pukInputActivity) {
        String dialString = PhoneNumberUtils.stripSeparators(input);
        if (handlePinEntry(context, dialString, pukInputActivity)) {
            return true;
        }
        return false;
    }
    static private boolean handleSecretCode(Context context, String input) {
        int len = input.length();
        if (len > 8 && input.startsWith("*#*#") && input.endsWith("#*#*")) {
            Intent intent = new Intent(Intents.SECRET_CODE_ACTION,
                    Uri.parse("android_secret_code:
            context.sendBroadcast(intent);
            return true;
        }
        return false;
    }
    static private boolean handleAdnEntry(Context context, String input) {
        if (PhoneApp.getInstance().getKeyguardManager().inKeyguardRestrictedInputMode()) {
            return false;
        }
        int len = input.length();
        if ((len > 1) && (len < 5) && (input.endsWith("#"))) {
            try {
                int index = Integer.parseInt(input.substring(0, len-1));
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setClassName("com.android.phone",
                                    "com.android.phone.SimContacts");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("index", index);
                PhoneApp.getInstance().startActivity(intent);
                return true;
            } catch (NumberFormatException ex) {}
        }
        return false;
    }
    static private boolean handlePinEntry(Context context, String input,
                                          Activity pukInputActivity) {
        if ((input.startsWith("**04") || input.startsWith("**05"))
                && input.endsWith("#")) {
            PhoneApp app = PhoneApp.getInstance();
            boolean isMMIHandled = app.phone.handlePinMmi(input);
            if (isMMIHandled && input.startsWith("**05")) {
                app.setPukEntryActivity(pukInputActivity);
            }
            return isMMIHandled;
        }
        return false;
    }
    static private boolean handleIMEIDisplay(Context context,
                                             String input) {
        if (input.equals(MMI_IMEI_DISPLAY)) {
            int phoneType = PhoneApp.getInstance().phone.getPhoneType();
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                showMEIDPanel(context);
                return true;
            } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                showIMEIPanel(context);
                return true;
            }
        }
        return false;
    }
    static private void showIMEIPanel(Context context) {
        if (DBG) log("showIMEIPanel");
        String imeiStr = PhoneFactory.getDefaultPhone().getDeviceId();
        AlertDialog alert = new AlertDialog.Builder(context)
                .setTitle(R.string.imei)
                .setMessage(imeiStr)
                .setPositiveButton(R.string.ok, null)
                .setCancelable(false)
                .show();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
    }
    static private void showMEIDPanel(Context context) {
        if (DBG) log("showMEIDPanel");
        String meidStr = PhoneFactory.getDefaultPhone().getDeviceId();
        AlertDialog alert = new AlertDialog.Builder(context)
                .setTitle(R.string.meid)
                .setMessage(meidStr)
                .setPositiveButton(R.string.ok, null)
                .setCancelable(false)
                .show();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
    }
    private static void log(String msg) {
        Log.d(TAG, "[SpecialCharSequenceMgr] " + msg);
    }
}
