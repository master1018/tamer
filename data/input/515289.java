class ArgsTest{
    public ArgsTest() {
    }
    private long mLongArray[] = new long[] {
        0x1122334455667788L, 0x9887766554433221L };
    void argTest(int a, char c, double d, long j, float f) {
        System.out.println("VALUES: " + Long.toHexString(mLongArray[0]) + " "
            + Long.toHexString(mLongArray[1]) + " " + Long.toHexString(j));
        System.out.println("VALUES: " + mLongArray[0] + " "
            + mLongArray[1] + " " + j);
        System.out.println(j);
        System.out.println("j = " + j);
        System.out.println("a=" + a + " c=" + c + " d=" + d
            + " j=" + j + " f=" + f);
    }
}
