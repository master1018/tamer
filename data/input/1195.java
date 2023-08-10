public class LatLngRectangle {
    private LatLngPoint southWest;
    private LatLngPoint northEast;
    public LatLngRectangle(LatLngPoint southWest, LatLngPoint northEast) {
        this.southWest = southWest;
        this.northEast = northEast;
    }
    public LatLngPoint getSouthWest() {
        return southWest;
    }
    public void setSouthWest(LatLngPoint southWest) {
        this.southWest = southWest;
    }
    public LatLngPoint getNorthEast() {
        return northEast;
    }
    public void setNorthEast(LatLngPoint northEast) {
        this.northEast = northEast;
    }
}
