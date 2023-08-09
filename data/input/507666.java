class ImageTransferHandler extends TransferHandler {
    private final MainFrame mainFrame;
    ImageTransferHandler(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
    @Override
    public boolean importData(JComponent component, Transferable transferable) {
        try {
            for (DataFlavor flavor : transferable.getTransferDataFlavors()) {
                if (flavor.isFlavorJavaFileListType()) {
                    Object data = transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    final File file = ((List<File>) data).get(0);
                    mainFrame.open(file).execute();
                    return true;
                } else if (flavor.isFlavorTextType()) {
                    if (flavor.getRepresentationClass() == String.class) {
                        String mime = flavor.getMimeType();
                        DataFlavor flave = new DataFlavor(mime);
                        Object data = transferable.getTransferData(flave);
                        final String path = convertPath(data.toString());
                        mainFrame.open(new File(path)).execute();
                        return true;
                    }
                }
            }
        } catch (UnsupportedFlavorException e) {
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private static String convertPath(String path) {
        if (path.startsWith("file:
        if (path.indexOf('\n') != -1) path = path.substring(0, path.indexOf('\n'));
        if (path.indexOf('\r') != -1) path = path.substring(0, path.indexOf('\r'));
        return path;
    }
    @Override
    public boolean canImport(JComponent component, DataFlavor[] dataFlavors) {
        for (DataFlavor flavor : dataFlavors) {
            if (flavor.isFlavorJavaFileListType() || flavor.isFlavorTextType()) {
                return true;
            }
        }
        return false;
    }
}
