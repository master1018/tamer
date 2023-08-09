public class FallWallpaper extends RenderScriptWallpaper {
    protected RenderScriptScene createScene(int width, int height) {
        return new FallRS(width, height);
    }
}
