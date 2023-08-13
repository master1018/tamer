class TagImpl implements Tag {
    protected final String text;
    protected final String name;
    protected final DocImpl holder;
    private Tag[] firstSentence;
    private Tag[] inlineTags;
    TagImpl(DocImpl holder, String name, String text) {
        this.holder = holder;
        this.name = name;
        this.text = text;
    }
    public String name() {
        return name;
    }
    public Doc holder() {
        return holder;
    }
    public String kind() {
        return name;
    }
    public String text() {
        return text;
    }
    DocEnv docenv() {
        return holder.env;
    }
    String[] divideAtWhite() {
        String[] sa = new String[2];
        int len = text.length();
        sa[0] = text;
        sa[1] = "";
        for (int inx = 0; inx < len; ++inx) {
            char ch = text.charAt(inx);
            if (Character.isWhitespace(ch)) {
                sa[0] = text.substring(0, inx);
                for (; inx < len; ++inx) {
                    ch = text.charAt(inx);
                    if (!Character.isWhitespace(ch)) {
                        sa[1] = text.substring(inx, len);
                        break;
                    }
                }
                break;
            }
        }
        return sa;
    }
    public String toString() {
        return name + ":" + text;
    }
    public Tag[] inlineTags() {
        if (inlineTags == null) {
            inlineTags = Comment.getInlineTags(holder, text);
        }
        return inlineTags;
    }
    public Tag[] firstSentenceTags() {
        if (firstSentence == null) {
            inlineTags();
            try {
                docenv().setSilent(true);
                firstSentence = Comment.firstSentenceTags(holder, text);
            } finally {
                docenv().setSilent(false);
            }
        }
        return firstSentence;
    }
    public SourcePosition position() {
        return holder.position();
    }
}
