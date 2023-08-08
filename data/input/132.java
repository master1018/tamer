public class AsfHeaderReader extends ChunkContainerReader<AsfHeader> {
    private static final GUID[] APPLYING = { GUID.GUID_HEADER };
    private static final AsfHeaderReader FULL_READER;
    private static final AsfHeaderReader INFO_READER;
    private static final AsfHeaderReader TAG_READER;
    static {
        final List<Class<? extends ChunkReader>> readers = new ArrayList<Class<? extends ChunkReader>>();
        readers.add(FileHeaderReader.class);
        readers.add(StreamChunkReader.class);
        INFO_READER = new AsfHeaderReader(readers, true);
        readers.clear();
        readers.add(ContentDescriptionReader.class);
        readers.add(ContentBrandingReader.class);
        readers.add(LanguageListReader.class);
        readers.add(MetadataReader.class);
        final AsfExtHeaderReader extReader = new AsfExtHeaderReader(readers, true);
        final AsfExtHeaderReader extReader2 = new AsfExtHeaderReader(readers, true);
        TAG_READER = new AsfHeaderReader(readers, true);
        TAG_READER.setExtendedHeaderReader(extReader);
        readers.add(FileHeaderReader.class);
        readers.add(StreamChunkReader.class);
        readers.add(EncodingChunkReader.class);
        readers.add(EncryptionChunkReader.class);
        readers.add(StreamBitratePropertiesReader.class);
        FULL_READER = new AsfHeaderReader(readers, false);
        FULL_READER.setExtendedHeaderReader(extReader2);
    }
    private static InputStream createStream(final RandomAccessFile raf) {
        return new FullRequestInputStream(new BufferedInputStream(new RandomAccessFileInputstream(raf)));
    }
    public static AsfHeader readHeader(final File file) throws IOException {
        final InputStream stream = new FileInputStream(file);
        final AsfHeader result = FULL_READER.read(Utils.readGUID(stream), stream, 0);
        stream.close();
        return result;
    }
    public static AsfHeader readHeader(final RandomAccessFile file) throws IOException {
        final InputStream stream = createStream(file);
        return FULL_READER.read(Utils.readGUID(stream), stream, 0);
    }
    public static AsfHeader readInfoHeader(final RandomAccessFile file) throws IOException {
        final InputStream stream = createStream(file);
        return INFO_READER.read(Utils.readGUID(stream), stream, 0);
    }
    public static AsfHeader readTagHeader(final RandomAccessFile file) throws IOException {
        final InputStream stream = createStream(file);
        return TAG_READER.read(Utils.readGUID(stream), stream, 0);
    }
    public AsfHeaderReader(final List<Class<? extends ChunkReader>> toRegister, final boolean readChunkOnce) {
        super(toRegister, readChunkOnce);
    }
    public boolean canFail() {
        return false;
    }
    @Override
    protected AsfHeader createContainer(final long streamPosition, final BigInteger chunkLength, final InputStream stream) throws IOException {
        final long chunkCount = Utils.readUINT32(stream);
        if (stream.read() != 1) {
            throw new IOException("No ASF");
        }
        if (stream.read() != 2) {
            throw new IOException("No ASF");
        }
        return new AsfHeader(streamPosition, chunkLength, chunkCount);
    }
    public GUID[] getApplyingIds() {
        return APPLYING.clone();
    }
    public void setExtendedHeaderReader(final AsfExtHeaderReader extReader) {
        for (final GUID curr : extReader.getApplyingIds()) {
            this.readerMap.put(curr, extReader);
        }
    }
}
