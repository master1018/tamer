public class RandomBitsSupplier implements SHA1_Data {
    private static FileInputStream bis = null;
    private static File randomFile = null;
    private static boolean serviceAvailable = false;
    static {
        AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    for ( int i = 0 ; i < DEVICE_NAMES.length ; i++ ) {
                        File file = new File(DEVICE_NAMES[i]);
                        try {
                            if ( file.canRead() ) {
                                bis = new FileInputStream(file);
                                randomFile = file;
                                serviceAvailable = true;
                                return null;
                            }
                        } catch (FileNotFoundException e) {
                        }
                    }
                    return null;
                }
            }
        );
    }
    static boolean isServiceAvailable() {
        return serviceAvailable;
    }
    private static synchronized byte[] getUnixDeviceRandom(int numBytes) {
        byte[] bytes = new byte[numBytes];
        int total = 0;
        int bytesRead;
        int offset = 0;
        try {
            for ( ; ; ) {
                bytesRead = bis.read(bytes, offset, numBytes-total);
                if ( bytesRead == -1 ) {
                    throw new ProviderException(
                        Messages.getString("security.193") ); 
                }
                total  += bytesRead;
                offset += bytesRead;
                if ( total >= numBytes ) {
                    break;
                }          
            }
        } catch (IOException e) {
            throw new ProviderException(
                Messages.getString("security.194"), e ); 
        }
        return bytes; 
    }
    public static byte[] getRandomBits(int numBytes) {
        if ( numBytes <= 0 ) {
            throw new IllegalArgumentException(Messages.getString("security.195", numBytes)); 
        }
        if ( !serviceAvailable ) {
            throw new ProviderException(
                Messages.getString("security.196")); 
        }
        return getUnixDeviceRandom(numBytes);
    }
}
