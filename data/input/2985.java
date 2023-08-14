public abstract class SunGraphicsEnvironment extends GraphicsEnvironment
    implements DisplayChangedListener {
    public static boolean isOpenSolaris;
    private static Font defaultFont;
    public SunGraphicsEnvironment() {
        java.security.AccessController.doPrivileged(
                                    new java.security.PrivilegedAction() {
            public Object run() {
                    String version = System.getProperty("os.version", "0.0");
                    try {
                        float ver = Float.parseFloat(version);
                        if (ver > 5.10f) {
                            File f = new File("/etc/release");
                            FileInputStream fis = new FileInputStream(f);
                            InputStreamReader isr
                                = new InputStreamReader(fis, "ISO-8859-1");
                            BufferedReader br = new BufferedReader(isr);
                            String line = br.readLine();
                            if (line.indexOf("OpenSolaris") >= 0) {
                                isOpenSolaris = true;
                            } else {
                                String courierNew =
                                    "/usr/openwin/lib/X11/fonts/TrueType/CourierNew.ttf";
                                File courierFile = new File(courierNew);
                                isOpenSolaris = !courierFile.exists();
                            }
                            fis.close();
                        }
                    } catch (Exception e) {
                    }
                defaultFont = new Font(Font.DIALOG, Font.PLAIN, 12);
                return null;
            }
        });
    }
    protected GraphicsDevice[] screens;
    public synchronized GraphicsDevice[] getScreenDevices() {
        GraphicsDevice[] ret = screens;
        if (ret == null) {
            int num = getNumScreens();
            ret = new GraphicsDevice[num];
            for (int i = 0; i < num; i++) {
                ret[i] = makeScreenDevice(i);
            }
            screens = ret;
        }
        return ret;
    }
    protected abstract int getNumScreens();
    protected abstract GraphicsDevice makeScreenDevice(int screennum);
    public GraphicsDevice getDefaultScreenDevice() {
        return getScreenDevices()[0];
    }
    public Graphics2D createGraphics(BufferedImage img) {
        if (img == null) {
            throw new NullPointerException("BufferedImage cannot be null");
        }
        SurfaceData sd = SurfaceData.getPrimarySurfaceData(img);
        return new SunGraphics2D(sd, Color.white, Color.black, defaultFont);
    }
    public static FontManagerForSGE getFontManagerForSGE() {
        FontManager fm = FontManagerFactory.getInstance();
        return (FontManagerForSGE) fm;
    }
    public static void useAlternateFontforJALocales() {
        getFontManagerForSGE().useAlternateFontforJALocales();
    }
    public Font[] getAllFonts() {
        FontManagerForSGE fm = getFontManagerForSGE();
        Font[] installedFonts = fm.getAllInstalledFonts();
        Font[] created = fm.getCreatedFonts();
        if (created == null || created.length == 0) {
            return installedFonts;
        } else {
            int newlen = installedFonts.length + created.length;
            Font [] fonts = java.util.Arrays.copyOf(installedFonts, newlen);
            System.arraycopy(created, 0, fonts,
                             installedFonts.length, created.length);
            return fonts;
        }
    }
    public String[] getAvailableFontFamilyNames(Locale requestedLocale) {
        FontManagerForSGE fm = getFontManagerForSGE();
        String[] installed = fm.getInstalledFontFamilyNames(requestedLocale);
        TreeMap<String, String> map = fm.getCreatedFontFamilyNames();
        if (map == null || map.size() == 0) {
            return installed;
        } else {
            for (int i=0; i<installed.length; i++) {
                map.put(installed[i].toLowerCase(requestedLocale),
                        installed[i]);
            }
            String[] retval =  new String[map.size()];
            Object [] keyNames = map.keySet().toArray();
            for (int i=0; i < keyNames.length; i++) {
                retval[i] = (String)map.get(keyNames[i]);
            }
            return retval;
        }
    }
    public String[] getAvailableFontFamilyNames() {
        return getAvailableFontFamilyNames(Locale.getDefault());
    }
    public static Rectangle getUsableBounds(GraphicsDevice gd) {
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        Rectangle usableBounds = gc.getBounds();
        usableBounds.x += insets.left;
        usableBounds.y += insets.top;
        usableBounds.width -= (insets.left + insets.right);
        usableBounds.height -= (insets.top + insets.bottom);
        return usableBounds;
    }
    public void displayChanged() {
        for (GraphicsDevice gd : getScreenDevices()) {
            if (gd instanceof DisplayChangedListener) {
                ((DisplayChangedListener) gd).displayChanged();
            }
        }
        displayChanger.notifyListeners();
    }
    public void paletteChanged() {
        displayChanger.notifyPaletteChanged();
    }
    public abstract boolean isDisplayLocal();
    protected SunDisplayChanger displayChanger = new SunDisplayChanger();
    public void addDisplayChangedListener(DisplayChangedListener client) {
        displayChanger.add(client);
    }
    public void removeDisplayChangedListener(DisplayChangedListener client) {
        displayChanger.remove(client);
    }
    public boolean isFlipStrategyPreferred(ComponentPeer peer) {
        return false;
    }
}
