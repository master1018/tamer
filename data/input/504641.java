public class ComposeShader extends Shader {
    public ComposeShader(Shader shaderA, Shader shaderB, Xfermode mode) {
        native_instance = nativeCreate1(shaderA.native_instance, shaderB.native_instance,
                                        (mode != null) ? mode.native_instance : 0);
    }
    public ComposeShader(Shader shaderA, Shader shaderB, PorterDuff.Mode mode) {
        native_instance = nativeCreate2(shaderA.native_instance, shaderB.native_instance,
                                        mode.nativeInt);
    }
    private static native int nativeCreate1(int native_shaderA, int native_shaderB, int native_mode);
    private static native int nativeCreate2(int native_shaderA, int native_shaderB, int porterDuffMode);
}
