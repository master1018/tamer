public class VoiceInputLogger {
    private static final String TAG = VoiceInputLogger.class.getSimpleName();
    private static VoiceInputLogger sVoiceInputLogger;
    private final Context mContext;
    private final Intent mBaseIntent;
    public static synchronized VoiceInputLogger getLogger(Context contextHint) {
        if (sVoiceInputLogger == null) {
            sVoiceInputLogger = new VoiceInputLogger(contextHint);
        }
        return sVoiceInputLogger;
    }
    public VoiceInputLogger(Context context) {
        mContext = context;
        mBaseIntent = new Intent(LoggingEvents.ACTION_LOG_EVENT);
        mBaseIntent.putExtra(LoggingEvents.EXTRA_APP_NAME, LoggingEvents.VoiceIme.APP_NAME);
    }
    private Intent newLoggingBroadcast(int event) {
        Intent i = new Intent(mBaseIntent);
        i.putExtra(LoggingEvents.EXTRA_EVENT, event);
        return i;
    }
    public void flush() {
        Intent i = new Intent(mBaseIntent);
        i.putExtra(LoggingEvents.EXTRA_FLUSH, true);
        mContext.sendBroadcast(i);
    }
    public void keyboardWarningDialogShown() {
        mContext.sendBroadcast(newLoggingBroadcast(
                LoggingEvents.VoiceIme.KEYBOARD_WARNING_DIALOG_SHOWN));
    }
    public void keyboardWarningDialogDismissed() {
        mContext.sendBroadcast(newLoggingBroadcast(
                LoggingEvents.VoiceIme.KEYBOARD_WARNING_DIALOG_DISMISSED));
    }
    public void keyboardWarningDialogOk() {
        mContext.sendBroadcast(newLoggingBroadcast(
                LoggingEvents.VoiceIme.KEYBOARD_WARNING_DIALOG_OK));
    }
    public void keyboardWarningDialogCancel() {
        mContext.sendBroadcast(newLoggingBroadcast(
                LoggingEvents.VoiceIme.KEYBOARD_WARNING_DIALOG_CANCEL));
    }
    public void settingsWarningDialogShown() {
        mContext.sendBroadcast(newLoggingBroadcast(
                LoggingEvents.VoiceIme.SETTINGS_WARNING_DIALOG_SHOWN));
    }
    public void settingsWarningDialogDismissed() {
        mContext.sendBroadcast(newLoggingBroadcast(
                LoggingEvents.VoiceIme.SETTINGS_WARNING_DIALOG_DISMISSED));
    }
    public void settingsWarningDialogOk() {
        mContext.sendBroadcast(newLoggingBroadcast(
                LoggingEvents.VoiceIme.SETTINGS_WARNING_DIALOG_OK));
    }
    public void settingsWarningDialogCancel() {
        mContext.sendBroadcast(newLoggingBroadcast(
                LoggingEvents.VoiceIme.SETTINGS_WARNING_DIALOG_CANCEL));
    }
    public void swipeHintDisplayed() {
        mContext.sendBroadcast(newLoggingBroadcast(LoggingEvents.VoiceIme.SWIPE_HINT_DISPLAYED));
    }
    public void cancelDuringListening() {
        mContext.sendBroadcast(newLoggingBroadcast(LoggingEvents.VoiceIme.CANCEL_DURING_LISTENING));
    }
    public void cancelDuringWorking() {
        mContext.sendBroadcast(newLoggingBroadcast(LoggingEvents.VoiceIme.CANCEL_DURING_WORKING));
    }
    public void cancelDuringError() {
        mContext.sendBroadcast(newLoggingBroadcast(LoggingEvents.VoiceIme.CANCEL_DURING_ERROR));
    }
    public void punctuationHintDisplayed() {
        mContext.sendBroadcast(newLoggingBroadcast(
                LoggingEvents.VoiceIme.PUNCTUATION_HINT_DISPLAYED));
    }
    public void error(int code) {
        Intent i = newLoggingBroadcast(LoggingEvents.VoiceIme.ERROR);
        i.putExtra(LoggingEvents.VoiceIme.EXTRA_ERROR_CODE, code);
        mContext.sendBroadcast(i);
    }
    public void start(String locale, boolean swipe) {
        Intent i = newLoggingBroadcast(LoggingEvents.VoiceIme.START);
        i.putExtra(LoggingEvents.VoiceIme.EXTRA_START_LOCALE, locale);
        i.putExtra(LoggingEvents.VoiceIme.EXTRA_START_SWIPE, swipe);
        i.putExtra(LoggingEvents.EXTRA_TIMESTAMP, System.currentTimeMillis());
        mContext.sendBroadcast(i);
    }
    public void voiceInputDelivered(int length) {
        Intent i = newLoggingBroadcast(LoggingEvents.VoiceIme.VOICE_INPUT_DELIVERED);
        i.putExtra(LoggingEvents.VoiceIme.EXTRA_TEXT_MODIFIED_LENGTH, length);
        mContext.sendBroadcast(i);
    }
    public void textModifiedByTypingInsertion(int length) {
        Intent i = newLoggingBroadcast(LoggingEvents.VoiceIme.TEXT_MODIFIED);
        i.putExtra(LoggingEvents.VoiceIme.EXTRA_TEXT_MODIFIED_LENGTH, length);
        i.putExtra(LoggingEvents.VoiceIme.EXTRA_TEXT_MODIFIED_TYPE,
                LoggingEvents.VoiceIme.TEXT_MODIFIED_TYPE_TYPING_INSERTION);
        mContext.sendBroadcast(i);
    }
    public void textModifiedByTypingInsertionPunctuation(int length) {
        Intent i = newLoggingBroadcast(LoggingEvents.VoiceIme.TEXT_MODIFIED);
        i.putExtra(LoggingEvents.VoiceIme.EXTRA_TEXT_MODIFIED_LENGTH, length);
        i.putExtra(LoggingEvents.VoiceIme.EXTRA_TEXT_MODIFIED_TYPE,
                LoggingEvents.VoiceIme.TEXT_MODIFIED_TYPE_TYPING_INSERTION_PUNCTUATION);
        mContext.sendBroadcast(i);
    }
    public void textModifiedByTypingDeletion(int length) {
        Intent i = newLoggingBroadcast(LoggingEvents.VoiceIme.TEXT_MODIFIED);
        i.putExtra(LoggingEvents.VoiceIme.EXTRA_TEXT_MODIFIED_LENGTH, length);
        i.putExtra(LoggingEvents.VoiceIme.EXTRA_TEXT_MODIFIED_TYPE,
                LoggingEvents.VoiceIme.TEXT_MODIFIED_TYPE_TYPING_DELETION);
        mContext.sendBroadcast(i);
    }
    public void textModifiedByChooseSuggestion(int length) {
        Intent i = newLoggingBroadcast(LoggingEvents.VoiceIme.TEXT_MODIFIED);
        i.putExtra(LoggingEvents.VoiceIme.EXTRA_TEXT_MODIFIED_LENGTH, length);
        i.putExtra(LoggingEvents.VoiceIme.EXTRA_TEXT_MODIFIED_TYPE,
                LoggingEvents.VoiceIme.TEXT_MODIFIED_TYPE_CHOOSE_SUGGESTION);
        mContext.sendBroadcast(i);
    }
    public void nBestChoose(int index) {
        Intent i = newLoggingBroadcast(LoggingEvents.VoiceIme.N_BEST_CHOOSE);
        i.putExtra(LoggingEvents.VoiceIme.EXTRA_N_BEST_CHOOSE_INDEX, index);
        mContext.sendBroadcast(i);
    }
    public void inputEnded() {
        mContext.sendBroadcast(newLoggingBroadcast(LoggingEvents.VoiceIme.INPUT_ENDED));
    }
    public void voiceInputSettingEnabled() {
        mContext.sendBroadcast(newLoggingBroadcast(
                LoggingEvents.VoiceIme.VOICE_INPUT_SETTING_ENABLED));
    }
    public void voiceInputSettingDisabled() {
        mContext.sendBroadcast(newLoggingBroadcast(
                LoggingEvents.VoiceIme.VOICE_INPUT_SETTING_DISABLED));
    }
}
