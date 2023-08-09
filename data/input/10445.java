public class NamedBitList {
    public static void main(String[] args) throws Exception {
        boolean[] bb = (new boolean[] {true, false, true, false, false, false});
        GeneralNames gns = new GeneralNames();
        gns.add(new GeneralName(new DNSName("dns")));
        DerOutputStream out;
        KeyUsageExtension x1 = new KeyUsageExtension(bb);
        check(new DerValue(x1.getExtensionValue()).getUnalignedBitString().length(), 3);
        NetscapeCertTypeExtension x2 = new NetscapeCertTypeExtension(bb);
        check(new DerValue(x2.getExtensionValue()).getUnalignedBitString().length(), 3);
        ReasonFlags r = new ReasonFlags(bb);
        out = new DerOutputStream();
        r.encode(out);
        check(new DerValue(out.toByteArray()).getUnalignedBitString().length(), 3);
        DistributionPoint dp = new DistributionPoint(gns, bb, gns);
        out = new DerOutputStream();
        dp.encode(out);
        DerValue v = new DerValue(out.toByteArray());
        v.data.getDerValue();
        DerValue v2 = v.data.getDerValue();
        v2.resetTag(DerValue.tag_BitString);
        check(v2.getUnalignedBitString().length(), 3);
        BitArray ba;
        ba = new BitArray(new boolean[] {false, false, false});
        check(ba.length(), 3);
        ba = ba.truncate();
        check(ba.length(), 1);
        ba = new BitArray(new boolean[] {
            true, true, true, true, true, true, true, true,
            false, false});
        check(ba.length(), 10);
        check(ba.toByteArray().length, 2);
        ba = ba.truncate();
        check(ba.length(), 8);
        check(ba.toByteArray().length, 1);
        ba = new BitArray(new boolean[] {
            true, true, true, true, true, true, true, true,
            true, false});
        check(ba.length(), 10);
        check(ba.toByteArray().length, 2);
        ba = ba.truncate();
        check(ba.length(), 9);
        check(ba.toByteArray().length, 2);
    }
    static void check(int la, int lb) throws Exception {
        if (la != lb) {
            System.err.println("Length is " + la + ", should be " + lb);
            throw new Exception("Encoding Error");
        } else {
            System.err.println("Correct, which is " + lb);
        }
    }
}
