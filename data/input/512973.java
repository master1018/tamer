public class NexusWallpaper extends RenderScriptWallpaper {
    protected RenderScriptScene createScene(int width, int height) {
        return new NexusRS(width, height);
    }
}
