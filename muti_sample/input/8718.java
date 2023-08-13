public class SynthSliderUI extends BasicSliderUI
                           implements PropertyChangeListener, SynthUI {
    private Rectangle valueRect = new Rectangle();
    private boolean paintValue;
    private Dimension lastSize;
    private int trackHeight;
    private int trackBorder;
    private int thumbWidth;
    private int thumbHeight;
    private SynthStyle style;
    private SynthStyle sliderTrackStyle;
    private SynthStyle sliderThumbStyle;
    private transient boolean thumbActive; 
    private transient boolean thumbPressed; 
    public static ComponentUI createUI(JComponent c) {
        return new SynthSliderUI((JSlider)c);
    }
    protected SynthSliderUI(JSlider c) {
        super(c);
    }
    @Override
    protected void installDefaults(JSlider slider) {
        updateStyle(slider);
    }
    protected void uninstallDefaults(JSlider slider) {
        SynthContext context = getContext(slider, ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style = null;
        context = getContext(slider, Region.SLIDER_TRACK, ENABLED);
        sliderTrackStyle.uninstallDefaults(context);
        context.dispose();
        sliderTrackStyle = null;
        context = getContext(slider, Region.SLIDER_THUMB, ENABLED);
        sliderThumbStyle.uninstallDefaults(context);
        context.dispose();
        sliderThumbStyle = null;
    }
    @Override
    protected void installListeners(JSlider slider) {
        super.installListeners(slider);
        slider.addPropertyChangeListener(this);
    }
    @Override
    protected void uninstallListeners(JSlider slider) {
        slider.removePropertyChangeListener(this);
        super.uninstallListeners(slider);
    }
    private void updateStyle(JSlider c) {
        SynthContext context = getContext(c, ENABLED);
        SynthStyle oldStyle = style;
        style = SynthLookAndFeel.updateStyle(context, this);
        if (style != oldStyle) {
            thumbWidth =
                style.getInt(context, "Slider.thumbWidth", 30);
            thumbHeight =
                style.getInt(context, "Slider.thumbHeight", 14);
            String scaleKey = (String)slider.getClientProperty(
                    "JComponent.sizeVariant");
            if (scaleKey != null){
                if ("large".equals(scaleKey)){
                    thumbWidth *= 1.15;
                    thumbHeight *= 1.15;
                } else if ("small".equals(scaleKey)){
                    thumbWidth *= 0.857;
                    thumbHeight *= 0.857;
                } else if ("mini".equals(scaleKey)){
                    thumbWidth *= 0.784;
                    thumbHeight *= 0.784;
                }
            }
            trackBorder =
                style.getInt(context, "Slider.trackBorder", 1);
            trackHeight = thumbHeight + trackBorder * 2;
            paintValue = style.getBoolean(context,
                    "Slider.paintValue", true);
            if (oldStyle != null) {
                uninstallKeyboardActions(c);
                installKeyboardActions(c);
            }
        }
        context.dispose();
        context = getContext(c, Region.SLIDER_TRACK, ENABLED);
        sliderTrackStyle =
            SynthLookAndFeel.updateStyle(context, this);
        context.dispose();
        context = getContext(c, Region.SLIDER_THUMB, ENABLED);
        sliderThumbStyle =
            SynthLookAndFeel.updateStyle(context, this);
        context.dispose();
    }
    @Override
    protected TrackListener createTrackListener(JSlider s) {
        return new SynthTrackListener();
    }
    private void updateThumbState(int x, int y) {
        setThumbActive(thumbRect.contains(x, y));
    }
    private void updateThumbState(int x, int y, boolean pressed) {
        updateThumbState(x, y);
        setThumbPressed(pressed);
    }
    private void setThumbActive(boolean active) {
        if (thumbActive != active) {
            thumbActive = active;
            slider.repaint(thumbRect);
        }
    }
    private void setThumbPressed(boolean pressed) {
        if (thumbPressed != pressed) {
            thumbPressed = pressed;
            slider.repaint(thumbRect);
        }
    }
    @Override
    public int getBaseline(JComponent c, int width, int height) {
        if (c == null) {
            throw new NullPointerException("Component must be non-null");
        }
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException(
                    "Width and height must be >= 0");
        }
        if (slider.getPaintLabels() && labelsHaveSameBaselines()) {
            Insets trackInsets = new Insets(0, 0, 0, 0);
            SynthContext trackContext = getContext(slider,
                                                   Region.SLIDER_TRACK);
            style.getInsets(trackContext, trackInsets);
            trackContext.dispose();
            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                int valueHeight = 0;
                if (paintValue) {
                    SynthContext context = getContext(slider);
                    valueHeight = context.getStyle().getGraphicsUtils(context).
                            getMaximumCharHeight(context);
                    context.dispose();
                }
                int tickHeight = 0;
                if (slider.getPaintTicks()) {
                    tickHeight = getTickLength();
                }
                int labelHeight = getHeightOfTallestLabel();
                int contentHeight = valueHeight + trackHeight +
                        trackInsets.top + trackInsets.bottom +
                        tickHeight + labelHeight + 4;
                int centerY = height / 2 - contentHeight / 2;
                centerY += valueHeight + 2;
                centerY += trackHeight + trackInsets.top + trackInsets.bottom;
                centerY += tickHeight + 2;
                JComponent label = (JComponent) slider.getLabelTable().elements().nextElement();
                Dimension pref = label.getPreferredSize();
                return centerY + label.getBaseline(pref.width, pref.height);
            }
            else { 
                Integer value = slider.getInverted() ? getLowestValue() :
                                                       getHighestValue();
                if (value != null) {
                    int valueY = insetCache.top;
                    int valueHeight = 0;
                    if (paintValue) {
                        SynthContext context = getContext(slider);
                        valueHeight = context.getStyle().getGraphicsUtils(
                                context).getMaximumCharHeight(context);
                        context.dispose();
                    }
                    int contentHeight = height - insetCache.top -
                            insetCache.bottom;
                    int trackY = valueY + valueHeight;
                    int trackHeight = contentHeight - valueHeight;
                    int yPosition = yPositionForValue(value.intValue(), trackY,
                                                      trackHeight);
                    JComponent label = (JComponent) slider.getLabelTable().get(value);
                    Dimension pref = label.getPreferredSize();
                    return yPosition - pref.height / 2 +
                            label.getBaseline(pref.width, pref.height);
                }
            }
        }
        return -1;
    }
    @Override
    public Dimension getPreferredSize(JComponent c)  {
        recalculateIfInsetsChanged();
        Dimension d = new Dimension(contentRect.width, contentRect.height);
        if (slider.getOrientation() == JSlider.VERTICAL) {
            d.height = 200;
        } else {
            d.width = 200;
        }
        Insets i = slider.getInsets();
        d.width += i.left + i.right;
        d.height += i.top + i.bottom;
        return d;
    }
    @Override
    public Dimension getMinimumSize(JComponent c) {
        recalculateIfInsetsChanged();
        Dimension d = new Dimension(contentRect.width, contentRect.height);
        if (slider.getOrientation() == JSlider.VERTICAL) {
            d.height = thumbRect.height + insetCache.top + insetCache.bottom;
        } else {
            d.width = thumbRect.width + insetCache.left + insetCache.right;
        }
        return d;
    }
    @Override
    protected void calculateGeometry() {
        calculateThumbSize();
        layout();
        calculateThumbLocation();
    }
    protected void layout() {
        SynthContext context = getContext(slider);
        SynthGraphicsUtils synthGraphics = style.getGraphicsUtils(context);
        Insets trackInsets = new Insets(0, 0, 0, 0);
        SynthContext trackContext = getContext(slider, Region.SLIDER_TRACK);
        style.getInsets(trackContext, trackInsets);
        trackContext.dispose();
        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            valueRect.height = 0;
            if (paintValue) {
                valueRect.height =
                    synthGraphics.getMaximumCharHeight(context);
            }
            trackRect.height = trackHeight;
            tickRect.height = 0;
            if (slider.getPaintTicks()) {
                tickRect.height = getTickLength();
            }
            labelRect.height = 0;
            if (slider.getPaintLabels()) {
                labelRect.height = getHeightOfTallestLabel();
            }
            contentRect.height = valueRect.height + trackRect.height
                + trackInsets.top + trackInsets.bottom
                + tickRect.height + labelRect.height + 4;
            contentRect.width = slider.getWidth() - insetCache.left
                - insetCache.right;
            int pad = 0;
            if (slider.getPaintLabels()) {
                trackRect.x = insetCache.left;
                trackRect.width = contentRect.width;
                Dictionary dictionary = slider.getLabelTable();
                if (dictionary != null) {
                    int minValue = slider.getMinimum();
                    int maxValue = slider.getMaximum();
                    int firstLblIdx = Integer.MAX_VALUE;
                    int lastLblIdx = Integer.MIN_VALUE;
                    for (Enumeration keys = dictionary.keys();
                            keys.hasMoreElements(); ) {
                        int keyInt = ((Integer)keys.nextElement()).intValue();
                        if (keyInt >= minValue && keyInt < firstLblIdx) {
                            firstLblIdx = keyInt;
                        }
                        if (keyInt <= maxValue && keyInt > lastLblIdx) {
                            lastLblIdx = keyInt;
                        }
                    }
                    pad = getPadForLabel(firstLblIdx);
                    pad = Math.max(pad, getPadForLabel(lastLblIdx));
                }
            }
            valueRect.x = trackRect.x = tickRect.x = labelRect.x =
                (insetCache.left + pad);
            valueRect.width = trackRect.width = tickRect.width =
                labelRect.width = (contentRect.width - (pad * 2));
            int centerY = slider.getHeight() / 2 - contentRect.height / 2;
            valueRect.y = centerY;
            centerY += valueRect.height + 2;
            trackRect.y = centerY + trackInsets.top;
            centerY += trackRect.height + trackInsets.top + trackInsets.bottom;
            tickRect.y = centerY;
            centerY += tickRect.height + 2;
            labelRect.y = centerY;
            centerY += labelRect.height;
        } else {
            trackRect.width = trackHeight;
            tickRect.width = 0;
            if (slider.getPaintTicks()) {
                tickRect.width = getTickLength();
            }
            labelRect.width = 0;
            if (slider.getPaintLabels()) {
                labelRect.width = getWidthOfWidestLabel();
            }
            valueRect.y = insetCache.top;
            valueRect.height = 0;
            if (paintValue) {
                valueRect.height =
                    synthGraphics.getMaximumCharHeight(context);
            }
            FontMetrics fm = slider.getFontMetrics(slider.getFont());
            valueRect.width = Math.max(
                synthGraphics.computeStringWidth(context, slider.getFont(),
                    fm, "" + slider.getMaximum()),
                synthGraphics.computeStringWidth(context, slider.getFont(),
                    fm, "" + slider.getMinimum()));
            int l = valueRect.width / 2;
            int w1 = trackInsets.left + trackRect.width / 2;
            int w2 = trackRect.width / 2 + trackInsets.right +
                              tickRect.width + labelRect.width;
            contentRect.width = Math.max(w1, l) + Math.max(w2, l) +
                    2 + insetCache.left + insetCache.right;
            contentRect.height = slider.getHeight() -
                                    insetCache.top - insetCache.bottom;
            trackRect.y = tickRect.y = labelRect.y =
                valueRect.y + valueRect.height;
            trackRect.height = tickRect.height = labelRect.height =
                contentRect.height - valueRect.height;
            int startX = slider.getWidth() / 2 - contentRect.width / 2;
            if (SynthLookAndFeel.isLeftToRight(slider)) {
                if (l > w1) {
                    startX += (l - w1);
                }
                trackRect.x = startX + trackInsets.left;
                startX += trackInsets.left + trackRect.width + trackInsets.right;
                tickRect.x = startX;
                labelRect.x = startX + tickRect.width + 2;
            } else {
                if (l > w2) {
                    startX += (l - w2);
                }
                labelRect.x = startX;
                startX += labelRect.width + 2;
                tickRect.x = startX;
                trackRect.x = startX + tickRect.width + trackInsets.left;
            }
        }
        context.dispose();
        lastSize = slider.getSize();
    }
    private int getPadForLabel(int i) {
        int pad = 0;
        JComponent c = (JComponent) slider.getLabelTable().get(i);
        if (c != null) {
            int centerX = xPositionForValue(i);
            int cHalfWidth = c.getPreferredSize().width / 2;
            if (centerX - cHalfWidth < insetCache.left) {
                pad = Math.max(pad, insetCache.left - (centerX - cHalfWidth));
            }
            if (centerX + cHalfWidth > slider.getWidth() - insetCache.right) {
                pad = Math.max(pad, (centerX + cHalfWidth) -
                        (slider.getWidth() - insetCache.right));
            }
        }
        return pad;
    }
    @Override
    protected void calculateThumbLocation() {
        super.calculateThumbLocation();
        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            thumbRect.y += trackBorder;
        } else {
            thumbRect.x += trackBorder;
        }
        Point mousePosition = slider.getMousePosition();
        if(mousePosition != null) {
        updateThumbState(mousePosition.x, mousePosition.y);
       }
    }
    @Override
    public void setThumbLocation(int x, int y) {
        super.setThumbLocation(x, y);
        slider.repaint(valueRect.x, valueRect.y,
                valueRect.width, valueRect.height);
        setThumbActive(false);
    }
    @Override
    protected int xPositionForValue(int value) {
        int min = slider.getMinimum();
        int max = slider.getMaximum();
        int trackLeft = trackRect.x + thumbRect.width / 2 + trackBorder;
        int trackRight = trackRect.x + trackRect.width - thumbRect.width / 2
            - trackBorder;
        int trackLength = trackRight - trackLeft;
        double valueRange = (double)max - (double)min;
        double pixelsPerValue = (double)trackLength / valueRange;
        int xPosition;
        if (!drawInverted()) {
            xPosition = trackLeft;
            xPosition += Math.round( pixelsPerValue * ((double)value - min));
        } else {
            xPosition = trackRight;
            xPosition -= Math.round( pixelsPerValue * ((double)value - min));
        }
        xPosition = Math.max(trackLeft, xPosition);
        xPosition = Math.min(trackRight, xPosition);
        return xPosition;
    }
    @Override
    protected int yPositionForValue(int value, int trackY, int trackHeight) {
        int min = slider.getMinimum();
        int max = slider.getMaximum();
        int trackTop = trackY + thumbRect.height / 2 + trackBorder;
        int trackBottom = trackY + trackHeight - thumbRect.height / 2 -
                trackBorder;
        int trackLength = trackBottom - trackTop;
        double valueRange = (double)max - (double)min;
        double pixelsPerValue = (double)trackLength / valueRange;
        int yPosition;
        if (!drawInverted()) {
            yPosition = trackTop;
            yPosition += Math.round(pixelsPerValue * ((double)max - value));
        } else {
            yPosition = trackTop;
            yPosition += Math.round(pixelsPerValue * ((double)value - min));
        }
        yPosition = Math.max(trackTop, yPosition);
        yPosition = Math.min(trackBottom, yPosition);
        return yPosition;
    }
    @Override
    public int valueForYPosition(int yPos) {
        int value;
        int minValue = slider.getMinimum();
        int maxValue = slider.getMaximum();
        int trackTop = trackRect.y + thumbRect.height / 2 + trackBorder;
        int trackBottom = trackRect.y + trackRect.height
            - thumbRect.height / 2 - trackBorder;
        int trackLength = trackBottom - trackTop;
        if (yPos <= trackTop) {
            value = drawInverted() ? minValue : maxValue;
        } else if (yPos >= trackBottom) {
            value = drawInverted() ? maxValue : minValue;
        } else {
            int distanceFromTrackTop = yPos - trackTop;
            double valueRange = (double)maxValue - (double)minValue;
            double valuePerPixel = valueRange / (double)trackLength;
            int valueFromTrackTop =
                (int)Math.round(distanceFromTrackTop * valuePerPixel);
            value = drawInverted() ?
                minValue + valueFromTrackTop : maxValue - valueFromTrackTop;
        }
        return value;
    }
    @Override
    public int valueForXPosition(int xPos) {
        int value;
        int minValue = slider.getMinimum();
        int maxValue = slider.getMaximum();
        int trackLeft = trackRect.x + thumbRect.width / 2 + trackBorder;
        int trackRight = trackRect.x + trackRect.width
            - thumbRect.width / 2 - trackBorder;
        int trackLength = trackRight - trackLeft;
        if (xPos <= trackLeft) {
            value = drawInverted() ? maxValue : minValue;
        } else if (xPos >= trackRight) {
            value = drawInverted() ? minValue : maxValue;
        } else {
            int distanceFromTrackLeft = xPos - trackLeft;
            double valueRange = (double)maxValue - (double)minValue;
            double valuePerPixel = valueRange / (double)trackLength;
            int valueFromTrackLeft =
                (int)Math.round(distanceFromTrackLeft * valuePerPixel);
            value = drawInverted() ?
                maxValue - valueFromTrackLeft : minValue + valueFromTrackLeft;
        }
        return value;
    }
    @Override
    protected Dimension getThumbSize() {
        Dimension size = new Dimension();
        if (slider.getOrientation() == JSlider.VERTICAL) {
            size.width = thumbHeight;
            size.height = thumbWidth;
        } else {
            size.width = thumbWidth;
            size.height = thumbHeight;
        }
        return size;
    }
    @Override
    protected void recalculateIfInsetsChanged() {
        SynthContext context = getContext(slider);
        Insets newInsets = style.getInsets(context, null);
        Insets compInsets = slider.getInsets();
        newInsets.left += compInsets.left; newInsets.right += compInsets.right;
        newInsets.top += compInsets.top; newInsets.bottom += compInsets.bottom;
        if (!newInsets.equals(insetCache)) {
            insetCache = newInsets;
            calculateGeometry();
        }
        context.dispose();
    }
    @Override
    public SynthContext getContext(JComponent c) {
        return getContext(c, SynthLookAndFeel.getComponentState(c));
    }
    private SynthContext getContext(JComponent c, int state) {
        return SynthContext.getContext(SynthContext.class, c,
                            SynthLookAndFeel.getRegion(c), style, state);
    }
    private SynthContext getContext(JComponent c, Region subregion) {
        return getContext(c, subregion, getComponentState(c, subregion));
    }
    private SynthContext getContext(JComponent c, Region subregion, int state) {
        SynthStyle style = null;
        Class klass = SynthContext.class;
        if (subregion == Region.SLIDER_TRACK) {
            style = sliderTrackStyle;
        } else if (subregion == Region.SLIDER_THUMB) {
            style = sliderThumbStyle;
        }
        return SynthContext.getContext(klass, c, subregion, style, state);
    }
    private int getComponentState(JComponent c, Region region) {
        if (region == Region.SLIDER_THUMB && thumbActive &&c.isEnabled()) {
            int state = thumbPressed ? PRESSED : MOUSE_OVER;
            if (c.isFocusOwner()) state |= FOCUSED;
            return state;
        }
        return SynthLookAndFeel.getComponentState(c);
    }
    @Override
    public void update(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        SynthLookAndFeel.update(context, g);
        context.getPainter().paintSliderBackground(context,
                          g, 0, 0, c.getWidth(), c.getHeight(),
                          slider.getOrientation());
        paint(context, g);
        context.dispose();
    }
    @Override
    public void paint(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        paint(context, g);
        context.dispose();
    }
    protected void paint(SynthContext context, Graphics g) {
        recalculateIfInsetsChanged();
        recalculateIfOrientationChanged();
        Rectangle clip = g.getClipBounds();
        if (lastSize == null || !lastSize.equals(slider.getSize())) {
            calculateGeometry();
        }
        if (paintValue) {
            FontMetrics fm = SwingUtilities2.getFontMetrics(slider, g);
            int labelWidth = context.getStyle().getGraphicsUtils(context).
                computeStringWidth(context, g.getFont(), fm,
                    "" + slider.getValue());
            valueRect.x = thumbRect.x + (thumbRect.width - labelWidth) / 2;
            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                if (valueRect.x + labelWidth > insetCache.left + contentRect.width) {
                    valueRect.x =  (insetCache.left + contentRect.width) - labelWidth;
                }
                valueRect.x = Math.max(valueRect.x, 0);
            }
            g.setColor(context.getStyle().getColor(
                    context, ColorType.TEXT_FOREGROUND));
            context.getStyle().getGraphicsUtils(context).paintText(
                    context, g, "" + slider.getValue(), valueRect.x,
                    valueRect.y, -1);
        }
        if (slider.getPaintTrack() && clip.intersects(trackRect)) {
            SynthContext subcontext = getContext(slider, Region.SLIDER_TRACK);
            paintTrack(subcontext, g, trackRect);
            subcontext.dispose();
        }
        if (clip.intersects(thumbRect)) {
            SynthContext subcontext = getContext(slider, Region.SLIDER_THUMB);
            paintThumb(subcontext, g, thumbRect);
            subcontext.dispose();
        }
        if (slider.getPaintTicks() && clip.intersects(tickRect)) {
            paintTicks(g);
        }
        if (slider.getPaintLabels() && clip.intersects(labelRect)) {
            paintLabels(g);
        }
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintSliderBorder(context, g, x, y, w, h,
                                               slider.getOrientation());
    }
    protected void paintThumb(SynthContext context, Graphics g,
            Rectangle thumbBounds)  {
        int orientation = slider.getOrientation();
        SynthLookAndFeel.updateSubregion(context, g, thumbBounds);
        context.getPainter().paintSliderThumbBackground(context, g,
                             thumbBounds.x, thumbBounds.y, thumbBounds.width,
                             thumbBounds.height, orientation);
        context.getPainter().paintSliderThumbBorder(context, g,
                             thumbBounds.x, thumbBounds.y, thumbBounds.width,
                             thumbBounds.height, orientation);
    }
    protected void paintTrack(SynthContext context, Graphics g,
            Rectangle trackBounds) {
        int orientation = slider.getOrientation();
        SynthLookAndFeel.updateSubregion(context, g, trackBounds);
        context.getPainter().paintSliderTrackBackground(context, g,
                trackBounds.x, trackBounds.y, trackBounds.width,
                trackBounds.height, orientation);
        context.getPainter().paintSliderTrackBorder(context, g,
                trackBounds.x, trackBounds.y, trackBounds.width,
                trackBounds.height, orientation);
    }
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (SynthLookAndFeel.shouldUpdateStyle(e)) {
            updateStyle((JSlider)e.getSource());
        }
    }
    private class SynthTrackListener extends TrackListener {
        @Override public void mouseExited(MouseEvent e) {
            setThumbActive(false);
        }
        @Override public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            setThumbPressed(thumbRect.contains(e.getX(), e.getY()));
        }
        @Override public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            updateThumbState(e.getX(), e.getY(), false);
        }
        @Override public void mouseDragged(MouseEvent e) {
            int thumbMiddle;
            if (!slider.isEnabled()) {
                return;
            }
            currentMouseX = e.getX();
            currentMouseY = e.getY();
            if (!isDragging()) {
                return;
            }
            slider.setValueIsAdjusting(true);
            switch (slider.getOrientation()) {
            case JSlider.VERTICAL:
                int halfThumbHeight = thumbRect.height / 2;
                int thumbTop = e.getY() - offset;
                int trackTop = trackRect.y;
                int trackBottom = trackRect.y + trackRect.height
                    - halfThumbHeight - trackBorder;
                int vMax = yPositionForValue(slider.getMaximum() -
                    slider.getExtent());
                if (drawInverted()) {
                    trackBottom = vMax;
                    trackTop = trackTop + halfThumbHeight;
                } else {
                    trackTop = vMax;
                }
                thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);
                setThumbLocation(thumbRect.x, thumbTop);
                thumbMiddle = thumbTop + halfThumbHeight;
                slider.setValue(valueForYPosition(thumbMiddle));
                break;
            case JSlider.HORIZONTAL:
                int halfThumbWidth = thumbRect.width / 2;
                int thumbLeft = e.getX() - offset;
                int trackLeft = trackRect.x + halfThumbWidth + trackBorder;
                int trackRight = trackRect.x + trackRect.width
                    - halfThumbWidth - trackBorder;
                int hMax = xPositionForValue(slider.getMaximum() -
                    slider.getExtent());
                if (drawInverted()) {
                    trackLeft = hMax;
                } else {
                    trackRight = hMax;
                }
                thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);
                setThumbLocation(thumbLeft, thumbRect.y);
                thumbMiddle = thumbLeft + halfThumbWidth;
                slider.setValue(valueForXPosition(thumbMiddle));
                break;
            default:
                return;
            }
            if (slider.getValueIsAdjusting()) {
                setThumbActive(true);
            }
        }
        @Override public void mouseMoved(MouseEvent e) {
            updateThumbState(e.getX(), e.getY());
        }
    }
}
