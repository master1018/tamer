public class CreateInfo {
    public final static Class<?>[] INJECTED_CLASSES = new Class<?>[] {
            OverrideMethod.class,
            MethodListener.class,
            MethodAdapter.class,
            CreateInfo.class
        };
    public final static String[] OVERRIDDEN_METHODS = new String[] {
            "android.view.View#isInEditMode",
            "android.content.res.Resources$Theme#obtainStyledAttributes",
        };
    public final static String[] RENAMED_CLASSES =
        new String[] {
            "android.graphics.Bitmap",              "android.graphics._Original_Bitmap",
            "android.graphics.BitmapFactory",       "android.graphics._Original_BitmapFactory",
            "android.graphics.BitmapShader",        "android.graphics._Original_BitmapShader",
            "android.graphics.Canvas",              "android.graphics._Original_Canvas",
            "android.graphics.ComposeShader",       "android.graphics._Original_ComposeShader",
            "android.graphics.DashPathEffect",       "android.graphics._Original_DashPathEffect",
            "android.graphics.LinearGradient",      "android.graphics._Original_LinearGradient",
            "android.graphics.Matrix",              "android.graphics._Original_Matrix",
            "android.graphics.Paint",               "android.graphics._Original_Paint",
            "android.graphics.Path",                "android.graphics._Original_Path",
            "android.graphics.PorterDuffXfermode",  "android.graphics._Original_PorterDuffXfermode",
            "android.graphics.RadialGradient",      "android.graphics._Original_RadialGradient",
            "android.graphics.Shader",              "android.graphics._Original_Shader",
            "android.graphics.SweepGradient",       "android.graphics._Original_SweepGradient",
            "android.graphics.Typeface",            "android.graphics._Original_Typeface",
            "android.os.ServiceManager",            "android.os._Original_ServiceManager",
            "android.util.FloatMath",               "android.util._Original_FloatMath",
            "android.view.SurfaceView",             "android.view._Original_SurfaceView",
            "android.view.accessibility.AccessibilityManager", "android.view.accessibility._Original_AccessibilityManager",
        };
    public final static String[] REMOVED_METHODS =
        new String[] {
            "android.graphics.Paint",       
            "android.graphics.Paint$Align", 
            "android.graphics.Paint$Style",
            "android.graphics.Paint$Join",
            "android.graphics.Paint$Cap",
            "android.graphics.Paint$FontMetrics",
            "android.graphics.Paint$FontMetricsInt",
            null };                         
}
