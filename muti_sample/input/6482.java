abstract class XScrollbar {
    private static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XScrollbar");
    private static XScrollRepeater scroller = new XScrollRepeater(null);
    private XScrollRepeater i_scroller = new XScrollRepeater(null);
    private final static int MIN_THUMB_H = 5;
    private static final int ARROW_IND = 1;
    XScrollbarClient sb;
    private int val;
    private int min;
    private int max;
    private int vis;
    private int line;
    private int page;
    private boolean needsRepaint = true;
    private boolean pressed = false;
    private boolean dragging = false;
    Polygon firstArrow, secondArrow;
    int width, height; 
    int barWidth, barLength; 
    int arrowArea;     
    int alignment;
    public static final int ALIGNMENT_VERTICAL = 1, ALIGNMENT_HORIZONTAL = 2;
    int mode;
    Point thumbOffset;
    private Rectangle prevThumb;
    public XScrollbar(int alignment, XScrollbarClient sb) {
        this.sb = sb;
        this.alignment = alignment;
    }
    public boolean needsRepaint() {
        return needsRepaint;
    }
    void notifyValue(int v) {
        notifyValue(v, false);
    }
    void notifyValue(int v, final boolean isAdjusting) {
        if (v < min) {
            v = min;
        } else if (v > max - vis) {
            v = max - vis;
        }
        final int value = v;
        final int mode = this.mode;
        if ((sb != null) && ((value != val)||(!pressed))) {
            SunToolkit.executeOnEventHandlerThread(sb.getEventSource(), new Runnable() {
                    public void run() {
                        sb.notifyValue(XScrollbar.this, mode, value, isAdjusting);
                    }
                });
        }
    }
    abstract protected void rebuildArrows();
    public void setSize(int width, int height) {
        if (log.isLoggable(PlatformLogger.FINER)) log.finer("Setting scroll bar " + this + " size to " + width + "x" + height);
        this.width = width;
        this.height = height;
    }
    protected Polygon createArrowShape(boolean vertical, boolean up) {
        Polygon arrow = new Polygon();
        if (vertical) {
            int x = width / 2 - getArrowWidth()/2;
            int y1 = (up ? ARROW_IND : barLength - ARROW_IND);
            int y2 = (up ? getArrowWidth() : barLength - getArrowWidth() - ARROW_IND);
            arrow.addPoint(x + getArrowWidth()/2, y1);
            arrow.addPoint(x + getArrowWidth(), y2);
            arrow.addPoint(x, y2);
            arrow.addPoint(x + getArrowWidth()/2, y1);
        } else {
            int y = height / 2 - getArrowWidth()/2;
            int x1 = (up ? ARROW_IND : barLength - ARROW_IND);
            int x2 = (up ? getArrowWidth() : barLength - getArrowWidth() - ARROW_IND);
            arrow.addPoint(x1, y + getArrowWidth()/2);
            arrow.addPoint(x2, y + getArrowWidth());
            arrow.addPoint(x2, y);
            arrow.addPoint(x1, y + getArrowWidth()/2);
        }
        return arrow;
    }
    protected abstract Rectangle getThumbArea();
    void paint(Graphics g, Color colors[], boolean paintAll) {
        if (log.isLoggable(PlatformLogger.FINER)) log.finer("Painting scrollbar " + this);
        boolean useBufferedImage = false;
        Graphics2D g2 = null;
        BufferedImage buffer = null;
        if (!(g instanceof Graphics2D)) {
            X11GraphicsConfig graphicsConfig = (X11GraphicsConfig)(sb.getEventSource().getGraphicsConfiguration());
            buffer = graphicsConfig.createCompatibleImage(width, height);
            g2 = buffer.createGraphics();
            useBufferedImage = true;
        } else {
            g2 = (Graphics2D)g;
        }
        try {
            Rectangle thumbRect = calculateThumbRect();
            prevThumb = thumbRect;
            Color back = colors[XComponentPeer.BACKGROUND_COLOR];
            Color selectColor = new Color(MotifColorUtilities.calculateSelectFromBackground(back.getRed(),back.getGreen(),back.getBlue()));
            Color darkShadow = new Color(MotifColorUtilities.calculateBottomShadowFromBackground(back.getRed(),back.getGreen(),back.getBlue()));
            Color lightShadow = new Color(MotifColorUtilities.calculateTopShadowFromBackground(back.getRed(),back.getGreen(),back.getBlue()));
            XToolkit.awtLock();
            try {
                XlibWrapper.XFlush(XToolkit.getDisplay());
            } finally {
                XToolkit.awtUnlock();
            }
            if (paintAll) {
                g2.setColor(selectColor);
                if (alignment == ALIGNMENT_HORIZONTAL) {
                    g2.fillRect(0, 0, thumbRect.x, height);
                    g2.fillRect(thumbRect.x + thumbRect.width , 0, width - (thumbRect.x + thumbRect.width), height);
                } else {
                    g2.fillRect(0, 0, width, thumbRect.y);
                    g2.fillRect(0, thumbRect.y + thumbRect.height, width, height - (thumbRect.y + thumbRect.height));
                }
                g2.setColor(darkShadow);
                g2.drawLine(0, 0, width-1, 0);           
                g2.drawLine(0, 0, 0, height-1);          
                g2.setColor(lightShadow);
                g2.drawLine(1, height-1, width-1, height-1); 
                g2.drawLine(width-1, 1, width-1, height-1);  
            } else {
                g2.setColor(selectColor);
                Rectangle thumbArea = getThumbArea();
                g2.fill(thumbArea);
            }
            if (paintAll) {
                 paintArrows(g2, colors[XComponentPeer.BACKGROUND_COLOR], darkShadow, lightShadow );
            }
            g2.setColor(colors[XComponentPeer.BACKGROUND_COLOR]);
            g2.fillRect(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
            g2.setColor(lightShadow);
            g2.drawLine(thumbRect.x, thumbRect.y,
                       thumbRect.x + thumbRect.width, thumbRect.y); 
            g2.drawLine(thumbRect.x, thumbRect.y,
                       thumbRect.x, thumbRect.y+thumbRect.height); 
            g2.setColor(darkShadow);
            g2.drawLine(thumbRect.x+1,
                       thumbRect.y+thumbRect.height,
                       thumbRect.x+thumbRect.width,
                       thumbRect.y+thumbRect.height);  
            g2.drawLine(thumbRect.x+thumbRect.width,
                       thumbRect.y+1,
                       thumbRect.x+thumbRect.width,
                       thumbRect.y+thumbRect.height); 
        } finally {
            if (useBufferedImage) {
                g2.dispose();
            }
        }
        if (useBufferedImage) {
            g.drawImage(buffer, 0, 0, null);
        }
        XToolkit.awtLock();
        try {
            XlibWrapper.XFlush(XToolkit.getDisplay());
        } finally {
            XToolkit.awtUnlock();
        }
    }
      void paintArrows(Graphics2D g, Color background, Color darkShadow, Color lightShadow) {
          g.setColor(background);
        if (pressed && (mode == AdjustmentEvent.UNIT_DECREMENT)) {
            g.fill(firstArrow);
            g.setColor(lightShadow);
            g.drawLine(firstArrow.xpoints[0],firstArrow.ypoints[0],
                    firstArrow.xpoints[1],firstArrow.ypoints[1]);
            g.drawLine(firstArrow.xpoints[1],firstArrow.ypoints[1],
                    firstArrow.xpoints[2],firstArrow.ypoints[2]);
            g.setColor(darkShadow);
            g.drawLine(firstArrow.xpoints[2],firstArrow.ypoints[2],
                    firstArrow.xpoints[0],firstArrow.ypoints[0]);
        }
        else {
            g.fill(firstArrow);
            g.setColor(darkShadow);
            g.drawLine(firstArrow.xpoints[0],firstArrow.ypoints[0],
                    firstArrow.xpoints[1],firstArrow.ypoints[1]);
            g.drawLine(firstArrow.xpoints[1],firstArrow.ypoints[1],
                    firstArrow.xpoints[2],firstArrow.ypoints[2]);
            g.setColor(lightShadow);
            g.drawLine(firstArrow.xpoints[2],firstArrow.ypoints[2],
                    firstArrow.xpoints[0],firstArrow.ypoints[0]);
        }
        g.setColor(background);
        if (pressed && (mode == AdjustmentEvent.UNIT_INCREMENT)) {
            g.fill(secondArrow);
            g.setColor(lightShadow);
            g.drawLine(secondArrow.xpoints[0],secondArrow.ypoints[0],
                    secondArrow.xpoints[1],secondArrow.ypoints[1]);
            g.setColor(darkShadow);
            g.drawLine(secondArrow.xpoints[1],secondArrow.ypoints[1],
                    secondArrow.xpoints[2],secondArrow.ypoints[2]);
            g.drawLine(secondArrow.xpoints[2],secondArrow.ypoints[2],
                    secondArrow.xpoints[0],secondArrow.ypoints[0]);
        }
        else {
            g.fill(secondArrow);
            g.setColor(darkShadow);
            g.drawLine(secondArrow.xpoints[0],secondArrow.ypoints[0],
                    secondArrow.xpoints[1],secondArrow.ypoints[1]);
            g.setColor(lightShadow);
            g.drawLine(secondArrow.xpoints[1],secondArrow.ypoints[1],
                    secondArrow.xpoints[2],secondArrow.ypoints[2]);
            g.drawLine(secondArrow.xpoints[2],secondArrow.ypoints[2],
                    secondArrow.xpoints[0],secondArrow.ypoints[0]);
        }
    }
    void startScrolling() {
        log.finer("Start scrolling on " + this);
        scroll();
        if (scroller == null) {
            scroller = new XScrollRepeater(this);
        } else {
            scroller.setScrollbar(this);
        }
        scroller.start();
    }
    void startScrollingInstance() {
        log.finer("Start scrolling on " + this);
        scroll();
        i_scroller.setScrollbar(this);
        i_scroller.start();
    }
    void stopScrollingInstance() {
        log.finer("Stop scrolling on " + this);
        i_scroller.stop();
    }
    public void setMode(int mode){
        this.mode = mode;
    }
    void scroll() {
        switch (mode) {
          case AdjustmentEvent.UNIT_DECREMENT:
              notifyValue(val - line);
              return;
          case AdjustmentEvent.UNIT_INCREMENT:
              notifyValue(val + line);
              return;
          case AdjustmentEvent.BLOCK_DECREMENT:
              notifyValue(val - page);
              return;
          case AdjustmentEvent.BLOCK_INCREMENT:
              notifyValue(val + page);
              return;
        }
        return;
    }
    boolean isInArrow(int x, int y) {
        int coord = (alignment == ALIGNMENT_HORIZONTAL ? x : y);
        int arrAreaH = getArrowAreaWidth();
        if (coord < arrAreaH || coord > barLength - arrAreaH + 1) {
            return true;
        }
        return false;
    }
    boolean isInThumb(int x, int y) {
        Rectangle thumbRect = calculateThumbRect();
        thumbRect.x -= 1;
        thumbRect.width += 3;
        thumbRect.height += 1;
        return thumbRect.contains(x,y);
    }
    abstract boolean beforeThumb(int x, int y);
    public void handleMouseEvent(int id, int modifiers, int x, int y) {
        if ((modifiers & InputEvent.BUTTON1_MASK) == 0) {
            return;
        }
        if (log.isLoggable(PlatformLogger.FINER)) {
             String type;
             switch (id) {
                case MouseEvent.MOUSE_PRESSED:
                    type = "press";
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    type = "release";
                    break;
                case MouseEvent.MOUSE_DRAGGED:
                    type = "drag";
                    break;
                default:
                    type = "other";
             }
             log.finer("Mouse " + type + " event in scroll bar " + this +
                                                   "x = " + x + ", y = " + y +
                                                   ", on arrow: " + isInArrow(x, y) +
                                                   ", on thumb: " + isInThumb(x, y) + ", before thumb: " + beforeThumb(x, y)
                                                   + ", thumb rect" + calculateThumbRect());
        }
        switch (id) {
          case MouseEvent.MOUSE_PRESSED:
              if (isInArrow(x, y)) {
                  pressed = true;
                  if (beforeThumb(x, y)) {
                      mode = AdjustmentEvent.UNIT_DECREMENT;
                  } else {
                      mode = AdjustmentEvent.UNIT_INCREMENT;
                  }
                  sb.repaintScrollbarRequest(this);
                  startScrolling();
                  break;
              }
              if (isInThumb(x, y)) {
                  mode = AdjustmentEvent.TRACK;
              } else {
                  if (beforeThumb(x, y)) {
                      mode = AdjustmentEvent.BLOCK_DECREMENT;
                  } else {
                      mode = AdjustmentEvent.BLOCK_INCREMENT;
                  }
                  startScrolling();
              }
              Rectangle pos = calculateThumbRect();
              thumbOffset = new Point(x - pos.x, y - pos.y);
              break;
          case MouseEvent.MOUSE_RELEASED:
              pressed = false;
              sb.repaintScrollbarRequest(this);
              scroller.stop();
              if(dragging){
                  handleTrackEvent(x, y, false);
                  dragging=false;
              }
              break;
          case MouseEvent.MOUSE_DRAGGED:
              dragging = true;
              handleTrackEvent(x, y, true);
        }
    }
    private void handleTrackEvent(int x, int y, boolean isAdjusting){
        if (mode == AdjustmentEvent.TRACK) {
            notifyValue(calculateCursorOffset(x, y), isAdjusting);
        }
    }
    private int calculateCursorOffset(int x, int y){
        if (alignment == ALIGNMENT_HORIZONTAL) {
            if (dragging)
                return Math.max(0,(int)((x - (thumbOffset.x + getArrowAreaWidth()))/getScaleFactor())) + min;
            else
                return Math.max(0,(int)((x - (getArrowAreaWidth()))/getScaleFactor())) + min;
        } else {
            if (dragging)
                return Math.max(0,(int)((y - (thumbOffset.y + getArrowAreaWidth()))/getScaleFactor())) + min;
            else
                return Math.max(0,(int)((y - (getArrowAreaWidth()))/getScaleFactor())) + min;
        }
    }
    synchronized void setValues(int value, int visible, int minimum, int maximum) {
        if (maximum <= minimum) {
            maximum = minimum + 1;
        }
        if (visible > maximum - minimum) {
            visible = maximum - minimum;
        }
        if (visible < 1) {
            visible = 1;
        }
        if (value < minimum) {
            value = minimum;
        }
        if (value > maximum - visible) {
            value = maximum - visible;
        }
        this.val = value;
        this.vis = visible;
        this.min = minimum;
        this.max = maximum;
    }
    synchronized void setValues(int value, int visible, int minimum, int maximum,
                                int unitSize, int blockSize) {
        setValues(value, visible, minimum, maximum);
        setUnitIncrement(unitSize);
        setBlockIncrement(blockSize);
    }
    int getValue() {
        return val;
    }
    synchronized void setValue(int newValue) {
        setValues(newValue, vis, min, max);
    }
    int getMinimum() {
        return min;
    }
    synchronized void setMinimum(int newMinimum) {
        setValues(val, vis, newMinimum, max);
    }
    int getMaximum() {
        return max;
    }
    synchronized void setMaximum(int newMaximum) {
        setValues(val, vis, min, newMaximum);
    }
    int getVisibleAmount() {
        return vis;
    }
    synchronized void setVisibleAmount(int newAmount) {
        setValues(val, newAmount, min, max);
    }
    synchronized void setUnitIncrement(int unitSize) {
        line = unitSize;
    }
    int getUnitIncrement() {
        return line;
    }
    synchronized void setBlockIncrement(int blockSize) {
        page = blockSize;
    }
    int getBlockIncrement() {
        return page;
    }
    int getArrowWidth() {
        return getArrowAreaWidth() - 2*ARROW_IND;
    }
    int getArrowAreaWidth() {
        return arrowArea;
    }
    void calculateArrowWidth() {
        if (barLength < 2*barWidth + MIN_THUMB_H + 2) {
            arrowArea = (barLength - MIN_THUMB_H + 2*ARROW_IND)/2 - 1;
        }
        else {
            arrowArea = barWidth - 1;
        }
    }
    private double getScaleFactor(){
        double f = (double)(barLength - 2*getArrowAreaWidth()) / Math.max(1,(max - min));
        return f;
    }
    protected Rectangle calculateThumbRect() {
        float range;
        float trueSize;  
        float factor;
        float slideSize;
        int minSliderWidth;
        int minSliderHeight;
        int hitTheWall = 0;
        int arrAreaH = getArrowAreaWidth();
        Rectangle retVal = new Rectangle(0,0,0,0);
        trueSize = barLength - 2*arrAreaH - 1;  
        if (alignment == ALIGNMENT_HORIZONTAL) {
            minSliderWidth = MIN_THUMB_H ;  
            minSliderHeight = height - 3;
        }
        else {  
            minSliderWidth = width - 3;
            minSliderHeight = MIN_THUMB_H ;
        }
            range = max - min;
            factor = trueSize / range;
            slideSize = vis * factor;
        if (alignment == ALIGNMENT_HORIZONTAL) {
            int localVal = (int) (slideSize + 0.5);
            int localMin = minSliderWidth;
            if (localVal > localMin) {
                retVal.width = localVal;
            }
            else {
                retVal.width = localMin;
                hitTheWall = localMin;
            }
            retVal.height = minSliderHeight;
        }
        else {  
            retVal.width = minSliderWidth;
            int localVal = (int) (slideSize + 0.5);
            int localMin = minSliderHeight;
            if (localVal > localMin) {
                retVal.height = localVal;
            }
            else {
                retVal.height = localMin;
                hitTheWall = localMin;
            }
        }
        if (hitTheWall != 0) {
            trueSize -= hitTheWall;  
            range -= vis;            
            factor = trueSize / range;
        }
        if (alignment == ALIGNMENT_HORIZONTAL) {
                    retVal.x = ((int) (((((float) val)
                        - ((float) min)) * factor) + 0.5))
                        + arrAreaH;
                    retVal.y = 1;
        }
        else {
            retVal.x = 1;
                    retVal.y = ((int) (((((float) val)
                        - ((float) min)) * factor) + 0.5))
                        + arrAreaH;
        }
        return retVal;
    }
    public String toString() {
        return getClass() + "[" + width + "x" + height + "," + barWidth + "x" + barLength + "]";
    }
}
class XScrollRepeater implements Runnable {
    static int beginPause = 500;
    static int repeatPause = 100;
    XScrollbar sb;
    boolean newScroll;
    boolean shouldSkip;
    XScrollRepeater(XScrollbar sb) {
        this.setScrollbar(sb);
        newScroll = true;
    }
    public void start() {
        stop();
        shouldSkip = false;
        XToolkit.schedule(this, beginPause);
    }
    public void stop() {
        synchronized(this) {
            shouldSkip = true;
        }
        XToolkit.remove(this);
    }
    public synchronized void setScrollbar(XScrollbar sb) {
        this.sb = sb;
        stop();
        newScroll = true;
    }
    public void run () {
        synchronized(this) {
            if (shouldSkip) {
                return;
            }
        }
        sb.scroll();
        XToolkit.schedule(this, repeatPause);
    }
}
