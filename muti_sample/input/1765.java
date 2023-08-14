final class LinearGradientPaintContext extends MultipleGradientPaintContext {
    private float dgdX, dgdY, gc;
    LinearGradientPaintContext(LinearGradientPaint paint,
                               ColorModel cm,
                               Rectangle deviceBounds,
                               Rectangle2D userBounds,
                               AffineTransform t,
                               RenderingHints hints,
                               Point2D start,
                               Point2D end,
                               float[] fractions,
                               Color[] colors,
                               CycleMethod cycleMethod,
                               ColorSpaceType colorSpace)
    {
        super(paint, cm, deviceBounds, userBounds, t, hints, fractions,
              colors, cycleMethod, colorSpace);
        float startx = (float)start.getX();
        float starty = (float)start.getY();
        float endx = (float)end.getX();
        float endy = (float)end.getY();
        float dx = endx - startx;  
        float dy = endy - starty;  
        float dSq = dx*dx + dy*dy; 
        float constX = dx/dSq;
        float constY = dy/dSq;
        dgdX = a00*constX + a10*constY;
        dgdY = a01*constX + a11*constY;
        gc = (a02-startx)*constX + (a12-starty)*constY;
    }
    protected void fillRaster(int[] pixels, int off, int adjust,
                              int x, int y, int w, int h)
    {
        float g = 0;
        int rowLimit = off + w;
        float initConst = (dgdX*x) + gc;
        for (int i = 0; i < h; i++) { 
            g = initConst + dgdY*(y+i);
            while (off < rowLimit) { 
                pixels[off++] = indexIntoGradientsArrays(g);
                g += dgdX;
            }
            off += adjust;
            rowLimit = off + w;
        }
    }
}
