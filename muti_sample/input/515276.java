public class ContactsFilter extends Activity {
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_filter);
        Button button = (Button)findViewById(R.id.go);
        button.setOnClickListener(mGoListener);
    }
    private OnClickListener mGoListener = new OnClickListener() {
        public void onClick(View v) {
            startInstrumentation(new ComponentName(ContactsFilter.this,
                            ContactsFilterInstrumentation.class), null, null);
        }
    };
}
