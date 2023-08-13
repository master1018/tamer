public class WallpaperSettingsActivity extends PreferenceActivity {
    final public static String EXTRA_PREVIEW_MODE
            = "android.service.wallpaper.PREVIEW_MODE";
    @Override
    protected void onCreate(Bundle icicle) {
        if (false) {
            Resources.Theme theme = getTheme();
            if (getIntent().getBooleanExtra(EXTRA_PREVIEW_MODE, false)) {
                theme.applyStyle(com.android.internal.R.style.PreviewWallpaperSettings, true);
            } else {
                theme.applyStyle(com.android.internal.R.style.ActiveWallpaperSettings, true);
            }
        }
        super.onCreate(icicle);
    }
}
