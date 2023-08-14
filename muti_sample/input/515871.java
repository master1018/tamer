public class NotifyWithText extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notify_with_text);
        Button button;
        button = (Button) findViewById(R.id.short_notify);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(NotifyWithText.this, R.string.short_notification_text,
                    Toast.LENGTH_SHORT).show();
            }
        });
        button = (Button) findViewById(R.id.long_notify);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(NotifyWithText.this, R.string.long_notification_text,
                    Toast.LENGTH_LONG).show();
            }
        });
    }
}
