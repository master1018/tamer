public class Visualization2 extends RenderScriptWallpaper<Visualization2RS> {
    @Override
    protected Visualization2RS createScene(int width, int height) {
        return new Visualization2RS(width, height);
    }
}
