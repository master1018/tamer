public class BasicDouble
    extends Basic
{
    private static final double[] VALUES = {
        Double.MIN_VALUE,
        (double) -1,
        (double) 0,
        (double) 1,
        Double.MAX_VALUE,
        Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY,
        Double.NaN,
        (double) -0.0,
    };
    private static void relGet(DoubleBuffer b) {
        int n = b.capacity();
        double v;
        for (int i = 0; i < n; i++)
            ck(b, (long)b.get(), (long)((double)ic(i)));
        b.rewind();
    }
    private static void relGet(DoubleBuffer b, int start) {
        int n = b.remaining();
        double v;
        for (int i = start; i < n; i++)
            ck(b, (long)b.get(), (long)((double)ic(i)));
        b.rewind();
    }
    private static void absGet(DoubleBuffer b) {
        int n = b.capacity();
        double v;
        for (int i = 0; i < n; i++)
            ck(b, (long)b.get(), (long)((double)ic(i)));
        b.rewind();
    }
    private static void bulkGet(DoubleBuffer b) {
        int n = b.capacity();
        double[] a = new double[n + 7];
        b.get(a, 7, n);
        for (int i = 0; i < n; i++)
            ck(b, (long)a[i + 7], (long)((double)ic(i)));
    }
    private static void relPut(DoubleBuffer b) {
        int n = b.capacity();
        b.clear();
        for (int i = 0; i < n; i++)
            b.put((double)ic(i));
        b.flip();
    }
    private static void absPut(DoubleBuffer b) {
        int n = b.capacity();
        b.clear();
        for (int i = 0; i < n; i++)
            b.put(i, (double)ic(i));
        b.limit(n);
        b.position(0);
    }
    private static void bulkPutArray(DoubleBuffer b) {
        int n = b.capacity();
        b.clear();
        double[] a = new double[n + 7];
        for (int i = 0; i < n; i++)
            a[i + 7] = (double)ic(i);
        b.put(a, 7, n);
        b.flip();
    }
    private static void bulkPutBuffer(DoubleBuffer b) {
        int n = b.capacity();
        b.clear();
        DoubleBuffer c = DoubleBuffer.allocate(n + 7);
        c.position(7);
        for (int i = 0; i < n; i++)
            c.put((double)ic(i));
        c.flip();
        c.position(7);
        b.put(c);
        b.flip();
    }
    private static void callReset(DoubleBuffer b) {
        b.position(0);
        b.mark();
        b.duplicate().reset();
        b.asReadOnlyBuffer().reset();
    }
    private static void putBuffer() {
        final int cap = 10;
        DoubleBuffer direct1 = ByteBuffer.allocateDirect(cap).asDoubleBuffer();
        DoubleBuffer nondirect1 = ByteBuffer.allocate(cap).asDoubleBuffer();
        direct1.put(nondirect1);
        DoubleBuffer direct2 = ByteBuffer.allocateDirect(cap).asDoubleBuffer();
        DoubleBuffer nondirect2 = ByteBuffer.allocate(cap).asDoubleBuffer();
        nondirect2.put(direct2);
        DoubleBuffer direct3 = ByteBuffer.allocateDirect(cap).asDoubleBuffer();
        DoubleBuffer direct4 = ByteBuffer.allocateDirect(cap).asDoubleBuffer();
        direct3.put(direct4);
        DoubleBuffer nondirect3 = ByteBuffer.allocate(cap).asDoubleBuffer();
        DoubleBuffer nondirect4 = ByteBuffer.allocate(cap).asDoubleBuffer();
        nondirect3.put(nondirect4);
    }
    private static void checkSlice(DoubleBuffer b, DoubleBuffer slice) {
        ck(slice, 0, slice.position());
        ck(slice, b.remaining(), slice.limit());
        ck(slice, b.remaining(), slice.capacity());
        if (b.isDirect() != slice.isDirect())
            fail("Lost direction", slice);
        if (b.isReadOnly() != slice.isReadOnly())
            fail("Lost read-only", slice);
    }
    private static void fail(String problem,
                             DoubleBuffer xb, DoubleBuffer yb,
                             double x, double y) {
        fail(problem + String.format(": x=%s y=%s", x, y), xb, yb);
    }
    private static void tryCatch(Buffer b, Class ex, Runnable thunk) {
        boolean caught = false;
        try {
            thunk.run();
        } catch (Throwable x) {
            if (ex.isAssignableFrom(x.getClass())) {
                caught = true;
            } else {
                fail(x.getMessage() + " not expected");
            }
        }
        if (!caught)
            fail(ex.getName() + " not thrown", b);
    }
    private static void tryCatch(double [] t, Class ex, Runnable thunk) {
        tryCatch(DoubleBuffer.wrap(t), ex, thunk);
    }
    public static void test(int level, final DoubleBuffer b, boolean direct) {
        show(level, b);
        if (direct != b.isDirect())
            fail("Wrong direction", b);
        relPut(b);
        relGet(b);
        absGet(b);
        bulkGet(b);
        absPut(b);
        relGet(b);
        absGet(b);
        bulkGet(b);
        bulkPutArray(b);
        relGet(b);
        bulkPutBuffer(b);
        relGet(b);
        relPut(b);
        b.position(13);
        b.compact();
        b.flip();
        relGet(b, 13);
        relPut(b);
        b.limit(b.capacity() / 2);
        b.position(b.limit());
        tryCatch(b, BufferUnderflowException.class, new Runnable() {
                public void run() {
                    b.get();
                }});
        tryCatch(b, BufferOverflowException.class, new Runnable() {
                public void run() {
                    b.put((double)42);
                }});
        tryCatch(b, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    b.get(b.limit());
                }});
        tryCatch(b, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    b.get(-1);
                }});
        tryCatch(b, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    b.put(b.limit(), (double)42);
                }});
        tryCatch(b, InvalidMarkException.class, new Runnable() {
                public void run() {
                    b.position(0);
                    b.mark();
                    b.compact();
                    b.reset();
                }});
        b.clear();
        b.put((double)0);
        b.put((double)-1);
        b.put((double)1);
        b.put(Double.MAX_VALUE);
        b.put(Double.MIN_VALUE);
        b.put(-Double.MAX_VALUE);
        b.put(-Double.MIN_VALUE);
        b.put(Double.NEGATIVE_INFINITY);
        b.put(Double.POSITIVE_INFINITY);
        b.put(Double.NaN);
        b.put(0.5121609353879392);      
        double v;
        b.flip();
        ck(b, b.get(), 0);
        ck(b, b.get(), (double)-1);
        ck(b, b.get(), 1);
        ck(b, b.get(), Double.MAX_VALUE);
        ck(b, b.get(), Double.MIN_VALUE);
        ck(b, b.get(), -Double.MAX_VALUE);
        ck(b, b.get(), -Double.MIN_VALUE);
        ck(b, b.get(), Double.NEGATIVE_INFINITY);
        ck(b, b.get(), Double.POSITIVE_INFINITY);
        if (Double.doubleToRawLongBits(v = b.get())
            != Double.doubleToRawLongBits(Double.NaN))
            fail(b, (long)Double.NaN, (long)v);
        ck(b, b.get(), 0.5121609353879392);
        b.rewind();
        DoubleBuffer b2 = DoubleBuffer.allocate(b.capacity());
        b2.put(b);
        b2.flip();
        b.position(2);
        b2.position(2);
        if (!b.equals(b2)) {
            for (int i = 2; i < b.limit(); i++) {
                double x = b.get(i);
                double y = b2.get(i);
                if (x != y
                    || Double.compare(x, y) != 0
                    )
                    out.println("[" + i + "] " + x + " != " + y);
            }
            fail("Identical buffers not equal", b, b2);
        }
        if (b.compareTo(b2) != 0)
            fail("Comparison to identical buffer != 0", b, b2);
        b.limit(b.limit() + 1);
        b.position(b.limit() - 1);
        b.put((double)99);
        b.rewind();
        b2.rewind();
        if (b.equals(b2))
            fail("Non-identical buffers equal", b, b2);
        if (b.compareTo(b2) <= 0)
            fail("Comparison to shorter buffer <= 0", b, b2);
        b.limit(b.limit() - 1);
        b.put(2, (double)42);
        if (b.equals(b2))
            fail("Non-identical buffers equal", b, b2);
        if (b.compareTo(b2) <= 0)
            fail("Comparison to lesser buffer <= 0", b, b2);
        for (double x : VALUES) {
            DoubleBuffer xb = DoubleBuffer.wrap(new double[] { x });
            if (xb.compareTo(xb) != 0) {
                fail("compareTo not reflexive", xb, xb, x, x);
            }
            if (! xb.equals(xb)) {
                fail("equals not reflexive", xb, xb, x, x);
            }
            for (double y : VALUES) {
                DoubleBuffer yb = DoubleBuffer.wrap(new double[] { y });
                if (xb.compareTo(yb) != - yb.compareTo(xb)) {
                    fail("compareTo not anti-symmetric",
                         xb, yb, x, y);
                }
                if ((xb.compareTo(yb) == 0) != xb.equals(yb)) {
                    fail("compareTo inconsistent with equals",
                         xb, yb, x, y);
                }
                if (xb.compareTo(yb) != Double.compare(x, y)) {
                    if (x == 0.0 && y == 0.0) continue;
                    fail("Incorrect results for DoubleBuffer.compareTo",
                         xb, yb, x, y);
                }
                if (xb.equals(yb) != ((x == y) || ((x != x) && (y != y)))) {
                    fail("Incorrect results for DoubleBuffer.equals",
                         xb, yb, x, y);
                }
            }
        }
        relPut(b);
        relGet(b.duplicate());
        b.position(13);
        relGet(b.duplicate(), 13);
        relGet(b.duplicate().slice(), 13);
        relGet(b.slice(), 13);
        relGet(b.slice().duplicate(), 13);
        b.position(5);
        DoubleBuffer sb = b.slice();
        checkSlice(b, sb);
        b.position(0);
        DoubleBuffer sb2 = sb.slice();
        checkSlice(sb, sb2);
        if (!sb.equals(sb2))
            fail("Sliced slices do not match", sb, sb2);
        if ((sb.hasArray()) && (sb.arrayOffset() != sb2.arrayOffset()))
            fail("Array offsets do not match: "
                 + sb.arrayOffset() + " != " + sb2.arrayOffset(), sb, sb2);
        b.rewind();
        final DoubleBuffer rb = b.asReadOnlyBuffer();
        if (!b.equals(rb))
            fail("Buffer not equal to read-only view", b, rb);
        show(level + 1, rb);
        tryCatch(b, ReadOnlyBufferException.class, new Runnable() {
                public void run() {
                    relPut(rb);
                }});
        tryCatch(b, ReadOnlyBufferException.class, new Runnable() {
                public void run() {
                    absPut(rb);
                }});
        tryCatch(b, ReadOnlyBufferException.class, new Runnable() {
                public void run() {
                    bulkPutArray(rb);
                }});
        tryCatch(b, ReadOnlyBufferException.class, new Runnable() {
                public void run() {
                    bulkPutBuffer(rb);
                }});
        tryCatch(b, ReadOnlyBufferException.class, new Runnable() {
                public void run() {
                    rb.compact();
                }});
        if (rb.getClass().getName().startsWith("java.nio.Heap")) {
            tryCatch(b, ReadOnlyBufferException.class, new Runnable() {
                    public void run() {
                        rb.array();
                    }});
            tryCatch(b, ReadOnlyBufferException.class, new Runnable() {
                    public void run() {
                        rb.arrayOffset();
                    }});
            if (rb.hasArray())
                fail("Read-only heap buffer's backing array is accessible",
                     rb);
        }
        b.clear();
        rb.rewind();
        b.put(rb);
        relPut(b);                       
    }
    public static void test(final double [] ba) {
        int offset = 47;
        int length = 900;
        final DoubleBuffer b = DoubleBuffer.wrap(ba, offset, length);
        show(0, b);
        ck(b, b.capacity(), ba.length);
        ck(b, b.position(), offset);
        ck(b, b.limit(), offset + length);
        tryCatch(ba, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    DoubleBuffer.wrap(ba, -1, ba.length);
                }});
        tryCatch(ba, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    DoubleBuffer.wrap(ba, ba.length + 1, ba.length);
                }});
        tryCatch(ba, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    DoubleBuffer.wrap(ba, 0, -1);
                }});
        tryCatch(ba, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    DoubleBuffer.wrap(ba, 0, ba.length + 1);
                }});
        tryCatch(ba, NullPointerException.class, new Runnable() {
                public void run() {
                    DoubleBuffer.wrap((double []) null, 0, 5);
                }});
        tryCatch(ba, NullPointerException.class, new Runnable() {
                public void run() {
                    DoubleBuffer.wrap((double []) null);
                }});
    }
    private static void testAllocate() {
        tryCatch((Buffer) null, IllegalArgumentException.class, new Runnable() {
                public void run() {
                    DoubleBuffer.allocate(-1);
                }});
    }
    public static void test() {
        testAllocate();
        test(0, DoubleBuffer.allocate(7 * 1024), false);
        test(0, DoubleBuffer.wrap(new double[7 * 1024], 0, 7 * 1024), false);
        test(new double[1024]);
        callReset(DoubleBuffer.allocate(10));
        putBuffer();
    }
}
