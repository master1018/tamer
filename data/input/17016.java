public class BasicScrollBarUI
    extends ScrollBarUI implements LayoutManager, SwingConstants
{
    private static final int POSITIVE_SCROLL = 1;
    private static final int NEGATIVE_SCROLL = -1;
    private static final int MIN_SCROLL = 2;
    private static final int MAX_SCROLL = 3;
    protected Dimension minimumThumbSize;
    protected Dimension maximumThumbSize;
    protected Color thumbHighlightColor;
    protected Color thumbLightShadowColor;
    protected Color thumbDarkShadowColor;
    protected Color thumbColor;
    protected Color trackColor;
    protected Color trackHighlightColor;
    protected JScrollBar scrollbar;
    protected JButton incrButton;
    protected JButton decrButton;
    protected boolean isDragging;
    protected TrackListener trackListener;
    protected ArrowButtonListener buttonListener;
    protected ModelListener modelListener;
    protected Rectangle thumbRect;
    protected Rectangle trackRect;
    protected int trackHighlight;
    protected static final int NO_HIGHLIGHT = 0;
    protected static final int DECREASE_HIGHLIGHT = 1;
    protected static final int INCREASE_HIGHLIGHT = 2;
    protected ScrollListener scrollListener;
    protected PropertyChangeListener propertyChangeListener;
    protected Timer scrollTimer;
    private final static int scrollSpeedThrottle = 60; 
    private boolean supportsAbsolutePositioning;
    protected int scrollBarWidth;
    private Handler handler;
    private boolean thumbActive;
    private boolean useCachedValue = false;
    private int scrollBarValue;
    protected int incrGap;
    protected int decrGap;
    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions(Actions.POSITIVE_UNIT_INCREMENT));
        map.put(new Actions(Actions.POSITIVE_BLOCK_INCREMENT));
        map.put(new Actions(Actions.NEGATIVE_UNIT_INCREMENT));
        map.put(new Actions(Actions.NEGATIVE_BLOCK_INCREMENT));
        map.put(new Actions(Actions.MIN_SCROLL));
        map.put(new Actions(Actions.MAX_SCROLL));
    }
    public static ComponentUI createUI(JComponent c)    {
        return new BasicScrollBarUI();
    }
    protected void configureScrollBarColors()
    {
        LookAndFeel.installColors(scrollbar, "ScrollBar.background",
                                  "ScrollBar.foreground");
        thumbHighlightColor = UIManager.getColor("ScrollBar.thumbHighlight");
        thumbLightShadowColor = UIManager.getColor("ScrollBar.thumbShadow");
        thumbDarkShadowColor = UIManager.getColor("ScrollBar.thumbDarkShadow");
        thumbColor = UIManager.getColor("ScrollBar.thumb");
        trackColor = UIManager.getColor("ScrollBar.track");
        trackHighlightColor = UIManager.getColor("ScrollBar.trackHighlight");
    }
    public void installUI(JComponent c)   {
        scrollbar = (JScrollBar)c;
        thumbRect = new Rectangle(0, 0, 0, 0);
        trackRect = new Rectangle(0, 0, 0, 0);
        installDefaults();
        installComponents();
        installListeners();
        installKeyboardActions();
    }
    public void uninstallUI(JComponent c) {
        scrollbar = (JScrollBar)c;
        uninstallListeners();
        uninstallDefaults();
        uninstallComponents();
        uninstallKeyboardActions();
        thumbRect = null;
        scrollbar = null;
        incrButton = null;
        decrButton = null;
    }
    protected void installDefaults()
    {
        scrollBarWidth = UIManager.getInt("ScrollBar.width");
        if (scrollBarWidth <= 0) {
            scrollBarWidth = 16;
        }
        minimumThumbSize = (Dimension)UIManager.get("ScrollBar.minimumThumbSize");
        maximumThumbSize = (Dimension)UIManager.get("ScrollBar.maximumThumbSize");
        Boolean absB = (Boolean)UIManager.get("ScrollBar.allowsAbsolutePositioning");
        supportsAbsolutePositioning = (absB != null) ? absB.booleanValue() :
                                      false;
        trackHighlight = NO_HIGHLIGHT;
        if (scrollbar.getLayout() == null ||
                     (scrollbar.getLayout() instanceof UIResource)) {
            scrollbar.setLayout(this);
        }
        configureScrollBarColors();
        LookAndFeel.installBorder(scrollbar, "ScrollBar.border");
        LookAndFeel.installProperty(scrollbar, "opaque", Boolean.TRUE);
        scrollBarValue = scrollbar.getValue();
        incrGap = UIManager.getInt("ScrollBar.incrementButtonGap");
        decrGap = UIManager.getInt("ScrollBar.decrementButtonGap");
        String scaleKey = (String)scrollbar.getClientProperty(
                "JComponent.sizeVariant");
        if (scaleKey != null){
            if ("large".equals(scaleKey)){
                scrollBarWidth *= 1.15;
                incrGap *= 1.15;
                decrGap *= 1.15;
            } else if ("small".equals(scaleKey)){
                scrollBarWidth *= 0.857;
                incrGap *= 0.857;
                decrGap *= 0.714;
            } else if ("mini".equals(scaleKey)){
                scrollBarWidth *= 0.714;
                incrGap *= 0.714;
                decrGap *= 0.714;
            }
        }
    }
    protected void installComponents(){
        switch (scrollbar.getOrientation()) {
        case JScrollBar.VERTICAL:
            incrButton = createIncreaseButton(SOUTH);
            decrButton = createDecreaseButton(NORTH);
            break;
        case JScrollBar.HORIZONTAL:
            if (scrollbar.getComponentOrientation().isLeftToRight()) {
                incrButton = createIncreaseButton(EAST);
                decrButton = createDecreaseButton(WEST);
            } else {
                incrButton = createIncreaseButton(WEST);
                decrButton = createDecreaseButton(EAST);
            }
            break;
        }
        scrollbar.add(incrButton);
        scrollbar.add(decrButton);
        scrollbar.setEnabled(scrollbar.isEnabled());
    }
    protected void uninstallComponents(){
        scrollbar.remove(incrButton);
        scrollbar.remove(decrButton);
    }
    protected void installListeners(){
        trackListener = createTrackListener();
        buttonListener = createArrowButtonListener();
        modelListener = createModelListener();
        propertyChangeListener = createPropertyChangeListener();
        scrollbar.addMouseListener(trackListener);
        scrollbar.addMouseMotionListener(trackListener);
        scrollbar.getModel().addChangeListener(modelListener);
        scrollbar.addPropertyChangeListener(propertyChangeListener);
        scrollbar.addFocusListener(getHandler());
        if (incrButton != null) {
            incrButton.addMouseListener(buttonListener);
        }
        if (decrButton != null) {
            decrButton.addMouseListener(buttonListener);
        }
        scrollListener = createScrollListener();
        scrollTimer = new Timer(scrollSpeedThrottle, scrollListener);
        scrollTimer.setInitialDelay(300);  
    }
    protected void installKeyboardActions(){
        LazyActionMap.installLazyActionMap(scrollbar, BasicScrollBarUI.class,
                                           "ScrollBar.actionMap");
        InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
        SwingUtilities.replaceUIInputMap(scrollbar, JComponent.WHEN_FOCUSED,
                                         inputMap);
        inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        SwingUtilities.replaceUIInputMap(scrollbar,
                   JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);
    }
    protected void uninstallKeyboardActions(){
        SwingUtilities.replaceUIInputMap(scrollbar, JComponent.WHEN_FOCUSED,
                                         null);
        SwingUtilities.replaceUIActionMap(scrollbar, null);
    }
    private InputMap getInputMap(int condition) {
        if (condition == JComponent.WHEN_FOCUSED) {
            InputMap keyMap = (InputMap)DefaultLookup.get(
                        scrollbar, this, "ScrollBar.focusInputMap");
            InputMap rtlKeyMap;
            if (scrollbar.getComponentOrientation().isLeftToRight() ||
                ((rtlKeyMap = (InputMap)DefaultLookup.get(scrollbar, this, "ScrollBar.focusInputMap.RightToLeft")) == null)) {
                return keyMap;
            } else {
                rtlKeyMap.setParent(keyMap);
                return rtlKeyMap;
            }
        }
        else if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
            InputMap keyMap = (InputMap)DefaultLookup.get(
                        scrollbar, this, "ScrollBar.ancestorInputMap");
            InputMap rtlKeyMap;
            if (scrollbar.getComponentOrientation().isLeftToRight() ||
                ((rtlKeyMap = (InputMap)DefaultLookup.get(scrollbar, this, "ScrollBar.ancestorInputMap.RightToLeft")) == null)) {
                return keyMap;
            } else {
                rtlKeyMap.setParent(keyMap);
                return rtlKeyMap;
            }
        }
        return null;
    }
    protected void uninstallListeners() {
        scrollTimer.stop();
        scrollTimer = null;
        if (decrButton != null){
            decrButton.removeMouseListener(buttonListener);
        }
        if (incrButton != null){
            incrButton.removeMouseListener(buttonListener);
        }
        scrollbar.getModel().removeChangeListener(modelListener);
        scrollbar.removeMouseListener(trackListener);
        scrollbar.removeMouseMotionListener(trackListener);
        scrollbar.removePropertyChangeListener(propertyChangeListener);
        scrollbar.removeFocusListener(getHandler());
        handler = null;
    }
    protected void uninstallDefaults(){
        LookAndFeel.uninstallBorder(scrollbar);
        if (scrollbar.getLayout() == this) {
            scrollbar.setLayout(null);
        }
    }
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    protected TrackListener createTrackListener(){
        return new TrackListener();
    }
    protected ArrowButtonListener createArrowButtonListener(){
        return new ArrowButtonListener();
    }
    protected ModelListener createModelListener(){
        return new ModelListener();
    }
    protected ScrollListener createScrollListener(){
        return new ScrollListener();
    }
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }
    private void updateThumbState(int x, int y) {
        Rectangle rect = getThumbBounds();
        setThumbRollover(rect.contains(x, y));
    }
    protected void setThumbRollover(boolean active) {
        if (thumbActive != active) {
            thumbActive = active;
            scrollbar.repaint(getThumbBounds());
        }
    }
    public boolean isThumbRollover() {
        return thumbActive;
    }
    public void paint(Graphics g, JComponent c) {
        paintTrack(g, c, getTrackBounds());
        Rectangle thumbBounds = getThumbBounds();
        if (thumbBounds.intersects(g.getClipBounds())) {
            paintThumb(g, c, thumbBounds);
        }
    }
    public Dimension getPreferredSize(JComponent c) {
        return (scrollbar.getOrientation() == JScrollBar.VERTICAL)
            ? new Dimension(scrollBarWidth, 48)
            : new Dimension(48, scrollBarWidth);
    }
    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    protected JButton createDecreaseButton(int orientation)  {
        return new BasicArrowButton(orientation,
                                    UIManager.getColor("ScrollBar.thumb"),
                                    UIManager.getColor("ScrollBar.thumbShadow"),
                                    UIManager.getColor("ScrollBar.thumbDarkShadow"),
                                    UIManager.getColor("ScrollBar.thumbHighlight"));
    }
    protected JButton createIncreaseButton(int orientation)  {
        return new BasicArrowButton(orientation,
                                    UIManager.getColor("ScrollBar.thumb"),
                                    UIManager.getColor("ScrollBar.thumbShadow"),
                                    UIManager.getColor("ScrollBar.thumbDarkShadow"),
                                    UIManager.getColor("ScrollBar.thumbHighlight"));
    }
    protected void paintDecreaseHighlight(Graphics g)
    {
        Insets insets = scrollbar.getInsets();
        Rectangle thumbR = getThumbBounds();
        g.setColor(trackHighlightColor);
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            int x = insets.left;
            int y = trackRect.y;
            int w = scrollbar.getWidth() - (insets.left + insets.right);
            int h = thumbR.y - y;
            g.fillRect(x, y, w, h);
        } else {
            int x, w;
            if (scrollbar.getComponentOrientation().isLeftToRight()) {
               x = trackRect.x;
                w = thumbR.x - x;
            } else {
                x = thumbR.x + thumbR.width;
                w = trackRect.x + trackRect.width - x;
            }
            int y = insets.top;
            int h = scrollbar.getHeight() - (insets.top + insets.bottom);
            g.fillRect(x, y, w, h);
        }
    }
    protected void paintIncreaseHighlight(Graphics g)
    {
        Insets insets = scrollbar.getInsets();
        Rectangle thumbR = getThumbBounds();
        g.setColor(trackHighlightColor);
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            int x = insets.left;
            int y = thumbR.y + thumbR.height;
            int w = scrollbar.getWidth() - (insets.left + insets.right);
            int h = trackRect.y + trackRect.height - y;
            g.fillRect(x, y, w, h);
        }
        else {
            int x, w;
            if (scrollbar.getComponentOrientation().isLeftToRight()) {
                x = thumbR.x + thumbR.width;
                w = trackRect.x + trackRect.width - x;
            } else {
                x = trackRect.x;
                w = thumbR.x - x;
            }
            int y = insets.top;
            int h = scrollbar.getHeight() - (insets.top + insets.bottom);
            g.fillRect(x, y, w, h);
        }
    }
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds)
    {
        g.setColor(trackColor);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        if(trackHighlight == DECREASE_HIGHLIGHT)        {
            paintDecreaseHighlight(g);
        }
        else if(trackHighlight == INCREASE_HIGHLIGHT)           {
            paintIncreaseHighlight(g);
        }
    }
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds)
    {
        if(thumbBounds.isEmpty() || !scrollbar.isEnabled())     {
            return;
        }
        int w = thumbBounds.width;
        int h = thumbBounds.height;
        g.translate(thumbBounds.x, thumbBounds.y);
        g.setColor(thumbDarkShadowColor);
        g.drawRect(0, 0, w-1, h-1);
        g.setColor(thumbColor);
        g.fillRect(0, 0, w-1, h-1);
        g.setColor(thumbHighlightColor);
        g.drawLine(1, 1, 1, h-2);
        g.drawLine(2, 1, w-3, 1);
        g.setColor(thumbLightShadowColor);
        g.drawLine(2, h-2, w-2, h-2);
        g.drawLine(w-2, 1, w-2, h-3);
        g.translate(-thumbBounds.x, -thumbBounds.y);
    }
    protected Dimension getMinimumThumbSize() {
        return minimumThumbSize;
    }
    protected Dimension getMaximumThumbSize()   {
        return maximumThumbSize;
    }
    public void addLayoutComponent(String name, Component child) {}
    public void removeLayoutComponent(Component child) {}
    public Dimension preferredLayoutSize(Container scrollbarContainer)  {
        return getPreferredSize((JComponent)scrollbarContainer);
    }
    public Dimension minimumLayoutSize(Container scrollbarContainer) {
        return getMinimumSize((JComponent)scrollbarContainer);
    }
    private int getValue(JScrollBar sb) {
        return (useCachedValue) ? scrollBarValue : sb.getValue();
    }
    protected void layoutVScrollbar(JScrollBar sb)
    {
        Dimension sbSize = sb.getSize();
        Insets sbInsets = sb.getInsets();
        int itemW = sbSize.width - (sbInsets.left + sbInsets.right);
        int itemX = sbInsets.left;
        boolean squareButtons = DefaultLookup.getBoolean(
            scrollbar, this, "ScrollBar.squareButtons", false);
        int decrButtonH = squareButtons ? itemW :
                          decrButton.getPreferredSize().height;
        int decrButtonY = sbInsets.top;
        int incrButtonH = squareButtons ? itemW :
                          incrButton.getPreferredSize().height;
        int incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
        int sbInsetsH = sbInsets.top + sbInsets.bottom;
        int sbButtonsH = decrButtonH + incrButtonH;
        int gaps = decrGap + incrGap;
        float trackH = sbSize.height - (sbInsetsH + sbButtonsH) - gaps;
        float min = sb.getMinimum();
        float extent = sb.getVisibleAmount();
        float range = sb.getMaximum() - min;
        float value = getValue(sb);
        int thumbH = (range <= 0)
            ? getMaximumThumbSize().height : (int)(trackH * (extent / range));
        thumbH = Math.max(thumbH, getMinimumThumbSize().height);
        thumbH = Math.min(thumbH, getMaximumThumbSize().height);
        int thumbY = incrButtonY - incrGap - thumbH;
        if (value < (sb.getMaximum() - sb.getVisibleAmount())) {
            float thumbRange = trackH - thumbH;
            thumbY = (int)(0.5f + (thumbRange * ((value - min) / (range - extent))));
            thumbY +=  decrButtonY + decrButtonH + decrGap;
        }
        int sbAvailButtonH = (sbSize.height - sbInsetsH);
        if (sbAvailButtonH < sbButtonsH) {
            incrButtonH = decrButtonH = sbAvailButtonH / 2;
            incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
        }
        decrButton.setBounds(itemX, decrButtonY, itemW, decrButtonH);
        incrButton.setBounds(itemX, incrButtonY, itemW, incrButtonH);
        int itrackY = decrButtonY + decrButtonH + decrGap;
        int itrackH = incrButtonY - incrGap - itrackY;
        trackRect.setBounds(itemX, itrackY, itemW, itrackH);
        if(thumbH >= (int)trackH)       {
            if (UIManager.getBoolean("ScrollBar.alwaysShowThumb")) {
                setThumbBounds(itemX, itrackY, itemW, itrackH);
            } else {
                setThumbBounds(0, 0, 0, 0);
            }
        }
        else {
            if ((thumbY + thumbH) > incrButtonY - incrGap) {
                thumbY = incrButtonY - incrGap - thumbH;
            }
            if (thumbY  < (decrButtonY + decrButtonH + decrGap)) {
                thumbY = decrButtonY + decrButtonH + decrGap + 1;
            }
            setThumbBounds(itemX, thumbY, itemW, thumbH);
        }
    }
    protected void layoutHScrollbar(JScrollBar sb)
    {
        Dimension sbSize = sb.getSize();
        Insets sbInsets = sb.getInsets();
        int itemH = sbSize.height - (sbInsets.top + sbInsets.bottom);
        int itemY = sbInsets.top;
        boolean ltr = sb.getComponentOrientation().isLeftToRight();
        boolean squareButtons = DefaultLookup.getBoolean(
            scrollbar, this, "ScrollBar.squareButtons", false);
        int leftButtonW = squareButtons ? itemH :
                          decrButton.getPreferredSize().width;
        int rightButtonW = squareButtons ? itemH :
                          incrButton.getPreferredSize().width;
        if (!ltr) {
            int temp = leftButtonW;
            leftButtonW = rightButtonW;
            rightButtonW = temp;
        }
        int leftButtonX = sbInsets.left;
        int rightButtonX = sbSize.width - (sbInsets.right + rightButtonW);
        int leftGap = ltr ? decrGap : incrGap;
        int rightGap = ltr ? incrGap : decrGap;
        int sbInsetsW = sbInsets.left + sbInsets.right;
        int sbButtonsW = leftButtonW + rightButtonW;
        float trackW = sbSize.width - (sbInsetsW + sbButtonsW) - (leftGap + rightGap);
        float min = sb.getMinimum();
        float max = sb.getMaximum();
        float extent = sb.getVisibleAmount();
        float range = max - min;
        float value = getValue(sb);
        int thumbW = (range <= 0)
            ? getMaximumThumbSize().width : (int)(trackW * (extent / range));
        thumbW = Math.max(thumbW, getMinimumThumbSize().width);
        thumbW = Math.min(thumbW, getMaximumThumbSize().width);
        int thumbX = ltr ? rightButtonX - rightGap - thumbW : leftButtonX + leftButtonW + leftGap;
        if (value < (max - sb.getVisibleAmount())) {
            float thumbRange = trackW - thumbW;
            if( ltr ) {
                thumbX = (int)(0.5f + (thumbRange * ((value - min) / (range - extent))));
            } else {
                thumbX = (int)(0.5f + (thumbRange * ((max - extent - value) / (range - extent))));
            }
            thumbX += leftButtonX + leftButtonW + leftGap;
        }
        int sbAvailButtonW = (sbSize.width - sbInsetsW);
        if (sbAvailButtonW < sbButtonsW) {
            rightButtonW = leftButtonW = sbAvailButtonW / 2;
            rightButtonX = sbSize.width - (sbInsets.right + rightButtonW + rightGap);
        }
        (ltr ? decrButton : incrButton).setBounds(leftButtonX, itemY, leftButtonW, itemH);
        (ltr ? incrButton : decrButton).setBounds(rightButtonX, itemY, rightButtonW, itemH);
        int itrackX = leftButtonX + leftButtonW + leftGap;
        int itrackW = rightButtonX - rightGap - itrackX;
        trackRect.setBounds(itrackX, itemY, itrackW, itemH);
        if (thumbW >= (int)trackW) {
            if (UIManager.getBoolean("ScrollBar.alwaysShowThumb")) {
                setThumbBounds(itrackX, itemY, itrackW, itemH);
            } else {
                setThumbBounds(0, 0, 0, 0);
            }
        }
        else {
            if (thumbX + thumbW > rightButtonX - rightGap) {
                thumbX = rightButtonX - rightGap - thumbW;
            }
            if (thumbX  < leftButtonX + leftButtonW + leftGap) {
                thumbX = leftButtonX + leftButtonW + leftGap + 1;
            }
            setThumbBounds(thumbX, itemY, thumbW, itemH);
        }
    }
    public void layoutContainer(Container scrollbarContainer)
    {
        if (isDragging) {
            return;
        }
        JScrollBar scrollbar = (JScrollBar)scrollbarContainer;
        switch (scrollbar.getOrientation()) {
        case JScrollBar.VERTICAL:
            layoutVScrollbar(scrollbar);
            break;
        case JScrollBar.HORIZONTAL:
            layoutHScrollbar(scrollbar);
            break;
        }
    }
    protected void setThumbBounds(int x, int y, int width, int height)
    {
        if ((thumbRect.x == x) &&
            (thumbRect.y == y) &&
            (thumbRect.width == width) &&
            (thumbRect.height == height)) {
            return;
        }
        int minX = Math.min(x, thumbRect.x);
        int minY = Math.min(y, thumbRect.y);
        int maxX = Math.max(x + width, thumbRect.x + thumbRect.width);
        int maxY = Math.max(y + height, thumbRect.y + thumbRect.height);
        thumbRect.setBounds(x, y, width, height);
        scrollbar.repaint(minX, minY, maxX - minX, maxY - minY);
        setThumbRollover(false);
    }
    protected Rectangle getThumbBounds() {
        return thumbRect;
    }
    protected Rectangle getTrackBounds() {
        return trackRect;
    }
    static void scrollByBlock(JScrollBar scrollbar, int direction) {
            int oldValue = scrollbar.getValue();
            int blockIncrement = scrollbar.getBlockIncrement(direction);
            int delta = blockIncrement * ((direction > 0) ? +1 : -1);
            int newValue = oldValue + delta;
            if (delta > 0 && newValue < oldValue) {
                newValue = scrollbar.getMaximum();
            }
            else if (delta < 0 && newValue > oldValue) {
                newValue = scrollbar.getMinimum();
            }
            scrollbar.setValue(newValue);
    }
    protected void scrollByBlock(int direction)
    {
        scrollByBlock(scrollbar, direction);
            trackHighlight = direction > 0 ? INCREASE_HIGHLIGHT : DECREASE_HIGHLIGHT;
            Rectangle dirtyRect = getTrackBounds();
            scrollbar.repaint(dirtyRect.x, dirtyRect.y, dirtyRect.width, dirtyRect.height);
    }
    static void scrollByUnits(JScrollBar scrollbar, int direction,
                              int units, boolean limitToBlock) {
        int delta;
        int limit = -1;
        if (limitToBlock) {
            if (direction < 0) {
                limit = scrollbar.getValue() -
                                         scrollbar.getBlockIncrement(direction);
            }
            else {
                limit = scrollbar.getValue() +
                                         scrollbar.getBlockIncrement(direction);
            }
        }
        for (int i=0; i<units; i++) {
            if (direction > 0) {
                delta = scrollbar.getUnitIncrement(direction);
            }
            else {
                delta = -scrollbar.getUnitIncrement(direction);
            }
            int oldValue = scrollbar.getValue();
            int newValue = oldValue + delta;
            if (delta > 0 && newValue < oldValue) {
                newValue = scrollbar.getMaximum();
            }
            else if (delta < 0 && newValue > oldValue) {
                newValue = scrollbar.getMinimum();
            }
            if (oldValue == newValue) {
                break;
            }
            if (limitToBlock && i > 0) {
                assert limit != -1;
                if ((direction < 0 && newValue < limit) ||
                    (direction > 0 && newValue > limit)) {
                    break;
                }
            }
            scrollbar.setValue(newValue);
        }
    }
    protected void scrollByUnit(int direction)  {
        scrollByUnits(scrollbar, direction, 1, false);
    }
    public boolean getSupportsAbsolutePositioning() {
        return supportsAbsolutePositioning;
    }
    protected class ModelListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            if (!useCachedValue) {
                scrollBarValue = scrollbar.getValue();
            }
            layoutContainer(scrollbar);
            useCachedValue = false;
        }
    }
    protected class TrackListener
        extends MouseAdapter implements MouseMotionListener
    {
        protected transient int offset;
        protected transient int currentMouseX, currentMouseY;
        private transient int direction = +1;
        public void mouseReleased(MouseEvent e)
        {
            if (isDragging) {
                updateThumbState(e.getX(), e.getY());
            }
            if (SwingUtilities.isRightMouseButton(e) ||
                (!getSupportsAbsolutePositioning() &&
                 SwingUtilities.isMiddleMouseButton(e)))
                return;
            if(!scrollbar.isEnabled())
                return;
            Rectangle r = getTrackBounds();
            scrollbar.repaint(r.x, r.y, r.width, r.height);
            trackHighlight = NO_HIGHLIGHT;
            isDragging = false;
            offset = 0;
            scrollTimer.stop();
            useCachedValue = true;
            scrollbar.setValueIsAdjusting(false);
        }
        public void mousePressed(MouseEvent e)
        {
            if (SwingUtilities.isRightMouseButton(e) ||
                (!getSupportsAbsolutePositioning() &&
                 SwingUtilities.isMiddleMouseButton(e)))
                return;
            if(!scrollbar.isEnabled())
                return;
            if (!scrollbar.hasFocus() && scrollbar.isRequestFocusEnabled()) {
                scrollbar.requestFocus();
            }
            useCachedValue = true;
            scrollbar.setValueIsAdjusting(true);
            currentMouseX = e.getX();
            currentMouseY = e.getY();
            if(getThumbBounds().contains(currentMouseX, currentMouseY)) {
                switch (scrollbar.getOrientation()) {
                case JScrollBar.VERTICAL:
                    offset = currentMouseY - getThumbBounds().y;
                    break;
                case JScrollBar.HORIZONTAL:
                    offset = currentMouseX - getThumbBounds().x;
                    break;
                }
                isDragging = true;
                return;
            }
            else if (getSupportsAbsolutePositioning() &&
                     SwingUtilities.isMiddleMouseButton(e)) {
                switch (scrollbar.getOrientation()) {
                case JScrollBar.VERTICAL:
                    offset = getThumbBounds().height / 2;
                    break;
                case JScrollBar.HORIZONTAL:
                    offset = getThumbBounds().width / 2;
                    break;
                }
                isDragging = true;
                setValueFrom(e);
                return;
            }
            isDragging = false;
            Dimension sbSize = scrollbar.getSize();
            direction = +1;
            switch (scrollbar.getOrientation()) {
            case JScrollBar.VERTICAL:
                if (getThumbBounds().isEmpty()) {
                    int scrollbarCenter = sbSize.height / 2;
                    direction = (currentMouseY < scrollbarCenter) ? -1 : +1;
                } else {
                    int thumbY = getThumbBounds().y;
                    direction = (currentMouseY < thumbY) ? -1 : +1;
                }
                break;
            case JScrollBar.HORIZONTAL:
                if (getThumbBounds().isEmpty()) {
                    int scrollbarCenter = sbSize.width / 2;
                    direction = (currentMouseX < scrollbarCenter) ? -1 : +1;
                } else {
                    int thumbX = getThumbBounds().x;
                    direction = (currentMouseX < thumbX) ? -1 : +1;
                }
                if (!scrollbar.getComponentOrientation().isLeftToRight()) {
                    direction = -direction;
                }
                break;
            }
            scrollByBlock(direction);
            scrollTimer.stop();
            scrollListener.setDirection(direction);
            scrollListener.setScrollByBlock(true);
            startScrollTimerIfNecessary();
        }
        public void mouseDragged(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e) ||
                (!getSupportsAbsolutePositioning() &&
                 SwingUtilities.isMiddleMouseButton(e)))
                return;
            if(!scrollbar.isEnabled() || getThumbBounds().isEmpty()) {
                return;
            }
            if (isDragging) {
                setValueFrom(e);
            } else {
                currentMouseX = e.getX();
                currentMouseY = e.getY();
                updateThumbState(currentMouseX, currentMouseY);
                startScrollTimerIfNecessary();
            }
        }
        private void setValueFrom(MouseEvent e) {
            boolean active = isThumbRollover();
            BoundedRangeModel model = scrollbar.getModel();
            Rectangle thumbR = getThumbBounds();
            float trackLength;
            int thumbMin, thumbMax, thumbPos;
            if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                thumbMin = trackRect.y;
                thumbMax = trackRect.y + trackRect.height - thumbR.height;
                thumbPos = Math.min(thumbMax, Math.max(thumbMin, (e.getY() - offset)));
                setThumbBounds(thumbR.x, thumbPos, thumbR.width, thumbR.height);
                trackLength = getTrackBounds().height;
            }
            else {
                thumbMin = trackRect.x;
                thumbMax = trackRect.x + trackRect.width - thumbR.width;
                thumbPos = Math.min(thumbMax, Math.max(thumbMin, (e.getX() - offset)));
                setThumbBounds(thumbPos, thumbR.y, thumbR.width, thumbR.height);
                trackLength = getTrackBounds().width;
            }
            if (thumbPos == thumbMax) {
                if (scrollbar.getOrientation() == JScrollBar.VERTICAL ||
                    scrollbar.getComponentOrientation().isLeftToRight()) {
                    scrollbar.setValue(model.getMaximum() - model.getExtent());
                } else {
                    scrollbar.setValue(model.getMinimum());
                }
            }
            else {
                float valueMax = model.getMaximum() - model.getExtent();
                float valueRange = valueMax - model.getMinimum();
                float thumbValue = thumbPos - thumbMin;
                float thumbRange = thumbMax - thumbMin;
                int value;
                if (scrollbar.getOrientation() == JScrollBar.VERTICAL ||
                    scrollbar.getComponentOrientation().isLeftToRight()) {
                    value = (int)(0.5 + ((thumbValue / thumbRange) * valueRange));
                } else {
                    value = (int)(0.5 + (((thumbMax - thumbPos) / thumbRange) * valueRange));
                }
                useCachedValue = true;
                scrollBarValue = value + model.getMinimum();
                scrollbar.setValue(adjustValueIfNecessary(scrollBarValue));
            }
            setThumbRollover(active);
        }
        private int adjustValueIfNecessary(int value) {
            if (scrollbar.getParent() instanceof JScrollPane) {
                JScrollPane scrollpane = (JScrollPane)scrollbar.getParent();
                JViewport viewport = scrollpane.getViewport();
                Component view = viewport.getView();
                if (view instanceof JList) {
                    JList list = (JList)view;
                    if (DefaultLookup.getBoolean(list, list.getUI(),
                                                 "List.lockToPositionOnScroll", false)) {
                        int adjustedValue = value;
                        int mode = list.getLayoutOrientation();
                        int orientation = scrollbar.getOrientation();
                        if (orientation == JScrollBar.VERTICAL && mode == JList.VERTICAL) {
                            int index = list.locationToIndex(new Point(0, value));
                            Rectangle rect = list.getCellBounds(index, index);
                            if (rect != null) {
                                adjustedValue = rect.y;
                            }
                        }
                        if (orientation == JScrollBar.HORIZONTAL &&
                            (mode == JList.VERTICAL_WRAP || mode == JList.HORIZONTAL_WRAP)) {
                            if (scrollpane.getComponentOrientation().isLeftToRight()) {
                                int index = list.locationToIndex(new Point(value, 0));
                                Rectangle rect = list.getCellBounds(index, index);
                                if (rect != null) {
                                    adjustedValue = rect.x;
                                }
                            }
                            else {
                                Point loc = new Point(value, 0);
                                int extent = viewport.getExtentSize().width;
                                loc.x += extent - 1;
                                int index = list.locationToIndex(loc);
                                Rectangle rect = list.getCellBounds(index, index);
                                if (rect != null) {
                                    adjustedValue = rect.x + rect.width - extent;
                                }
                            }
                        }
                        value = adjustedValue;
                    }
                }
            }
            return value;
        }
        private void startScrollTimerIfNecessary() {
            if (scrollTimer.isRunning()) {
                return;
            }
            Rectangle tb = getThumbBounds();
            switch (scrollbar.getOrientation()) {
            case JScrollBar.VERTICAL:
                if (direction > 0) {
                    if (tb.y + tb.height < trackListener.currentMouseY) {
                        scrollTimer.start();
                    }
                } else if (tb.y > trackListener.currentMouseY) {
                    scrollTimer.start();
                }
                break;
            case JScrollBar.HORIZONTAL:
                if ((direction > 0 && isMouseAfterThumb())
                        || (direction < 0 && isMouseBeforeThumb())) {
                    scrollTimer.start();
                }
                break;
            }
        }
        public void mouseMoved(MouseEvent e) {
            if (!isDragging) {
                updateThumbState(e.getX(), e.getY());
            }
        }
        public void mouseExited(MouseEvent e) {
            if (!isDragging) {
                setThumbRollover(false);
            }
        }
    }
    protected class ArrowButtonListener extends MouseAdapter
    {
        boolean handledEvent;
        public void mousePressed(MouseEvent e)          {
            if(!scrollbar.isEnabled()) { return; }
            if( ! SwingUtilities.isLeftMouseButton(e)) { return; }
            int direction = (e.getSource() == incrButton) ? 1 : -1;
            scrollByUnit(direction);
            scrollTimer.stop();
            scrollListener.setDirection(direction);
            scrollListener.setScrollByBlock(false);
            scrollTimer.start();
            handledEvent = true;
            if (!scrollbar.hasFocus() && scrollbar.isRequestFocusEnabled()) {
                scrollbar.requestFocus();
            }
        }
        public void mouseReleased(MouseEvent e)         {
            scrollTimer.stop();
            handledEvent = false;
            scrollbar.setValueIsAdjusting(false);
        }
    }
    protected class ScrollListener implements ActionListener
    {
        int direction = +1;
        boolean useBlockIncrement;
        public ScrollListener() {
            direction = +1;
            useBlockIncrement = false;
        }
        public ScrollListener(int dir, boolean block)   {
            direction = dir;
            useBlockIncrement = block;
        }
        public void setDirection(int direction) { this.direction = direction; }
        public void setScrollByBlock(boolean block) { this.useBlockIncrement = block; }
        public void actionPerformed(ActionEvent e) {
            if(useBlockIncrement)       {
                scrollByBlock(direction);
                if(scrollbar.getOrientation() == JScrollBar.VERTICAL)   {
                    if(direction > 0)   {
                        if(getThumbBounds().y + getThumbBounds().height
                                >= trackListener.currentMouseY)
                                    ((Timer)e.getSource()).stop();
                    } else if(getThumbBounds().y <= trackListener.currentMouseY)        {
                        ((Timer)e.getSource()).stop();
                    }
                } else {
                    if ((direction > 0 && !isMouseAfterThumb())
                           || (direction < 0 && !isMouseBeforeThumb())) {
                       ((Timer)e.getSource()).stop();
                    }
                }
            } else {
                scrollByUnit(direction);
            }
            if(direction > 0
                && scrollbar.getValue()+scrollbar.getVisibleAmount()
                        >= scrollbar.getMaximum())
                ((Timer)e.getSource()).stop();
            else if(direction < 0
                && scrollbar.getValue() <= scrollbar.getMinimum())
                ((Timer)e.getSource()).stop();
        }
    }
    private boolean isMouseLeftOfThumb() {
        return trackListener.currentMouseX < getThumbBounds().x;
    }
    private boolean isMouseRightOfThumb() {
        Rectangle tb = getThumbBounds();
        return trackListener.currentMouseX > tb.x + tb.width;
    }
    private boolean isMouseBeforeThumb() {
        return scrollbar.getComponentOrientation().isLeftToRight()
            ? isMouseLeftOfThumb()
            : isMouseRightOfThumb();
    }
    private boolean isMouseAfterThumb() {
        return scrollbar.getComponentOrientation().isLeftToRight()
            ? isMouseRightOfThumb()
            : isMouseLeftOfThumb();
    }
    private void updateButtonDirections() {
        int orient = scrollbar.getOrientation();
        if (scrollbar.getComponentOrientation().isLeftToRight()) {
            if (incrButton instanceof BasicArrowButton) {
                ((BasicArrowButton)incrButton).setDirection(
                        orient == HORIZONTAL? EAST : SOUTH);
            }
            if (decrButton instanceof BasicArrowButton) {
                ((BasicArrowButton)decrButton).setDirection(
                        orient == HORIZONTAL? WEST : NORTH);
            }
        }
        else {
            if (incrButton instanceof BasicArrowButton) {
                ((BasicArrowButton)incrButton).setDirection(
                        orient == HORIZONTAL? WEST : SOUTH);
            }
            if (decrButton instanceof BasicArrowButton) {
                ((BasicArrowButton)decrButton).setDirection(
                        orient == HORIZONTAL ? EAST : NORTH);
            }
        }
    }
    public class PropertyChangeHandler implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent e) {
            getHandler().propertyChange(e);
        }
    }
    private static class Actions extends UIAction {
        private static final String POSITIVE_UNIT_INCREMENT =
                                    "positiveUnitIncrement";
        private static final String POSITIVE_BLOCK_INCREMENT =
                                    "positiveBlockIncrement";
        private static final String NEGATIVE_UNIT_INCREMENT =
                                    "negativeUnitIncrement";
        private static final String NEGATIVE_BLOCK_INCREMENT =
                                    "negativeBlockIncrement";
        private static final String MIN_SCROLL = "minScroll";
        private static final String MAX_SCROLL = "maxScroll";
        Actions(String name) {
            super(name);
        }
        public void actionPerformed(ActionEvent e) {
            JScrollBar scrollBar = (JScrollBar)e.getSource();
            String key = getName();
            if (key == POSITIVE_UNIT_INCREMENT) {
                scroll(scrollBar, POSITIVE_SCROLL, false);
            }
            else if (key == POSITIVE_BLOCK_INCREMENT) {
                scroll(scrollBar, POSITIVE_SCROLL, true);
            }
            else if (key == NEGATIVE_UNIT_INCREMENT) {
                scroll(scrollBar, NEGATIVE_SCROLL, false);
            }
            else if (key == NEGATIVE_BLOCK_INCREMENT) {
                scroll(scrollBar, NEGATIVE_SCROLL, true);
            }
            else if (key == MIN_SCROLL) {
                scroll(scrollBar, BasicScrollBarUI.MIN_SCROLL, true);
            }
            else if (key == MAX_SCROLL) {
                scroll(scrollBar, BasicScrollBarUI.MAX_SCROLL, true);
            }
        }
        private void scroll(JScrollBar scrollBar, int dir, boolean block) {
            if (dir == NEGATIVE_SCROLL || dir == POSITIVE_SCROLL) {
                int amount;
                if (block) {
                    if (dir == NEGATIVE_SCROLL) {
                        amount = -1 * scrollBar.getBlockIncrement(-1);
                    }
                    else {
                        amount = scrollBar.getBlockIncrement(1);
                    }
                }
                else {
                    if (dir == NEGATIVE_SCROLL) {
                        amount = -1 * scrollBar.getUnitIncrement(-1);
                    }
                    else {
                        amount = scrollBar.getUnitIncrement(1);
                    }
                }
                scrollBar.setValue(scrollBar.getValue() + amount);
            }
            else if (dir == BasicScrollBarUI.MIN_SCROLL) {
                scrollBar.setValue(scrollBar.getMinimum());
            }
            else if (dir == BasicScrollBarUI.MAX_SCROLL) {
                scrollBar.setValue(scrollBar.getMaximum());
            }
        }
    }
    private class Handler implements FocusListener, PropertyChangeListener {
        public void focusGained(FocusEvent e) {
            scrollbar.repaint();
        }
        public void focusLost(FocusEvent e) {
            scrollbar.repaint();
        }
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if ("model" == propertyName) {
                BoundedRangeModel oldModel = (BoundedRangeModel)e.getOldValue();
                BoundedRangeModel newModel = (BoundedRangeModel)e.getNewValue();
                oldModel.removeChangeListener(modelListener);
                newModel.addChangeListener(modelListener);
                scrollBarValue = scrollbar.getValue();
                scrollbar.repaint();
                scrollbar.revalidate();
            } else if ("orientation" == propertyName) {
                updateButtonDirections();
            } else if ("componentOrientation" == propertyName) {
                updateButtonDirections();
                InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
                SwingUtilities.replaceUIInputMap(scrollbar, JComponent.WHEN_FOCUSED, inputMap);
            }
        }
    }
}
