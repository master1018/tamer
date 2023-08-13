public class BaselineAlignmentZeroWidthAndWeight extends Activity {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.baseline_0width_and_weight);
        findViewById(R.id.show).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(R.id.layout).setVisibility(View.VISIBLE);
            }
        });
    }
}
