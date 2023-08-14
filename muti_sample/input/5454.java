class BindingWithControls extends Binding implements HasControls {
    private Control[] controls;
    public BindingWithControls(String name, Object obj, Control[] controls) {
        super(name, obj);
        this.controls = controls;
    }
    public Control[] getControls() throws NamingException {
        return controls;
    }
    private static final long serialVersionUID = 9117274533692320040L;
}
