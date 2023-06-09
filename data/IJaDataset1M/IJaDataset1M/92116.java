package worldwind.contrib.layers.quadkey;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;

/**
 * Google Roads (MT) PNG tile renderer
 * @author Owner
 *
 */
public class GoogleRoadsLayer extends AbstractQuadKeyLayer {

    public GoogleRoadsLayer() {
        super("Google Roads");
        super.cacheRoot = "GoogleRoads/";
        super.mapExtension = ".png";
        setMapType("");
    }

    @Override
    protected Box QuadKeyToBox(String quadKey, int x, int y, int zoomLevel) {
        char c = quadKey.charAt(0);
        int tileSize = 2 << (18 - zoomLevel - 1);
        if (c == 'q') {
            y = y - tileSize;
        } else if (c == 'r') {
            y = y - tileSize;
            x = x + tileSize;
        } else if (c == 's') {
            x = x + tileSize;
        }
        if (quadKey.length() > 1) {
            return QuadKeyToBox(quadKey.substring(1), x, y, zoomLevel + 1);
        } else {
            return new Box(x, y, tileSize, tileSize);
        }
    }

    @Override
    protected String TileToQuadKey(int tx, int ty, int zl) {
        String quad = "";
        for (int i = zl; i > 0; i--) {
            int mask = 1 << (i - 1);
            int cell = 0;
            if ((tx & mask) != 0) {
                cell++;
            }
            if ((ty & mask) != 0) {
                cell += 2;
            }
            if (cell == 0) quad += "q";
            if (cell == 1) quad += "r";
            if (cell == 2) quad += "t";
            if (cell == 3) quad += "s";
        }
        return quad;
    }

    @Override
    protected String buildRequestUrl(String quadKey, String mapType, String mapExtension) {
        return null;
    }

    /**
	 * Build mass transit tile PNG urls
	 * @param zoom
	 * @return
	 */
    private String buildRoadsTileUrl(int tileX, int tileY, int zoom) {
        return "http://mt" + (int) (Math.random() * 4.0) + ".google.com/mt?v=w2t.80&nl=en&x=" + tileX + "&y=" + tileY + "&zoom=" + zoom + "&s=Galileo";
    }

    @Override
    protected void renderNeighbor(int tileX, int tileY, int zoomLevel, DrawContext dc) {
        String quadKey = TileToQuadKey(tileX, tileY, zoomLevel);
        Sector sector = QuadKeyToSector(quadKey);
        if (dc.getVisibleSector().intersection(sector) == null) return;
        final String mtUrl = buildRoadsTileUrl(tileX, tileY, 17 - zoomLevel);
        final String mtKey = tileX + "." + tileY + mapExtension;
        renderTile(dc, mtKey, mtUrl, sector);
    }

    @Override
    protected void doRender(DrawContext dc) {
        final String quadKey = computeSectors(dc);
        if (quadKey == null) return;
        final String mtUrl = buildRoadsTileUrl(tileX, tileY, 17 - zoomLevel);
        final String mtKey = tileX + "." + tileY + mapExtension;
        renderTile(dc, mtKey, mtUrl, sector);
        for (int i = 1; i <= 5; i++) {
            renderNeighborTiles(tileY, tileX, zoomLevel, dc, i);
        }
    }
}
