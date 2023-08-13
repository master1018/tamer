public class Rfc822Validator implements AutoCompleteTextView.Validator {
    private static final Pattern EMAIL_ADDRESS_PATTERN =
            Pattern.compile("[^\\s@]+@[^\\s@]+\\.[a-zA-z][a-zA-Z][a-zA-Z]*");
    private String mDomain;
    public Rfc822Validator(String domain) {
        mDomain = domain;
    }
    public boolean isValid(CharSequence text) {
        Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(text);
        return tokens.length == 1 &&
               EMAIL_ADDRESS_PATTERN.
                   matcher(tokens[0].getAddress()).matches();
    }
    private String removeIllegalCharacters(String s) {
        StringBuilder result = new StringBuilder();
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c <= ' ' || c > '~') {
                continue;
            }
            if (c == '(' || c == ')' || c == '<' || c == '>' ||
                c == '@' || c == ',' || c == ';' || c == ':' ||
                c == '\\' || c == '"' || c == '[' || c == ']') {
                continue;
            }
            result.append(c);
        }
        return result.toString();
    }
    public CharSequence fixText(CharSequence cs) {
        if (TextUtils.getTrimmedLength(cs) == 0) return "";
        Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(cs);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            String text = tokens[i].getAddress();
            int index = text.indexOf('@');
            if (index < 0) {
                tokens[i].setAddress(removeIllegalCharacters(text) + "@" + mDomain);
            } else {
                String fix = removeIllegalCharacters(text.substring(0, index));
                String domain = removeIllegalCharacters(text.substring(index + 1));
                tokens[i].setAddress(fix + "@" + (domain.length() != 0 ? domain : mDomain));
            }
            sb.append(tokens[i].toString());
            if (i + 1 < tokens.length) {
                sb.append(", ");
            }
        }
        return sb;
    }
}
