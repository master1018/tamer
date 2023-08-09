public class CharToByteCp964
        extends CharToByteEUC
{
        private final static IBM964 nioCoder = new IBM964();
        public String getCharacterEncoding()
        {
                return "Cp964";
        }
        public int getMaxBytesPerChar() {
                return 4;
        }
        public CharToByteCp964()
        {
                super();
                super.mask1 = 0xFFC0;
                super.mask2 = 0x003F;
                super.shift = 6;
                super.index1 = nioCoder.getEncoderIndex1();
                super.index2 = nioCoder.getEncoderIndex2();
                super.index2a = nioCoder.getEncoderIndex2a();
                super.index2b = nioCoder.getEncoderIndex2b();
                super.index2c = nioCoder.getEncoderIndex2c();
        }
}
