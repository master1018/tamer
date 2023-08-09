public class EditResponseHelper implements DialogInterface.OnClickListener {
    private final Activity mParent;
    private int mWhichEvents = -1;
    private AlertDialog mAlertDialog;
    private DialogInterface.OnClickListener mDialogListener;
    public EditResponseHelper(Activity parent) {
        mParent = parent;
    }
    public void setOnClickListener(DialogInterface.OnClickListener listener) {
        mDialogListener = listener;
    }
    public int getWhichEvents() {
        return mWhichEvents;
    }
    public void onClick(DialogInterface dialog, int which) {
    }
    private DialogInterface.OnClickListener mListListener =
            new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            mWhichEvents = which;
            Button ok = mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            ok.setEnabled(true);
        }
    };
    public void showDialog(int whichEvents) {
        if (mDialogListener == null) {
            mDialogListener = this;
        }
        AlertDialog dialog = new AlertDialog.Builder(mParent)
                .setTitle(R.string.change_response_title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setSingleChoiceItems(R.array.change_response_labels, whichEvents,
                        mListListener)
                .setPositiveButton(android.R.string.ok, mDialogListener)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        mAlertDialog = dialog;
        if (whichEvents == -1) {
            Button ok = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            ok.setEnabled(false);
        }
    }
}
