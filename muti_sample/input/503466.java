public class ZipFile implements ZipConstants {
    public static final int OPEN_READ = 1;
    public static final int OPEN_DELETE = 4;
    private final String fileName;
    private File fileToDeleteOnClose;
    private RandomAccessFile mRaf;
    private final ZipEntry.LittleEndianReader ler = new ZipEntry.LittleEndianReader();
    private final LinkedHashMap<String, ZipEntry> mEntries
            = new LinkedHashMap<String, ZipEntry>();
    public ZipFile(File file) throws ZipException, IOException {
        this(file, OPEN_READ);
    }
    public ZipFile(File file, int mode) throws IOException {
        fileName = file.getPath();
        if (mode != OPEN_READ && mode != (OPEN_READ | OPEN_DELETE)) {
            throw new IllegalArgumentException();
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(fileName);
        }
        if ((mode & OPEN_DELETE) != 0) {
            if (security != null) {
                security.checkDelete(fileName);
            }
            fileToDeleteOnClose = file; 
        } else {
            fileToDeleteOnClose = null;
        }
        mRaf = new RandomAccessFile(fileName, "r");
        readCentralDir();
    }
    public ZipFile(String name) throws IOException {
        this(new File(name), OPEN_READ);
    }
    @Override
    protected void finalize() throws IOException {
        close();
    }
    public void close() throws IOException {
        RandomAccessFile raf = mRaf;
        if (raf != null) { 
            synchronized(raf) {
                mRaf = null;
                raf.close();
            }
            if (fileToDeleteOnClose != null) {
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        new File(fileName).delete();
                        return null;
                    }
                });
                fileToDeleteOnClose = null;
            }
        }
    }
    private void checkNotClosed() {
        if (mRaf == null) {
            throw new IllegalStateException("Zip File closed.");
        }
    }
    public Enumeration<? extends ZipEntry> entries() {
        checkNotClosed();
        final Iterator<ZipEntry> iterator = mEntries.values().iterator();
        return new Enumeration<ZipEntry>() {
            public boolean hasMoreElements() {
                checkNotClosed();
                return iterator.hasNext();
            }
            public ZipEntry nextElement() {
                checkNotClosed();
                return iterator.next();
            }
        };
    }
    public ZipEntry getEntry(String entryName) {
        checkNotClosed();
        if (entryName == null) {
            throw new NullPointerException();
        }
        ZipEntry ze = mEntries.get(entryName);
        if (ze == null) {
            ze = mEntries.get(entryName + "/");
        }
        return ze;
    }
    public InputStream getInputStream(ZipEntry entry) throws IOException {
        entry = getEntry(entry.getName());
        if (entry == null) {
            return null;
        }
        RandomAccessFile raf = mRaf;
        synchronized (raf) {
            RAFStream rafstrm = new RAFStream(raf,
                    entry.mLocalHeaderRelOffset + 28);
            int localExtraLenOrWhatever = ler.readShortLE(rafstrm);
            rafstrm.skip(entry.nameLen + localExtraLenOrWhatever);
            rafstrm.mLength = rafstrm.mOffset + entry.compressedSize;
            if (entry.compressionMethod == ZipEntry.DEFLATED) {
                int bufSize = Math.max(1024, (int)Math.min(entry.getSize(), 65535L));
                return new ZipInflaterInputStream(rafstrm, new Inflater(true), bufSize, entry);
            } else {
                return rafstrm;
            }
        }
    }
    public String getName() {
        return fileName;
    }
    public int size() {
        checkNotClosed();
        return mEntries.size();
    }
    private void readCentralDir() throws IOException {
        long scanOffset = mRaf.length() - ENDHDR;
        if (scanOffset < 0) {
            throw new ZipException("too short to be Zip");
        }
        long stopOffset = scanOffset - 65536;
        if (stopOffset < 0) {
            stopOffset = 0;
        }
        while (true) {
            mRaf.seek(scanOffset);
            if (ZipEntry.readIntLE(mRaf) == 101010256L) {
                break;
            }
            scanOffset--;
            if (scanOffset < stopOffset) {
                throw new ZipException("EOCD not found; not a Zip archive?");
            }
        }
        RAFStream rafs = new RAFStream(mRaf, mRaf.getFilePointer());
        BufferedInputStream bin = new BufferedInputStream(rafs, ENDHDR);
        int diskNumber = ler.readShortLE(bin);
        int diskWithCentralDir = ler.readShortLE(bin);
        int numEntries = ler.readShortLE(bin);
        int totalNumEntries = ler.readShortLE(bin);
         ler.readIntLE(bin);
        long centralDirOffset = ler.readIntLE(bin);
         ler.readShortLE(bin);
        if (numEntries != totalNumEntries ||
            diskNumber != 0 ||
            diskWithCentralDir != 0) {
            throw new ZipException("spanned archives not supported");
        }
        rafs = new RAFStream(mRaf, centralDirOffset);
        bin = new BufferedInputStream(rafs, 4096);
        for (int i = 0; i < numEntries; i++) {
            ZipEntry newEntry = new ZipEntry(ler, bin);
            mEntries.put(newEntry.getName(), newEntry);
        }
    }
    static class RAFStream extends InputStream {
        RandomAccessFile mSharedRaf;
        long mOffset;
        long mLength;
        public RAFStream(RandomAccessFile raf, long pos) throws IOException {
            mSharedRaf = raf;
            mOffset = pos;
            mLength = raf.length();
        }
        @Override
        public int available() throws IOException {
            return (mOffset < mLength ? 1 : 0);
        }
        @Override
        public int read() throws IOException {
            byte[] singleByteBuf = new byte[1];
            if (read(singleByteBuf, 0, 1) == 1) {
                return singleByteBuf[0] & 0XFF;
            } else {
                return -1;
            }
        }
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            synchronized (mSharedRaf) {
                mSharedRaf.seek(mOffset);
                if (len > mLength - mOffset) {
                    len = (int) (mLength - mOffset);
                }
                int count = mSharedRaf.read(b, off, len);
                if (count > 0) {
                    mOffset += count;
                    return count;
                } else {
                    return -1;
                }
            }
        }
        @Override
        public long skip(long n) throws IOException {
            if (n > mLength - mOffset) {
                n = mLength - mOffset;
            }
            mOffset += n;
            return n;
        }
    }
    static class ZipInflaterInputStream extends InflaterInputStream {
        ZipEntry entry;
        long bytesRead = 0;
        public ZipInflaterInputStream(InputStream is, Inflater inf, int bsize, ZipEntry entry) {
            super(is, inf, bsize);
            this.entry = entry;
        }
        @Override
        public int read(byte[] buffer, int off, int nbytes) throws IOException {
            int i = super.read(buffer, off, nbytes);
            if (i != -1) {
                bytesRead += i;
            }
            return i;
        }
        @Override
        public int available() throws IOException {
            return super.available() == 0 ? 0 : (int) (entry.getSize() - bytesRead);
        }
    }
}
