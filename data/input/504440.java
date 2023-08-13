public class MapView extends MockView {
    public MapView(Context context) {
        this(context, null);
    }
    public MapView(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.mapViewStyle);
    }
    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public void displayZoomControls(boolean takeFocus) {
    }
    public boolean canCoverCenter() {
        return false;
    }
    public void preLoad() {
    }
    public int getZoomLevel() {
        return 0;
    }
    public void setSatellite(boolean on) {
    }
    public boolean isSatellite() {
        return false;
    }
    public void setTraffic(boolean on) {
    }
    public boolean isTraffic() {
        return false;
    }
    public void setStreetView(boolean on) {
    }
    public boolean isStreetView() {
        return false;
    }
    public int getLatitudeSpan() {
        return 0;
    }
    public int getLongitudeSpan() {
        return 0;
    }
    public int getMaxZoomLevel() {
        return 0;
    }
    public void onSaveInstanceState(Bundle state) {
    }
    public void onRestoreInstanceState(Bundle state) {
    }
    public View getZoomControls() {
        return null;
    }
}
