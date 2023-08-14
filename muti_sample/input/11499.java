class SerialFieldTagImpl
    extends TagImpl
    implements SerialFieldTag, Comparable<Object>
{
    private String fieldName;    
    private String fieldType;    
    private String description;  
    private ClassDoc containingClass;   
    private ClassDoc fieldTypeDoc;      
    private FieldDocImpl matchingField; 
   SerialFieldTagImpl(DocImpl holder, String name, String text) {
        super(holder, name, text);
        parseSerialFieldString();
        if (holder instanceof MemberDoc) {
            containingClass = ((MemberDocImpl)holder).containingClass();
        }
    }
    private void parseSerialFieldString() {
        int len = text.length();
        int inx = 0;
        int cp;
        for (; inx < len; inx += Character.charCount(cp)) {
             cp = text.codePointAt(inx);
             if (!Character.isWhitespace(cp)) {
                 break;
             }
        }
        int first = inx;
        int last = inx;
        cp = text.codePointAt(inx);
        if (! Character.isJavaIdentifierStart(cp)) {
            docenv().warning(holder,
                             "tag.serialField.illegal_character",
                             new String(Character.toChars(cp)), text);
            return;
        }
        for (inx += Character.charCount(cp); inx < len; inx += Character.charCount(cp)) {
             cp = text.codePointAt(inx);
             if (!Character.isJavaIdentifierPart(cp)) {
                 break;
             }
        }
        if (inx < len && ! Character.isWhitespace(cp = text.codePointAt(inx))) {
            docenv().warning(holder,
                             "tag.serialField.illegal_character",
                             new String(Character.toChars(cp)), text);
            return;
        }
        last = inx;
        fieldName = text.substring(first, last);
        for (; inx < len; inx += Character.charCount(cp)) {
             cp = text.codePointAt(inx);
             if (!Character.isWhitespace(cp)) {
                 break;
             }
        }
        first = inx;
        last = inx;
        for (; inx < len; inx += Character.charCount(cp)) {
             cp = text.codePointAt(inx);
             if (Character.isWhitespace(cp)) {
                 break;
             }
        }
        if (inx < len && ! Character.isWhitespace(cp = text.codePointAt(inx))) {
            docenv().warning(holder,
                             "tag.serialField.illegal_character",
                             new String(Character.toChars(cp)), text);
            return;
        }
        last = inx;
        fieldType = text.substring(first, last);
        for (; inx < len; inx += Character.charCount(cp)) {
             cp = text.codePointAt(inx);
             if (!Character.isWhitespace(cp)) {
                 break;
             }
        }
        description = text.substring(inx);
    }
    String key() {
        return fieldName;
    }
    void mapToFieldDocImpl(FieldDocImpl fd) {
        matchingField = fd;
    }
    public String fieldName() {
        return fieldName;
    }
    public String fieldType() {
        return fieldType;
    }
    public ClassDoc fieldTypeDoc() {
        if (fieldTypeDoc == null && containingClass != null) {
            fieldTypeDoc = containingClass.findClass(fieldType);
        }
        return fieldTypeDoc;
    }
    FieldDocImpl getMatchingField() {
        return matchingField;
    }
    public String description() {
        if (description.length() == 0 && matchingField != null) {
            Comment comment = matchingField.comment();
            if (comment != null) {
                return comment.commentText();
            }
        }
        return description;
    }
    public String kind() {
        return "@serialField";
    }
    public String toString() {
        return name + ":" + text;
    }
    public int compareTo(Object obj) {
        return key().compareTo(((SerialFieldTagImpl)obj).key());
    }
}
