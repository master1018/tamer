public abstract class DocImpl implements Doc, Comparable<Object> {
    protected final DocEnv env;   
    private Comment comment;
    private CollationKey collationkey = null;
    protected String documentation;  
    private Tag[] firstSentence;
    private Tag[] inlineTags;
    DocImpl(DocEnv env, String documentation) {
        this.documentation = documentation;
        this.env = env;
    }
    String documentation() {
        if (documentation == null) documentation = "";
        return documentation;
    }
    Comment comment() {
        if (comment == null) {
            comment = new Comment(this, documentation());
        }
        return comment;
    }
    public String commentText() {
        return comment().commentText();
    }
    public Tag[] tags() {
        return comment().tags();
    }
    public Tag[] tags(String tagname) {
        return comment().tags(tagname);
    }
    public SeeTag[] seeTags() {
        return comment().seeTags();
    }
    public Tag[] inlineTags() {
        if (inlineTags == null) {
            inlineTags = Comment.getInlineTags(this, commentText());
        }
        return inlineTags;
    }
    public Tag[] firstSentenceTags() {
        if (firstSentence == null) {
            inlineTags();
            try {
                env.setSilent(true);
                firstSentence = Comment.firstSentenceTags(this, commentText());
            } finally {
                env.setSilent(false);
            }
        }
        return firstSentence;
    }
    String readHTMLDocumentation(InputStream input, FileObject filename) throws IOException {
        byte[] filecontents = new byte[input.available()];
        try {
            DataInputStream dataIn = new DataInputStream(input);
            dataIn.readFully(filecontents);
        } finally {
            input.close();
        }
        String encoding = env.getEncoding();
        String rawDoc = (encoding!=null)
            ? new String(filecontents, encoding)
            : new String(filecontents);
        Pattern bodyPat = Pattern.compile("(?is).*<body\\b[^>]*>(.*)</body\\b.*");
        Matcher m = bodyPat.matcher(rawDoc);
        if (m.matches()) {
            return m.group(1);
        } else {
            String key = rawDoc.matches("(?is).*<body\\b.*")
                    ? "javadoc.End_body_missing_from_html_file"
                    : "javadoc.Body_missing_from_html_file";
            env.error(SourcePositionImpl.make(filename, Position.NOPOS, null), key);
            return "";
        }
    }
    public String getRawCommentText() {
        return documentation();
    }
    public void setRawCommentText(String rawDocumentation) {
        documentation = rawDocumentation;
        comment = null;
    }
    CollationKey key() {
        if (collationkey == null) {
            collationkey = generateKey();
        }
        return collationkey;
    }
    CollationKey generateKey() {
        String k = name();
        return env.doclocale.collator.getCollationKey(k);
    }
    @Override
    public String toString() {
        return qualifiedName();
    }
    public abstract String name();
    public abstract String qualifiedName();
    public int compareTo(Object obj) {
        return key().compareTo(((DocImpl)obj).key());
    }
    public boolean isField() {
        return false;
    }
    public boolean isEnumConstant() {
        return false;
    }
    public boolean isConstructor() {
        return false;
    }
    public boolean isMethod() {
        return false;
    }
    public boolean isAnnotationTypeElement() {
        return false;
    }
    public boolean isInterface() {
        return false;
    }
    public boolean isException() {
        return false;
    }
    public boolean isError() {
        return false;
    }
    public boolean isEnum() {
        return false;
    }
    public boolean isAnnotationType() {
        return false;
    }
    public boolean isOrdinaryClass() {
        return false;
    }
    public boolean isClass() {
        return false;
    }
    public abstract boolean isIncluded();
    public SourcePosition position() { return null; }
}
