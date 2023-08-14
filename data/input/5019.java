public class ELFFileParser {
    private static ELFFileParser elfParser;
    private static final String US_ASCII = "US-ASCII";
    public static ELFFileParser getParser() {
        if (elfParser == null) {
            elfParser = new ELFFileParser();
        }
        return elfParser;
    }
    public ELFFile parse(String filename) throws ELFException {
        try {
            RandomAccessFile file = new RandomAccessFile(filename, "r");
            return parse(new RandomAccessFileDataSource(file));
        } catch (FileNotFoundException e) {
            throw new ELFException(e);
        }
    }
    public ELFFile parse(DataSource source) throws ELFException {
        return new ELFFileImpl(source);
    }
    class ELFFileImpl implements ELFFile {
        private DataSource file;
        private ELFHeader header;
        private byte ident[] = new byte[16];
        ELFFileImpl(DataSource file) throws ELFException {
            this.file = file;
            int bytesRead = readBytes(ident);
            if (bytesRead != ident.length) {
                throw new ELFException("Error reading elf header (read " +
                            bytesRead + "bytes, expected to " +
                            "read " + ident.length + "bytes).");
            }
            if (!Arrays.equals(getMagicNumber(), ELF_MAGIC_NUMBER)) {
                    throw new ELFException("Bad magic number for file.");
            }
            header = new ELFHeaderImpl();
        }
        public ELFHeader getHeader()     { return header; }
        public byte[] getMagicNumber() {
            byte magicNumber[] = new byte[4];
            magicNumber[0] = ident[NDX_MAGIC_0];
            magicNumber[1] = ident[NDX_MAGIC_1];
            magicNumber[2] = ident[NDX_MAGIC_2];
            magicNumber[3] = ident[NDX_MAGIC_3];
            return magicNumber;
        }
        public byte getObjectSize()         { return ident[NDX_OBJECT_SIZE]; }
        public byte getEncoding()           { return ident[NDX_ENCODING]; }
        public byte getVersion()            { return ident[NDX_VERSION]; }
        class ELFHeaderImpl implements ELFHeader {
            private byte ident[] = new byte[16];        
            private short file_type;                    
            private short arch;                         
            private int version;                        
            private int entry_point;                    
            private int ph_offset;                      
            private int sh_offset;                      
            private int flags;                          
            private short eh_size;                      
            private short ph_entry_size;                
            private short num_ph;                       
            private short sh_entry_size;                
            private short num_sh;                       
            private short sh_string_ndx;                
            private MemoizedObject[] sectionHeaders;
            private MemoizedObject[] programHeaders;
            private ELFSectionHeader symbolTableSection;
            private ELFSectionHeader dynamicSymbolTableSection;
            private ELFHashTable hashTable;
            ELFHeaderImpl() throws ELFException {
                file_type = readShort();
                arch = readShort();
                version = readInt();
                entry_point = readInt();
                ph_offset = readInt();
                sh_offset = readInt();
                flags = readInt();
                eh_size = readShort();
                ph_entry_size = readShort();
                num_ph = readShort();
                sh_entry_size = readShort();
                num_sh = readShort();
                sh_string_ndx = readShort();
                sectionHeaders = new MemoizedObject[num_sh];
                for (int i = 0; i < num_sh; i++) {
                    final long sectionHeaderOffset =
                            (long)(sh_offset + (i * sh_entry_size));
                    sectionHeaders[i] = new MemoizedObject() {
                        public Object computeValue() {
                            return new ELFSectionHeaderImpl(sectionHeaderOffset);
                        }
                    };
                }
            }
            public short getFileType()                 { return file_type; }
            public short getArch()                     { return arch; }
            public short getSectionHeaderSize()        { return sh_entry_size; }
            public short getNumberOfSectionHeaders()   { return num_sh; }
            public ELFSectionHeader getSectionHeader(int index) {
                return (ELFSectionHeader)sectionHeaders[index].getValue();
            }
            public ELFStringTable getSectionHeaderStringTable() {
                return getSectionHeader(sh_string_ndx).getStringTable();
            }
            public ELFStringTable getStringTable() {
                return findStringTableWithName(ELFSectionHeader.STRING_TABLE_NAME);
            }
            public ELFStringTable getDynamicStringTable() {
                return findStringTableWithName(
                        ELFSectionHeader.DYNAMIC_STRING_TABLE_NAME);
            }
            private ELFStringTable findStringTableWithName(String tableName) {
                ELFSectionHeader sh = null;
                for (int i = 1; i < getNumberOfSectionHeaders(); i++) {
                    sh = getSectionHeader(i);
                    if (tableName.equals(sh.getName())) {
                        return sh.getStringTable();
                    }
                }
                return null;
            }
            public ELFHashTable getHashTable() {
                return null;
            }
            public ELFSectionHeader getSymbolTableSection() {
                if (symbolTableSection != null) {
                    return symbolTableSection;
                }
                symbolTableSection =
                        getSymbolTableSection(ELFSectionHeader.TYPE_SYMTBL);
                return symbolTableSection;
            }
            public ELFSectionHeader getDynamicSymbolTableSection() {
                if (dynamicSymbolTableSection != null) {
                    return dynamicSymbolTableSection;
                }
                dynamicSymbolTableSection =
                        getSymbolTableSection(ELFSectionHeader.TYPE_DYNSYM);
                return dynamicSymbolTableSection;
            }
            private ELFSectionHeader getSymbolTableSection(int type) {
                ELFSectionHeader sh = null;
                for (int i = 1; i < getNumberOfSectionHeaders(); i++) {
                    sh = getSectionHeader(i);
                    if (sh.getType() == type) {
                        dynamicSymbolTableSection = sh;
                        return sh;
                    }
                }
                return null;
            }
            public ELFSymbol getELFSymbol(String symbolName) {
                if (symbolName == null) {
                    return null;
                }
                ELFSymbol symbol = null;
                int numSymbols = 0;
                ELFSectionHeader sh = getDynamicSymbolTableSection();
                if (sh != null) {
                    numSymbols = sh.getNumberOfSymbols();
                    for (int i = 0; i < Math.ceil(numSymbols / 2); i++) {
                        if (symbolName.equals(
                                (symbol = sh.getELFSymbol(i)).getName())) {
                            return symbol;
                        } else if (symbolName.equals(
                                (symbol = sh.getELFSymbol(
                                        numSymbols - 1 - i)).getName())) {
                            return symbol;
                        }
                    }
                }
                sh = getSymbolTableSection();
                if (sh != null) {
                    numSymbols = sh.getNumberOfSymbols();
                    for (int i = 0; i < Math.ceil(numSymbols / 2); i++) {
                        if (symbolName.equals(
                                (symbol = sh.getELFSymbol(i)).getName())) {
                            return symbol;
                        } else if (symbolName.equals(
                                (symbol = sh.getELFSymbol(
                                        numSymbols - 1 - i)).getName())) {
                            return symbol;
                        }
                    }
                }
                return null;
            }
            public ELFSymbol getELFSymbol(long address) {
                ELFSymbol symbol = null;
                int numSymbols = 0;
                long value = 0L;
                ELFSectionHeader sh = getDynamicSymbolTableSection();
                if (sh != null) {
                    numSymbols = sh.getNumberOfSymbols();
                    for (int i = 0; i < numSymbols; i++) {
                        symbol = sh.getELFSymbol(i);
                        value = symbol.getValue();
                        if (address >= value && address < value + symbol.getSize()) {
                           return symbol;
                        }
                    }
                }
                sh = getSymbolTableSection();
                if (sh != null) {
                    numSymbols = sh.getNumberOfSymbols();
                    for (int i = 0; i < numSymbols; i++) {
                        symbol = sh.getELFSymbol(i);
                        value = symbol.getValue();
                        if (address >= value && address < value + symbol.getSize()) {
                           return symbol;
                        }
                    }
                }
                return null;
            }
        }
        class ELFSectionHeaderImpl implements ELFSectionHeader {
            private int name_ndx;                     
            private int type;                         
            private int flags;                        
            private int address;                      
            private int section_offset;               
            private int size;                         
            private int link;                         
            private int info;                         
            private int address_alignment;            
            private int entry_size;                   
            private MemoizedObject[] symbols;
            private MemoizedObject stringTable;
            private MemoizedObject hashTable;
            ELFSectionHeaderImpl(long offset) throws ELFException {
                seek(offset);
                name_ndx = readInt();
                type = readInt();
                flags = readInt();
                address = readInt();
                section_offset = readInt();
                size = readInt();
                link = readInt();
                info = readInt();
                address_alignment = readInt();
                entry_size = readInt();
                switch (type) {
                    case ELFSectionHeader.TYPE_NULL:
                        break;
                    case ELFSectionHeader.TYPE_PROGBITS:
                        break;
                    case ELFSectionHeader.TYPE_SYMTBL:
                    case ELFSectionHeader.TYPE_DYNSYM:
                        int num_entries = size / entry_size;
                        symbols = new MemoizedObject[num_entries];
                        for (int i = 0; i < num_entries; i++) {
                            final int symbolOffset = section_offset +
                                    (i * entry_size);
                            symbols[i] = new MemoizedObject() {
                                public Object computeValue() {
                                    return new ELFSymbolImpl(symbolOffset,type);
                                }
                            };
                        }
                        break;
                    case ELFSectionHeader.TYPE_STRTBL:
                        final int strTableOffset = section_offset;
                        final int strTableSize = size;
                        stringTable = new MemoizedObject() {
                            public Object computeValue() {
                                return new ELFStringTableImpl(strTableOffset,
                                                           strTableSize);
                            }
                        };
                        break;
                    case ELFSectionHeader.TYPE_RELO_EXPLICIT:
                        break;
                    case ELFSectionHeader.TYPE_HASH:
                        final int hashTableOffset = section_offset;
                        final int hashTableSize = size;
                        hashTable = new MemoizedObject() {
                            public Object computeValue() {
                                return new ELFHashTableImpl(hashTableOffset,
                                                         hashTableSize);
                            }
                        };
                        break;
                    case ELFSectionHeader.TYPE_DYNAMIC:
                        break;
                    case ELFSectionHeader.TYPE_NOTE:
                        break;
                    case ELFSectionHeader.TYPE_NOBITS:
                        break;
                    case ELFSectionHeader.TYPE_RELO:
                        break;
                    case ELFSectionHeader.TYPE_SHLIB:
                        break;
                    default:
                        break;
                }
            }
            public int getType() {
                return type;
            }
            public int getNumberOfSymbols() {
                if (symbols != null) {
                    return symbols.length;
                }
                return 0;
            }
            public ELFSymbol getELFSymbol(int index) {
                return (ELFSymbol)symbols[index].getValue();
            }
            public ELFStringTable getStringTable() {
                if (stringTable != null) {
                    return (ELFStringTable)stringTable.getValue();
                }
                return null;
            }
            public ELFHashTable getHashTable() {
                if (hashTable != null) {
                    return (ELFHashTable)hashTable.getValue();
                }
                return null;
            }
            public String getName() {
                if (name_ndx == 0) {
                    return null;
                }
                ELFStringTable tbl = getHeader().getSectionHeaderStringTable();
                return tbl.get(name_ndx);
            }
            public int getLink() {
                return link;
            }
            public int getOffset() {
                return section_offset;
            }
        }
        class ELFSymbolImpl implements ELFSymbol {
            private int name_ndx;                       
            private int value;                          
            private int size;                           
            private byte info;                          
            private byte other;                         
            private short section_header_ndx;             
            private int section_type;
            private long offset;
            ELFSymbolImpl(long offset, int section_type) throws ELFException {
                seek(offset);
                this.offset = offset;
                name_ndx = readInt();
                value = readInt();
                size = readInt();
                info = readByte();
                other = readByte();
                section_header_ndx = readShort();
                this.section_type = section_type;
                switch (getType()) {
                    case TYPE_NOOBJECT:
                        break;
                    case TYPE_OBJECT:
                        break;
                    case TYPE_FUNCTION:
                        break;
                    case TYPE_SECTION:
                        break;
                    case TYPE_FILE:
                        break;
                    case TYPE_LOPROC:
                        break;
                    case TYPE_HIPROC:
                        break;
                    default:
                        break;
                }
            }
            public int getBinding()             { return info >> 4; }
            public int getType()                { return info & 0x0F; }
            public long getOffset()             { return offset; }
            public String getName() {
                if (name_ndx == 0) {
                    return null;
                }
                String symbol_name = null;
                if (section_type == ELFSectionHeader.TYPE_SYMTBL) {
                    symbol_name = getHeader().getStringTable().get(name_ndx);
                } else if (section_type == ELFSectionHeader.TYPE_DYNSYM) {
                    symbol_name =
                            getHeader().getDynamicStringTable().get(name_ndx);
                }
                return symbol_name;
            }
            public long getValue() {
                return value;
            }
            public int getSize() {
                return size;
            }
        }
        class ELFStringTableImpl implements ELFStringTable {
            private byte data[];
            private int numStrings;
            ELFStringTableImpl(long offset, int length) throws ELFException {
                seek(offset);
                data = new byte[length];
                int bytesRead = readBytes(data);
                if (bytesRead != length) {
                    throw new ELFException("Error reading string table (read " +
                                           bytesRead + "bytes, expected to " +
                                           "read " + data.length + "bytes).");
                }
                numStrings = 0;
                for (int ptr = 0; ptr < data.length; ptr++) {
                    if (data[ptr] == '\0') {
                        numStrings++;
                    }
                }
            }
            public String get(int index) {
                int startPtr = index;
                int endPtr = index;
                while (data[endPtr] != '\0') {
                    endPtr++;
                }
                return new String(data, startPtr, endPtr - startPtr);
            }
            public int getNumStrings() {
                return numStrings;
            }
        }
        class ELFHashTableImpl implements ELFHashTable {
            private int num_buckets;
            private int num_chains;
            private int buckets[];
            private int chains[];
            ELFHashTableImpl(long offset, int length) throws ELFException {
                seek(offset);
                num_buckets = readInt();
                num_chains = readInt();
                buckets = new int[num_buckets];
                chains = new int[num_chains];
                for (int i = 0; i < num_buckets; i++) {
                    buckets[i] = readInt();
                }
                for (int i = 0; i < num_chains; i++) {
                    chains[i] = readInt();
                }
                int actual = num_buckets * 4 + num_chains * 4 + 8;
                if (length != actual) {
                    throw new ELFException("Error reading string table (read " +
                                           actual + "bytes, expected to " +
                                           "read " + length + "bytes).");
                }
            }
            public ELFSymbol getSymbol(String symbolName) {
                return null;
            }
        }
        public void close() throws ELFException {
            try {
                file.close();
            } catch (IOException e) {
                throw new ELFException(e);
            }
        }
        void seek(long offset) throws ELFException {
            try {
                file.seek(offset);
            } catch (IOException e) {
                throw new ELFException(e);
            }
        }
        long getFilePointer() throws ELFException {
            try {
                return file.getFilePointer();
            } catch (IOException e) {
                throw new ELFException(e);
            }
        }
        byte readByte() throws ELFException {
            try {
                return file.readByte();
            } catch (IOException e) {
                throw new ELFException(e);
            }
        }
        int readBytes(byte[] b) throws ELFException {
            try {
                return file.read(b);
            } catch (IOException e) {
                throw new ELFException(e);
            }
        }
        short readShort() throws ELFException {
            try {
                short val;
                switch (ident[NDX_ENCODING]) {
                    case DATA_LSB:
                        val = byteSwap(file.readShort());
                        break;
                    case DATA_MSB:
                        val = file.readShort();
                        break;
                    default:
                        throw new ELFException("Invalid encoding.");
                }
                return val;
            } catch (IOException e) {
                throw new ELFException(e);
            }
        }
        int readInt() throws ELFException {
            try {
                int val;
                switch (ident[NDX_ENCODING]) {
                    case DATA_LSB:
                        val = byteSwap(file.readInt());
                        break;
                    case DATA_MSB:
                        val = file.readInt();
                        break;
                    default:
                        throw new ELFException("Invalid encoding.");
                }
                return val;
            } catch (IOException e) {
                throw new ELFException(e);
            }
        }
        long readLong() throws ELFException {
            try {
                long val;
                switch (ident[NDX_ENCODING]) {
                    case DATA_LSB:
                        val = byteSwap(file.readLong());
                        break;
                    case DATA_MSB:
                        val = file.readLong();
                        break;
                    default:
                        throw new ELFException("Invalid encoding.");
                }
                return val;
            } catch (IOException e) {
                throw new ELFException(e);
            }
        }
        short byteSwap(short arg) {
          return (short) ((arg << 8) | ((arg >>> 8) & 0xFF));
        }
        int byteSwap(int arg) {
            return (((int) byteSwap((short) arg)) << 16) |
                   (((int) (byteSwap((short) (arg >>> 16)))) & 0xFFFF);
        }
        long byteSwap(long arg) {
            return ((((long) byteSwap((int) arg)) << 32) |
                   (((long) byteSwap((int) (arg >>> 32))) & 0xFFFFFFFF));
        }
        short readUnsignedByte() throws ELFException {
            try {
                return unsignedByte(file.readByte());
            } catch (IOException e) {
                throw new ELFException(e);
            }
        }
        int readUnsignedShort() throws ELFException {
            try {
                int val;
                switch (ident[NDX_ENCODING]) {
                    case DATA_LSB:
                        val = unsignedByteSwap(file.readShort());
                        break;
                    case DATA_MSB:
                        val = unsignedByte(file.readShort());
                        break;
                    default:
                        throw new ELFException("Invalid encoding.");
                }
                return val;
            } catch (IOException e) {
                throw new ELFException(e);
            }
        }
        long readUnsignedInt() throws ELFException {
            try {
                long val;
                switch (ident[NDX_ENCODING]) {
                    case DATA_LSB:
                        val = unsignedByteSwap(file.readInt());
                        break;
                    case DATA_MSB:
                        val = unsignedByte(file.readInt());
                        break;
                    default:
                        throw new ELFException("Invalid encoding.");
                }
                return val;
            } catch (IOException e) {
                throw new ELFException(e);
            }
        }
        short unsignedByte(byte arg) {
            return (short)(arg & 0x00FF);
        }
        int unsignedByte(short arg) {
            int val;
            if (arg >= 0) {
                val = arg;
            } else {
                val = (int)(((int)unsignedByte((byte)(arg >>> 8)) << 8) |
                            ((byte)arg));
            }
            return val;
        }
        long unsignedByte(int arg) {
            long val;
            if (arg >= 0) {
                val = arg;
            } else {
                val = (long)(((long)unsignedByte((short)(arg >>> 16)) << 16) |
                             ((short)arg));
            }
            return val;
        }
        int unsignedByteSwap(short arg) {
            return (int)(((int)unsignedByte((byte)arg)) << 8) |
                         ((int)unsignedByte((byte)(arg >>> 8)));
        }
        long unsignedByteSwap(int arg) {
            return (long)(((long)unsignedByteSwap((short)arg)) << 16) |
                          ((long)unsignedByteSwap((short)(arg >>> 16)));
        }
    }
    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("Usage: java ELFFileParser <elf file>");
            System.exit(0);
        }
        ELFFile elfFile = ELFFileParser.getParser().parse(args[0]);
        ELFHeader elfHeader = elfFile.getHeader();
        System.out.println("ELF File: " + args[0]);
        System.out.println("ELF object size: " +
                ((elfFile.getObjectSize() == 0) ? "Invalid Object Size" :
                (elfFile.getObjectSize() == 1) ? "32-bit" : "64-bit"));
        System.out.println("ELF data encoding: " +
                ((elfFile.getEncoding() == 0) ? "Invalid Data Encoding" :
                (elfFile.getEncoding() == 1) ? "LSB" : "MSB"));
        int h = elfHeader.getNumberOfSectionHeaders();
        System.out.println("--> Start: reading " + h + " section headers.");
        for (int i = 0; i < elfHeader.getNumberOfSectionHeaders(); i++) {
            ELFSectionHeader sh = elfHeader.getSectionHeader(i);
            String str = sh.getName();
            System.out.println("----> Start: Section (" + i + ") " + str);
            int num = 0;
            if ((num = sh.getNumberOfSymbols()) != 0) {
                System.out.println("------> Start: reading " + num + " symbols.");
                for (int j = 0; j < num ; j++) {
                    ELFSymbol sym = sh.getELFSymbol(j);
                }
                System.out.println("<------ End: reading " + num + " symbols.");
            }
            ELFStringTable st;
            if (sh.getType() == ELFSectionHeader.TYPE_STRTBL) {
                System.out.println("------> Start: reading string table.");
                st = sh.getStringTable();
                System.out.println("<------ End: reading string table.");
            }
            if (sh.getType() == ELFSectionHeader.TYPE_HASH) {
                System.out.println("------> Start: reading hash table.");
                sh.getHashTable();
                System.out.println("<------ End: reading hash table.");
            }
            System.out.println("<---- End: Section (" + i + ") " + str);
        }
        System.out.println("<-- End: reading " + h + " section headers.");
        elfFile.close();
    }
}
