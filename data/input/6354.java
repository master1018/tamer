public class ToolkitImage extends Image {
    ImageProducer source;
    InputStreamImageSource src;
    ImageRepresentation imagerep;
    static {
        NativeLibLoader.loadLibraries();
    }
    protected ToolkitImage() {
    }
    public ToolkitImage(ImageProducer is) {
        source = is;
        if (is instanceof InputStreamImageSource) {
            src = (InputStreamImageSource) is;
        }
    }
    public ImageProducer getSource() {
        if (src != null) {
            src.checkSecurity(null, false);
        }
        return source;
    }
    private int width = -1;
    private int height = -1;
    private Hashtable properties;
    private int availinfo;
    public int getWidth() {
        if (src != null) {
            src.checkSecurity(null, false);
        }
        if ((availinfo & ImageObserver.WIDTH) == 0) {
            reconstruct(ImageObserver.WIDTH);
        }
        return width;
    }
    public synchronized int getWidth(ImageObserver iw) {
        if (src != null) {
            src.checkSecurity(null, false);
        }
        if ((availinfo & ImageObserver.WIDTH) == 0) {
            addWatcher(iw, true);
            if ((availinfo & ImageObserver.WIDTH) == 0) {
                return -1;
            }
        }
        return width;
    }
    public int getHeight() {
        if (src != null) {
            src.checkSecurity(null, false);
        }
        if ((availinfo & ImageObserver.HEIGHT) == 0) {
            reconstruct(ImageObserver.HEIGHT);
        }
        return height;
    }
    public synchronized int getHeight(ImageObserver iw) {
        if (src != null) {
            src.checkSecurity(null, false);
        }
        if ((availinfo & ImageObserver.HEIGHT) == 0) {
            addWatcher(iw, true);
            if ((availinfo & ImageObserver.HEIGHT) == 0) {
                return -1;
            }
        }
        return height;
    }
    public Object getProperty(String name, ImageObserver observer) {
        if (name == null) {
            throw new NullPointerException("null property name is not allowed");
        }
        if (src != null) {
            src.checkSecurity(null, false);
        }
        if (properties == null) {
            addWatcher(observer, true);
            if (properties == null) {
                return null;
            }
        }
        Object o = properties.get(name);
        if (o == null) {
            o = Image.UndefinedProperty;
        }
        return o;
    }
    public boolean hasError() {
        if (src != null) {
            src.checkSecurity(null, false);
        }
        return (availinfo & ImageObserver.ERROR) != 0;
    }
    public int check(ImageObserver iw) {
        if (src != null) {
            src.checkSecurity(null, false);
        }
        if ((availinfo & ImageObserver.ERROR) == 0 &&
            ((~availinfo) & (ImageObserver.WIDTH |
                             ImageObserver.HEIGHT |
                             ImageObserver.PROPERTIES)) != 0) {
            addWatcher(iw, false);
        }
        return availinfo;
    }
    public void preload(ImageObserver iw) {
        if (src != null) {
            src.checkSecurity(null, false);
        }
        if ((availinfo & ImageObserver.ALLBITS) == 0) {
            addWatcher(iw, true);
        }
    }
    private synchronized void addWatcher(ImageObserver iw, boolean load) {
        if ((availinfo & ImageObserver.ERROR) != 0) {
            if (iw != null) {
                iw.imageUpdate(this, ImageObserver.ERROR|ImageObserver.ABORT,
                               -1, -1, -1, -1);
            }
            return;
        }
        ImageRepresentation ir = getImageRep();
        ir.addWatcher(iw);
        if (load) {
            ir.startProduction();
        }
    }
    private synchronized void reconstruct(int flags) {
        if ((flags & ~availinfo) != 0) {
            if ((availinfo & ImageObserver.ERROR) != 0) {
                return;
            }
            ImageRepresentation ir = getImageRep();
            ir.startProduction();
            while ((flags & ~availinfo) != 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if ((availinfo & ImageObserver.ERROR) != 0) {
                    return;
                }
            }
        }
    }
    synchronized void addInfo(int newinfo) {
        availinfo |= newinfo;
        notifyAll();
    }
    void setDimensions(int w, int h) {
        width = w;
        height = h;
        addInfo(ImageObserver.WIDTH | ImageObserver.HEIGHT);
    }
    void setProperties(Hashtable props) {
        if (props == null) {
            props = new Hashtable();
        }
        properties = props;
        addInfo(ImageObserver.PROPERTIES);
    }
    synchronized void infoDone(int status) {
        if (status == ImageConsumer.IMAGEERROR ||
            ((~availinfo) & (ImageObserver.WIDTH |
                             ImageObserver.HEIGHT)) != 0) {
            addInfo(ImageObserver.ERROR);
        } else if ((availinfo & ImageObserver.PROPERTIES) == 0) {
            setProperties(null);
        }
    }
    public void flush() {
        if (src != null) {
            src.checkSecurity(null, false);
        }
        ImageRepresentation ir;
        synchronized (this) {
            availinfo &= ~ImageObserver.ERROR;
            ir = imagerep;
            imagerep = null;
        }
        if (ir != null) {
            ir.abort();
        }
        if (src != null) {
            src.flush();
        }
    }
    protected ImageRepresentation makeImageRep() {
        return new ImageRepresentation(this, ColorModel.getRGBdefault(),
                                       false);
    }
    public synchronized ImageRepresentation getImageRep() {
        if (src != null) {
            src.checkSecurity(null, false);
        }
        if (imagerep == null) {
            imagerep = makeImageRep();
        }
        return imagerep;
    }
    public Graphics getGraphics() {
        throw new UnsupportedOperationException("getGraphics() not valid for images " +
                                     "created with createImage(producer)");
    }
    public ColorModel getColorModel() {
        ImageRepresentation imageRep = getImageRep();
        return imageRep.getColorModel();
    }
    public BufferedImage getBufferedImage() {
        ImageRepresentation imageRep = getImageRep();
        return imageRep.getBufferedImage();
    }
    public void setAccelerationPriority(float priority) {
        super.setAccelerationPriority(priority);
        ImageRepresentation imageRep = getImageRep();
        imageRep.setAccelerationPriority(accelerationPriority);
    }
}
