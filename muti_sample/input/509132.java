public class UiColorValueNode extends UiTextValueNode {
    private static final Pattern RGBA_REGEXP = Pattern.compile(
            "#(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})"); 
    public UiColorValueNode(TextValueDescriptor attributeDescriptor, UiElementNode uiParent) {
        super(attributeDescriptor, uiParent);
    }
    @Override
    protected void onAddValidators(final Text text) {
        ModifyListener listener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String color = text.getText();
                if (RGBA_REGEXP.matcher(color).matches()) {
                    getManagedForm().getMessageManager().removeMessage(text, text);
                } else {
                    getManagedForm().getMessageManager().addMessage(text,
                            "Accepted color formats are one of #RGB, #ARGB, #RRGGBB or #AARRGGBB.",
                            null , IMessageProvider.ERROR, text);
                }
            }
        };
        text.addModifyListener(listener);
        text.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                getManagedForm().getMessageManager().removeMessage(text, text);
            }
        });
        listener.modifyText(null);
    }
}
