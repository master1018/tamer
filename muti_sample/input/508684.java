public class PorterDuffColorFilter extends ColorFilter {
    public PorterDuffColorFilter(int srcColor, PorterDuff.Mode mode) {
        native_instance = native_CreatePorterDuffFilter(srcColor,
                                                        mode.nativeInt);
    }
    private static native int native_CreatePorterDuffFilter(int srcColor,
                                                            int porterDuffMode);
}
