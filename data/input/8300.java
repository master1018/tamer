class WDialogPeer extends WWindowPeer implements DialogPeer {
    final static Color defaultBackground =  SystemColor.control;
    boolean needDefaultBackground;
    WDialogPeer(Dialog target) {
        super(target);
        InputMethodManager imm = InputMethodManager.getInstance();
        String menuString = imm.getTriggerMenuString();
        if (menuString != null)
        {
            pSetIMMOption(menuString);
        }
    }
    native void createAwtDialog(WComponentPeer parent);
    void create(WComponentPeer parent) {
        preCreate(parent);
        createAwtDialog(parent);
    }
    native void showModal();
    native void endModal();
    void initialize() {
        Dialog target = (Dialog)this.target;
        if (needDefaultBackground) {
            target.setBackground(defaultBackground);
        }
        super.initialize();
        if (target.getTitle() != null) {
            setTitle(target.getTitle());
        }
        setResizable(target.isResizable());
    }
    protected void realShow() {
        Dialog dlg = (Dialog)target;
        if (dlg.getModalityType() != Dialog.ModalityType.MODELESS) {
            showModal();
        } else {
            super.realShow();
        }
    }
    public void hide() {
        Dialog dlg = (Dialog)target;
        if (dlg.getModalityType() != Dialog.ModalityType.MODELESS) {
            endModal();
        } else {
            super.hide();
        }
    }
    public void blockWindows(java.util.List<Window> toBlock) {
        for (Window w : toBlock) {
            WWindowPeer wp = (WWindowPeer)AWTAccessor.getComponentAccessor().getPeer(w);
            if (wp != null) {
                wp.setModalBlocked((Dialog)target, true);
            }
        }
    }
    public Dimension getMinimumSize() {
        if (((Dialog)target).isUndecorated()) {
            return super.getMinimumSize();
        } else {
            return new Dimension(getSysMinWidth(), getSysMinHeight());
        }
    }
    @Override
    boolean isTargetUndecorated() {
        return ((Dialog)target).isUndecorated();
    }
    public void reshape(int x, int y, int width, int height) {
        if (((Dialog)target).isUndecorated()) {
            super.reshape(x, y, width, height);
        } else {
            reshapeFrame(x, y, width, height);
        }
    }
    private void setDefaultColor() {
        needDefaultBackground = true;
    }
    native void pSetIMMOption(String option);
    void notifyIMMOptionChange(){
      InputMethodManager.getInstance().notifyChangeRequest((Component)target);
    }
}
