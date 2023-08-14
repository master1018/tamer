public class MultiTreeUI extends TreeUI {
    protected Vector uis = new Vector();
    public ComponentUI[] getUIs() {
        return MultiLookAndFeel.uisToArray(uis);
    }
    public Rectangle getPathBounds(JTree a, TreePath b) {
        Rectangle returnValue =
            ((TreeUI) (uis.elementAt(0))).getPathBounds(a,b);
        for (int i = 1; i < uis.size(); i++) {
            ((TreeUI) (uis.elementAt(i))).getPathBounds(a,b);
        }
        return returnValue;
    }
    public TreePath getPathForRow(JTree a, int b) {
        TreePath returnValue =
            ((TreeUI) (uis.elementAt(0))).getPathForRow(a,b);
        for (int i = 1; i < uis.size(); i++) {
            ((TreeUI) (uis.elementAt(i))).getPathForRow(a,b);
        }
        return returnValue;
    }
    public int getRowForPath(JTree a, TreePath b) {
        int returnValue =
            ((TreeUI) (uis.elementAt(0))).getRowForPath(a,b);
        for (int i = 1; i < uis.size(); i++) {
            ((TreeUI) (uis.elementAt(i))).getRowForPath(a,b);
        }
        return returnValue;
    }
    public int getRowCount(JTree a) {
        int returnValue =
            ((TreeUI) (uis.elementAt(0))).getRowCount(a);
        for (int i = 1; i < uis.size(); i++) {
            ((TreeUI) (uis.elementAt(i))).getRowCount(a);
        }
        return returnValue;
    }
    public TreePath getClosestPathForLocation(JTree a, int b, int c) {
        TreePath returnValue =
            ((TreeUI) (uis.elementAt(0))).getClosestPathForLocation(a,b,c);
        for (int i = 1; i < uis.size(); i++) {
            ((TreeUI) (uis.elementAt(i))).getClosestPathForLocation(a,b,c);
        }
        return returnValue;
    }
    public boolean isEditing(JTree a) {
        boolean returnValue =
            ((TreeUI) (uis.elementAt(0))).isEditing(a);
        for (int i = 1; i < uis.size(); i++) {
            ((TreeUI) (uis.elementAt(i))).isEditing(a);
        }
        return returnValue;
    }
    public boolean stopEditing(JTree a) {
        boolean returnValue =
            ((TreeUI) (uis.elementAt(0))).stopEditing(a);
        for (int i = 1; i < uis.size(); i++) {
            ((TreeUI) (uis.elementAt(i))).stopEditing(a);
        }
        return returnValue;
    }
    public void cancelEditing(JTree a) {
        for (int i = 0; i < uis.size(); i++) {
            ((TreeUI) (uis.elementAt(i))).cancelEditing(a);
        }
    }
    public void startEditingAtPath(JTree a, TreePath b) {
        for (int i = 0; i < uis.size(); i++) {
            ((TreeUI) (uis.elementAt(i))).startEditingAtPath(a,b);
        }
    }
    public TreePath getEditingPath(JTree a) {
        TreePath returnValue =
            ((TreeUI) (uis.elementAt(0))).getEditingPath(a);
        for (int i = 1; i < uis.size(); i++) {
            ((TreeUI) (uis.elementAt(i))).getEditingPath(a);
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
        ComponentUI mui = new MultiTreeUI();
        return MultiLookAndFeel.createUIs(mui,
                                          ((MultiTreeUI) mui).uis,
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
