public class SmileyParser extends AbstractMessageParser {
    private SmileyResources mRes;
    public SmileyParser(String text, SmileyResources res) {
        super(text,
                true,   
                false,  
                false,  
                false,  
                false,  
                false   
        );
        mRes = res;
    }
    @Override
    protected Resources getResources() {
        return mRes;
    }
    public CharSequence getSpannableString(Context context) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (getPartCount() == 0) {
            return "";
        }
        Part part = getPart(0);
        ArrayList<Token> tokens = part.getTokens();
        int len = tokens.size();
        for (int i = 0; i < len; i++) {
            Token token = tokens.get(i);
            int start = builder.length();
            builder.append(token.getRawText());
            if (token.getType() == AbstractMessageParser.Token.Type.SMILEY) {
                int resid = mRes.getSmileyRes(token.getRawText());
                if (resid != -1) {
                    builder.setSpan(new ImageSpan(context, resid),
                            start,
                            builder.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return builder;
    }
}
