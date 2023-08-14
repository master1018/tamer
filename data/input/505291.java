public class ImRingtonePreference extends RingtonePreference {
    private long mProviderId;
    public ImRingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        Intent intent = ((Activity)context).getIntent();
        mProviderId = intent.getLongExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, -1);
        if (mProviderId < 0) {
            Log.e(ImApp.LOG_TAG,"ImRingtonePreference intent requires provider id extra");
            throw new RuntimeException("ImRingtonePreference must be created with an provider id");
        }
    }
    @Override
    protected Uri onRestoreRingtone() {
        final Imps.ProviderSettings.QueryMap settings = new Imps.ProviderSettings.QueryMap(
                getContext().getContentResolver(), mProviderId, 
                false , null );
        String uri = settings.getRingtoneURI();
        if (Log.isLoggable(ImApp.LOG_TAG, Log.VERBOSE)) {
            Log.v(ImApp.LOG_TAG, "onRestoreRingtone() finds uri=" + uri + " key=" + getKey());
        }
        if (TextUtils.isEmpty(uri)) {
            return null;
        }
        Uri result = Uri.parse(uri);
        settings.close();
        return result;
    }
    @Override
    protected void onSaveRingtone(Uri ringtoneUri) {
        final Imps.ProviderSettings.QueryMap settings = new Imps.ProviderSettings.QueryMap(
                getContext().getContentResolver(), mProviderId, 
               false , null );
        settings.setRingtoneURI(ringtoneUri == null ? "" : ringtoneUri.toString());
        settings.close();
    }
}
