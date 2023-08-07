public class WindowWithDataPanelElement extends DialogBoxWithCaptionButton {
    private final ScrollPanel sp = new ScrollPanel();
    private BasicElementPanel bep = null;
    private Boolean showCloseBottomButton;
    public final void setShowCloseBottomButton(final Boolean ashowCloseBottomButton) {
        this.showCloseBottomButton = ashowCloseBottomButton;
    }
    public Boolean getShowCloseBottomButton() {
        return showCloseBottomButton;
    }
    public WindowWithDataPanelElement(final Boolean ashowCloseBottomButton) {
        super();
        setShowCloseBottomButton(ashowCloseBottomButton);
        final int n100 = 100;
        sp.setSize(String.valueOf(Window.getClientWidth() - n100) + "px", String.valueOf(Window.getClientHeight() - n100) + "px");
    }
    public WindowWithDataPanelElement(final String caption, final Boolean ashowCloseBottomButton) {
        super(caption);
        setShowCloseBottomButton(ashowCloseBottomButton);
        final int n100 = 100;
        sp.setSize(String.valueOf(Window.getClientWidth() - n100) + "px", String.valueOf(Window.getClientHeight() - n100) + "px");
    }
    public WindowWithDataPanelElement(final String caption, final Integer width, final Integer heigth, final Boolean ashowCloseBottomButton) {
        super(caption);
        sp.setSize(String.valueOf(width) + "px", String.valueOf(heigth) + "px");
        setShowCloseBottomButton(ashowCloseBottomButton);
    }
    public void showModalWindow(final BasicElementPanel bep1) {
        bep = bep1;
        if (bep instanceof XFormPanel) {
            XFormPanel.beforeModalWindow(bep);
        }
        VerticalPanel dialogContents = new VerticalPanel();
        DOM.setElementAttribute(dialogContents.getElement(), "id", "showcaseModalWindow");
        sp.add(dialogContents);
        final int n = 10;
        dialogContents.setSpacing(n);
        dialogContents.setSize("100%", "100%");
        setWidget(sp);
        setAnimationEnabled(true);
        setGlassEnabled(true);
        dialogContents.add(bep.getPanel());
        if (getShowCloseBottomButton()) {
            Button ok = new Button("Закрыть");
            ok.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    closeWindow();
                }
            });
            dialogContents.add(ok);
            dialogContents.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_RIGHT);
            ok.setFocus(true);
        }
        AppCurrContext.getInstance().setCurrentOpenWindowWithDataPanelElement(this);
        center();
        show();
    }
    @Override
    public void closeWindow() {
        super.closeWindow();
        AppCurrContext.getInstance().setCurrentOpenWindowWithDataPanelElement(null);
        if (bep != null) {
            if (bep instanceof XFormPanel) {
                XFormPanel.destroyXForms();
            }
        }
        bep = null;
        ActionExecuter.drawXFormPanelsAfterModalWindowShown(AppCurrContext.getInstance().getCurrentAction());
    }
}
