public final class PrinterGraphicsDevice extends GraphicsDevice {
    String printerID;
    GraphicsConfiguration graphicsConf;
    protected PrinterGraphicsDevice(GraphicsConfiguration conf, String id) {
        printerID = id;
        graphicsConf = conf;
    }
    public int getType() {
        return TYPE_PRINTER;
    }
    public String getIDstring() {
        return printerID;
    }
    public GraphicsConfiguration[] getConfigurations() {
        GraphicsConfiguration[] confs = new GraphicsConfiguration[1];
        confs[0] = graphicsConf;
        return confs;
    }
    public GraphicsConfiguration getDefaultConfiguration() {
        return graphicsConf;
    }
    public void setFullScreenWindow(Window w) {
    }
    public Window getFullScreenWindow() {
        return null;
    }
}
