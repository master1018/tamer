public class ProfileSelf extends ProfileData {
    public ProfileSelf(MethodData methodData) {
        mElement = methodData;
        mContext = methodData;
    }
    @Override
    public String getProfileName() {
        return "self";
    }
    @Override
    public long getElapsedInclusive() {
        return mElement.getTopExclusive();
    }
}
