public class MultiTextUI extends TextUI {
    protected Vector uis = new Vector();
    public ComponentUI[] getUIs() {
        return MultiLookAndFeel.uisToArray(uis);
    }
    public String getToolTipText(JTextComponent a, Point b) {
        String returnValue =
            ((TextUI) (uis.elementAt(0))).getToolTipText(a,b);
        for (int i = 1; i < uis.size(); i++) {
            ((TextUI) (uis.elementAt(i))).getToolTipText(a,b);
        }
        return returnValue;
    }
    public Rectangle modelToView(JTextComponent a, int b)
            throws BadLocationException {
        Rectangle returnValue =
            ((TextUI) (uis.elementAt(0))).modelToView(a,b);
        for (int i = 1; i < uis.size(); i++) {
            ((TextUI) (uis.elementAt(i))).modelToView(a,b);
        }
        return returnValue;
    }
    public Rectangle modelToView(JTextComponent a, int b, Position.Bias c)
            throws BadLocationException {
        Rectangle returnValue =
            ((TextUI) (uis.elementAt(0))).modelToView(a,b,c);
        for (int i = 1; i < uis.size(); i++) {
            ((TextUI) (uis.elementAt(i))).modelToView(a,b,c);
        }
        return returnValue;
    }
    public int viewToModel(JTextComponent a, Point b) {
        int returnValue =
            ((TextUI) (uis.elementAt(0))).viewToModel(a,b);
        for (int i = 1; i < uis.size(); i++) {
            ((TextUI) (uis.elementAt(i))).viewToModel(a,b);
        }
        return returnValue;
    }
    public int viewToModel(JTextComponent a, Point b, Position.Bias[] c) {
        int returnValue =
            ((TextUI) (uis.elementAt(0))).viewToModel(a,b,c);
        for (int i = 1; i < uis.size(); i++) {
            ((TextUI) (uis.elementAt(i))).viewToModel(a,b,c);
        }
        return returnValue;
    }
    public int getNextVisualPositionFrom(JTextComponent a, int b, Position.Bias c, int d, Position.Bias[] e)
            throws BadLocationException {
        int returnValue =
            ((TextUI) (uis.elementAt(0))).getNextVisualPositionFrom(a,b,c,d,e);
        for (int i = 1; i < uis.size(); i++) {
            ((TextUI) (uis.elementAt(i))).getNextVisualPositionFrom(a,b,c,d,e);
        }
        return returnValue;
    }
    public void damageRange(JTextComponent a, int b, int c) {
        for (int i = 0; i < uis.size(); i++) {
            ((TextUI) (uis.elementAt(i))).damageRange(a,b,c);
        }
    }
    public void damageRange(JTextComponent a, int b, int c, Position.Bias d, Position.Bias e) {
        for (int i = 0; i < uis.size(); i++) {
            ((TextUI) (uis.elementAt(i))).damageRange(a,b,c,d,e);
        }
    }
    public EditorKit getEditorKit(JTextComponent a) {
        EditorKit returnValue =
            ((TextUI) (uis.elementAt(0))).getEditorKit(a);
        for (int i = 1; i < uis.size(); i++) {
            ((TextUI) (uis.elementAt(i))).getEditorKit(a);
        }
        return returnValue;
    }
    public View getRootView(JTextComponent a) {
        View returnValue =
            ((TextUI) (uis.elementAt(0))).getRootView(a);
        for (int i = 1; i < uis.size(); i++) {
            ((TextUI) (uis.elementAt(i))).getRootView(a);
        }
        return returnValue;
    }
    public boolean contains(JComponent a, int b, int c) {
        boolean returnValue =
            ((ComponentUI) (uis.elementAt(0))).contains(a,b,c);
        for (int i = 1; i < uis.size(); i++) {
            ((ComponentUI) (uis.elementAt(i))).contains(a,b,c);
        }
        return returnValue;
    }
    public void update(Graphics a, JComponent b) {
        for (int i = 0; i < uis.size(); i++) {
            ((ComponentUI) (uis.elementAt(i))).update(a,b);
        }
    }
    public static ComponentUI createUI(JComponent a) {
        ComponentUI mui = new MultiTextUI();
        return MultiLookAndFeel.createUIs(mui,
                                          ((MultiTextUI) mui).uis,
                                          a);
    }
    public void installUI(JComponent a) {
        for (int i = 0; i < uis.size(); i++) {
            ((ComponentUI) (uis.elementAt(i))).installUI(a);
        }
    }
    public void uninstallUI(JComponent a) {
        for (int i = 0; i < uis.size(); i++) {
            ((ComponentUI) (uis.elementAt(i))).uninstallUI(a);
        }
    }
    public void paint(Graphics a, JComponent b) {
        for (int i = 0; i < uis.size(); i++) {
            ((ComponentUI) (uis.elementAt(i))).paint(a,b);
        }
    }
    public Dimension getPreferredSize(JComponent a) {
        Dimension returnValue =
            ((ComponentUI) (uis.elementAt(0))).getPreferredSize(a);
        for (int i = 1; i < uis.size(); i++) {
            ((ComponentUI) (uis.elementAt(i))).getPreferredSize(a);
        }
        return returnValue;
    }
    public Dimension getMinimumSize(JComponent a) {
        Dimension returnValue =
            ((ComponentUI) (uis.elementAt(0))).getMinimumSize(a);
        for (int i = 1; i < uis.size(); i++) {
            ((ComponentUI) (uis.elementAt(i))).getMinimumSize(a);
        }
        return returnValue;
    }
    public Dimension getMaximumSize(JComponent a) {
        Dimension returnValue =
            ((ComponentUI) (uis.elementAt(0))).getMaximumSize(a);
        for (int i = 1; i < uis.size(); i++) {
            ((ComponentUI) (uis.elementAt(i))).getMaximumSize(a);
        }
        return returnValue;
    }
    public int getAccessibleChildrenCount(JComponent a) {
        int returnValue =
            ((ComponentUI) (uis.elementAt(0))).getAccessibleChildrenCount(a);
        for (int i = 1; i < uis.size(); i++) {
            ((ComponentUI) (uis.elementAt(i))).getAccessibleChildrenCount(a);
        }
        return returnValue;
    }
    public Accessible getAccessibleChild(JComponent a, int b) {
        Accessible returnValue =
            ((ComponentUI) (uis.elementAt(0))).getAccessibleChild(a,b);
        for (int i = 1; i < uis.size(); i++) {
            ((ComponentUI) (uis.elementAt(i))).getAccessibleChild(a,b);
        }
        return returnValue;
    }
}
