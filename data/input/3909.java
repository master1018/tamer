public class CharToByteCp33722
        extends CharToByteEUC
{
        private final static IBM33722 nioCoder = new IBM33722();
        public String getCharacterEncoding()
        {
                return "Cp33722";
        }
        public int getMaxBytesPerChar() {
                return 3;
        }
        public CharToByteCp33722()
        {
                super();
                super.mask1 = 0xFFE0;
                super.mask2 = 0x001F;
                super.shift = 5;
                super.index1 = nioCoder.getEncoderIndex1();
                super.index2 = nioCoder.getEncoderIndex2();
                super.index2a = nioCoder.getEncoderIndex2a();
                super.index2b = nioCoder.getEncoderIndex2b();
        }
}
