class NameClassPairWithControls extends NameClassPair implements HasControls {
    private Control[] controls;
    public NameClassPairWithControls(String name, String className,
        Control[] controls) {
        super(name, className);
        this.controls = controls;
    }
    public Control[] getControls() throws NamingException {
        return controls;
    }
    private static final long serialVersionUID = 2010738921219112944L;
}
