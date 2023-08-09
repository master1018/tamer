public class ImageIcon implements Icon, Serializable, Accessible {
    transient private String filename;
    transient private URL location;
    transient Image image;
    transient int loadStatus = 0;
    ImageObserver imageObserver;
    String description = null;
    protected final static Component component;
    protected final static MediaTracker tracker;
    static {
        component = AccessController.doPrivileged(new PrivilegedAction<Component>() {
            public Component run() {
                try {
                    final Component component = createNoPermsComponent();
                    Field appContextField =
                            Component.class.getDeclaredField("appContext");
                    appContextField.setAccessible(true);
                    appContextField.set(component, null);
                    return component;
                } catch (Throwable e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
        tracker = new MediaTracker(component);
    }
    private static Component createNoPermsComponent() {
        return AccessController.doPrivileged(
                new PrivilegedAction<Component>() {
                    public Component run() {
                        return new Component() {
                        };
                    }
                },
                new AccessControlContext(new ProtectionDomain[]{
                        new ProtectionDomain(null, null)
                })
        );
    }
    private static int mediaTrackerID;
    private final static Object TRACKER_KEY = new StringBuilder("TRACKER_KEY");
    int width = -1;
    int height = -1;
    public ImageIcon(String filename, String description) {
        image = Toolkit.getDefaultToolkit().getImage(filename);
        if (image == null) {
            return;
        }
        this.filename = filename;
        this.description = description;
        loadImage(image);
    }
    @ConstructorProperties({"description"})
    public ImageIcon (String filename) {
        this(filename, filename);
    }
    public ImageIcon(URL location, String description) {
        image = Toolkit.getDefaultToolkit().getImage(location);
        if (image == null) {
            return;
        }
        this.location = location;
        this.description = description;
        loadImage(image);
    }
    public ImageIcon (URL location) {
        this(location, location.toExternalForm());
    }
    public ImageIcon(Image image, String description) {
        this(image);
        this.description = description;
    }
    public ImageIcon (Image image) {
        this.image = image;
        Object o = image.getProperty("comment", imageObserver);
        if (o instanceof String) {
            description = (String) o;
        }
        loadImage(image);
    }
    public ImageIcon (byte[] imageData, String description) {
        this.image = Toolkit.getDefaultToolkit().createImage(imageData);
        if (image == null) {
            return;
        }
        this.description = description;
        loadImage(image);
    }
    public ImageIcon (byte[] imageData) {
        this.image = Toolkit.getDefaultToolkit().createImage(imageData);
        if (image == null) {
            return;
        }
        Object o = image.getProperty("comment", imageObserver);
        if (o instanceof String) {
            description = (String) o;
        }
        loadImage(image);
    }
    public ImageIcon() {
    }
    protected void loadImage(Image image) {
        MediaTracker mTracker = getTracker();
        synchronized(mTracker) {
            int id = getNextID();
            mTracker.addImage(image, id);
            try {
                mTracker.waitForID(id, 0);
            } catch (InterruptedException e) {
                System.out.println("INTERRUPTED while loading Image");
            }
            loadStatus = mTracker.statusID(id, false);
            mTracker.removeImage(image, id);
            width = image.getWidth(imageObserver);
            height = image.getHeight(imageObserver);
        }
    }
    private int getNextID() {
        synchronized(getTracker()) {
            return ++mediaTrackerID;
        }
    }
    private MediaTracker getTracker() {
        Object trackerObj;
        AppContext ac = AppContext.getAppContext();
        synchronized(ac) {
            trackerObj = ac.get(TRACKER_KEY);
            if (trackerObj == null) {
                Component comp = new Component() {};
                trackerObj = new MediaTracker(comp);
                ac.put(TRACKER_KEY, trackerObj);
            }
        }
        return (MediaTracker) trackerObj;
    }
    public int getImageLoadStatus() {
        return loadStatus;
    }
    @Transient
    public Image getImage() {
        return image;
    }
    public void setImage(Image image) {
        this.image = image;
        loadImage(image);
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        if(imageObserver == null) {
           g.drawImage(image, x, y, c);
        } else {
           g.drawImage(image, x, y, imageObserver);
        }
    }
    public int getIconWidth() {
        return width;
    }
    public int getIconHeight() {
        return height;
    }
    public void setImageObserver(ImageObserver observer) {
        imageObserver = observer;
    }
    @Transient
    public ImageObserver getImageObserver() {
        return imageObserver;
    }
    public String toString() {
        if (description != null) {
            return description;
        }
        return super.toString();
    }
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException
    {
        s.defaultReadObject();
        int w = s.readInt();
        int h = s.readInt();
        int[] pixels = (int[])(s.readObject());
        if (pixels != null) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            ColorModel cm = ColorModel.getRGBdefault();
            image = tk.createImage(new MemoryImageSource(w, h, cm, pixels, 0, w));
            loadImage(image);
        }
    }
    private void writeObject(ObjectOutputStream s)
        throws IOException
    {
        s.defaultWriteObject();
        int w = getIconWidth();
        int h = getIconHeight();
        int[] pixels = image != null? new int[w * h] : null;
        if (image != null) {
            try {
                PixelGrabber pg = new PixelGrabber(image, 0, 0, w, h, pixels, 0, w);
                pg.grabPixels();
                if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
                    throw new IOException("failed to load image contents");
                }
            }
            catch (InterruptedException e) {
                throw new IOException("image load interrupted");
            }
        }
        s.writeInt(w);
        s.writeInt(h);
        s.writeObject(pixels);
    }
    private AccessibleImageIcon accessibleContext = null;
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleImageIcon();
        }
        return accessibleContext;
    }
    protected class AccessibleImageIcon extends AccessibleContext
        implements AccessibleIcon, Serializable {
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.ICON;
        }
        public AccessibleStateSet getAccessibleStateSet() {
            return null;
        }
        public Accessible getAccessibleParent() {
            return null;
        }
        public int getAccessibleIndexInParent() {
            return -1;
        }
        public int getAccessibleChildrenCount() {
            return 0;
        }
        public Accessible getAccessibleChild(int i) {
            return null;
        }
        public Locale getLocale() throws IllegalComponentStateException {
            return null;
        }
        public String getAccessibleIconDescription() {
            return ImageIcon.this.getDescription();
        }
        public void setAccessibleIconDescription(String description) {
            ImageIcon.this.setDescription(description);
        }
        public int getAccessibleIconHeight() {
            return ImageIcon.this.height;
        }
        public int getAccessibleIconWidth() {
            return ImageIcon.this.width;
        }
        private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException
        {
            s.defaultReadObject();
        }
        private void writeObject(ObjectOutputStream s)
            throws IOException
        {
            s.defaultWriteObject();
        }
    }  
}
