public class RegionSpanIterator implements SpanIterator {
    RegionIterator ri;
    int lox, loy, hix, hiy;
    int curloy, curhiy;
    boolean done = false;
    boolean isrect;
    public RegionSpanIterator(Region r) {
        int[] bounds = new int[4];
        r.getBounds(bounds);
        lox = bounds[0];
        loy = bounds[1];
        hix = bounds[2];
        hiy = bounds[3];
        isrect = r.isRectangular();
        ri = r.getIterator();
    }
    public void getPathBox(int pathbox[]) {
        pathbox[0] = lox;
        pathbox[1] = loy;
        pathbox[2] = hix;
        pathbox[3] = hiy;
    }
    public void intersectClipBox(int clox, int cloy, int chix, int chiy) {
        if (clox > lox) {
            lox = clox;
        }
        if (cloy > loy) {
            loy = cloy;
        }
        if (chix < hix) {
            hix = chix;
        }
        if (chiy < hiy) {
            hiy = chiy;
        }
        done = lox >= hix || loy >= hiy;
    }
    public boolean nextSpan(int spanbox[]) {
        if (done) {
            return false;
        }
        if (isrect) {
            getPathBox(spanbox);
            done = true;
            return true;
        }
        int curlox, curhix;
        int curloy = this.curloy;
        int curhiy = this.curhiy;
        while (true) {
            if (!ri.nextXBand(spanbox)) {
                if (!ri.nextYRange(spanbox)) {
                    done = true;
                    return false;
                }
                curloy = spanbox[1];
                curhiy = spanbox[3];
                if (curloy < loy) {
                    curloy = loy;
                }
                if (curhiy > hiy) {
                    curhiy = hiy;
                }
                if (curloy >= hiy) {
                    done = true;
                    return false;
                }
                continue;
            }
            curlox = spanbox[0];
            curhix = spanbox[2];
            if (curlox < lox) {
                curlox = lox;
            }
            if (curhix > hix) {
                curhix = hix;
            }
            if (curlox < curhix && curloy < curhiy) {
                break;
            }
        }
        spanbox[0] = curlox;
        spanbox[1] = this.curloy = curloy;
        spanbox[2] = curhix;
        spanbox[3] = this.curhiy = curhiy;
        return true;
    }
    public void skipDownTo(int y) {
        loy = y;
    }
    public long getNativeIterator() {
        return 0;
    }
}
