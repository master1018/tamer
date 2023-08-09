public class WClipboard extends SunClipboard {
    private boolean isClipboardViewerRegistered;
    public WClipboard() {
        super("System");
    }
    public long getID() {
        return 0;
    }
    protected void setContentsNative(Transferable contents) {
        Map formatMap = WDataTransferer.getInstance().
            getFormatsForTransferable(contents, flavorMap);
        openClipboard(this);
        try {
            for (Iterator iter = formatMap.keySet().iterator();
                 iter.hasNext(); ) {
                Long lFormat = (Long)iter.next();
                long format = lFormat.longValue();
                DataFlavor flavor = (DataFlavor)formatMap.get(lFormat);
                try {
                    byte[] bytes = WDataTransferer.getInstance().
                        translateTransferable(contents, flavor, format);
                    publishClipboardData(format, bytes);
                } catch (IOException e) {
                    if (!(flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType) &&
                          e instanceof java.io.NotSerializableException)) {
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            closeClipboard();
        }
    }
    private void lostSelectionOwnershipImpl() {
        lostOwnershipImpl();
    }
    protected void clearNativeContext() {}
    public native void openClipboard(SunClipboard newOwner) throws IllegalStateException;
    public native void closeClipboard();
    private native void publishClipboardData(long format, byte[] bytes);
    private static native void init();
    static {
        init();
    }
    protected native long[] getClipboardFormats();
    protected native byte[] getClipboardData(long format) throws IOException;
    protected void registerClipboardViewerChecked() {
        if (!isClipboardViewerRegistered) {
            registerClipboardViewer();
            isClipboardViewerRegistered = true;
        }
    }
    private native void registerClipboardViewer();
    protected void unregisterClipboardViewerChecked() {}
    private void handleContentsChanged() {
        if (!areFlavorListenersRegistered()) {
            return;
        }
        long[] formats = null;
        try {
            openClipboard(null);
            formats = getClipboardFormats();
        } catch (IllegalStateException exc) {
        } finally {
            closeClipboard();
        }
        checkChange(formats);
    }
    protected Transferable createLocaleTransferable(long[] formats) throws IOException {
        boolean found = false;
        for (int i = 0; i < formats.length; i++) {
            if (formats[i] == WDataTransferer.CF_LOCALE) {
                found = true;
                break;
            }
        }
        if (!found) {
            return null;
        }
        byte[] localeData = null;
        try {
            localeData = getClipboardData(WDataTransferer.CF_LOCALE);
        } catch (IOException ioexc) {
            return null;
        }
        final byte[] localeDataFinal = localeData;
        return new Transferable() {
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[] { DataTransferer.javaTextEncodingFlavor };
                }
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return flavor.equals(DataTransferer.javaTextEncodingFlavor);
                }
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    if (isDataFlavorSupported(flavor)) {
                        return localeDataFinal;
                    }
                    throw new UnsupportedFlavorException(flavor);
                }
            };
    }
}
