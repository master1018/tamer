public final class CompositionArea extends JPanel implements InputMethodListener {
    private CompositionAreaHandler handler;
    private TextLayout composedTextLayout;
    private TextHitInfo caret = null;
    private JFrame compositionWindow;
    private final static int TEXT_ORIGIN_X = 5;
    private final static int TEXT_ORIGIN_Y = 15;
    private final static int PASSIVE_WIDTH = 480;
    private final static int WIDTH_MARGIN=10;
    private final static int HEIGHT_MARGIN=3;
    CompositionArea() {
        String windowTitle = Toolkit.getProperty("AWT.CompositionWindowTitle", "Input Window");
        compositionWindow =
            (JFrame)InputMethodContext.createInputMethodWindow(windowTitle, null, true);
        setOpaque(true);
        setBorder(LineBorder.createGrayLineBorder());
        setForeground(Color.black);
        setBackground(Color.white);
        enableInputMethods(true);
        enableEvents(AWTEvent.KEY_EVENT_MASK);
        compositionWindow.getContentPane().add(this);
        compositionWindow.addWindowListener(new FrameWindowAdapter());
        addInputMethodListener(this);
        compositionWindow.enableInputMethods(false);
        compositionWindow.pack();
        Dimension windowSize = compositionWindow.getSize();
        Dimension screenSize = (getToolkit()).getScreenSize();
        compositionWindow.setLocation(screenSize.width - windowSize.width-20,
                                    screenSize.height - windowSize.height-100);
        compositionWindow.setVisible(false);
    }
    synchronized void setHandlerInfo(CompositionAreaHandler handler, InputContext inputContext) {
        this.handler = handler;
        ((InputMethodWindow) compositionWindow).setInputContext(inputContext);
    }
    public InputMethodRequests getInputMethodRequests() {
        return handler;
    }
    private Rectangle getCaretRectangle(TextHitInfo caret) {
        int caretLocation = 0;
        TextLayout layout = composedTextLayout;
        if (layout != null) {
            caretLocation = Math.round(layout.getCaretInfo(caret)[0]);
        }
        Graphics g = getGraphics();
        FontMetrics metrics = null;
        try {
            metrics = g.getFontMetrics();
        } finally {
            g.dispose();
        }
        return new Rectangle(TEXT_ORIGIN_X + caretLocation,
                             TEXT_ORIGIN_Y - metrics.getAscent(),
                             0, metrics.getAscent() + metrics.getDescent());
    }
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(getForeground());
        TextLayout layout = composedTextLayout;
        if (layout != null) {
            layout.draw((Graphics2D) g, TEXT_ORIGIN_X, TEXT_ORIGIN_Y);
        }
        if (caret != null) {
            Rectangle rectangle = getCaretRectangle(caret);
            g.setXORMode(getBackground());
            g.fillRect(rectangle.x, rectangle.y, 1, rectangle.height);
            g.setPaintMode();
        }
    }
    void setCompositionAreaVisible(boolean visible) {
        compositionWindow.setVisible(visible);
    }
    boolean isCompositionAreaVisible() {
        return compositionWindow.isVisible();
    }
    class FrameWindowAdapter extends WindowAdapter {
        public void windowActivated(WindowEvent e) {
            requestFocus();
        }
    }
    public void inputMethodTextChanged(InputMethodEvent event) {
        handler.inputMethodTextChanged(event);
    }
    public void caretPositionChanged(InputMethodEvent event) {
        handler.caretPositionChanged(event);
    }
    void setText(AttributedCharacterIterator composedText, TextHitInfo caret) {
        composedTextLayout = null;
        if (composedText == null) {
            compositionWindow.setVisible(false);
            this.caret = null;
        } else {
            if (!compositionWindow.isVisible()) {
                compositionWindow.setVisible(true);
            }
            Graphics g = getGraphics();
            if (g == null) {
                return;
            }
            try {
                updateWindowLocation();
                FontRenderContext context = ((Graphics2D)g).getFontRenderContext();
                composedTextLayout = new TextLayout(composedText, context);
                Rectangle2D bounds = composedTextLayout.getBounds();
                this.caret = caret;
                FontMetrics metrics = g.getFontMetrics();
                Rectangle2D maxCharBoundsRec = metrics.getMaxCharBounds(g);
                int newHeight = (int)maxCharBoundsRec.getHeight() + HEIGHT_MARGIN;
                int newFrameHeight = newHeight +compositionWindow.getInsets().top
                                               +compositionWindow.getInsets().bottom;
                InputMethodRequests req = handler.getClientInputMethodRequests();
                int newWidth = (req==null) ? PASSIVE_WIDTH : (int)bounds.getWidth() + WIDTH_MARGIN;
                int newFrameWidth = newWidth + compositionWindow.getInsets().left
                                             + compositionWindow.getInsets().right;
                setPreferredSize(new Dimension(newWidth, newHeight));
                compositionWindow.setSize(new Dimension(newFrameWidth, newFrameHeight));
                paint(g);
            }
            finally {
                g.dispose();
            }
        }
    }
    void setCaret(TextHitInfo caret) {
        this.caret = caret;
        if (compositionWindow.isVisible()) {
            Graphics g = getGraphics();
            try {
                paint(g);
            } finally {
                g.dispose();
            }
        }
    }
    void updateWindowLocation() {
        InputMethodRequests req = handler.getClientInputMethodRequests();
        if (req == null) {
            return;
        }
        Point windowLocation = new Point();
        Rectangle caretRect = req.getTextLocation(null);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = compositionWindow.getSize();
        final int SPACING = 2;
        if (caretRect.x + windowSize.width > screenSize.width) {
            windowLocation.x = screenSize.width - windowSize.width;
        } else {
            windowLocation.x = caretRect.x;
        }
        if (caretRect.y + caretRect.height + SPACING + windowSize.height > screenSize.height) {
            windowLocation.y = caretRect.y - SPACING - windowSize.height;
        } else {
            windowLocation.y = caretRect.y + caretRect.height + SPACING;
        }
        compositionWindow.setLocation(windowLocation);
    }
    Rectangle getTextLocation(TextHitInfo offset) {
        Rectangle rectangle = getCaretRectangle(offset);
        Point location = getLocationOnScreen();
        rectangle.translate(location.x, location.y);
        return rectangle;
    }
   TextHitInfo getLocationOffset(int x, int y) {
        TextLayout layout = composedTextLayout;
        if (layout == null) {
            return null;
        } else {
            Point location = getLocationOnScreen();
            x -= location.x + TEXT_ORIGIN_X;
            y -= location.y + TEXT_ORIGIN_Y;
            if (layout.getBounds().contains(x, y)) {
                return layout.hitTestChar(x, y);
            } else {
                return null;
            }
        }
    }
    void setCompositionAreaUndecorated(boolean setUndecorated){
          if (compositionWindow.isDisplayable()){
              compositionWindow.removeNotify();
          }
          compositionWindow.setUndecorated(setUndecorated);
          compositionWindow.pack();
    }
}
