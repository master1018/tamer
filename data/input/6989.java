public class KeyTabInputStream extends KrbDataInputStream implements KeyTabConstants {
    boolean DEBUG = Krb5.DEBUG;
    int index;
    public KeyTabInputStream(InputStream is) {
        super(is);
    }
    int readEntryLength() throws IOException {
        return read(4);
    }
    KeyTabEntry readEntry(int entryLen, int ktVersion) throws IOException, RealmException {
        index = entryLen;
        if (index == 0) {    
            return null;
        }
        if (index < 0) {    
            skip(Math.abs(index));                
            return null;
        }
        int principalNum = read(2);     
        index -= 2;
        if (ktVersion == KRB5_KT_VNO_1) {   
            principalNum -= 1;
        }
        Realm realm = new Realm(readName());
        String[] nameParts = new String[principalNum];
        for (int i = 0; i < principalNum; i++) {
            nameParts[i] = readName();
        }
        int nameType = read(4);
        index -= 4;
        PrincipalName service = new PrincipalName(nameParts, nameType);
        service.setRealm(realm);
        KerberosTime timeStamp = readTimeStamp();
        int keyVersion = read() & 0xff;
        index -= 1;
        int keyType = read(2);
        index -= 2;
        int keyLength = read(2);
        index -= 2;
        byte[] keyblock = readKey(keyLength);
        index -= keyLength;
        if (index >= 4) {
            int extKvno = read(4);
            if (extKvno != 0) {
                keyVersion = extKvno;
            }
            index -= 4;
        }
        if (index < 0) {
            throw new RealmException("Keytab is corrupted");
        }
        skip(index);
        return new KeyTabEntry(service, realm, timeStamp, keyVersion, keyType, keyblock);
    }
    byte[] readKey(int length) throws IOException {
        byte[] bytes = new byte[length];
        read(bytes, 0, length);
        return bytes;
    }
    KerberosTime readTimeStamp() throws IOException {
        index -= 4;
        return new KerberosTime((long)read(4) * 1000);
    }
    String readName() throws IOException {
        String name;
        int length = read(2);   
        index -= 2;
        byte[] bytes = new byte[length];
        read(bytes, 0, length);
        index -= length;
        name = new String(bytes);
        if (DEBUG) {
            System.out.println(">>> KeyTabInputStream, readName(): " + name);
        }
        return name;
    }
}
