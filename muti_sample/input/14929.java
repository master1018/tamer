class XInputMethodDescriptor extends X11InputMethodDescriptor {
    public InputMethod createInputMethod() throws Exception {
        return new XInputMethod();
    }
}
