public class ReceiveResult extends Activity {
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_result);
        mResults = (TextView)findViewById(R.id.results);
        mResults.setText(mResults.getText(), TextView.BufferType.EDITABLE);
        Button getButton = (Button)findViewById(R.id.get);
        getButton.setOnClickListener(mGetListener);
    }
    @Override
	protected void onActivityResult(int requestCode, int resultCode,
		Intent data) {
        if (requestCode == GET_CODE) {
            Editable text = (Editable)mResults.getText();
            if (resultCode == RESULT_CANCELED) {
                text.append("(cancelled)");
            } else {
                text.append("(okay ");
                text.append(Integer.toString(resultCode));
                text.append(") ");
                if (data != null) {
                    text.append(data.getAction());
                }
            }
            text.append("\n");
        }
    }
    static final private int GET_CODE = 0;
    private OnClickListener mGetListener = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(ReceiveResult.this, SendResult.class);
            startActivityForResult(intent, GET_CODE);
        }
    };
    private TextView mResults;
}
