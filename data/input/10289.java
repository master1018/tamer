public class MultiSplitPaneUI extends SplitPaneUI {
    protected Vector uis = new Vector();
    public ComponentUI[] getUIs() {
        return MultiLookAndFeel.uisToArray(uis);
    }
    public void resetToPreferredSizes(JSplitPane a) {
        for (int i = 0; i < uis.size(); i++) {
            ((SplitPaneUI) (uis.elementAt(i))).resetToPreferredSizes(a);
        }
    }
    public void setDividerLocation(JSplitPane a, int b) {
        for (int i = 0; i < uis.size(); i++) {
            ((SplitPaneUI) (uis.elementAt(i))).setDividerLocation(a,b);
        }
    }
    public int getDividerLocation(JSplitPane a) {
        int returnValue =
            ((SplitPaneUI) (uis.elementAt(0))).getDividerLocation(a);
        for (int i = 1; i < uis.size(); i++) {
            ((SplitPaneUI) (uis.elementAt(i))).getDividerLocation(a);
        }
        return returnValue;
    }
    public int getMinimumDividerLocation(JSplitPane a) {
        int returnValue =
            ((SplitPaneUI) (uis.elementAt(0))).getMinimumDividerLocation(a);
        for (int i = 1; i < uis.size(); i++) {
            ((SplitPaneUI) (uis.elementAt(i))).getMinimumDividerLocation(a);
        }
        return returnValue;
    }
    public int getMaximumDividerLocation(JSplitPane a) {
        int returnValue =
            ((SplitPaneUI) (uis.elementAt(0))).getMaximumDividerLocation(a);
        for (int i = 1; i < uis.size(); i++) {
            ((SplitPaneUI) (uis.elementAt(i))).getMaximumDividerLocation(a);
        }
        return returnValue;
    }
    public void finishedPaintingChildren(JSplitPane a, Graphics b) {
        for (int i = 0; i < uis.size(); i++) {
            ((SplitPaneUI) (uis.elementAt(i))).finishedPaintingChildren(a,b);
        }
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
        ComponentUI mui = new MultiSplitPaneUI();
        return MultiLookAndFeel.createUIs(mui,
                                          ((MultiSplitPaneUI) mui).uis,
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
