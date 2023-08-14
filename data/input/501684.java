public class PngDecoderJava {
  public static BufferedImage decode(InputStream in) throws IOException {
    DataInputStream dataIn = new DataInputStream(in);
    readSignature(dataIn);
    PNGData chunks = readChunks(dataIn);
    long widthLong = chunks.getWidth();
    long heightLong = chunks.getHeight();
    if (widthLong > Integer.MAX_VALUE || heightLong > Integer.MAX_VALUE)
      throw new IOException("That image is too wide or tall.");
    int width = (int) widthLong;
    int height = (int) heightLong;
    ColorModel cm = chunks.getColorModel();
    WritableRaster raster = chunks.getRaster();
    BufferedImage image = new BufferedImage(cm, raster, false, null);
    return image;
  }
  protected static void readSignature(DataInputStream in) throws IOException {
    long signature = in.readLong();
    if (signature != 0x89504e470d0a1a0aL)
      throw new IOException("PNG signature not found!");
  }
  protected static PNGData readChunks(DataInputStream in) throws IOException {
    PNGData chunks = new PNGData();
    boolean trucking = true;
    while (trucking) {
      try {
        int length = in.readInt();
        if (length < 0)
          throw new IOException("Sorry, that file is too long.");
        byte[] typeBytes = new byte[4];
        in.readFully(typeBytes);
        byte[] data = new byte[length];
        in.readFully(data);
        long crc = in.readInt() & 0x00000000ffffffffL; 
        if (verifyCRC(typeBytes, data, crc) == false)
          throw new IOException("That file appears to be corrupted.");
        PNGChunk chunk = new PNGChunk(typeBytes, data);
        chunks.add(chunk);
      } catch (EOFException eofe) {
        trucking = false;
      }
    }
    return chunks;
  }
  protected static boolean verifyCRC(byte[] typeBytes, byte[] data, long crc) {
    CRC32 crc32 = new CRC32();
    crc32.update(typeBytes);
    crc32.update(data);
    long calculated = crc32.getValue();
    return (calculated == crc);
  }
}
class PNGData {
  private int mNumberOfChunks;
  private PNGChunk[] mChunks;
  public PNGData() {
    mNumberOfChunks = 0;
    mChunks = new PNGChunk[10];
  }
  public void add(PNGChunk chunk) {
    mChunks[mNumberOfChunks++] = chunk;
    if (mNumberOfChunks >= mChunks.length) {
      PNGChunk[] largerArray = new PNGChunk[mChunks.length + 10];
      System.arraycopy(mChunks, 0, largerArray, 0, mChunks.length);
      mChunks = largerArray;
    }
  }
  public long getWidth() {
    return getChunk("IHDR").getUnsignedInt(0);
  }
  public long getHeight() {    return getChunk("IHDR").getUnsignedInt(4);
  }
  public short getBitsPerPixel() {
    return getChunk("IHDR").getUnsignedByte(8);
  }
  public short getColorType() {
    return getChunk("IHDR").getUnsignedByte(9);
  }
  public short getCompression() {
    return getChunk("IHDR").getUnsignedByte(10);
  }
  public short getFilter() {
    return getChunk("IHDR").getUnsignedByte(11);
  }
  public short getInterlace() {
    return getChunk("IHDR").getUnsignedByte(12);
  }
  public ColorModel getColorModel() {
    short colorType = getColorType();
    int bitsPerPixel = getBitsPerPixel();
    if (colorType == 3) {
      byte[] paletteData = getChunk("PLTE").getData();
      int paletteLength = paletteData.length / 3;
      return new IndexColorModel(bitsPerPixel, paletteLength,
          paletteData, 0, false);
    }
    System.out.println("Unsupported color type: " + colorType);
    return null;
  }
  public WritableRaster getRaster() {
    int width = (int) getWidth();
    int height = (int) getHeight();
    int bitsPerPixel = getBitsPerPixel();
    short colorType = getColorType();
    if (colorType == 3) {
      byte[] imageData = getImageData();
      int len = Math.max(imageData.length, (width - 1) * (height -1));
      DataBuffer db = new DataBufferByte(imageData, len);
      WritableRaster raster = Raster.createPackedRaster(db, width,
          height, bitsPerPixel, null);
      return raster;
    } else
      System.out.println("Unsupported color type!");
    return null;
  }
  public byte[] getImageData() {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      for (int i = 0; i < mNumberOfChunks; i++) {
        PNGChunk chunk = mChunks[i];
        if (chunk.getTypeString().equals("IDAT")) {
          out.write(chunk.getData());
        }
      }
      out.flush();
      InflaterInputStream in = new InflaterInputStream(
          new ByteArrayInputStream(out.toByteArray()));
      ByteArrayOutputStream inflatedOut = new ByteArrayOutputStream();
      int readLength;
      byte[] block = new byte[8192];
      while ((readLength = in.read(block)) != -1)
        inflatedOut.write(block, 0, readLength);
      inflatedOut.flush();
      byte[] imageData = inflatedOut.toByteArray();
      int width = (int) getWidth();
      int height = (int) getHeight();
      int bitsPerPixel = getBitsPerPixel();
      int length = width * height * bitsPerPixel / 8;
      byte[] prunedData = new byte[length];
      if (getInterlace() == 0) {
        int index = 0;
        for (int i = 0; i < length; i++) {
          if ((i * 8 / bitsPerPixel) % width == 0) {
            index++; 
          }
          prunedData[i] = imageData[index++];
        }
      } else
        System.out.println("Couldn't undo interlacing.");
      return prunedData;
    } catch (IOException ioe) {
    }
    return null;
  }
  public PNGChunk getChunk(String type) {
    for (int i = 0; i < mNumberOfChunks; i++)
      if (mChunks[i].getTypeString().equals(type))
        return mChunks[i];
    return null;
  }
}
class PNGChunk {
  private byte[] mType;
  private byte[] mData;
  public PNGChunk(byte[] type, byte[] data) {
    mType = type;
    mData = data;
  }
  public String getTypeString() {
    try {
      return new String(mType, "UTF8");
    } catch (UnsupportedEncodingException uee) {
      return "";
    }
  }
  public byte[] getData() {
    return mData;
  }
  public long getUnsignedInt(int offset) {
    long value = 0;
    for (int i = 0; i < 4; i++)
      value += (mData[offset + i] & 0xff) << ((3 - i) * 8);
    return value;
  }
  public short getUnsignedByte(int offset) {
    return (short) (mData[offset] & 0x00ff);
  }
}
