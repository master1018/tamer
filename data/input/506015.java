public class CorpusViewInflater implements CorpusViewFactory {
    private final Context mContext;
    public CorpusViewInflater(Context context) {
        mContext = context;
    }
    protected LayoutInflater getInflater() {
        return (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public CorpusView createGridCorpusView(ViewGroup parentViewType) {
        return inflateCorpusView(R.layout.corpus_grid_item, parentViewType);
    }
    public CorpusView createListCorpusView(ViewGroup parentViewType) {
        return inflateCorpusView(R.layout.corpus_list_item, parentViewType);
    }
    protected CorpusView inflateCorpusView(int res, ViewGroup parentViewType) {
        return (CorpusView) getInflater().inflate(res, parentViewType, false);
    }
    public String getGlobalSearchLabel() {
        return mContext.getString(R.string.corpus_label_global);
    }
    private int getGlobalSearchIconResource() {
        return R.drawable.search_app_icon;
    }
    public Drawable getGlobalSearchIcon() {
        return mContext.getResources().getDrawable(getGlobalSearchIconResource());
    }
    public Uri getGlobalSearchIconUri() {
        return Util.getResourceUri(mContext, getGlobalSearchIconResource());
    }
}
