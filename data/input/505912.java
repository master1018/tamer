public class PathDashPathEffect extends PathEffect {
    public enum Style {
        TRANSLATE(0),   
        ROTATE(1),      
        MORPH(2);       
        Style(int value) {
            native_style = value;
        }
        int native_style;
    }
    public PathDashPathEffect(Path shape, float advance, float phase,
                              Style style) {
        native_instance = nativeCreate(shape.ni(), advance, phase,
                                       style.native_style);
    }
    private static native int nativeCreate(int native_path, float advance,
                                           float phase, int native_style);
}
