public abstract class FrameTexture extends Texture {
    public FrameTexture() {
    }
    public FrameTexture(GL11 gl, int id, int state) {
        super(gl, id, state);
    }
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
    }
    abstract public Rect getPaddings();
}
