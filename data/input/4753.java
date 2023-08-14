public class bug6463712 implements ChangeListener {
    public bug6463712() {
        SpinnerNumberModel m1 = new SpinnerNumberModel();
        JSpinner s = new JSpinner(m1);
        s.addChangeListener(this);
        SpinnerDateModel m2 = new SpinnerDateModel();
        s.setModel(m2);
        m1.setValue(new Integer(1));
    }
    public void stateChanged(ChangeEvent e) {
        throw new RuntimeException("Should not receive this event.");
    }
    public static void main(String[] args) {
        bug6463712 bug = new bug6463712();
    }
}
