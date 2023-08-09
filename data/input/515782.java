public class InstField {
    public void run() {
        assignFields();
        checkFields();
        InstField.nullCheck(null);
    }
    static public void nullCheck(InstField nully) {
        System.out.println("InstField.nullCheck");
        try {
            int x = nully.mInt1;
            assert(false);
        } catch (NullPointerException npe) {
        }
        try {
            long l = nully.mLong1;
            assert(false);
        } catch (NullPointerException npe) {
        }
        try {
            nully.mInt1 = 5;
            assert(false);
        } catch (NullPointerException npe) {
        }
        try {
            nully.mLong1 = 17L;
            assert(false);
        } catch (NullPointerException npe) {
        }
    }
    public void assignFields() {
        System.out.println("InstField assign...");
        mBoolean1 = true;
        mBoolean2 = false;
        mByte1 = 127;
        mByte2 = -128;
        mChar1 = 32767;
        mChar2 = 65535;
        mShort1 = 32767;
        mShort2 = -32768;
        mInt1 = 65537;
        mInt2 = -65537;
        mFloat1 = 3.1415f;
        mFloat2 = -1.0f / 0.0f;                
        mLong1 = 1234605616436508552L;     
        mLong2 = -1234605616436508552L;
        mDouble1 = 3.1415926535;
        mDouble2 = 1.0 / 0.0;               
    }
    public void checkFields() {
        System.out.println("InstField check...");
        assert(mBoolean1);
        assert(!mBoolean2);
        assert(mByte1 == 127);
        assert(mByte2 == -128);
        assert(mChar1 == 32767);
        assert(mChar2 == 65535);
        assert(mShort1 == 32767);
        assert(mShort2 == -32768);
        assert(mInt1 == 65537);
        assert(mInt2 == -65537);
        assert(mFloat1 > 3.141f && mFloat1 < 3.142f);
        assert(mFloat2 < mFloat1);
        assert(mLong1 == 1234605616436508552L);
        assert(mLong2 == -1234605616436508552L);
        assert(mDouble1 > 3.141592653 && mDouble1 < 3.141592654);
        assert(mDouble2 > mDouble1);
    }
    public boolean mBoolean1, mBoolean2;
    public byte mByte1, mByte2;
    public char mChar1, mChar2;
    public short mShort1, mShort2;
    public int mInt1, mInt2;
    public float mFloat1, mFloat2;
    public long mLong1, mLong2;
    public double mDouble1, mDouble2;
}
