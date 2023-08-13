public class ReorderTwo extends Activity {
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.reorder_two);
        Button twoButton = (Button) findViewById(R.id.reorder_launch_three);
        twoButton.setOnClickListener(mClickListener);
    }
    private final OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(ReorderTwo.this, ReorderThree.class));
        }
    };
}