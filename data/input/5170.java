class XCheckboxPeer extends XComponentPeer implements CheckboxPeer {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XCheckboxPeer");
    private static final Insets focusInsets = new Insets(0,0,0,0);
    private static final Insets borderInsets = new Insets(2,2,2,2);
    private static final int checkBoxInsetFromText = 2;
    private static final double MASTER_SIZE = 128.0;
    private static final Polygon MASTER_CHECKMARK = new Polygon(
        new int[] {1, 25,56,124,124,85, 64},  
        new int[] {59,35,67,  0, 12,66,123},  
      7);
    private Shape myCheckMark;
    private Color focusColor = SystemColor.windowText;
    private boolean pressed;
    private boolean armed;
    private boolean selected;
    private Rectangle textRect;
    private Rectangle focusRect;
    private int checkBoxSize;
    private int cbX;
    private int cbY;
    String label;
    CheckboxGroup checkBoxGroup;
    XCheckboxPeer(Checkbox target) {
        super(target);
        pressed = false;
        armed = false;
        selected = target.getState();
        label = target.getLabel();
        if ( label == null ) {
            label = "";
        }
        checkBoxGroup = target.getCheckboxGroup();
        updateMotifColors(getPeerBackground());
    }
    public void preInit(XCreateWindowParams params) {
        textRect = new Rectangle();
        focusRect = new Rectangle();
        super.preInit(params);
    }
    public boolean isFocusable() { return true; }
    public void focusGained(FocusEvent e) {
        super.focusGained(e);
        repaint();
    }
    public void focusLost(FocusEvent e) {
        super.focusLost(e);
        repaint();
    }
    void handleJavaKeyEvent(KeyEvent e) {
        int i = e.getID();
        switch (i) {
          case KeyEvent.KEY_PRESSED:
              keyPressed(e);
              break;
          case KeyEvent.KEY_RELEASED:
              keyReleased(e);
              break;
          case KeyEvent.KEY_TYPED:
              keyTyped(e);
              break;
        }
    }
    public void keyTyped(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE)
        {
            action(!selected);
        }
    }
    public void keyReleased(KeyEvent e) {}
    public void  setLabel(java.lang.String label) {
        if ( label == null ) {
            this.label = "";
        } else {
            this.label = label;
        }
        layout();
        repaint();
    }
    void handleJavaMouseEvent(MouseEvent e) {
        super.handleJavaMouseEvent(e);
        int i = e.getID();
        switch (i) {
          case MouseEvent.MOUSE_PRESSED:
              mousePressed(e);
              break;
          case MouseEvent.MOUSE_RELEASED:
              mouseReleased(e);
              break;
          case MouseEvent.MOUSE_ENTERED:
              mouseEntered(e);
              break;
          case MouseEvent.MOUSE_EXITED:
              mouseExited(e);
              break;
          case MouseEvent.MOUSE_CLICKED:
              mouseClicked(e);
              break;
        }
    }
    public void mousePressed(MouseEvent e) {
        if (XToolkit.isLeftMouseButton(e)) {
            Checkbox cb = (Checkbox) e.getSource();
            if (cb.contains(e.getX(), e.getY())) {
                if (log.isLoggable(PlatformLogger.FINER)) {
                    log.finer("mousePressed() on " + target.getName() + " : armed = " + armed + ", pressed = " + pressed
                              + ", selected = " + selected + ", enabled = " + isEnabled());
                }
                if (!isEnabled()) {
                    return;
                }
                if (!armed) {
                    armed = true;
                }
                pressed = true;
                repaint();
            }
        }
    }
    public void mouseReleased(MouseEvent e) {
        if (log.isLoggable(PlatformLogger.FINER)) {
            log.finer("mouseReleased() on " + target.getName() + ": armed = " + armed + ", pressed = " + pressed
                      + ", selected = " + selected + ", enabled = " + isEnabled());
        }
        boolean sendEvent = false;
        if (XToolkit.isLeftMouseButton(e)) {
            if (armed) {
                sendEvent = true;
            }
            pressed = false;
            armed = false;
            if (sendEvent) {
                action(!selected);  
            }
            else {
                repaint();
            }
        }
    }
    public void mouseEntered(MouseEvent e) {
        if (log.isLoggable(PlatformLogger.FINER)) {
            log.finer("mouseEntered() on " + target.getName() + ": armed = " + armed + ", pressed = " + pressed
                      + ", selected = " + selected + ", enabled = " + isEnabled());
        }
        if (pressed) {
            armed = true;
            repaint();
        }
    }
    public void mouseExited(MouseEvent e) {
        if (log.isLoggable(PlatformLogger.FINER)) {
            log.finer("mouseExited() on " + target.getName() + ": armed = " + armed + ", pressed = " + pressed
                      + ", selected = " + selected + ", enabled = " + isEnabled());
        }
        if (armed) {
            armed = false;
            repaint();
        }
    }
    public void mouseClicked(MouseEvent e) {}
    public Dimension getMinimumSize() {
        FontMetrics fm = getFontMetrics(getPeerFont());
        int wdth = fm.stringWidth(label) + getCheckboxSize(fm) + (2 * checkBoxInsetFromText) + 8;
        int hght = Math.max(fm.getHeight() + 8, 15);
        return new Dimension(wdth, hght);
    }
    private int getCheckboxSize(FontMetrics fm) {
        return (fm.getHeight() * 76 / 100) - 1;
    }
    public void setBackground(Color c) {
        updateMotifColors(c);
        super.setBackground(c);
    }
    public void layout() {
        Dimension size = getPeerSize();
        Font f = getPeerFont();
        FontMetrics fm = getFontMetrics(f);
        String text = label;
        checkBoxSize = getCheckboxSize(fm);
        cbX = borderInsets.left + checkBoxInsetFromText;
        cbY = size.height / 2 - checkBoxSize / 2;
        int minTextX = borderInsets.left + 2 * checkBoxInsetFromText + checkBoxSize;
        textRect.width = fm.stringWidth(text == null ? "" : text);
        textRect.height = fm.getHeight();
        textRect.x = Math.max(minTextX, size.width / 2 - textRect.width / 2);
        textRect.y = (size.height - textRect.height) / 2;
        focusRect.x = focusInsets.left;
        focusRect.y = focusInsets.top;
        focusRect.width = size.width-(focusInsets.left+focusInsets.right)-1;
        focusRect.height = size.height-(focusInsets.top+focusInsets.bottom)-1;
        double fsize = (double) checkBoxSize;
        myCheckMark = AffineTransform.getScaleInstance(fsize / MASTER_SIZE, fsize / MASTER_SIZE).createTransformedShape(MASTER_CHECKMARK);
    }
    public void paint(Graphics g) {
        if (g != null) {
            Dimension size = getPeerSize();
            Font f = getPeerFont();
            flush();
            g.setColor(getPeerBackground());   
            g.fillRect(0,0, size.width, size.height);
            if (label != null) {
                g.setFont(f);
                paintText(g, textRect, label);
            }
            if (hasFocus()) {
                paintFocus(g,
                           focusRect.x,
                           focusRect.y,
                           focusRect.width,
                           focusRect.height);
            }
            if (checkBoxGroup == null) {
                paintCheckbox(g, cbX, cbY, checkBoxSize, checkBoxSize);
            }
            else {
                paintRadioButton(g, cbX, cbY, checkBoxSize, checkBoxSize);
            }
        }
        flush();
    }
    public void paintCheckbox(Graphics g,
                              int x, int y, int w, int h) {
        boolean useBufferedImage = false;
        BufferedImage buffer = null;
        Graphics2D g2 = null;
        int rx = x;
        int ry = y;
        if (!(g instanceof Graphics2D)) {
            buffer = graphicsConfig.createCompatibleImage(w, h);
            g2 = buffer.createGraphics();
            useBufferedImage = true;
            rx = 0;
            ry = 0;
        }
        else {
            g2 = (Graphics2D)g;
        }
        try {
            drawMotif3DRect(g2, rx, ry, w-1, h-1, armed | selected);
            g2.setColor((armed | selected) ? selectColor : getPeerBackground());
            g2.fillRect(rx+1, ry+1, w-2, h-2);
            if (armed | selected) {
                g2.setColor(getPeerForeground());
                AffineTransform af = g2.getTransform();
                g2.setTransform(AffineTransform.getTranslateInstance(rx,ry));
                g2.fill(myCheckMark);
                g2.setTransform(af);
            }
        } finally {
            if (useBufferedImage) {
                g2.dispose();
            }
        }
        if (useBufferedImage) {
            g.drawImage(buffer, x, y, null);
        }
    }
    public void setFont(Font f) {
        super.setFont(f);
        target.repaint();
    }
    public void paintRadioButton(Graphics g, int x, int y, int w, int h) {
        g.setColor((armed | selected) ? darkShadow : lightShadow);
        g.drawArc(x-1, y-1, w+2, h+2, 45, 180);
        g.setColor((armed | selected) ? lightShadow : darkShadow);
        g.drawArc(x-1, y-1, w+2, h+2, 45, -180);
        if (armed | selected) {
            g.setColor(selectColor);
            g.fillArc(x+1, y+1, w-1, h-1, 0, 360);
        }
    }
    protected void paintText(Graphics g, Rectangle textRect, String text) {
        FontMetrics fm = g.getFontMetrics();
        int mnemonicIndex = -1;
        if(isEnabled()) {
            g.setColor(getPeerForeground());
            BasicGraphicsUtils.drawStringUnderlineCharAt(g,text,mnemonicIndex , textRect.x , textRect.y + fm.getAscent() );
        }
        else {
            g.setColor(getPeerBackground().brighter());
            BasicGraphicsUtils.drawStringUnderlineCharAt(g,text, mnemonicIndex,
                                                         textRect.x, textRect.y + fm.getAscent());
            g.setColor(getPeerBackground().darker());
            BasicGraphicsUtils.drawStringUnderlineCharAt(g,text, mnemonicIndex,
                                                         textRect.x - 1, textRect.y + fm.getAscent() - 1);
        }
    }
    protected void paintFocus(Graphics g, int x, int y, int w, int h) {
        g.setColor(focusColor);
        g.drawRect(x,y,w,h);
    }
    public void setState(boolean state) {
        if (selected != state) {
            selected = state;
            repaint();
        }
    }
    public void setCheckboxGroup(CheckboxGroup g) {
        checkBoxGroup = g;
        repaint();
    }
    void action(boolean state) {
        final Checkbox cb = (Checkbox)target;
        final boolean newState = state;
        XToolkit.executeOnEventHandlerThread(cb, new Runnable() {
                public void run() {
                    CheckboxGroup cbg = checkBoxGroup;
                    if ((cbg != null) && (cbg.getSelectedCheckbox() == cb) &&
                        cb.getState()) {
                        cb.setState(true);
                        return;
                    }
                    cb.setState(newState);
                    notifyStateChanged(newState);
                }
            });
    }
    void notifyStateChanged(boolean state) {
        Checkbox cb = (Checkbox) target;
        ItemEvent e = new ItemEvent(cb,
                                    ItemEvent.ITEM_STATE_CHANGED,
                                    cb.getLabel(),
                                    state ? ItemEvent.SELECTED : ItemEvent.DESELECTED);
        postEvent(e);
    }
}
