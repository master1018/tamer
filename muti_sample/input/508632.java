public class Link extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.link);
        TextView t2 = (TextView) findViewById(R.id.text2);
        t2.setMovementMethod(LinkMovementMethod.getInstance());
        TextView t3 = (TextView) findViewById(R.id.text3);
        t3.setText(
            Html.fromHtml(
                "<b>text3:</b>  Text with a " +
                "<a href=\"http:
                "created in the Java source code using HTML."));
        t3.setMovementMethod(LinkMovementMethod.getInstance());
        SpannableString ss = new SpannableString(
            "text4: Click here to dial the phone.");
        ss.setSpan(new StyleSpan(Typeface.BOLD), 0, 6,
                   Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new URLSpan("tel:4155551212"), 13, 17,
                   Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView t4 = (TextView) findViewById(R.id.text4);
        t4.setText(ss);
        t4.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
