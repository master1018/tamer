public class ReorderOnLaunch extends Activity {
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.reorder_on_launch);
        Button twoButton = (Button) findViewById(R.id.reorder_launch_two);
        twoButton.setOnClickListener(mClickListener);
    }
    private final OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(ReorderOnLaunch.this, ReorderTwo.class));
        }
    };
}