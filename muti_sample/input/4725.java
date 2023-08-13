public class XSettings {
    private long serial = -1;
    public Map update(byte[] data) {
        return (new Update(data)).update();
    }
    class Update {
        private static final int LITTLE_ENDIAN = 0;
        private static final int BIG_ENDIAN    = 1;
        private static final int TYPE_INTEGER = 0;
        private static final int TYPE_STRING  = 1;
        private static final int TYPE_COLOR   = 2;
        private byte[] data;
        private int dlen;
        private int idx;
        private boolean isLittle;
        private long serial = -1;
        private int nsettings = 0;
        private boolean isValid;
        private HashMap updatedSettings;
        Update(byte[] data) {
            this.data = data;
            dlen = data.length;
            if (dlen < 12) {
                return;
            }
            idx = 0;
            isLittle = (getCARD8() == LITTLE_ENDIAN);
            idx = 4;
            serial = getCARD32();
            idx = 8;
            nsettings = getINT32();
            updatedSettings = new HashMap();
            isValid = true;
        }
        private void needBytes(int n)
            throws IndexOutOfBoundsException
        {
            if (idx + n <= dlen) {
                return;
            }
            throw new IndexOutOfBoundsException("at " + idx
                                                + " need " + n
                                                + " length " + dlen);
        }
        private int getCARD8()
            throws IndexOutOfBoundsException
        {
            needBytes(1);
            int val = data[idx] & 0xff;
            ++idx;
            return val;
        }
        private int getCARD16()
            throws IndexOutOfBoundsException
        {
            needBytes(2);
            int val;
            if (isLittle) {
                val = ((data[idx + 0] & 0xff)      )
                    | ((data[idx + 1] & 0xff) <<  8);
            } else {
                val = ((data[idx + 0] & 0xff) <<  8)
                    | ((data[idx + 1] & 0xff)      );
            }
            idx += 2;
            return val;
        }
        private int getINT32()
            throws IndexOutOfBoundsException
        {
            needBytes(4);
            int val;
            if (isLittle) {
                val = ((data[idx + 0] & 0xff)      )
                    | ((data[idx + 1] & 0xff) <<  8)
                    | ((data[idx + 2] & 0xff) << 16)
                    | ((data[idx + 3] & 0xff) << 24);
            } else {
                val = ((data[idx + 0] & 0xff) << 24)
                    | ((data[idx + 1] & 0xff) << 16)
                    | ((data[idx + 2] & 0xff) <<  8)
                    | ((data[idx + 3] & 0xff) <<  0);
            }
            idx += 4;
            return val;
        }
        private long getCARD32()
            throws IndexOutOfBoundsException
        {
            return getINT32() & 0x00000000ffffffffL;
        }
        private String getString(int len)
            throws IndexOutOfBoundsException
        {
            needBytes(len);
            String str = null;
            try {
                str = new String(data, idx, len, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
            idx = (idx + len + 3) & ~0x3;
            return str;
        }
        public Map update() {
            if (!isValid) {
                return null;
            }
            synchronized (XSettings.this) {
                long currentSerial = XSettings.this.serial;
                if (this.serial <= currentSerial) {
                    return null;
                }
                for (int i = 0; i < nsettings && idx < dlen; ++i) {
                    updateOne(currentSerial);
                }
                XSettings.this.serial = this.serial;
            }
            return updatedSettings;
        }
        private void updateOne(long currentSerial)
            throws IndexOutOfBoundsException,
                   IllegalArgumentException
        {
            int type = getCARD8();
            ++idx;              
            int nameLen = getCARD16();
            int nameIdx = idx;
            idx = (idx + nameLen + 3) & ~0x3; 
            long lastChanged = getCARD32();
            if (lastChanged <= currentSerial) { 
                if (type == TYPE_INTEGER) {
                    idx += 4;
                } else if (type == TYPE_STRING) {
                    int len = getINT32();
                    idx = (idx + len + 3) & ~0x3;
                } else if (type == TYPE_COLOR) {
                    idx += 8;   
                } else {
                    throw new IllegalArgumentException("Unknown type: "
                                                       + type);
                }
                return;
            }
            idx = nameIdx;
            String name = getString(nameLen);
            idx += 4;           
            Object value = null;
            if (type == TYPE_INTEGER) {
                value = Integer.valueOf(getINT32());
            }
            else if (type == TYPE_STRING) {
                value = getString(getINT32());
            }
            else if (type == TYPE_COLOR) {
                int r = getCARD16();
                int g = getCARD16();
                int b = getCARD16();
                int a = getCARD16();
                value = new Color(r / 65535.0f,
                                  g / 65535.0f,
                                  b / 65535.0f,
                                  a / 65535.0f);
            }
            else {
                throw new IllegalArgumentException("Unknown type: " + type);
            }
            if (name == null) {
                return;
            }
            updatedSettings.put(name, value);
        }
    } 
}
