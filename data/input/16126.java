final class ToolBarSeparatorPainter extends AbstractRegionPainter {
    private static final int SPACE = 3;
    private static final int INSET = 2;
    @Override
    protected PaintContext getPaintContext() {
        return new PaintContext(
                new Insets(1, 0, 1, 0),
                new Dimension(38, 7),
                false, CacheMode.NO_CACHING, 1, 1);
    }
    @Override
    protected void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] extendedCacheKeys) {
        g.setColor(c.getForeground());
        int y = height / 2;
        for (int i=INSET; i<=width-INSET; i+=SPACE) {
            g.fillRect(i, y, 1, 1);
        }
    }
}
