public class SF2Layer extends SoundbankResource {
    protected String name = "";
    protected SF2GlobalRegion globalregion = null;
    protected List<SF2LayerRegion> regions = new ArrayList<SF2LayerRegion>();
    public SF2Layer(SF2Soundbank soundBank) {
        super(soundBank, null, null);
    }
    public SF2Layer() {
        super(null, null, null);
    }
    public Object getData() {
        return null;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<SF2LayerRegion> getRegions() {
        return regions;
    }
    public SF2GlobalRegion getGlobalRegion() {
        return globalregion;
    }
    public void setGlobalZone(SF2GlobalRegion zone) {
        globalregion = zone;
    }
    public String toString() {
        return "Layer: " + name;
    }
}
