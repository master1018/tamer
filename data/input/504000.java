public final class MessageListView extends ListView {
    public MessageListView(Context context) {
        super(context);
    }
    public MessageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_C:
            MessageListItem view = (MessageListItem)getSelectedView();
            if (view == null) {
                break;
            }
            MessageItem item = view.getMessageItem();
            if (item != null && item.isSms()) {
                ClipboardManager clip =
                    (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clip.setText(item.mBody);
                return true;
            }
            break;
        }
        return super.onKeyShortcut(keyCode, event);
    }
}
