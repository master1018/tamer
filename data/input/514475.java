public class LightingColorFilter extends ColorFilter {
    public LightingColorFilter(int mul, int add) {
        native_instance = native_CreateLightingFilter(mul, add);
    }
    private static native int native_CreateLightingFilter(int mul, int add);
}
