final class PiscesCache {
    final int bboxX0, bboxY0, bboxX1, bboxY1;
    final int[][] rowAARLE;
    private int x0 = Integer.MIN_VALUE, y0 = Integer.MIN_VALUE;
    private final int[][] touchedTile;
    static final int TILE_SIZE_LG = 5;
    static final int TILE_SIZE = 1 << TILE_SIZE_LG; 
    private static final int INIT_ROW_SIZE = 8; 
    PiscesCache(int minx, int miny, int maxx, int maxy) {
        assert maxy >= miny && maxx >= minx;
        bboxX0 = minx;
        bboxY0 = miny;
        bboxX1 = maxx + 1;
        bboxY1 = maxy + 1;
        rowAARLE = new int[bboxY1 - bboxY0 + 1][INIT_ROW_SIZE];
        x0 = 0;
        y0 = -1; 
        int nyTiles = (maxy - miny + TILE_SIZE) >> TILE_SIZE_LG;
        int nxTiles = (maxx - minx + TILE_SIZE) >> TILE_SIZE_LG;
        touchedTile = new int[nyTiles][nxTiles];
    }
    void addRLERun(int val, int runLen) {
        if (runLen > 0) {
            addTupleToRow(y0, val, runLen);
            if (val != 0) {
                int tx = x0 >> TILE_SIZE_LG;
                int ty = y0 >> TILE_SIZE_LG;
                int tx1 = (x0 + runLen - 1) >> TILE_SIZE_LG;
                if (tx1 >= touchedTile[ty].length) {
                    tx1 = touchedTile[ty].length - 1;
                }
                if (tx <= tx1) {
                    int nextTileXCoord = (tx + 1) << TILE_SIZE_LG;
                    if (nextTileXCoord > x0+runLen) {
                        touchedTile[ty][tx] += val * runLen;
                    } else {
                        touchedTile[ty][tx] += val * (nextTileXCoord - x0);
                    }
                    tx++;
                }
                for (; tx < tx1; tx++) {
                    touchedTile[ty][tx] += (val << TILE_SIZE_LG);
                }
                if (tx == tx1) {
                    int lastXCoord = Math.min(x0 + runLen, (tx + 1) << TILE_SIZE_LG);
                    int txXCoord = tx << TILE_SIZE_LG;
                    touchedTile[ty][tx] += val * (lastXCoord - txXCoord);
                }
            }
            x0 += runLen;
        }
    }
    void startRow(int y, int x) {
        assert y - bboxY0 > y0;
        assert y <= bboxY1; 
        y0 = y - bboxY0;
        assert rowAARLE[y0][1] == 0;
        x0 = x - bboxX0;
        assert x0 >= 0 : "Input must not be to the left of bbox bounds";
        rowAARLE[y0][0] = x;
        rowAARLE[y0][1] = 2;
    }
    int alphaSumInTile(int x, int y) {
        x -= bboxX0;
        y -= bboxY0;
        return touchedTile[y>>TILE_SIZE_LG][x>>TILE_SIZE_LG];
    }
    int minTouched(int rowidx) {
        return rowAARLE[rowidx][0];
    }
    int rowLength(int rowidx) {
        return rowAARLE[rowidx][1];
    }
    private void addTupleToRow(int row, int a, int b) {
        int end = rowAARLE[row][1];
        rowAARLE[row] = Helpers.widenArray(rowAARLE[row], end, 2);
        rowAARLE[row][end++] = a;
        rowAARLE[row][end++] = b;
        rowAARLE[row][1] = end;
    }
    @Override
    public String toString() {
        String ret = "bbox = ["+
                      bboxX0+", "+bboxY0+" => "+
                      bboxX1+", "+bboxY1+"]\n";
        for (int[] row : rowAARLE) {
            if (row != null) {
                ret += ("minTouchedX=" + row[0] +
                        "\tRLE Entries: " + Arrays.toString(
                                Arrays.copyOfRange(row, 2, row[1])) + "\n");
            } else {
                ret += "[]\n";
            }
        }
        return ret;
    }
}
