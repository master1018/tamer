public class TrendsToolkit {
    public static final String tagNameSeparator = "";
    public static String getDisplayableTagName(String tagName) {
        String value = "";
        int pos = tagName.indexOf(TrendsToolkit.tagNameSeparator);
        if (pos != -1) {
            value = tagName.substring(0, pos);
        }
        return value;
    }
    public static String getTagType(String tagName) {
        String tagType = "";
        int pos = tagName.indexOf(TrendsToolkit.tagNameSeparator);
        if (pos != -1) {
            tagType = tagName.substring(pos + TrendsToolkit.tagNameSeparator.length(), tagName.length());
        }
        return tagType;
    }
}
