public class LogTextBox1 extends Activity {
    private LogTextBox mText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_text_box_1);
        mText = (LogTextBox) findViewById(R.id.text);
        Button addButton = (Button) findViewById(R.id.add);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mText.append("This is a test\n");
            } });
    }
}
