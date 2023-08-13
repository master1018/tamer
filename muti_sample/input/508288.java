public class ChatBackgroundMaker {
    private final Drawable mIncomingBg;
    private final Drawable mDivider;
    private final Rect mPadding;
    public ChatBackgroundMaker(Context context) {
        Resources res = context.getResources();
        mIncomingBg = res.getDrawable(R.drawable.textfield_im_received);
        mDivider = res.getDrawable(R.drawable.text_divider_horizontal);
        mPadding = new Rect();
        mIncomingBg.getPadding(mPadding);
    }
    public void setBackground(MessageView view, String contact, int type) {
        View msgText = view.findViewById(R.id.message);
        switch (type) {
            case Imps.MessageType.INCOMING:
                msgText.setBackgroundDrawable(mIncomingBg);
                break;
            case Imps.MessageType.OUTGOING:
            case Imps.MessageType.POSTPONED:
                msgText.setBackgroundDrawable(null);
                msgText.setPadding(mPadding.left, mPadding.top, mPadding.right,
                      mPadding.bottom);
                break;
            default:
                msgText.setBackgroundDrawable(mDivider);
        }
    }
}
