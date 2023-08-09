public class KeyTabOutputStream extends KrbDataOutputStream implements KeyTabConstants {
    private KeyTabEntry entry;
    private int keyType;
    private byte[] keyValue;
    public int version;
    public KeyTabOutputStream(OutputStream os) {
        super(os);
    }
    public void writeVersion(int num) throws IOException {
        version = num;
        write16(num);           
    }
    public void writeEntry(KeyTabEntry entry) throws IOException {
        write32(entry.entryLength());
        String[] serviceNames =  entry.service.getNameStrings();
        int comp_num = serviceNames.length;
        if (version == KRB5_KT_VNO_1) {
            write16(comp_num + 1);
        }
        else write16(comp_num);
        byte[] realm = null;
        try {
            realm = entry.service.getRealmString().getBytes("8859_1");
        } catch (UnsupportedEncodingException exc) {
        }
        write16(realm.length);
        write(realm);
        for (int i = 0; i < comp_num; i++) {
            try {
                write16(serviceNames[i].getBytes("8859_1").length);
                write(serviceNames[i].getBytes("8859_1"));
            } catch (UnsupportedEncodingException exc) {
            }
        }
        write32(entry.service.getNameType());
        write32((int)(entry.timestamp.getTime()/1000));
        write8(entry.keyVersion % 256 );
        write16(entry.keyType);
        write16(entry.keyblock.length);
        write(entry.keyblock);
    }
}
