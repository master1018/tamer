public class StubbedView extends Activity {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.viewstub);
        findViewById(R.id.vis).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final View view = findViewById(R.id.viewStub);
                if (view != null) {
                    view.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
