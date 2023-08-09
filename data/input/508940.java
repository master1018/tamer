public class SweepGradient extends Shader {
    public SweepGradient(float cx, float cy,
                         int colors[], float positions[]) {
        if (colors.length < 2) {
            throw new IllegalArgumentException("needs >= 2 number of colors");
        }
        if (positions != null && colors.length != positions.length) {
            throw new IllegalArgumentException(
                        "color and position arrays must be of equal length");
        }
        native_instance = nativeCreate1(cx, cy, colors, positions);
    }
    public SweepGradient(float cx, float cy, int color0, int color1) {
        native_instance = nativeCreate2(cx, cy, color0, color1);
    }
    private static native int nativeCreate1(float x, float y,
                                            int colors[], float positions[]);
    private static native int nativeCreate2(float x, float y,
                                            int color0, int color1);
}
