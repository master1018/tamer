public class GrassWallpaper extends RenderScriptWallpaper {
    protected RenderScriptScene createScene(int width, int height) {
        return new GrassRS(this, width, height);
    }
}
