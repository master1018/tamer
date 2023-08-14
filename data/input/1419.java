public class ClipboardTransferable implements Transferable {
    private final HashMap flavorsToData = new HashMap();
    private DataFlavor[] flavors = new DataFlavor[0];
    private final class DataFactory {
        final long format;
        final byte[] data;
        DataFactory(long format, byte[] data) {
            this.format = format;
            this.data   = data;
        }
        public Object getTransferData(DataFlavor flavor) throws IOException {
            return DataTransferer.getInstance().
                translateBytes(data, flavor, format,
                               ClipboardTransferable.this);
        }
    }
    public ClipboardTransferable(SunClipboard clipboard) {
        clipboard.openClipboard(null);
        try {
            long[] formats = clipboard.getClipboardFormats();
            if (formats != null && formats.length > 0) {
                HashMap cached_data = new HashMap(formats.length, 1.0f);
                Map flavorsForFormats = DataTransferer.getInstance().
                    getFlavorsForFormats(formats, SunClipboard.flavorMap);
                for (Iterator iter = flavorsForFormats.keySet().iterator();
                     iter.hasNext(); )
                {
                    DataFlavor flavor = (DataFlavor)iter.next();
                    Long lFormat = (Long)flavorsForFormats.get(flavor);
                    fetchOneFlavor(clipboard, flavor, lFormat, cached_data);
                }
                flavors = DataTransferer.getInstance().
                    setToSortedDataFlavorArray(flavorsToData.keySet(),
                                               flavorsForFormats);
            }
        } finally {
            clipboard.closeClipboard();
        }
    }
    private boolean fetchOneFlavor(SunClipboard clipboard, DataFlavor flavor,
                                   Long lFormat, HashMap cached_data)
    {
        if (!flavorsToData.containsKey(flavor)) {
            long format = lFormat.longValue();
            Object data = null;
            if (!cached_data.containsKey(lFormat)) {
                try {
                    data = clipboard.getClipboardData(format);
                } catch (IOException e) {
                    data = e;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                cached_data.put(lFormat, data);
            } else {
                data = cached_data.get(lFormat);
            }
            if (data instanceof IOException) {
                flavorsToData.put(flavor, data);
                return false;
            } else if (data != null) {
                flavorsToData.put(flavor, new DataFactory(format,
                                                          (byte[])data));
                return true;
            }
        }
        return false;
    }
    public DataFlavor[] getTransferDataFlavors() {
        return (DataFlavor[])flavors.clone();
    }
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavorsToData.containsKey(flavor);
    }
    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException
    {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        Object ret = flavorsToData.get(flavor);
        if (ret instanceof IOException) {
            throw (IOException)ret;
        } else if (ret instanceof DataFactory) {
            DataFactory factory = (DataFactory)ret;
            ret = factory.getTransferData(flavor);
        }
        return ret;
    }
}
