public class BasicFloat
    extends Basic
{
    private static final float[] VALUES = {
        Float.MIN_VALUE,
        (float) -1,
        (float) 0,
        (float) 1,
        Float.MAX_VALUE,
        Float.NEGATIVE_INFINITY,
        Float.POSITIVE_INFINITY,
        Float.NaN,
        (float) -0.0,
    };
    private static void relGet(FloatBuffer b) {
        int n = b.capacity();
        float v;
        for (int i = 0; i < n; i++)
            ck(b, (long)b.get(), (long)((float)ic(i)));
        b.rewind();
    }
    private static void relGet(FloatBuffer b, int start) {
        int n = b.remaining();
        float v;
        for (int i = start; i < n; i++)
            ck(b, (long)b.get(), (long)((float)ic(i)));
        b.rewind();
    }
    private static void absGet(FloatBuffer b) {
        int n = b.capacity();
        float v;
        for (int i = 0; i < n; i++)
            ck(b, (long)b.get(), (long)((float)ic(i)));
        b.rewind();
    }
    private static void bulkGet(FloatBuffer b) {
        int n = b.capacity();
        float[] a = new float[n + 7];
        b.get(a, 7, n);
        for (int i = 0; i < n; i++)
            ck(b, (long)a[i + 7], (long)((float)ic(i)));
    }
    private static void relPut(FloatBuffer b) {
        int n = b.capacity();
        b.clear();
        for (int i = 0; i < n; i++)
            b.put((float)ic(i));
        b.flip();
    }
    private static void absPut(FloatBuffer b) {
        int n = b.capacity();
        b.clear();
        for (int i = 0; i < n; i++)
            b.put(i, (float)ic(i));
        b.limit(n);
        b.position(0);
    }
    private static void bulkPutArray(FloatBuffer b) {
        int n = b.capacity();
        b.clear();
        float[] a = new float[n + 7];
        for (int i = 0; i < n; i++)
            a[i + 7] = (float)ic(i);
        b.put(a, 7, n);
        b.flip();
    }
    private static void bulkPutBuffer(FloatBuffer b) {
        int n = b.capacity();
        b.clear();
        FloatBuffer c = FloatBuffer.allocate(n + 7);
        c.position(7);
        for (int i = 0; i < n; i++)
            c.put((float)ic(i));
        c.flip();
        c.position(7);
        b.put(c);
        b.flip();
    }
    private static void callReset(FloatBuffer b) {
        b.position(0);
        b.mark();
        b.duplicate().reset();
        b.asReadOnlyBuffer().reset();
    }
    private static void putBuffer() {
        final int cap = 10;
        FloatBuffer direct1 = ByteBuffer.allocateDirect(cap).asFloatBuffer();
        FloatBuffer nondirect1 = ByteBuffer.allocate(cap).asFloatBuffer();
        direct1.put(nondirect1);
        FloatBuffer direct2 = ByteBuffer.allocateDirect(cap).asFloatBuffer();
        FloatBuffer nondirect2 = ByteBuffer.allocate(cap).asFloatBuffer();
        nondirect2.put(direct2);
        FloatBuffer direct3 = ByteBuffer.allocateDirect(cap).asFloatBuffer();
        FloatBuffer direct4 = ByteBuffer.allocateDirect(cap).asFloatBuffer();
        direct3.put(direct4);
        FloatBuffer nondirect3 = ByteBuffer.allocate(cap).asFloatBuffer();
        FloatBuffer nondirect4 = ByteBuffer.allocate(cap).asFloatBuffer();
        nondirect3.put(nondirect4);
    }
    private static void checkSlice(FloatBuffer b, FloatBuffer slice) {
        ck(slice, 0, slice.position());
        ck(slice, b.remaining(), slice.limit());
        ck(slice, b.remaining(), slice.capacity());
        if (b.isDirect() != slice.isDirect())
            fail("Lost direction", slice);
        if (b.isReadOnly() != slice.isReadOnly())
            fail("Lost read-only", slice);
    }
    private static void fail(String problem,
                             FloatBuffer xb, FloatBuffer yb,
                             float x, float y) {
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
    private static void tryCatch(float [] t, Class ex, Runnable thunk) {
        tryCatch(FloatBuffer.wrap(t), ex, thunk);
    }
    public static void test(int level, final FloatBuffer b, boolean direct) {
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
                    b.put((float)42);
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
                    b.put(b.limit(), (float)42);
                }});
        tryCatch(b, InvalidMarkException.class, new Runnable() {
                public void run() {
                    b.position(0);
                    b.mark();
                    b.compact();
                    b.reset();
                }});
        b.clear();
        b.put((float)0);
        b.put((float)-1);
        b.put((float)1);
        b.put(Float.MAX_VALUE);
        b.put(Float.MIN_VALUE);
        b.put(-Float.MAX_VALUE);
        b.put(-Float.MIN_VALUE);
        b.put(Float.NEGATIVE_INFINITY);
        b.put(Float.POSITIVE_INFINITY);
        b.put(Float.NaN);
        b.put(0.91697687f);             
        float v;
        b.flip();
        ck(b, b.get(), 0);
        ck(b, b.get(), (float)-1);
        ck(b, b.get(), 1);
        ck(b, b.get(), Float.MAX_VALUE);
        ck(b, b.get(), Float.MIN_VALUE);
        ck(b, b.get(), -Float.MAX_VALUE);
        ck(b, b.get(), -Float.MIN_VALUE);
        ck(b, b.get(), Float.NEGATIVE_INFINITY);
        ck(b, b.get(), Float.POSITIVE_INFINITY);
        if (Float.floatToRawIntBits(v = b.get()) != Float.floatToRawIntBits(Float.NaN))
            fail(b, (long)Float.NaN, (long)v);
        ck(b, b.get(), 0.91697687f);
        b.rewind();
        FloatBuffer b2 = FloatBuffer.allocate(b.capacity());
        b2.put(b);
        b2.flip();
        b.position(2);
        b2.position(2);
        if (!b.equals(b2)) {
            for (int i = 2; i < b.limit(); i++) {
                float x = b.get(i);
                float y = b2.get(i);
                if (x != y
                    || Float.compare(x, y) != 0
                    )
                    out.println("[" + i + "] " + x + " != " + y);
            }
            fail("Identical buffers not equal", b, b2);
        }
        if (b.compareTo(b2) != 0)
            fail("Comparison to identical buffer != 0", b, b2);
        b.limit(b.limit() + 1);
        b.position(b.limit() - 1);
        b.put((float)99);
        b.rewind();
        b2.rewind();
        if (b.equals(b2))
            fail("Non-identical buffers equal", b, b2);
        if (b.compareTo(b2) <= 0)
            fail("Comparison to shorter buffer <= 0", b, b2);
        b.limit(b.limit() - 1);
        b.put(2, (float)42);
        if (b.equals(b2))
            fail("Non-identical buffers equal", b, b2);
        if (b.compareTo(b2) <= 0)
            fail("Comparison to lesser buffer <= 0", b, b2);
        for (float x : VALUES) {
            FloatBuffer xb = FloatBuffer.wrap(new float[] { x });
            if (xb.compareTo(xb) != 0) {
                fail("compareTo not reflexive", xb, xb, x, x);
            }
            if (! xb.equals(xb)) {
                fail("equals not reflexive", xb, xb, x, x);
            }
            for (float y : VALUES) {
                FloatBuffer yb = FloatBuffer.wrap(new float[] { y });
                if (xb.compareTo(yb) != - yb.compareTo(xb)) {
                    fail("compareTo not anti-symmetric",
                         xb, yb, x, y);
                }
                if ((xb.compareTo(yb) == 0) != xb.equals(yb)) {
                    fail("compareTo inconsistent with equals",
                         xb, yb, x, y);
                }
                if (xb.compareTo(yb) != Float.compare(x, y)) {
                    if (x == 0.0 && y == 0.0) continue;
                    fail("Incorrect results for FloatBuffer.compareTo",
                         xb, yb, x, y);
                }
                if (xb.equals(yb) != ((x == y) || ((x != x) && (y != y)))) {
                    fail("Incorrect results for FloatBuffer.equals",
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
        FloatBuffer sb = b.slice();
        checkSlice(b, sb);
        b.position(0);
        FloatBuffer sb2 = sb.slice();
        checkSlice(sb, sb2);
        if (!sb.equals(sb2))
            fail("Sliced slices do not match", sb, sb2);
        if ((sb.hasArray()) && (sb.arrayOffset() != sb2.arrayOffset()))
            fail("Array offsets do not match: "
                 + sb.arrayOffset() + " != " + sb2.arrayOffset(), sb, sb2);
        b.rewind();
        final FloatBuffer rb = b.asReadOnlyBuffer();
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
    public static void test(final float [] ba) {
        int offset = 47;
        int length = 900;
        final FloatBuffer b = FloatBuffer.wrap(ba, offset, length);
        show(0, b);
        ck(b, b.capacity(), ba.length);
        ck(b, b.position(), offset);
        ck(b, b.limit(), offset + length);
        tryCatch(ba, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    FloatBuffer.wrap(ba, -1, ba.length);
                }});
        tryCatch(ba, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    FloatBuffer.wrap(ba, ba.length + 1, ba.length);
                }});
        tryCatch(ba, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    FloatBuffer.wrap(ba, 0, -1);
                }});
        tryCatch(ba, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    FloatBuffer.wrap(ba, 0, ba.length + 1);
                }});
        tryCatch(ba, NullPointerException.class, new Runnable() {
                public void run() {
                    FloatBuffer.wrap((float []) null, 0, 5);
                }});
        tryCatch(ba, NullPointerException.class, new Runnable() {
                public void run() {
                    FloatBuffer.wrap((float []) null);
                }});
    }
    private static void testAllocate() {
        tryCatch((Buffer) null, IllegalArgumentException.class, new Runnable() {
                public void run() {
                    FloatBuffer.allocate(-1);
                }});
    }
    public static void test() {
        testAllocate();
        test(0, FloatBuffer.allocate(7 * 1024), false);
        test(0, FloatBuffer.wrap(new float[7 * 1024], 0, 7 * 1024), false);
        test(new float[1024]);
        callReset(FloatBuffer.allocate(10));
        putBuffer();
    }
}
