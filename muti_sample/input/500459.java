public class ElementDescTransfer extends ByteArrayTransfer {
    private static final String TYPE_NAME = "android.ADT.element.desc.transfer.1";
    private static final int TYPE_ID = registerType(TYPE_NAME);
    private static final ElementDescTransfer sInstance = new ElementDescTransfer();
    private ElementDescTransfer() {
    }
    public static ElementDescTransfer getInstance() {
        return sInstance;
    }
    public static String getFqcn(ElementDescriptor desc) {
        if (desc instanceof ViewElementDescriptor) {
            return ((ViewElementDescriptor) desc).getFullClassName();
        } else if (desc != null) {
            return desc.getXmlName();
        }
        return null;
    }
    @Override
    protected int[] getTypeIds() {
        return new int[] { TYPE_ID };
    }
    @Override
    protected String[] getTypeNames() {
        return new String[] { TYPE_NAME };
    }
    @Override
    protected void javaToNative(Object object, TransferData transferData) {
        if (object == null || !(object instanceof ElementDescriptor)) {
            return;
        }
        if (isSupportedType(transferData)) {
            String data = getFqcn((ElementDescriptor)object);
            if (data != null) {
                try {
                    byte[] buf = data.getBytes("UTF-8");  
                    super.javaToNative(buf, transferData);
                } catch (UnsupportedEncodingException e) {
                }
            }
        }
    }
    @Override
    protected Object nativeToJava(TransferData transferData) {
        if (isSupportedType(transferData)) {
            byte[] buf = (byte[]) super.nativeToJava(transferData);
            if (buf != null && buf.length > 0) {
                try {
                    String s = new String(buf, "UTF-8"); 
                    return s;
                } catch (UnsupportedEncodingException e) {
                }
            }
        }
        return null;
    }
}
