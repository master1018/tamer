public class Menu implements Parcelable {
    public List<Item> items;
    public List<TextAttribute> titleAttrs;
    public PresentationType presentationType;
    public String title;
    public Bitmap titleIcon;
    public int defaultItem;
    public boolean softKeyPreferred;
    public boolean helpAvailable;
    public boolean titleIconSelfExplanatory;
    public boolean itemsIconSelfExplanatory;
    public Menu() {
        items = new ArrayList<Item>();
        title = null;
        titleAttrs = null;
        defaultItem = 0;
        softKeyPreferred = false;
        helpAvailable = false;
        titleIconSelfExplanatory = false;
        itemsIconSelfExplanatory = false;
        titleIcon = null;
        presentationType = PresentationType.NAVIGATION_OPTIONS;
    }
    private Menu(Parcel in) {
        title = in.readString();
        titleIcon = in.readParcelable(null);
        items = new ArrayList<Item>();
        int size = in.readInt();
        for (int i=0; i<size; i++) {
            Item item = in.readParcelable(null);
            items.add(item);
        }
        defaultItem = in.readInt();
        softKeyPreferred = in.readInt() == 1 ? true : false;
        helpAvailable = in.readInt() == 1 ? true : false;
        titleIconSelfExplanatory = in.readInt() == 1 ? true : false;
        itemsIconSelfExplanatory = in.readInt() == 1 ? true : false;
        presentationType = PresentationType.values()[in.readInt()];
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeParcelable(titleIcon, flags);
        int size = items.size();
        dest.writeInt(size);
        for (int i=0; i<size; i++) {
            dest.writeParcelable(items.get(i), flags);
        }
        dest.writeInt(defaultItem);
        dest.writeInt(softKeyPreferred ? 1 : 0);
        dest.writeInt(helpAvailable ? 1 : 0);
        dest.writeInt(titleIconSelfExplanatory ? 1 : 0);
        dest.writeInt(itemsIconSelfExplanatory ? 1 : 0);
        dest.writeInt(presentationType.ordinal());
    }
    public static final Parcelable.Creator<Menu> CREATOR = new Parcelable.Creator<Menu>() {
        public Menu createFromParcel(Parcel in) {
            return new Menu(in);
        }
        public Menu[] newArray(int size) {
            return new Menu[size];
        }
    };
}
