public class ListWithMailMessages extends ListActivity {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.list_with_button_above);
        List<MailMessage> messages = Lists.newArrayList();
        messages.add(new MailMessage("hello!", "<p>this is a test "
                + "message, with a bunch of text and stuff.</p>", true));
        String android = "<a href=\"www.android.com\">android</a>";
        String sentance = "all work and no play makes "
        + android + " a dull... robot!";
        StringBuffer longBody = new StringBuffer().append("<ol>\n");
        for (int i = 0; i < 12; i++) {
            longBody.append("<li>").append(sentance).append("</li>");
        }
        longBody.append("</ol>");
        messages.add(new MailMessage("hello2!", longBody.toString(), true));
        messages.add(new MailMessage("phone number?", "<p>hey man, what's ur "
                + "contact info? i need to mail you this photo of my two"
                + " cats, they've gotten soooo fat!</p>", true));
        setListAdapter(new MyAdapter(this, R.layout.mail_message, messages));
        getListView().setItemsCanFocus(true);
    }
    static class MailMessage {
        private String mSubject;
        private String mBody;
        private boolean mFocusable;
        public MailMessage(String subject, String body) {
            this(subject, body, false);
        }
        public MailMessage(String subject, String body, boolean focusable) {
            mSubject = subject;
            mBody = body;
            mFocusable = focusable;
        }
        public String getSubject() {
            return mSubject;
        }
        public void setSubject(String subject) {
            this.mSubject = subject;
        }
        public String getBody() {
            return mBody;
        }
        public void setBody(String body) {
            this.mBody = body;
        }
        public boolean isFocusable() {
            return mFocusable;
        }
        public void setFocusable(boolean focusable) {
            mFocusable = focusable;
        }
    }
    public static class MyAdapter extends ArrayAdapter<MailMessage> {
        public MyAdapter(Context context, int resource,
                List<MailMessage> objects) {
            super(context, resource, objects);
        }
        final String mimeType = "text/html";
        final String encoding = "utf-8";
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MailMessage message = getItem(position);
            LayoutInflater inflater = (LayoutInflater)
                    getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout messageUi = (LinearLayout) inflater
                    .inflate(R.layout.mail_message, null);
            TextView subject = (TextView) messageUi.findViewById(R.id.subject);
            subject.setText(message.getSubject());
            WebView body = (WebView) messageUi.findViewById(R.id.body);
            body.loadData(message.getBody(), mimeType, encoding);
            body.setFocusable(message.isFocusable());
            return messageUi;
        }
    }
}
