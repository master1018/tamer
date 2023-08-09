public class MagicSmoke extends RenderScriptWallpaper<MagicSmokeRS> {
    @Override
    protected MagicSmokeRS createScene(int width, int height) {
        return new MagicSmokeRS(this, width, height);
    }
}
