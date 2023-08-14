public class JulesAATileGenerator implements AATileGenerator {
    final static ExecutorService rasterThreadPool =
                                          Executors.newCachedThreadPool();
    final static int CPU_CNT = Runtime.getRuntime().availableProcessors();
    final static boolean ENABLE_THREADING = false;
    final static int THREAD_MIN = 16;
    final static int THREAD_BEGIN = 16;
    IdleTileCache tileCache;
    TileWorker worker;
    boolean threaded = false;
    int rasterTileCnt;
    final static int TILE_SIZE = 32;
    final static int TILE_SIZE_FP = 32 << 16;
    int left, right, top, bottom, width, height;
    int leftFP, topFP;
    int tileCnt, tilesX, tilesY;
    int currTilePos = 0;
    TrapezoidList traps;
    TileTrapContainer[] tiledTrapArray;
    JulesTile mainTile;
    public JulesAATileGenerator(Shape s, AffineTransform at, Region clip,
                                BasicStroke bs, boolean thin,
                                boolean normalize, int[] bbox) {
        JulesPathBuf buf = new JulesPathBuf();
        if (bs == null) {
            traps = buf.tesselateFill(s, at, clip);
        } else {
            traps = buf.tesselateStroke(s, bs, thin, false, true, at, clip);
        }
        calculateArea(bbox);
        bucketSortTraps();
        calculateTypicalAlpha();
        threaded = ENABLE_THREADING &&
                   rasterTileCnt >= THREAD_MIN && CPU_CNT >= 2;
        if (threaded) {
            tileCache = new IdleTileCache();
            worker = new TileWorker(this, THREAD_BEGIN, tileCache);
            rasterThreadPool.execute(worker);
        }
        mainTile = new JulesTile();
    }
    private static native long
        rasterizeTrapezoidsNative(long pixmanImagePtr, int[] traps,
                                  int[] trapPos, int trapCnt,
                                  byte[] buffer, int xOff, int yOff);
    private static native void freePixmanImgPtr(long pixmanImgPtr);
    private void calculateArea(int[] bbox) {
        tilesX = 0;
        tilesY = 0;
        tileCnt = 0;
        bbox[0] = 0;
        bbox[1] = 0;
        bbox[2] = 0;
        bbox[3] = 0;
        if (traps.getSize() > 0) {
            left = traps.getLeft();
            right = traps.getRight();
            top = traps.getTop();
            bottom = traps.getBottom();
            leftFP = left << 16;
            topFP = top << 16;
            bbox[0] = left;
            bbox[1] = top;
            bbox[2] = right;
            bbox[3] = bottom;
            width = right - left;
            height = bottom - top;
            if (width > 0 && height > 0) {
                tilesX = (int) Math.ceil(((double) width) / TILE_SIZE);
                tilesY = (int) Math.ceil(((double) height) / TILE_SIZE);
                tileCnt = tilesY * tilesX;
                tiledTrapArray = new TileTrapContainer[tileCnt];
            } else {
                traps.setSize(0);
            }
        }
    }
    private void bucketSortTraps() {
        for (int i = 0; i < traps.getSize(); i++) {
            int top = traps.getTop(i) - XRUtils.XDoubleToFixed(this.top);
            int bottom = traps.getBottom(i) - topFP;
            int p1xLeft = traps.getP1XLeft(i) - leftFP;
            int p2xLeft = traps.getP2XLeft(i) - leftFP;
            int p1xRight = traps.getP1XRight(i) - leftFP;
            int p2xRight = traps.getP2XRight(i) - leftFP;
            int minLeft = Math.min(p1xLeft, p2xLeft);
            int maxRight = Math.max(p1xRight, p2xRight);
            maxRight = maxRight > 0 ? maxRight - 1 : maxRight;
            bottom = bottom > 0 ? bottom - 1 : bottom;
            int startTileY = top / TILE_SIZE_FP;
            int endTileY = bottom / TILE_SIZE_FP;
            int startTileX = minLeft / TILE_SIZE_FP;
            int endTileX = maxRight / TILE_SIZE_FP;
            for (int n = startTileY; n <= endTileY; n++) {
                for (int m = startTileX; m <= endTileX; m++) {
                    int trapArrayPos = n * tilesX + m;
                    TileTrapContainer trapTileList = tiledTrapArray[trapArrayPos];
                    if (trapTileList == null) {
                        trapTileList = new TileTrapContainer(new GrowableIntArray(1, 16));
                        tiledTrapArray[trapArrayPos] = trapTileList;
                    }
                    trapTileList.getTraps().addInt(i);
                }
            }
        }
    }
    public void getAlpha(byte[] tileBuffer, int offset, int rowstride) {
        JulesTile tile = null;
        if (threaded) {
            tile = worker.getPreRasterizedTile(currTilePos);
        }
        if (tile != null) {
            System.arraycopy(tile.getImgBuffer(), 0,
                             tileBuffer, 0, tileBuffer.length);
            tileCache.releaseTile(tile);
        } else {
            mainTile.setImgBuffer(tileBuffer);
            rasterizeTile(currTilePos, mainTile);
        }
        nextTile();
    }
    public void calculateTypicalAlpha() {
        rasterTileCnt = 0;
        for (int index = 0; index < tileCnt; index++) {
            TileTrapContainer trapCont = tiledTrapArray[index];
            if (trapCont != null) {
                GrowableIntArray trapList = trapCont.getTraps();
                int tileAlpha = 127;
                if (trapList == null || trapList.getSize() == 0) {
                    tileAlpha = 0;
                } else if (doTrapsCoverTile(trapList, index)) {
                    tileAlpha = 0xff;
                }
                if (tileAlpha == 127 || tileAlpha == 0xff) {
                    rasterTileCnt++;
                }
                trapCont.setTileAlpha(tileAlpha);
            }
        }
    }
    protected boolean doTrapsCoverTile(GrowableIntArray trapList, int tileIndex) {
        if (trapList.getSize() > TILE_SIZE) {
            return false;
        }
        int tileStartX = getXPos(tileIndex) * TILE_SIZE_FP + leftFP;
        int tileStartY = getYPos(tileIndex) * TILE_SIZE_FP + topFP;
        int tileEndX = tileStartX + TILE_SIZE_FP;
        int tileEndY = tileStartY + TILE_SIZE_FP;
        int firstTop = traps.getTop(trapList.getInt(0));
        int firstBottom = traps.getBottom(trapList.getInt(0));
        if (firstTop > tileStartY || firstBottom < tileStartY) {
            return false;
        }
        int lastBottom = firstTop;
        for (int i = 0; i < trapList.getSize(); i++) {
            int trapPos = trapList.getInt(i);
            if (traps.getP1XLeft(trapPos) > tileStartX ||
                traps.getP2XLeft(trapPos) > tileStartX ||
                traps.getP1XRight(trapPos) < tileEndX  ||
                traps.getP2XRight(trapPos) < tileEndX  ||
                 traps.getTop(trapPos) != lastBottom)
            {
                return false;
            }
            lastBottom = traps.getBottom(trapPos);
        }
        return lastBottom >= tileEndY;
    }
    public int getTypicalAlpha() {
        if (tiledTrapArray[currTilePos] == null) {
            return 0;
        } else {
            return tiledTrapArray[currTilePos].getTileAlpha();
        }
    }
    public void dispose() {
        freePixmanImgPtr(mainTile.getPixmanImgPtr());
        if (threaded) {
            tileCache.disposeConsumerResources();
            worker.disposeConsumerResources();
        }
    }
    protected JulesTile rasterizeTile(int tileIndex, JulesTile tile) {
        int tileOffsetX = left + getXPos(tileIndex) * TILE_SIZE;
        int tileOffsetY = top + getYPos(tileIndex) * TILE_SIZE;
        TileTrapContainer trapCont = tiledTrapArray[tileIndex];
        GrowableIntArray trapList = trapCont.getTraps();
        if (trapCont.getTileAlpha() == 127) {
            long pixmanImgPtr =
                 rasterizeTrapezoidsNative(tile.getPixmanImgPtr(),
                                           traps.getTrapArray(),
                                           trapList.getArray(),
                                           trapList.getSize(),
                                           tile.getImgBuffer(),
                                           tileOffsetX, tileOffsetY);
            tile.setPixmanImgPtr(pixmanImgPtr);
        }
        tile.setTilePos(tileIndex);
        return tile;
    }
    protected int getXPos(int arrayPos) {
        return arrayPos % tilesX;
    }
    protected int getYPos(int arrayPos) {
        return arrayPos / tilesX;
    }
    public void nextTile() {
        currTilePos++;
    }
    public int getTileHeight() {
        return TILE_SIZE;
    }
    public int getTileWidth() {
        return TILE_SIZE;
    }
    public int getTileCount() {
        return tileCnt;
    }
    public TileTrapContainer getTrapContainer(int index) {
        return tiledTrapArray[index];
    }
}
class TileTrapContainer {
    int tileAlpha;
    GrowableIntArray traps;
    public TileTrapContainer(GrowableIntArray traps) {
        this.traps = traps;
    }
    public void setTileAlpha(int tileAlpha) {
        this.tileAlpha = tileAlpha;
    }
    public int getTileAlpha() {
        return tileAlpha;
    }
    public GrowableIntArray getTraps() {
        return traps;
    }
}
