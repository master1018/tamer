abstract class InputStreamTests extends InputTests {
    private static Group streamRoot;
    private static Group streamTestRoot;
    public static void init() {
        streamRoot = new Group(inputRoot, "stream",
                               "Image Stream Benchmarks");
        streamTestRoot = new Group(streamRoot, "tests",
                                   "ImageInputStream Tests");
        new IISConstruct();
        new IISRead();
        new IISReadByteArray();
        new IISReadFullyByteArray();
        new IISReadBit();
        new IISReadByte();
        new IISReadUnsignedByte();
        new IISReadShort();
        new IISReadUnsignedShort();
        new IISReadInt();
        new IISReadUnsignedInt();
        new IISReadFloat();
        new IISReadLong();
        new IISReadDouble();
        new IISSkipBytes();
    }
    protected InputStreamTests(Group parent,
                               String nodeName, String description)
    {
        super(parent, nodeName, description);
        addDependency(generalSourceRoot);
        addDependencies(imageioGeneralOptRoot, true);
    }
    public void cleanupTest(TestEnvironment env, Object ctx) {
        Context iioctx = (Context)ctx;
        iioctx.cleanup(env);
    }
    private static class Context extends InputTests.Context {
        ImageInputStream inputStream;
        int scanlineStride; 
        int length; 
        byte[] byteBuf;
        Context(TestEnvironment env, Result result) {
            super(env, result);
            scanlineStride = size * 4;
            length = (scanlineStride * size) + 4;
            byteBuf = new byte[scanlineStride];
            initInput();
            try {
                inputStream = createImageInputStream();
            } catch (IOException e) {
                System.err.println("Error creating ImageInputStream");
            }
        }
        void initContents(File f) throws IOException {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                initContents(fos);
            } finally {
                fos.close();
            }
        }
        void initContents(OutputStream out) throws IOException {
            for (int i = 0; i < size; i++) {
                out.write(byteBuf);
            }
            out.write(new byte[4]); 
            out.flush();
        }
        void cleanup(TestEnvironment env) {
            super.cleanup(env);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.err.println("error closing stream");
                }
                inputStream = null;
            }
        }
    }
    private static class IISConstruct extends InputStreamTests {
        public IISConstruct() {
            super(streamTestRoot,
                  "construct",
                  "Construct");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(1);
            result.setUnitName("stream");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            try {
                do {
                    ImageInputStream iis = ictx.createImageInputStream();
                    iis.close();
                    ictx.closeOriginalStream();
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static class IISRead extends InputStreamTests {
        public IISRead() {
            super(streamTestRoot,
                  "read",
                  "read()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(1);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageInputStream iis = ictx.inputStream;
            final int length = ictx.length;
            int pos = 0;
            try {
                iis.mark();
                do {
                    if (pos >= length) {
                        iis.reset();
                        iis.mark();
                        pos = 0;
                    }
                    iis.read();
                    pos++;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { iis.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IISReadByteArray extends InputStreamTests {
        public IISReadByteArray() {
            super(streamTestRoot,
                  "readByteArray",
                  "read(byte[]) (one \"scanline\" at a time)");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(ctx.scanlineStride);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageInputStream iis = ictx.inputStream;
            final byte[] buf = ictx.byteBuf;
            final int scanlineStride = ictx.scanlineStride;
            final int length = ictx.length;
            int pos = 0;
            try {
                iis.mark();
                do {
                    if (pos + scanlineStride > length) {
                        iis.reset();
                        iis.mark();
                        pos = 0;
                    }
                    iis.read(buf);
                    pos += scanlineStride;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { iis.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IISReadFullyByteArray extends InputStreamTests {
        public IISReadFullyByteArray() {
            super(streamTestRoot,
                  "readFullyByteArray",
                  "readFully(byte[]) (one \"scanline\" at a time)");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(ctx.scanlineStride);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageInputStream iis = ictx.inputStream;
            final byte[] buf = ictx.byteBuf;
            final int scanlineStride = ictx.scanlineStride;
            final int length = ictx.length;
            int pos = 0;
            try {
                iis.mark();
                do {
                    if (pos + scanlineStride > length) {
                        iis.reset();
                        iis.mark();
                        pos = 0;
                    }
                    iis.readFully(buf);
                    pos += scanlineStride;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { iis.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IISReadBit extends InputStreamTests {
        public IISReadBit() {
            super(streamTestRoot,
                  "readBit",
                  "readBit()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(1);
            result.setUnitName("bit");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageInputStream iis = ictx.inputStream;
            final int length = ictx.length * 8;
            int pos = 0;
            try {
                iis.mark();
                do {
                    if (pos >= length) {
                        iis.reset();
                        iis.mark();
                        pos = 0;
                    }
                    iis.readBit();
                    pos++;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { iis.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IISReadByte extends InputStreamTests {
        public IISReadByte() {
            super(streamTestRoot,
                  "readByte",
                  "readByte()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(1);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageInputStream iis = ictx.inputStream;
            final int length = ictx.length;
            int pos = 0;
            try {
                iis.mark();
                do {
                    if (pos >= length) {
                        iis.reset();
                        iis.mark();
                        pos = 0;
                    }
                    iis.readByte();
                    pos++;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { iis.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IISReadUnsignedByte extends InputStreamTests {
        public IISReadUnsignedByte() {
            super(streamTestRoot,
                  "readUnsignedByte",
                  "readUnsignedByte()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(1);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageInputStream iis = ictx.inputStream;
            final int length = ictx.length;
            int pos = 0;
            try {
                iis.mark();
                do {
                    if (pos >= length) {
                        iis.reset();
                        iis.mark();
                        pos = 0;
                    }
                    iis.readUnsignedByte();
                    pos++;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { iis.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IISReadShort extends InputStreamTests {
        public IISReadShort() {
            super(streamTestRoot,
                  "readShort",
                  "readShort()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(2);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageInputStream iis = ictx.inputStream;
            final int length = ictx.length;
            int pos = 0;
            try {
                iis.mark();
                do {
                    if (pos + 2 > length) {
                        iis.reset();
                        iis.mark();
                        pos = 0;
                    }
                    iis.readShort();
                    pos += 2;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { iis.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IISReadUnsignedShort extends InputStreamTests {
        public IISReadUnsignedShort() {
            super(streamTestRoot,
                  "readUnsignedShort",
                  "readUnsignedShort()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(2);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageInputStream iis = ictx.inputStream;
            final int length = ictx.length;
            int pos = 0;
            try {
                iis.mark();
                do {
                    if (pos + 2 > length) {
                        iis.reset();
                        iis.mark();
                        pos = 0;
                    }
                    iis.readUnsignedShort();
                    pos += 2;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { iis.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IISReadInt extends InputStreamTests {
        public IISReadInt() {
            super(streamTestRoot,
                  "readInt",
                  "readInt()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(4);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageInputStream iis = ictx.inputStream;
            final int length = ictx.length;
            int pos = 0;
            try {
                iis.mark();
                do {
                    if (pos + 4 > length) {
                        iis.reset();
                        iis.mark();
                        pos = 0;
                    }
                    iis.readInt();
                    pos += 4;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { iis.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IISReadUnsignedInt extends InputStreamTests {
        public IISReadUnsignedInt() {
            super(streamTestRoot,
                  "readUnsignedInt",
                  "readUnsignedInt()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(4);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageInputStream iis = ictx.inputStream;
            final int length = ictx.length;
            int pos = 0;
            try {
                iis.mark();
                do {
                    if (pos + 4 > length) {
                        iis.reset();
                        iis.mark();
                        pos = 0;
                    }
                    iis.readUnsignedInt();
                    pos += 4;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { iis.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IISReadFloat extends InputStreamTests {
        public IISReadFloat() {
            super(streamTestRoot,
                  "readFloat",
                  "readFloat()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(4);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageInputStream iis = ictx.inputStream;
            final int length = ictx.length;
            int pos = 0;
            try {
                iis.mark();
                do {
                    if (pos + 4 > length) {
                        iis.reset();
                        iis.mark();
                        pos = 0;
                    }
                    iis.readFloat();
                    pos += 4;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { iis.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IISReadLong extends InputStreamTests {
        public IISReadLong() {
            super(streamTestRoot,
                  "readLong",
                  "readLong()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(8);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageInputStream iis = ictx.inputStream;
            final int length = ictx.length;
            int pos = 0;
            try {
                iis.mark();
                do {
                    if (pos + 8 > length) {
                        iis.reset();
                        iis.mark();
                        pos = 0;
                    }
                    iis.readLong();
                    pos += 8;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { iis.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IISReadDouble extends InputStreamTests {
        public IISReadDouble() {
            super(streamTestRoot,
                  "readDouble",
                  "readDouble()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(8);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageInputStream iis = ictx.inputStream;
            final int length = ictx.length;
            int pos = 0;
            try {
                iis.mark();
                do {
                    if (pos + 8 > length) {
                        iis.reset();
                        iis.mark();
                        pos = 0;
                    }
                    iis.readDouble();
                    pos += 8;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { iis.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IISSkipBytes extends InputStreamTests {
        public IISSkipBytes() {
            super(streamTestRoot,
                  "skipBytes",
                  "skipBytes() (one \"scanline\" at a time)");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(ctx.scanlineStride);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context ictx = (Context)ctx;
            final ImageInputStream iis = ictx.inputStream;
            final int scanlineStride = ictx.scanlineStride;
            final int length = ictx.length;
            int pos = 0;
            try {
                iis.mark();
                do {
                    if (pos + scanlineStride > length) {
                        iis.reset();
                        iis.mark();
                        pos = 0;
                    }
                    iis.skipBytes(scanlineStride);
                    pos += scanlineStride;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { iis.reset(); } catch (IOException e) {}
            }
        }
    }
}
