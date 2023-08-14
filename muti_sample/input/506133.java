public class KindSectionView extends LinearLayout implements OnClickListener, EditorListener {
    private static final String TAG = "KindSectionView";
    private LayoutInflater mInflater;
    private ViewGroup mEditors;
    private View mAdd;
    private ImageView mAddPlusButton;
    private TextView mTitle;
    private DataKind mKind;
    private EntityDelta mState;
    private boolean mReadOnly;
    private ViewIdGenerator mViewIdGenerator;
    public KindSectionView(Context context) {
        super(context);
    }
    public KindSectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onFinishInflate() {
        mInflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        setDrawingCacheEnabled(true);
        setAlwaysDrawnWithCacheEnabled(true);
        mEditors = (ViewGroup)findViewById(R.id.kind_editors);
        mAdd = findViewById(R.id.kind_header);
        mAdd.setOnClickListener(this);
        mAddPlusButton = (ImageView) findViewById(R.id.kind_plus);
        mTitle = (TextView)findViewById(R.id.kind_title);
    }
    public void onDeleted(Editor editor) {
        this.updateAddEnabled();
        this.updateEditorsVisible();
    }
    public void onRequest(int request) {
    }
    public void setState(DataKind kind, EntityDelta state, boolean readOnly, ViewIdGenerator vig) {
        mKind = kind;
        mState = state;
        mReadOnly = readOnly;
        mViewIdGenerator = vig;
        setId(mViewIdGenerator.getId(state, kind, null, ViewIdGenerator.NO_VIEW_INDEX));
        mTitle.setText(kind.titleRes);
        mAddPlusButton.setVisibility(mKind.isList ? View.VISIBLE : View.GONE);
        this.rebuildFromState();
        this.updateAddEnabled();
        this.updateEditorsVisible();
    }
    public boolean isAnyEditorFilledOut() {
        if (mState == null) {
            return false;
        }
        if (!mState.hasMimeEntries(mKind.mimeType)) {
            return false;
        }
        int editorCount = mEditors.getChildCount();
        for (int i = 0; i < editorCount; i++) {
            GenericEditorView editorView = (GenericEditorView) mEditors.getChildAt(i);
            if (editorView.isAnyFieldFilledOut()) {
                return true;
            }
        }
        return false;
    }
    public void rebuildFromState() {
        mEditors.removeAllViews();
        boolean hasEntries = mState.hasMimeEntries(mKind.mimeType);
        if (!mKind.isList) {
            if (hasEntries) {
                for (ValuesDelta entry : mState.getMimeEntries(mKind.mimeType)) {
                    if (!entry.isVisible()) {
                        hasEntries = false;
                        break;
                    }
                }
            }
            if (!hasEntries) {
                EntityModifier.insertChild(mState, mKind);
                hasEntries = true;
            }
        }
        if (hasEntries) {
            int entryIndex = 0;
            for (ValuesDelta entry : mState.getMimeEntries(mKind.mimeType)) {
                if (!entry.isVisible()) continue;
                final GenericEditorView editor = (GenericEditorView)mInflater.inflate(
                        R.layout.item_generic_editor, mEditors, false);
                editor.setValues(mKind, entry, mState, mReadOnly, mViewIdGenerator);
                editor.mDelete.setVisibility((mKind.isList || (entryIndex != 0))
                        ? View.VISIBLE : View.GONE);
                editor.setEditorListener(this);
                mEditors.addView(editor);
                entryIndex++;
            }
        }
    }
    protected void updateEditorsVisible() {
        final boolean hasChildren = mEditors.getChildCount() > 0;
        mEditors.setVisibility(hasChildren ? View.VISIBLE : View.GONE);
    }
    protected void updateAddEnabled() {
        final boolean canInsert = EntityModifier.canInsert(mState, mKind);
        final boolean isEnabled = !mReadOnly && canInsert;
        mAdd.setEnabled(isEnabled);
    }
    public void onClick(View v) {
        if (!mKind.isList)
            return;
        final ValuesDelta newValues = EntityModifier.insertChild(mState, mKind);
        this.rebuildFromState();
        this.updateAddEnabled();
        this.updateEditorsVisible();
        final int newFieldId = mViewIdGenerator.getId(mState, mKind, newValues, 0);
        final View newField = findViewById(newFieldId);
        if (newField != null) {
            newField.requestFocus();
        }
    }
}
