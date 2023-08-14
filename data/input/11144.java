final class PiscesTileGenerator implements AATileGenerator {
    public static final int TILE_SIZE = PiscesCache.TILE_SIZE;
    private static final Map<Integer, byte[]> alphaMapsCache = new
                   ConcurrentHashMap<Integer, byte[]>();
    PiscesCache cache;
    int x, y;
    final int maxalpha;
    private final int maxTileAlphaSum;
    byte alphaMap[];
    public PiscesTileGenerator(Renderer r, int maxalpha) {
        this.cache = r.getCache();
        this.x = cache.bboxX0;
        this.y = cache.bboxY0;
        this.alphaMap = getAlphaMap(maxalpha);
        this.maxalpha = maxalpha;
        this.maxTileAlphaSum = TILE_SIZE*TILE_SIZE*maxalpha;
    }
    private static byte[] buildAlphaMap(int maxalpha) {
        byte[] alMap = new byte[maxalpha+1];
        int halfmaxalpha = maxalpha>>2;
        for (int i = 0; i <= maxalpha; i++) {
            alMap[i] = (byte) ((i * 255 + halfmaxalpha) / maxalpha);
        }
        return alMap;
    }
    public static byte[] getAlphaMap(int maxalpha) {
        if (!alphaMapsCache.containsKey(maxalpha)) {
            alphaMapsCache.put(maxalpha, buildAlphaMap(maxalpha));
        }
        return alphaMapsCache.get(maxalpha);
    }
    public void getBbox(int bbox[]) {
        bbox[0] = cache.bboxX0;
        bbox[1] = cache.bboxY0;
        bbox[2] = cache.bboxX1;
        bbox[3] = cache.bboxY1;
    }
    public int getTileWidth() {
        return TILE_SIZE;
    }
    public int getTileHeight() {
        return TILE_SIZE;
    }
    public int getTypicalAlpha() {
        int al = cache.alphaSumInTile(x, y);
        return (al == 0x00 ? 0x00 :
            (al == maxTileAlphaSum ? 0xff : 0x80));
    }
    public void nextTile() {
        if ((x += TILE_SIZE) >= cache.bboxX1) {
            x = cache.bboxX0;
            y += TILE_SIZE;
        }
    }
    public void getAlpha(byte tile[], int offset, int rowstride) {
        int x0 = this.x;
        int x1 = x0 + TILE_SIZE;
        int y0 = this.y;
        int y1 = y0 + TILE_SIZE;
        if (x1 > cache.bboxX1) x1 = cache.bboxX1;
        if (y1 > cache.bboxY1) y1 = cache.bboxY1;
        y0 -= cache.bboxY0;
        y1 -= cache.bboxY0;
        int idx = offset;
        for (int cy = y0; cy < y1; cy++) {
            int[] row = cache.rowAARLE[cy];
            assert row != null;
            int cx = cache.minTouched(cy);
            if (cx > x1) cx = x1;
            for (int i = x0; i < cx; i++) {
                tile[idx++] = 0x00;
            }
            int pos = 2;
            while (cx < x1 && pos < row[1]) {
                byte val;
                int runLen = 0;
                assert row[1] > 2;
                try {
                    val = alphaMap[row[pos]];
                    runLen = row[pos + 1];
                    assert runLen > 0;
                } catch (RuntimeException e0) {
                    System.out.println("maxalpha = "+maxalpha);
                    System.out.println("tile["+x0+", "+y0+
                                       " => "+x1+", "+y1+"]");
                    System.out.println("cx = "+cx+", cy = "+cy);
                    System.out.println("idx = "+idx+", pos = "+pos);
                    System.out.println("len = "+runLen);
                    System.out.print(cache.toString());
                    e0.printStackTrace();
                    throw e0;
                }
                int rx0 = cx;
                cx += runLen;
                int rx1 = cx;
                if (rx0 < x0) rx0 = x0;
                if (rx1 > x1) rx1 = x1;
                runLen = rx1 - rx0;
                while (--runLen >= 0) {
                    try {
                        tile[idx++] = val;
                    } catch (RuntimeException e) {
                        System.out.println("maxalpha = "+maxalpha);
                        System.out.println("tile["+x0+", "+y0+
                                           " => "+x1+", "+y1+"]");
                        System.out.println("cx = "+cx+", cy = "+cy);
                        System.out.println("idx = "+idx+", pos = "+pos);
                        System.out.println("rx0 = "+rx0+", rx1 = "+rx1);
                        System.out.println("len = "+runLen);
                        System.out.print(cache.toString());
                        e.printStackTrace();
                        throw e;
                    }
                }
                pos += 2;
            }
            if (cx < x0) { cx = x0; }
            while (cx < x1) {
                tile[idx++] = 0x00;
                cx++;
            }
            idx += (rowstride - (x1-x0));
        }
        nextTile();
    }
    static String hex(int v, int d) {
        String s = Integer.toHexString(v);
        while (s.length() < d) {
            s = "0"+s;
        }
        return s.substring(0, d);
    }
    public void dispose() {}
}
