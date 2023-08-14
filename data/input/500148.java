public class EditingUtil {
    private EditingUtil() {};
    public static void appendText(InputConnection connection, String newText) {
        if (connection == null) {
            return;
        }
        connection.finishComposingText();
        CharSequence charBeforeCursor = connection.getTextBeforeCursor(1, 0);
        if (charBeforeCursor != null
                && !charBeforeCursor.equals(" ")
                && (charBeforeCursor.length() > 0)) {
            newText = " " + newText;
        }
        connection.setComposingText(newText, 1);
    }
    private static int getCursorPosition(InputConnection connection) {
        ExtractedText extracted = connection.getExtractedText(
            new ExtractedTextRequest(), 0);
        if (extracted == null) {
          return -1;
        }
        return extracted.startOffset + extracted.selectionStart;
    }
    private static int getSelectionEnd(InputConnection connection) {
        ExtractedText extracted = connection.getExtractedText(
            new ExtractedTextRequest(), 0);
        if (extracted == null) {
          return -1;
        }
        return extracted.startOffset + extracted.selectionEnd;
    }
    public static String getWordAtCursor(
        InputConnection connection, String separators) {
        Range range = getWordRangeAtCursor(connection, separators);
        return (range == null) ? null : range.word;
    }
    public static void deleteWordAtCursor(
        InputConnection connection, String separators) {
        Range range = getWordRangeAtCursor(connection, separators);
        if (range == null) return;
        connection.finishComposingText();
        int newCursor = getCursorPosition(connection) - range.charsBefore;
        connection.setSelection(newCursor, newCursor);
        connection.deleteSurroundingText(0, range.charsBefore + range.charsAfter);
    }
    private static class Range {
        int charsBefore;
        int charsAfter;
        String word;
        public Range(int charsBefore, int charsAfter, String word) {
            if (charsBefore < 0 || charsAfter < 0) {
                throw new IndexOutOfBoundsException();
            }
            this.charsBefore = charsBefore;
            this.charsAfter = charsAfter;
            this.word = word;
        }
    }
    private static Range getWordRangeAtCursor(
        InputConnection connection, String sep) {
        if (connection == null || sep == null) {
            return null;
        }
        CharSequence before = connection.getTextBeforeCursor(1000, 0);
        CharSequence after = connection.getTextAfterCursor(1000, 0);
        if (before == null || after == null) {
            return null;
        }
        int start = before.length();
        while (--start > 0 && !isWhitespace(before.charAt(start - 1), sep));
        int end = -1;
        while (++end < after.length() && !isWhitespace(after.charAt(end), sep));
        if (end < after.length() - 1) {
            end++; 
        }
        int cursor = getCursorPosition(connection);
        if (start >= 0 && cursor + end <= after.length() + before.length()) {
            String word = before.toString().substring(start, before.length())
                + after.toString().substring(0, end);
            return new Range(before.length() - start, end, word);
        }
        return null;
    }
    private static boolean isWhitespace(int code, String whitespace) {
        return whitespace.contains(String.valueOf((char) code));
    }
}
