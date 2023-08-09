public abstract class ContactAccessor {
    private static ContactAccessor sInstance;
    public static ContactAccessor getInstance() {
        if (sInstance == null) {
            String className;
            @SuppressWarnings("deprecation")
            int sdkVersion = Integer.parseInt(Build.VERSION.SDK);       
            if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
                className = "com.example.android.businesscard.ContactAccessorSdk3_4";
            } else {
                className = "com.example.android.businesscard.ContactAccessorSdk5";
            }
            try {
                Class<? extends ContactAccessor> clazz =
                        Class.forName(className).asSubclass(ContactAccessor.class);
                sInstance = clazz.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return sInstance;
    }
    public abstract Intent getPickContactIntent();
    public abstract ContactInfo loadContact(ContentResolver contentResolver, Uri contactUri);
}
