public class AppsCorpus extends SingleSourceCorpus {
    private static final String TAG = "QSB.AppsCorpus";
    private static final String APPS_CORPUS_NAME = "apps";
    public AppsCorpus(Context context, Config config, Source appsSource) {
        super(context, config, appsSource);
    }
    @Override
    public CharSequence getLabel() {
        return getContext().getText(R.string.corpus_label_apps);
    }
    @Override
    public CharSequence getHint() {
        return getContext().getText(R.string.corpus_hint_apps);
    }
    @Override
    public Drawable getCorpusIcon() {
        return getContext().getResources().getDrawable(R.drawable.corpus_icon_apps);
    }
    @Override
    public Uri getCorpusIconUri() {
        return Util.getResourceUri(getContext(), R.drawable.corpus_icon_apps);
    }
    @Override
    public String getName() {
        return APPS_CORPUS_NAME;
    }
    @Override
    public CharSequence getSettingsDescription() {
        return getContext().getText(R.string.corpus_description_apps);
    }
    @Override
    public Intent createSearchIntent(String query, Bundle appData) {
        Intent appSearchIntent = createAppSearchIntent(query, appData);
        if (appSearchIntent != null) {
            return appSearchIntent;
        } else {
            return super.createSearchIntent(query, appData);
        }
    }
    private Intent createAppSearchIntent(String query, Bundle appData) {
        ComponentName name = getComponentName(getContext(), R.string.apps_search_activity);
        if (name == null) return null;
        Intent intent = AbstractSource.createSourceSearchIntent(name, query, appData);
        if (intent == null) return null;
        ActivityInfo ai = intent.resolveActivityInfo(getContext().getPackageManager(), 0);
        if (ai != null) {
            return intent;
        } else {
            Log.w(TAG, "Can't find app search activity " + name);
            return null;
        }
    }
    private static ComponentName getComponentName(Context context, int res) {
        String nameStr = context.getString(res);
        if (TextUtils.isEmpty(nameStr)) {
            return null;
        } else {
            return ComponentName.unflattenFromString(nameStr);
        }
    }
}
