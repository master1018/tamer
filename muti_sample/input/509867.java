public class IconData {
    public static final int TEXT = 1;
    public static final int ICON = 2;
    public int type;
    public String slot;
    public String iconPackage;
    public int iconId;
    public int iconLevel;
    public int number;
    public CharSequence text;
    private IconData() {
    }
    public static IconData makeIcon(String slot,
            String iconPackage, int iconId, int iconLevel, int number) {
        IconData data = new IconData();
        data.type = ICON;
        data.slot = slot;
        data.iconPackage = iconPackage;
        data.iconId = iconId;
        data.iconLevel = iconLevel;
        data.number = number;
        return data;
    }
    public static IconData makeText(String slot, CharSequence text) {
        IconData data = new IconData();
        data.type = TEXT;
        data.slot = slot;
        data.text = text;
        return data;
    }
    public void copyFrom(IconData that) {
        this.type = that.type;
        this.slot = that.slot;
        this.iconPackage = that.iconPackage;
        this.iconId = that.iconId;
        this.iconLevel = that.iconLevel;
        this.number = that.number;
        this.text = that.text; 
    }
    public IconData clone() {
        IconData that = new IconData();
        that.copyFrom(this);
        return that;
    }
    public String toString() {
        if (this.type == TEXT) {
            return "IconData(slot=" + (this.slot != null ? "'" + this.slot + "'" : "null")
                    + " text='" + this.text + "')"; 
        }
        else if (this.type == ICON) {
            return "IconData(slot=" + (this.slot != null ? "'" + this.slot + "'" : "null")
                    + " package=" + this.iconPackage
                    + " iconId=" + Integer.toHexString(this.iconId)
                    + " iconLevel=" + this.iconLevel + ")"; 
        }
        else {
            return "IconData(type=" + type + ")";
        }
    }
}
