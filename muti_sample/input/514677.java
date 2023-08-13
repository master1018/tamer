public class Rfc822InputFilter implements InputFilter {
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
        int dstart, int dend) {
        if (end-start != 1 || source.charAt(start) != ' ') {
            return null;
        }
        int scanBack = dstart;
        boolean dotFound = false;
        while (scanBack > 0) {
            char c = dest.charAt(--scanBack);
            switch (c) {
                case '.':
                    dotFound = true;    
                    break;
                case ',':
                    return null;
                case '@':
                    if (!dotFound) {
                        return null;
                    }
                    if (source instanceof Spanned) {
                        SpannableStringBuilder sb = new SpannableStringBuilder(",");
                        sb.append(source);
                        return sb;
                    } else {
                        return ", ";
                    }
                default:
            }
        }
        return null;
    }
}
