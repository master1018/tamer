public class ReorderFour extends Activity {
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.reorder_four);
        Button twoButton = (Button) findViewById(R.id.reorder_second_to_front);
        twoButton.setOnClickListener(mClickListener);
    }
    private final OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(ReorderFour.this, ReorderTwo.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    };
}