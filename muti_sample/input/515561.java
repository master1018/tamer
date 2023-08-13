public class ContentRestrictionFactory {
    private static ContentRestriction sContentRestriction;
    private ContentRestrictionFactory() {
    }
    public static ContentRestriction getContentRestriction() {
        if (null == sContentRestriction) {
            sContentRestriction = new CarrierContentRestriction();
        }
        return sContentRestriction;
    }
}
