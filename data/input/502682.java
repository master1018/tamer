public class CorpusSelectionDialog extends Dialog {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.SelectSearchSourceDialog";
    private GridView mCorpusGrid;
    private ImageView mEditItems;
    private OnCorpusSelectedListener mListener;
    private Corpus mCorpus;
    private CorporaAdapter mAdapter;
    public CorpusSelectionDialog(Context context) {
        super(context, R.style.Theme_SelectSearchSource);
    }
    public void show(Corpus corpus) {
        mCorpus = corpus;
        show();
    }
    public void setOnCorpusSelectedListener(OnCorpusSelectedListener listener) {
        mListener = listener;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.corpus_selection_dialog);
        mCorpusGrid = (GridView) findViewById(R.id.corpus_grid);
        mCorpusGrid.setOnItemClickListener(new CorpusClickListener());
        mCorpusGrid.setFocusable(true);
        mEditItems = (ImageView) findViewById(R.id.corpus_edit_items);
        mEditItems.setOnClickListener(new CorpusEditListener());
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        window.setAttributes(lp);
        if (DBG) Log.d(TAG, "Window params: " + lp);
    }
    @Override
    protected void onStart() {
        super.onStart();
        CorporaAdapter adapter =
                CorporaAdapter.createGridAdapter(getViewFactory(), getCorpusRanker());
        setAdapter(adapter);
        mCorpusGrid.setSelection(adapter.getCorpusPosition(mCorpus));
    }
    @Override
    protected void onStop() {
        setAdapter(null);
        super.onStop();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        SearchSettings.addSearchSettingsMenuItem(getContext(), menu);
        return true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            cancel();
            return true;
        }
        return false;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = super.onKeyDown(keyCode, event);
        if (handled) {
            return handled;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (mEditItems.isFocused()) {
                cancel();
                return true;
            }
        }
        if (event.isPrintingKey()) {
            cancel();
            return true;
        }
        return false;
    }
    @Override
    public void onBackPressed() {
        SearchActivity searchActivity = getSearchActivity();
        if (searchActivity.startedIntoCorpusSelectionDialog()) {
            searchActivity.onBackPressed();
        }
        cancel();
    }
    private SearchActivity getSearchActivity() {
        return (SearchActivity) getOwnerActivity();
    }
    private void setAdapter(CorporaAdapter adapter) {
        if (adapter == mAdapter) return;
        if (mAdapter != null) mAdapter.close();
        mAdapter = adapter;
        mCorpusGrid.setAdapter(mAdapter);
    }
    private QsbApplication getQsbApplication() {
        return QsbApplication.get(getContext());
    }
    private CorpusRanker getCorpusRanker() {
        return getQsbApplication().getCorpusRanker();
    }
    private CorpusViewFactory getViewFactory() {
        return getQsbApplication().getCorpusViewFactory();
    }
    protected void selectCorpus(Corpus corpus) {
        dismiss();
        if (mListener != null) {
            String corpusName = corpus == null ? null : corpus.getName();
            mListener.onCorpusSelected(corpusName);
        }
    }
    private class CorpusClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Corpus corpus = (Corpus) parent.getItemAtPosition(position);
            if (DBG) Log.d(TAG, "Corpus selected: " + corpus);
            selectCorpus(corpus);
        }
    }
    private class CorpusEditListener implements View.OnClickListener {
        public void onClick(View v) {
            Intent intent = SearchSettings.getSearchableItemsIntent(getContext());
            getContext().startActivity(intent);
        }
    }
    public interface OnCorpusSelectedListener {
        void onCorpusSelected(String corpusName);
    }
}
