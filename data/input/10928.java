class XScrollPanePeer extends XComponentPeer implements ScrollPanePeer, XScrollbarClient {
    public final static int     MARGIN = 1;
    public final static int     SCROLLBAR;
    public final static int     SPACE = 2;
    public final static int     SCROLLBAR_INSET = 2;
    public final static int     VERTICAL = 1 << 0;
    public final static int     HORIZONTAL = 1 << 1;
    private static Method m_setValue;
    static {
        m_setValue = SunToolkit.getMethod(ScrollPaneAdjustable.class, "setTypedValue", new Class[] {Integer.TYPE, Integer.TYPE});
        SCROLLBAR = XToolkit.getUIDefaults().getInt("ScrollBar.defaultWidth");
    }
    XVerticalScrollbar       vsb;
    XHorizontalScrollbar     hsb;
    XWindow                  clip;
    int                         active=VERTICAL;
    int                         hsbSpace;
    int                         vsbSpace;
    static class XScrollPaneContentWindow extends XWindow {
        XScrollPaneContentWindow(ScrollPane target, long parentWindow) {
            super(target, parentWindow);
        }
        public String getWMName() {
            return "ScrollPane content";
        }
    }
    XScrollPanePeer(ScrollPane target) {
        super(target);
        clip = null;
        XWindow c = new XScrollPaneContentWindow(target,window);
        clip = c;
        vsb = new XVerticalScrollbar(this);
        hsb = new XHorizontalScrollbar(this);
        if (target.getScrollbarDisplayPolicy() == ScrollPane.SCROLLBARS_ALWAYS) {
            vsbSpace = hsbSpace = SCROLLBAR;
        } else {
            vsbSpace = hsbSpace = 0;
        }
        int unitIncrement = 1;
        Adjustable vAdjustable = target.getVAdjustable();
        if (vAdjustable != null){
            unitIncrement = vAdjustable.getUnitIncrement();
        }
        int h = height-hsbSpace;
        vsb.setValues(0, h, 0, h, unitIncrement, Math.max(1, (int)(h * 0.90)));
        vsb.setSize(vsbSpace-SCROLLBAR_INSET, h);
        unitIncrement = 1;
        Adjustable hAdjustable = target.getHAdjustable();
        if (hAdjustable != null){
            unitIncrement = hAdjustable.getUnitIncrement();
        }
        int w = width - vsbSpace;
        hsb.setValues(0, w, 0, w, unitIncrement, Math.max(1, (int)(w * 0.90)));
        hsb.setSize(w, hsbSpace-SCROLLBAR_INSET);
        setViewportSize();
        clip.xSetVisible(true);
    }
    public long getContentWindow()
    {
        return (clip == null) ? window : clip.getWindow();
    }
    public void setBounds(int x, int y, int w, int h, int op) {
        super.setBounds(x, y, w, h, op);
        if (clip == null) return;
        setScrollbarSpace();
        setViewportSize();
        repaint();
    }
    public Insets getInsets() {
        return new Insets(MARGIN, MARGIN, MARGIN+hsbSpace, MARGIN+vsbSpace);
    }
    public int getHScrollbarHeight() {
        return SCROLLBAR;
    }
    public int getVScrollbarWidth() {
        return SCROLLBAR;
    }
    public void childResized(int w, int h) {
        if (setScrollbarSpace()) {
            setViewportSize();
        }
        repaint();
    }
    Dimension getChildSize() {
        ScrollPane sp = (ScrollPane)target;
        if (sp.countComponents() > 0) {
            Component c = sp.getComponent(0);
            return c.size();
        } else {
            return new Dimension(0, 0);
        }
    }
    boolean setScrollbarSpace() {
        ScrollPane sp = (ScrollPane)target;
        boolean changed = false;
        int sbDisplayPolicy = sp.getScrollbarDisplayPolicy();
        if (sbDisplayPolicy == ScrollPane.SCROLLBARS_NEVER) {
            return changed;
        }
        Dimension cSize = getChildSize();
        if (sbDisplayPolicy == ScrollPane.SCROLLBARS_AS_NEEDED) {
            int oldHsbSpace = hsbSpace;
            int oldVsbSpace = vsbSpace;
            hsbSpace = (cSize.width <= (width - 2*MARGIN) ? 0 : SCROLLBAR);
            vsbSpace = (cSize.height <= (height - 2*MARGIN) ? 0 : SCROLLBAR);
            if (hsbSpace == 0 && vsbSpace != 0) {
                hsbSpace = (cSize.width <= (width - SCROLLBAR - 2*MARGIN) ? 0 : SCROLLBAR);
            }
            if (vsbSpace == 0 && hsbSpace != 0) {
                vsbSpace = (cSize.height <= (height - SCROLLBAR - 2*MARGIN) ? 0 : SCROLLBAR);
            }
            if (oldHsbSpace != hsbSpace || oldVsbSpace != vsbSpace) {
                changed = true;
            }
        }
        if (vsbSpace > 0) {
            int vis = height - (2*MARGIN) - hsbSpace;
            int max = Math.max(cSize.height, vis);
            vsb.setValues(vsb.getValue(), vis, 0, max);
            vsb.setBlockIncrement((int)(vsb.getVisibleAmount() * .90));
            vsb.setSize(vsbSpace-SCROLLBAR_INSET, height-hsbSpace);
        }
        if (hsbSpace > 0) {
            int vis = width - (2*MARGIN) - vsbSpace;
            int max = Math.max(cSize.width, vis);
            hsb.setValues(hsb.getValue(), vis, 0, max);
            hsb.setBlockIncrement((int)(hsb.getVisibleAmount() * .90));
            hsb.setSize(width-vsbSpace, hsbSpace-SCROLLBAR_INSET);
        }
        boolean must_scroll = false;
        Point p = new Point(0, 0);
        if (((ScrollPane)target).getComponentCount() > 0){
            p = ((ScrollPane)target).getComponent(0).location();
            if ((vsbSpace == 0) && (p.y < 0)) {
                p.y = 0;
                must_scroll = true;
            }
            if ((hsbSpace == 0) && (p.x < 0)) {
                p.x = 0;
                must_scroll = true;
            }
        }
        if (must_scroll)
            scroll(x, y, VERTICAL | HORIZONTAL);
        return changed;
    }
    void setViewportSize() {
        clip.xSetBounds(MARGIN, MARGIN,
                width - (2*MARGIN)  - vsbSpace,
                height - (2*MARGIN) - hsbSpace);
    }
    public void setUnitIncrement(Adjustable adj, int u) {
        if (adj.getOrientation() == Adjustable.VERTICAL) {
            vsb.setUnitIncrement(u);
        } else {
            hsb.setUnitIncrement(u);
        }
    }
    public void setValue(Adjustable adj, int v) {
        if (adj.getOrientation() == Adjustable.VERTICAL) {
            scroll(-1, v, VERTICAL);
        } else {
            scroll(v, -1, HORIZONTAL);
        }
    }
    public void setScrollPosition(int x, int y) {
        scroll(x, y, VERTICAL | HORIZONTAL);
    }
    void scroll(int x, int y, int flag) {
        scroll(x, y, flag, AdjustmentEvent.TRACK);
    }
    void scroll(int x, int y, int flag, int type) {
        checkSecurity();
        ScrollPane sp = (ScrollPane)target;
        Component c = getScrollChild();
        if (c == null) {
            return;
        }
        int sx, sy;
        Color colors[] = getGUIcolors();
        if (sp.getScrollbarDisplayPolicy() == ScrollPane.SCROLLBARS_NEVER) {
            sx = -x;
            sy = -y;
        } else {
            Point p = c.location();
            sx = p.x;
            sy = p.y;
            if ((flag & HORIZONTAL) != 0) {
                hsb.setValue(Math.min(x, hsb.getMaximum()-hsb.getVisibleAmount()));
                ScrollPaneAdjustable hadj = (ScrollPaneAdjustable)sp.getHAdjustable();
                setAdjustableValue(hadj, hsb.getValue(), type);
                sx = -(hsb.getValue());
                Graphics g = getGraphics();
                try {
                    paintHorScrollbar(g, colors, true);
                } finally {
                    g.dispose();
                }
            }
            if ((flag & VERTICAL) != 0) {
                vsb.setValue(Math.min(y, vsb.getMaximum() - vsb.getVisibleAmount()));
                ScrollPaneAdjustable vadj = (ScrollPaneAdjustable)sp.getVAdjustable();
                setAdjustableValue(vadj, vsb.getValue(), type);
                sy = -(vsb.getValue());
                Graphics g = getGraphics();
                try {
                    paintVerScrollbar(g, colors, true);
                } finally {
                    g.dispose();
                }
            }
        }
        c.move(sx, sy);
    }
    void setAdjustableValue(ScrollPaneAdjustable adj, int value, int type) {
        try {
            m_setValue.invoke(adj, new Object[] {Integer.valueOf(value), Integer.valueOf(type)});
        } catch (IllegalAccessException iae) {
            adj.setValue(value);
        } catch (IllegalArgumentException iae2) {
            adj.setValue(value);
        } catch (InvocationTargetException ite) {
            adj.setValue(value);
            ite.getCause().printStackTrace();
        }
    }
    public void paint(Graphics g) {
        paintComponent(g);
    }
    void paintScrollBars(Graphics g, Color[] colors) {
        if (vsbSpace > 0) {
            paintVerScrollbar(g, colors, true);
        }
        if (hsbSpace > 0) {
            paintHorScrollbar(g, colors, true);
        }
    }
   void repaintScrollBars() {
       Graphics g = getGraphics();
       Color colors[] = getGUIcolors();
       if (g != null) {
           paintScrollBars(g,colors);
       }
       g.dispose();
   }
    public void repaintScrollbarRequest(XScrollbar sb) {
       Graphics g = getGraphics();
       Color colors[] = getGUIcolors();
       if (g != null) {
           if (sb ==  vsb)  {
               paintVerScrollbar(g,colors,true);
           }
           else if (sb ==  hsb) {
               paintHorScrollbar(g,colors,true);
           }
       }
    }
    public void paintComponent(Graphics g) {
        Color colors[] = getGUIcolors();
        g.setColor(colors[BACKGROUND_COLOR]);
        int h = height - hsbSpace;
        int w = width - vsbSpace;
        g.fillRect(0, 0, w, h);
        g.fillRect(w, h, vsbSpace, hsbSpace);
        if (MARGIN > 0) {
            draw3DRect(g, colors, 0, 0, w - 1, h - 1, false);
        }
        paintScrollBars(g,colors);
    }
    public void handleEvent(java.awt.AWTEvent e) {
        super.handleEvent(e);
        int id = e.getID();
        switch(id) {
            case PaintEvent.PAINT:
            case PaintEvent.UPDATE:
                repaintScrollBars();
                break;
        }
    }
    void paintHorScrollbar(Graphics g, Color colors[], boolean paintAll) {
        if (hsbSpace <= 0) {
            return;
        }
        Graphics ng = g.create();
        g.setColor(colors[BACKGROUND_COLOR]);
        int w = width - vsbSpace - (2*MARGIN);
        g.fillRect(MARGIN, height-SCROLLBAR, w, SPACE);
        g.fillRect(0, height-SCROLLBAR, MARGIN, SCROLLBAR);
        g.fillRect(MARGIN + w, height-SCROLLBAR, MARGIN, SCROLLBAR);
        try {
            ng.translate(MARGIN, height - (SCROLLBAR - SPACE));
            hsb.paint(ng, colors, paintAll);
        }
        finally {
            ng.dispose();
        }
    }
    void paintVerScrollbar(Graphics g, Color colors[], boolean paintAll) {
        if (vsbSpace <= 0) {
            return;
        }
        Graphics ng = g.create();
        g.setColor(colors[BACKGROUND_COLOR]);
        int h = height - hsbSpace - (2*MARGIN);
        g.fillRect(width-SCROLLBAR, MARGIN, SPACE, h);
        g.fillRect(width-SCROLLBAR, 0, SCROLLBAR, MARGIN);
        g.fillRect(width-SCROLLBAR, MARGIN+h, SCROLLBAR, MARGIN);
        try {
            ng.translate(width - (SCROLLBAR - SPACE), MARGIN);
            vsb.paint(ng, colors, paintAll);
        }
        finally {
            ng.dispose();
        }
    }
    public void handleJavaMouseEvent( MouseEvent mouseEvent ) {
        super.handleJavaMouseEvent(mouseEvent);
        int modifiers = mouseEvent.getModifiers();
        int id = mouseEvent.getID();
        int x = mouseEvent.getX();
        int y = mouseEvent.getY();
        if ((modifiers & InputEvent.BUTTON1_MASK) == 0) {
            return;
        }
        switch (id) {
            case MouseEvent.MOUSE_PRESSED:
                if (inVerticalScrollbar(x,y )) {
                    active = VERTICAL;
                    int h = height - hsbSpace - (2*MARGIN);
                    vsb.handleMouseEvent(id,modifiers,x - (width - SCROLLBAR + SPACE),y-MARGIN);
                } else if (inHorizontalScrollbar(x, y) ) {
                    active = HORIZONTAL;
                    int w = width - 2*MARGIN - vsbSpace;
                    hsb.handleMouseEvent(id,modifiers,x-MARGIN,y-(height - SCROLLBAR + SPACE));
                }
                break;
            case MouseEvent.MOUSE_RELEASED:
                if (active == VERTICAL) {
                    vsb.handleMouseEvent(id,modifiers,x,y);
                } else if (active == HORIZONTAL) {
                    hsb.handleMouseEvent(id,modifiers,x,y);
                }
                break;
            case MouseEvent.MOUSE_DRAGGED:
                if ((active == VERTICAL)) {
                    int h = height - 2*MARGIN - hsbSpace;
                    vsb.handleMouseEvent(id,modifiers,x-(width - SCROLLBAR + SPACE),y-MARGIN);
                } else if ((active == HORIZONTAL)) {
                    int w = width - 2*MARGIN - vsbSpace;
                    hsb.handleMouseEvent(id,modifiers,x-MARGIN,y-(height - SCROLLBAR + SPACE));
                }
                break;
        }
    }
    public void notifyValue(XScrollbar obj, int type, int v, boolean isAdjusting) {
        if (obj == vsb) {
            scroll(-1, v, VERTICAL, type);
        } else if ((XHorizontalScrollbar)obj == hsb) {
            scroll(v, -1, HORIZONTAL, type);
        }
    }
    boolean inVerticalScrollbar(int x, int y) {
        if (vsbSpace <= 0) {
            return false;
        }
        int h = height - MARGIN - hsbSpace;
        return (x >= width - (SCROLLBAR - SPACE)) && (x < width) && (y >= MARGIN) && (y < h);
    }
    boolean inHorizontalScrollbar(int x, int y) {
        if (hsbSpace <= 0) {
            return false;
        }
        int w = width - MARGIN - vsbSpace;
        return (x >= MARGIN) && (x < w) && (y >= height - (SCROLLBAR - SPACE)) && (y < height);
    }
    private Component getScrollChild() {
        ScrollPane sp = (ScrollPane)target;
        Component child = null;
        try {
            child = sp.getComponent(0);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return child;
    }
    int vval;
    int hval;
    int vmax;
    int hmax;
    public void print(Graphics g) {
        ScrollPane sp = (ScrollPane)target;
        Dimension d = sp.size();
        Color bg = sp.getBackground();
        Color fg = sp.getForeground();
        Point p = sp.getScrollPosition();
        Component c = getScrollChild();
        Dimension cd;
        if (c != null) {
            cd = c.size();
        } else {
            cd = new Dimension(0, 0);
        }
        int sbDisplay = sp.getScrollbarDisplayPolicy();
        int vvis, hvis, vmin, hmin, vmax, hmax, vval, hval;
        switch (sbDisplay) {
            case ScrollPane.SCROLLBARS_NEVER:
                hsbSpace = vsbSpace = 0;
                break;
            case ScrollPane.SCROLLBARS_ALWAYS:
                hsbSpace = vsbSpace = SCROLLBAR;
                break;
            case ScrollPane.SCROLLBARS_AS_NEEDED:
                hsbSpace = (cd.width <= (d.width - 2*MARGIN)? 0 : SCROLLBAR);
                vsbSpace = (cd.height <= (d.height - 2*MARGIN)? 0 : SCROLLBAR);
                if (hsbSpace == 0 && vsbSpace != 0) {
                    hsbSpace = (cd.width <= (d.width - SCROLLBAR - 2*MARGIN)? 0 : SCROLLBAR);
                }
                if (vsbSpace == 0 && hsbSpace != 0) {
                    vsbSpace = (cd.height <= (d.height - SCROLLBAR - 2*MARGIN)? 0 : SCROLLBAR);
                }
        }
        vvis = hvis = vmin = hmin = vmax = hmax = vval = hval = 0;
        if (vsbSpace > 0) {
            vmin = 0;
            vvis = d.height - (2*MARGIN) - hsbSpace;
            vmax = Math.max(cd.height - vvis, 0);
            vval = p.y;
        }
        if (hsbSpace > 0) {
            hmin = 0;
            hvis = d.width - (2*MARGIN) - vsbSpace;
            hmax = Math.max(cd.width - hvis, 0);
            hval = p.x;
        }
        int w = d.width - vsbSpace;
        int h = d.height - hsbSpace;
        g.setColor(bg);
        g.fillRect(0, 0, d.width, d.height);
        if (hsbSpace > 0) {
            int sbw = d.width - vsbSpace;
            g.fillRect(1, d.height - SCROLLBAR - 3, sbw - 1, SCROLLBAR - 3);
            Graphics ng = g.create();
            try {
                ng.translate(0, d.height - (SCROLLBAR - 2));
                drawScrollbar(ng, bg, SCROLLBAR - 2, sbw,
                        hmin, hmax, hval, hvis, true);
            } finally {
                ng.dispose();
            }
        }
        if (vsbSpace > 0) {
            int sbh = d.height - hsbSpace;
            g.fillRect(d.width - SCROLLBAR - 3, 1, SCROLLBAR - 3, sbh - 1);
            Graphics ng = g.create();
            try {
                ng.translate(d.width - (SCROLLBAR - 2), 0);
                drawScrollbar(ng, bg, SCROLLBAR - 2, sbh,
                        vmin, vmax, vval, vvis, false);
            } finally {
                ng.dispose();
            }
        }
        draw3DRect(g, bg, 0, 0, w - 1, h - 1, false);
        target.print(g);
        sp.printComponents(g);
    }
}
