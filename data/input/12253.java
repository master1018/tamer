class GTKPainter extends SynthPainter {
    private static final PositionType[] POSITIONS = {
        PositionType.BOTTOM, PositionType.RIGHT,
        PositionType.TOP, PositionType.LEFT
    };
    private static final ShadowType SHADOWS[] = {
        ShadowType.NONE, ShadowType.IN, ShadowType.OUT,
        ShadowType.ETCHED_IN, ShadowType.OUT
    };
    private final static GTKEngine ENGINE = GTKEngine.INSTANCE;
    final static GTKPainter INSTANCE = new GTKPainter();
    private GTKPainter() {
    }
    private String getName(SynthContext context) {
        return (context.getRegion().isSubregion()) ? null :
               context.getComponent().getName();
    }
    public void paintCheckBoxBackground(SynthContext context,
            Graphics g, int x, int y, int w, int h) {
        paintRadioButtonBackground(context, g, x, y, w, h);
    }
    public void paintCheckBoxMenuItemBackground(SynthContext context,
            Graphics g, int x, int y, int w, int h) {
        paintRadioButtonMenuItemBackground(context, g, x, y, w, h);
    }
    public void paintFormattedTextFieldBackground(SynthContext context,
                                          Graphics g, int x, int y,
                                          int w, int h) {
        paintTextBackground(context, g, x, y, w, h);
    }
    public void paintToolBarDragWindowBackground(SynthContext context,
                                     Graphics g, int x, int y,
                                     int w, int h) {
        paintToolBarBackground(context, g, x, y, w, h);
    }
    public void paintToolBarBackground(SynthContext context,
                                     Graphics g, int x, int y,
                                     int w, int h) {
        Region id = context.getRegion();
        int state = context.getComponentState();
        int gtkState = GTKLookAndFeel.synthStateToGTKState(id, state);
        int orientation = ((JToolBar)context.getComponent()).getOrientation();
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h, id,
                                          state, orientation))
            {
                ENGINE.startPainting(g, x, y, w, h, id, state, orientation);
                ENGINE.paintBox(g, context, id, gtkState, ShadowType.OUT,
                                "handlebox_bin", x, y, w, h);
                ENGINE.finishPainting();
            }
        }
    }
    public void paintToolBarContentBackground(SynthContext context,
                                              Graphics g,
                                              int x, int y, int w, int h) {
        Region id = context.getRegion();
        int orientation = ((JToolBar)context.getComponent()).getOrientation();
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h, id, orientation)) {
                ENGINE.startPainting(g, x, y, w, h, id, orientation);
                ENGINE.paintBox(g, context, id, SynthConstants.ENABLED,
                                ShadowType.OUT, "toolbar", x, y, w, h);
                ENGINE.finishPainting();
            }
        }
    }
    public void paintPasswordFieldBackground(SynthContext context,
                                     Graphics g, int x, int y,
                                     int w, int h) {
        paintTextBackground(context, g, x, y, w, h);
    }
    public void paintTextFieldBackground(SynthContext context, Graphics g,
                                         int x, int y, int w, int h) {
        if (getName(context) == "Tree.cellEditor") {
            paintTreeCellEditorBackground(context, g, x, y, w, h);
        } else {
            paintTextBackground(context, g, x, y, w, h);
        }
    }
    public void paintRadioButtonBackground(SynthContext context,
                                     Graphics g, int x, int y,
                                     int w, int h) {
        Region id = context.getRegion();
        int gtkState = GTKLookAndFeel.synthStateToGTKState(
                id, context.getComponentState());
        if (gtkState == SynthConstants.MOUSE_OVER) {
            synchronized (UNIXToolkit.GTK_LOCK) {
                if (! ENGINE.paintCachedImage(g, x, y, w, h, id)) {
                    ENGINE.startPainting(g, x, y, w, h, id);
                    ENGINE.paintFlatBox(g, context, id,
                            SynthConstants.MOUSE_OVER, ShadowType.ETCHED_OUT,
                            "checkbutton", x, y, w, h, ColorType.BACKGROUND);
                    ENGINE.finishPainting();
                }
            }
        }
    }
    public void paintRadioButtonMenuItemBackground(SynthContext context,
                                     Graphics g, int x, int y,
                                     int w, int h) {
        Region id = context.getRegion();
        int gtkState = GTKLookAndFeel.synthStateToGTKState(
                id, context.getComponentState());
        if (gtkState == SynthConstants.MOUSE_OVER) {
            synchronized (UNIXToolkit.GTK_LOCK) {
                if (! ENGINE.paintCachedImage(g, x, y, w, h, id)) {
                    ShadowType shadow = (GTKLookAndFeel.is2_2() ?
                        ShadowType.NONE : ShadowType.OUT);
                    ENGINE.startPainting(g, x, y, w, h, id);
                    ENGINE.paintBox(g, context, id, gtkState,
                            shadow, "menuitem", x, y, w, h);
                    ENGINE.finishPainting();
                }
            }
        }
    }
    public void paintLabelBackground(SynthContext context,
                                     Graphics g, int x, int y,
                                     int w, int h) {
        String name = getName(context);
        JComponent c = context.getComponent();
        Container  container = c.getParent();
        if (name == "TableHeader.renderer" ||
            name == "GTKFileChooser.directoryListLabel" ||
            name == "GTKFileChooser.fileListLabel") {
            paintButtonBackgroundImpl(context, g, Region.BUTTON, "button",
                    x, y, w, h, true, false, false, false);
        }
        else if (c instanceof ListCellRenderer &&
                 container != null &&
                 container.getParent() instanceof JComboBox ) {
            paintTextBackground(context, g, x, y, w, h);
        }
    }
    public void paintInternalFrameBorder(SynthContext context,
                                      Graphics g, int x, int y,
                                      int w, int h) {
        Metacity.INSTANCE.paintFrameBorder(context, g, x, y, w, h);
    }
    public void paintDesktopPaneBackground(SynthContext context,
                                           Graphics g, int x, int y,
                                           int w, int h) {
        fillArea(context, g, x, y, w, h, ColorType.BACKGROUND);
    }
    public void paintDesktopIconBorder(SynthContext context,
                                           Graphics g, int x, int y,
                                           int w, int h) {
        Metacity.INSTANCE.paintFrameBorder(context, g, x, y, w, h);
    }
    public void paintButtonBackground(SynthContext context, Graphics g,
                                      int x, int y, int w, int h) {
        String name = getName(context);
        if (name != null && name.startsWith("InternalFrameTitlePane.")) {
            Metacity.INSTANCE.paintButtonBackground(context, g, x, y, w, h);
        } else {
            AbstractButton button = (AbstractButton)context.getComponent();
            boolean paintBG = button.isContentAreaFilled() &&
                              button.isBorderPainted();
            boolean paintFocus = button.isFocusPainted();
            boolean defaultCapable = (button instanceof JButton) &&
                    ((JButton)button).isDefaultCapable();
            boolean toolButton = (button.getParent() instanceof JToolBar);
            paintButtonBackgroundImpl(context, g, Region.BUTTON, "button",
                    x, y, w, h, paintBG, paintFocus, defaultCapable, toolButton);
        }
    }
    private void paintButtonBackgroundImpl(SynthContext context, Graphics g,
            Region id, String detail, int x, int y, int w, int h,
            boolean paintBackground, boolean paintFocus,
            boolean defaultCapable, boolean toolButton) {
        int state = context.getComponentState();
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (ENGINE.paintCachedImage(g, x, y, w, h, id, state, detail,
                    paintBackground, paintFocus, defaultCapable, toolButton)) {
                return;
            }
            ENGINE.startPainting(g, x, y, w, h, id, state, detail,
                paintBackground, paintFocus, defaultCapable, toolButton);
            GTKStyle style = (GTKStyle)context.getStyle();
            if (defaultCapable && !toolButton) {
                Insets defaultInsets = style.getClassSpecificInsetsValue(
                        context, "default-border",
                        GTKStyle.BUTTON_DEFAULT_BORDER_INSETS);
                if (paintBackground && (state & SynthConstants.DEFAULT) != 0) {
                    ENGINE.paintBox(g, context, id, SynthConstants.ENABLED,
                            ShadowType.IN, "buttondefault", x, y, w, h);
                }
                x += defaultInsets.left;
                y += defaultInsets.top;
                w -= (defaultInsets.left + defaultInsets.right);
                h -= (defaultInsets.top + defaultInsets.bottom);
            }
            boolean interiorFocus = style.getClassSpecificBoolValue(
                    context, "interior-focus", true);
            int focusSize = style.getClassSpecificIntValue(
                    context, "focus-line-width",1);
            int focusPad = style.getClassSpecificIntValue(
                    context, "focus-padding", 1);
            int totalFocusSize = focusSize + focusPad;
            int xThickness = style.getXThickness();
            int yThickness = style.getYThickness();
            if (!interiorFocus &&
                    (state & SynthConstants.FOCUSED) == SynthConstants.FOCUSED) {
                x += totalFocusSize;
                y += totalFocusSize;
                w -= 2 * totalFocusSize;
                h -= 2 * totalFocusSize;
            }
            int gtkState = GTKLookAndFeel.synthStateToGTKState(id, state);
            boolean paintBg;
            if (toolButton) {
                paintBg =
                    (gtkState != SynthConstants.ENABLED) &&
                    (gtkState != SynthConstants.DISABLED);
            } else {
                paintBg =
                    paintBackground ||
                    (gtkState != SynthConstants.ENABLED);
            }
            if (paintBg) {
                ShadowType shadowType = ShadowType.OUT;
                if ((state & (SynthConstants.PRESSED |
                              SynthConstants.SELECTED)) != 0) {
                    shadowType = ShadowType.IN;
                }
                ENGINE.paintBox(g, context, id, gtkState,
                        shadowType, detail, x, y, w, h);
            }
            if (paintFocus && (state & SynthConstants.FOCUSED) != 0) {
                if (interiorFocus) {
                    x += xThickness + focusPad;
                    y += yThickness + focusPad;
                    w -= 2 * (xThickness + focusPad);
                    h -= 2 * (yThickness + focusPad);
                } else {
                    x -= totalFocusSize;
                    y -= totalFocusSize;
                    w += 2 * totalFocusSize;
                    h += 2 * totalFocusSize;
                }
                ENGINE.paintFocus(g, context, id, gtkState, detail, x, y, w, h);
            }
            ENGINE.finishPainting();
        }
    }
    public void paintArrowButtonForeground(SynthContext context, Graphics g,
                                           int x, int y, int w, int h,
                                           int direction) {
        Region id = context.getRegion();
        Component c = context.getComponent();
        String name = c.getName();
        ArrowType arrowType = null;
        switch (direction) {
            case SwingConstants.NORTH:
                arrowType = ArrowType.UP; break;
            case SwingConstants.SOUTH:
                arrowType = ArrowType.DOWN; break;
            case SwingConstants.EAST:
                arrowType = ArrowType.RIGHT; break;
            case SwingConstants.WEST:
                arrowType = ArrowType.LEFT; break;
        }
        String detail = "arrow";
        if ((name == "ScrollBar.button") || (name == "TabbedPane.button")) {
            if (arrowType == ArrowType.UP || arrowType == ArrowType.DOWN) {
                detail = "vscrollbar";
            } else {
                detail = "hscrollbar";
            }
        } else if (name == "Spinner.nextButton" ||
                   name == "Spinner.previousButton") {
            detail = "spinbutton";
        } else if (name != "ComboBox.arrowButton") {
            assert false : "unexpected name: " + name;
        }
        int gtkState = GTKLookAndFeel.synthStateToGTKState(
                id, context.getComponentState());
        ShadowType shadowType = (gtkState == SynthConstants.PRESSED ?
            ShadowType.IN : ShadowType.OUT);
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (ENGINE.paintCachedImage(g, x, y, w, h,
                    gtkState, name, direction)) {
                return;
            }
            ENGINE.startPainting(g, x, y, w, h, gtkState, name, direction);
            ENGINE.paintArrow(g, context, id, gtkState,
                    shadowType, arrowType, detail, x, y, w, h);
            ENGINE.finishPainting();
        }
    }
    public void paintArrowButtonBackground(SynthContext context,
            Graphics g, int x, int y, int w, int h) {
        Region id = context.getRegion();
        AbstractButton button = (AbstractButton)context.getComponent();
        String name = button.getName();
        String detail = "button";
        int direction = SwingConstants.CENTER;
        if ((name == "ScrollBar.button") || (name == "TabbedPane.button")) {
            Integer prop = (Integer)
                button.getClientProperty("__arrow_direction__");
            direction = (prop != null) ?
                prop.intValue() : SwingConstants.WEST;
            switch (direction) {
            default:
            case SwingConstants.EAST:
            case SwingConstants.WEST:
                detail = "hscrollbar";
                break;
            case SwingConstants.NORTH:
            case SwingConstants.SOUTH:
                detail = "vscrollbar";
                break;
            }
        } else if (name == "Spinner.previousButton") {
            detail = "spinbutton_down";
        } else if (name == "Spinner.nextButton") {
            detail = "spinbutton_up";
        } else if (name != "ComboBox.arrowButton") {
            assert false : "unexpected name: " + name;
        }
        int state = context.getComponentState();
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (ENGINE.paintCachedImage(g, x, y, w, h, id,
                                        state, detail, direction))
            {
                return;
            }
            ENGINE.startPainting(g, x, y, w, h, id,
                                 state, detail, direction);
            if (detail.startsWith("spin")) {
                int spinState = button.getParent().isEnabled() ?
                    SynthConstants.ENABLED : SynthConstants.DISABLED;
                int mody = (detail == "spinbutton_up") ? y : y-h;
                int modh = h*2;
                ENGINE.paintBox(g, context, id, spinState,
                                ShadowType.IN, "spinbutton",
                                x, mody, w, modh);
            }
            int gtkState = GTKLookAndFeel.synthStateToGTKState(id, state);
            ShadowType shadowType = ShadowType.OUT;
            if ((gtkState & (SynthConstants.PRESSED |
                             SynthConstants.SELECTED)) != 0)
            {
                shadowType = ShadowType.IN;
            }
            ENGINE.paintBox(g, context, id, gtkState,
                            shadowType, detail,
                            x, y, w, h);
            ENGINE.finishPainting();
        }
    }
    public void paintListBackground(SynthContext context, Graphics g,
                                    int x, int y, int w, int h) {
        fillArea(context, g, x, y, w, h, GTKColorType.TEXT_BACKGROUND);
    }
    public void paintMenuBarBackground(SynthContext context, Graphics g,
                                       int x, int y, int w, int h) {
        Region id = context.getRegion();
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (ENGINE.paintCachedImage(g, x, y, w, h, id)) {
                return;
            }
            GTKStyle style = (GTKStyle)context.getStyle();
            int shadow = style.getClassSpecificIntValue(
                    context, "shadow-type", 2);
            ShadowType shadowType = SHADOWS[shadow];
            int gtkState = GTKLookAndFeel.synthStateToGTKState(
                    id, context.getComponentState());
            ENGINE.startPainting(g, x, y, w, h, id);
            ENGINE.paintBox(g, context, id, gtkState,
                shadowType, "menubar", x, y, w, h);
            ENGINE.finishPainting();
        }
    }
    public void paintMenuBackground(SynthContext context,
                                     Graphics g,
                                     int x, int y, int w, int h) {
        paintMenuItemBackground(context, g, x, y, w, h);
    }
    public void paintMenuItemBackground(SynthContext context,
                                     Graphics g,
                                     int x, int y, int w, int h) {
        int gtkState = GTKLookAndFeel.synthStateToGTKState(
                context.getRegion(), context.getComponentState());
        if (gtkState == SynthConstants.MOUSE_OVER) {
            Region id = Region.MENU_ITEM;
            synchronized (UNIXToolkit.GTK_LOCK) {
                if (! ENGINE.paintCachedImage(g, x, y, w, h, id)) {
                    ShadowType shadow = (GTKLookAndFeel.is2_2() ?
                        ShadowType.NONE : ShadowType.OUT);
                    ENGINE.startPainting(g, x, y, w, h, id);
                    ENGINE.paintBox(g, context, id, gtkState, shadow,
                            "menuitem", x, y, w, h);
                    ENGINE.finishPainting();
                }
            }
        }
    }
    public void paintPopupMenuBackground(SynthContext context, Graphics g,
                                        int x, int y, int w, int h) {
        Region id = context.getRegion();
        int gtkState = GTKLookAndFeel.synthStateToGTKState(
                id, context.getComponentState());
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (ENGINE.paintCachedImage(g, x, y, w, h, id, gtkState)) {
                return;
            }
            ENGINE.startPainting(g, x, y, w, h, id, gtkState);
            ENGINE.paintBox(g, context, id, gtkState,
                    ShadowType.OUT, "menu", x, y, w, h);
            GTKStyle style = (GTKStyle)context.getStyle();
            int xThickness = style.getXThickness();
            int yThickness = style.getYThickness();
            ENGINE.paintBackground(g, context, id, gtkState,
                    style.getGTKColor(context, gtkState, GTKColorType.BACKGROUND),
                    x + xThickness, y + yThickness,
                    w - xThickness - xThickness, h - yThickness - yThickness);
            ENGINE.finishPainting();
        }
    }
    public void paintProgressBarBackground(SynthContext context,
                                            Graphics g,
                                            int x, int y, int w, int h) {
        Region id = context.getRegion();
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h, id)) {
                ENGINE.startPainting(g, x, y, w, h, id);
                ENGINE.paintBox(g, context, id, SynthConstants.ENABLED,
                        ShadowType.IN, "trough", x, y, w, h);
                ENGINE.finishPainting();
            }
        }
    }
    public void paintProgressBarForeground(SynthContext context, Graphics g,
                                            int x, int y, int w, int h,
                                            int orientation) {
        Region id = context.getRegion();
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (w <= 0 || h <= 0) {
                return;
            }
            ENGINE.startPainting(g, x, y, w, h, id, "fg");
            ENGINE.paintBox(g, context, id, SynthConstants.MOUSE_OVER,
                            ShadowType.OUT, "bar", x, y, w, h);
            ENGINE.finishPainting(false); 
        }
    }
    public void paintViewportBorder(SynthContext context, Graphics g,
                                           int x, int y, int w, int h) {
        Region id = context.getRegion();
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h, id)) {
                ENGINE.startPainting(g, x, y, w, h, id);
                ENGINE.paintShadow(g, context, id, SynthConstants.ENABLED,
                        ShadowType.IN, "scrolled_window", x, y, w, h);
                ENGINE.finishPainting();
            }
        }
    }
    public void paintSeparatorBackground(SynthContext context,
                                          Graphics g,
                                          int x, int y, int w, int h,
                                         int orientation) {
        Region id = context.getRegion();
        int state = context.getComponentState();
        JComponent c = context.getComponent();
        String detail;
        if (c instanceof JToolBar.Separator) {
            detail = "toolbar";
            float pct = 0.2f;
            JToolBar.Separator sep = (JToolBar.Separator)c;
            Dimension size = sep.getSeparatorSize();
            GTKStyle style = (GTKStyle)context.getStyle();
            if (orientation == JSeparator.HORIZONTAL) {
                x += (int)(w * pct);
                w -= (int)(w * pct * 2);
                y += (size.height - style.getYThickness()) / 2;
            } else {
                y += (int)(h * pct);
                h -= (int)(h * pct * 2);
                x += (size.width - style.getXThickness()) / 2;
            }
        } else {
            detail = "separator";
            Insets insets = c.getInsets();
            x += insets.left;
            y += insets.top;
            if (orientation == JSeparator.HORIZONTAL) {
                w -= (insets.left + insets.right);
            } else {
                h -= (insets.top + insets.bottom);
            }
        }
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h, id,
                                          state, detail, orientation)) {
                ENGINE.startPainting(g, x, y, w, h, id,
                                     state, detail, orientation);
                if (orientation == JSeparator.HORIZONTAL) {
                    ENGINE.paintHline(g, context, id, state,
                                      detail, x, y, w, h);
                } else {
                    ENGINE.paintVline(g, context, id, state,
                                      detail, x, y, w, h);
                }
                ENGINE.finishPainting();
            }
        }
    }
    public void paintSliderTrackBackground(SynthContext context,
                                       Graphics g,
                                       int x, int y, int w,int h) {
        Region id = context.getRegion();
        int state = context.getComponentState();
        boolean focused = ((state & SynthConstants.FOCUSED) != 0);
        int focusSize = 0;
        if (focused) {
            GTKStyle style = (GTKStyle)context.getStyle();
            focusSize = style.getClassSpecificIntValue(
                                context, "focus-line-width", 1) +
                        style.getClassSpecificIntValue(
                                context, "focus-padding", 1);
            x -= focusSize;
            y -= focusSize;
            w += focusSize * 2;
            h += focusSize * 2;
        }
        JSlider slider = (JSlider)context.getComponent();
        double value = slider.getValue();
        double min = slider.getMinimum();
        double max = slider.getMaximum();
        double visible = 20; 
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (w <= 0 || h <= 0) {
                return;
            }
            ENGINE.startPainting(g, x, y, w, h, id, state, value);
            int gtkState = GTKLookAndFeel.synthStateToGTKState(id, state);
            ENGINE.setRangeValue(context, id, value, min, max, visible);
            ENGINE.paintBox(g, context, id, gtkState, ShadowType.IN,
                            "trough", x + focusSize, y + focusSize,
                            w - 2 * focusSize, h - 2 * focusSize);
            if (focused) {
                ENGINE.paintFocus(g, context, id, SynthConstants.ENABLED,
                                  "trough", x, y, w, h);
            }
            ENGINE.finishPainting(false); 
        }
    }
    public void paintSliderThumbBackground(SynthContext context,
            Graphics g, int x, int y, int w, int h, int dir) {
        Region id = context.getRegion();
        int gtkState = GTKLookAndFeel.synthStateToGTKState(
                id, context.getComponentState());
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h, id, gtkState, dir)) {
                Orientation orientation = (dir == JSlider.HORIZONTAL ?
                    Orientation.HORIZONTAL : Orientation.VERTICAL);
                String detail = (dir == JSlider.HORIZONTAL ?
                    "hscale" : "vscale");
                ENGINE.startPainting(g, x, y, w, h, id, gtkState, dir);
                ENGINE.paintSlider(g, context, id, gtkState,
                        ShadowType.OUT, detail, x, y, w, h, orientation);
                ENGINE.finishPainting();
            }
        }
    }
    public void paintSpinnerBackground(SynthContext context,
                                        Graphics g,
                                        int x, int y, int w, int h) {
    }
    public void paintSplitPaneDividerBackground(SynthContext context,
                                       Graphics g,
                                       int x, int y, int w, int h) {
        Region id = context.getRegion();
        int gtkState = GTKLookAndFeel.synthStateToGTKState(
                id, context.getComponentState());
        JSplitPane splitPane = (JSplitPane)context.getComponent();
        Orientation orientation =
                (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT ?
                    Orientation.VERTICAL : Orientation.HORIZONTAL);
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h,
                    id, gtkState, orientation)) {
                ENGINE.startPainting(g, x, y, w, h, id, gtkState, orientation);
                ENGINE.paintHandle(g, context, id, gtkState,
                        ShadowType.OUT, "paned", x, y, w, h, orientation);
                ENGINE.finishPainting();
            }
        }
    }
    public void paintSplitPaneDragDivider(SynthContext context,
                                       Graphics g,int x, int y, int w, int h,
                                       int orientation) {
        paintSplitPaneDividerForeground(context, g, x, y, w, h, orientation);
    }
    public void paintTabbedPaneContentBackground(SynthContext context,
                                      Graphics g, int x, int y, int w, int h) {
        JTabbedPane pane = (JTabbedPane)context.getComponent();
        int selectedIndex = pane.getSelectedIndex();
        PositionType placement = GTKLookAndFeel.SwingOrientationConstantToGTK(
                                                        pane.getTabPlacement());
        int gapStart = 0;
        int gapSize = 0;
        if (selectedIndex != -1) {
            Rectangle tabBounds = pane.getBoundsAt(selectedIndex);
            if (placement == PositionType.TOP ||
                placement == PositionType.BOTTOM) {
                gapStart = tabBounds.x - x;
                gapSize = tabBounds.width;
            }
            else {
                gapStart = tabBounds.y - y;
                gapSize = tabBounds.height;
            }
        }
        Region id = context.getRegion();
        int gtkState = GTKLookAndFeel.synthStateToGTKState(
                id, context.getComponentState());
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h,
                    id, gtkState, placement, gapStart, gapSize)) {
                ENGINE.startPainting(g, x, y, w, h,
                        id, gtkState, placement, gapStart, gapSize);
                ENGINE.paintBoxGap(g, context, id, gtkState, ShadowType.OUT,
                        "notebook", x, y, w, h, placement, gapStart, gapSize);
                ENGINE.finishPainting();
            }
        }
    }
    public void paintTabbedPaneTabBackground(SynthContext context,
                                           Graphics g,
                                           int x, int y, int w, int h,
                                           int tabIndex) {
        Region id = context.getRegion();
        int state = context.getComponentState();
        int gtkState = ((state & SynthConstants.SELECTED) != 0 ?
            SynthConstants.ENABLED : SynthConstants.PRESSED);
        JTabbedPane pane = (JTabbedPane)context.getComponent();
        int placement = pane.getTabPlacement();
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h,
                    id, gtkState, placement, tabIndex)) {
                PositionType side = POSITIONS[placement - 1];
                ENGINE.startPainting(g, x, y, w, h,
                        id, gtkState, placement, tabIndex);
                ENGINE.paintExtension(g, context, id, gtkState,
                        ShadowType.OUT, "tab", x, y, w, h, side, tabIndex);
                ENGINE.finishPainting();
            }
        }
    }
    public void paintTextPaneBackground(SynthContext context, Graphics g,
                                        int x, int y, int w, int h) {
        paintTextAreaBackground(context, g, x, y, w, h);
    }
    public void paintEditorPaneBackground(SynthContext context, Graphics g,
                                          int x, int y, int w, int h) {
        paintTextAreaBackground(context, g, x, y, w, h);
    }
    public void paintTextAreaBackground(SynthContext context, Graphics g,
                                        int x, int y, int w, int h) {
        fillArea(context, g, x, y, w, h, GTKColorType.TEXT_BACKGROUND);
    }
    private void paintTextBackground(SynthContext context, Graphics g,
                                     int x, int y, int w, int h) {
        JComponent c = context.getComponent();
        GTKStyle style = (GTKStyle)context.getStyle();
        Region id = context.getRegion();
        int state = context.getComponentState();
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (ENGINE.paintCachedImage(g, x, y, w, h, id, state)) {
                return;
            }
            int gtkState = GTKLookAndFeel.synthStateToGTKState(id, state);
            int focusSize = 0;
            boolean interiorFocus = style.getClassSpecificBoolValue(
                    context, "interior-focus", true);
            if (!interiorFocus && (state & SynthConstants.FOCUSED) != 0) {
                focusSize = style.getClassSpecificIntValue(context,
                        "focus-line-width",1);
                x += focusSize;
                y += focusSize;
                w -= 2 * focusSize;
                h -= 2 * focusSize;
            }
            int xThickness = style.getXThickness();
            int yThickness = style.getYThickness();
            ENGINE.startPainting(g, x, y, w, h, id, state);
            ENGINE.paintShadow(g, context, id, gtkState,
                               ShadowType.IN, "entry", x, y, w, h);
            ENGINE.paintFlatBox(g, context, id,
                                gtkState, ShadowType.NONE, "entry_bg",
                                x + xThickness,
                                y + yThickness,
                                w - (2 * xThickness),
                                h - (2 * yThickness),
                                ColorType.TEXT_BACKGROUND);
            if (focusSize > 0) {
                x -= focusSize;
                y -= focusSize;
                w += 2 * focusSize;
                h += 2 * focusSize;
                ENGINE.paintFocus(g, context, id, gtkState,
                        "entry", x, y, w, h);
            }
            ENGINE.finishPainting();
        }
    }
    private void paintTreeCellEditorBackground(SynthContext context, Graphics g,
                                               int x, int y, int w, int h) {
        Region id = context.getRegion();
        int gtkState = GTKLookAndFeel.synthStateToGTKState(
                id, context.getComponentState());
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h, id, gtkState)) {
                ENGINE.startPainting(g, x, y, w, h, id, gtkState);
                ENGINE.paintFlatBox(g, context, id, gtkState, ShadowType.NONE,
                        "entry_bg", x, y, w, h, ColorType.TEXT_BACKGROUND);
                ENGINE.finishPainting();
            }
        }
    }
    public void paintRootPaneBackground(SynthContext context, Graphics g,
                                        int x, int y, int w, int h) {
        fillArea(context, g, x, y, w, h, GTKColorType.BACKGROUND);
    }
    public void paintToggleButtonBackground(SynthContext context,
                                            Graphics g,
                                            int x, int y, int w, int h) {
        Region id = context.getRegion();
        JToggleButton toggleButton = (JToggleButton)context.getComponent();
        boolean paintBG = toggleButton.isContentAreaFilled() &&
                          toggleButton.isBorderPainted();
        boolean paintFocus = toggleButton.isFocusPainted();
        boolean toolButton = (toggleButton.getParent() instanceof JToolBar);
        paintButtonBackgroundImpl(context, g, id, "button",
                                  x, y, w, h,
                                  paintBG, paintFocus, false, toolButton);
    }
    public void paintScrollBarBackground(SynthContext context,
                                          Graphics g,
                                          int x, int y, int w,int h) {
        Region id = context.getRegion();
        boolean focused =
                (context.getComponentState() & SynthConstants.FOCUSED) != 0;
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (ENGINE.paintCachedImage(g, x, y, w, h, id, focused)) {
                return;
            }
            ENGINE.startPainting(g, x, y, w, h, id, focused);
            Insets insets = context.getComponent().getInsets();
            GTKStyle style = (GTKStyle)context.getStyle();
            int troughBorder =
                style.getClassSpecificIntValue(context, "trough-border", 1);
            insets.left   -= troughBorder;
            insets.right  -= troughBorder;
            insets.top    -= troughBorder;
            insets.bottom -= troughBorder;
            ENGINE.paintBox(g, context, id, SynthConstants.PRESSED,
                            ShadowType.IN, "trough",
                            x + insets.left,
                            y + insets.top,
                            w - insets.left - insets.right,
                            h - insets.top - insets.bottom);
            if (focused) {
                ENGINE.paintFocus(g, context, id,
                        SynthConstants.ENABLED, "trough", x, y, w, h);
            }
            ENGINE.finishPainting();
        }
    }
    public void paintScrollBarThumbBackground(SynthContext context,
            Graphics g, int x, int y, int w, int h, int dir) {
        Region id = context.getRegion();
        int gtkState = GTKLookAndFeel.synthStateToGTKState(
                id, context.getComponentState());
        JScrollBar sb = (JScrollBar)context.getComponent();
        boolean rtl =
            sb.getOrientation() == JScrollBar.HORIZONTAL &&
            !sb.getComponentOrientation().isLeftToRight();
        double min = 0;
        double max = 100;
        double visible = 20;
        double value;
        if (sb.getMaximum() - sb.getMinimum() == sb.getVisibleAmount()) {
            value = 0;
            visible = 100;
        } else if (sb.getValue() == sb.getMinimum()) {
            value = rtl ? 100 : 0;
        } else if (sb.getValue() >= sb.getMaximum() - sb.getVisibleAmount()) {
            value = rtl ? 0 : 100;
        } else {
            value = 50;
        }
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h, id, gtkState,
                                          dir, value, visible, rtl))
            {
                ENGINE.startPainting(g, x, y, w, h, id, gtkState,
                                     dir, value, visible, rtl);
                Orientation orientation = (dir == JScrollBar.HORIZONTAL ?
                    Orientation.HORIZONTAL : Orientation.VERTICAL);
                ENGINE.setRangeValue(context, id, value, min, max, visible);
                ENGINE.paintSlider(g, context, id, gtkState,
                        ShadowType.OUT, "slider", x, y, w, h, orientation);
                ENGINE.finishPainting();
            }
        }
    }
    public void paintToolTipBackground(SynthContext context, Graphics g,
                                        int x, int y, int w,int h) {
        Region id = context.getRegion();
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h, id)) {
                ENGINE.startPainting(g, x, y, w, h, id);
                ENGINE.paintFlatBox(g, context, id, SynthConstants.ENABLED,
                        ShadowType.OUT, "tooltip", x, y, w, h,
                        ColorType.BACKGROUND);
                ENGINE.finishPainting();
            }
        }
    }
    public void paintTreeCellBackground(SynthContext context, Graphics g,
                                        int x, int y, int w, int h) {
        Region id = context.getRegion();
        int state = context.getComponentState();
        int gtkState = GTKLookAndFeel.synthStateToGTKState(id, state);
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h, id, state)) {
                ENGINE.startPainting(g, x, y, w, h, id, state);
                ENGINE.paintFlatBox(g, context, id, gtkState, ShadowType.NONE,
                        "cell_odd", x, y, w, h, ColorType.TEXT_BACKGROUND);
                ENGINE.finishPainting();
            }
        }
    }
    public void paintTreeCellFocus(SynthContext context, Graphics g,
                                    int x, int y, int w, int h) {
        Region id = Region.TREE_CELL;
        int state = context.getComponentState();
        paintFocus(context, g, id, state, "treeview", x, y, w, h);
    }
    public void paintTreeBackground(SynthContext context, Graphics g,
                                    int x, int y, int w, int h) {
        fillArea(context, g, x, y, w, h, GTKColorType.TEXT_BACKGROUND);
    }
    public void paintViewportBackground(SynthContext context, Graphics g,
                                        int x, int y, int w, int h) {
        fillArea(context, g, x, y, w, h, GTKColorType.TEXT_BACKGROUND);
    }
    void paintFocus(SynthContext context, Graphics g, Region id,
            int state, String detail, int x, int y, int w, int h) {
        int gtkState = GTKLookAndFeel.synthStateToGTKState(id, state);
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h, id, gtkState, "focus")) {
                ENGINE.startPainting(g, x, y, w, h, id, gtkState, "focus");
                ENGINE.paintFocus(g, context, id, gtkState, detail, x, y, w, h);
                ENGINE.finishPainting();
            }
        }
    }
    void paintMetacityElement(SynthContext context, Graphics g,
            int gtkState, String detail, int x, int y, int w, int h,
            ShadowType shadow, ArrowType direction) {
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(
                    g, x, y, w, h, gtkState, detail, shadow, direction)) {
                ENGINE.startPainting(
                        g, x, y, w, h, gtkState, detail, shadow, direction);
                if (detail == "metacity-arrow") {
                    ENGINE.paintArrow(g, context, Region.INTERNAL_FRAME_TITLE_PANE,
                            gtkState, shadow, direction, "", x, y, w, h);
                } else if (detail == "metacity-box") {
                    ENGINE.paintBox(g, context, Region.INTERNAL_FRAME_TITLE_PANE,
                            gtkState, shadow, "", x, y, w, h);
                } else if (detail == "metacity-vline") {
                    ENGINE.paintVline(g, context, Region.INTERNAL_FRAME_TITLE_PANE,
                            gtkState, "", x, y, w, h);
                }
                ENGINE.finishPainting();
            }
        }
    }
    void paintIcon(SynthContext context, Graphics g,
            Method paintMethod, int x, int y, int w, int h) {
        int state = context.getComponentState();
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g, x, y, w, h, state, paintMethod)) {
                ENGINE.startPainting(g, x, y, w, h, state, paintMethod);
                try {
                    paintMethod.invoke(this, context, g, state, x, y, w, h);
                } catch (IllegalAccessException iae) {
                    assert false;
                } catch (InvocationTargetException ite) {
                    assert false;
                }
                ENGINE.finishPainting();
            }
        }
    }
    void paintIcon(SynthContext context, Graphics g,
            Method paintMethod, int x, int y, int w, int h, Object direction) {
        int state = context.getComponentState();
        synchronized (UNIXToolkit.GTK_LOCK) {
            if (! ENGINE.paintCachedImage(g,
                    x, y, w, h, state, paintMethod, direction)) {
                ENGINE.startPainting(g,
                        x, y, w, h, state, paintMethod, direction);
                try {
                    paintMethod.invoke(this, context,
                            g, state, x, y, w, h, direction);
                } catch (IllegalAccessException iae) {
                    assert false;
                } catch (InvocationTargetException ite) {
                    assert false;
                }
                ENGINE.finishPainting();
            }
        }
    }
    public void paintTreeExpandedIcon(SynthContext context,
            Graphics g, int state, int x, int y, int w, int h) {
        ENGINE.paintExpander(g, context, Region.TREE,
                GTKLookAndFeel.synthStateToGTKState(context.getRegion(), state),
                ExpanderStyle.EXPANDED, "treeview", x, y, w, h);
    }
    public void paintTreeCollapsedIcon(SynthContext context,
            Graphics g, int state, int x, int y, int w, int h) {
        ENGINE.paintExpander(g, context, Region.TREE,
                GTKLookAndFeel.synthStateToGTKState(context.getRegion(), state),
                ExpanderStyle.COLLAPSED, "treeview", x, y, w, h);
    }
    public void paintCheckBoxIcon(SynthContext context,
            Graphics g, int state, int x, int y, int w, int h) {
        GTKStyle style = (GTKStyle)context.getStyle();
        int size = style.getClassSpecificIntValue(context,
                        "indicator-size", GTKIconFactory.DEFAULT_ICON_SIZE);
        int offset = style.getClassSpecificIntValue(context,
                        "indicator-spacing", GTKIconFactory.DEFAULT_ICON_SPACING);
        ENGINE.paintCheck(g, context, Region.CHECK_BOX, "checkbutton",
                x+offset, y+offset, size, size);
    }
    public void paintRadioButtonIcon(SynthContext context,
            Graphics g, int state, int x, int y, int w, int h) {
        GTKStyle style = (GTKStyle)context.getStyle();
        int size = style.getClassSpecificIntValue(context,
                        "indicator-size", GTKIconFactory.DEFAULT_ICON_SIZE);
        int offset = style.getClassSpecificIntValue(context,
                        "indicator-spacing", GTKIconFactory.DEFAULT_ICON_SPACING);
        ENGINE.paintOption(g, context, Region.RADIO_BUTTON, "radiobutton",
                x+offset, y+offset, size, size);
    }
    public void paintMenuArrowIcon(SynthContext context, Graphics g,
            int state, int x, int y, int w, int h, ArrowType dir) {
        int gtkState = GTKLookAndFeel.synthStateToGTKState(
                context.getRegion(), state);
        ShadowType shadow = ShadowType.OUT;
        if (gtkState == SynthConstants.MOUSE_OVER) {
            shadow = ShadowType.IN;
        }
        ENGINE.paintArrow(g, context, Region.MENU_ITEM, gtkState, shadow,
                dir, "menuitem", x + 3, y + 3, 7, 7);
    }
    public void paintCheckBoxMenuItemCheckIcon(SynthContext context,
            Graphics g, int state, int x, int y, int w, int h) {
        GTKStyle style = (GTKStyle)context.getStyle();
        int size = style.getClassSpecificIntValue(context,"indicator-size",
                GTKIconFactory.DEFAULT_TOGGLE_MENU_ITEM_SIZE);
        ENGINE.paintCheck(g, context, Region.CHECK_BOX_MENU_ITEM, "check",
                x + GTKIconFactory.CHECK_ICON_EXTRA_INSET,
                y + GTKIconFactory.CHECK_ICON_EXTRA_INSET,
                size, size);
    }
    public void paintRadioButtonMenuItemCheckIcon(SynthContext context,
            Graphics g, int state, int x, int y, int w, int h) {
        GTKStyle style = (GTKStyle)context.getStyle();
        int size = style.getClassSpecificIntValue(context,"indicator-size",
                GTKIconFactory.DEFAULT_TOGGLE_MENU_ITEM_SIZE);
        ENGINE.paintOption(g, context, Region.RADIO_BUTTON_MENU_ITEM, "option",
                x + GTKIconFactory.CHECK_ICON_EXTRA_INSET,
                y + GTKIconFactory.CHECK_ICON_EXTRA_INSET,
                size, size);
    }
    public void paintToolBarHandleIcon(SynthContext context, Graphics g,
            int state, int x, int y, int w, int h, Orientation orientation) {
        int gtkState = GTKLookAndFeel.synthStateToGTKState(
                context.getRegion(), state);
        orientation = (orientation == Orientation.HORIZONTAL) ?
            Orientation.VERTICAL : Orientation.HORIZONTAL;
        ENGINE.paintHandle(g, context, Region.TOOL_BAR, gtkState,
                ShadowType.OUT, "handlebox", x, y, w, h, orientation);
    }
    public void paintAscendingSortIcon(SynthContext context,
            Graphics g, int state, int x, int y, int w, int h) {
        ENGINE.paintArrow(g, context, Region.TABLE, SynthConstants.ENABLED,
                ShadowType.IN, ArrowType.UP, "arrow", x, y, w, h);
    }
    public void paintDescendingSortIcon(SynthContext context,
            Graphics g, int state, int x, int y, int w, int h) {
        ENGINE.paintArrow(g, context, Region.TABLE, SynthConstants.ENABLED,
                ShadowType.IN, ArrowType.DOWN, "arrow", x, y, w, h);
    }
    private void fillArea(SynthContext context, Graphics g,
                          int x, int y, int w, int h, ColorType colorType) {
        if (context.getComponent().isOpaque()) {
            Region id = context.getRegion();
            int gtkState = GTKLookAndFeel.synthStateToGTKState(id,
                    context.getComponentState());
            GTKStyle style = (GTKStyle)context.getStyle();
            g.setColor(style.getGTKColor(context, gtkState, colorType));
            g.fillRect(x, y, w, h);
        }
    }
    static class ListTableFocusBorder extends AbstractBorder implements
                          UIResource {
        private boolean selectedCell;
        private boolean focusedCell;
        public static ListTableFocusBorder getSelectedCellBorder() {
            return new ListTableFocusBorder(true, true);
        }
        public static ListTableFocusBorder getUnselectedCellBorder() {
            return new ListTableFocusBorder(false, true);
        }
        public static ListTableFocusBorder getNoFocusCellBorder() {
            return new ListTableFocusBorder(false, false);
        }
        public ListTableFocusBorder(boolean selectedCell, boolean focusedCell) {
            this.selectedCell = selectedCell;
            this.focusedCell = focusedCell;
        }
        private SynthContext getContext(Component c) {
            SynthContext context = null;
            ComponentUI ui = null;
            if (c instanceof JLabel) {
                ui = ((JLabel)c).getUI();
            }
            if (ui instanceof SynthUI) {
                context = ((SynthUI)ui).getContext((JComponent)c);
            }
            return context;
        }
        public void paintBorder(Component c, Graphics g, int x, int y,
                                int w, int h) {
            if (focusedCell) {
                SynthContext context = getContext(c);
                int state = (selectedCell? SynthConstants.SELECTED:
                             SynthConstants.FOCUSED | SynthConstants.ENABLED);
                if (context != null) {
                    GTKPainter.INSTANCE.paintFocus(context, g,
                            Region.TABLE, state, "", x, y, w, h);
                }
            }
        }
        public Insets getBorderInsets(Component c, Insets i) {
            SynthContext context = getContext(c);
            if (context != null) {
                i = context.getStyle().getInsets(context, i);
            }
            return i;
        }
        public boolean isBorderOpaque() {
            return true;
        }
    }
    static class TitledBorder extends AbstractBorder implements UIResource {
        public void paintBorder(Component c, Graphics g, int x, int y,
                                int w, int h) {
            SynthContext context = getContext((JComponent)c);
            Region id = context.getRegion();
            int state = context.getComponentState();
            int gtkState = GTKLookAndFeel.synthStateToGTKState(id, state);
            synchronized (UNIXToolkit.GTK_LOCK) {
                if (! ENGINE.paintCachedImage(g, x, y, w, h, id)) {
                    ENGINE.startPainting(g, x, y, w, h, id);
                    ENGINE.paintShadow(g, context, id, gtkState, ShadowType.ETCHED_IN,
                                      "frame", x, y, w, h);
                    ENGINE.finishPainting();
                }
            }
        }
        public Insets getBorderInsets(Component c, Insets i) {
            SynthContext context = getContext((JComponent)c);
            return context.getStyle().getInsets(context, i);
        }
        public boolean isBorderOpaque() {
            return true;
        }
        private SynthStyle getStyle(JComponent c) {
            return SynthLookAndFeel.getStyle(c, GTKEngine.CustomRegion.TITLED_BORDER);
        }
        private SynthContext getContext(JComponent c) {
            int state = SynthConstants.DEFAULT;
            return new SynthContext(c, GTKEngine.CustomRegion.TITLED_BORDER,
                                    getStyle(c), state);
        }
    }
}
