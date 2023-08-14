public class AutoCompleteTextViewSimple extends Activity 
        implements OnItemClickListener, OnItemSelectedListener {
    private final String LOG_TAG = "AutoCompleteTextViewSimple";
    private AutoCompleteTextView mTextView;
    public boolean mItemClickCalled;
    public int mItemClickPosition;
    public boolean mItemSelectedCalled;
    public int mItemSelectedPosition;
    public boolean mNothingSelectedCalled;
    @Override
    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.autocompletetextview_simple);
        mTextView = (AutoCompleteTextView) findViewById(R.id.autocompletetextview1);
        mTextView.setOnItemClickListener(this);
        mTextView.setOnItemSelectedListener(this);
        resetItemListeners();
        setStringAdapter(5, "a");
    }
    public AutoCompleteTextView getTextView() {
        return mTextView;
    }
    public void setStringAdapter(int numSuggestions, String prefix) {
        String[] strings = new String[numSuggestions];
        for (int i = 0; i < numSuggestions; ++i) {
            strings[i] = prefix + String.valueOf(i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, strings);
        mTextView.setAdapter(adapter);
    }
    public void resetItemListeners() {
        mItemClickCalled = false;
        mItemClickPosition = -1;
        mItemSelectedCalled = false;
        mItemSelectedPosition = -1;
        mNothingSelectedCalled = false;
    }
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(LOG_TAG, "onItemClick() position " + position);
        mItemClickCalled = true;
        mItemClickPosition = position;
    }
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(LOG_TAG, "onItemSelected() position " + position);
        mItemSelectedCalled = true;
        mItemSelectedPosition = position;
    }
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(LOG_TAG, "onNothingSelected()");
        mNothingSelectedCalled = true;
    }
}
