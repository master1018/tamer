public class MetalScrollPaneUI extends BasicScrollPaneUI
{
    private PropertyChangeListener scrollBarSwapListener;
    public static ComponentUI createUI(JComponent x) {
        return new MetalScrollPaneUI();
    }
    public void installUI(JComponent c) {
        super.installUI(c);
        JScrollPane sp = (JScrollPane)c;
        updateScrollbarsFreeStanding();
    }
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        JScrollPane sp = (JScrollPane)c;
        JScrollBar hsb = sp.getHorizontalScrollBar();
        JScrollBar vsb = sp.getVerticalScrollBar();
        if (hsb != null) {
            hsb.putClientProperty( MetalScrollBarUI.FREE_STANDING_PROP, null);
        }
        if (vsb != null) {
            vsb.putClientProperty( MetalScrollBarUI.FREE_STANDING_PROP, null);
        }
    }
    public void installListeners(JScrollPane scrollPane) {
        super.installListeners(scrollPane);
        scrollBarSwapListener = createScrollBarSwapListener();
        scrollPane.addPropertyChangeListener(scrollBarSwapListener);
    }
    protected void uninstallListeners(JComponent c) {
        super.uninstallListeners(c);
        c.removePropertyChangeListener(scrollBarSwapListener);
    }
    @Deprecated
    public void uninstallListeners(JScrollPane scrollPane) {
        super.uninstallListeners(scrollPane);
        scrollPane.removePropertyChangeListener(scrollBarSwapListener);
    }
    private void updateScrollbarsFreeStanding() {
        if (scrollpane == null) {
            return;
        }
        Border border = scrollpane.getBorder();
        Object value;
        if (border instanceof MetalBorders.ScrollPaneBorder) {
            value = Boolean.FALSE;
        }
        else {
            value = Boolean.TRUE;
        }
        JScrollBar sb = scrollpane.getHorizontalScrollBar();
        if (sb != null) {
            sb.putClientProperty
                   (MetalScrollBarUI.FREE_STANDING_PROP, value);
        }
        sb = scrollpane.getVerticalScrollBar();
        if (sb != null) {
            sb.putClientProperty
                   (MetalScrollBarUI.FREE_STANDING_PROP, value);
        }
    }
    protected PropertyChangeListener createScrollBarSwapListener() {
        return new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                  String propertyName = e.getPropertyName();
                  if (propertyName.equals("verticalScrollBar") ||
                      propertyName.equals("horizontalScrollBar")) {
                      JScrollBar oldSB = (JScrollBar)e.getOldValue();
                      if (oldSB != null) {
                          oldSB.putClientProperty(
                              MetalScrollBarUI.FREE_STANDING_PROP, null);
                      }
                      JScrollBar newSB = (JScrollBar)e.getNewValue();
                      if (newSB != null) {
                          newSB.putClientProperty(
                              MetalScrollBarUI.FREE_STANDING_PROP,
                              Boolean.FALSE);
                      }
                  }
                  else if ("border".equals(propertyName)) {
                      updateScrollbarsFreeStanding();
                  }
        }};
    }
}
