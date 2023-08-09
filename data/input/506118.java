public class CharacterPickerDialog extends Dialog
        implements OnItemClickListener, OnClickListener {
    private View mView;
    private Editable mText;
    private String mOptions;
    private boolean mInsert;
    private LayoutInflater mInflater;
    private Button mCancelButton;
    public CharacterPickerDialog(Context context, View view,
                                 Editable text, String options,
                                 boolean insert) {
        super(context, com.android.internal.R.style.Theme_Panel);
        mView = view;
        mText = text;
        mOptions = options;
        mInsert = insert;
        mInflater = LayoutInflater.from(context);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.token = mView.getApplicationWindowToken();
        params.type = params.TYPE_APPLICATION_ATTACHED_DIALOG;
        params.flags = params.flags | Window.FEATURE_NO_TITLE;
        setContentView(R.layout.character_picker);
        GridView grid = (GridView) findViewById(R.id.characterPicker);
        grid.setAdapter(new OptionsAdapter(getContext()));
        grid.setOnItemClickListener(this);
        mCancelButton = (Button) findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(this);
    }
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        String result = String.valueOf(mOptions.charAt(position));
        replaceCharacterAndClose(result);
    }
    private void replaceCharacterAndClose(CharSequence replace) {
        int selEnd = Selection.getSelectionEnd(mText);
        if (mInsert || selEnd == 0) {
            mText.insert(selEnd, replace);
        } else {
            mText.replace(selEnd - 1, selEnd, replace);
        }
        dismiss();
    }
    public void onClick(View v) {
        if (v == mCancelButton) {
            dismiss();
        } else if (v instanceof Button) {
            CharSequence result = ((Button) v).getText();
            replaceCharacterAndClose(result);
        }
    }
    private class OptionsAdapter extends BaseAdapter {
        public OptionsAdapter(Context context) {
            super();
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            Button b = (Button)
                mInflater.inflate(R.layout.character_picker_button, null);
            b.setText(String.valueOf(mOptions.charAt(position)));
            b.setOnClickListener(CharacterPickerDialog.this);
            return b;
        }
        public final int getCount() {
            return mOptions.length();
        }
        public final Object getItem(int position) {
            return String.valueOf(mOptions.charAt(position));
        }
        public final long getItemId(int position) {
            return position;
        }
    }
}
