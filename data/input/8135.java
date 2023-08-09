public class ZipFileIndex {
    private static final String MIN_CHAR = String.valueOf(Character.MIN_VALUE);
    private static final String MAX_CHAR = String.valueOf(Character.MAX_VALUE);
    public final static long NOT_MODIFIED = Long.MIN_VALUE;
    private static boolean NON_BATCH_MODE = System.getProperty("nonBatchMode") != null;
    private Map<RelativeDirectory, DirectoryEntry> directories =
            Collections.<RelativeDirectory, DirectoryEntry>emptyMap();
    private Set<RelativeDirectory> allDirs =
            Collections.<RelativeDirectory>emptySet();
    final File zipFile;
    private Reference<File> absFileRef;
    long zipFileLastModified = NOT_MODIFIED;
    private RandomAccessFile zipRandomFile;
    private Entry[] entries;
    private boolean readFromIndex = false;
    private File zipIndexFile = null;
    private boolean triedToReadIndex = false;
    final RelativeDirectory symbolFilePrefix;
    private final int symbolFilePrefixLength;
    private boolean hasPopulatedData = false;
    long lastReferenceTimeStamp = NOT_MODIFIED;
    private final boolean usePreindexedCache;
    private final String preindexedCacheLocation;
    private boolean writeIndex = false;
    private Map<String, SoftReference<RelativeDirectory>> relativeDirectoryCache =
            new HashMap<String, SoftReference<RelativeDirectory>>();
    public synchronized boolean isOpen() {
        return (zipRandomFile != null);
    }
    ZipFileIndex(File zipFile, RelativeDirectory symbolFilePrefix, boolean writeIndex,
            boolean useCache, String cacheLocation) throws IOException {
        this.zipFile = zipFile;
        this.symbolFilePrefix = symbolFilePrefix;
        this.symbolFilePrefixLength = (symbolFilePrefix == null ? 0 :
            symbolFilePrefix.getPath().getBytes("UTF-8").length);
        this.writeIndex = writeIndex;
        this.usePreindexedCache = useCache;
        this.preindexedCacheLocation = cacheLocation;
        if (zipFile != null) {
            this.zipFileLastModified = zipFile.lastModified();
        }
        checkIndex();
    }
    @Override
    public String toString() {
        return "ZipFileIndex[" + zipFile + "]";
    }
    @Override
    protected void finalize() throws Throwable {
        closeFile();
        super.finalize();
    }
    private boolean isUpToDate() {
        if (zipFile != null
                && ((!NON_BATCH_MODE) || zipFileLastModified == zipFile.lastModified())
                && hasPopulatedData) {
            return true;
        }
        return false;
    }
    private void checkIndex() throws IOException {
        boolean isUpToDate = true;
        if (!isUpToDate()) {
            closeFile();
            isUpToDate = false;
        }
        if (zipRandomFile != null || isUpToDate) {
            lastReferenceTimeStamp = System.currentTimeMillis();
            return;
        }
        hasPopulatedData = true;
        if (readIndex()) {
            lastReferenceTimeStamp = System.currentTimeMillis();
            return;
        }
        directories = Collections.<RelativeDirectory, DirectoryEntry>emptyMap();
        allDirs = Collections.<RelativeDirectory>emptySet();
        try {
            openFile();
            long totalLength = zipRandomFile.length();
            ZipDirectory directory = new ZipDirectory(zipRandomFile, 0L, totalLength, this);
            directory.buildIndex();
        } finally {
            if (zipRandomFile != null) {
                closeFile();
            }
        }
        lastReferenceTimeStamp = System.currentTimeMillis();
    }
    private void openFile() throws FileNotFoundException {
        if (zipRandomFile == null && zipFile != null) {
            zipRandomFile = new RandomAccessFile(zipFile, "r");
        }
    }
    private void cleanupState() {
        entries = Entry.EMPTY_ARRAY;
        directories = Collections.<RelativeDirectory, DirectoryEntry>emptyMap();
        zipFileLastModified = NOT_MODIFIED;
        allDirs = Collections.<RelativeDirectory>emptySet();
    }
    public synchronized void close() {
        writeIndex();
        closeFile();
    }
    private void closeFile() {
        if (zipRandomFile != null) {
            try {
                zipRandomFile.close();
            } catch (IOException ex) {
            }
            zipRandomFile = null;
        }
    }
    synchronized Entry getZipIndexEntry(RelativePath path) {
        try {
            checkIndex();
            DirectoryEntry de = directories.get(path.dirname());
            String lookFor = path.basename();
            return (de == null) ? null : de.getEntry(lookFor);
        }
        catch (IOException e) {
            return null;
        }
    }
    public synchronized com.sun.tools.javac.util.List<String> getFiles(RelativeDirectory path) {
        try {
            checkIndex();
            DirectoryEntry de = directories.get(path);
            com.sun.tools.javac.util.List<String> ret = de == null ? null : de.getFiles();
            if (ret == null) {
                return com.sun.tools.javac.util.List.<String>nil();
            }
            return ret;
        }
        catch (IOException e) {
            return com.sun.tools.javac.util.List.<String>nil();
        }
    }
    public synchronized List<String> getDirectories(RelativeDirectory path) {
        try {
            checkIndex();
            DirectoryEntry de = directories.get(path);
            com.sun.tools.javac.util.List<String> ret = de == null ? null : de.getDirectories();
            if (ret == null) {
                return com.sun.tools.javac.util.List.<String>nil();
            }
            return ret;
        }
        catch (IOException e) {
            return com.sun.tools.javac.util.List.<String>nil();
        }
    }
    public synchronized Set<RelativeDirectory> getAllDirectories() {
        try {
            checkIndex();
            if (allDirs == Collections.EMPTY_SET) {
                allDirs = new HashSet<RelativeDirectory>(directories.keySet());
            }
            return allDirs;
        }
        catch (IOException e) {
            return Collections.<RelativeDirectory>emptySet();
        }
    }
    public synchronized boolean contains(RelativePath path) {
        try {
            checkIndex();
            return getZipIndexEntry(path) != null;
        }
        catch (IOException e) {
            return false;
        }
    }
    public synchronized boolean isDirectory(RelativePath path) throws IOException {
        if (path.getPath().length() == 0) {
            lastReferenceTimeStamp = System.currentTimeMillis();
            return true;
        }
        checkIndex();
        return directories.get(path) != null;
    }
    public synchronized long getLastModified(RelativeFile path) throws IOException {
        Entry entry = getZipIndexEntry(path);
        if (entry == null)
            throw new FileNotFoundException();
        return entry.getLastModified();
    }
    public synchronized int length(RelativeFile path) throws IOException {
        Entry entry = getZipIndexEntry(path);
        if (entry == null)
            throw new FileNotFoundException();
        if (entry.isDir) {
            return 0;
        }
        byte[] header = getHeader(entry);
        if (get2ByteLittleEndian(header, 8) == 0) {
            return entry.compressedSize;
        } else {
            return entry.size;
        }
    }
    public synchronized byte[] read(RelativeFile path) throws IOException {
        Entry entry = getZipIndexEntry(path);
        if (entry == null)
            throw new FileNotFoundException("Path not found in ZIP: " + path.path);
        return read(entry);
    }
    synchronized byte[] read(Entry entry) throws IOException {
        openFile();
        byte[] result = readBytes(entry);
        closeFile();
        return result;
    }
    public synchronized int read(RelativeFile path, byte[] buffer) throws IOException {
        Entry entry = getZipIndexEntry(path);
        if (entry == null)
            throw new FileNotFoundException();
        return read(entry, buffer);
    }
    synchronized int read(Entry entry, byte[] buffer)
            throws IOException {
        int result = readBytes(entry, buffer);
        return  result;
    }
    private byte[] readBytes(Entry entry) throws IOException {
        byte[] header = getHeader(entry);
        int csize = entry.compressedSize;
        byte[] cbuf = new byte[csize];
        zipRandomFile.skipBytes(get2ByteLittleEndian(header, 26) + get2ByteLittleEndian(header, 28));
        zipRandomFile.readFully(cbuf, 0, csize);
        if (get2ByteLittleEndian(header, 8) == 0)
            return cbuf;
        int size = entry.size;
        byte[] buf = new byte[size];
        if (inflate(cbuf, buf) != size)
            throw new ZipException("corrupted zip file");
        return buf;
    }
    private int readBytes(Entry entry, byte[] buffer) throws IOException {
        byte[] header = getHeader(entry);
        if (get2ByteLittleEndian(header, 8) == 0) {
            zipRandomFile.skipBytes(get2ByteLittleEndian(header, 26) + get2ByteLittleEndian(header, 28));
            int offset = 0;
            int size = buffer.length;
            while (offset < size) {
                int count = zipRandomFile.read(buffer, offset, size - offset);
                if (count == -1)
                    break;
                offset += count;
            }
            return entry.size;
        }
        int csize = entry.compressedSize;
        byte[] cbuf = new byte[csize];
        zipRandomFile.skipBytes(get2ByteLittleEndian(header, 26) + get2ByteLittleEndian(header, 28));
        zipRandomFile.readFully(cbuf, 0, csize);
        int count = inflate(cbuf, buffer);
        if (count == -1)
            throw new ZipException("corrupted zip file");
        return entry.size;
    }
    private byte[] getHeader(Entry entry) throws IOException {
        zipRandomFile.seek(entry.offset);
        byte[] header = new byte[30];
        zipRandomFile.readFully(header);
        if (get4ByteLittleEndian(header, 0) != 0x04034b50)
            throw new ZipException("corrupted zip file");
        if ((get2ByteLittleEndian(header, 6) & 1) != 0)
            throw new ZipException("encrypted zip file"); 
        return header;
    }
    private SoftReference<Inflater> inflaterRef;
    private int inflate(byte[] src, byte[] dest) {
        Inflater inflater = (inflaterRef == null ? null : inflaterRef.get());
        if (inflater == null)
            inflaterRef = new SoftReference<Inflater>(inflater = new Inflater(true));
        inflater.reset();
        inflater.setInput(src);
        try {
            return inflater.inflate(dest);
        } catch (DataFormatException ex) {
            return -1;
        }
    }
    private static int get2ByteLittleEndian(byte[] buf, int pos) {
        return (buf[pos] & 0xFF) + ((buf[pos+1] & 0xFF) << 8);
    }
    private static int get4ByteLittleEndian(byte[] buf, int pos) {
        return (buf[pos] & 0xFF) + ((buf[pos + 1] & 0xFF) << 8) +
                ((buf[pos + 2] & 0xFF) << 16) + ((buf[pos + 3] & 0xFF) << 24);
    }
    private class ZipDirectory {
        private RelativeDirectory lastDir;
        private int lastStart;
        private int lastLen;
        byte[] zipDir;
        RandomAccessFile zipRandomFile = null;
        ZipFileIndex zipFileIndex = null;
        public ZipDirectory(RandomAccessFile zipRandomFile, long start, long end, ZipFileIndex index) throws IOException {
            this.zipRandomFile = zipRandomFile;
            this.zipFileIndex = index;
            hasValidHeader();
            findCENRecord(start, end);
        }
        private boolean hasValidHeader() throws IOException {
            final long pos = zipRandomFile.getFilePointer();
            try {
                if (zipRandomFile.read() == 'P') {
                    if (zipRandomFile.read() == 'K') {
                        if (zipRandomFile.read() == 0x03) {
                            if (zipRandomFile.read() == 0x04) {
                                return true;
                            }
                        }
                    }
                }
            } finally {
                zipRandomFile.seek(pos);
            }
            throw new ZipFormatException("invalid zip magic");
        }
        private void findCENRecord(long start, long end) throws IOException {
            long totalLength = end - start;
            int endbuflen = 1024;
            byte[] endbuf = new byte[endbuflen];
            long endbufend = end - start;
            while (endbufend >= 22) {
                if (endbufend < endbuflen)
                    endbuflen = (int)endbufend;
                long endbufpos = endbufend - endbuflen;
                zipRandomFile.seek(start + endbufpos);
                zipRandomFile.readFully(endbuf, 0, endbuflen);
                int i = endbuflen - 22;
                while (i >= 0 &&
                        !(endbuf[i] == 0x50 &&
                        endbuf[i + 1] == 0x4b &&
                        endbuf[i + 2] == 0x05 &&
                        endbuf[i + 3] == 0x06 &&
                        endbufpos + i + 22 +
                        get2ByteLittleEndian(endbuf, i + 20) == totalLength)) {
                    i--;
                }
                if (i >= 0) {
                    zipDir = new byte[get4ByteLittleEndian(endbuf, i + 12) + 2];
                    zipDir[0] = endbuf[i + 10];
                    zipDir[1] = endbuf[i + 11];
                    int sz = get4ByteLittleEndian(endbuf, i + 16);
                    if (sz < 0 || get2ByteLittleEndian(zipDir, 0) == 0xffff) {
                        throw new ZipFormatException("detected a zip64 archive");
                    }
                    zipRandomFile.seek(start + sz);
                    zipRandomFile.readFully(zipDir, 2, zipDir.length - 2);
                    return;
                } else {
                    endbufend = endbufpos + 21;
                }
            }
            throw new ZipException("cannot read zip file");
        }
        private void buildIndex() throws IOException {
            int entryCount = get2ByteLittleEndian(zipDir, 0);
            if (entryCount > 0) {
                directories = new HashMap<RelativeDirectory, DirectoryEntry>();
                ArrayList<Entry> entryList = new ArrayList<Entry>();
                int pos = 2;
                for (int i = 0; i < entryCount; i++) {
                    pos = readEntry(pos, entryList, directories);
                }
                for (RelativeDirectory d: directories.keySet()) {
                    RelativeDirectory parent = getRelativeDirectory(d.dirname().getPath());
                    String file = d.basename();
                    Entry zipFileIndexEntry = new Entry(parent, file);
                    zipFileIndexEntry.isDir = true;
                    entryList.add(zipFileIndexEntry);
                }
                entries = entryList.toArray(new Entry[entryList.size()]);
                Arrays.sort(entries);
            } else {
                cleanupState();
            }
        }
        private int readEntry(int pos, List<Entry> entryList,
                Map<RelativeDirectory, DirectoryEntry> directories) throws IOException {
            if (get4ByteLittleEndian(zipDir, pos) != 0x02014b50) {
                throw new ZipException("cannot read zip file entry");
            }
            int dirStart = pos + 46;
            int fileStart = dirStart;
            int fileEnd = fileStart + get2ByteLittleEndian(zipDir, pos + 28);
            if (zipFileIndex.symbolFilePrefixLength != 0 &&
                    ((fileEnd - fileStart) >= symbolFilePrefixLength)) {
                dirStart += zipFileIndex.symbolFilePrefixLength;
               fileStart += zipFileIndex.symbolFilePrefixLength;
            }
            for (int index = fileStart; index < fileEnd; index++) {
                byte nextByte = zipDir[index];
                if (nextByte == (byte)'\\') {
                    zipDir[index] = (byte)'/';
                    fileStart = index + 1;
                } else if (nextByte == (byte)'/') {
                    fileStart = index + 1;
                }
            }
            RelativeDirectory directory = null;
            if (fileStart == dirStart)
                directory = getRelativeDirectory("");
            else if (lastDir != null && lastLen == fileStart - dirStart - 1) {
                int index = lastLen - 1;
                while (zipDir[lastStart + index] == zipDir[dirStart + index]) {
                    if (index == 0) {
                        directory = lastDir;
                        break;
                    }
                    index--;
                }
            }
            if (directory == null) {
                lastStart = dirStart;
                lastLen = fileStart - dirStart - 1;
                directory = getRelativeDirectory(new String(zipDir, dirStart, lastLen, "UTF-8"));
                lastDir = directory;
                RelativeDirectory tempDirectory = directory;
                while (directories.get(tempDirectory) == null) {
                    directories.put(tempDirectory, new DirectoryEntry(tempDirectory, zipFileIndex));
                    if (tempDirectory.path.indexOf("/") == tempDirectory.path.length() - 1)
                        break;
                    else {
                        tempDirectory = getRelativeDirectory(tempDirectory.dirname().getPath());
                    }
                }
            }
            else {
                if (directories.get(directory) == null) {
                    directories.put(directory, new DirectoryEntry(directory, zipFileIndex));
                }
            }
            if (fileStart != fileEnd) {
                Entry entry = new Entry(directory,
                        new String(zipDir, fileStart, fileEnd - fileStart, "UTF-8"));
                entry.setNativeTime(get4ByteLittleEndian(zipDir, pos + 12));
                entry.compressedSize = get4ByteLittleEndian(zipDir, pos + 20);
                entry.size = get4ByteLittleEndian(zipDir, pos + 24);
                entry.offset = get4ByteLittleEndian(zipDir, pos + 42);
                entryList.add(entry);
            }
            return pos + 46 +
                    get2ByteLittleEndian(zipDir, pos + 28) +
                    get2ByteLittleEndian(zipDir, pos + 30) +
                    get2ByteLittleEndian(zipDir, pos + 32);
        }
    }
    public long getZipFileLastModified() throws IOException {
        synchronized (this) {
            checkIndex();
            return zipFileLastModified;
        }
    }
    static class DirectoryEntry {
        private boolean filesInited;
        private boolean directoriesInited;
        private boolean zipFileEntriesInited;
        private boolean entriesInited;
        private long writtenOffsetOffset = 0;
        private RelativeDirectory dirName;
        private com.sun.tools.javac.util.List<String> zipFileEntriesFiles = com.sun.tools.javac.util.List.<String>nil();
        private com.sun.tools.javac.util.List<String> zipFileEntriesDirectories = com.sun.tools.javac.util.List.<String>nil();
        private com.sun.tools.javac.util.List<Entry>  zipFileEntries = com.sun.tools.javac.util.List.<Entry>nil();
        private List<Entry> entries = new ArrayList<Entry>();
        private ZipFileIndex zipFileIndex;
        private int numEntries;
        DirectoryEntry(RelativeDirectory dirName, ZipFileIndex index) {
            filesInited = false;
            directoriesInited = false;
            entriesInited = false;
            this.dirName = dirName;
            this.zipFileIndex = index;
        }
        private com.sun.tools.javac.util.List<String> getFiles() {
            if (!filesInited) {
                initEntries();
                for (Entry e : entries) {
                    if (!e.isDir) {
                        zipFileEntriesFiles = zipFileEntriesFiles.append(e.name);
                    }
                }
                filesInited = true;
            }
            return zipFileEntriesFiles;
        }
        private com.sun.tools.javac.util.List<String> getDirectories() {
            if (!directoriesInited) {
                initEntries();
                for (Entry e : entries) {
                    if (e.isDir) {
                        zipFileEntriesDirectories = zipFileEntriesDirectories.append(e.name);
                    }
                }
                directoriesInited = true;
            }
            return zipFileEntriesDirectories;
        }
        private com.sun.tools.javac.util.List<Entry> getEntries() {
            if (!zipFileEntriesInited) {
                initEntries();
                zipFileEntries = com.sun.tools.javac.util.List.nil();
                for (Entry zfie : entries) {
                    zipFileEntries = zipFileEntries.append(zfie);
                }
                zipFileEntriesInited = true;
            }
            return zipFileEntries;
        }
        private Entry getEntry(String rootName) {
            initEntries();
            int index = Collections.binarySearch(entries, new Entry(dirName, rootName));
            if (index < 0) {
                return null;
            }
            return entries.get(index);
        }
        private void initEntries() {
            if (entriesInited) {
                return;
            }
            if (!zipFileIndex.readFromIndex) {
                int from = -Arrays.binarySearch(zipFileIndex.entries,
                        new Entry(dirName, ZipFileIndex.MIN_CHAR)) - 1;
                int to = -Arrays.binarySearch(zipFileIndex.entries,
                        new Entry(dirName, MAX_CHAR)) - 1;
                for (int i = from; i < to; i++) {
                    entries.add(zipFileIndex.entries[i]);
                }
            } else {
                File indexFile = zipFileIndex.getIndexFile();
                if (indexFile != null) {
                    RandomAccessFile raf = null;
                    try {
                        raf = new RandomAccessFile(indexFile, "r");
                        raf.seek(writtenOffsetOffset);
                        for (int nFiles = 0; nFiles < numEntries; nFiles++) {
                            int zfieNameBytesLen = raf.readInt();
                            byte [] zfieNameBytes = new byte[zfieNameBytesLen];
                            raf.read(zfieNameBytes);
                            String eName = new String(zfieNameBytes, "UTF-8");
                            boolean eIsDir = raf.readByte() == (byte)0 ? false : true;
                            int eOffset = raf.readInt();
                            int eSize = raf.readInt();
                            int eCsize = raf.readInt();
                            long eJavaTimestamp = raf.readLong();
                            Entry rfie = new Entry(dirName, eName);
                            rfie.isDir = eIsDir;
                            rfie.offset = eOffset;
                            rfie.size = eSize;
                            rfie.compressedSize = eCsize;
                            rfie.javatime = eJavaTimestamp;
                            entries.add(rfie);
                        }
                    } catch (Throwable t) {
                    } finally {
                        try {
                            if (raf != null) {
                                raf.close();
                            }
                        } catch (Throwable t) {
                        }
                    }
                }
            }
            entriesInited = true;
        }
        List<Entry> getEntriesAsCollection() {
            initEntries();
            return entries;
        }
    }
    private boolean readIndex() {
        if (triedToReadIndex || !usePreindexedCache) {
            return false;
        }
        boolean ret = false;
        synchronized (this) {
            triedToReadIndex = true;
            RandomAccessFile raf = null;
            try {
                File indexFileName = getIndexFile();
                raf = new RandomAccessFile(indexFileName, "r");
                long fileStamp = raf.readLong();
                if (zipFile.lastModified() != fileStamp) {
                    ret = false;
                } else {
                    directories = new HashMap<RelativeDirectory, DirectoryEntry>();
                    int numDirs = raf.readInt();
                    for (int nDirs = 0; nDirs < numDirs; nDirs++) {
                        int dirNameBytesLen = raf.readInt();
                        byte [] dirNameBytes = new byte[dirNameBytesLen];
                        raf.read(dirNameBytes);
                        RelativeDirectory dirNameStr = getRelativeDirectory(new String(dirNameBytes, "UTF-8"));
                        DirectoryEntry de = new DirectoryEntry(dirNameStr, this);
                        de.numEntries = raf.readInt();
                        de.writtenOffsetOffset = raf.readLong();
                        directories.put(dirNameStr, de);
                    }
                    ret = true;
                    zipFileLastModified = fileStamp;
                }
            } catch (Throwable t) {
            } finally {
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (Throwable tt) {
                    }
                }
            }
            if (ret == true) {
                readFromIndex = true;
            }
        }
        return ret;
    }
    private boolean writeIndex() {
        boolean ret = false;
        if (readFromIndex || !usePreindexedCache) {
            return true;
        }
        if (!writeIndex) {
            return true;
        }
        File indexFile = getIndexFile();
        if (indexFile == null) {
            return false;
        }
        RandomAccessFile raf = null;
        long writtenSoFar = 0;
        try {
            raf = new RandomAccessFile(indexFile, "rw");
            raf.writeLong(zipFileLastModified);
            writtenSoFar += 8;
            List<DirectoryEntry> directoriesToWrite = new ArrayList<DirectoryEntry>();
            Map<RelativeDirectory, Long> offsets = new HashMap<RelativeDirectory, Long>();
            raf.writeInt(directories.keySet().size());
            writtenSoFar += 4;
            for (RelativeDirectory dirName: directories.keySet()) {
                DirectoryEntry dirEntry = directories.get(dirName);
                directoriesToWrite.add(dirEntry);
                byte [] dirNameBytes = dirName.getPath().getBytes("UTF-8");
                int dirNameBytesLen = dirNameBytes.length;
                raf.writeInt(dirNameBytesLen);
                writtenSoFar += 4;
                raf.write(dirNameBytes);
                writtenSoFar += dirNameBytesLen;
                List<Entry> dirEntries = dirEntry.getEntriesAsCollection();
                raf.writeInt(dirEntries.size());
                writtenSoFar += 4;
                offsets.put(dirName, new Long(writtenSoFar));
                dirEntry.writtenOffsetOffset = 0L;
                raf.writeLong(0L);
                writtenSoFar += 8;
            }
            for (DirectoryEntry de : directoriesToWrite) {
                long currFP = raf.getFilePointer();
                long offsetOffset = offsets.get(de.dirName).longValue();
                raf.seek(offsetOffset);
                raf.writeLong(writtenSoFar);
                raf.seek(currFP);
                List<Entry> list = de.getEntriesAsCollection();
                for (Entry zfie : list) {
                    byte [] zfieNameBytes = zfie.name.getBytes("UTF-8");
                    int zfieNameBytesLen = zfieNameBytes.length;
                    raf.writeInt(zfieNameBytesLen);
                    writtenSoFar += 4;
                    raf.write(zfieNameBytes);
                    writtenSoFar += zfieNameBytesLen;
                    raf.writeByte(zfie.isDir ? (byte)1 : (byte)0);
                    writtenSoFar += 1;
                    raf.writeInt(zfie.offset);
                    writtenSoFar += 4;
                    raf.writeInt(zfie.size);
                    writtenSoFar += 4;
                    raf.writeInt(zfie.compressedSize);
                    writtenSoFar += 4;
                    raf.writeLong(zfie.getLastModified());
                    writtenSoFar += 8;
                }
            }
        } catch (Throwable t) {
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch(IOException ioe) {
            }
        }
        return ret;
    }
    public boolean writeZipIndex() {
        synchronized (this) {
            return writeIndex();
        }
    }
    private File getIndexFile() {
        if (zipIndexFile == null) {
            if (zipFile == null) {
                return null;
            }
            zipIndexFile = new File((preindexedCacheLocation == null ? "" : preindexedCacheLocation) +
                    zipFile.getName() + ".index");
        }
        return zipIndexFile;
    }
    public File getZipFile() {
        return zipFile;
    }
    File getAbsoluteFile() {
        File absFile = (absFileRef == null ? null : absFileRef.get());
        if (absFile == null) {
            absFile = zipFile.getAbsoluteFile();
            absFileRef = new SoftReference<File>(absFile);
        }
        return absFile;
    }
    private RelativeDirectory getRelativeDirectory(String path) {
        RelativeDirectory rd;
        SoftReference<RelativeDirectory> ref = relativeDirectoryCache.get(path);
        if (ref != null) {
            rd = ref.get();
            if (rd != null)
                return rd;
        }
        rd = new RelativeDirectory(path);
        relativeDirectoryCache.put(path, new SoftReference<RelativeDirectory>(rd));
        return rd;
    }
    static class Entry implements Comparable<Entry> {
        public static final Entry[] EMPTY_ARRAY = {};
        RelativeDirectory dir;
        boolean isDir;
        String name;
        int offset;
        int size;
        int compressedSize;
        long javatime;
        private int nativetime;
        public Entry(RelativePath path) {
            this(path.dirname(), path.basename());
        }
        public Entry(RelativeDirectory directory, String name) {
            this.dir = directory;
            this.name = name;
        }
        public String getName() {
            return new RelativeFile(dir, name).getPath();
        }
        public String getFileName() {
            return name;
        }
        public long getLastModified() {
            if (javatime == 0) {
                    javatime = dosToJavaTime(nativetime);
            }
            return javatime;
        }
        private static long dosToJavaTime(int dtime) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR,        ((dtime >> 25) & 0x7f) + 1980);
            c.set(Calendar.MONTH,       ((dtime >> 21) & 0x0f) - 1);
            c.set(Calendar.DATE,        ((dtime >> 16) & 0x1f));
            c.set(Calendar.HOUR_OF_DAY, ((dtime >> 11) & 0x1f));
            c.set(Calendar.MINUTE,      ((dtime >>  5) & 0x3f));
            c.set(Calendar.SECOND,      ((dtime <<  1) & 0x3e));
            c.set(Calendar.MILLISECOND, 0);
            return c.getTimeInMillis();
        }
        void setNativeTime(int natTime) {
            nativetime = natTime;
        }
        public boolean isDirectory() {
            return isDir;
        }
        public int compareTo(Entry other) {
            RelativeDirectory otherD = other.dir;
            if (dir != otherD) {
                int c = dir.compareTo(otherD);
                if (c != 0)
                    return c;
            }
            return name.compareTo(other.name);
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Entry))
                return false;
            Entry other = (Entry) o;
            return dir.equals(other.dir) && name.equals(other.name);
        }
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (this.dir != null ? this.dir.hashCode() : 0);
            hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }
        @Override
        public String toString() {
            return isDir ? ("Dir:" + dir + " : " + name) :
                (dir + ":" + name);
        }
    }
    static final class ZipFormatException extends IOException {
        private static final long serialVersionUID = 8000196834066748623L;
        protected ZipFormatException(String message) {
            super(message);
        }
        protected ZipFormatException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
