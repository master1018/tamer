public abstract class CompoundControl extends Control {
    public static class Type extends Control.Type {
        protected Type(String name) {
            super(name);
        }
    }
    private Control[] memberControls;
    protected CompoundControl(CompoundControl.Type type,
            Control[] memberControls) {
        super(type);
        this.memberControls = memberControls;
    }
    public Control[] getMemberControls() {
        return this.memberControls;
    }
    public String toString() {
        return getType() + "CompoundControl containing "  
            + String.valueOf(memberControls) + " Controls."; 
    }
}
