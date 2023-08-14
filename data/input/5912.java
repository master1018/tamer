public class FocusingTextField extends TextField {
    TextField next;
    boolean willSelect;
    public FocusingTextField(int cols) {
        super("", cols);
    }
    public FocusingTextField(int cols, boolean willSelect) {
        this(cols);
        this.willSelect = willSelect;
    }
    public void setWillSelect(boolean will) {
        willSelect = will;
    }
    public boolean getWillSelect() {
        return willSelect;
    }
    public void setNextField(TextField next) {
        this.next = next;
    }
    public boolean gotFocus(Event e, Object arg) {
        if (willSelect) {
            select(0, getText().length());
        }
        return true;
    }
    public boolean lostFocus(Event e, Object arg) {
        if (willSelect) {
            select(0, 0);
        }
        return true;
    }
    public void nextFocus() {
        if (next != null) {
            next.requestFocus();
        }
        super.nextFocus();
    }
}
