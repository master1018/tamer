public class SynthToolBarUI extends BasicToolBarUI
                            implements PropertyChangeListener, SynthUI {
    private Icon handleIcon = null;
    private Rectangle contentRect = new Rectangle();
    private SynthStyle style;
    private SynthStyle contentStyle;
    private SynthStyle dragWindowStyle;
    public static ComponentUI createUI(JComponent c) {
        return new SynthToolBarUI();
    }
    @Override
    protected void installDefaults() {
        toolBar.setLayout(createLayout());
        updateStyle(toolBar);
    }
    @Override
    protected void installListeners() {
        super.installListeners();
        toolBar.addPropertyChangeListener(this);
    }
    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        toolBar.removePropertyChangeListener(this);
    }
    private void updateStyle(JToolBar c) {
        SynthContext context = getContext(
                c, Region.TOOL_BAR_CONTENT, null, ENABLED);
        contentStyle = SynthLookAndFeel.updateStyle(context, this);
        context.dispose();
        context = getContext(c, Region.TOOL_BAR_DRAG_WINDOW, null, ENABLED);
        dragWindowStyle = SynthLookAndFeel.updateStyle(context, this);
        context.dispose();
        context = getContext(c, ENABLED);
        SynthStyle oldStyle = style;
        style = SynthLookAndFeel.updateStyle(context, this);
        if (oldStyle != style) {
            handleIcon =
                style.getIcon(context, "ToolBar.handleIcon");
            if (oldStyle != null) {
                uninstallKeyboardActions();
                installKeyboardActions();
            }
        }
        context.dispose();
    }
    @Override
    protected void uninstallDefaults() {
        SynthContext context = getContext(toolBar, ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style = null;
        handleIcon = null;
        context = getContext(toolBar, Region.TOOL_BAR_CONTENT,
                             contentStyle, ENABLED);
        contentStyle.uninstallDefaults(context);
        context.dispose();
        contentStyle = null;
        context = getContext(toolBar, Region.TOOL_BAR_DRAG_WINDOW,
                             dragWindowStyle, ENABLED);
        dragWindowStyle.uninstallDefaults(context);
        context.dispose();
        dragWindowStyle = null;
        toolBar.setLayout(null);
    }
    @Override
    protected void installComponents() {}
    @Override
    protected void uninstallComponents() {}
    protected LayoutManager createLayout() {
        return new SynthToolBarLayoutManager();
    }
    @Override
    public SynthContext getContext(JComponent c) {
        return getContext(c, SynthLookAndFeel.getComponentState(c));
    }
    private SynthContext getContext(JComponent c, int state) {
        return SynthContext.getContext(SynthContext.class, c,
                    SynthLookAndFeel.getRegion(c), style, state);
    }
    private SynthContext getContext(JComponent c, Region region, SynthStyle style) {
        return SynthContext.getContext(SynthContext.class, c, region,
                                       style, getComponentState(c, region));
    }
    private SynthContext getContext(JComponent c, Region region,
                                    SynthStyle style, int state) {
        return SynthContext.getContext(SynthContext.class, c, region,
                                       style, state);
    }
    private int getComponentState(JComponent c, Region region) {
        return SynthLookAndFeel.getComponentState(c);
    }
    @Override
    public void update(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        SynthLookAndFeel.update(context, g);
        context.getPainter().paintToolBarBackground(context,
                          g, 0, 0, c.getWidth(), c.getHeight(),
                          toolBar.getOrientation());
        paint(context, g);
        context.dispose();
    }
    @Override
    public void paint(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        paint(context, g);
        context.dispose();
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintToolBarBorder(context, g, x, y, w, h,
                                                toolBar.getOrientation());
    }
    @Override
    protected void setBorderToNonRollover(Component c) {}
    @Override
    protected void setBorderToRollover(Component c) {}
    @Override
    protected void setBorderToNormal(Component c) {}
    protected void paint(SynthContext context, Graphics g) {
        if (handleIcon != null && toolBar.isFloatable()) {
            int startX = toolBar.getComponentOrientation().isLeftToRight() ?
                0 : toolBar.getWidth() -
                    SynthIcon.getIconWidth(handleIcon, context);
            SynthIcon.paintIcon(handleIcon, context, g, startX, 0,
                    SynthIcon.getIconWidth(handleIcon, context),
                    SynthIcon.getIconHeight(handleIcon, context));
        }
        SynthContext subcontext = getContext(
                toolBar, Region.TOOL_BAR_CONTENT, contentStyle);
        paintContent(subcontext, g, contentRect);
        subcontext.dispose();
    }
    protected void paintContent(SynthContext context, Graphics g,
            Rectangle bounds) {
        SynthLookAndFeel.updateSubregion(context, g, bounds);
        context.getPainter().paintToolBarContentBackground(context, g,
                             bounds.x, bounds.y, bounds.width, bounds.height,
                             toolBar.getOrientation());
        context.getPainter().paintToolBarContentBorder(context, g,
                             bounds.x, bounds.y, bounds.width, bounds.height,
                             toolBar.getOrientation());
    }
    @Override
    protected void paintDragWindow(Graphics g) {
        int w = dragWindow.getWidth();
        int h = dragWindow.getHeight();
        SynthContext context = getContext(
                toolBar, Region.TOOL_BAR_DRAG_WINDOW, dragWindowStyle);
        SynthLookAndFeel.updateSubregion(
                context, g, new Rectangle(0, 0, w, h));
        context.getPainter().paintToolBarDragWindowBackground(context,
                                                           g, 0, 0, w, h,
                                                           dragWindow.getOrientation());
        context.getPainter().paintToolBarDragWindowBorder(context, g, 0, 0, w, h,
                                                          dragWindow.getOrientation());
        context.dispose();
    }
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (SynthLookAndFeel.shouldUpdateStyle(e)) {
            updateStyle((JToolBar)e.getSource());
        }
    }
    class SynthToolBarLayoutManager implements LayoutManager {
        public void addLayoutComponent(String name, Component comp) {}
        public void removeLayoutComponent(Component comp) {}
        public Dimension minimumLayoutSize(Container parent) {
            JToolBar tb = (JToolBar)parent;
            Insets insets = tb.getInsets();
            Dimension dim = new Dimension();
            SynthContext context = getContext(tb);
            if (tb.getOrientation() == JToolBar.HORIZONTAL) {
                dim.width = tb.isFloatable() ?
                    SynthIcon.getIconWidth(handleIcon, context) : 0;
                Dimension compDim;
                for (int i = 0; i < tb.getComponentCount(); i++) {
                    Component component = tb.getComponent(i);
                    if (component.isVisible()) {
                        compDim = component.getMinimumSize();
                        dim.width += compDim.width;
                        dim.height = Math.max(dim.height, compDim.height);
                    }
                }
            } else {
                dim.height = tb.isFloatable() ?
                    SynthIcon.getIconHeight(handleIcon, context) : 0;
                Dimension compDim;
                for (int i = 0; i < tb.getComponentCount(); i++) {
                    Component component = tb.getComponent(i);
                    if (component.isVisible()) {
                        compDim = component.getMinimumSize();
                        dim.width = Math.max(dim.width, compDim.width);
                        dim.height += compDim.height;
                    }
                }
            }
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;
            context.dispose();
            return dim;
        }
        public Dimension preferredLayoutSize(Container parent) {
            JToolBar tb = (JToolBar)parent;
            Insets insets = tb.getInsets();
            Dimension dim = new Dimension();
            SynthContext context = getContext(tb);
            if (tb.getOrientation() == JToolBar.HORIZONTAL) {
                dim.width = tb.isFloatable() ?
                    SynthIcon.getIconWidth(handleIcon, context) : 0;
                Dimension compDim;
                for (int i = 0; i < tb.getComponentCount(); i++) {
                    Component component = tb.getComponent(i);
                    if (component.isVisible()) {
                        compDim = component.getPreferredSize();
                        dim.width += compDim.width;
                        dim.height = Math.max(dim.height, compDim.height);
                    }
                }
            } else {
                dim.height = tb.isFloatable() ?
                    SynthIcon.getIconHeight(handleIcon, context) : 0;
                Dimension compDim;
                for (int i = 0; i < tb.getComponentCount(); i++) {
                    Component component = tb.getComponent(i);
                    if (component.isVisible()) {
                        compDim = component.getPreferredSize();
                        dim.width = Math.max(dim.width, compDim.width);
                        dim.height += compDim.height;
                    }
                }
            }
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;
            context.dispose();
            return dim;
        }
        public void layoutContainer(Container parent) {
            JToolBar tb = (JToolBar)parent;
            Insets insets = tb.getInsets();
            boolean ltr = tb.getComponentOrientation().isLeftToRight();
            SynthContext context = getContext(tb);
            Component c;
            Dimension d;
            int glueCount = 0;
            for (int i=0; i<tb.getComponentCount(); i++) {
                if (isGlue(tb.getComponent(i))) glueCount++;
            }
            if (tb.getOrientation() == JToolBar.HORIZONTAL) {
                int handleWidth = tb.isFloatable() ?
                    SynthIcon.getIconWidth(handleIcon, context) : 0;
                contentRect.x = ltr ? handleWidth : 0;
                contentRect.y = 0;
                contentRect.width = tb.getWidth() - handleWidth;
                contentRect.height = tb.getHeight();
                int x = ltr ?
                    handleWidth + insets.left :
                    tb.getWidth() - handleWidth - insets.right;
                int baseY = insets.top;
                int baseH = tb.getHeight() - insets.top - insets.bottom;
                int extraSpacePerGlue = 0;
                if (glueCount > 0) {
                    int minWidth = minimumLayoutSize(parent).width;
                    extraSpacePerGlue = (tb.getWidth() - minWidth) / glueCount;
                    if (extraSpacePerGlue < 0) extraSpacePerGlue = 0;
                }
                for (int i = 0; i < tb.getComponentCount(); i++) {
                    c = tb.getComponent(i);
                    if (c.isVisible()) {
                        d = c.getPreferredSize();
                        int y, h;
                        if (d.height >= baseH || c instanceof JSeparator) {
                            y = baseY;
                            h = baseH;
                        } else {
                            y = baseY + (baseH / 2) - (d.height / 2);
                            h = d.height;
                        }
                        if (isGlue(c)) d.width += extraSpacePerGlue;
                        c.setBounds(ltr ? x : x - d.width, y, d.width, h);
                        x = ltr ? x + d.width : x - d.width;
                    }
                }
            } else {
                int handleHeight = tb.isFloatable() ?
                    SynthIcon.getIconHeight(handleIcon, context) : 0;
                contentRect.x = 0;
                contentRect.y = handleHeight;
                contentRect.width = tb.getWidth();
                contentRect.height = tb.getHeight() - handleHeight;
                int baseX = insets.left;
                int baseW = tb.getWidth() - insets.left - insets.right;
                int y = handleHeight + insets.top;
                int extraSpacePerGlue = 0;
                if (glueCount > 0) {
                    int minHeight = minimumLayoutSize(parent).height;
                    extraSpacePerGlue = (tb.getHeight() - minHeight) / glueCount;
                    if (extraSpacePerGlue < 0) extraSpacePerGlue = 0;
                }
                for (int i = 0; i < tb.getComponentCount(); i++) {
                    c = tb.getComponent(i);
                    if (c.isVisible()) {
                        d = c.getPreferredSize();
                        int x, w;
                        if (d.width >= baseW || c instanceof JSeparator) {
                            x = baseX;
                            w = baseW;
                        } else {
                            x = baseX + (baseW / 2) - (d.width / 2);
                            w = d.width;
                        }
                        if (isGlue(c)) d.height += extraSpacePerGlue;
                        c.setBounds(x, y, w, d.height);
                        y += d.height;
                    }
                }
            }
            context.dispose();
        }
        private boolean isGlue(Component c) {
            if (c.isVisible() && c instanceof Box.Filler) {
                Box.Filler f = (Box.Filler)c;
                Dimension min = f.getMinimumSize();
                Dimension pref = f.getPreferredSize();
                return min.width == 0 &&  min.height == 0 &&
                        pref.width == 0 && pref.height == 0;
            }
            return false;
        }
    }
}
