public abstract class TextAction extends AbstractAction {
    public TextAction(String name) {
        super(name);
    }
    protected final JTextComponent getTextComponent(ActionEvent e) {
        if (e != null) {
            Object o = e.getSource();
            if (o instanceof JTextComponent) {
                return (JTextComponent) o;
            }
        }
        return getFocusedComponent();
    }
    public static final Action[] augmentList(Action[] list1, Action[] list2) {
        Hashtable<String, Action> h = new Hashtable<String, Action>();
        for (Action a : list1) {
            String value = (String)a.getValue(Action.NAME);
            h.put((value!=null ? value:""), a);
        }
        for (Action a : list2) {
            String value = (String)a.getValue(Action.NAME);
            h.put((value!=null ? value:""), a);
        }
        Action[] actions = new Action[h.size()];
        int index = 0;
        for (Enumeration e = h.elements() ; e.hasMoreElements() ;) {
            actions[index++] = (Action) e.nextElement();
        }
        return actions;
    }
    protected final JTextComponent getFocusedComponent() {
        return JTextComponent.getFocusedComponent();
    }
}
