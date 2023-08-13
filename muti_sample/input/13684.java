public class TileWorker implements Runnable {
    final static int RASTERIZED_TILE_SYNC_GRANULARITY = 8;
    final ArrayList<JulesTile> rasterizedTileConsumerCache =
         new ArrayList<JulesTile>();
    final LinkedList<JulesTile> rasterizedBuffers = new LinkedList<JulesTile>();
    IdleTileCache tileCache;
    JulesAATileGenerator tileGenerator;
    int workerStartIndex;
    volatile int consumerPos = 0;
    int mainThreadCnt = 0;
    int workerCnt = 0;
    int doubled = 0;
    public TileWorker(JulesAATileGenerator tileGenerator, int workerStartIndex, IdleTileCache tileCache) {
        this.tileGenerator = tileGenerator;
        this.workerStartIndex = workerStartIndex;
        this.tileCache = tileCache;
    }
    public void run() {
        ArrayList<JulesTile> tiles = new ArrayList<JulesTile>(16);
        for (int i = workerStartIndex; i < tileGenerator.getTileCount(); i++) {
            TileTrapContainer tile = tileGenerator.getTrapContainer(i);
            if (tile != null && tile.getTileAlpha() == 127) {
                JulesTile rasterizedTile =
                      tileGenerator.rasterizeTile(i,
                           tileCache.getIdleTileWorker(
                               tileGenerator.getTileCount() - i - 1));
                tiles.add(rasterizedTile);
                if (tiles.size() > RASTERIZED_TILE_SYNC_GRANULARITY) {
                    addRasterizedTiles(tiles);
                    tiles.clear();
                }
            }
            i = Math.max(i, consumerPos + RASTERIZED_TILE_SYNC_GRANULARITY / 2);
        }
        addRasterizedTiles(tiles);
        tileCache.disposeRasterizerResources();
    }
    public JulesTile getPreRasterizedTile(int tilePos) {
        JulesTile tile = null;
        if (rasterizedTileConsumerCache.size() == 0 &&
            tilePos >= workerStartIndex)
        {
            synchronized (rasterizedBuffers) {
                rasterizedTileConsumerCache.addAll(rasterizedBuffers);
                rasterizedBuffers.clear();
            }
        }
        while (tile == null && rasterizedTileConsumerCache.size() > 0) {
            JulesTile t = rasterizedTileConsumerCache.get(0);
            if (t.getTilePos() > tilePos) {
                break;
            }
            if (t.getTilePos() < tilePos) {
                tileCache.releaseTile(t);
                doubled++;
            }
            if (t.getTilePos() <= tilePos) {
                rasterizedTileConsumerCache.remove(0);
            }
            if (t.getTilePos() == tilePos) {
                tile = t;
            }
        }
        if (tile == null) {
            mainThreadCnt++;
            consumerPos = tilePos;
        } else {
            workerCnt++;
        }
        return tile;
    }
    private void addRasterizedTiles(ArrayList<JulesTile> tiles) {
        synchronized (rasterizedBuffers) {
            rasterizedBuffers.addAll(tiles);
        }
    }
    public void disposeConsumerResources() {
        synchronized (rasterizedBuffers) {
            tileCache.releaseTiles(rasterizedBuffers);
        }
        tileCache.releaseTiles(rasterizedTileConsumerCache);
    }
}
