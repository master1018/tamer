public class ReorderThree extends Activity {
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.reorder_three);
        Button twoButton = (Button) findViewById(R.id.reorder_launch_four);
        twoButton.setOnClickListener(mClickListener);
    }
    private final OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(ReorderThree.this, ReorderFour.class));
        }
    };
}