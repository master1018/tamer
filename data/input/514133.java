public class RadialGradient extends Shader {
	public RadialGradient(float x, float y, float radius,
                          int colors[], float positions[], TileMode tile) {
        if (radius <= 0) {
            throw new IllegalArgumentException("radius must be > 0");
        }
        if (colors.length < 2) {
            throw new IllegalArgumentException("needs >= 2 number of colors");
        }
        if (positions != null && colors.length != positions.length) {
            throw new IllegalArgumentException("color and position arrays must be of equal length");
        }
        native_instance = nativeCreate1(x, y, radius, colors, positions, tile.nativeInt);
    }
	public RadialGradient(float x, float y, float radius,
                          int color0, int color1, TileMode tile) {
        if (radius <= 0) {
            throw new IllegalArgumentException("radius must be > 0");
        }
        native_instance = nativeCreate2(x, y, radius, color0, color1, tile.nativeInt);
    }
	private static native int nativeCreate1(float x, float y, float radius,
                                            int colors[], float positions[], int tileMode);
	private static native int nativeCreate2(float x, float y, float radius,
                                            int color0, int color1, int tileMode);
}
