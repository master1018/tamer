public class GallerySettings extends PreferenceActivity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.gallery_preferences);
    }
}
