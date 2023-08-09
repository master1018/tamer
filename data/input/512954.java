public class TextTagInfo extends TagInfo {
    TextTagInfo(String n, String k, String t, SourcePositionInfo p) {
        super(n, k, DroidDoc.escape(t), p);
    }
}
