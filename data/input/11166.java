class LoweredBorder extends AbstractRegionPainter implements Border {
    private static final int IMG_SIZE = 30;
    private static final int RADIUS = 13;
    private static final Insets INSETS = new Insets(10,10,10,10);
    private static final PaintContext PAINT_CONTEXT = new PaintContext(INSETS,
            new Dimension(IMG_SIZE,IMG_SIZE),false,
            PaintContext.CacheMode.NINE_SQUARE_SCALE,
            Integer.MAX_VALUE, Integer.MAX_VALUE);
    @Override
    protected Object[] getExtendedCacheKeys(JComponent c) {
        return (c != null)
                ? new Object[] { c.getBackground() }
                : null;
    }
    protected void doPaint(Graphics2D g, JComponent c, int width, int height,
            Object[] extendedCacheKeys) {
        Color color = (c == null) ? Color.BLACK : c.getBackground();
        BufferedImage img1 = new BufferedImage(IMG_SIZE,IMG_SIZE,
                    BufferedImage.TYPE_INT_ARGB);
        BufferedImage img2 = new BufferedImage(IMG_SIZE,IMG_SIZE,
                    BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D)img1.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillRoundRect(2,0,26,26,RADIUS,RADIUS);
        g2.dispose();
        InnerShadowEffect effect = new InnerShadowEffect();
        effect.setDistance(1);
        effect.setSize(3);
        effect.setColor(getLighter(color, 2.1f));
        effect.setAngle(90);
        effect.applyEffect(img1,img2,IMG_SIZE,IMG_SIZE);
        g2 = (Graphics2D)img2.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(0,28,IMG_SIZE,1);
        g2.setColor(getLighter(color, 0.90f));
        g2.drawRoundRect(2,1,25,25,RADIUS,RADIUS);
        g2.dispose();
        if (width != IMG_SIZE || height != IMG_SIZE){
            ImageScalingHelper.paint(g,0,0,width,height,img2, INSETS, INSETS,
                    ImageScalingHelper.PaintType.PAINT9_STRETCH,
                    ImageScalingHelper.PAINT_ALL);
        } else {
            g.drawImage(img2,0,0,c);
        }
        img1 = null;
        img2 = null;
    }
    protected PaintContext getPaintContext() {
        return PAINT_CONTEXT;
    }
    public Insets getBorderInsets(Component c) {
        return (Insets) INSETS.clone();
    }
    public boolean isBorderOpaque() {
        return false;
    }
    public void paintBorder(Component c, Graphics g, int x, int y, int width,
                            int height) {
        JComponent comp = (c instanceof JComponent)?(JComponent)c:null;
        if (g instanceof Graphics2D){
            Graphics2D g2 = (Graphics2D)g;
            g2.translate(x,y);
            paint(g2,comp, width, height);
            g2.translate(-x,-y);
        } else {
            BufferedImage img =  new BufferedImage(IMG_SIZE,IMG_SIZE,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = (Graphics2D)img.getGraphics();
            paint(g2,comp, width, height);
            g2.dispose();
            ImageScalingHelper.paint(g,x,y,width,height,img,INSETS, INSETS,
                    ImageScalingHelper.PaintType.PAINT9_STRETCH,
                    ImageScalingHelper.PAINT_ALL);
        }
    }
    private Color getLighter(Color c, float factor){
        return new Color(Math.min((int)(c.getRed()/factor), 255),
                         Math.min((int)(c.getGreen()/factor), 255),
                         Math.min((int)(c.getBlue()/factor), 255));
    }
}
