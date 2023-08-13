public class Disabled extends Activity implements OnClickListener {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.disabled);
        Button disabledButton = (Button) findViewById(R.id.disabledButton);
        disabledButton.setEnabled(false);
        Button disabledButtonA = (Button) findViewById(R.id.disabledButtonA);
        disabledButtonA.setOnClickListener(this);
    }
    public void onClick(View v) {
        Button disabledButtonB = (Button) findViewById(R.id.disabledButtonB);
        disabledButtonB.setEnabled(!disabledButtonB.isEnabled());
    }
}
