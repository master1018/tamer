public class MotifSliderUI extends BasicSliderUI {
    static final Dimension PREFERRED_HORIZONTAL_SIZE = new Dimension(164, 15);
    static final Dimension PREFERRED_VERTICAL_SIZE = new Dimension(15, 164);
    static final Dimension MINIMUM_HORIZONTAL_SIZE = new Dimension(43, 15);
    static final Dimension MINIMUM_VERTICAL_SIZE = new Dimension(15, 43);
    public MotifSliderUI(JSlider b)   {
        super(b);
    }
    public static ComponentUI createUI(JComponent b)    {
        return new MotifSliderUI((JSlider)b);
    }
    public Dimension getPreferredHorizontalSize() {
        return PREFERRED_HORIZONTAL_SIZE;
    }
    public Dimension getPreferredVerticalSize() {
        return PREFERRED_VERTICAL_SIZE;
    }
    public Dimension getMinimumHorizontalSize() {
        return MINIMUM_HORIZONTAL_SIZE;
    }
    public Dimension getMinimumVerticalSize() {
        return MINIMUM_VERTICAL_SIZE;
    }
    protected Dimension getThumbSize() {
        if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
            return new Dimension( 30, 15 );
        }
        else {
            return new Dimension( 15, 30 );
        }
    }
    public void paintFocus(Graphics g)  {
    }
    public void paintTrack(Graphics g)  {
    }
    public void paintThumb(Graphics g)  {
        Rectangle knobBounds = thumbRect;
        int x = knobBounds.x;
        int y = knobBounds.y;
        int w = knobBounds.width;
        int h = knobBounds.height;
        if ( slider.isEnabled() ) {
            g.setColor(slider.getForeground());
        }
        else {
            g.setColor(slider.getForeground().darker());
        }
        if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
            g.translate(x, knobBounds.y-1);
            g.fillRect(0, 1, w, h - 1);
            g.setColor(getHighlightColor());
            g.drawLine(0, 1, w - 1, 1);             
            g.drawLine(0, 1, 0, h);                     
            g.drawLine(w/2, 2, w/2, h-1);       
            g.setColor(getShadowColor());
            g.drawLine(0, h, w - 1, h);         
            g.drawLine(w - 1, 1, w - 1, h);     
            g.drawLine(w/2 - 1, 2, w/2 - 1, h); 
            g.translate(-x, -(knobBounds.y-1));
        }
        else {
            g.translate(knobBounds.x-1, 0);
            g.fillRect(1, y, w - 1, h);
            g.setColor(getHighlightColor());
            g.drawLine(1, y, w, y);                     
            g.drawLine(1, y+1, 1, y+h-1);               
            g.drawLine(2, y+h/2, w-1, y+h/2);           
            g.setColor(getShadowColor());
            g.drawLine(2, y+h-1, w, y+h-1);             
            g.drawLine(w, y+h-1, w, y);                 
            g.drawLine(2, y+h/2-1, w-1, y+h/2-1);       
            g.translate(-(knobBounds.x-1), 0);
        }
    }
}
