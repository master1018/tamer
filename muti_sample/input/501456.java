public class UserDictionaryToolsListFocus extends TextView {
    private TextView mPairView = null;
    public UserDictionaryToolsListFocus(Context context) {
        super(context);
    }
    public View getPairView() {
        return mPairView;
    }
    public void setPairView(TextView pairView) {
        mPairView = pairView;
    }
}
