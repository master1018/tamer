public abstract class ClickableSpan extends CharacterStyle implements UpdateAppearance {
    public abstract void onClick(View widget);
    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(ds.linkColor);
        ds.setUnderlineText(true);
    }
}
