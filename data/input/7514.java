public class BasicChar
    extends Basic
{
    private static final char[] VALUES = {
        Character.MIN_VALUE,
        (char) -1,
        (char) 0,
        (char) 1,
        Character.MAX_VALUE,
    };
    private static void relGet(CharBuffer b) {
        int n = b.capacity();
        char v;
        for (int i = 0; i < n; i++)
            ck(b, (long)b.get(), (long)((char)ic(i)));
        b.rewind();
    }
    private static void relGet(CharBuffer b, int start) {
        int n = b.remaining();
        char v;
        for (int i = start; i < n; i++)
            ck(b, (long)b.get(), (long)((char)ic(i)));
        b.rewind();
    }
    private static void absGet(CharBuffer b) {
        int n = b.capacity();
        char v;
        for (int i = 0; i < n; i++)
            ck(b, (long)b.get(), (long)((char)ic(i)));
        b.rewind();
    }
    private static void bulkGet(CharBuffer b) {
        int n = b.capacity();
        char[] a = new char[n + 7];
        b.get(a, 7, n);
        for (int i = 0; i < n; i++)
            ck(b, (long)a[i + 7], (long)((char)ic(i)));
    }
    private static void relPut(CharBuffer b) {
        int n = b.capacity();
        b.clear();
        for (int i = 0; i < n; i++)
            b.put((char)ic(i));
        b.flip();
    }
    private static void absPut(CharBuffer b) {
        int n = b.capacity();
        b.clear();
        for (int i = 0; i < n; i++)
            b.put(i, (char)ic(i));
        b.limit(n);
        b.position(0);
    }
    private static void bulkPutArray(CharBuffer b) {
        int n = b.capacity();
        b.clear();
        char[] a = new char[n + 7];
        for (int i = 0; i < n; i++)
            a[i + 7] = (char)ic(i);
        b.put(a, 7, n);
        b.flip();
    }
    private static void bulkPutBuffer(CharBuffer b) {
        int n = b.capacity();
        b.clear();
        CharBuffer c = CharBuffer.allocate(n + 7);
        c.position(7);
        for (int i = 0; i < n; i++)
            c.put((char)ic(i));
        c.flip();
        c.position(7);
        b.put(c);
        b.flip();
    }
    private static void callReset(CharBuffer b) {
        b.position(0);
        b.mark();
        b.duplicate().reset();
        b.asReadOnlyBuffer().reset();
    }
    private static void putBuffer() {
        final int cap = 10;
        CharBuffer direct1 = ByteBuffer.allocateDirect(cap).asCharBuffer();
        CharBuffer nondirect1 = ByteBuffer.allocate(cap).asCharBuffer();
        direct1.put(nondirect1);
        CharBuffer direct2 = ByteBuffer.allocateDirect(cap).asCharBuffer();
        CharBuffer nondirect2 = ByteBuffer.allocate(cap).asCharBuffer();
        nondirect2.put(direct2);
        CharBuffer direct3 = ByteBuffer.allocateDirect(cap).asCharBuffer();
        CharBuffer direct4 = ByteBuffer.allocateDirect(cap).asCharBuffer();
        direct3.put(direct4);
        CharBuffer nondirect3 = ByteBuffer.allocate(cap).asCharBuffer();
        CharBuffer nondirect4 = ByteBuffer.allocate(cap).asCharBuffer();
        nondirect3.put(nondirect4);
    }
    private static void bulkPutString(CharBuffer b) {
        int n = b.capacity();
        b.clear();
        StringBuffer sb = new StringBuffer(n + 7);
        sb.append("1234567");
        for (int i = 0; i < n; i++)
            sb.append((char)ic(i));
        b.put(sb.toString(), 7, 7 + n);
        b.flip();
    }
    private static void checkSlice(CharBuffer b, CharBuffer slice) {
        ck(slice, 0, slice.position());
        ck(slice, b.remaining(), slice.limit());
        ck(slice, b.remaining(), slice.capacity());
        if (b.isDirect() != slice.isDirect())
            fail("Lost direction", slice);
        if (b.isReadOnly() != slice.isReadOnly())
            fail("Lost read-only", slice);
    }
    private static void fail(String problem,
                             CharBuffer xb, CharBuffer yb,
                             char x, char y) {
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
    private static void tryCatch(char [] t, Class ex, Runnable thunk) {
        tryCatch(CharBuffer.wrap(t), ex, thunk);
    }
    public static void test(int level, final CharBuffer b, boolean direct) {
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
        bulkPutString(b);
        relGet(b);
        b.position(1);
        b.limit(7);
        ck(b, b.toString().equals("bcdefg"));
        b.position(2);
        ck(b, b.charAt(1), 'd');
        CharBuffer c = b.subSequence(1, 4);
        ck(c, c.capacity(), b.capacity());
        ck(c, c.position(), b.position()+1);
        ck(c, c.limit(), b.position()+4);
        ck(c, b.subSequence(1, 4).toString().equals("def"));
        b.position(4);
        ck(b, b.charAt(1), 'f');
        ck(b, b.subSequence(1, 3).toString().equals("fg"));
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
                    b.put((char)42);
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
                    b.put(b.limit(), (char)42);
                }});
        tryCatch(b, InvalidMarkException.class, new Runnable() {
                public void run() {
                    b.position(0);
                    b.mark();
                    b.compact();
                    b.reset();
                }});
        b.clear();
        b.put((char)0);
        b.put((char)-1);
        b.put((char)1);
        b.put(Character.MAX_VALUE);
        b.put(Character.MIN_VALUE);
        char v;
        b.flip();
        ck(b, b.get(), 0);
        ck(b, b.get(), (char)-1);
        ck(b, b.get(), 1);
        ck(b, b.get(), Character.MAX_VALUE);
        ck(b, b.get(), Character.MIN_VALUE);
        b.rewind();
        CharBuffer b2 = CharBuffer.allocate(b.capacity());
        b2.put(b);
        b2.flip();
        b.position(2);
        b2.position(2);
        if (!b.equals(b2)) {
            for (int i = 2; i < b.limit(); i++) {
                char x = b.get(i);
                char y = b2.get(i);
                if (x != y
                    )
                    out.println("[" + i + "] " + x + " != " + y);
            }
            fail("Identical buffers not equal", b, b2);
        }
        if (b.compareTo(b2) != 0)
            fail("Comparison to identical buffer != 0", b, b2);
        b.limit(b.limit() + 1);
        b.position(b.limit() - 1);
        b.put((char)99);
        b.rewind();
        b2.rewind();
        if (b.equals(b2))
            fail("Non-identical buffers equal", b, b2);
        if (b.compareTo(b2) <= 0)
            fail("Comparison to shorter buffer <= 0", b, b2);
        b.limit(b.limit() - 1);
        b.put(2, (char)42);
        if (b.equals(b2))
            fail("Non-identical buffers equal", b, b2);
        if (b.compareTo(b2) <= 0)
            fail("Comparison to lesser buffer <= 0", b, b2);
        for (char x : VALUES) {
            CharBuffer xb = CharBuffer.wrap(new char[] { x });
            if (xb.compareTo(xb) != 0) {
                fail("compareTo not reflexive", xb, xb, x, x);
            }
            if (! xb.equals(xb)) {
                fail("equals not reflexive", xb, xb, x, x);
            }
            for (char y : VALUES) {
                CharBuffer yb = CharBuffer.wrap(new char[] { y });
                if (xb.compareTo(yb) != - yb.compareTo(xb)) {
                    fail("compareTo not anti-symmetric",
                         xb, yb, x, y);
                }
                if ((xb.compareTo(yb) == 0) != xb.equals(yb)) {
                    fail("compareTo inconsistent with equals",
                         xb, yb, x, y);
                }
                if (xb.compareTo(yb) != Character.compare(x, y)) {
                    fail("Incorrect results for CharBuffer.compareTo",
                         xb, yb, x, y);
                }
                if (xb.equals(yb) != ((x == y) || ((x != x) && (y != y)))) {
                    fail("Incorrect results for CharBuffer.equals",
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
        CharBuffer sb = b.slice();
        checkSlice(b, sb);
        b.position(0);
        CharBuffer sb2 = sb.slice();
        checkSlice(sb, sb2);
        if (!sb.equals(sb2))
            fail("Sliced slices do not match", sb, sb2);
        if ((sb.hasArray()) && (sb.arrayOffset() != sb2.arrayOffset()))
            fail("Array offsets do not match: "
                 + sb.arrayOffset() + " != " + sb2.arrayOffset(), sb, sb2);
        b.rewind();
        final CharBuffer rb = b.asReadOnlyBuffer();
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
    private static void testStr() {
        final String s = "abcdefghijklm";
        int start = 3;
        int end = 9;
        final CharBuffer b = CharBuffer.wrap(s, start, end);
        show(0, b);
        ck(b, b.toString().equals(s.substring(start, end)));
        ck(b, b.toString().equals("defghi"));
        ck(b, b.isReadOnly());
        tryCatch(b, ReadOnlyBufferException.class, new Runnable() {
                public void run() {
                    b.put('x');
                }});
        ck(b, start, b.position());
        ck(b, end, b.limit());
        ck(b, s.length(), b.capacity());
        b.position(6);
        ck(b, b.subSequence(0,3).toString().equals("ghi"));
        tryCatch(b, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    b.charAt(-1);
                }});
        tryCatch(b, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    b.charAt(b.remaining());
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
                    b.subSequence(-1, b.remaining());
                }});
        tryCatch(b, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    b.subSequence(b.remaining() + 1, b.remaining());
                }});
        tryCatch(b, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    b.subSequence(2, 1);
                }});
        tryCatch(b, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    b.subSequence(0, b.remaining() + 1);
                }});
        tryCatch(b, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    CharBuffer.wrap(s, -1, s.length());
                }});
        tryCatch(b, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    CharBuffer.wrap(s, s.length() + 1, s.length());
                }});
        tryCatch(b, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    CharBuffer.wrap(s, 1, 0);
                }});
        tryCatch(b, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    CharBuffer.wrap(s, 0, s.length() + 1);
                }});
    }
    public static void test(final char [] ba) {
        int offset = 47;
        int length = 900;
        final CharBuffer b = CharBuffer.wrap(ba, offset, length);
        show(0, b);
        ck(b, b.capacity(), ba.length);
        ck(b, b.position(), offset);
        ck(b, b.limit(), offset + length);
        tryCatch(ba, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    CharBuffer.wrap(ba, -1, ba.length);
                }});
        tryCatch(ba, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    CharBuffer.wrap(ba, ba.length + 1, ba.length);
                }});
        tryCatch(ba, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    CharBuffer.wrap(ba, 0, -1);
                }});
        tryCatch(ba, IndexOutOfBoundsException.class, new Runnable() {
                public void run() {
                    CharBuffer.wrap(ba, 0, ba.length + 1);
                }});
        tryCatch(ba, NullPointerException.class, new Runnable() {
                public void run() {
                    CharBuffer.wrap((char []) null, 0, 5);
                }});
        tryCatch(ba, NullPointerException.class, new Runnable() {
                public void run() {
                    CharBuffer.wrap((char []) null);
                }});
    }
    private static void testAllocate() {
        tryCatch((Buffer) null, IllegalArgumentException.class, new Runnable() {
                public void run() {
                    CharBuffer.allocate(-1);
                }});
    }
    public static void test() {
        testAllocate();
        test(0, CharBuffer.allocate(7 * 1024), false);
        test(0, CharBuffer.wrap(new char[7 * 1024], 0, 7 * 1024), false);
        test(new char[1024]);
        testStr();
        callReset(CharBuffer.allocate(10));
        putBuffer();
    }
}
