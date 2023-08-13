public class RadioGroup1 extends Activity implements RadioGroup.OnCheckedChangeListener,
        View.OnClickListener {
    private TextView mChoice;
    private RadioGroup mRadioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radio_group_1);
        mRadioGroup = (RadioGroup) findViewById(R.id.menu);
        RadioButton newRadioButton = new RadioButton(this);
        newRadioButton.setText(R.string.radio_group_snack);
        newRadioButton.setId(R.id.snack);
        LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT);
        mRadioGroup.addView(newRadioButton, 0, layoutParams);
        String selection = getString(R.string.radio_group_selection);
        mRadioGroup.setOnCheckedChangeListener(this);
        mChoice = (TextView) findViewById(R.id.choice);
        mChoice.setText(selection + mRadioGroup.getCheckedRadioButtonId());
        Button clearButton = (Button) findViewById(R.id.clear);
        clearButton.setOnClickListener(this);
    }
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        String selection = getString(R.string.radio_group_selection);
        String none = getString(R.string.radio_group_none);
        mChoice.setText(selection +
                (checkedId == View.NO_ID ? none : checkedId));
    }
    public void onClick(View v) {
        mRadioGroup.clearCheck();
    }
}
