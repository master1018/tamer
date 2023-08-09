public class SynthTabbedPaneUI extends BasicTabbedPaneUI
                               implements PropertyChangeListener, SynthUI {
    private int tabOverlap = 0;
    private boolean extendTabsToBase = false;
    private SynthContext tabAreaContext;
    private SynthContext tabContext;
    private SynthContext tabContentContext;
    private SynthStyle style;
    private SynthStyle tabStyle;
    private SynthStyle tabAreaStyle;
    private SynthStyle tabContentStyle;
    private Rectangle textRect = new Rectangle();
    private Rectangle iconRect = new Rectangle();
    private Rectangle tabAreaBounds = new Rectangle();
    private boolean tabAreaStatesMatchSelectedTab = false;
    private boolean nudgeSelectedLabel = true;
    private boolean selectedTabIsPressed = false;
    public static ComponentUI createUI(JComponent c) {
        return new SynthTabbedPaneUI();
    }
     private boolean scrollableTabLayoutEnabled() {
        return (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT);
    }
    @Override
    protected void installDefaults() {
        updateStyle(tabPane);
    }
    private void updateStyle(JTabbedPane c) {
        SynthContext context = getContext(c, ENABLED);
        SynthStyle oldStyle = style;
        style = SynthLookAndFeel.updateStyle(context, this);
        if (style != oldStyle) {
            tabRunOverlay =
                style.getInt(context, "TabbedPane.tabRunOverlay", 0);
            tabOverlap = style.getInt(context, "TabbedPane.tabOverlap", 0);
            extendTabsToBase = style.getBoolean(context,
                    "TabbedPane.extendTabsToBase", false);
            textIconGap = style.getInt(context, "TabbedPane.textIconGap", 0);
            selectedTabPadInsets = (Insets)style.get(context,
                "TabbedPane.selectedTabPadInsets");
            if (selectedTabPadInsets == null) {
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
            }
            tabAreaStatesMatchSelectedTab = style.getBoolean(context,
                    "TabbedPane.tabAreaStatesMatchSelectedTab", false);
            nudgeSelectedLabel = style.getBoolean(context,
                    "TabbedPane.nudgeSelectedLabel", true);
            if (oldStyle != null) {
                uninstallKeyboardActions();
                installKeyboardActions();
            }
        }
        context.dispose();
        if (tabContext != null) {
            tabContext.dispose();
        }
        tabContext = getContext(c, Region.TABBED_PANE_TAB, ENABLED);
        this.tabStyle = SynthLookAndFeel.updateStyle(tabContext, this);
        tabInsets = tabStyle.getInsets(tabContext, null);
        if (tabAreaContext != null) {
            tabAreaContext.dispose();
        }
        tabAreaContext = getContext(c, Region.TABBED_PANE_TAB_AREA, ENABLED);
        this.tabAreaStyle = SynthLookAndFeel.updateStyle(tabAreaContext, this);
        tabAreaInsets = tabAreaStyle.getInsets(tabAreaContext, null);
        if (tabContentContext != null) {
            tabContentContext.dispose();
        }
        tabContentContext = getContext(c, Region.TABBED_PANE_CONTENT, ENABLED);
        this.tabContentStyle = SynthLookAndFeel.updateStyle(tabContentContext,
                                                            this);
        contentBorderInsets =
            tabContentStyle.getInsets(tabContentContext, null);
    }
    @Override
    protected void installListeners() {
        super.installListeners();
        tabPane.addPropertyChangeListener(this);
    }
    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        tabPane.removePropertyChangeListener(this);
    }
    @Override
    protected void uninstallDefaults() {
        SynthContext context = getContext(tabPane, ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style = null;
        tabStyle.uninstallDefaults(tabContext);
        tabContext.dispose();
        tabContext = null;
        tabStyle = null;
        tabAreaStyle.uninstallDefaults(tabAreaContext);
        tabAreaContext.dispose();
        tabAreaContext = null;
        tabAreaStyle = null;
        tabContentStyle.uninstallDefaults(tabContentContext);
        tabContentContext.dispose();
        tabContentContext = null;
        tabContentStyle = null;
    }
    @Override
    public SynthContext getContext(JComponent c) {
        return getContext(c, SynthLookAndFeel.getComponentState(c));
    }
    private SynthContext getContext(JComponent c, int state) {
        return SynthContext.getContext(SynthContext.class, c,
                    SynthLookAndFeel.getRegion(c),style, state);
    }
    private SynthContext getContext(JComponent c, Region subregion, int state){
        SynthStyle style = null;
        Class klass = SynthContext.class;
        if (subregion == Region.TABBED_PANE_TAB) {
            style = tabStyle;
        }
        else if (subregion == Region.TABBED_PANE_TAB_AREA) {
            style = tabAreaStyle;
        }
        else if (subregion == Region.TABBED_PANE_CONTENT) {
            style = tabContentStyle;
        }
        return SynthContext.getContext(klass, c, subregion, style, state);
    }
    @Override
    protected JButton createScrollButton(int direction) {
        if (UIManager.getBoolean("TabbedPane.useBasicArrows")) {
            JButton btn = super.createScrollButton(direction);
            btn.setBorder(BorderFactory.createEmptyBorder());
            return btn;
        }
        return new SynthScrollableTabButton(direction);
    }
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (SynthLookAndFeel.shouldUpdateStyle(e)) {
            updateStyle(tabPane);
        }
    }
    @Override
    protected MouseListener createMouseListener() {
        final MouseListener delegate = super.createMouseListener();
        final MouseMotionListener delegate2 = (MouseMotionListener)delegate;
        return new MouseListener() {
            public void mouseClicked(MouseEvent e) { delegate.mouseClicked(e); }
            public void mouseEntered(MouseEvent e) { delegate.mouseEntered(e); }
            public void mouseExited(MouseEvent e) { delegate.mouseExited(e); }
            public void mousePressed(MouseEvent e) {
                if (!tabPane.isEnabled()) {
                    return;
                }
                int tabIndex = tabForCoordinate(tabPane, e.getX(), e.getY());
                if (tabIndex >= 0 && tabPane.isEnabledAt(tabIndex)) {
                    if (tabIndex == tabPane.getSelectedIndex()) {
                        selectedTabIsPressed = true;
                        tabPane.repaint();
                    }
                }
                delegate.mousePressed(e);
            }
            public void mouseReleased(MouseEvent e) {
                if (selectedTabIsPressed) {
                    selectedTabIsPressed = false;
                    tabPane.repaint();
                }
                delegate.mouseReleased(e);
                delegate2.mouseMoved(e);
            }
        };
    }
    @Override
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        if (nudgeSelectedLabel) {
            return super.getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
        } else {
            return 0;
        }
    }
    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        if (nudgeSelectedLabel) {
            return super.getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
        } else {
            return 0;
        }
    }
    @Override
    public void update(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        SynthLookAndFeel.update(context, g);
        context.getPainter().paintTabbedPaneBackground(context,
                          g, 0, 0, c.getWidth(), c.getHeight());
        paint(context, g);
        context.dispose();
    }
    @Override
    protected int getBaseline(int tab) {
        if (tabPane.getTabComponentAt(tab) != null ||
                getTextViewForTab(tab) != null) {
            return super.getBaseline(tab);
        }
        String title = tabPane.getTitleAt(tab);
        Font font = tabContext.getStyle().getFont(tabContext);
        FontMetrics metrics = getFontMetrics(font);
        Icon icon = getIconForTab(tab);
        textRect.setBounds(0, 0, 0, 0);
        iconRect.setBounds(0, 0, 0, 0);
        calcRect.setBounds(0, 0, Short.MAX_VALUE, maxTabHeight);
        tabContext.getStyle().getGraphicsUtils(tabContext).layoutText(
                tabContext, metrics, title, icon, SwingUtilities.CENTER,
                SwingUtilities.CENTER, SwingUtilities.LEADING,
                SwingUtilities.CENTER, calcRect,
                iconRect, textRect, textIconGap);
        return textRect.y + metrics.getAscent() + getBaselineOffset();
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintTabbedPaneBorder(context, g, x, y, w, h);
    }
    @Override
    public void paint(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        paint(context, g);
        context.dispose();
    }
    protected void paint(SynthContext context, Graphics g) {
        int selectedIndex = tabPane.getSelectedIndex();
        int tabPlacement = tabPane.getTabPlacement();
        ensureCurrentLayout();
        if (!scrollableTabLayoutEnabled()) { 
            Insets insets = tabPane.getInsets();
            int x = insets.left;
            int y = insets.top;
            int width = tabPane.getWidth() - insets.left - insets.right;
            int height = tabPane.getHeight() - insets.top - insets.bottom;
            int size;
            switch(tabPlacement) {
            case LEFT:
                width = calculateTabAreaWidth(tabPlacement, runCount,
                                              maxTabWidth);
                break;
            case RIGHT:
                size = calculateTabAreaWidth(tabPlacement, runCount,
                                             maxTabWidth);
                x = x + width - size;
                width = size;
                break;
            case BOTTOM:
                size = calculateTabAreaHeight(tabPlacement, runCount,
                                              maxTabHeight);
                y = y + height - size;
                height = size;
                break;
            case TOP:
            default:
                height = calculateTabAreaHeight(tabPlacement, runCount,
                                                maxTabHeight);
            }
            tabAreaBounds.setBounds(x, y, width, height);
            if (g.getClipBounds().intersects(tabAreaBounds)) {
                paintTabArea(tabAreaContext, g, tabPlacement,
                         selectedIndex, tabAreaBounds);
            }
        }
        paintContentBorder(tabContentContext, g, tabPlacement, selectedIndex);
    }
    protected void paintTabArea(Graphics g, int tabPlacement,
                                int selectedIndex) {
        Insets insets = tabPane.getInsets();
        int x = insets.left;
        int y = insets.top;
        int width = tabPane.getWidth() - insets.left - insets.right;
        int height = tabPane.getHeight() - insets.top - insets.bottom;
        paintTabArea(tabAreaContext, g, tabPlacement, selectedIndex,
                     new Rectangle(x, y, width, height));
    }
    private void paintTabArea(SynthContext ss, Graphics g,
                                int tabPlacement, int selectedIndex,
                                Rectangle tabAreaBounds) {
        Rectangle clipRect = g.getClipBounds();
        if (tabAreaStatesMatchSelectedTab && selectedIndex >= 0) {
            updateTabContext(selectedIndex, true, selectedTabIsPressed,
                              (getRolloverTab() == selectedIndex),
                              (getFocusIndex() == selectedIndex));
            ss.setComponentState(tabContext.getComponentState());
        } else {
            ss.setComponentState(SynthConstants.ENABLED);
        }
        SynthLookAndFeel.updateSubregion(ss, g, tabAreaBounds);
        ss.getPainter().paintTabbedPaneTabAreaBackground(ss, g,
             tabAreaBounds.x, tabAreaBounds.y, tabAreaBounds.width,
             tabAreaBounds.height, tabPlacement);
        ss.getPainter().paintTabbedPaneTabAreaBorder(ss, g, tabAreaBounds.x,
             tabAreaBounds.y, tabAreaBounds.width, tabAreaBounds.height,
             tabPlacement);
        int tabCount = tabPane.getTabCount();
        iconRect.setBounds(0, 0, 0, 0);
        textRect.setBounds(0, 0, 0, 0);
        for (int i = runCount - 1; i >= 0; i--) {
            int start = tabRuns[i];
            int next = tabRuns[(i == runCount - 1)? 0 : i + 1];
            int end = (next != 0? next - 1: tabCount - 1);
            for (int j = start; j <= end; j++) {
                if (rects[j].intersects(clipRect) && selectedIndex != j) {
                    paintTab(tabContext, g, tabPlacement, rects, j, iconRect,
                             textRect);
                }
            }
        }
        if (selectedIndex >= 0) {
            if (rects[selectedIndex].intersects(clipRect)) {
                paintTab(tabContext, g, tabPlacement, rects, selectedIndex,
                         iconRect, textRect);
            }
        }
    }
    @Override
    protected void setRolloverTab(int index) {
        int oldRolloverTab = getRolloverTab();
        super.setRolloverTab(index);
        Rectangle r = null;
        if (oldRolloverTab != index && tabAreaStatesMatchSelectedTab) {
            tabPane.repaint();
        } else {
            if ((oldRolloverTab >= 0) && (oldRolloverTab < tabPane.getTabCount())) {
                r = getTabBounds(tabPane, oldRolloverTab);
                if (r != null) {
                    tabPane.repaint(r);
                }
            }
            if (index >= 0) {
                r = getTabBounds(tabPane, index);
                if (r != null) {
                    tabPane.repaint(r);
                }
            }
        }
    }
    private void paintTab(SynthContext ss, Graphics g,
                            int tabPlacement, Rectangle[] rects, int tabIndex,
                            Rectangle iconRect, Rectangle textRect) {
        Rectangle tabRect = rects[tabIndex];
        int selectedIndex = tabPane.getSelectedIndex();
        boolean isSelected = selectedIndex == tabIndex;
        updateTabContext(tabIndex, isSelected, isSelected && selectedTabIsPressed,
                            (getRolloverTab() == tabIndex),
                            (getFocusIndex() == tabIndex));
        SynthLookAndFeel.updateSubregion(ss, g, tabRect);
        int x = tabRect.x;
        int y = tabRect.y;
        int height = tabRect.height;
        int width = tabRect.width;
        int placement = tabPane.getTabPlacement();
        if (extendTabsToBase && runCount > 1) {
            if (selectedIndex >= 0) {
                Rectangle r = rects[selectedIndex];
                switch (placement) {
                    case TOP:
                        int bottomY = r.y + r.height;
                        height = bottomY - tabRect.y;
                        break;
                    case LEFT:
                        int rightX = r.x + r.width;
                        width = rightX - tabRect.x;
                        break;
                    case BOTTOM:
                        int topY = r.y;
                        height = (tabRect.y + tabRect.height) - topY;
                        y = topY;
                        break;
                    case RIGHT:
                        int leftX = r.x;
                        width = (tabRect.x + tabRect.width) - leftX;
                        x = leftX;
                        break;
                }
            }
        }
        tabContext.getPainter().paintTabbedPaneTabBackground(tabContext, g,
                x, y, width, height, tabIndex, placement);
        tabContext.getPainter().paintTabbedPaneTabBorder(tabContext, g,
                x, y, width, height, tabIndex, placement);
        if (tabPane.getTabComponentAt(tabIndex) == null) {
            String title = tabPane.getTitleAt(tabIndex);
            Font font = ss.getStyle().getFont(ss);
            FontMetrics metrics = SwingUtilities2.getFontMetrics(tabPane, g, font);
            Icon icon = getIconForTab(tabIndex);
            layoutLabel(ss, tabPlacement, metrics, tabIndex, title, icon,
                    tabRect, iconRect, textRect, isSelected);
            paintText(ss, g, tabPlacement, font, metrics,
                    tabIndex, title, textRect, isSelected);
            paintIcon(g, tabPlacement, tabIndex, icon, iconRect, isSelected);
        }
    }
    private void layoutLabel(SynthContext ss, int tabPlacement,
                               FontMetrics metrics, int tabIndex,
                               String title, Icon icon,
                               Rectangle tabRect, Rectangle iconRect,
                               Rectangle textRect, boolean isSelected ) {
        View v = getTextViewForTab(tabIndex);
        if (v != null) {
            tabPane.putClientProperty("html", v);
        }
        textRect.x = textRect.y = iconRect.x = iconRect.y = 0;
        ss.getStyle().getGraphicsUtils(ss).layoutText(ss, metrics, title,
                         icon, SwingUtilities.CENTER, SwingUtilities.CENTER,
                         SwingUtilities.LEADING, SwingUtilities.CENTER,
                         tabRect, iconRect, textRect, textIconGap);
        tabPane.putClientProperty("html", null);
        int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
        int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
        iconRect.x += xNudge;
        iconRect.y += yNudge;
        textRect.x += xNudge;
        textRect.y += yNudge;
    }
    private void paintText(SynthContext ss,
                             Graphics g, int tabPlacement,
                             Font font, FontMetrics metrics, int tabIndex,
                             String title, Rectangle textRect,
                             boolean isSelected) {
        g.setFont(font);
        View v = getTextViewForTab(tabIndex);
        if (v != null) {
            v.paint(g, textRect);
        } else {
            int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);
            g.setColor(ss.getStyle().getColor(ss, ColorType.TEXT_FOREGROUND));
            ss.getStyle().getGraphicsUtils(ss).paintText(ss, g, title,
                                  textRect, mnemIndex);
        }
    }
    private void paintContentBorder(SynthContext ss, Graphics g,
                                      int tabPlacement, int selectedIndex) {
        int width = tabPane.getWidth();
        int height = tabPane.getHeight();
        Insets insets = tabPane.getInsets();
        int x = insets.left;
        int y = insets.top;
        int w = width - insets.right - insets.left;
        int h = height - insets.top - insets.bottom;
        switch(tabPlacement) {
          case LEFT:
              x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
              w -= (x - insets.left);
              break;
          case RIGHT:
              w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
              break;
          case BOTTOM:
              h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
              break;
          case TOP:
          default:
              y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
              h -= (y - insets.top);
        }
        SynthLookAndFeel.updateSubregion(ss, g, new Rectangle(x, y, w, h));
        ss.getPainter().paintTabbedPaneContentBackground(ss, g, x, y,
                                                           w, h);
        ss.getPainter().paintTabbedPaneContentBorder(ss, g, x, y, w, h);
    }
    private void ensureCurrentLayout() {
        if (!tabPane.isValid()) {
            tabPane.validate();
        }
        if (!tabPane.isValid()) {
            TabbedPaneLayout layout = (TabbedPaneLayout)tabPane.getLayout();
            layout.calculateLayoutInfo();
        }
    }
    @Override
    protected int calculateMaxTabHeight(int tabPlacement) {
        FontMetrics metrics = getFontMetrics(tabContext.getStyle().getFont(
                                             tabContext));
        int tabCount = tabPane.getTabCount();
        int result = 0;
        int fontHeight = metrics.getHeight();
        for(int i = 0; i < tabCount; i++) {
            result = Math.max(calculateTabHeight(tabPlacement, i, fontHeight), result);
        }
        return result;
    }
    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex,
                                    FontMetrics metrics) {
        Icon icon = getIconForTab(tabIndex);
        Insets tabInsets = getTabInsets(tabPlacement, tabIndex);
        int width = tabInsets.left + tabInsets.right;
        Component tabComponent = tabPane.getTabComponentAt(tabIndex);
        if (tabComponent != null) {
            width += tabComponent.getPreferredSize().width;
        } else {
            if (icon != null) {
                width += icon.getIconWidth() + textIconGap;
            }
            View v = getTextViewForTab(tabIndex);
            if (v != null) {
                width += (int) v.getPreferredSpan(View.X_AXIS);
            } else {
                String title = tabPane.getTitleAt(tabIndex);
                width += tabContext.getStyle().getGraphicsUtils(tabContext).
                        computeStringWidth(tabContext, metrics.getFont(),
                                metrics, title);
            }
        }
        return width;
    }
    @Override
    protected int calculateMaxTabWidth(int tabPlacement) {
        FontMetrics metrics = getFontMetrics(tabContext.getStyle().getFont(
                                     tabContext));
        int tabCount = tabPane.getTabCount();
        int result = 0;
        for(int i = 0; i < tabCount; i++) {
            result = Math.max(calculateTabWidth(tabPlacement, i, metrics),
                              result);
        }
        return result;
    }
    @Override
    protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        updateTabContext(tabIndex, false, false, false,
                          (getFocusIndex() == tabIndex));
        return tabInsets;
    }
    @Override
    protected FontMetrics getFontMetrics() {
        return getFontMetrics(tabContext.getStyle().getFont(tabContext));
    }
    private FontMetrics getFontMetrics(Font font) {
        return tabPane.getFontMetrics(font);
    }
    private void updateTabContext(int index, boolean selected,
                                  boolean isMouseDown, boolean isMouseOver, boolean hasFocus) {
        int state = 0;
        if (!tabPane.isEnabled() || !tabPane.isEnabledAt(index)) {
            state |= SynthConstants.DISABLED;
            if (selected) {
                state |= SynthConstants.SELECTED;
            }
        }
        else if (selected) {
            state |= (SynthConstants.ENABLED | SynthConstants.SELECTED);
            if (isMouseOver && UIManager.getBoolean("TabbedPane.isTabRollover")) {
                state |= SynthConstants.MOUSE_OVER;
            }
        }
        else if (isMouseOver) {
            state |= (SynthConstants.ENABLED | SynthConstants.MOUSE_OVER);
        }
        else {
            state = SynthLookAndFeel.getComponentState(tabPane);
            state &= ~SynthConstants.FOCUSED; 
        }
        if (hasFocus && tabPane.hasFocus()) {
            state |= SynthConstants.FOCUSED; 
        }
        if (isMouseDown) {
            state |= SynthConstants.PRESSED;
        }
        tabContext.setComponentState(state);
    }
    @Override
    protected LayoutManager createLayoutManager() {
        if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            return super.createLayoutManager();
        } else { 
            return new TabbedPaneLayout() {
                @Override
                public void calculateLayoutInfo() {
                    super.calculateLayoutInfo();
                    if (tabOverlap != 0) {
                        int tabCount = tabPane.getTabCount();
                        boolean ltr = tabPane.getComponentOrientation().isLeftToRight();
                        for (int i = runCount - 1; i >= 0; i--) {
                            int start = tabRuns[i];
                            int next = tabRuns[(i == runCount - 1)? 0 : i + 1];
                            int end = (next != 0? next - 1: tabCount - 1);
                            for (int j = start+1; j <= end; j++) {
                                int xshift = 0;
                                int yshift = 0;
                                switch (tabPane.getTabPlacement()) {
                                    case JTabbedPane.TOP:
                                    case JTabbedPane.BOTTOM:
                                        xshift = ltr ? tabOverlap : -tabOverlap;
                                        break;
                                    case JTabbedPane.LEFT:
                                    case JTabbedPane.RIGHT:
                                        yshift = tabOverlap;
                                        break;
                                    default: 
                                }
                                rects[j].x += xshift;
                                rects[j].y += yshift;
                                rects[j].width += Math.abs(xshift);
                                rects[j].height += Math.abs(yshift);
                            }
                        }
                    }
                }
            };
        }
    }
    private class SynthScrollableTabButton extends SynthArrowButton implements
            UIResource {
        public SynthScrollableTabButton(int direction) {
            super(direction);
            setName("TabbedPane.button");
        }
    }
}
