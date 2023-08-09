public class GalaxyWallpaper extends RenderScriptWallpaper<GalaxyRS> {
    protected GalaxyRS createScene(int width, int height) {
        return new GalaxyRS(width, height);
    }
}
