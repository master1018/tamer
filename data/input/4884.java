public class XRTextRenderer extends GlyphListPipe {
    XRGlyphCache glyphCache;
    XRCompositeManager maskBuffer;
    XRBackend backend;
    GrowableEltArray eltList;
    public XRTextRenderer(XRCompositeManager buffer) {
        glyphCache = new XRGlyphCache(buffer);
        maskBuffer = buffer;
        backend = buffer.getBackend();
        eltList = new GrowableEltArray(64);
    }
    protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl) {
        if (gl.getNumGlyphs() == 0) {
            return;
        }
        try {
            SunToolkit.awtLock();
            XRSurfaceData x11sd = (XRSurfaceData) sg2d.surfaceData;
            x11sd.validateAsDestination(null, sg2d.getCompClip());
            x11sd.maskBuffer.validateCompositeState(sg2d.composite, sg2d.transform, sg2d.paint, sg2d);
            float advX = gl.getX();
            float advY = gl.getY();
            int oldPosX = 0, oldPosY = 0;
            if (gl.isSubPixPos()) {
                advX += 0.1666667f;
                advY += 0.1666667f;
            } else {
                advX += 0.5f;
                advY += 0.5f;
            }
            XRGlyphCacheEntry[] cachedGlyphs = glyphCache.cacheGlyphs(gl);
            boolean containsLCDGlyphs = false;
            int activeGlyphSet = cachedGlyphs[0].getGlyphSet();
            int eltIndex = -1;
            gl.getBounds();
            float[] positions = gl.getPositions();
            for (int i = 0; i < gl.getNumGlyphs(); i++) {
                gl.setGlyphIndex(i);
                XRGlyphCacheEntry cacheEntry = cachedGlyphs[i];
                eltList.getGlyphs().addInt(cacheEntry.getGlyphID());
                int glyphSet = cacheEntry.getGlyphSet();
                containsLCDGlyphs |= (glyphSet == glyphCache.lcdGlyphSet);
                int posX = 0, posY = 0;
                if (gl.usePositions()
                        || (cacheEntry.getXAdvance() != ((float) cacheEntry.getXOff()) || cacheEntry.getYAdvance() != ((float) cacheEntry.getYOff()))
                        || eltIndex < 0 || glyphSet != activeGlyphSet) {
                    eltIndex = eltList.getNextIndex();
                    eltList.setCharCnt(eltIndex, 1);
                    activeGlyphSet = glyphSet;
                    eltList.setGlyphSet(eltIndex, glyphSet);
                    if (gl.usePositions()) {
                        float x = positions[i * 2] + advX;
                        float y = positions[i * 2 + 1] + advY;
                        posX = (int) Math.floor(x);
                        posY = (int) Math.floor(y);
                        advX -= cacheEntry.getXOff();
                        advY -= cacheEntry.getYOff();
                    } else {
                        posX = (int) Math.floor(advX);
                        posY = (int) Math.floor(advY);
                        advX += (cacheEntry.getXAdvance() - cacheEntry.getXOff());
                        advY += (cacheEntry.getYAdvance() - cacheEntry.getYOff());
                    }
                    eltList.setXOff(eltIndex, (posX - oldPosX));
                    eltList.setYOff(eltIndex, (posY - oldPosY));
                    oldPosX = posX;
                    oldPosY = posY;
                } else {
                    eltList.setCharCnt(eltIndex, eltList.getCharCnt(eltIndex) + 1);
                }
            }
            int maskFormat = containsLCDGlyphs ? XRUtils.PictStandardARGB32 : XRUtils.PictStandardA8;
            maskBuffer.compositeText(x11sd.picture, 0, maskFormat, eltList);
            eltList.clear();
        } finally {
            SunToolkit.awtUnlock();
        }
    }
}
