public abstract class BaseKeyListener
extends MetaKeyKeyListener
implements KeyListener {
     static final Object OLD_SEL_START = new NoCopySpan.Concrete();
    public boolean backspace(View view, Editable content, int keyCode,
                             KeyEvent event) {
        int selStart, selEnd;
        boolean result = true;
        {
            int a = Selection.getSelectionStart(content);
            int b = Selection.getSelectionEnd(content);
            selStart = Math.min(a, b);
            selEnd = Math.max(a, b);
        }
        if (selStart != selEnd) {
            content.delete(selStart, selEnd);
        } else if (altBackspace(view, content, keyCode, event)) {
            result = true;
        } else {
            int to = TextUtils.getOffsetBefore(content, selEnd);
            if (to != selEnd) {
                content.delete(Math.min(to, selEnd), Math.max(to, selEnd));
            }
            else {
                result = false;
            }
        }
        if (result)
            adjustMetaAfterKeypress(content);
        return result;
    }
    private boolean altBackspace(View view, Editable content, int keyCode,
                                 KeyEvent event) {
        if (getMetaState(content, META_ALT_ON) != 1) {
            return false;
        }
        if (!(view instanceof TextView)) {
            return false;
        }
        Layout layout = ((TextView) view).getLayout();
        if (layout == null) {
            return false;
        }
        int l = layout.getLineForOffset(Selection.getSelectionStart(content));
        int start = layout.getLineStart(l);
        int end = layout.getLineEnd(l);
        if (end == start) {
            return false;
        }
        content.delete(start, end);
        return true;
    }
    static int makeTextContentType(Capitalize caps, boolean autoText) {
        int contentType = InputType.TYPE_CLASS_TEXT;
        switch (caps) {
            case CHARACTERS:
                contentType |= InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
                break;
            case WORDS:
                contentType |= InputType.TYPE_TEXT_FLAG_CAP_WORDS;
                break;
            case SENTENCES:
                contentType |= InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
                break;
        }
        if (autoText) {
            contentType |= InputType.TYPE_TEXT_FLAG_AUTO_CORRECT;
        }
        return contentType;
    }
    public boolean onKeyDown(View view, Editable content,
                             int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            backspace(view, content, keyCode, event);
            return true;
        }
        return super.onKeyDown(view, content, keyCode, event);
    }
    public boolean onKeyOther(View view, Editable content, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_MULTIPLE
                || event.getKeyCode() != KeyEvent.KEYCODE_UNKNOWN) {
            return false;
        }
        int selStart, selEnd;
        {
            int a = Selection.getSelectionStart(content);
            int b = Selection.getSelectionEnd(content);
            selStart = Math.min(a, b);
            selEnd = Math.max(a, b);
        }
        CharSequence text = event.getCharacters();
        if (text == null) {
            return false;
        }
        content.replace(selStart, selEnd, text);
        return true;
    }
}
