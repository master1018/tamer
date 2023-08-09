public class VoiceSearch {
    private static final String TAG = "QSB.VoiceSearch";
    private final Context mContext;
    public VoiceSearch(Context context) {
        mContext = context;
    }
    protected Context getContext() {
        return mContext;
    }
    public boolean shouldShowVoiceSearch(Corpus corpus) {
        return corpusSupportsVoiceSearch(corpus) && isVoiceSearchAvailable();
    }
    protected boolean corpusSupportsVoiceSearch(Corpus corpus) {
        return (corpus == null || corpus.voiceSearchEnabled());
    }
    protected Intent createVoiceSearchIntent() {
        return new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
    }
    private ResolveInfo getResolveInfo() {
        Intent intent = createVoiceSearchIntent();
        ResolveInfo ri = mContext.getPackageManager().
                resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return ri;
    }
    public boolean isVoiceSearchAvailable() {
        return getResolveInfo() != null;
    }
    public Intent createVoiceWebSearchIntent(Bundle appData) {
        if (!isVoiceSearchAvailable()) return null;
        Intent intent = createVoiceSearchIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        if (appData != null) {
            intent.putExtra(SearchManager.APP_DATA, appData);
        }
        return intent;
    }
    public Intent createVoiceSearchHelpIntent() {
        return null;
    }
    public int getVersion() {
        ResolveInfo ri = getResolveInfo();
        if (ri == null) return 0;
        ComponentInfo ci = ri.activityInfo != null ? ri.activityInfo : ri.serviceInfo;
        try {
            return getContext().getPackageManager().getPackageInfo(ci.packageName, 0).versionCode;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Cannot find voice search package " + ci.packageName, e);
            return 0;
        }
    }
}
