public class BasicTabbedPaneUI extends TabbedPaneUI implements SwingConstants {
    protected JTabbedPane tabPane;
    protected Color highlight;
    protected Color lightHighlight;
    protected Color shadow;
    protected Color darkShadow;
    protected Color focus;
    private   Color selectedColor;
    protected int textIconGap;
    protected int tabRunOverlay;
    protected Insets tabInsets;
    protected Insets selectedTabPadInsets;
    protected Insets tabAreaInsets;
    protected Insets contentBorderInsets;
    private boolean tabsOverlapBorder;
    private boolean tabsOpaque = true;
    private boolean contentOpaque = true;
    @Deprecated
    protected KeyStroke upKey;
    @Deprecated
    protected KeyStroke downKey;
    @Deprecated
    protected KeyStroke leftKey;
    @Deprecated
    protected KeyStroke rightKey;
    protected int tabRuns[] = new int[10];
    protected int runCount = 0;
    protected int selectedRun = -1;
    protected Rectangle rects[] = new Rectangle[0];
    protected int maxTabHeight;
    protected int maxTabWidth;
    protected ChangeListener tabChangeListener;
    protected PropertyChangeListener propertyChangeListener;
    protected MouseListener mouseListener;
    protected FocusListener focusListener;
    private Insets currentPadInsets = new Insets(0,0,0,0);
    private Insets currentTabAreaInsets = new Insets(0,0,0,0);
    private Component visibleComponent;
    private Vector<View> htmlViews;
    private Hashtable<Integer, Integer> mnemonicToIndexMap;
    private InputMap mnemonicInputMap;
    private ScrollableTabSupport tabScroller;
    private TabContainer tabContainer;
    protected transient Rectangle calcRect = new Rectangle(0,0,0,0);
    private int focusIndex;
    private Handler handler;
    private int rolloverTabIndex;
    private boolean isRunsDirty;
    private boolean calculatedBaseline;
    private int baseline;
    public static ComponentUI createUI(JComponent c) {
        return new BasicTabbedPaneUI();
    }
    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions(Actions.NEXT));
        map.put(new Actions(Actions.PREVIOUS));
        map.put(new Actions(Actions.RIGHT));
        map.put(new Actions(Actions.LEFT));
        map.put(new Actions(Actions.UP));
        map.put(new Actions(Actions.DOWN));
        map.put(new Actions(Actions.PAGE_UP));
        map.put(new Actions(Actions.PAGE_DOWN));
        map.put(new Actions(Actions.REQUEST_FOCUS));
        map.put(new Actions(Actions.REQUEST_FOCUS_FOR_VISIBLE));
        map.put(new Actions(Actions.SET_SELECTED));
        map.put(new Actions(Actions.SELECT_FOCUSED));
        map.put(new Actions(Actions.SCROLL_FORWARD));
        map.put(new Actions(Actions.SCROLL_BACKWARD));
    }
    public void installUI(JComponent c) {
        this.tabPane = (JTabbedPane)c;
        calculatedBaseline = false;
        rolloverTabIndex = -1;
        focusIndex = -1;
        c.setLayout(createLayoutManager());
        installComponents();
        installDefaults();
        installListeners();
        installKeyboardActions();
    }
    public void uninstallUI(JComponent c) {
        uninstallKeyboardActions();
        uninstallListeners();
        uninstallDefaults();
        uninstallComponents();
        c.setLayout(null);
        this.tabPane = null;
    }
    protected LayoutManager createLayoutManager() {
        if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            return new TabbedPaneScrollLayout();
        } else { 
            return new TabbedPaneLayout();
        }
    }
    private boolean scrollableTabLayoutEnabled() {
        return (tabPane.getLayout() instanceof TabbedPaneScrollLayout);
    }
    protected void installComponents() {
        if (scrollableTabLayoutEnabled()) {
            if (tabScroller == null) {
                tabScroller = new ScrollableTabSupport(tabPane.getTabPlacement());
                tabPane.add(tabScroller.viewport);
            }
        }
        installTabContainer();
    }
    private void installTabContainer() {
         for (int i = 0; i < tabPane.getTabCount(); i++) {
             Component tabComponent = tabPane.getTabComponentAt(i);
             if (tabComponent != null) {
                 if(tabContainer == null) {
                     tabContainer = new TabContainer();
                 }
                 tabContainer.add(tabComponent);
             }
         }
         if(tabContainer == null) {
             return;
         }
         if (scrollableTabLayoutEnabled()) {
             tabScroller.tabPanel.add(tabContainer);
         } else {
             tabPane.add(tabContainer);
         }
    }
    protected JButton createScrollButton(int direction) {
        if (direction != SOUTH && direction != NORTH && direction != EAST &&
                                  direction != WEST) {
            throw new IllegalArgumentException("Direction must be one of: " +
                                               "SOUTH, NORTH, EAST or WEST");
        }
        return new ScrollableTabButton(direction);
    }
    protected void uninstallComponents() {
        uninstallTabContainer();
        if (scrollableTabLayoutEnabled()) {
            tabPane.remove(tabScroller.viewport);
            tabPane.remove(tabScroller.scrollForwardButton);
            tabPane.remove(tabScroller.scrollBackwardButton);
            tabScroller = null;
        }
    }
    private void uninstallTabContainer() {
         if(tabContainer == null) {
             return;
         }
         tabContainer.notifyTabbedPane = false;
         tabContainer.removeAll();
         if(scrollableTabLayoutEnabled()) {
             tabContainer.remove(tabScroller.croppedEdge);
             tabScroller.tabPanel.remove(tabContainer);
         } else {
           tabPane.remove(tabContainer);
         }
         tabContainer = null;
    }
    protected void installDefaults() {
        LookAndFeel.installColorsAndFont(tabPane, "TabbedPane.background",
                                    "TabbedPane.foreground", "TabbedPane.font");
        highlight = UIManager.getColor("TabbedPane.light");
        lightHighlight = UIManager.getColor("TabbedPane.highlight");
        shadow = UIManager.getColor("TabbedPane.shadow");
        darkShadow = UIManager.getColor("TabbedPane.darkShadow");
        focus = UIManager.getColor("TabbedPane.focus");
        selectedColor = UIManager.getColor("TabbedPane.selected");
        textIconGap = UIManager.getInt("TabbedPane.textIconGap");
        tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
        selectedTabPadInsets = UIManager.getInsets("TabbedPane.selectedTabPadInsets");
        tabAreaInsets = UIManager.getInsets("TabbedPane.tabAreaInsets");
        tabsOverlapBorder = UIManager.getBoolean("TabbedPane.tabsOverlapBorder");
        contentBorderInsets = UIManager.getInsets("TabbedPane.contentBorderInsets");
        tabRunOverlay = UIManager.getInt("TabbedPane.tabRunOverlay");
        tabsOpaque = UIManager.getBoolean("TabbedPane.tabsOpaque");
        contentOpaque = UIManager.getBoolean("TabbedPane.contentOpaque");
        Object opaque = UIManager.get("TabbedPane.opaque");
        if (opaque == null) {
            opaque = Boolean.FALSE;
        }
        LookAndFeel.installProperty(tabPane, "opaque", opaque);
        if (tabInsets == null) tabInsets = new Insets(0,4,1,4);
        if (selectedTabPadInsets == null) selectedTabPadInsets = new Insets(2,2,2,1);
        if (tabAreaInsets == null) tabAreaInsets = new Insets(3,2,0,2);
        if (contentBorderInsets == null) contentBorderInsets = new Insets(2,2,3,3);
    }
    protected void uninstallDefaults() {
        highlight = null;
        lightHighlight = null;
        shadow = null;
        darkShadow = null;
        focus = null;
        tabInsets = null;
        selectedTabPadInsets = null;
        tabAreaInsets = null;
        contentBorderInsets = null;
    }
    protected void installListeners() {
        if ((propertyChangeListener = createPropertyChangeListener()) != null) {
            tabPane.addPropertyChangeListener(propertyChangeListener);
        }
        if ((tabChangeListener = createChangeListener()) != null) {
            tabPane.addChangeListener(tabChangeListener);
        }
        if ((mouseListener = createMouseListener()) != null) {
            tabPane.addMouseListener(mouseListener);
        }
        tabPane.addMouseMotionListener(getHandler());
        if ((focusListener = createFocusListener()) != null) {
            tabPane.addFocusListener(focusListener);
        }
        tabPane.addContainerListener(getHandler());
        if (tabPane.getTabCount()>0) {
            htmlViews = createHTMLVector();
        }
    }
    protected void uninstallListeners() {
        if (mouseListener != null) {
            tabPane.removeMouseListener(mouseListener);
            mouseListener = null;
        }
        tabPane.removeMouseMotionListener(getHandler());
        if (focusListener != null) {
            tabPane.removeFocusListener(focusListener);
            focusListener = null;
        }
        tabPane.removeContainerListener(getHandler());
        if (htmlViews!=null) {
            htmlViews.removeAllElements();
            htmlViews = null;
        }
        if (tabChangeListener != null) {
            tabPane.removeChangeListener(tabChangeListener);
            tabChangeListener = null;
        }
        if (propertyChangeListener != null) {
            tabPane.removePropertyChangeListener(propertyChangeListener);
            propertyChangeListener = null;
        }
        handler = null;
    }
    protected MouseListener createMouseListener() {
        return getHandler();
    }
    protected FocusListener createFocusListener() {
        return getHandler();
    }
    protected ChangeListener createChangeListener() {
        return getHandler();
    }
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    protected void installKeyboardActions() {
        InputMap km = getInputMap(JComponent.
                                  WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        SwingUtilities.replaceUIInputMap(tabPane, JComponent.
                                         WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                                         km);
        km = getInputMap(JComponent.WHEN_FOCUSED);
        SwingUtilities.replaceUIInputMap(tabPane, JComponent.WHEN_FOCUSED, km);
        LazyActionMap.installLazyActionMap(tabPane, BasicTabbedPaneUI.class,
                                           "TabbedPane.actionMap");
        updateMnemonics();
    }
    InputMap getInputMap(int condition) {
        if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
            return (InputMap)DefaultLookup.get(tabPane, this,
                                               "TabbedPane.ancestorInputMap");
        }
        else if (condition == JComponent.WHEN_FOCUSED) {
            return (InputMap)DefaultLookup.get(tabPane, this,
                                               "TabbedPane.focusInputMap");
        }
        return null;
    }
    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIActionMap(tabPane, null);
        SwingUtilities.replaceUIInputMap(tabPane, JComponent.
                                         WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                                         null);
        SwingUtilities.replaceUIInputMap(tabPane, JComponent.WHEN_FOCUSED,
                                         null);
        SwingUtilities.replaceUIInputMap(tabPane,
                                         JComponent.WHEN_IN_FOCUSED_WINDOW,
                                         null);
        mnemonicToIndexMap = null;
        mnemonicInputMap = null;
    }
    private void updateMnemonics() {
        resetMnemonics();
        for (int counter = tabPane.getTabCount() - 1; counter >= 0;
             counter--) {
            int mnemonic = tabPane.getMnemonicAt(counter);
            if (mnemonic > 0) {
                addMnemonic(counter, mnemonic);
            }
        }
    }
    private void resetMnemonics() {
        if (mnemonicToIndexMap != null) {
            mnemonicToIndexMap.clear();
            mnemonicInputMap.clear();
        }
    }
    private void addMnemonic(int index, int mnemonic) {
        if (mnemonicToIndexMap == null) {
            initMnemonics();
        }
        mnemonicInputMap.put(KeyStroke.getKeyStroke(mnemonic, Event.ALT_MASK),
                             "setSelectedIndex");
        mnemonicToIndexMap.put(Integer.valueOf(mnemonic), Integer.valueOf(index));
    }
    private void initMnemonics() {
        mnemonicToIndexMap = new Hashtable<Integer, Integer>();
        mnemonicInputMap = new ComponentInputMapUIResource(tabPane);
        mnemonicInputMap.setParent(SwingUtilities.getUIInputMap(tabPane,
                              JComponent.WHEN_IN_FOCUSED_WINDOW));
        SwingUtilities.replaceUIInputMap(tabPane,
                              JComponent.WHEN_IN_FOCUSED_WINDOW,
                                         mnemonicInputMap);
    }
    private void setRolloverTab(int x, int y) {
        setRolloverTab(tabForCoordinate(tabPane, x, y, false));
    }
    protected void setRolloverTab(int index) {
        rolloverTabIndex = index;
    }
    protected int getRolloverTab() {
        return rolloverTabIndex;
    }
    public Dimension getMinimumSize(JComponent c) {
        return null;
    }
    public Dimension getMaximumSize(JComponent c) {
        return null;
    }
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        int baseline = calculateBaselineIfNecessary();
        if (baseline != -1) {
            int placement = tabPane.getTabPlacement();
            Insets insets = tabPane.getInsets();
            Insets tabAreaInsets = getTabAreaInsets(placement);
            switch(placement) {
            case JTabbedPane.TOP:
                baseline += insets.top + tabAreaInsets.top;
                return baseline;
            case JTabbedPane.BOTTOM:
                baseline = height - insets.bottom -
                    tabAreaInsets.bottom - maxTabHeight + baseline;
                return baseline;
            case JTabbedPane.LEFT:
            case JTabbedPane.RIGHT:
                baseline += insets.top + tabAreaInsets.top;
                return baseline;
            }
        }
        return -1;
    }
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            JComponent c) {
        super.getBaselineResizeBehavior(c);
        switch(tabPane.getTabPlacement()) {
        case JTabbedPane.LEFT:
        case JTabbedPane.RIGHT:
        case JTabbedPane.TOP:
            return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
        case JTabbedPane.BOTTOM:
            return Component.BaselineResizeBehavior.CONSTANT_DESCENT;
        }
        return Component.BaselineResizeBehavior.OTHER;
    }
    protected int getBaseline(int tab) {
        if (tabPane.getTabComponentAt(tab) != null) {
            int offset = getBaselineOffset();
            if (offset != 0) {
                return -1;
            }
            Component c = tabPane.getTabComponentAt(tab);
            Dimension pref = c.getPreferredSize();
            Insets tabInsets = getTabInsets(tabPane.getTabPlacement(), tab);
            int cellHeight = maxTabHeight - tabInsets.top - tabInsets.bottom;
            return c.getBaseline(pref.width, pref.height) +
                    (cellHeight - pref.height) / 2 + tabInsets.top;
        }
        else {
            View view = getTextViewForTab(tab);
            if (view != null) {
                int viewHeight = (int)view.getPreferredSpan(View.Y_AXIS);
                int baseline = BasicHTML.getHTMLBaseline(
                    view, (int)view.getPreferredSpan(View.X_AXIS), viewHeight);
                if (baseline >= 0) {
                    return maxTabHeight / 2 - viewHeight / 2 + baseline +
                        getBaselineOffset();
                }
                return -1;
            }
        }
        FontMetrics metrics = getFontMetrics();
        int fontHeight = metrics.getHeight();
        int fontBaseline = metrics.getAscent();
        return maxTabHeight / 2 - fontHeight / 2 + fontBaseline +
                getBaselineOffset();
    }
    protected int getBaselineOffset() {
        switch(tabPane.getTabPlacement()) {
        case JTabbedPane.TOP:
            if (tabPane.getTabCount() > 1) {
                return 1;
            }
            else {
                return -1;
            }
        case JTabbedPane.BOTTOM:
            if (tabPane.getTabCount() > 1) {
                return -1;
            }
            else {
                return 1;
            }
        default: 
            return (maxTabHeight % 2);
        }
    }
    private int calculateBaselineIfNecessary() {
        if (!calculatedBaseline) {
            calculatedBaseline = true;
            baseline = -1;
            if (tabPane.getTabCount() > 0) {
                calculateBaseline();
            }
        }
        return baseline;
    }
    private void calculateBaseline() {
        int tabCount = tabPane.getTabCount();
        int tabPlacement = tabPane.getTabPlacement();
        maxTabHeight = calculateMaxTabHeight(tabPlacement);
        baseline = getBaseline(0);
        if (isHorizontalTabPlacement()) {
            for(int i = 1; i < tabCount; i++) {
                if (getBaseline(i) != baseline) {
                    baseline = -1;
                    break;
                }
            }
        }
        else {
            FontMetrics fontMetrics = getFontMetrics();
            int fontHeight = fontMetrics.getHeight();
            int height = calculateTabHeight(tabPlacement, 0, fontHeight);
            for(int i = 1; i < tabCount; i++) {
                int newHeight = calculateTabHeight(tabPlacement, i,fontHeight);
                if (height != newHeight) {
                    baseline = -1;
                    break;
                }
            }
        }
    }
    public void paint(Graphics g, JComponent c) {
        int selectedIndex = tabPane.getSelectedIndex();
        int tabPlacement = tabPane.getTabPlacement();
        ensureCurrentLayout();
        if (tabsOverlapBorder) {
            paintContentBorder(g, tabPlacement, selectedIndex);
        }
        if (!scrollableTabLayoutEnabled()) { 
            paintTabArea(g, tabPlacement, selectedIndex);
        }
        if (!tabsOverlapBorder) {
            paintContentBorder(g, tabPlacement, selectedIndex);
        }
    }
    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
        int tabCount = tabPane.getTabCount();
        Rectangle iconRect = new Rectangle(),
                  textRect = new Rectangle();
        Rectangle clipRect = g.getClipBounds();
        for (int i = runCount - 1; i >= 0; i--) {
            int start = tabRuns[i];
            int next = tabRuns[(i == runCount - 1)? 0 : i + 1];
            int end = (next != 0? next - 1: tabCount - 1);
            for (int j = start; j <= end; j++) {
                if (j != selectedIndex && rects[j].intersects(clipRect)) {
                    paintTab(g, tabPlacement, rects, j, iconRect, textRect);
                }
            }
        }
        if (selectedIndex >= 0 && rects[selectedIndex].intersects(clipRect)) {
            paintTab(g, tabPlacement, rects, selectedIndex, iconRect, textRect);
        }
    }
    protected void paintTab(Graphics g, int tabPlacement,
                            Rectangle[] rects, int tabIndex,
                            Rectangle iconRect, Rectangle textRect) {
        Rectangle tabRect = rects[tabIndex];
        int selectedIndex = tabPane.getSelectedIndex();
        boolean isSelected = selectedIndex == tabIndex;
        if (tabsOpaque || tabPane.isOpaque()) {
            paintTabBackground(g, tabPlacement, tabIndex, tabRect.x, tabRect.y,
                    tabRect.width, tabRect.height, isSelected);
        }
        paintTabBorder(g, tabPlacement, tabIndex, tabRect.x, tabRect.y,
                       tabRect.width, tabRect.height, isSelected);
        String title = tabPane.getTitleAt(tabIndex);
        Font font = tabPane.getFont();
        FontMetrics metrics = SwingUtilities2.getFontMetrics(tabPane, g, font);
        Icon icon = getIconForTab(tabIndex);
        layoutLabel(tabPlacement, metrics, tabIndex, title, icon,
                    tabRect, iconRect, textRect, isSelected);
        if (tabPane.getTabComponentAt(tabIndex) == null) {
            String clippedTitle = title;
            if (scrollableTabLayoutEnabled() && tabScroller.croppedEdge.isParamsSet() &&
                    tabScroller.croppedEdge.getTabIndex() == tabIndex && isHorizontalTabPlacement()) {
                int availTextWidth = tabScroller.croppedEdge.getCropline() -
                        (textRect.x - tabRect.x) - tabScroller.croppedEdge.getCroppedSideWidth();
                clippedTitle = SwingUtilities2.clipStringIfNecessary(null, metrics, title, availTextWidth);
            }
            paintText(g, tabPlacement, font, metrics,
                    tabIndex, clippedTitle, textRect, isSelected);
            paintIcon(g, tabPlacement, tabIndex, icon, iconRect, isSelected);
        }
        paintFocusIndicator(g, tabPlacement, rects, tabIndex,
                  iconRect, textRect, isSelected);
    }
    private boolean isHorizontalTabPlacement() {
        return tabPane.getTabPlacement() == TOP || tabPane.getTabPlacement() == BOTTOM;
    }
    private static int xCropLen[] = {1,1,0,0,1,1,2,2};
    private static int yCropLen[] = {0,3,3,6,6,9,9,12};
    private static final int CROP_SEGMENT = 12;
    private static Polygon createCroppedTabShape(int tabPlacement, Rectangle tabRect, int cropline) {
        int rlen;
        int start;
        int end;
        int ostart;
        switch(tabPlacement) {
          case LEFT:
          case RIGHT:
              rlen = tabRect.width;
              start = tabRect.x;
              end = tabRect.x + tabRect.width;
              ostart = tabRect.y + tabRect.height;
              break;
          case TOP:
          case BOTTOM:
          default:
             rlen = tabRect.height;
             start = tabRect.y;
             end = tabRect.y + tabRect.height;
             ostart = tabRect.x + tabRect.width;
        }
        int rcnt = rlen/CROP_SEGMENT;
        if (rlen%CROP_SEGMENT > 0) {
            rcnt++;
        }
        int npts = 2 + (rcnt*8);
        int xp[] = new int[npts];
        int yp[] = new int[npts];
        int pcnt = 0;
        xp[pcnt] = ostart;
        yp[pcnt++] = end;
        xp[pcnt] = ostart;
        yp[pcnt++] = start;
        for(int i = 0; i < rcnt; i++) {
            for(int j = 0; j < xCropLen.length; j++) {
                xp[pcnt] = cropline - xCropLen[j];
                yp[pcnt] = start + (i*CROP_SEGMENT) + yCropLen[j];
                if (yp[pcnt] >= end) {
                    yp[pcnt] = end;
                    pcnt++;
                    break;
                }
                pcnt++;
            }
        }
        if (tabPlacement == JTabbedPane.TOP || tabPlacement == JTabbedPane.BOTTOM) {
           return new Polygon(xp, yp, pcnt);
        } else { 
           return new Polygon(yp, xp, pcnt);
        }
    }
    private void paintCroppedTabEdge(Graphics g) {
        int tabIndex = tabScroller.croppedEdge.getTabIndex();
        int cropline = tabScroller.croppedEdge.getCropline();
        int x,y;
        switch(tabPane.getTabPlacement()) {
          case LEFT:
          case RIGHT:
            x = rects[tabIndex].x;
            y = cropline;
            int xx = x;
            g.setColor(shadow);
            while(xx <= x+rects[tabIndex].width) {
                for (int i=0; i < xCropLen.length; i+=2) {
                    g.drawLine(xx+yCropLen[i],y-xCropLen[i],
                               xx+yCropLen[i+1]-1,y-xCropLen[i+1]);
                }
                xx+=CROP_SEGMENT;
            }
            break;
          case TOP:
          case BOTTOM:
          default:
            x = cropline;
            y = rects[tabIndex].y;
            int yy = y;
            g.setColor(shadow);
            while(yy <= y+rects[tabIndex].height) {
                for (int i=0; i < xCropLen.length; i+=2) {
                    g.drawLine(x-xCropLen[i],yy+yCropLen[i],
                               x-xCropLen[i+1],yy+yCropLen[i+1]-1);
                }
                yy+=CROP_SEGMENT;
            }
        }
    }
    protected void layoutLabel(int tabPlacement,
                               FontMetrics metrics, int tabIndex,
                               String title, Icon icon,
                               Rectangle tabRect, Rectangle iconRect,
                               Rectangle textRect, boolean isSelected ) {
        textRect.x = textRect.y = iconRect.x = iconRect.y = 0;
        View v = getTextViewForTab(tabIndex);
        if (v != null) {
            tabPane.putClientProperty("html", v);
        }
        SwingUtilities.layoutCompoundLabel(tabPane,
                                           metrics, title, icon,
                                           SwingUtilities.CENTER,
                                           SwingUtilities.CENTER,
                                           SwingUtilities.CENTER,
                                           SwingUtilities.TRAILING,
                                           tabRect,
                                           iconRect,
                                           textRect,
                                           textIconGap);
        tabPane.putClientProperty("html", null);
        int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
        int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
        iconRect.x += xNudge;
        iconRect.y += yNudge;
        textRect.x += xNudge;
        textRect.y += yNudge;
    }
    protected void paintIcon(Graphics g, int tabPlacement,
                             int tabIndex, Icon icon, Rectangle iconRect,
                             boolean isSelected ) {
        if (icon != null) {
            icon.paintIcon(tabPane, g, iconRect.x, iconRect.y);
        }
    }
    protected void paintText(Graphics g, int tabPlacement,
                             Font font, FontMetrics metrics, int tabIndex,
                             String title, Rectangle textRect,
                             boolean isSelected) {
        g.setFont(font);
        View v = getTextViewForTab(tabIndex);
        if (v != null) {
            v.paint(g, textRect);
        } else {
            int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);
            if (tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex)) {
                Color fg = tabPane.getForegroundAt(tabIndex);
                if (isSelected && (fg instanceof UIResource)) {
                    Color selectedFG = UIManager.getColor(
                                  "TabbedPane.selectedForeground");
                    if (selectedFG != null) {
                        fg = selectedFG;
                    }
                }
                g.setColor(fg);
                SwingUtilities2.drawStringUnderlineCharAt(tabPane, g,
                             title, mnemIndex,
                             textRect.x, textRect.y + metrics.getAscent());
            } else { 
                g.setColor(tabPane.getBackgroundAt(tabIndex).brighter());
                SwingUtilities2.drawStringUnderlineCharAt(tabPane, g,
                             title, mnemIndex,
                             textRect.x, textRect.y + metrics.getAscent());
                g.setColor(tabPane.getBackgroundAt(tabIndex).darker());
                SwingUtilities2.drawStringUnderlineCharAt(tabPane, g,
                             title, mnemIndex,
                             textRect.x - 1, textRect.y + metrics.getAscent() - 1);
            }
        }
    }
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        Rectangle tabRect = rects[tabIndex];
        String propKey = (isSelected ? "selectedLabelShift" : "labelShift");
        int nudge = DefaultLookup.getInt(
                tabPane, this, "TabbedPane." + propKey, 1);
        switch (tabPlacement) {
            case LEFT:
                return nudge;
            case RIGHT:
                return -nudge;
            case BOTTOM:
            case TOP:
            default:
                return tabRect.width % 2;
        }
    }
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        Rectangle tabRect = rects[tabIndex];
        String propKey = (isSelected ? "selectedLabelShift" : "labelShift");
        int nudge = DefaultLookup.getInt(
                tabPane, this, "TabbedPane." + propKey, 1);
        switch (tabPlacement) {
            case BOTTOM:
                return -nudge;
            case LEFT:
            case RIGHT:
                return tabRect.height % 2;
            case TOP:
            default:
                return nudge;
        }
    }
    protected void paintFocusIndicator(Graphics g, int tabPlacement,
                                       Rectangle[] rects, int tabIndex,
                                       Rectangle iconRect, Rectangle textRect,
                                       boolean isSelected) {
        Rectangle tabRect = rects[tabIndex];
        if (tabPane.hasFocus() && isSelected) {
            int x, y, w, h;
            g.setColor(focus);
            switch(tabPlacement) {
              case LEFT:
                  x = tabRect.x + 3;
                  y = tabRect.y + 3;
                  w = tabRect.width - 5;
                  h = tabRect.height - 6;
                  break;
              case RIGHT:
                  x = tabRect.x + 2;
                  y = tabRect.y + 3;
                  w = tabRect.width - 5;
                  h = tabRect.height - 6;
                  break;
              case BOTTOM:
                  x = tabRect.x + 3;
                  y = tabRect.y + 2;
                  w = tabRect.width - 6;
                  h = tabRect.height - 5;
                  break;
              case TOP:
              default:
                  x = tabRect.x + 3;
                  y = tabRect.y + 3;
                  w = tabRect.width - 6;
                  h = tabRect.height - 5;
            }
            BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
        }
    }
    protected void paintTabBorder(Graphics g, int tabPlacement,
                                  int tabIndex,
                                  int x, int y, int w, int h,
                                  boolean isSelected ) {
        g.setColor(lightHighlight);
        switch (tabPlacement) {
          case LEFT:
              g.drawLine(x+1, y+h-2, x+1, y+h-2); 
              g.drawLine(x, y+2, x, y+h-3); 
              g.drawLine(x+1, y+1, x+1, y+1); 
              g.drawLine(x+2, y, x+w-1, y); 
              g.setColor(shadow);
              g.drawLine(x+2, y+h-2, x+w-1, y+h-2); 
              g.setColor(darkShadow);
              g.drawLine(x+2, y+h-1, x+w-1, y+h-1); 
              break;
          case RIGHT:
              g.drawLine(x, y, x+w-3, y); 
              g.setColor(shadow);
              g.drawLine(x, y+h-2, x+w-3, y+h-2); 
              g.drawLine(x+w-2, y+2, x+w-2, y+h-3); 
              g.setColor(darkShadow);
              g.drawLine(x+w-2, y+1, x+w-2, y+1); 
              g.drawLine(x+w-2, y+h-2, x+w-2, y+h-2); 
              g.drawLine(x+w-1, y+2, x+w-1, y+h-3); 
              g.drawLine(x, y+h-1, x+w-3, y+h-1); 
              break;
          case BOTTOM:
              g.drawLine(x, y, x, y+h-3); 
              g.drawLine(x+1, y+h-2, x+1, y+h-2); 
              g.setColor(shadow);
              g.drawLine(x+2, y+h-2, x+w-3, y+h-2); 
              g.drawLine(x+w-2, y, x+w-2, y+h-3); 
              g.setColor(darkShadow);
              g.drawLine(x+2, y+h-1, x+w-3, y+h-1); 
              g.drawLine(x+w-2, y+h-2, x+w-2, y+h-2); 
              g.drawLine(x+w-1, y, x+w-1, y+h-3); 
              break;
          case TOP:
          default:
              g.drawLine(x, y+2, x, y+h-1); 
              g.drawLine(x+1, y+1, x+1, y+1); 
              g.drawLine(x+2, y, x+w-3, y); 
              g.setColor(shadow);
              g.drawLine(x+w-2, y+2, x+w-2, y+h-1); 
              g.setColor(darkShadow);
              g.drawLine(x+w-1, y+2, x+w-1, y+h-1); 
              g.drawLine(x+w-2, y+1, x+w-2, y+1); 
        }
    }
    protected void paintTabBackground(Graphics g, int tabPlacement,
                                      int tabIndex,
                                      int x, int y, int w, int h,
                                      boolean isSelected ) {
        g.setColor(!isSelected || selectedColor == null?
                   tabPane.getBackgroundAt(tabIndex) : selectedColor);
        switch(tabPlacement) {
          case LEFT:
              g.fillRect(x+1, y+1, w-1, h-3);
              break;
          case RIGHT:
              g.fillRect(x, y+1, w-2, h-3);
              break;
          case BOTTOM:
              g.fillRect(x+1, y, w-3, h-1);
              break;
          case TOP:
          default:
              g.fillRect(x+1, y+1, w-3, h-1);
        }
    }
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        int width = tabPane.getWidth();
        int height = tabPane.getHeight();
        Insets insets = tabPane.getInsets();
        Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
        int x = insets.left;
        int y = insets.top;
        int w = width - insets.right - insets.left;
        int h = height - insets.top - insets.bottom;
        switch(tabPlacement) {
          case LEFT:
              x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
              if (tabsOverlapBorder) {
                  x -= tabAreaInsets.right;
              }
              w -= (x - insets.left);
              break;
          case RIGHT:
              w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
              if (tabsOverlapBorder) {
                  w += tabAreaInsets.left;
              }
              break;
          case BOTTOM:
              h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
              if (tabsOverlapBorder) {
                  h += tabAreaInsets.top;
              }
              break;
          case TOP:
          default:
              y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
              if (tabsOverlapBorder) {
                  y -= tabAreaInsets.bottom;
              }
              h -= (y - insets.top);
        }
            if ( tabPane.getTabCount() > 0 && (contentOpaque || tabPane.isOpaque()) ) {
            Color color = UIManager.getColor("TabbedPane.contentAreaColor");
            if (color != null) {
                g.setColor(color);
            }
            else if ( selectedColor == null || selectedIndex == -1 ) {
                g.setColor(tabPane.getBackground());
            }
            else {
                g.setColor(selectedColor);
            }
            g.fillRect(x,y,w,h);
        }
        paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
        paintContentBorderLeftEdge(g, tabPlacement, selectedIndex, x, y, w, h);
        paintContentBorderBottomEdge(g, tabPlacement, selectedIndex, x, y, w, h);
        paintContentBorderRightEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    }
    protected void paintContentBorderTopEdge(Graphics g, int tabPlacement,
                                         int selectedIndex,
                                         int x, int y, int w, int h) {
        Rectangle selRect = selectedIndex < 0? null :
                               getTabBounds(selectedIndex, calcRect);
        g.setColor(lightHighlight);
        if (tabPlacement != TOP || selectedIndex < 0 ||
            (selRect.y + selRect.height + 1 < y) ||
            (selRect.x < x || selRect.x > x + w)) {
            g.drawLine(x, y, x+w-2, y);
        } else {
            g.drawLine(x, y, selRect.x - 1, y);
            if (selRect.x + selRect.width < x + w - 2) {
                g.drawLine(selRect.x + selRect.width, y,
                           x+w-2, y);
            } else {
                g.setColor(shadow);
                g.drawLine(x+w-2, y, x+w-2, y);
            }
        }
    }
    protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
                                               int selectedIndex,
                                               int x, int y, int w, int h) {
        Rectangle selRect = selectedIndex < 0? null :
                               getTabBounds(selectedIndex, calcRect);
        g.setColor(lightHighlight);
        if (tabPlacement != LEFT || selectedIndex < 0 ||
            (selRect.x + selRect.width + 1 < x) ||
            (selRect.y < y || selRect.y > y + h)) {
            g.drawLine(x, y, x, y+h-2);
        } else {
            g.drawLine(x, y, x, selRect.y - 1);
            if (selRect.y + selRect.height < y + h - 2) {
                g.drawLine(x, selRect.y + selRect.height,
                           x, y+h-2);
            }
        }
    }
    protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
                                               int selectedIndex,
                                               int x, int y, int w, int h) {
        Rectangle selRect = selectedIndex < 0? null :
                               getTabBounds(selectedIndex, calcRect);
        g.setColor(shadow);
        if (tabPlacement != BOTTOM || selectedIndex < 0 ||
             (selRect.y - 1 > h) ||
             (selRect.x < x || selRect.x > x + w)) {
            g.drawLine(x+1, y+h-2, x+w-2, y+h-2);
            g.setColor(darkShadow);
            g.drawLine(x, y+h-1, x+w-1, y+h-1);
        } else {
            g.drawLine(x+1, y+h-2, selRect.x - 1, y+h-2);
            g.setColor(darkShadow);
            g.drawLine(x, y+h-1, selRect.x - 1, y+h-1);
            if (selRect.x + selRect.width < x + w - 2) {
                g.setColor(shadow);
                g.drawLine(selRect.x + selRect.width, y+h-2, x+w-2, y+h-2);
                g.setColor(darkShadow);
                g.drawLine(selRect.x + selRect.width, y+h-1, x+w-1, y+h-1);
            }
        }
    }
    protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
                                               int selectedIndex,
                                               int x, int y, int w, int h) {
        Rectangle selRect = selectedIndex < 0? null :
                               getTabBounds(selectedIndex, calcRect);
        g.setColor(shadow);
        if (tabPlacement != RIGHT || selectedIndex < 0 ||
             (selRect.x - 1 > w) ||
             (selRect.y < y || selRect.y > y + h)) {
            g.drawLine(x+w-2, y+1, x+w-2, y+h-3);
            g.setColor(darkShadow);
            g.drawLine(x+w-1, y, x+w-1, y+h-1);
        } else {
            g.drawLine(x+w-2, y+1, x+w-2, selRect.y - 1);
            g.setColor(darkShadow);
            g.drawLine(x+w-1, y, x+w-1, selRect.y - 1);
            if (selRect.y + selRect.height < y + h - 2) {
                g.setColor(shadow);
                g.drawLine(x+w-2, selRect.y + selRect.height,
                           x+w-2, y+h-2);
                g.setColor(darkShadow);
                g.drawLine(x+w-1, selRect.y + selRect.height,
                           x+w-1, y+h-2);
            }
        }
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
    public Rectangle getTabBounds(JTabbedPane pane, int i) {
        ensureCurrentLayout();
        Rectangle tabRect = new Rectangle();
        return getTabBounds(i, tabRect);
    }
    public int getTabRunCount(JTabbedPane pane) {
        ensureCurrentLayout();
        return runCount;
    }
    public int tabForCoordinate(JTabbedPane pane, int x, int y) {
        return tabForCoordinate(pane, x, y, true);
    }
    private int tabForCoordinate(JTabbedPane pane, int x, int y,
                                 boolean validateIfNecessary) {
        if (validateIfNecessary) {
            ensureCurrentLayout();
        }
        if (isRunsDirty) {
            return -1;
        }
        Point p = new Point(x, y);
        if (scrollableTabLayoutEnabled()) {
            translatePointToTabPanel(x, y, p);
            Rectangle viewRect = tabScroller.viewport.getViewRect();
            if (!viewRect.contains(p)) {
                return -1;
            }
        }
        int tabCount = tabPane.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            if (rects[i].contains(p.x, p.y)) {
                return i;
            }
        }
        return -1;
    }
    protected Rectangle getTabBounds(int tabIndex, Rectangle dest) {
        dest.width = rects[tabIndex].width;
        dest.height = rects[tabIndex].height;
        if (scrollableTabLayoutEnabled()) { 
            Point vpp = tabScroller.viewport.getLocation();
            Point viewp = tabScroller.viewport.getViewPosition();
            dest.x = rects[tabIndex].x + vpp.x - viewp.x;
            dest.y = rects[tabIndex].y + vpp.y - viewp.y;
        } else { 
            dest.x = rects[tabIndex].x;
            dest.y = rects[tabIndex].y;
        }
        return dest;
    }
    private int getClosestTab(int x, int y) {
        int min = 0;
        int tabCount = Math.min(rects.length, tabPane.getTabCount());
        int max = tabCount;
        int tabPlacement = tabPane.getTabPlacement();
        boolean useX = (tabPlacement == TOP || tabPlacement == BOTTOM);
        int want = (useX) ? x : y;
        while (min != max) {
            int current = (max + min) / 2;
            int minLoc;
            int maxLoc;
            if (useX) {
                minLoc = rects[current].x;
                maxLoc = minLoc + rects[current].width;
            }
            else {
                minLoc = rects[current].y;
                maxLoc = minLoc + rects[current].height;
            }
            if (want < minLoc) {
                max = current;
                if (min == max) {
                    return Math.max(0, current - 1);
                }
            }
            else if (want >= maxLoc) {
                min = current;
                if (max - min <= 1) {
                    return Math.max(current + 1, tabCount - 1);
                }
            }
            else {
                return current;
            }
        }
        return min;
    }
    private Point translatePointToTabPanel(int srcx, int srcy, Point dest) {
        Point vpp = tabScroller.viewport.getLocation();
        Point viewp = tabScroller.viewport.getViewPosition();
        dest.x = srcx - vpp.x + viewp.x;
        dest.y = srcy - vpp.y + viewp.y;
        return dest;
    }
    protected Component getVisibleComponent() {
        return visibleComponent;
    }
    protected void setVisibleComponent(Component component) {
        if (visibleComponent != null
                && visibleComponent != component
                && visibleComponent.getParent() == tabPane
                && visibleComponent.isVisible()) {
            visibleComponent.setVisible(false);
        }
        if (component != null && !component.isVisible()) {
            component.setVisible(true);
        }
        visibleComponent = component;
    }
    protected void assureRectsCreated(int tabCount) {
        int rectArrayLen = rects.length;
        if (tabCount != rectArrayLen ) {
            Rectangle[] tempRectArray = new Rectangle[tabCount];
            System.arraycopy(rects, 0, tempRectArray, 0,
                             Math.min(rectArrayLen, tabCount));
            rects = tempRectArray;
            for (int rectIndex = rectArrayLen; rectIndex < tabCount; rectIndex++) {
                rects[rectIndex] = new Rectangle();
            }
        }
    }
    protected void expandTabRunsArray() {
        int rectLen = tabRuns.length;
        int[] newArray = new int[rectLen+10];
        System.arraycopy(tabRuns, 0, newArray, 0, runCount);
        tabRuns = newArray;
    }
    protected int getRunForTab(int tabCount, int tabIndex) {
        for (int i = 0; i < runCount; i++) {
            int first = tabRuns[i];
            int last = lastTabInRun(tabCount, i);
            if (tabIndex >= first && tabIndex <= last) {
                return i;
            }
        }
        return 0;
    }
    protected int lastTabInRun(int tabCount, int run) {
        if (runCount == 1) {
            return tabCount - 1;
        }
        int nextRun = (run == runCount - 1? 0 : run + 1);
        if (tabRuns[nextRun] == 0) {
            return tabCount - 1;
        }
        return tabRuns[nextRun]-1;
    }
    protected int getTabRunOverlay(int tabPlacement) {
        return tabRunOverlay;
    }
    protected int getTabRunIndent(int tabPlacement, int run) {
        return 0;
    }
    protected boolean shouldPadTabRun(int tabPlacement, int run) {
        return runCount > 1;
    }
    protected boolean shouldRotateTabRuns(int tabPlacement) {
        return true;
    }
    protected Icon getIconForTab(int tabIndex) {
        return (!tabPane.isEnabled() || !tabPane.isEnabledAt(tabIndex))?
                          tabPane.getDisabledIconAt(tabIndex) : tabPane.getIconAt(tabIndex);
    }
    protected View getTextViewForTab(int tabIndex) {
        if (htmlViews != null) {
            return htmlViews.elementAt(tabIndex);
        }
        return null;
    }
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        int height = 0;
        Component c = tabPane.getTabComponentAt(tabIndex);
        if (c != null) {
            height = c.getPreferredSize().height;
        } else {
            View v = getTextViewForTab(tabIndex);
            if (v != null) {
                height += (int) v.getPreferredSpan(View.Y_AXIS);
            } else {
                height += fontHeight;
            }
            Icon icon = getIconForTab(tabIndex);
            if (icon != null) {
                height = Math.max(height, icon.getIconHeight());
            }
        }
        Insets tabInsets = getTabInsets(tabPlacement, tabIndex);
        height += tabInsets.top + tabInsets.bottom + 2;
        return height;
    }
    protected int calculateMaxTabHeight(int tabPlacement) {
        FontMetrics metrics = getFontMetrics();
        int tabCount = tabPane.getTabCount();
        int result = 0;
        int fontHeight = metrics.getHeight();
        for(int i = 0; i < tabCount; i++) {
            result = Math.max(calculateTabHeight(tabPlacement, i, fontHeight), result);
        }
        return result;
    }
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        Insets tabInsets = getTabInsets(tabPlacement, tabIndex);
        int width = tabInsets.left + tabInsets.right + 3;
        Component tabComponent = tabPane.getTabComponentAt(tabIndex);
        if (tabComponent != null) {
            width += tabComponent.getPreferredSize().width;
        } else {
            Icon icon = getIconForTab(tabIndex);
            if (icon != null) {
                width += icon.getIconWidth() + textIconGap;
            }
            View v = getTextViewForTab(tabIndex);
            if (v != null) {
                width += (int) v.getPreferredSpan(View.X_AXIS);
            } else {
                String title = tabPane.getTitleAt(tabIndex);
                width += SwingUtilities2.stringWidth(tabPane, metrics, title);
            }
        }
        return width;
    }
    protected int calculateMaxTabWidth(int tabPlacement) {
        FontMetrics metrics = getFontMetrics();
        int tabCount = tabPane.getTabCount();
        int result = 0;
        for(int i = 0; i < tabCount; i++) {
            result = Math.max(calculateTabWidth(tabPlacement, i, metrics), result);
        }
        return result;
    }
    protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
        Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
        int tabRunOverlay = getTabRunOverlay(tabPlacement);
        return (horizRunCount > 0?
                horizRunCount * (maxTabHeight-tabRunOverlay) + tabRunOverlay +
                tabAreaInsets.top + tabAreaInsets.bottom :
                0);
    }
    protected int calculateTabAreaWidth(int tabPlacement, int vertRunCount, int maxTabWidth) {
        Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
        int tabRunOverlay = getTabRunOverlay(tabPlacement);
        return (vertRunCount > 0?
                vertRunCount * (maxTabWidth-tabRunOverlay) + tabRunOverlay +
                tabAreaInsets.left + tabAreaInsets.right :
                0);
    }
    protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        return tabInsets;
    }
    protected Insets getSelectedTabPadInsets(int tabPlacement) {
        rotateInsets(selectedTabPadInsets, currentPadInsets, tabPlacement);
        return currentPadInsets;
    }
    protected Insets getTabAreaInsets(int tabPlacement) {
        rotateInsets(tabAreaInsets, currentTabAreaInsets, tabPlacement);
        return currentTabAreaInsets;
    }
    protected Insets getContentBorderInsets(int tabPlacement) {
        return contentBorderInsets;
    }
    protected FontMetrics getFontMetrics() {
        Font font = tabPane.getFont();
        return tabPane.getFontMetrics(font);
    }
    protected void navigateSelectedTab(int direction) {
        int tabPlacement = tabPane.getTabPlacement();
        int current = DefaultLookup.getBoolean(tabPane, this,
                             "TabbedPane.selectionFollowsFocus", true) ?
                             tabPane.getSelectedIndex() : getFocusIndex();
        int tabCount = tabPane.getTabCount();
        boolean leftToRight = BasicGraphicsUtils.isLeftToRight(tabPane);
        if (tabCount <= 0) {
            return;
        }
        int offset;
        switch(tabPlacement) {
          case LEFT:
          case RIGHT:
              switch(direction) {
                 case NEXT:
                     selectNextTab(current);
                     break;
                 case PREVIOUS:
                     selectPreviousTab(current);
                     break;
                case NORTH:
                    selectPreviousTabInRun(current);
                    break;
                case SOUTH:
                    selectNextTabInRun(current);
                    break;
                case WEST:
                    offset = getTabRunOffset(tabPlacement, tabCount, current, false);
                    selectAdjacentRunTab(tabPlacement, current, offset);
                    break;
                case EAST:
                    offset = getTabRunOffset(tabPlacement, tabCount, current, true);
                    selectAdjacentRunTab(tabPlacement, current, offset);
                    break;
                default:
              }
              break;
          case BOTTOM:
          case TOP:
          default:
              switch(direction) {
                case NEXT:
                    selectNextTab(current);
                    break;
                case PREVIOUS:
                    selectPreviousTab(current);
                    break;
                case NORTH:
                    offset = getTabRunOffset(tabPlacement, tabCount, current, false);
                    selectAdjacentRunTab(tabPlacement, current, offset);
                    break;
                case SOUTH:
                    offset = getTabRunOffset(tabPlacement, tabCount, current, true);
                    selectAdjacentRunTab(tabPlacement, current, offset);
                    break;
                case EAST:
                    if (leftToRight) {
                        selectNextTabInRun(current);
                    } else {
                        selectPreviousTabInRun(current);
                    }
                    break;
                case WEST:
                    if (leftToRight) {
                        selectPreviousTabInRun(current);
                    } else {
                        selectNextTabInRun(current);
                    }
                    break;
                default:
              }
        }
    }
    protected void selectNextTabInRun(int current) {
        int tabCount = tabPane.getTabCount();
        int tabIndex = getNextTabIndexInRun(tabCount, current);
        while(tabIndex != current && !tabPane.isEnabledAt(tabIndex)) {
            tabIndex = getNextTabIndexInRun(tabCount, tabIndex);
        }
        navigateTo(tabIndex);
    }
    protected void selectPreviousTabInRun(int current) {
        int tabCount = tabPane.getTabCount();
        int tabIndex = getPreviousTabIndexInRun(tabCount, current);
        while(tabIndex != current && !tabPane.isEnabledAt(tabIndex)) {
            tabIndex = getPreviousTabIndexInRun(tabCount, tabIndex);
        }
        navigateTo(tabIndex);
    }
    protected void selectNextTab(int current) {
        int tabIndex = getNextTabIndex(current);
        while (tabIndex != current && !tabPane.isEnabledAt(tabIndex)) {
            tabIndex = getNextTabIndex(tabIndex);
        }
        navigateTo(tabIndex);
    }
    protected void selectPreviousTab(int current) {
        int tabIndex = getPreviousTabIndex(current);
        while (tabIndex != current && !tabPane.isEnabledAt(tabIndex)) {
            tabIndex = getPreviousTabIndex(tabIndex);
        }
        navigateTo(tabIndex);
    }
    protected void selectAdjacentRunTab(int tabPlacement,
                                        int tabIndex, int offset) {
        if ( runCount < 2 ) {
            return;
        }
        int newIndex;
        Rectangle r = rects[tabIndex];
        switch(tabPlacement) {
          case LEFT:
          case RIGHT:
              newIndex = tabForCoordinate(tabPane, r.x + r.width/2 + offset,
                                       r.y + r.height/2);
              break;
          case BOTTOM:
          case TOP:
          default:
              newIndex = tabForCoordinate(tabPane, r.x + r.width/2,
                                       r.y + r.height/2 + offset);
        }
        if (newIndex != -1) {
            while (!tabPane.isEnabledAt(newIndex) && newIndex != tabIndex) {
                newIndex = getNextTabIndex(newIndex);
            }
            navigateTo(newIndex);
        }
    }
    private void navigateTo(int index) {
        if (DefaultLookup.getBoolean(tabPane, this,
                             "TabbedPane.selectionFollowsFocus", true)) {
            tabPane.setSelectedIndex(index);
        } else {
            setFocusIndex(index, true);
        }
    }
    void setFocusIndex(int index, boolean repaint) {
        if (repaint && !isRunsDirty) {
            repaintTab(focusIndex);
            focusIndex = index;
            repaintTab(focusIndex);
        }
        else {
            focusIndex = index;
        }
    }
    private void repaintTab(int index) {
        if (!isRunsDirty && index >= 0 && index < tabPane.getTabCount()) {
            tabPane.repaint(getTabBounds(tabPane, index));
        }
    }
    private void validateFocusIndex() {
        if (focusIndex >= tabPane.getTabCount()) {
            setFocusIndex(tabPane.getSelectedIndex(), false);
        }
    }
    protected int getFocusIndex() {
        return focusIndex;
    }
    protected int getTabRunOffset(int tabPlacement, int tabCount,
                                  int tabIndex, boolean forward) {
        int run = getRunForTab(tabCount, tabIndex);
        int offset;
        switch(tabPlacement) {
          case LEFT: {
              if (run == 0) {
                  offset = (forward?
                            -(calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth)-maxTabWidth) :
                            -maxTabWidth);
              } else if (run == runCount - 1) {
                  offset = (forward?
                            maxTabWidth :
                            calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth)-maxTabWidth);
              } else {
                  offset = (forward? maxTabWidth : -maxTabWidth);
              }
              break;
          }
          case RIGHT: {
              if (run == 0) {
                  offset = (forward?
                            maxTabWidth :
                            calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth)-maxTabWidth);
              } else if (run == runCount - 1) {
                  offset = (forward?
                            -(calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth)-maxTabWidth) :
                            -maxTabWidth);
              } else {
                  offset = (forward? maxTabWidth : -maxTabWidth);
              }
              break;
          }
          case BOTTOM: {
              if (run == 0) {
                  offset = (forward?
                            maxTabHeight :
                            calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight)-maxTabHeight);
              } else if (run == runCount - 1) {
                  offset = (forward?
                            -(calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight)-maxTabHeight) :
                            -maxTabHeight);
              } else {
                  offset = (forward? maxTabHeight : -maxTabHeight);
              }
              break;
          }
          case TOP:
          default: {
              if (run == 0) {
                  offset = (forward?
                            -(calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight)-maxTabHeight) :
                            -maxTabHeight);
              } else if (run == runCount - 1) {
                  offset = (forward?
                            maxTabHeight :
                            calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight)-maxTabHeight);
              } else {
                  offset = (forward? maxTabHeight : -maxTabHeight);
              }
          }
        }
        return offset;
    }
    protected int getPreviousTabIndex(int base) {
        int tabIndex = (base - 1 >= 0? base - 1 : tabPane.getTabCount() - 1);
        return (tabIndex >= 0? tabIndex : 0);
    }
    protected int getNextTabIndex(int base) {
        return (base+1)%tabPane.getTabCount();
    }
    protected int getNextTabIndexInRun(int tabCount, int base) {
        if (runCount < 2) {
            return getNextTabIndex(base);
        }
        int currentRun = getRunForTab(tabCount, base);
        int next = getNextTabIndex(base);
        if (next == tabRuns[getNextTabRun(currentRun)]) {
            return tabRuns[currentRun];
        }
        return next;
    }
    protected int getPreviousTabIndexInRun(int tabCount, int base) {
        if (runCount < 2) {
            return getPreviousTabIndex(base);
        }
        int currentRun = getRunForTab(tabCount, base);
        if (base == tabRuns[currentRun]) {
            int previous = tabRuns[getNextTabRun(currentRun)]-1;
            return (previous != -1? previous : tabCount-1);
        }
        return getPreviousTabIndex(base);
    }
    protected int getPreviousTabRun(int baseRun) {
        int runIndex = (baseRun - 1 >= 0? baseRun - 1 : runCount - 1);
        return (runIndex >= 0? runIndex : 0);
    }
    protected int getNextTabRun(int baseRun) {
        return (baseRun+1)%runCount;
    }
    protected static void rotateInsets(Insets topInsets, Insets targetInsets, int targetPlacement) {
        switch(targetPlacement) {
          case LEFT:
              targetInsets.top = topInsets.left;
              targetInsets.left = topInsets.top;
              targetInsets.bottom = topInsets.right;
              targetInsets.right = topInsets.bottom;
              break;
          case BOTTOM:
              targetInsets.top = topInsets.bottom;
              targetInsets.left = topInsets.left;
              targetInsets.bottom = topInsets.top;
              targetInsets.right = topInsets.right;
              break;
          case RIGHT:
              targetInsets.top = topInsets.left;
              targetInsets.left = topInsets.bottom;
              targetInsets.bottom = topInsets.right;
              targetInsets.right = topInsets.top;
              break;
          case TOP:
          default:
              targetInsets.top = topInsets.top;
              targetInsets.left = topInsets.left;
              targetInsets.bottom = topInsets.bottom;
              targetInsets.right = topInsets.right;
        }
    }
    boolean requestFocusForVisibleComponent() {
        return SwingUtilities2.tabbedPaneChangeFocusTo(getVisibleComponent());
    }
    private static class Actions extends UIAction {
        final static String NEXT = "navigateNext";
        final static String PREVIOUS = "navigatePrevious";
        final static String RIGHT = "navigateRight";
        final static String LEFT = "navigateLeft";
        final static String UP = "navigateUp";
        final static String DOWN = "navigateDown";
        final static String PAGE_UP = "navigatePageUp";
        final static String PAGE_DOWN = "navigatePageDown";
        final static String REQUEST_FOCUS = "requestFocus";
        final static String REQUEST_FOCUS_FOR_VISIBLE =
                                    "requestFocusForVisibleComponent";
        final static String SET_SELECTED = "setSelectedIndex";
        final static String SELECT_FOCUSED = "selectTabWithFocus";
        final static String SCROLL_FORWARD = "scrollTabsForwardAction";
        final static String SCROLL_BACKWARD = "scrollTabsBackwardAction";
        Actions(String key) {
            super(key);
        }
        public void actionPerformed(ActionEvent e) {
            String key = getName();
            JTabbedPane pane = (JTabbedPane)e.getSource();
            BasicTabbedPaneUI ui = (BasicTabbedPaneUI)BasicLookAndFeel.
                       getUIOfType(pane.getUI(), BasicTabbedPaneUI.class);
            if (ui == null) {
                return;
            }
            if (key == NEXT) {
                ui.navigateSelectedTab(SwingConstants.NEXT);
            }
            else if (key == PREVIOUS) {
                ui.navigateSelectedTab(SwingConstants.PREVIOUS);
            }
            else if (key == RIGHT) {
                ui.navigateSelectedTab(SwingConstants.EAST);
            }
            else if (key == LEFT) {
                ui.navigateSelectedTab(SwingConstants.WEST);
            }
            else if (key == UP) {
                ui.navigateSelectedTab(SwingConstants.NORTH);
            }
            else if (key == DOWN) {
                ui.navigateSelectedTab(SwingConstants.SOUTH);
            }
            else if (key == PAGE_UP) {
                int tabPlacement = pane.getTabPlacement();
                if (tabPlacement == TOP|| tabPlacement == BOTTOM) {
                    ui.navigateSelectedTab(SwingConstants.WEST);
                } else {
                    ui.navigateSelectedTab(SwingConstants.NORTH);
                }
            }
            else if (key == PAGE_DOWN) {
                int tabPlacement = pane.getTabPlacement();
                if (tabPlacement == TOP || tabPlacement == BOTTOM) {
                    ui.navigateSelectedTab(SwingConstants.EAST);
                } else {
                    ui.navigateSelectedTab(SwingConstants.SOUTH);
                }
            }
            else if (key == REQUEST_FOCUS) {
                pane.requestFocus();
            }
            else if (key == REQUEST_FOCUS_FOR_VISIBLE) {
                ui.requestFocusForVisibleComponent();
            }
            else if (key == SET_SELECTED) {
                String command = e.getActionCommand();
                if (command != null && command.length() > 0) {
                    int mnemonic = (int)e.getActionCommand().charAt(0);
                    if (mnemonic >= 'a' && mnemonic <='z') {
                        mnemonic  -= ('a' - 'A');
                    }
                    Integer index = ui.mnemonicToIndexMap.get(Integer.valueOf(mnemonic));
                    if (index != null && pane.isEnabledAt(index.intValue())) {
                        pane.setSelectedIndex(index.intValue());
                    }
                }
            }
            else if (key == SELECT_FOCUSED) {
                int focusIndex = ui.getFocusIndex();
                if (focusIndex != -1) {
                    pane.setSelectedIndex(focusIndex);
                }
            }
            else if (key == SCROLL_FORWARD) {
                if (ui.scrollableTabLayoutEnabled()) {
                    ui.tabScroller.scrollForward(pane.getTabPlacement());
                }
            }
            else if (key == SCROLL_BACKWARD) {
                if (ui.scrollableTabLayoutEnabled()) {
                    ui.tabScroller.scrollBackward(pane.getTabPlacement());
                }
            }
        }
    }
    public class TabbedPaneLayout implements LayoutManager {
        public void addLayoutComponent(String name, Component comp) {}
        public void removeLayoutComponent(Component comp) {}
        public Dimension preferredLayoutSize(Container parent) {
            return calculateSize(false);
        }
        public Dimension minimumLayoutSize(Container parent) {
            return calculateSize(true);
        }
        protected Dimension calculateSize(boolean minimum) {
            int tabPlacement = tabPane.getTabPlacement();
            Insets insets = tabPane.getInsets();
            Insets contentInsets = getContentBorderInsets(tabPlacement);
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
            Dimension zeroSize = new Dimension(0,0);
            int height = 0;
            int width = 0;
            int cWidth = 0;
            int cHeight = 0;
            for (int i = 0; i < tabPane.getTabCount(); i++) {
                Component component = tabPane.getComponentAt(i);
                if (component != null) {
                    Dimension size = minimum ? component.getMinimumSize() :
                                component.getPreferredSize();
                    if (size != null) {
                        cHeight = Math.max(size.height, cHeight);
                        cWidth = Math.max(size.width, cWidth);
                    }
                }
            }
            width += cWidth;
            height += cHeight;
            int tabExtent;
            switch(tabPlacement) {
              case LEFT:
              case RIGHT:
                  height = Math.max(height, calculateMaxTabHeight(tabPlacement));
                  tabExtent = preferredTabAreaWidth(tabPlacement, height - tabAreaInsets.top - tabAreaInsets.bottom);
                  width += tabExtent;
                  break;
              case TOP:
              case BOTTOM:
              default:
                  width = Math.max(width, calculateMaxTabWidth(tabPlacement));
                  tabExtent = preferredTabAreaHeight(tabPlacement, width - tabAreaInsets.left - tabAreaInsets.right);
                  height += tabExtent;
            }
            return new Dimension(width + insets.left + insets.right + contentInsets.left + contentInsets.right,
                             height + insets.bottom + insets.top + contentInsets.top + contentInsets.bottom);
        }
        protected int preferredTabAreaHeight(int tabPlacement, int width) {
            FontMetrics metrics = getFontMetrics();
            int tabCount = tabPane.getTabCount();
            int total = 0;
            if (tabCount > 0) {
                int rows = 1;
                int x = 0;
                int maxTabHeight = calculateMaxTabHeight(tabPlacement);
                for (int i = 0; i < tabCount; i++) {
                    int tabWidth = calculateTabWidth(tabPlacement, i, metrics);
                    if (x != 0 && x + tabWidth > width) {
                        rows++;
                        x = 0;
                    }
                    x += tabWidth;
                }
                total = calculateTabAreaHeight(tabPlacement, rows, maxTabHeight);
            }
            return total;
        }
        protected int preferredTabAreaWidth(int tabPlacement, int height) {
            FontMetrics metrics = getFontMetrics();
            int tabCount = tabPane.getTabCount();
            int total = 0;
            if (tabCount > 0) {
                int columns = 1;
                int y = 0;
                int fontHeight = metrics.getHeight();
                maxTabWidth = calculateMaxTabWidth(tabPlacement);
                for (int i = 0; i < tabCount; i++) {
                    int tabHeight = calculateTabHeight(tabPlacement, i, fontHeight);
                    if (y != 0 && y + tabHeight > height) {
                        columns++;
                        y = 0;
                    }
                    y += tabHeight;
                }
                total = calculateTabAreaWidth(tabPlacement, columns, maxTabWidth);
            }
            return total;
        }
        public void layoutContainer(Container parent) {
            setRolloverTab(-1);
            int tabPlacement = tabPane.getTabPlacement();
            Insets insets = tabPane.getInsets();
            int selectedIndex = tabPane.getSelectedIndex();
            Component visibleComponent = getVisibleComponent();
            calculateLayoutInfo();
            Component selectedComponent = null;
            if (selectedIndex < 0) {
                if (visibleComponent != null) {
                    setVisibleComponent(null);
                }
            } else {
                selectedComponent = tabPane.getComponentAt(selectedIndex);
            }
            int cx, cy, cw, ch;
            int totalTabWidth = 0;
            int totalTabHeight = 0;
            Insets contentInsets = getContentBorderInsets(tabPlacement);
            boolean shouldChangeFocus = false;
            if(selectedComponent != null) {
                if(selectedComponent != visibleComponent &&
                        visibleComponent != null) {
                    if(SwingUtilities.findFocusOwner(visibleComponent) != null) {
                        shouldChangeFocus = true;
                    }
                }
                setVisibleComponent(selectedComponent);
            }
            Rectangle bounds = tabPane.getBounds();
            int numChildren = tabPane.getComponentCount();
            if(numChildren > 0) {
                switch(tabPlacement) {
                    case LEFT:
                        totalTabWidth = calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                        cx = insets.left + totalTabWidth + contentInsets.left;
                        cy = insets.top + contentInsets.top;
                        break;
                    case RIGHT:
                        totalTabWidth = calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                        cx = insets.left + contentInsets.left;
                        cy = insets.top + contentInsets.top;
                        break;
                    case BOTTOM:
                        totalTabHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                        cx = insets.left + contentInsets.left;
                        cy = insets.top + contentInsets.top;
                        break;
                    case TOP:
                    default:
                        totalTabHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                        cx = insets.left + contentInsets.left;
                        cy = insets.top + totalTabHeight + contentInsets.top;
                }
                cw = bounds.width - totalTabWidth -
                        insets.left - insets.right -
                        contentInsets.left - contentInsets.right;
                ch = bounds.height - totalTabHeight -
                        insets.top - insets.bottom -
                        contentInsets.top - contentInsets.bottom;
                for(int i = 0; i < numChildren; i++) {
                    Component child = tabPane.getComponent(i);
                    if(child == tabContainer) {
                        int tabContainerWidth = totalTabWidth == 0 ? bounds.width :
                                totalTabWidth + insets.left + insets.right +
                                        contentInsets.left + contentInsets.right;
                        int tabContainerHeight = totalTabHeight == 0 ? bounds.height :
                                totalTabHeight + insets.top + insets.bottom +
                                        contentInsets.top + contentInsets.bottom;
                        int tabContainerX = 0;
                        int tabContainerY = 0;
                        if(tabPlacement == BOTTOM) {
                            tabContainerY = bounds.height - tabContainerHeight;
                        } else if(tabPlacement == RIGHT) {
                            tabContainerX = bounds.width - tabContainerWidth;
                        }
                        child.setBounds(tabContainerX, tabContainerY, tabContainerWidth, tabContainerHeight);
                    } else {
                        child.setBounds(cx, cy, cw, ch);
                    }
                }
            }
            layoutTabComponents();
            if(shouldChangeFocus) {
                if(!requestFocusForVisibleComponent()) {
                    tabPane.requestFocus();
                }
            }
        }
        public void calculateLayoutInfo() {
            int tabCount = tabPane.getTabCount();
            assureRectsCreated(tabCount);
            calculateTabRects(tabPane.getTabPlacement(), tabCount);
            isRunsDirty = false;
        }
        private void layoutTabComponents() {
            if (tabContainer == null) {
                return;
            }
            Rectangle rect = new Rectangle();
            Point delta = new Point(-tabContainer.getX(), -tabContainer.getY());
            if (scrollableTabLayoutEnabled()) {
                translatePointToTabPanel(0, 0, delta);
            }
            for (int i = 0; i < tabPane.getTabCount(); i++) {
                Component c = tabPane.getTabComponentAt(i);
                if (c == null) {
                    continue;
                }
                getTabBounds(i, rect);
                Dimension preferredSize = c.getPreferredSize();
                Insets insets = getTabInsets(tabPane.getTabPlacement(), i);
                int outerX = rect.x + insets.left + delta.x;
                int outerY = rect.y + insets.top + delta.y;
                int outerWidth = rect.width - insets.left - insets.right;
                int outerHeight = rect.height - insets.top - insets.bottom;
                int x = outerX + (outerWidth - preferredSize.width) / 2;
                int y = outerY + (outerHeight - preferredSize.height) / 2;
                int tabPlacement = tabPane.getTabPlacement();
                boolean isSeleceted = i == tabPane.getSelectedIndex();
                c.setBounds(x + getTabLabelShiftX(tabPlacement, i, isSeleceted),
                            y + getTabLabelShiftY(tabPlacement, i, isSeleceted),
                        preferredSize.width, preferredSize.height);
            }
        }
        protected void calculateTabRects(int tabPlacement, int tabCount) {
            FontMetrics metrics = getFontMetrics();
            Dimension size = tabPane.getSize();
            Insets insets = tabPane.getInsets();
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
            int fontHeight = metrics.getHeight();
            int selectedIndex = tabPane.getSelectedIndex();
            int tabRunOverlay;
            int i, j;
            int x, y;
            int returnAt;
            boolean verticalTabRuns = (tabPlacement == LEFT || tabPlacement == RIGHT);
            boolean leftToRight = BasicGraphicsUtils.isLeftToRight(tabPane);
            switch(tabPlacement) {
              case LEFT:
                  maxTabWidth = calculateMaxTabWidth(tabPlacement);
                  x = insets.left + tabAreaInsets.left;
                  y = insets.top + tabAreaInsets.top;
                  returnAt = size.height - (insets.bottom + tabAreaInsets.bottom);
                  break;
              case RIGHT:
                  maxTabWidth = calculateMaxTabWidth(tabPlacement);
                  x = size.width - insets.right - tabAreaInsets.right - maxTabWidth;
                  y = insets.top + tabAreaInsets.top;
                  returnAt = size.height - (insets.bottom + tabAreaInsets.bottom);
                  break;
              case BOTTOM:
                  maxTabHeight = calculateMaxTabHeight(tabPlacement);
                  x = insets.left + tabAreaInsets.left;
                  y = size.height - insets.bottom - tabAreaInsets.bottom - maxTabHeight;
                  returnAt = size.width - (insets.right + tabAreaInsets.right);
                  break;
              case TOP:
              default:
                  maxTabHeight = calculateMaxTabHeight(tabPlacement);
                  x = insets.left + tabAreaInsets.left;
                  y = insets.top + tabAreaInsets.top;
                  returnAt = size.width - (insets.right + tabAreaInsets.right);
                  break;
            }
            tabRunOverlay = getTabRunOverlay(tabPlacement);
            runCount = 0;
            selectedRun = -1;
            if (tabCount == 0) {
                return;
            }
            Rectangle rect;
            for (i = 0; i < tabCount; i++) {
                rect = rects[i];
                if (!verticalTabRuns) {
                    if (i > 0) {
                        rect.x = rects[i-1].x + rects[i-1].width;
                    } else {
                        tabRuns[0] = 0;
                        runCount = 1;
                        maxTabWidth = 0;
                        rect.x = x;
                    }
                    rect.width = calculateTabWidth(tabPlacement, i, metrics);
                    maxTabWidth = Math.max(maxTabWidth, rect.width);
                    if (rect.x != 2 + insets.left && rect.x + rect.width > returnAt) {
                        if (runCount > tabRuns.length - 1) {
                            expandTabRunsArray();
                        }
                        tabRuns[runCount] = i;
                        runCount++;
                        rect.x = x;
                    }
                    rect.y = y;
                    rect.height = maxTabHeight;
                } else {
                    if (i > 0) {
                        rect.y = rects[i-1].y + rects[i-1].height;
                    } else {
                        tabRuns[0] = 0;
                        runCount = 1;
                        maxTabHeight = 0;
                        rect.y = y;
                    }
                    rect.height = calculateTabHeight(tabPlacement, i, fontHeight);
                    maxTabHeight = Math.max(maxTabHeight, rect.height);
                    if (rect.y != 2 + insets.top && rect.y + rect.height > returnAt) {
                        if (runCount > tabRuns.length - 1) {
                            expandTabRunsArray();
                        }
                        tabRuns[runCount] = i;
                        runCount++;
                        rect.y = y;
                    }
                    rect.x = x;
                    rect.width = maxTabWidth;
                }
                if (i == selectedIndex) {
                    selectedRun = runCount - 1;
                }
            }
            if (runCount > 1) {
                normalizeTabRuns(tabPlacement, tabCount, verticalTabRuns? y : x, returnAt);
                selectedRun = getRunForTab(tabCount, selectedIndex);
                if (shouldRotateTabRuns(tabPlacement)) {
                    rotateTabRuns(tabPlacement, selectedRun);
                }
            }
            for (i = runCount - 1; i >= 0; i--) {
                int start = tabRuns[i];
                int next = tabRuns[i == (runCount - 1)? 0 : i + 1];
                int end = (next != 0? next - 1 : tabCount - 1);
                if (!verticalTabRuns) {
                    for (j = start; j <= end; j++) {
                        rect = rects[j];
                        rect.y = y;
                        rect.x += getTabRunIndent(tabPlacement, i);
                    }
                    if (shouldPadTabRun(tabPlacement, i)) {
                        padTabRun(tabPlacement, start, end, returnAt);
                    }
                    if (tabPlacement == BOTTOM) {
                        y -= (maxTabHeight - tabRunOverlay);
                    } else {
                        y += (maxTabHeight - tabRunOverlay);
                    }
                } else {
                    for (j = start; j <= end; j++) {
                        rect = rects[j];
                        rect.x = x;
                        rect.y += getTabRunIndent(tabPlacement, i);
                    }
                    if (shouldPadTabRun(tabPlacement, i)) {
                        padTabRun(tabPlacement, start, end, returnAt);
                    }
                    if (tabPlacement == RIGHT) {
                        x -= (maxTabWidth - tabRunOverlay);
                    } else {
                        x += (maxTabWidth - tabRunOverlay);
                    }
                }
            }
            padSelectedTab(tabPlacement, selectedIndex);
            if (!leftToRight && !verticalTabRuns) {
                int rightMargin = size.width
                                  - (insets.right + tabAreaInsets.right);
                for (i = 0; i < tabCount; i++) {
                    rects[i].x = rightMargin - rects[i].x - rects[i].width;
                }
            }
        }
        protected void rotateTabRuns(int tabPlacement, int selectedRun) {
            for (int i = 0; i < selectedRun; i++) {
                int save = tabRuns[0];
                for (int j = 1; j < runCount; j++) {
                    tabRuns[j - 1] = tabRuns[j];
                }
                tabRuns[runCount-1] = save;
            }
        }
        protected void normalizeTabRuns(int tabPlacement, int tabCount,
                                     int start, int max) {
            boolean verticalTabRuns = (tabPlacement == LEFT || tabPlacement == RIGHT);
            int run = runCount - 1;
            boolean keepAdjusting = true;
            double weight = 1.25;
            while (keepAdjusting) {
                int last = lastTabInRun(tabCount, run);
                int prevLast = lastTabInRun(tabCount, run-1);
                int end;
                int prevLastLen;
                if (!verticalTabRuns) {
                    end = rects[last].x + rects[last].width;
                    prevLastLen = (int)(maxTabWidth*weight);
                } else {
                    end = rects[last].y + rects[last].height;
                    prevLastLen = (int)(maxTabHeight*weight*2);
                }
                if (max - end > prevLastLen) {
                    tabRuns[run] = prevLast;
                    if (!verticalTabRuns) {
                        rects[prevLast].x = start;
                    } else {
                        rects[prevLast].y = start;
                    }
                    for (int i = prevLast+1; i <= last; i++) {
                        if (!verticalTabRuns) {
                            rects[i].x = rects[i-1].x + rects[i-1].width;
                        } else {
                            rects[i].y = rects[i-1].y + rects[i-1].height;
                        }
                    }
                } else if (run == runCount - 1) {
                    keepAdjusting = false;
                }
                if (run - 1 > 0) {
                    run -= 1;
                } else {
                    run = runCount - 1;
                    weight += .25;
                }
            }
        }
        protected void padTabRun(int tabPlacement, int start, int end, int max) {
            Rectangle lastRect = rects[end];
            if (tabPlacement == TOP || tabPlacement == BOTTOM) {
                int runWidth = (lastRect.x + lastRect.width) - rects[start].x;
                int deltaWidth = max - (lastRect.x + lastRect.width);
                float factor = (float)deltaWidth / (float)runWidth;
                for (int j = start; j <= end; j++) {
                    Rectangle pastRect = rects[j];
                    if (j > start) {
                        pastRect.x = rects[j-1].x + rects[j-1].width;
                    }
                    pastRect.width += Math.round((float)pastRect.width * factor);
                }
                lastRect.width = max - lastRect.x;
            } else {
                int runHeight = (lastRect.y + lastRect.height) - rects[start].y;
                int deltaHeight = max - (lastRect.y + lastRect.height);
                float factor = (float)deltaHeight / (float)runHeight;
                for (int j = start; j <= end; j++) {
                    Rectangle pastRect = rects[j];
                    if (j > start) {
                        pastRect.y = rects[j-1].y + rects[j-1].height;
                    }
                    pastRect.height += Math.round((float)pastRect.height * factor);
                }
                lastRect.height = max - lastRect.y;
            }
        }
        protected void padSelectedTab(int tabPlacement, int selectedIndex) {
            if (selectedIndex >= 0) {
                Rectangle selRect = rects[selectedIndex];
                Insets padInsets = getSelectedTabPadInsets(tabPlacement);
                selRect.x -= padInsets.left;
                selRect.width += (padInsets.left + padInsets.right);
                selRect.y -= padInsets.top;
                selRect.height += (padInsets.top + padInsets.bottom);
                if (!scrollableTabLayoutEnabled()) { 
                    Dimension size = tabPane.getSize();
                    Insets insets = tabPane.getInsets();
                    if ((tabPlacement == LEFT) || (tabPlacement == RIGHT)) {
                        int top = insets.top - selRect.y;
                        if (top > 0) {
                            selRect.y += top;
                            selRect.height -= top;
                        }
                        int bottom = (selRect.y + selRect.height) + insets.bottom - size.height;
                        if (bottom > 0) {
                            selRect.height -= bottom;
                        }
                    } else {
                        int left = insets.left - selRect.x;
                        if (left > 0) {
                            selRect.x += left;
                            selRect.width -= left;
                        }
                        int right = (selRect.x + selRect.width) + insets.right - size.width;
                        if (right > 0) {
                            selRect.width -= right;
                        }
                    }
                }
            }
        }
    }
    private class TabbedPaneScrollLayout extends TabbedPaneLayout {
        protected int preferredTabAreaHeight(int tabPlacement, int width) {
            return calculateMaxTabHeight(tabPlacement);
        }
        protected int preferredTabAreaWidth(int tabPlacement, int height) {
            return calculateMaxTabWidth(tabPlacement);
        }
        public void layoutContainer(Container parent) {
            setRolloverTab(-1);
            int tabPlacement = tabPane.getTabPlacement();
            int tabCount = tabPane.getTabCount();
            Insets insets = tabPane.getInsets();
            int selectedIndex = tabPane.getSelectedIndex();
            Component visibleComponent = getVisibleComponent();
            calculateLayoutInfo();
            Component selectedComponent = null;
            if (selectedIndex < 0) {
                if (visibleComponent != null) {
                    setVisibleComponent(null);
                }
            } else {
                selectedComponent = tabPane.getComponentAt(selectedIndex);
            }
            if (tabPane.getTabCount() == 0) {
                tabScroller.croppedEdge.resetParams();
                tabScroller.scrollForwardButton.setVisible(false);
                tabScroller.scrollBackwardButton.setVisible(false);
                return;
            }
            boolean shouldChangeFocus = false;
            if(selectedComponent != null) {
                if(selectedComponent != visibleComponent &&
                        visibleComponent != null) {
                    if(SwingUtilities.findFocusOwner(visibleComponent) != null) {
                        shouldChangeFocus = true;
                    }
                }
                setVisibleComponent(selectedComponent);
            }
            int tx, ty, tw, th; 
            int cx, cy, cw, ch; 
            Insets contentInsets = getContentBorderInsets(tabPlacement);
            Rectangle bounds = tabPane.getBounds();
            int numChildren = tabPane.getComponentCount();
            if(numChildren > 0) {
                switch(tabPlacement) {
                    case LEFT:
                        tw = calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                        th = bounds.height - insets.top - insets.bottom;
                        tx = insets.left;
                        ty = insets.top;
                        cx = tx + tw + contentInsets.left;
                        cy = ty + contentInsets.top;
                        cw = bounds.width - insets.left - insets.right - tw -
                                contentInsets.left - contentInsets.right;
                        ch = bounds.height - insets.top - insets.bottom -
                                contentInsets.top - contentInsets.bottom;
                        break;
                    case RIGHT:
                        tw = calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                        th = bounds.height - insets.top - insets.bottom;
                        tx = bounds.width - insets.right - tw;
                        ty = insets.top;
                        cx = insets.left + contentInsets.left;
                        cy = insets.top + contentInsets.top;
                        cw = bounds.width - insets.left - insets.right - tw -
                                contentInsets.left - contentInsets.right;
                        ch = bounds.height - insets.top - insets.bottom -
                                contentInsets.top - contentInsets.bottom;
                        break;
                    case BOTTOM:
                        tw = bounds.width - insets.left - insets.right;
                        th = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                        tx = insets.left;
                        ty = bounds.height - insets.bottom - th;
                        cx = insets.left + contentInsets.left;
                        cy = insets.top + contentInsets.top;
                        cw = bounds.width - insets.left - insets.right -
                                contentInsets.left - contentInsets.right;
                        ch = bounds.height - insets.top - insets.bottom - th -
                                contentInsets.top - contentInsets.bottom;
                        break;
                    case TOP:
                    default:
                        tw = bounds.width - insets.left - insets.right;
                        th = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                        tx = insets.left;
                        ty = insets.top;
                        cx = tx + contentInsets.left;
                        cy = ty + th + contentInsets.top;
                        cw = bounds.width - insets.left - insets.right -
                                contentInsets.left - contentInsets.right;
                        ch = bounds.height - insets.top - insets.bottom - th -
                                contentInsets.top - contentInsets.bottom;
                }
                for(int i = 0; i < numChildren; i++) {
                    Component child = tabPane.getComponent(i);
                    if(tabScroller != null && child == tabScroller.viewport) {
                        JViewport viewport = (JViewport) child;
                        Rectangle viewRect = viewport.getViewRect();
                        int vw = tw;
                        int vh = th;
                        Dimension butSize = tabScroller.scrollForwardButton.getPreferredSize();
                        switch(tabPlacement) {
                            case LEFT:
                            case RIGHT:
                                int totalTabHeight = rects[tabCount - 1].y + rects[tabCount - 1].height;
                                if(totalTabHeight > th) {
                                    vh = (th > 2 * butSize.height) ? th - 2 * butSize.height : 0;
                                    if(totalTabHeight - viewRect.y <= vh) {
                                        vh = totalTabHeight - viewRect.y;
                                    }
                                }
                                break;
                            case BOTTOM:
                            case TOP:
                            default:
                                int totalTabWidth = rects[tabCount - 1].x + rects[tabCount - 1].width;
                                if(totalTabWidth > tw) {
                                    vw = (tw > 2 * butSize.width) ? tw - 2 * butSize.width : 0;
                                    if(totalTabWidth - viewRect.x <= vw) {
                                        vw = totalTabWidth - viewRect.x;
                                    }
                                }
                        }
                        child.setBounds(tx, ty, vw, vh);
                    } else if(tabScroller != null &&
                            (child == tabScroller.scrollForwardButton ||
                            child == tabScroller.scrollBackwardButton)) {
                        Component scrollbutton = child;
                        Dimension bsize = scrollbutton.getPreferredSize();
                        int bx = 0;
                        int by = 0;
                        int bw = bsize.width;
                        int bh = bsize.height;
                        boolean visible = false;
                        switch(tabPlacement) {
                            case LEFT:
                            case RIGHT:
                                int totalTabHeight = rects[tabCount - 1].y + rects[tabCount - 1].height;
                                if(totalTabHeight > th) {
                                    visible = true;
                                    bx = (tabPlacement == LEFT ? tx + tw - bsize.width : tx);
                                    by = (child == tabScroller.scrollForwardButton) ?
                                            bounds.height - insets.bottom - bsize.height :
                                            bounds.height - insets.bottom - 2 * bsize.height;
                                }
                                break;
                            case BOTTOM:
                            case TOP:
                            default:
                                int totalTabWidth = rects[tabCount - 1].x + rects[tabCount - 1].width;
                                if(totalTabWidth > tw) {
                                    visible = true;
                                    bx = (child == tabScroller.scrollForwardButton) ?
                                            bounds.width - insets.left - bsize.width :
                                            bounds.width - insets.left - 2 * bsize.width;
                                    by = (tabPlacement == TOP ? ty + th - bsize.height : ty);
                                }
                        }
                        child.setVisible(visible);
                        if(visible) {
                            child.setBounds(bx, by, bw, bh);
                        }
                    } else {
                        child.setBounds(cx, cy, cw, ch);
                    }
                }
                super.layoutTabComponents();
                layoutCroppedEdge();
                if(shouldChangeFocus) {
                    if(!requestFocusForVisibleComponent()) {
                        tabPane.requestFocus();
                    }
                }
            }
        }
        private void layoutCroppedEdge() {
            tabScroller.croppedEdge.resetParams();
            Rectangle viewRect = tabScroller.viewport.getViewRect();
            int cropline;
            for (int i = 0; i < rects.length; i++) {
                Rectangle tabRect = rects[i];
                switch (tabPane.getTabPlacement()) {
                    case LEFT:
                    case RIGHT:
                        cropline = viewRect.y + viewRect.height;
                        if ((tabRect.y < cropline) && (tabRect.y + tabRect.height > cropline)) {
                            tabScroller.croppedEdge.setParams(i, cropline - tabRect.y - 1,
                                    -currentTabAreaInsets.left,  0);
                        }
                        break;
                    case TOP:
                    case BOTTOM:
                    default:
                        cropline = viewRect.x + viewRect.width;
                        if ((tabRect.x < cropline - 1) && (tabRect.x + tabRect.width > cropline)) {
                            tabScroller.croppedEdge.setParams(i, cropline - tabRect.x - 1,
                                    0, -currentTabAreaInsets.top);
                        }
                }
            }
        }
        protected void calculateTabRects(int tabPlacement, int tabCount) {
            FontMetrics metrics = getFontMetrics();
            Dimension size = tabPane.getSize();
            Insets insets = tabPane.getInsets();
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
            int fontHeight = metrics.getHeight();
            int selectedIndex = tabPane.getSelectedIndex();
            int i;
            boolean verticalTabRuns = (tabPlacement == LEFT || tabPlacement == RIGHT);
            boolean leftToRight = BasicGraphicsUtils.isLeftToRight(tabPane);
            int x = tabAreaInsets.left;
            int y = tabAreaInsets.top;
            int totalWidth = 0;
            int totalHeight = 0;
            switch(tabPlacement) {
              case LEFT:
              case RIGHT:
                  maxTabWidth = calculateMaxTabWidth(tabPlacement);
                  break;
              case BOTTOM:
              case TOP:
              default:
                  maxTabHeight = calculateMaxTabHeight(tabPlacement);
            }
            runCount = 0;
            selectedRun = -1;
            if (tabCount == 0) {
                return;
            }
            selectedRun = 0;
            runCount = 1;
            Rectangle rect;
            for (i = 0; i < tabCount; i++) {
                rect = rects[i];
                if (!verticalTabRuns) {
                    if (i > 0) {
                        rect.x = rects[i-1].x + rects[i-1].width;
                    } else {
                        tabRuns[0] = 0;
                        maxTabWidth = 0;
                        totalHeight += maxTabHeight;
                        rect.x = x;
                    }
                    rect.width = calculateTabWidth(tabPlacement, i, metrics);
                    totalWidth = rect.x + rect.width;
                    maxTabWidth = Math.max(maxTabWidth, rect.width);
                    rect.y = y;
                    rect.height = maxTabHeight;
                } else {
                    if (i > 0) {
                        rect.y = rects[i-1].y + rects[i-1].height;
                    } else {
                        tabRuns[0] = 0;
                        maxTabHeight = 0;
                        totalWidth = maxTabWidth;
                        rect.y = y;
                    }
                    rect.height = calculateTabHeight(tabPlacement, i, fontHeight);
                    totalHeight = rect.y + rect.height;
                    maxTabHeight = Math.max(maxTabHeight, rect.height);
                    rect.x = x;
                    rect.width = maxTabWidth;
                }
            }
            if (tabsOverlapBorder) {
                padSelectedTab(tabPlacement, selectedIndex);
            }
            if (!leftToRight && !verticalTabRuns) {
                int rightMargin = size.width
                                  - (insets.right + tabAreaInsets.right);
                for (i = 0; i < tabCount; i++) {
                    rects[i].x = rightMargin - rects[i].x - rects[i].width;
                }
            }
            tabScroller.tabPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
        }
    }
    private class ScrollableTabSupport implements ActionListener,
                            ChangeListener {
        public ScrollableTabViewport viewport;
        public ScrollableTabPanel tabPanel;
        public JButton scrollForwardButton;
        public JButton scrollBackwardButton;
        public CroppedEdge croppedEdge;
        public int leadingTabIndex;
        private Point tabViewPosition = new Point(0,0);
        ScrollableTabSupport(int tabPlacement) {
            viewport = new ScrollableTabViewport();
            tabPanel = new ScrollableTabPanel();
            viewport.setView(tabPanel);
            viewport.addChangeListener(this);
            croppedEdge = new CroppedEdge();
            createButtons();
        }
        void createButtons() {
            if (scrollForwardButton != null) {
                tabPane.remove(scrollForwardButton);
                scrollForwardButton.removeActionListener(this);
                tabPane.remove(scrollBackwardButton);
                scrollBackwardButton.removeActionListener(this);
            }
            int tabPlacement = tabPane.getTabPlacement();
            if (tabPlacement == TOP || tabPlacement == BOTTOM) {
                scrollForwardButton = createScrollButton(EAST);
                scrollBackwardButton = createScrollButton(WEST);
            } else { 
                scrollForwardButton = createScrollButton(SOUTH);
                scrollBackwardButton = createScrollButton(NORTH);
            }
            scrollForwardButton.addActionListener(this);
            scrollBackwardButton.addActionListener(this);
            tabPane.add(scrollForwardButton);
            tabPane.add(scrollBackwardButton);
        }
        public void scrollForward(int tabPlacement) {
            Dimension viewSize = viewport.getViewSize();
            Rectangle viewRect = viewport.getViewRect();
            if (tabPlacement == TOP || tabPlacement == BOTTOM) {
                if (viewRect.width >= viewSize.width - viewRect.x) {
                    return; 
                }
            } else { 
                if (viewRect.height >= viewSize.height - viewRect.y) {
                    return;
                }
            }
            setLeadingTabIndex(tabPlacement, leadingTabIndex+1);
        }
        public void scrollBackward(int tabPlacement) {
            if (leadingTabIndex == 0) {
                return; 
            }
            setLeadingTabIndex(tabPlacement, leadingTabIndex-1);
        }
        public void setLeadingTabIndex(int tabPlacement, int index) {
            leadingTabIndex = index;
            Dimension viewSize = viewport.getViewSize();
            Rectangle viewRect = viewport.getViewRect();
            switch(tabPlacement) {
              case TOP:
              case BOTTOM:
                tabViewPosition.x = leadingTabIndex == 0? 0 : rects[leadingTabIndex].x;
                if ((viewSize.width - tabViewPosition.x) < viewRect.width) {
                    Dimension extentSize = new Dimension(viewSize.width - tabViewPosition.x,
                                                         viewRect.height);
                    viewport.setExtentSize(extentSize);
                }
                break;
              case LEFT:
              case RIGHT:
                tabViewPosition.y = leadingTabIndex == 0? 0 : rects[leadingTabIndex].y;
                if ((viewSize.height - tabViewPosition.y) < viewRect.height) {
                     Dimension extentSize = new Dimension(viewRect.width,
                                                          viewSize.height - tabViewPosition.y);
                     viewport.setExtentSize(extentSize);
                }
            }
            viewport.setViewPosition(tabViewPosition);
        }
        public void stateChanged(ChangeEvent e) {
            updateView();
        }
        private void updateView() {
            int tabPlacement = tabPane.getTabPlacement();
            int tabCount = tabPane.getTabCount();
            Rectangle vpRect = viewport.getBounds();
            Dimension viewSize = viewport.getViewSize();
            Rectangle viewRect = viewport.getViewRect();
            leadingTabIndex = getClosestTab(viewRect.x, viewRect.y);
            if (leadingTabIndex + 1 < tabCount) {
                switch (tabPlacement) {
                case TOP:
                case BOTTOM:
                    if (rects[leadingTabIndex].x < viewRect.x) {
                        leadingTabIndex++;
                    }
                    break;
                case LEFT:
                case RIGHT:
                    if (rects[leadingTabIndex].y < viewRect.y) {
                        leadingTabIndex++;
                    }
                    break;
                }
            }
            Insets contentInsets = getContentBorderInsets(tabPlacement);
            switch(tabPlacement) {
              case LEFT:
                  tabPane.repaint(vpRect.x+vpRect.width, vpRect.y,
                                  contentInsets.left, vpRect.height);
                  scrollBackwardButton.setEnabled(
                          viewRect.y > 0 && leadingTabIndex > 0);
                  scrollForwardButton.setEnabled(
                          leadingTabIndex < tabCount-1 &&
                          viewSize.height-viewRect.y > viewRect.height);
                  break;
              case RIGHT:
                  tabPane.repaint(vpRect.x-contentInsets.right, vpRect.y,
                                  contentInsets.right, vpRect.height);
                  scrollBackwardButton.setEnabled(
                          viewRect.y > 0 && leadingTabIndex > 0);
                  scrollForwardButton.setEnabled(
                          leadingTabIndex < tabCount-1 &&
                          viewSize.height-viewRect.y > viewRect.height);
                  break;
              case BOTTOM:
                  tabPane.repaint(vpRect.x, vpRect.y-contentInsets.bottom,
                                  vpRect.width, contentInsets.bottom);
                  scrollBackwardButton.setEnabled(
                          viewRect.x > 0 && leadingTabIndex > 0);
                  scrollForwardButton.setEnabled(
                          leadingTabIndex < tabCount-1 &&
                          viewSize.width-viewRect.x > viewRect.width);
                  break;
              case TOP:
              default:
                  tabPane.repaint(vpRect.x, vpRect.y+vpRect.height,
                                  vpRect.width, contentInsets.top);
                  scrollBackwardButton.setEnabled(
                          viewRect.x > 0 && leadingTabIndex > 0);
                  scrollForwardButton.setEnabled(
                          leadingTabIndex < tabCount-1 &&
                          viewSize.width-viewRect.x > viewRect.width);
            }
        }
        public void actionPerformed(ActionEvent e) {
            ActionMap map = tabPane.getActionMap();
            if (map != null) {
                String actionKey;
                if (e.getSource() == scrollForwardButton) {
                    actionKey = "scrollTabsForwardAction";
                }
                else {
                    actionKey = "scrollTabsBackwardAction";
                }
                Action action = map.get(actionKey);
                if (action != null && action.isEnabled()) {
                    action.actionPerformed(new ActionEvent(tabPane,
                        ActionEvent.ACTION_PERFORMED, null, e.getWhen(),
                        e.getModifiers()));
                }
            }
        }
        public String toString() {
            return "viewport.viewSize=" + viewport.getViewSize() + "\n" +
                              "viewport.viewRectangle="+viewport.getViewRect()+"\n"+
                              "leadingTabIndex="+leadingTabIndex+"\n"+
                              "tabViewPosition=" + tabViewPosition;
        }
    }
    private class ScrollableTabViewport extends JViewport implements UIResource {
        public ScrollableTabViewport() {
            super();
            setName("TabbedPane.scrollableViewport");
            setScrollMode(SIMPLE_SCROLL_MODE);
            setOpaque(tabPane.isOpaque());
            Color bgColor = UIManager.getColor("TabbedPane.tabAreaBackground");
            if (bgColor == null) {
                bgColor = tabPane.getBackground();
            }
            setBackground(bgColor);
        }
    }
    private class ScrollableTabPanel extends JPanel implements UIResource {
        public ScrollableTabPanel() {
            super(null);
            setOpaque(tabPane.isOpaque());
            Color bgColor = UIManager.getColor("TabbedPane.tabAreaBackground");
            if (bgColor == null) {
                bgColor = tabPane.getBackground();
            }
            setBackground(bgColor);
        }
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            BasicTabbedPaneUI.this.paintTabArea(g, tabPane.getTabPlacement(),
                                                tabPane.getSelectedIndex());
            if (tabScroller.croppedEdge.isParamsSet() && tabContainer == null) {
                Rectangle croppedRect = rects[tabScroller.croppedEdge.getTabIndex()];
                g.translate(croppedRect.x, croppedRect.y);
                tabScroller.croppedEdge.paintComponent(g);
                g.translate(-croppedRect.x, -croppedRect.y);
            }
        }
        public void doLayout() {
            if (getComponentCount() > 0) {
                Component child = getComponent(0);
                child.setBounds(0, 0, getWidth(), getHeight());
            }
        }
    }
    private class ScrollableTabButton extends BasicArrowButton implements UIResource,
                                                                            SwingConstants {
        public ScrollableTabButton(int direction) {
            super(direction,
                  UIManager.getColor("TabbedPane.selected"),
                  UIManager.getColor("TabbedPane.shadow"),
                  UIManager.getColor("TabbedPane.darkShadow"),
                  UIManager.getColor("TabbedPane.highlight"));
        }
    }
    private class Handler implements ChangeListener, ContainerListener,
                  FocusListener, MouseListener, MouseMotionListener,
                  PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            JTabbedPane pane = (JTabbedPane)e.getSource();
            String name = e.getPropertyName();
            boolean isScrollLayout = scrollableTabLayoutEnabled();
            if (name == "mnemonicAt") {
                updateMnemonics();
                pane.repaint();
            }
            else if (name == "displayedMnemonicIndexAt") {
                pane.repaint();
            }
            else if (name =="indexForTitle") {
                calculatedBaseline = false;
                Integer index = (Integer) e.getNewValue();
                if (htmlViews != null) {
                    htmlViews.removeElementAt(index);
                }
                updateHtmlViews(index);
            } else if (name == "tabLayoutPolicy") {
                BasicTabbedPaneUI.this.uninstallUI(pane);
                BasicTabbedPaneUI.this.installUI(pane);
                calculatedBaseline = false;
            } else if (name == "tabPlacement") {
                if (scrollableTabLayoutEnabled()) {
                    tabScroller.createButtons();
                }
                calculatedBaseline = false;
            } else if (name == "opaque" && isScrollLayout) {
                boolean newVal = ((Boolean)e.getNewValue()).booleanValue();
                tabScroller.tabPanel.setOpaque(newVal);
                tabScroller.viewport.setOpaque(newVal);
            } else if (name == "background" && isScrollLayout) {
                Color newVal = (Color)e.getNewValue();
                tabScroller.tabPanel.setBackground(newVal);
                tabScroller.viewport.setBackground(newVal);
                Color newColor = selectedColor == null ? newVal : selectedColor;
                tabScroller.scrollForwardButton.setBackground(newColor);
                tabScroller.scrollBackwardButton.setBackground(newColor);
            } else if (name == "indexForTabComponent") {
                if (tabContainer != null) {
                    tabContainer.removeUnusedTabComponents();
                }
                Component c = tabPane.getTabComponentAt(
                        (Integer)e.getNewValue());
                if (c != null) {
                    if (tabContainer == null) {
                        installTabContainer();
                    } else {
                        tabContainer.add(c);
                    }
                }
                tabPane.revalidate();
                tabPane.repaint();
                calculatedBaseline = false;
            } else if (name == "indexForNullComponent") {
                isRunsDirty = true;
                updateHtmlViews((Integer)e.getNewValue());
            } else if (name == "font") {
                calculatedBaseline = false;
            }
        }
        private void updateHtmlViews(int index) {
            String title = tabPane.getTitleAt(index);
            boolean isHTML = BasicHTML.isHTMLString(title);
            if (isHTML) {
                if (htmlViews==null) {    
                    htmlViews = createHTMLVector();
                } else {                  
                    View v = BasicHTML.createHTMLView(tabPane, title);
                    htmlViews.insertElementAt(v, index);
                }
            } else {                             
                if (htmlViews!=null) {           
                    htmlViews.insertElementAt(null, index);
                }                                
            }
            updateMnemonics();
        }
        public void stateChanged(ChangeEvent e) {
            JTabbedPane tabPane = (JTabbedPane)e.getSource();
            tabPane.revalidate();
            tabPane.repaint();
            setFocusIndex(tabPane.getSelectedIndex(), false);
            if (scrollableTabLayoutEnabled()) {
                int index = tabPane.getSelectedIndex();
                if (index < rects.length && index != -1) {
                    tabScroller.tabPanel.scrollRectToVisible(
                            (Rectangle)rects[index].clone());
                }
            }
        }
        public void mouseClicked(MouseEvent e) {
        }
        public void mouseReleased(MouseEvent e) {
        }
        public void mouseEntered(MouseEvent e) {
            setRolloverTab(e.getX(), e.getY());
        }
        public void mouseExited(MouseEvent e) {
            setRolloverTab(-1);
        }
        public void mousePressed(MouseEvent e) {
            if (!tabPane.isEnabled()) {
                return;
            }
            int tabIndex = tabForCoordinate(tabPane, e.getX(), e.getY());
            if (tabIndex >= 0 && tabPane.isEnabledAt(tabIndex)) {
                if (tabIndex != tabPane.getSelectedIndex()) {
                    tabPane.setSelectedIndex(tabIndex);
                }
                else if (tabPane.isRequestFocusEnabled()) {
                    tabPane.requestFocus();
                }
            }
        }
        public void mouseDragged(MouseEvent e) {
        }
        public void mouseMoved(MouseEvent e) {
            setRolloverTab(e.getX(), e.getY());
        }
        public void focusGained(FocusEvent e) {
           setFocusIndex(tabPane.getSelectedIndex(), true);
        }
        public void focusLost(FocusEvent e) {
           repaintTab(focusIndex);
        }
        public void componentAdded(ContainerEvent e) {
            JTabbedPane tp = (JTabbedPane)e.getContainer();
            Component child = e.getChild();
            if (child instanceof UIResource) {
                return;
            }
            isRunsDirty = true;
            updateHtmlViews(tp.indexOfComponent(child));
        }
        public void componentRemoved(ContainerEvent e) {
            JTabbedPane tp = (JTabbedPane)e.getContainer();
            Component child = e.getChild();
            if (child instanceof UIResource) {
                return;
            }
            Integer indexObj =
                (Integer)tp.getClientProperty("__index_to_remove__");
            if (indexObj != null) {
                int index = indexObj.intValue();
                if (htmlViews != null && htmlViews.size() > index) {
                    htmlViews.removeElementAt(index);
                }
                tp.putClientProperty("__index_to_remove__", null);
            }
            isRunsDirty = true;
            updateMnemonics();
            validateFocusIndex();
        }
    }
    public class PropertyChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            getHandler().propertyChange(e);
        }
    }
    public class TabSelectionHandler implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            getHandler().stateChanged(e);
        }
    }
    public class MouseHandler extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            getHandler().mousePressed(e);
        }
    }
    public class FocusHandler extends FocusAdapter {
        public void focusGained(FocusEvent e) {
            getHandler().focusGained(e);
        }
        public void focusLost(FocusEvent e) {
            getHandler().focusLost(e);
        }
    }
    private Vector<View> createHTMLVector() {
        Vector<View> htmlViews = new Vector<View>();
        int count = tabPane.getTabCount();
        if (count>0) {
            for (int i=0 ; i<count; i++) {
                String title = tabPane.getTitleAt(i);
                if (BasicHTML.isHTMLString(title)) {
                    htmlViews.addElement(BasicHTML.createHTMLView(tabPane, title));
                } else {
                    htmlViews.addElement(null);
                }
            }
        }
        return htmlViews;
    }
    private class TabContainer extends JPanel implements UIResource {
        private boolean notifyTabbedPane = true;
        public TabContainer() {
            super(null);
            setOpaque(false);
        }
        public void remove(Component comp) {
            int index = tabPane.indexOfTabComponent(comp);
            super.remove(comp);
            if (notifyTabbedPane && index != -1) {
                tabPane.setTabComponentAt(index, null);
            }
        }
        private void removeUnusedTabComponents() {
            for (Component c : getComponents()) {
                if (!(c instanceof UIResource)) {
                    int index = tabPane.indexOfTabComponent(c);
                    if (index == -1) {
                        super.remove(c);
                    }
                }
            }
        }
        public boolean isOptimizedDrawingEnabled() {
            return tabScroller != null && !tabScroller.croppedEdge.isParamsSet();
        }
        public void doLayout() {
            if (scrollableTabLayoutEnabled()) {
                tabScroller.tabPanel.repaint();
                tabScroller.updateView();
            } else {
                tabPane.repaint(getBounds());
            }
        }
    }
    private class CroppedEdge extends JPanel implements UIResource {
        private Shape shape;
        private int tabIndex;
        private int cropline;
        private int cropx, cropy;
        public CroppedEdge() {
            setOpaque(false);
        }
        public void setParams(int tabIndex, int cropline, int cropx, int cropy) {
            this.tabIndex = tabIndex;
            this.cropline = cropline;
            this.cropx = cropx;
            this.cropy = cropy;
            Rectangle tabRect = rects[tabIndex];
            setBounds(tabRect);
            shape = createCroppedTabShape(tabPane.getTabPlacement(), tabRect, cropline);
            if (getParent() == null && tabContainer != null) {
                tabContainer.add(this, 0);
            }
        }
        public void resetParams() {
            shape = null;
            if (getParent() == tabContainer && tabContainer != null) {
                tabContainer.remove(this);
            }
        }
        public boolean isParamsSet() {
            return shape != null;
        }
        public int getTabIndex() {
            return tabIndex;
        }
        public int getCropline() {
            return cropline;
        }
        public int getCroppedSideWidth() {
            return 3;
        }
        private Color getBgColor() {
            Component parent = tabPane.getParent();
            if (parent != null) {
                Color bg = parent.getBackground();
                if (bg != null) {
                    return bg;
                }
            }
            return UIManager.getColor("control");
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isParamsSet() && g instanceof Graphics2D) {
                Graphics2D g2 = (Graphics2D) g;
                g2.clipRect(0, 0, getWidth(), getHeight());
                g2.setColor(getBgColor());
                g2.translate(cropx, cropy);
                g2.fill(shape);
                paintCroppedTabEdge(g);
                g2.translate(-cropx, -cropy);
            }
        }
    }
}
