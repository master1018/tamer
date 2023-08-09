public class RenderableImageProducer implements ImageProducer, Runnable {
    RenderableImage rbl;
    RenderContext rc;
    Vector<ImageConsumer> consumers = new Vector<ImageConsumer>();
    public RenderableImageProducer(RenderableImage rdblImage, RenderContext rc) {
        this.rbl = rdblImage;
        this.rc = rc;
    }
    public synchronized void setRenderContext(RenderContext rc) {
        this.rc = rc;
    }
    public synchronized boolean isConsumer(ImageConsumer ic) {
        return consumers.contains(ic);
    }
    public synchronized void startProduction(ImageConsumer ic) {
        addConsumer(ic);
        Thread t = new Thread(this, "RenderableImageProducer thread"); 
        t.start();
    }
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }
    public synchronized void removeConsumer(ImageConsumer ic) {
        if (ic != null) {
            consumers.removeElement(ic);
        }
    }
    public synchronized void addConsumer(ImageConsumer ic) {
        if (ic != null && !consumers.contains(ic)) {
            consumers.addElement(ic);
        }
    }
    public void run() {
        if (rbl == null) {
            return;
        }
        RenderedImage rd;
        if (rc != null) {
            rd = rbl.createRendering(rc);
        } else {
            rd = rbl.createDefaultRendering();
        }
        ColorModel cm = rd.getColorModel();
        if (cm == null) {
            cm = ColorModel.getRGBdefault();
        }
        Raster r = rd.getData();
        int w = r.getWidth();
        int h = r.getHeight();
        for (ImageConsumer c : consumers) {
            c.setDimensions(w, h);
            c.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES
                    | ImageConsumer.SINGLEFRAME | ImageConsumer.SINGLEPASS);
        }
        int scanLine[] = new int[w];
        int pixel[] = null;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pixel = r.getPixel(x, y, pixel);
                scanLine[x] = cm.getDataElement(pixel, 0);
            }
            for (ImageConsumer c : consumers) {
                c.setPixels(0, y, w, 1, cm, scanLine, 0, w);
            }
        }
        for (ImageConsumer c : consumers) {
            c.imageComplete(ImageConsumer.STATICIMAGEDONE);
        }
    }
}
