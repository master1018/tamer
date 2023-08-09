public class ConstantPoolParser {
    final byte[] classFile;
    final byte[] tags;
    final char[] firstHeader;  
    int endOffset;
    char[] secondHeader;       
    private char[] charArray = new char[80];
    public ConstantPoolParser(byte[] classFile) throws InvalidConstantPoolFormatException {
        this.classFile = classFile;
        this.firstHeader = parseHeader(classFile);
        this.tags = new byte[firstHeader[4]];
    }
    public ConstantPoolParser(Class<?> templateClass) throws IOException, InvalidConstantPoolFormatException {
        this(AnonymousClassLoader.readClassFile(templateClass));
    }
    public ConstantPoolPatch createPatch() {
        return new ConstantPoolPatch(this);
    }
    public byte getTag(int index) {
        getEndOffset();  
        return tags[index];
    }
    public int getLength() {
        return firstHeader[4];
    }
    public int getStartOffset() {
        return firstHeader.length * 2;
    }
    public int getEndOffset() {
        if (endOffset == 0)
            throw new IllegalStateException("class file has not yet been parsed");
        return endOffset;
    }
    public int getThisClassIndex() {
        getEndOffset();   
        return secondHeader[1];
    }
    public int getTailLength() {
        return classFile.length - getEndOffset();
    }
    public void writeHead(OutputStream out) throws IOException {
        out.write(classFile, 0, getEndOffset());
    }
    void writePatchedHead(OutputStream out, Object[] patchArray) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public void writeTail(OutputStream out) throws IOException {
        out.write(classFile, getEndOffset(), getTailLength());
    }
    private static char[] parseHeader(byte[] classFile) throws InvalidConstantPoolFormatException {
        char[] result = new char[5];
        ByteBuffer buffer = ByteBuffer.wrap(classFile);
        for (int i = 0; i < result.length; i++)
            result[i] = (char) getUnsignedShort(buffer);
        int magic = result[0] << 16 | result[1] << 0;
        if (magic != 0xCAFEBABE)
            throw new InvalidConstantPoolFormatException("invalid magic number "+magic);
        int len = result[4];
        if (len < 1)
            throw new InvalidConstantPoolFormatException("constant pool length < 1");
        return result;
    }
    public void parse(ConstantPoolVisitor visitor) throws InvalidConstantPoolFormatException {
        ByteBuffer buffer = ByteBuffer.wrap(classFile);
        buffer.position(getStartOffset()); 
        Object[] values = new Object[getLength()];
        try {
            parseConstantPool(buffer, values, visitor);
        } catch(BufferUnderflowException e) {
            throw new InvalidConstantPoolFormatException(e);
        }
        if (endOffset == 0) {
            endOffset = buffer.position();
            secondHeader = new char[4];
            for (int i = 0; i < secondHeader.length; i++) {
                secondHeader[i] = (char) getUnsignedShort(buffer);
            }
        }
        resolveConstantPool(values, visitor);
    }
    private char[] getCharArray(int utfLength) {
        if (utfLength <= charArray.length)
            return charArray;
        return charArray = new char[utfLength];
    }
    private void parseConstantPool(ByteBuffer buffer, Object[] values, ConstantPoolVisitor visitor) throws InvalidConstantPoolFormatException {
        for (int i = 1; i < tags.length; ) {
            byte tag = (byte) getUnsignedByte(buffer);
            assert(tags[i] == 0 || tags[i] == tag);
            tags[i] = tag;
            switch (tag) {
                case CONSTANT_Utf8:
                    int utfLen = getUnsignedShort(buffer);
                    String value = getUTF8(buffer, utfLen, getCharArray(utfLen));
                    visitor.visitUTF8(i, CONSTANT_Utf8, value);
                    tags[i] = tag;
                    values[i++] = value;
                    break;
                case CONSTANT_Integer:
                    visitor.visitConstantValue(i, tag, buffer.getInt());
                    i++;
                    break;
                case CONSTANT_Float:
                    visitor.visitConstantValue(i, tag, buffer.getFloat());
                    i++;
                    break;
                case CONSTANT_Long:
                    visitor.visitConstantValue(i, tag, buffer.getLong());
                    i+=2;
                    break;
                case CONSTANT_Double:
                    visitor.visitConstantValue(i, tag, buffer.getDouble());
                    i+=2;
                    break;
                case CONSTANT_Class:    
                case CONSTANT_String:
                    tags[i] = tag;
                    values[i++] = new int[] { getUnsignedShort(buffer) };
                    break;
                case CONSTANT_Fieldref:           
                case CONSTANT_Methodref:          
                case CONSTANT_InterfaceMethodref: 
                case CONSTANT_NameAndType:
                    tags[i] = tag;
                    values[i++] = new int[] { getUnsignedShort(buffer), getUnsignedShort(buffer) };
                    break;
                default:
                    throw new AssertionError("invalid constant "+tag);
            }
        }
    }
    private void resolveConstantPool(Object[] values, ConstantPoolVisitor visitor) {
        for (int beg = 1, end = values.length-1, beg2, end2;
             beg <= end;
             beg = beg2, end = end2) {
             beg2 = end; end2 = beg-1;
             for (int i = beg; i <= end; i++) {
                  Object value = values[i];
                  if (!(value instanceof int[]))
                      continue;
                  int[] array = (int[]) value;
                  byte tag = tags[i];
                  switch (tag) {
                      case CONSTANT_String:
                          String stringBody = (String) values[array[0]];
                          visitor.visitConstantString(i, tag, stringBody, array[0]);
                          values[i] = null;
                          break;
                      case CONSTANT_Class: {
                          String className = (String) values[array[0]];
                          className = className.replace('/', '.');
                          visitor.visitConstantString(i, tag, className, array[0]);
                          values[i] = className;
                          break;
                      }
                      case CONSTANT_NameAndType: {
                          String memberName = (String) values[array[0]];
                          String signature  = (String) values[array[1]];
                          visitor.visitDescriptor(i, tag, memberName, signature,
                                                  array[0], array[1]);
                          values[i] = new String[] {memberName, signature};
                          break;
                      }
                      case CONSTANT_Fieldref:           
                      case CONSTANT_Methodref:          
                      case CONSTANT_InterfaceMethodref: {
                              Object className   = values[array[0]];
                              Object nameAndType = values[array[1]];
                              if (!(className instanceof String) ||
                                  !(nameAndType instanceof String[])) {
                                   if (beg2 > i)  beg2 = i;
                                   if (end2 < i)  end2 = i;
                                   continue;
                              }
                              String[] nameAndTypeArray = (String[]) nameAndType;
                              visitor.visitMemberRef(i, tag,
                                  (String)className,
                                  nameAndTypeArray[0],
                                  nameAndTypeArray[1],
                                  array[0], array[1]);
                              values[i] = null;
                          }
                          break;
                      default:
                          continue;
                }
            }
        }
    }
    private static int getUnsignedByte(ByteBuffer buffer) {
        return buffer.get() & 0xFF;
    }
    private static int getUnsignedShort(ByteBuffer buffer) {
        int b1 = getUnsignedByte(buffer);
        int b2 = getUnsignedByte(buffer);
        return (b1 << 8) + (b2 << 0);
    }
    private static String getUTF8(ByteBuffer buffer, int utfLen, char[] charArray) throws InvalidConstantPoolFormatException {
      int utfLimit = buffer.position() + utfLen;
      int index = 0;
      while (buffer.position() < utfLimit) {
          int c = buffer.get() & 0xff;
          if (c > 127) {
              buffer.position(buffer.position() - 1);
              return getUTF8Extended(buffer, utfLimit, charArray, index);
          }
          charArray[index++] = (char)c;
      }
      return new String(charArray, 0, index);
    }
    private static String getUTF8Extended(ByteBuffer buffer, int utfLimit, char[] charArray, int index) throws InvalidConstantPoolFormatException {
        int c, c2, c3;
        while (buffer.position() < utfLimit) {
            c = buffer.get() & 0xff;
            switch (c >> 4) {
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                    charArray[index++] = (char)c;
                    break;
                case 12: case 13:
                    c2 = buffer.get();
                    if ((c2 & 0xC0) != 0x80)
                        throw new InvalidConstantPoolFormatException(
                            "malformed input around byte " + buffer.position());
                     charArray[index++] = (char)(((c  & 0x1F) << 6) |
                                                  (c2 & 0x3F));
                    break;
                case 14:
                    c2 = buffer.get();
                    c3 = buffer.get();
                    if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80))
                       throw new InvalidConstantPoolFormatException(
                          "malformed input around byte " + (buffer.position()));
                    charArray[index++] = (char)(((c  & 0x0F) << 12) |
                                                ((c2 & 0x3F) << 6)  |
                                                ((c3 & 0x3F) << 0));
                    break;
                default:
                    throw new InvalidConstantPoolFormatException(
                        "malformed input around byte " + buffer.position());
            }
        }
        return new String(charArray, 0, index);
    }
}
