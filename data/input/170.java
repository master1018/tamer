final class Helpers {
    private Helpers() {
        throw new Error("This is a non instantiable class");
    }
    static boolean within(final float x, final float y, final float err) {
        final float d = y - x;
        return (d <= err && d >= -err);
    }
    static boolean within(final double x, final double y, final double err) {
        final double d = y - x;
        return (d <= err && d >= -err);
    }
    static int quadraticRoots(final float a, final float b,
                              final float c, float[] zeroes, final int off)
    {
        int ret = off;
        float t;
        if (a != 0f) {
            final float dis = b*b - 4*a*c;
            if (dis > 0) {
                final float sqrtDis = (float)Math.sqrt(dis);
                if (b >= 0) {
                    zeroes[ret++] = (2 * c) / (-b - sqrtDis);
                    zeroes[ret++] = (-b - sqrtDis) / (2 * a);
                } else {
                    zeroes[ret++] = (-b + sqrtDis) / (2 * a);
                    zeroes[ret++] = (2 * c) / (-b + sqrtDis);
                }
            } else if (dis == 0f) {
                t = (-b) / (2 * a);
                zeroes[ret++] = t;
            }
        } else {
            if (b != 0f) {
                t = (-c) / b;
                zeroes[ret++] = t;
            }
        }
        return ret - off;
    }
    static int cubicRootsInAB(float d, float a, float b, float c,
                              float[] pts, final int off,
                              final float A, final float B)
    {
        if (d == 0) {
            int num = quadraticRoots(a, b, c, pts, off);
            return filterOutNotInAB(pts, off, num, A, B) - off;
        }
        a /= d;
        b /= d;
        c /= d;
        double sq_A = a * a;
        double p = 1.0/3 * (-1.0/3 * sq_A + b);
        double q = 1.0/2 * (2.0/27 * a * sq_A - 1.0/3 * a * b + c);
        double cb_p = p * p * p;
        double D = q * q + cb_p;
        int num;
        if (D < 0) {
            final double phi = 1.0/3 * acos(-q / sqrt(-cb_p));
            final double t = 2 * sqrt(-p);
            pts[ off+0 ] =  (float)( t * cos(phi));
            pts[ off+1 ] =  (float)(-t * cos(phi + PI / 3));
            pts[ off+2 ] =  (float)(-t * cos(phi - PI / 3));
            num = 3;
        } else {
            final double sqrt_D = sqrt(D);
            final double u = cbrt(sqrt_D - q);
            final double v = - cbrt(sqrt_D + q);
            pts[ off ] = (float)(u + v);
            num = 1;
            if (within(D, 0, 1e-8)) {
                pts[off+1] = -(pts[off] / 2);
                num = 2;
            }
        }
        final float sub = 1.0f/3 * a;
        for (int i = 0; i < num; ++i) {
            pts[ off+i ] -= sub;
        }
        return filterOutNotInAB(pts, off, num, A, B) - off;
    }
    static float[] widenArray(float[] in, final int cursize, final int numToAdd) {
        if (in.length >= cursize + numToAdd) {
            return in;
        }
        return Arrays.copyOf(in, 2 * (cursize + numToAdd));
    }
    static int[] widenArray(int[] in, final int cursize, final int numToAdd) {
        if (in.length >= cursize + numToAdd) {
            return in;
        }
        return Arrays.copyOf(in, 2 * (cursize + numToAdd));
    }
    static float evalCubic(final float a, final float b,
                           final float c, final float d,
                           final float t)
    {
        return t * (t * (t * a + b) + c) + d;
    }
    static float evalQuad(final float a, final float b,
                          final float c, final float t)
    {
        return t * (t * a + b) + c;
    }
    static int filterOutNotInAB(float[] nums, final int off, final int len,
                                final float a, final float b)
    {
        int ret = off;
        for (int i = off; i < off + len; i++) {
            if (nums[i] >= a && nums[i] < b) {
                nums[ret++] = nums[i];
            }
        }
        return ret;
    }
    static float polyLineLength(float[] poly, final int off, final int nCoords) {
        assert nCoords % 2 == 0 && poly.length >= off + nCoords : "";
        float acc = 0;
        for (int i = off + 2; i < off + nCoords; i += 2) {
            acc += linelen(poly[i], poly[i+1], poly[i-2], poly[i-1]);
        }
        return acc;
    }
    static float linelen(float x1, float y1, float x2, float y2) {
        final float dx = x2 - x1;
        final float dy = y2 - y1;
        return (float)Math.sqrt(dx*dx + dy*dy);
    }
    static void subdivide(float[] src, int srcoff, float[] left, int leftoff,
                          float[] right, int rightoff, int type)
    {
        switch(type) {
        case 6:
            Helpers.subdivideQuad(src, srcoff, left, leftoff, right, rightoff);
            break;
        case 8:
            Helpers.subdivideCubic(src, srcoff, left, leftoff, right, rightoff);
            break;
        default:
            throw new InternalError("Unsupported curve type");
        }
    }
    static void isort(float[] a, int off, int len) {
        for (int i = off + 1; i < off + len; i++) {
            float ai = a[i];
            int j = i - 1;
            for (; j >= off && a[j] > ai; j--) {
                a[j+1] = a[j];
            }
            a[j+1] = ai;
        }
    }
    static void subdivideCubic(float src[], int srcoff,
                               float left[], int leftoff,
                               float right[], int rightoff)
    {
        float x1 = src[srcoff + 0];
        float y1 = src[srcoff + 1];
        float ctrlx1 = src[srcoff + 2];
        float ctrly1 = src[srcoff + 3];
        float ctrlx2 = src[srcoff + 4];
        float ctrly2 = src[srcoff + 5];
        float x2 = src[srcoff + 6];
        float y2 = src[srcoff + 7];
        if (left != null) {
            left[leftoff + 0] = x1;
            left[leftoff + 1] = y1;
        }
        if (right != null) {
            right[rightoff + 6] = x2;
            right[rightoff + 7] = y2;
        }
        x1 = (x1 + ctrlx1) / 2.0f;
        y1 = (y1 + ctrly1) / 2.0f;
        x2 = (x2 + ctrlx2) / 2.0f;
        y2 = (y2 + ctrly2) / 2.0f;
        float centerx = (ctrlx1 + ctrlx2) / 2.0f;
        float centery = (ctrly1 + ctrly2) / 2.0f;
        ctrlx1 = (x1 + centerx) / 2.0f;
        ctrly1 = (y1 + centery) / 2.0f;
        ctrlx2 = (x2 + centerx) / 2.0f;
        ctrly2 = (y2 + centery) / 2.0f;
        centerx = (ctrlx1 + ctrlx2) / 2.0f;
        centery = (ctrly1 + ctrly2) / 2.0f;
        if (left != null) {
            left[leftoff + 2] = x1;
            left[leftoff + 3] = y1;
            left[leftoff + 4] = ctrlx1;
            left[leftoff + 5] = ctrly1;
            left[leftoff + 6] = centerx;
            left[leftoff + 7] = centery;
        }
        if (right != null) {
            right[rightoff + 0] = centerx;
            right[rightoff + 1] = centery;
            right[rightoff + 2] = ctrlx2;
            right[rightoff + 3] = ctrly2;
            right[rightoff + 4] = x2;
            right[rightoff + 5] = y2;
        }
    }
    static void subdivideCubicAt(float t, float src[], int srcoff,
                                 float left[], int leftoff,
                                 float right[], int rightoff)
    {
        float x1 = src[srcoff + 0];
        float y1 = src[srcoff + 1];
        float ctrlx1 = src[srcoff + 2];
        float ctrly1 = src[srcoff + 3];
        float ctrlx2 = src[srcoff + 4];
        float ctrly2 = src[srcoff + 5];
        float x2 = src[srcoff + 6];
        float y2 = src[srcoff + 7];
        if (left != null) {
            left[leftoff + 0] = x1;
            left[leftoff + 1] = y1;
        }
        if (right != null) {
            right[rightoff + 6] = x2;
            right[rightoff + 7] = y2;
        }
        x1 = x1 + t * (ctrlx1 - x1);
        y1 = y1 + t * (ctrly1 - y1);
        x2 = ctrlx2 + t * (x2 - ctrlx2);
        y2 = ctrly2 + t * (y2 - ctrly2);
        float centerx = ctrlx1 + t * (ctrlx2 - ctrlx1);
        float centery = ctrly1 + t * (ctrly2 - ctrly1);
        ctrlx1 = x1 + t * (centerx - x1);
        ctrly1 = y1 + t * (centery - y1);
        ctrlx2 = centerx + t * (x2 - centerx);
        ctrly2 = centery + t * (y2 - centery);
        centerx = ctrlx1 + t * (ctrlx2 - ctrlx1);
        centery = ctrly1 + t * (ctrly2 - ctrly1);
        if (left != null) {
            left[leftoff + 2] = x1;
            left[leftoff + 3] = y1;
            left[leftoff + 4] = ctrlx1;
            left[leftoff + 5] = ctrly1;
            left[leftoff + 6] = centerx;
            left[leftoff + 7] = centery;
        }
        if (right != null) {
            right[rightoff + 0] = centerx;
            right[rightoff + 1] = centery;
            right[rightoff + 2] = ctrlx2;
            right[rightoff + 3] = ctrly2;
            right[rightoff + 4] = x2;
            right[rightoff + 5] = y2;
        }
    }
    static void subdivideQuad(float src[], int srcoff,
                              float left[], int leftoff,
                              float right[], int rightoff)
    {
        float x1 = src[srcoff + 0];
        float y1 = src[srcoff + 1];
        float ctrlx = src[srcoff + 2];
        float ctrly = src[srcoff + 3];
        float x2 = src[srcoff + 4];
        float y2 = src[srcoff + 5];
        if (left != null) {
            left[leftoff + 0] = x1;
            left[leftoff + 1] = y1;
        }
        if (right != null) {
            right[rightoff + 4] = x2;
            right[rightoff + 5] = y2;
        }
        x1 = (x1 + ctrlx) / 2.0f;
        y1 = (y1 + ctrly) / 2.0f;
        x2 = (x2 + ctrlx) / 2.0f;
        y2 = (y2 + ctrly) / 2.0f;
        ctrlx = (x1 + x2) / 2.0f;
        ctrly = (y1 + y2) / 2.0f;
        if (left != null) {
            left[leftoff + 2] = x1;
            left[leftoff + 3] = y1;
            left[leftoff + 4] = ctrlx;
            left[leftoff + 5] = ctrly;
        }
        if (right != null) {
            right[rightoff + 0] = ctrlx;
            right[rightoff + 1] = ctrly;
            right[rightoff + 2] = x2;
            right[rightoff + 3] = y2;
        }
    }
    static void subdivideQuadAt(float t, float src[], int srcoff,
                                float left[], int leftoff,
                                float right[], int rightoff)
    {
        float x1 = src[srcoff + 0];
        float y1 = src[srcoff + 1];
        float ctrlx = src[srcoff + 2];
        float ctrly = src[srcoff + 3];
        float x2 = src[srcoff + 4];
        float y2 = src[srcoff + 5];
        if (left != null) {
            left[leftoff + 0] = x1;
            left[leftoff + 1] = y1;
        }
        if (right != null) {
            right[rightoff + 4] = x2;
            right[rightoff + 5] = y2;
        }
        x1 = x1 + t * (ctrlx - x1);
        y1 = y1 + t * (ctrly - y1);
        x2 = ctrlx + t * (x2 - ctrlx);
        y2 = ctrly + t * (y2 - ctrly);
        ctrlx = x1 + t * (x2 - x1);
        ctrly = y1 + t * (y2 - y1);
        if (left != null) {
            left[leftoff + 2] = x1;
            left[leftoff + 3] = y1;
            left[leftoff + 4] = ctrlx;
            left[leftoff + 5] = ctrly;
        }
        if (right != null) {
            right[rightoff + 0] = ctrlx;
            right[rightoff + 1] = ctrly;
            right[rightoff + 2] = x2;
            right[rightoff + 3] = y2;
        }
    }
    static void subdivideAt(float t, float src[], int srcoff,
                            float left[], int leftoff,
                            float right[], int rightoff, int size)
    {
        switch(size) {
        case 8:
            subdivideCubicAt(t, src, srcoff, left, leftoff, right, rightoff);
            break;
        case 6:
            subdivideQuadAt(t, src, srcoff, left, leftoff, right, rightoff);
            break;
        }
    }
}
