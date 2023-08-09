public class Rfc822Token {
    private String mName, mAddress, mComment;
    public Rfc822Token(String name, String address, String comment) {
        mName = name;
        mAddress = address;
        mComment = comment;
    }
    public String getName() {
        return mName;
    }
    public String getAddress() {
        return mAddress;
    }
    public String getComment() {
        return mComment;
    }
    public void setName(String name) {
        mName = name;
    }
    public void setAddress(String address) {
        mAddress = address;
    }
    public void setComment(String comment) {
        mComment = comment;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mName != null && mName.length() != 0) {
            sb.append(quoteNameIfNecessary(mName));
            sb.append(' ');
        }
        if (mComment != null && mComment.length() != 0) {
            sb.append('(');
            sb.append(quoteComment(mComment));
            sb.append(") ");
        }
        if (mAddress != null && mAddress.length() != 0) {
            sb.append('<');
            sb.append(mAddress);
            sb.append('>');
        }
        return sb.toString();
    }
    public static String quoteNameIfNecessary(String name) {
        int len = name.length();
        for (int i = 0; i < len; i++) {
            char c = name.charAt(i);
            if (! ((c >= 'A' && c <= 'Z') ||
                   (c >= 'a' && c <= 'z') ||
                   (c == ' ') ||
                   (c >= '0' && c <= '9'))) {
                return '"' + quoteName(name) + '"';
            }
        }
        return name;
    }
    public static String quoteName(String name) {
        StringBuilder sb = new StringBuilder();
        int len = name.length();
        for (int i = 0; i < len; i++) {
            char c = name.charAt(i);
            if (c == '\\' || c == '"') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }
    public static String quoteComment(String comment) {
        int len = comment.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char c = comment.charAt(i);
            if (c == '(' || c == ')' || c == '\\') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }
    public int hashCode() {
        int result = 17;
        if (mName != null) result = 31 * result + mName.hashCode();
        if (mAddress != null) result = 31 * result + mAddress.hashCode();
        if (mComment != null) result = 31 * result + mComment.hashCode();
        return result;
    }
    private static boolean stringEquals(String a, String b) {
        if (a == null) {
            return (b == null);
        } else {
            return (a.equals(b));
        }
    }
    public boolean equals(Object o) {
        if (!(o instanceof Rfc822Token)) {
            return false;
        }
        Rfc822Token other = (Rfc822Token) o;
        return (stringEquals(mName, other.mName) &&
                stringEquals(mAddress, other.mAddress) &&
                stringEquals(mComment, other.mComment));
    }
}
