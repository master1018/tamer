public abstract class ConnectionState {
    protected Cipher encCipher;
    protected Cipher decCipher;
    protected boolean is_block_cipher;
    protected int hash_size;
    protected final byte[] write_seq_num = {0, 0, 0, 0, 0, 0, 0, 0};
    protected final byte[] read_seq_num = {0, 0, 0, 0, 0, 0, 0, 0};
    protected Logger.Stream logger = Logger.getStream("conn_state");
    protected int getMinFragmentSize() {
        return encCipher.getOutputSize(1+hash_size); 
    }
    protected int getFragmentSize(int content_size) {
        return encCipher.getOutputSize(content_size+hash_size);
    }
    protected int getContentSize(int generic_cipher_size) {
        return decCipher.getOutputSize(generic_cipher_size)-hash_size;
    }
    protected byte[] encrypt(byte type, byte[] fragment) {
        return encrypt(type, fragment, 0, fragment.length);
    }
    protected abstract byte[] encrypt
        (byte type, byte[] fragment, int offset, int len);
    protected byte[] decrypt(byte type, byte[] fragment) {
        return decrypt(type, fragment, 0, fragment.length);
    }
    protected abstract byte[] decrypt
        (byte type, byte[] fragment, int offset, int len);
    protected static void incSequenceNumber(byte[] seq_num) {
        int octet = 7;
        while (octet >= 0) {
            seq_num[octet] ++;
            if (seq_num[octet] == 0) {
                octet --;
            } else {
                return;
            }
        }
    }
    protected void shutdown() {
        encCipher = null;
        decCipher = null;
        for (int i=0; i<write_seq_num.length; i++) {
            write_seq_num[i] = 0;
            read_seq_num[i] = 0;
        }
    }
}
