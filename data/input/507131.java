public class Markup {
    private BrandingResources mRes;
    private IntTrie mSmileys;
    public Markup(BrandingResources res) {
        mRes = res;
        mSmileys = new IntTrie(
                res.getStringArray(BrandingResourceIDs.STRING_ARRAY_SMILEY_TEXTS), res.getSmileyIcons());
    }
    public final CharSequence markup(CharSequence text) {
        SpannableString result;
        if (text instanceof SpannableString) {
            result = (SpannableString) text;
        } else {
            result = new SpannableString(text);
        }
        Linkify.addLinks(result, Linkify.ALL);
        applyEmoticons(result);
        return result;
    }
    public final CharSequence applyEmoticons(CharSequence text) {
        int offset = 0;
        final int len = text.length();
        SpannableString result = null;
        while (offset < len) {
            int index = offset;
            IntTrie.Node n = mSmileys.getNode(text.charAt(index++));
            int candidate = 0;
            int lastMatchEnd = -1;
            while (n != null) {
                if (n.mValue != 0) {
                    candidate = n.mValue;
                    lastMatchEnd = index;
                }
                if (index >= len) {
                    break;
                }
                n = n.getNode(text.charAt(index++));
            }
            if (candidate != 0) {
                if (result == null) {
                    if (text instanceof SpannableString) {
                        result = (SpannableString) text;
                    } else {
                        result = new SpannableString(text);
                        text = result;
                    }
                }
                Drawable smiley = mRes.getSmileyIcon(candidate);
                smiley.setBounds(0, 0, smiley.getIntrinsicWidth(), smiley.getIntrinsicHeight());
                result.setSpan(new ImageSpan(smiley, ImageSpan.ALIGN_BASELINE),
                    offset, lastMatchEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                candidate = 0;
            }
            if (lastMatchEnd != -1) {
                offset = lastMatchEnd;
                lastMatchEnd = -1;
            } else {
                offset++;
            }
        }
        if (result == null) {
            return text;
        }
        return result;
    }
}
