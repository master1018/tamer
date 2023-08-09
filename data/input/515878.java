public class SmsBroadcastConfigInfo {
    private int fromServiceId;
    private int toServiceId;
    private int fromCodeScheme;
    private int toCodeScheme;
    private boolean selected;
    public SmsBroadcastConfigInfo(int fromId, int toId, int fromScheme,
            int toScheme, boolean selected) {
        setFromServiceId(fromId);
        setToServiceId(toId);
        setFromCodeScheme(fromScheme);
        setToCodeScheme(toScheme);
        this.setSelected(selected);
    }
    public void setFromServiceId(int fromServiceId) {
        this.fromServiceId = fromServiceId;
    }
    public int getFromServiceId() {
        return fromServiceId;
    }
    public void setToServiceId(int toServiceId) {
        this.toServiceId = toServiceId;
    }
    public int getToServiceId() {
        return toServiceId;
    }
    public void setFromCodeScheme(int fromCodeScheme) {
        this.fromCodeScheme = fromCodeScheme;
    }
    public int getFromCodeScheme() {
        return fromCodeScheme;
    }
    public void setToCodeScheme(int toCodeScheme) {
        this.toCodeScheme = toCodeScheme;
    }
    public int getToCodeScheme() {
        return toCodeScheme;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    public boolean isSelected() {
        return selected;
    }
    @Override
    public String toString() {
        return "SmsBroadcastConfigInfo: Id [" +
            getFromServiceId() + "," + getToServiceId() + "] Code [" +
            getFromCodeScheme() + "," + getToCodeScheme() + "] " +
            (isSelected() ? "ENABLED" : "DISABLED");
    }
}