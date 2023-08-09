public class InputStreamHelper {
    private static final Field BAIS_BUF;
    private static final Field BAIS_POS;
    static {
        final Field[] f = new Field[2];
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    f[0] = ByteArrayInputStream.class.getDeclaredField("buf"); 
                    f[0].setAccessible(true);
                    f[1] = ByteArrayInputStream.class.getDeclaredField("pos"); 
                    f[1].setAccessible(true);
                } catch (NoSuchFieldException nsfe) {
                    throw new InternalError(nsfe.getLocalizedMessage());
                }
                return null;
            }
        });
        BAIS_BUF = f[0];
        BAIS_POS = f[1];
    }
    static class ExposedByteArrayInputStream extends ByteArrayInputStream {
        public ExposedByteArrayInputStream(byte buf[]) {
            super(buf);
        }
        public ExposedByteArrayInputStream(byte buf[], int offset, int length) {
            super(buf, offset, length);
        }
        public synchronized byte[] expose() {
            if (pos == 0 && count == buf.length) {
                skip(count);
                return buf;
            }
            final int available = available();
            final byte[] buffer = new byte[available];
            System.arraycopy(buf, pos, buffer, 0, available);
            skip(available);
            return buffer;
        }
    }
    private static byte[] expose(ByteArrayInputStream bais) {
        byte[] buffer, buf;
        int pos;
        synchronized (bais) {
            int available = bais.available();
            try {
                buf = (byte[]) BAIS_BUF.get(bais);
                pos = BAIS_POS.getInt(bais);
            } catch (IllegalAccessException iae) {
                throw new InternalError(iae.getLocalizedMessage());
            }
            if (pos == 0 && available == buf.length) {
                buffer = buf;
            } else {
                buffer = new byte[available];
                System.arraycopy(buf, pos, buffer, 0, available);
            }
            bais.skip(available);
        }
        return buffer;
    }
    public static byte[] expose(InputStream is) throws IOException,
            UnsupportedOperationException {
        if (is instanceof ExposedByteArrayInputStream) {
            return ((ExposedByteArrayInputStream) is).expose();
        }
        if (is.getClass().equals(ByteArrayInputStream.class)) {
            return expose((ByteArrayInputStream) is);
        }
        throw new UnsupportedOperationException();
    }
    public static byte[] readFullyAndClose(InputStream is) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            int count = is.read(buffer);
            int nextByte = is.read();
            if (nextByte == -1) {
                byte[] dest = new byte[count];
                System.arraycopy(buffer, 0, dest, 0, count);
                return dest;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream(count * 2);
            baos.write(buffer, 0, count);
            baos.write(nextByte);
            while (true) {
                count = is.read(buffer);
                if (count == -1) {
                    return baos.toByteArray();
                }
                baos.write(buffer, 0, count);
            }
        } finally {
            is.close();
        }
    }
}
