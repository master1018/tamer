public class TableLayout9 extends Activity {
    private boolean mShrink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_layout_9);
        final TableLayout table = (TableLayout) findViewById(R.id.menu);
        Button button = (Button) findViewById(R.id.toggle);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mShrink = !mShrink;
                table.setColumnShrinkable(0, mShrink);
            }
        });
        mShrink = table.isColumnShrinkable(0);
    }
}
