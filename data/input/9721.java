public class BasicProgressBarUI extends ProgressBarUI {
    private int cachedPercent;
    private int cellLength, cellSpacing;
    private Color selectionForeground, selectionBackground;
    private Animator animator;
    protected JProgressBar progressBar;
    protected ChangeListener changeListener;
    private Handler handler;
    private int animationIndex = 0;
    private int numFrames;   
    private int repaintInterval;
    private int cycleTime;  
    private static boolean ADJUSTTIMER = true; 
    protected Rectangle boxRect;
    private Rectangle nextPaintRect;
    private Rectangle componentInnards;    
    private Rectangle oldComponentInnards; 
    private double delta = 0.0;
    private int maxPosition = 0; 
    public static ComponentUI createUI(JComponent x) {
        return new BasicProgressBarUI();
    }
    public void installUI(JComponent c) {
        progressBar = (JProgressBar)c;
        installDefaults();
        installListeners();
        if (progressBar.isIndeterminate()) {
            initIndeterminateValues();
        }
    }
    public void uninstallUI(JComponent c) {
        if (progressBar.isIndeterminate()) {
            cleanUpIndeterminateValues();
        }
        uninstallDefaults();
        uninstallListeners();
        progressBar = null;
    }
    protected void installDefaults() {
        LookAndFeel.installProperty(progressBar, "opaque", Boolean.TRUE);
        LookAndFeel.installBorder(progressBar,"ProgressBar.border");
        LookAndFeel.installColorsAndFont(progressBar,
                                         "ProgressBar.background",
                                         "ProgressBar.foreground",
                                         "ProgressBar.font");
        cellLength = UIManager.getInt("ProgressBar.cellLength");
        if (cellLength == 0) cellLength = 1;
        cellSpacing = UIManager.getInt("ProgressBar.cellSpacing");
        selectionForeground = UIManager.getColor("ProgressBar.selectionForeground");
        selectionBackground = UIManager.getColor("ProgressBar.selectionBackground");
    }
    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(progressBar);
    }
    protected void installListeners() {
        changeListener = getHandler();
        progressBar.addChangeListener(changeListener);
        progressBar.addPropertyChangeListener(getHandler());
    }
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    protected void startAnimationTimer() {
        if (animator == null) {
            animator = new Animator();
        }
        animator.start(getRepaintInterval());
    }
    protected void stopAnimationTimer() {
        if (animator != null) {
            animator.stop();
        }
    }
    protected void uninstallListeners() {
        progressBar.removeChangeListener(changeListener);
        progressBar.removePropertyChangeListener(getHandler());
        handler = null;
    }
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        if (progressBar.isStringPainted() &&
                progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            FontMetrics metrics = progressBar.
                    getFontMetrics(progressBar.getFont());
            Insets insets = progressBar.getInsets();
            int y = insets.top;
            height = height - insets.top - insets.bottom;
            return y + (height + metrics.getAscent() -
                        metrics.getLeading() -
                        metrics.getDescent()) / 2;
        }
        return -1;
    }
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            JComponent c) {
        super.getBaselineResizeBehavior(c);
        if (progressBar.isStringPainted() &&
                progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            return Component.BaselineResizeBehavior.CENTER_OFFSET;
        }
        return Component.BaselineResizeBehavior.OTHER;
    }
    protected Dimension getPreferredInnerHorizontal() {
        Dimension horizDim = (Dimension)DefaultLookup.get(progressBar, this,
            "ProgressBar.horizontalSize");
        if (horizDim == null) {
            horizDim = new Dimension(146, 12);
        }
        return horizDim;
    }
    protected Dimension getPreferredInnerVertical() {
        Dimension vertDim = (Dimension)DefaultLookup.get(progressBar, this,
            "ProgressBar.verticalSize");
        if (vertDim == null) {
            vertDim = new Dimension(12, 146);
        }
        return vertDim;
    }
    protected Color getSelectionForeground() {
        return selectionForeground;
    }
    protected Color getSelectionBackground() {
        return selectionBackground;
    }
    private int getCachedPercent() {
        return cachedPercent;
    }
    private void setCachedPercent(int cachedPercent) {
        this.cachedPercent = cachedPercent;
    }
    protected int getCellLength() {
        if (progressBar.isStringPainted()) {
            return 1;
        } else {
            return cellLength;
        }
    }
    protected void setCellLength(int cellLen) {
        this.cellLength = cellLen;
    }
    protected int getCellSpacing() {
        if (progressBar.isStringPainted()) {
            return 0;
        } else {
            return cellSpacing;
        }
    }
    protected void setCellSpacing(int cellSpace) {
        this.cellSpacing = cellSpace;
    }
    protected int getAmountFull(Insets b, int width, int height) {
        int amountFull = 0;
        BoundedRangeModel model = progressBar.getModel();
        if ( (model.getMaximum() - model.getMinimum()) != 0) {
            if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
                amountFull = (int)Math.round(width *
                                             progressBar.getPercentComplete());
            } else {
                amountFull = (int)Math.round(height *
                                             progressBar.getPercentComplete());
            }
        }
        return amountFull;
    }
    public void paint(Graphics g, JComponent c) {
        if (progressBar.isIndeterminate()) {
            paintIndeterminate(g, c);
        } else {
            paintDeterminate(g, c);
        }
    }
    protected Rectangle getBox(Rectangle r) {
        int currentFrame = getAnimationIndex();
        int middleFrame = numFrames/2;
        if (sizeChanged() || delta == 0.0 || maxPosition == 0.0) {
            updateSizes();
        }
        r = getGenericBox(r);
        if (r == null) {
            return null;
        }
        if (middleFrame <= 0) {
            return null;
        }
        if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            if (currentFrame < middleFrame) {
                r.x = componentInnards.x
                      + (int)Math.round(delta * (double)currentFrame);
            } else {
                r.x = maxPosition
                      - (int)Math.round(delta *
                                        (currentFrame - middleFrame));
            }
        } else { 
            if (currentFrame < middleFrame) {
                r.y = componentInnards.y
                      + (int)Math.round(delta * currentFrame);
            } else {
                r.y = maxPosition
                      - (int)Math.round(delta *
                                        (currentFrame - middleFrame));
            }
        }
        return r;
    }
    private void updateSizes() {
        int length = 0;
        if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            length = getBoxLength(componentInnards.width,
                                  componentInnards.height);
            maxPosition = componentInnards.x + componentInnards.width
                          - length;
        } else { 
            length = getBoxLength(componentInnards.height,
                                  componentInnards.width);
            maxPosition = componentInnards.y + componentInnards.height
                          - length;
        }
        delta = 2.0 * (double)maxPosition/(double)numFrames;
    }
    private Rectangle getGenericBox(Rectangle r) {
        if (r == null) {
            r = new Rectangle();
        }
        if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            r.width = getBoxLength(componentInnards.width,
                                   componentInnards.height);
            if (r.width < 0) {
                r = null;
            } else {
                r.height = componentInnards.height;
                r.y = componentInnards.y;
            }
        } else { 
            r.height = getBoxLength(componentInnards.height,
                                    componentInnards.width);
            if (r.height < 0) {
                r = null;
            } else {
                r.width = componentInnards.width;
                r.x = componentInnards.x;
            }
        } 
        return r;
    }
    protected int getBoxLength(int availableLength, int otherDimension) {
        return (int)Math.round(availableLength/6.0);
    }
    protected void paintIndeterminate(Graphics g, JComponent c) {
        if (!(g instanceof Graphics2D)) {
            return;
        }
        Insets b = progressBar.getInsets(); 
        int barRectWidth = progressBar.getWidth() - (b.right + b.left);
        int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);
        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D)g;
        boxRect = getBox(boxRect);
        if (boxRect != null) {
            g2.setColor(progressBar.getForeground());
            g2.fillRect(boxRect.x, boxRect.y,
                       boxRect.width, boxRect.height);
        }
        if (progressBar.isStringPainted()) {
            if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
                paintString(g2, b.left, b.top,
                            barRectWidth, barRectHeight,
                            boxRect.x, boxRect.width, b);
            }
            else {
                paintString(g2, b.left, b.top,
                            barRectWidth, barRectHeight,
                            boxRect.y, boxRect.height, b);
            }
        }
    }
    protected void paintDeterminate(Graphics g, JComponent c) {
        if (!(g instanceof Graphics2D)) {
            return;
        }
        Insets b = progressBar.getInsets(); 
        int barRectWidth = progressBar.getWidth() - (b.right + b.left);
        int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);
        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }
        int cellLength = getCellLength();
        int cellSpacing = getCellSpacing();
        int amountFull = getAmountFull(b, barRectWidth, barRectHeight);
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(progressBar.getForeground());
        if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            if (cellSpacing == 0 && amountFull > 0) {
                g2.setStroke(new BasicStroke((float)barRectHeight,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            } else {
                g2.setStroke(new BasicStroke((float)barRectHeight,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        0.f, new float[] { cellLength, cellSpacing }, 0.f));
            }
            if (BasicGraphicsUtils.isLeftToRight(c)) {
                g2.drawLine(b.left, (barRectHeight/2) + b.top,
                        amountFull + b.left, (barRectHeight/2) + b.top);
            } else {
                g2.drawLine((barRectWidth + b.left),
                        (barRectHeight/2) + b.top,
                        barRectWidth + b.left - amountFull,
                        (barRectHeight/2) + b.top);
            }
        } else { 
            if (cellSpacing == 0 && amountFull > 0) {
                g2.setStroke(new BasicStroke((float)barRectWidth,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            } else {
                g2.setStroke(new BasicStroke((float)barRectWidth,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        0f, new float[] { cellLength, cellSpacing }, 0f));
            }
            g2.drawLine(barRectWidth/2 + b.left,
                    b.top + barRectHeight,
                    barRectWidth/2 + b.left,
                    b.top + barRectHeight - amountFull);
        }
        if (progressBar.isStringPainted()) {
            paintString(g, b.left, b.top,
                        barRectWidth, barRectHeight,
                        amountFull, b);
        }
    }
    protected void paintString(Graphics g, int x, int y,
                               int width, int height,
                               int amountFull, Insets b) {
        if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            if (BasicGraphicsUtils.isLeftToRight(progressBar)) {
                if (progressBar.isIndeterminate()) {
                    boxRect = getBox(boxRect);
                    paintString(g, x, y, width, height,
                            boxRect.x, boxRect.width, b);
                } else {
                    paintString(g, x, y, width, height, x, amountFull, b);
                }
            }
            else {
                paintString(g, x, y, width, height, x + width - amountFull,
                            amountFull, b);
            }
        }
        else {
            if (progressBar.isIndeterminate()) {
                boxRect = getBox(boxRect);
                paintString(g, x, y, width, height,
                        boxRect.y, boxRect.height, b);
            } else {
                paintString(g, x, y, width, height, y + height - amountFull,
                        amountFull, b);
            }
        }
    }
    private void paintString(Graphics g, int x, int y, int width, int height,
                             int fillStart, int amountFull, Insets b) {
        if (!(g instanceof Graphics2D)) {
            return;
        }
        Graphics2D g2 = (Graphics2D)g;
        String progressString = progressBar.getString();
        g2.setFont(progressBar.getFont());
        Point renderLocation = getStringPlacement(g2, progressString,
                                                  x, y, width, height);
        Rectangle oldClip = g2.getClipBounds();
        if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            g2.setColor(getSelectionBackground());
            SwingUtilities2.drawString(progressBar, g2, progressString,
                                       renderLocation.x, renderLocation.y);
            g2.setColor(getSelectionForeground());
            g2.clipRect(fillStart, y, amountFull, height);
            SwingUtilities2.drawString(progressBar, g2, progressString,
                                       renderLocation.x, renderLocation.y);
        } else { 
            g2.setColor(getSelectionBackground());
            AffineTransform rotate =
                    AffineTransform.getRotateInstance(Math.PI/2);
            g2.setFont(progressBar.getFont().deriveFont(rotate));
            renderLocation = getStringPlacement(g2, progressString,
                                                  x, y, width, height);
            SwingUtilities2.drawString(progressBar, g2, progressString,
                                       renderLocation.x, renderLocation.y);
            g2.setColor(getSelectionForeground());
            g2.clipRect(x, fillStart, width, amountFull);
            SwingUtilities2.drawString(progressBar, g2, progressString,
                                       renderLocation.x, renderLocation.y);
        }
        g2.setClip(oldClip);
    }
    protected Point getStringPlacement(Graphics g, String progressString,
                                       int x,int y,int width,int height) {
        FontMetrics fontSizer = SwingUtilities2.getFontMetrics(progressBar, g,
                                            progressBar.getFont());
        int stringWidth = SwingUtilities2.stringWidth(progressBar, fontSizer,
                                                      progressString);
        if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            return new Point(x + Math.round(width/2 - stringWidth/2),
                             y + ((height +
                                 fontSizer.getAscent() -
                                 fontSizer.getLeading() -
                                 fontSizer.getDescent()) / 2));
        } else { 
            return new Point(x + ((width - fontSizer.getAscent() +
                    fontSizer.getLeading() + fontSizer.getDescent()) / 2),
                    y + Math.round(height/2 - stringWidth/2));
        }
    }
    public Dimension getPreferredSize(JComponent c) {
        Dimension       size;
        Insets          border = progressBar.getInsets();
        FontMetrics     fontSizer = progressBar.getFontMetrics(
                                                  progressBar.getFont());
        if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            size = new Dimension(getPreferredInnerHorizontal());
            if (progressBar.isStringPainted()) {
                String progString = progressBar.getString();
                int stringWidth = SwingUtilities2.stringWidth(
                          progressBar, fontSizer, progString);
                if (stringWidth > size.width) {
                    size.width = stringWidth;
                }
                int stringHeight = fontSizer.getHeight() +
                                   fontSizer.getDescent();
                if (stringHeight > size.height) {
                    size.height = stringHeight;
                }
            }
        } else {
            size = new Dimension(getPreferredInnerVertical());
            if (progressBar.isStringPainted()) {
                String progString = progressBar.getString();
                int stringHeight = fontSizer.getHeight() +
                        fontSizer.getDescent();
                if (stringHeight > size.width) {
                    size.width = stringHeight;
                }
                int stringWidth = SwingUtilities2.stringWidth(
                                       progressBar, fontSizer, progString);
                if (stringWidth > size.height) {
                    size.height = stringWidth;
                }
            }
        }
        size.width += border.left + border.right;
        size.height += border.top + border.bottom;
        return size;
    }
    public Dimension getMinimumSize(JComponent c) {
        Dimension pref = getPreferredSize(progressBar);
        if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            pref.width = 10;
        } else {
            pref.height = 10;
        }
        return pref;
    }
    public Dimension getMaximumSize(JComponent c) {
        Dimension pref = getPreferredSize(progressBar);
        if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            pref.width = Short.MAX_VALUE;
        } else {
            pref.height = Short.MAX_VALUE;
        }
        return pref;
    }
    protected int getAnimationIndex() {
        return animationIndex;
    }
    protected final int getFrameCount() {
        return numFrames;
    }
    protected void setAnimationIndex(int newValue) {
        if (animationIndex != newValue) {
            if (sizeChanged()) {
                animationIndex = newValue;
                maxPosition = 0;  
                delta = 0.0;      
                progressBar.repaint();
                return;
            }
            nextPaintRect = getBox(nextPaintRect);
            animationIndex = newValue;
            if (nextPaintRect != null) {
                boxRect = getBox(boxRect);
                if (boxRect != null) {
                    nextPaintRect.add(boxRect);
                }
            }
        } else { 
            return;
        }
        if (nextPaintRect != null) {
            progressBar.repaint(nextPaintRect);
        } else {
            progressBar.repaint();
        }
    }
    private boolean sizeChanged() {
        if ((oldComponentInnards == null) || (componentInnards == null)) {
            return true;
        }
        oldComponentInnards.setRect(componentInnards);
        componentInnards = SwingUtilities.calculateInnerArea(progressBar,
                                                             componentInnards);
        return !oldComponentInnards.equals(componentInnards);
    }
    protected void incrementAnimationIndex() {
        int newValue = getAnimationIndex() + 1;
        if (newValue < numFrames) {
            setAnimationIndex(newValue);
        } else {
            setAnimationIndex(0);
        }
    }
    private int getRepaintInterval() {
        return repaintInterval;
    }
    private int initRepaintInterval() {
        repaintInterval = DefaultLookup.getInt(progressBar,
                this, "ProgressBar.repaintInterval", 50);
        return repaintInterval;
    }
    private int getCycleTime() {
        return cycleTime;
    }
    private int initCycleTime() {
        cycleTime = DefaultLookup.getInt(progressBar, this,
                "ProgressBar.cycleTime", 3000);
        return cycleTime;
    }
    private void initIndeterminateDefaults() {
        initRepaintInterval(); 
        initCycleTime();       
        if (repaintInterval <= 0) {
            repaintInterval = 100;
        }
        if (repaintInterval > cycleTime) {
            cycleTime = repaintInterval * 20;
        } else {
            int factor = (int)Math.ceil(
                                 ((double)cycleTime)
                               / ((double)repaintInterval*2));
            cycleTime = repaintInterval*factor*2;
        }
    }
    private void initIndeterminateValues() {
        initIndeterminateDefaults();
        numFrames = cycleTime/repaintInterval;
        initAnimationIndex();
        boxRect = new Rectangle();
        nextPaintRect = new Rectangle();
        componentInnards = new Rectangle();
        oldComponentInnards = new Rectangle();
        progressBar.addHierarchyListener(getHandler());
        if (progressBar.isDisplayable()) {
            startAnimationTimer();
        }
    }
    private void cleanUpIndeterminateValues() {
        if (progressBar.isDisplayable()) {
            stopAnimationTimer();
        }
        cycleTime = repaintInterval = 0;
        numFrames = animationIndex = 0;
        maxPosition = 0;
        delta = 0.0;
        boxRect = nextPaintRect = null;
        componentInnards = oldComponentInnards = null;
        progressBar.removeHierarchyListener(getHandler());
    }
    private void initAnimationIndex() {
        if ((progressBar.getOrientation() == JProgressBar.HORIZONTAL) &&
            (BasicGraphicsUtils.isLeftToRight(progressBar))) {
            setAnimationIndex(0);
        } else {
            setAnimationIndex(numFrames/2);
        }
    }
    private class Animator implements ActionListener {
        private Timer timer;
        private long previousDelay; 
        private int interval; 
        private long lastCall; 
        private int MINIMUM_DELAY = 5;
        private void start(int interval) {
            previousDelay = interval;
            lastCall = 0;
            if (timer == null) {
                timer = new Timer(interval, this);
            } else {
                timer.setDelay(interval);
            }
            if (ADJUSTTIMER) {
                timer.setRepeats(false);
                timer.setCoalesce(false);
            }
            timer.start();
        }
        private void stop() {
            timer.stop();
        }
        public void actionPerformed(ActionEvent e) {
            if (ADJUSTTIMER) {
                long time = System.currentTimeMillis();
                if (lastCall > 0) { 
                   int nextDelay = (int)(previousDelay
                                          - time + lastCall
                                          + getRepaintInterval());
                    if (nextDelay < MINIMUM_DELAY) {
                        nextDelay = MINIMUM_DELAY;
                    }
                    timer.setInitialDelay(nextDelay);
                    previousDelay = nextDelay;
                }
                timer.start();
                lastCall = time;
            }
            incrementAnimationIndex(); 
        }
    }
    public class ChangeHandler implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            getHandler().stateChanged(e);
        }
    }
    private class Handler implements ChangeListener, PropertyChangeListener, HierarchyListener {
        public void stateChanged(ChangeEvent e) {
            BoundedRangeModel model = progressBar.getModel();
            int newRange = model.getMaximum() - model.getMinimum();
            int newPercent;
            int oldPercent = getCachedPercent();
            if (newRange > 0) {
                newPercent = (int)((100 * (long)model.getValue()) / newRange);
            } else {
                newPercent = 0;
            }
            if (newPercent != oldPercent) {
                setCachedPercent(newPercent);
                progressBar.repaint();
            }
        }
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if ("indeterminate" == prop) {
                if (progressBar.isIndeterminate()) {
                    initIndeterminateValues();
                } else {
                    cleanUpIndeterminateValues();
                }
                progressBar.repaint();
            }
        }
        public void hierarchyChanged(HierarchyEvent he) {
            if ((he.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                if (progressBar.isIndeterminate()) {
                    if (progressBar.isDisplayable()) {
                        startAnimationTimer();
                    } else {
                        stopAnimationTimer();
                    }
                }
            }
        }
    }
}
