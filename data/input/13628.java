public abstract class CompoundControl extends Control {
    private Control[] controls;
    protected CompoundControl(Type type, Control[] memberControls) {
        super(type);
        this.controls = memberControls;
    }
    public Control[] getMemberControls() {
        Control[] localArray = new Control[controls.length];
        for (int i = 0; i < controls.length; i++) {
            localArray[i] = controls[i];
        }
        return localArray;
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < controls.length; i++) {
            if (i != 0) {
                buf.append(", ");
                if ((i + 1) == controls.length) {
                    buf.append("and ");
                }
            }
            buf.append(controls[i].getType());
        }
        return new String(getType() + " Control containing " + buf + " Controls.");
    }
    public static class Type extends Control.Type {
        protected Type(String name) {
            super(name);
        }
    } 
} 
