public class ManyInterfaces
    implements
        Interface000,
        Interface001,
        Interface002,
        Interface003,
        Interface004,
        Interface005,
        Interface006,
        Interface007,
        Interface008,
        Interface009,
        Interface010,
        Interface011,
        Interface012,
        Interface013,
        Interface014,
        Interface015,
        Interface016,
        Interface017,
        Interface018,
        Interface019,
        Interface020,
        Interface021,
        Interface022,
        Interface023,
        Interface024,
        Interface025,
        Interface026,
        Interface027,
        Interface028,
        Interface029,
        Interface030,
        Interface031,
        Interface032,
        Interface033,
        Interface034,
        Interface035,
        Interface036,
        Interface037,
        Interface038,
        Interface039,
        Interface040,
        Interface041,
        Interface042,
        Interface043,
        Interface044,
        Interface045,
        Interface046,
        Interface047,
        Interface048,
        Interface049,
        Interface050,
        Interface051,
        Interface052,
        Interface053,
        Interface054,
        Interface055,
        Interface056,
        Interface057,
        Interface058,
        Interface059,
        Interface060,
        Interface061,
        Interface062,
        Interface063,
        Interface064,
        Interface065,
        Interface066,
        Interface067,
        Interface068,
        Interface069,
        Interface070,
        Interface071,
        Interface072,
        Interface073,
        Interface074,
        Interface075,
        Interface076,
        Interface077,
        Interface078,
        Interface079,
        Interface080,
        Interface081,
        Interface082,
        Interface083,
        Interface084,
        Interface085,
        Interface086,
        Interface087,
        Interface088,
        Interface089,
        Interface090,
        Interface091,
        Interface092,
        Interface093,
        Interface094,
        Interface095,
        Interface096,
        Interface097,
        Interface098,
        Interface099
{
    private static boolean timing = false;
    private static void report(String label, long start, long end, int iter,
            int rept) {
        if (timing) {
            System.out.println(label + ": " + (end - start) / 1000 + "us"
                    + "  (" + (end - start) / (iter*rept) + "ns per call)");
        } else {
            System.out.println(label + ": done");
        }
    }
    public static void run(boolean timing) {
        ManyInterfaces.timing = timing;
        ManyInterfaces obj = new ManyInterfaces();
        Interface001 one;
        Interface049 forty;
        Interface099 ninety;
        long start, end;
        int iter = 32768;
        int rept = 16;
        int i;
        System.gc();
        start = System.nanoTime();
        testIface001(obj, iter);
        end = System.nanoTime();
        report("testIface001", start, end, iter, rept);
        start = System.nanoTime();
        testIface049(obj, iter);
        end = System.nanoTime();
        report("testIface049", start, end, iter, rept);
        start = System.nanoTime();
        testIface099(obj, iter);
        end = System.nanoTime();
        report("testIface099", start, end, iter, rept);
        start = System.nanoTime();
        testVirt001(obj, iter);
        end = System.nanoTime();
        report("testVirt001", start, end, iter, rept);
        start = System.nanoTime();
        testVirt049(obj, iter);
        end = System.nanoTime();
        report("testVirt049", start, end, iter, rept);
        start = System.nanoTime();
        testVirt099(obj, iter);
        end = System.nanoTime();
        report("testVirt099", start, end, iter, rept);
        start = System.nanoTime();
        testInstance001(obj, iter);
        end = System.nanoTime();
        report("testInst001", start, end, iter, rept);
        start = System.nanoTime();
        testInstance049(obj, iter);
        end = System.nanoTime();
        report("testInst049", start, end, iter, rept);
        start = System.nanoTime();
        testInstance099(obj, iter);
        end = System.nanoTime();
        report("testInst099", start, end, iter, rept);
    }
    public int func001() { return 1; }
    public int func003() { return 3; }
    public int func005() { return 5; }
    public int func007() { return 7; }
    public int func009() { return 9; }
    public int func011() { return 11; }
    public int func013() { return 13; }
    public int func015() { return 15; }
    public int func017() { return 17; }
    public int func019() { return 19; }
    public int func021() { return 21; }
    public int func023() { return 23; }
    public int func025() { return 25; }
    public int func027() { return 27; }
    public int func029() { return 29; }
    public int func031() { return 31; }
    public int func033() { return 33; }
    public int func035() { return 35; }
    public int func037() { return 37; }
    public int func039() { return 39; }
    public int func041() { return 41; }
    public int func043() { return 43; }
    public int func045() { return 45; }
    public int func047() { return 47; }
    public int func049() { return 49; }
    public int func051() { return 51; }
    public int func053() { return 53; }
    public int func055() { return 55; }
    public int func057() { return 57; }
    public int func059() { return 59; }
    public int func061() { return 61; }
    public int func063() { return 63; }
    public int func065() { return 65; }
    public int func067() { return 67; }
    public int func069() { return 69; }
    public int func071() { return 71; }
    public int func073() { return 73; }
    public int func075() { return 75; }
    public int func077() { return 77; }
    public int func079() { return 79; }
    public int func081() { return 81; }
    public int func083() { return 83; }
    public int func085() { return 85; }
    public int func087() { return 87; }
    public int func089() { return 89; }
    public int func091() { return 91; }
    public int func093() { return 93; }
    public int func095() { return 95; }
    public int func097() { return 97; }
    public int func099() { return 99; }
    static void testIface001(Interface001 iface, int count) {
        while (count-- != 0) {
            iface.func001(); iface.func001(); iface.func001(); iface.func001();
            iface.func001(); iface.func001(); iface.func001(); iface.func001();
            iface.func001(); iface.func001(); iface.func001(); iface.func001();
            iface.func001(); iface.func001(); iface.func001(); iface.func001();
        }
    }
    static void testIface049(Interface049 iface, int count) {
        while (count-- != 0) {
            iface.func049(); iface.func049(); iface.func049(); iface.func049();
            iface.func049(); iface.func049(); iface.func049(); iface.func049();
            iface.func049(); iface.func049(); iface.func049(); iface.func049();
            iface.func049(); iface.func049(); iface.func049(); iface.func049();
        }
    }
    static void testIface099(Interface099 iface, int count) {
        while (count-- != 0) {
            iface.func099(); iface.func099(); iface.func099(); iface.func099();
            iface.func099(); iface.func099(); iface.func099(); iface.func099();
            iface.func099(); iface.func099(); iface.func099(); iface.func099();
            iface.func099(); iface.func099(); iface.func099(); iface.func099();
        }
    }
    static void testVirt001(ManyInterfaces obj, int count) {
        while (count-- != 0) {
            obj.func001(); obj.func001(); obj.func001(); obj.func001();
            obj.func001(); obj.func001(); obj.func001(); obj.func001();
            obj.func001(); obj.func001(); obj.func001(); obj.func001();
            obj.func001(); obj.func001(); obj.func001(); obj.func001();
        }
    }
    static void testVirt049(ManyInterfaces obj, int count) {
        while (count-- != 0) {
            obj.func049(); obj.func049(); obj.func049(); obj.func049();
            obj.func049(); obj.func049(); obj.func049(); obj.func049();
            obj.func049(); obj.func049(); obj.func049(); obj.func049();
            obj.func049(); obj.func049(); obj.func049(); obj.func049();
        }
    }
    static void testVirt099(ManyInterfaces obj, int count) {
        while (count-- != 0) {
            obj.func099(); obj.func099(); obj.func099(); obj.func099();
            obj.func099(); obj.func099(); obj.func099(); obj.func099();
            obj.func099(); obj.func099(); obj.func099(); obj.func099();
            obj.func099(); obj.func099(); obj.func099(); obj.func099();
        }
    }
    static void testInstance001(Object obj, int count) {
        if (!(obj instanceof Interface001))
            System.err.println("BAD");
        while (count-- != 0) {
            boolean is;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
            is = obj instanceof Interface001;
        }
    }
    static void testInstance049(Object obj, int count) {
        if (!(obj instanceof Interface049))
            System.err.println("BAD");
        while (count-- != 0) {
            boolean is;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
            is = obj instanceof Interface049;
        }
    }
    static void testInstance099(Object obj, int count) {
        if (!(obj instanceof Interface099))
            System.err.println("BAD");
        while (count-- != 0) {
            boolean is;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
            is = obj instanceof Interface099;
        }
    }
}
