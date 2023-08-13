public class Visualization3 extends RenderScriptWallpaper<Visualization3RS> {
    @Override
    protected Visualization3RS createScene(int width, int height) {
        return new Visualization3RS(width, height);
    }
}
