class XTextTransferHelper {
    private static Object transferHandlerKey = null;
    static Object getTransferHandlerKey() {
        if (transferHandlerKey == null) {
            try {
                Class clazz = Class.forName("javax.swing.ClientPropertyKey");
                Field field = SunToolkit.getField(clazz, "JComponent_TRANSFER_HANDLER");
                transferHandlerKey = field.get(null);
            } catch (IllegalAccessException ex) {
                return null;
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
        return transferHandlerKey;
    }
}
