public final class TypePrecedence {
    private TypePrecedence() {}
    private static final int[] TYPE_PRECEDENCE_PHONES = {
            Phone.TYPE_CUSTOM,
            Phone.TYPE_MOBILE,
            Phone.TYPE_HOME,
            Phone.TYPE_WORK,
            Phone.TYPE_OTHER,
            Phone.TYPE_FAX_HOME,
            Phone.TYPE_FAX_WORK,
            Phone.TYPE_PAGER};
    private static final int[] TYPE_PRECEDENCE_EMAIL = {
            Email.TYPE_CUSTOM,
            Email.TYPE_HOME,
            Email.TYPE_WORK,
            Email.TYPE_OTHER};
    private static final int[] TYPE_PRECEDENCE_POSTAL = {
            StructuredPostal.TYPE_CUSTOM,
            StructuredPostal.TYPE_HOME,
            StructuredPostal.TYPE_WORK,
            StructuredPostal.TYPE_OTHER};
    private static final int[] TYPE_PRECEDENCE_IM = {
            Im.TYPE_CUSTOM,
            Im.TYPE_HOME,
            Im.TYPE_WORK,
            Im.TYPE_OTHER};
    private static final int[] TYPE_PRECEDENCE_ORG = {
            Organization.TYPE_CUSTOM,
            Organization.TYPE_WORK,
            Organization.TYPE_OTHER};
    @Deprecated
    public static int getTypePrecedence(String mimetype, int type) {
        int[] typePrecedence = getTypePrecedenceList(mimetype);
        if (typePrecedence == null) {
            return -1;
        }
        for (int i = 0; i < typePrecedence.length; i++) {
            if (typePrecedence[i] == type) {
                return i;
            }
        }
        return typePrecedence.length;
    }
    @Deprecated
    private static int[] getTypePrecedenceList(String mimetype) {
        if (mimetype.equals(Phone.CONTENT_ITEM_TYPE)) {
            return TYPE_PRECEDENCE_PHONES;
        } else if (mimetype.equals(Constants.MIME_SMS_ADDRESS)) {
            return TYPE_PRECEDENCE_PHONES;
        } else if (mimetype.equals(Email.CONTENT_ITEM_TYPE)) {
            return TYPE_PRECEDENCE_EMAIL;
        } else if (mimetype.equals(StructuredPostal.CONTENT_ITEM_TYPE)) {
            return TYPE_PRECEDENCE_POSTAL;
        } else if (mimetype.equals(Im.CONTENT_ITEM_TYPE)) {
            return TYPE_PRECEDENCE_IM;
        } else if (mimetype.equals(Organization.CONTENT_ITEM_TYPE)) {
            return TYPE_PRECEDENCE_ORG;
        } else {
            return null;
        }
    }
}
