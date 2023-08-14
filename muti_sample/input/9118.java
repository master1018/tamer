public class IdleTileCache {
    final static int IDLE_TILE_SYNC_GRANULARITY = 16;
    final static ArrayList<JulesTile> idleBuffers = new ArrayList<JulesTile>();
    ArrayList<JulesTile> idleTileWorkerCacheList = new ArrayList<JulesTile>();
    ArrayList<JulesTile> idleTileConsumerCacheList =
              new ArrayList<JulesTile>(IDLE_TILE_SYNC_GRANULARITY);
    public JulesTile getIdleTileWorker(int maxCache) {
        if (idleTileWorkerCacheList.size() == 0) {
            idleTileWorkerCacheList.ensureCapacity(maxCache);
            synchronized (idleBuffers) {
                for (int i = 0; i < maxCache && idleBuffers.size() > 0; i++) {
                    idleTileWorkerCacheList.add(
                            idleBuffers.remove(idleBuffers.size() - 1));
                }
            }
        }
        if (idleTileWorkerCacheList.size() > 0) {
            return idleTileWorkerCacheList.remove(idleTileWorkerCacheList.size() - 1);
        }
        return new JulesTile();
    }
    public void releaseTile(JulesTile tile) {
        if (tile != null && tile.hasBuffer()) {
            idleTileConsumerCacheList.add(tile);
            if (idleTileConsumerCacheList.size() > IDLE_TILE_SYNC_GRANULARITY) {
                synchronized (idleBuffers) {
                    idleBuffers.addAll(idleTileConsumerCacheList);
                }
                idleTileConsumerCacheList.clear();
            }
        }
    }
    public void disposeRasterizerResources() {
        releaseTiles(idleTileWorkerCacheList);
    }
    public void disposeConsumerResources() {
        releaseTiles(idleTileConsumerCacheList);
    }
    public void releaseTiles(List<JulesTile> tileList) {
        if (tileList.size() > 0) {
            synchronized (idleBuffers) {
                idleBuffers.addAll(tileList);
            }
            tileList.clear();
        }
    }
}
