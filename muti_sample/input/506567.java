public final class HeaderItem extends IndexedItem {
    private static final String MAGIC = "dex\n035\0";
    private static final int HEADER_SIZE = 0x70;
    private static final int ENDIAN_TAG = 0x12345678;
    public HeaderItem() {
    }
    @Override
    public ItemType itemType() {
        return ItemType.TYPE_HEADER_ITEM;
    }
    @Override
    public int writeSize() {
        return HEADER_SIZE;
    }
    @Override
    public void addContents(DexFile file) {
    }
    @Override
    public void writeTo(DexFile file, AnnotatedOutput out) {
        int mapOff = file.getMap().getFileOffset();
        Section firstDataSection = file.getFirstDataSection();
        Section lastDataSection = file.getLastDataSection();
        int dataOff = firstDataSection.getFileOffset();
        int dataSize = lastDataSection.getFileOffset() +
            lastDataSection.writeSize() - dataOff;
        if (out.annotates()) {
            out.annotate(8, "magic: " + new CstUtf8(MAGIC).toQuoted());
            out.annotate(4, "checksum");
            out.annotate(20, "signature");
            out.annotate(4, "file_size:       " +
                         Hex.u4(file.getFileSize()));
            out.annotate(4, "header_size:     " + Hex.u4(HEADER_SIZE));
            out.annotate(4, "endian_tag:      " + Hex.u4(ENDIAN_TAG));
            out.annotate(4, "link_size:       0");
            out.annotate(4, "link_off:        0");
            out.annotate(4, "map_off:         " + Hex.u4(mapOff));
        }
        for (int i = 0; i < 8; i++) {
            out.writeByte(MAGIC.charAt(i));
        }
        out.writeZeroes(24);
        out.writeInt(file.getFileSize());
        out.writeInt(HEADER_SIZE);
        out.writeInt(ENDIAN_TAG);
        out.writeZeroes(8);
        out.writeInt(mapOff);
        file.getStringIds().writeHeaderPart(out);
        file.getTypeIds().writeHeaderPart(out);
        file.getProtoIds().writeHeaderPart(out);
        file.getFieldIds().writeHeaderPart(out);
        file.getMethodIds().writeHeaderPart(out);
        file.getClassDefs().writeHeaderPart(out);
        if (out.annotates()) {
            out.annotate(4, "data_size:       " + Hex.u4(dataSize));
            out.annotate(4, "data_off:        " + Hex.u4(dataOff));
        }
        out.writeInt(dataSize);
        out.writeInt(dataOff);
    }
}
