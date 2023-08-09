public class LinearGradient extends Shader {
	public LinearGradient(float x0, float y0, float x1, float y1,
                          int colors[], float positions[], TileMode tile) {
        if (colors.length < 2) {
            throw new IllegalArgumentException("needs >= 2 number of colors");
        }
        if (positions != null && colors.length != positions.length) {
            throw new IllegalArgumentException("color and position arrays must be of equal length");
        }
        native_instance = nativeCreate1(x0, y0, x1, y1, colors, positions, tile.nativeInt);
    }
	public LinearGradient(float x0, float y0, float x1, float y1,
                          int color0, int color1, TileMode tile) {
        native_instance = nativeCreate2(x0, y0, x1, y1, color0, color1, tile.nativeInt);
    }
	private static native int nativeCreate1(float x0, float y0, float x1, float y1,
                                            int colors[], float positions[], int tileMode);
	private static native int nativeCreate2(float x0, float y0, float x1, float y1,
                                            int color0, int color1, int tileMode);
}
