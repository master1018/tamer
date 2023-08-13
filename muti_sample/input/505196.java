public class InputTypeActivity extends Activity {
    private LinearLayout mLayout;
    private ScrollView mScrollView;
    private LayoutInflater mInflater;
    private ViewGroup mParent;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mScrollView = new ScrollView(this);
        mLayout = new LinearLayout(this);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mInflater = getLayoutInflater();
        mParent = mLayout;
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_NORMAL, 
                R.string.normal_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_NORMAL|EditorInfo.TYPE_TEXT_FLAG_CAP_CHARACTERS, 
                R.string.cap_chars_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_NORMAL|EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS, 
                R.string.cap_words_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_NORMAL|EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE, 
                R.string.multiline_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_NORMAL|EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES, 
                R.string.cap_sentences_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_NORMAL|EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE, 
                R.string.auto_complete_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_NORMAL|EditorInfo.TYPE_TEXT_FLAG_AUTO_CORRECT, 
                R.string.auto_correct_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_URI, 
        		R.string.uri_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, 
        		R.string.email_address_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_EMAIL_SUBJECT, 
                R.string.email_subject_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_LONG_MESSAGE, 
                R.string.email_content_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME, 
                R.string.person_name_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_POSTAL_ADDRESS, 
                R.string.postal_address_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_PASSWORD, 
                R.string.password_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_TEXT|EditorInfo.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT, 
                R.string.web_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_NUMBER|EditorInfo.TYPE_NUMBER_FLAG_SIGNED, 
                R.string.signed_number_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_NUMBER|EditorInfo.TYPE_NUMBER_FLAG_DECIMAL, 
                R.string.decimal_number_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_PHONE, 
                R.string.phone_number_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_DATETIME|EditorInfo.TYPE_DATETIME_VARIATION_NORMAL, 
                R.string.normal_datetime_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_DATETIME|EditorInfo.TYPE_DATETIME_VARIATION_DATE, 
                R.string.date_edit_text_label));
        mLayout.addView(buildEntryView(EditorInfo.TYPE_CLASS_DATETIME|EditorInfo.TYPE_DATETIME_VARIATION_TIME, 
                R.string.time_edit_text_label));
        mScrollView.addView(mLayout);
        setContentView(mScrollView);
    }
    private View buildEntryView(int inputType, int label) {
    	View view = mInflater.inflate(R.layout.sample_edit_text, mParent, false);
        EditText editText = (EditText) view.findViewById(R.id.data);
        editText.setInputType(inputType);
        TextView textView = (TextView) view.findViewById(R.id.label);
        textView.setText(label);
        return view;
    }
}
