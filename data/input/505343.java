public class BusinessCardActivity extends Activity  {
    private static final int PICK_CONTACT_REQUEST = 1;
    private final ContactAccessor mContactAccessor = ContactAccessor.getInstance();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.business_card);
        Button pickContact = (Button)findViewById(R.id.pick_contact_button);
        pickContact.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                pickContact();
            }
        });
    }
    protected void pickContact() {
        startActivityForResult(mContactAccessor.getPickContactIntent(), PICK_CONTACT_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            loadContactInfo(data.getData());
        }
    }
    private void loadContactInfo(Uri contactUri) {
        AsyncTask<Uri, Void, ContactInfo> task = new AsyncTask<Uri, Void, ContactInfo>() {
            @Override
            protected ContactInfo doInBackground(Uri... uris) {
                return mContactAccessor.loadContact(getContentResolver(), uris[0]);
            }
            @Override
            protected void onPostExecute(ContactInfo result) {
                bindView(result);
            }
        };
        task.execute(contactUri);
    }
    protected void bindView(ContactInfo contactInfo) {
        TextView displayNameView = (TextView) findViewById(R.id.display_name_text_view);
        displayNameView.setText(contactInfo.getDisplayName());
        TextView phoneNumberView = (TextView) findViewById(R.id.phone_number_text_view);
        phoneNumberView.setText(contactInfo.getPhoneNumber());
    }
}
