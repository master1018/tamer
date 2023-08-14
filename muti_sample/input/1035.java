public class RegionIterator {
    Region region;
    int curIndex;
    int numXbands;
    RegionIterator(Region r) {
        region = r;
    }
    public RegionIterator createCopy() {
        RegionIterator r = new RegionIterator(region);
        r.curIndex = this.curIndex;
        r.numXbands = this.numXbands;
        return r;
    }
    public void copyStateFrom(RegionIterator ri) {
        if (this.region != ri.region) {
            throw new InternalError("region mismatch");
        }
        this.curIndex = ri.curIndex;
        this.numXbands = ri.numXbands;
    }
    public boolean nextYRange(int range[]) {
        curIndex += numXbands * 2;
        numXbands = 0;
        if (curIndex >= region.endIndex) {
            return false;
        }
        range[1] = region.bands[curIndex++];
        range[3] = region.bands[curIndex++];
        numXbands = region.bands[curIndex++];
        return true;
    }
    public boolean nextXBand(int range[]) {
        if (numXbands <= 0) {
            return false;
        }
        numXbands--;
        range[0] = region.bands[curIndex++];
        range[2] = region.bands[curIndex++];
        return true;
    }
}
