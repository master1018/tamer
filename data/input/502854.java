public class IBinderParcelable implements Parcelable {
    public IBinder binder;
    public IBinderParcelable(IBinder source) {
        binder = source;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(binder);
    }
    public static final Parcelable.Creator<IBinderParcelable>
        CREATOR = new Parcelable.Creator<IBinderParcelable>() {
        public IBinderParcelable createFromParcel(Parcel source) {
            return new IBinderParcelable(source);
        }
        public IBinderParcelable[] newArray(int size) {
            return new IBinderParcelable[size];
        }
    };
    private IBinderParcelable(Parcel source) {
        binder = source.readStrongBinder();
    }
}
