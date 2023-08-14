public abstract class BufferStrategy {
    public abstract boolean contentsLost();
    public abstract boolean contentsRestored();
    public abstract BufferCapabilities getCapabilities();
    public abstract Graphics getDrawGraphics();
    public abstract void show();
}
