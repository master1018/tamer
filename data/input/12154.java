abstract class ByteToCharGB18030DB extends ByteToCharDoubleByte {
    public String getCharacterEncoding() {
        return "ByteToCharGB18030DB";
    }
    public ByteToCharGB18030DB() {
        GB18030 nioCoder = new GB18030();
        super.index1 = nioCoder.getSubDecoderIndex1();
        super.index2 = nioCoder.getSubDecoderIndex2();
        start = 0x40;
        end = 0xFE;
    }
}
