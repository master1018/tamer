public final class StringCustom {
    private StringCustom() {
    }
    public static String trimLeft(final Context context, final Scriptable thisObj, final Object[] args, final Function function) {
        final String string = Context.toString(thisObj);
        int start = 0;
        final int length = string.length();
        while (start < length && ScriptRuntime.isJSWhitespaceOrLineTerminator(string.charAt(start))) {
            start++;
        }
        if (start == 0) {
            return string;
        }
        return string.substring(start, length);
    }
    public static String trimRight(final Context context, final Scriptable thisObj, final Object[] args, final Function function) {
        final String string = Context.toString(thisObj);
        final int length = string.length();
        int end = length;
        while (end > 0 && ScriptRuntime.isJSWhitespaceOrLineTerminator(string.charAt(end - 1))) {
            end--;
        }
        if (end == length) {
            return string;
        }
        return string.substring(0, end);
    }
}
