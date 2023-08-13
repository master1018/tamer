public class HeadlessGraphicsEnvironment extends CommonGraphicsEnvironment {
    @Override
    public boolean isHeadlessInstance() {
        return true;
    }
    @Override
    public GraphicsDevice getDefaultScreenDevice() throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    public GraphicsDevice[] getScreenDevices() throws HeadlessException {
        throw new HeadlessException();
    }
}
