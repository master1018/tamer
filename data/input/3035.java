public class MaskTile {
    GrowableRectArray rects;
    DirtyRegion dirtyArea;
    public MaskTile()
    {
        rects = new GrowableRectArray(128);
        dirtyArea = new DirtyRegion();
    }
    public void addRect(int x, int y, int width, int height) {
        int index = rects.getNextIndex();
        rects.setX(index, x);
        rects.setY(index, y);
        rects.setWidth(index, width);
        rects.setHeight(index, height);
    }
    public void addLine(int x1, int y1, int x2, int y2) {
        DirtyRegion region = new DirtyRegion();
        region.setDirtyLineRegion(x1, y1, x2, y2);
        int xDiff = region.x2 - region.x;
        int yDiff = region.y2 - region.y;
        if (xDiff == 0 || yDiff == 0) {
            addRect(region.x, region.y,
                    region.x2 - region.x + 1, region.y2 - region.y + 1);
        } else if (xDiff == 1 && yDiff == 1) {
            addRect(x1, y1, 1, 1);
            addRect(x2, y2, 1, 1);
        } else {
            lineToRects(x1, y1, x2, y2);
        }
    }
    private void lineToRects(int xstart, int ystart, int xend, int yend) {
        int x, y, t, dx, dy, incx, incy, pdx, pdy, ddx, ddy, es, el, err;
        dx = xend - xstart;
        dy = yend - ystart;
        incx = dx > 0 ? 1 : (dx < 0) ? -1 : 0;
        incy = dy > 0 ? 1 : (dy < 0) ? -1 : 0;
        if (dx < 0)
            dx = -dx;
        if (dy < 0)
            dy = -dy;
        if (dx > dy) {
            pdx = incx;
            pdy = 0; 
            ddx = incx;
            ddy = incy; 
            es = dy;
            el = dx; 
        } else {
            pdx = 0;
            pdy = incy; 
            ddx = incx;
            ddy = incy; 
            es = dx;
            el = dy; 
        }
        x = xstart;
        y = ystart;
        err = el / 2;
        addRect(x, y, 1, 1);
        for (t = 0; t < el; ++t) 
        {
            err -= es;
            if (err < 0) {
                err += el;
                x += ddx;
                y += ddy;
            } else {
                x += pdx;
                y += pdy;
            }
            addRect(x, y, 1, 1);
        }
    }
    public void calculateDirtyAreas()
    {
        for (int i=0; i < rects.getSize(); i++) {
            int x = rects.getX(i);
            int y = rects.getY(i);
            dirtyArea.growDirtyRegion(x, y,
                                      x + rects.getWidth(i),
                                      y + rects.getHeight(i));
        }
    }
    public void reset() {
        rects.clear();
        dirtyArea.clear();
    }
    public void translate(int x, int y) {
        if (rects.getSize() > 0) {
            dirtyArea.translate(x, y);
        }
        rects.translateRects(x, y);
    }
    public GrowableRectArray getRects() {
        return rects;
    }
    public DirtyRegion getDirtyArea() {
        return dirtyArea;
    }
}
