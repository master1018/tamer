public class TextMessage implements Parcelable {
    public String title = "";
    public String text = null;
    public Bitmap icon = null;
    public boolean iconSelfExplanatory = false;
    public boolean isHighPriority = false;
    public boolean responseNeeded = true;
    public boolean userClear = false;
    public Duration duration = null;
    TextMessage() {
    }
    private TextMessage(Parcel in) {
        title = in.readString();
        text = in.readString();
        icon = in.readParcelable(null);
        iconSelfExplanatory = in.readInt() == 1 ? true : false;
        isHighPriority = in.readInt() == 1 ? true : false;
        responseNeeded = in.readInt() == 1 ? true : false;
        userClear = in.readInt() == 1 ? true : false;
        duration = in.readParcelable(null);
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(text);
        dest.writeParcelable(icon, 0);
        dest.writeInt(iconSelfExplanatory ? 1 : 0);
        dest.writeInt(isHighPriority ? 1 : 0);
        dest.writeInt(responseNeeded ? 1 : 0);
        dest.writeInt(userClear ? 1 : 0);
        dest.writeParcelable(duration, 0);
    }
    public static final Parcelable.Creator<TextMessage> CREATOR = new Parcelable.Creator<TextMessage>() {
        public TextMessage createFromParcel(Parcel in) {
            return new TextMessage(in);
        }
        public TextMessage[] newArray(int size) {
            return new TextMessage[size];
        }
    };
}