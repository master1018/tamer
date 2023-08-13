public class EnableButton extends JButton implements ActionListener {
    public static final int SET = 0;
    public static final int CLEAR = 1;
    public static final int INVERT = 2;
    public static final int DEFAULT = 3;
    private Group group;
    private int type;
    public static final String icons[] = {
        "Set",
        "Clear",
        "Invert",
        "Default",
    };
    public EnableButton(Group group, int type) {
        super(icons[type]);
        this.group = group;
        this.type = type;
        addActionListener(this);
        setMargin(new Insets(0, 0, 0, 0));
        setBorderPainted(false);
    }
    public void actionPerformed(ActionEvent e) {
        Node.Iterator children = group.getRecursiveChildIterator();
        String newval = (type == SET) ? "enabled" : "disabled";
        while (children.hasNext()) {
            Node child = children.next();
            if (type == DEFAULT) {
                child.restoreDefault();
            } else if (child instanceof Option.Enable) {
                Option.Enable enable = (Option.Enable) child;
                if (type == INVERT) {
                    newval = enable.isEnabled() ? "disabled" : "enabled";
                }
                enable.setValueFromString(newval);
            }
        }
    }
}
