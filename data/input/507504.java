public class FocusAfterRemoval extends Activity {
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.focus_after_removal);
        final LinearLayout left = (LinearLayout) findViewById(R.id.leftLayout);
        Button topLeftButton = (Button) findViewById(R.id.topLeftButton);
        topLeftButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                left.setVisibility(View.GONE);
            }
        });
        Button bottomLeftButton = (Button) findViewById(R.id.bottomLeftButton);
        bottomLeftButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                left.setVisibility(View.INVISIBLE);
            }
        });
        final Button topRightButton = (Button) findViewById(R.id.topRightButton);
        topRightButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                topRightButton.setVisibility(View.GONE);
            }
        });
        final Button bottomRightButton = (Button) findViewById(R.id.bottomRightButton);
        bottomRightButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bottomRightButton.setVisibility(View.INVISIBLE);
            }
        });
    }
}
