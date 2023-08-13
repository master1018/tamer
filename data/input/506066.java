public abstract class ChoiceActivity extends Activity {
    protected TextView mTitleView;
    protected ListView mChoicesView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choice_activity);
        mTitleView = (TextView) findViewById(R.id.alertTitle);
        mChoicesView = (ListView) findViewById(R.id.list);
    }
    public void setHeading(int titleRes) {
        mTitleView.setText(titleRes);
    }
    public void setHeading(CharSequence title) {
        mTitleView.setText(title);
    }
    public void setAdapter(ListAdapter adapter) {
        mChoicesView.setAdapter(adapter);
    }
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mChoicesView.setOnItemClickListener(listener);
        mChoicesView.setFocusable(true);
    }
}
