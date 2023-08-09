public class PBEKeySpec implements KeySpec {
    private char[] password;
    private final byte[] salt;
    private final int iterationCount;
    private final int keyLength;
    public PBEKeySpec(char[] password) {
        if (password == null) {
            this.password = new char[0];
        } else {
            this.password = new char[password.length];
            System.arraycopy(password, 0, this.password, 0, password.length);
        }
        salt = null;
        iterationCount = 0;
        keyLength = 0;
    }
    public PBEKeySpec(char[] password, byte[] salt, int iterationCount,
                      int keyLength) {
        if (salt == null) {
            throw new NullPointerException(Messages.getString("crypto.3B")); 
        }
        if (salt.length == 0) {
            throw new IllegalArgumentException(Messages.getString("crypto.3C")); 
        }
        if (iterationCount <= 0) {
            throw new IllegalArgumentException(
                    Messages.getString("crypto.3D")); 
        }
        if (keyLength <= 0) {
            throw new IllegalArgumentException(Messages.getString("crypto.3E")); 
        }
        if (password == null) {
            this.password = new char[0];
        } else {
            this.password = new char[password.length];
            System.arraycopy(password, 0, this.password, 0, password.length);
        }
        this.salt = new byte[salt.length];
        System.arraycopy(salt, 0, this.salt, 0, salt.length);
        this.iterationCount = iterationCount;
        this.keyLength = keyLength;
    }
    public PBEKeySpec(char[] password, byte[] salt, int iterationCount) {
        if (salt == null) {
            throw new NullPointerException(Messages.getString("crypto.3B")); 
        }
        if (salt.length == 0) {
            throw new IllegalArgumentException(Messages.getString("crypto.3C")); 
        }
        if (iterationCount <= 0) {
            throw new IllegalArgumentException(
                    Messages.getString("crypto.3D")); 
        }
        if (password == null) {
            this.password = new char[0];
        } else {
            this.password = new char[password.length];
            System.arraycopy(password, 0, this.password, 0, password.length);
        }
        this.salt = new byte[salt.length];
        System.arraycopy(salt, 0, this.salt, 0, salt.length);
        this.iterationCount = iterationCount;
        this.keyLength = 0;
    }
    public final void clearPassword() {
        Arrays.fill(password, '?');
        password = null;
    }
    public final char[] getPassword() {
        if (password == null) {
            throw new IllegalStateException(Messages.getString("crypto.3F")); 
        }
        char[] result = new char[password.length];
        System.arraycopy(password, 0, result, 0, password.length);
        return result;
    }
    public final byte[] getSalt() {
        if (salt == null) {
            return null;
        }
        byte[] result = new byte[salt.length];
        System.arraycopy(salt, 0, result, 0, salt.length);
        return result;
    }
    public final int getIterationCount() {
        return iterationCount;
    }
    public final int getKeyLength() {
        return keyLength;
    }
}
