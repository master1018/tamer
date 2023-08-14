public class PBEKeySpec implements KeySpec {
    private char[] password;
    private byte[] salt = null;
    private int iterationCount = 0;
    private int keyLength = 0;
    public PBEKeySpec(char[] password) {
        if ((password == null) || (password.length == 0)) {
            this.password = new char[0];
        } else {
            this.password = (char[])password.clone();
        }
    }
    public PBEKeySpec(char[] password, byte[] salt, int iterationCount,
        int keyLength) {
        if ((password == null) || (password.length == 0)) {
            this.password = new char[0];
        } else {
            this.password = (char[])password.clone();
        }
        if (salt == null) {
            throw new NullPointerException("the salt parameter " +
                                            "must be non-null");
        } else if (salt.length == 0) {
            throw new IllegalArgumentException("the salt parameter " +
                                                "must not be empty");
        } else {
            this.salt = (byte[]) salt.clone();
        }
        if (iterationCount<=0) {
            throw new IllegalArgumentException("invalid iterationCount value");
        }
        if (keyLength<=0) {
            throw new IllegalArgumentException("invalid keyLength value");
        }
        this.iterationCount = iterationCount;
        this.keyLength = keyLength;
    }
    public PBEKeySpec(char[] password, byte[] salt, int iterationCount) {
        if ((password == null) || (password.length == 0)) {
            this.password = new char[0];
        } else {
            this.password = (char[])password.clone();
        }
        if (salt == null) {
            throw new NullPointerException("the salt parameter " +
                                            "must be non-null");
        } else if (salt.length == 0) {
            throw new IllegalArgumentException("the salt parameter " +
                                                "must not be empty");
        } else {
            this.salt = (byte[]) salt.clone();
        }
        if (iterationCount<=0) {
            throw new IllegalArgumentException("invalid iterationCount value");
        }
        this.iterationCount = iterationCount;
    }
    public final void clearPassword() {
        if (password != null) {
            for (int i = 0; i < password.length; i++) {
                password[i] = ' ';
            }
            password = null;
        }
    }
    public final char[] getPassword() {
        if (password == null) {
            throw new IllegalStateException("password has been cleared");
        }
        return (char[]) password.clone();
    }
    public final byte[] getSalt() {
        if (salt != null) {
            return (byte[]) salt.clone();
        } else {
            return null;
        }
    }
    public final int getIterationCount() {
        return iterationCount;
    }
    public final int getKeyLength() {
        return keyLength;
    }
}
