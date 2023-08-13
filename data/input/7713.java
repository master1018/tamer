final class ByteArrayAccess {
    private ByteArrayAccess() {
    }
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final boolean littleEndianUnaligned;
    private static final boolean bigEndian;
    private final static int byteArrayOfs = unsafe.arrayBaseOffset(byte[].class);
    static {
        boolean scaleOK = ((unsafe.arrayIndexScale(byte[].class) == 1)
            && (unsafe.arrayIndexScale(int[].class) == 4)
            && (unsafe.arrayIndexScale(long[].class) == 8)
            && ((byteArrayOfs & 3) == 0));
        ByteOrder byteOrder = ByteOrder.nativeOrder();
        littleEndianUnaligned =
            scaleOK && unaligned() && (byteOrder == ByteOrder.LITTLE_ENDIAN);
        bigEndian =
            scaleOK && (byteOrder == ByteOrder.BIG_ENDIAN);
    }
    private static boolean unaligned() {
        String arch = java.security.AccessController.doPrivileged
            (new sun.security.action.GetPropertyAction("os.arch", ""));
        return arch.equals("i386") || arch.equals("x86") || arch.equals("amd64");
    }
    static void b2iLittle(byte[] in, int inOfs, int[] out, int outOfs, int len) {
        if (littleEndianUnaligned) {
            inOfs += byteArrayOfs;
            len += inOfs;
            while (inOfs < len) {
                out[outOfs++] = unsafe.getInt(in, (long)inOfs);
                inOfs += 4;
            }
        } else if (bigEndian && ((inOfs & 3) == 0)) {
            inOfs += byteArrayOfs;
            len += inOfs;
            while (inOfs < len) {
                out[outOfs++] = reverseBytes(unsafe.getInt(in, (long)inOfs));
                inOfs += 4;
            }
        } else {
            len += inOfs;
            while (inOfs < len) {
                out[outOfs++] = ((in[inOfs    ] & 0xff)      )
                              | ((in[inOfs + 1] & 0xff) <<  8)
                              | ((in[inOfs + 2] & 0xff) << 16)
                              | ((in[inOfs + 3]       ) << 24);
                inOfs += 4;
            }
        }
    }
    static void b2iLittle64(byte[] in, int inOfs, int[] out) {
        if (littleEndianUnaligned) {
            inOfs += byteArrayOfs;
            out[ 0] = unsafe.getInt(in, (long)(inOfs     ));
            out[ 1] = unsafe.getInt(in, (long)(inOfs +  4));
            out[ 2] = unsafe.getInt(in, (long)(inOfs +  8));
            out[ 3] = unsafe.getInt(in, (long)(inOfs + 12));
            out[ 4] = unsafe.getInt(in, (long)(inOfs + 16));
            out[ 5] = unsafe.getInt(in, (long)(inOfs + 20));
            out[ 6] = unsafe.getInt(in, (long)(inOfs + 24));
            out[ 7] = unsafe.getInt(in, (long)(inOfs + 28));
            out[ 8] = unsafe.getInt(in, (long)(inOfs + 32));
            out[ 9] = unsafe.getInt(in, (long)(inOfs + 36));
            out[10] = unsafe.getInt(in, (long)(inOfs + 40));
            out[11] = unsafe.getInt(in, (long)(inOfs + 44));
            out[12] = unsafe.getInt(in, (long)(inOfs + 48));
            out[13] = unsafe.getInt(in, (long)(inOfs + 52));
            out[14] = unsafe.getInt(in, (long)(inOfs + 56));
            out[15] = unsafe.getInt(in, (long)(inOfs + 60));
        } else if (bigEndian && ((inOfs & 3) == 0)) {
            inOfs += byteArrayOfs;
            out[ 0] = reverseBytes(unsafe.getInt(in, (long)(inOfs     )));
            out[ 1] = reverseBytes(unsafe.getInt(in, (long)(inOfs +  4)));
            out[ 2] = reverseBytes(unsafe.getInt(in, (long)(inOfs +  8)));
            out[ 3] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 12)));
            out[ 4] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 16)));
            out[ 5] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 20)));
            out[ 6] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 24)));
            out[ 7] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 28)));
            out[ 8] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 32)));
            out[ 9] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 36)));
            out[10] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 40)));
            out[11] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 44)));
            out[12] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 48)));
            out[13] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 52)));
            out[14] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 56)));
            out[15] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 60)));
        } else {
            b2iLittle(in, inOfs, out, 0, 64);
        }
    }
    static void i2bLittle(int[] in, int inOfs, byte[] out, int outOfs, int len) {
        if (littleEndianUnaligned) {
            outOfs += byteArrayOfs;
            len += outOfs;
            while (outOfs < len) {
                unsafe.putInt(out, (long)outOfs, in[inOfs++]);
                outOfs += 4;
            }
        } else if (bigEndian && ((outOfs & 3) == 0)) {
            outOfs += byteArrayOfs;
            len += outOfs;
            while (outOfs < len) {
                unsafe.putInt(out, (long)outOfs, reverseBytes(in[inOfs++]));
                outOfs += 4;
            }
        } else {
            len += outOfs;
            while (outOfs < len) {
                int i = in[inOfs++];
                out[outOfs++] = (byte)(i      );
                out[outOfs++] = (byte)(i >>  8);
                out[outOfs++] = (byte)(i >> 16);
                out[outOfs++] = (byte)(i >> 24);
            }
        }
    }
    static void i2bLittle4(int val, byte[] out, int outOfs) {
        if (littleEndianUnaligned) {
            unsafe.putInt(out, (long)(byteArrayOfs + outOfs), val);
        } else if (bigEndian && ((outOfs & 3) == 0)) {
            unsafe.putInt(out, (long)(byteArrayOfs + outOfs), reverseBytes(val));
        } else {
            out[outOfs    ] = (byte)(val      );
            out[outOfs + 1] = (byte)(val >>  8);
            out[outOfs + 2] = (byte)(val >> 16);
            out[outOfs + 3] = (byte)(val >> 24);
        }
    }
    static void b2iBig(byte[] in, int inOfs, int[] out, int outOfs, int len) {
        if (littleEndianUnaligned) {
            inOfs += byteArrayOfs;
            len += inOfs;
            while (inOfs < len) {
                out[outOfs++] = reverseBytes(unsafe.getInt(in, (long)inOfs));
                inOfs += 4;
            }
        } else if (bigEndian && ((inOfs & 3) == 0)) {
            inOfs += byteArrayOfs;
            len += inOfs;
            while (inOfs < len) {
                out[outOfs++] = unsafe.getInt(in, (long)inOfs);
                inOfs += 4;
            }
        } else {
            len += inOfs;
            while (inOfs < len) {
                out[outOfs++] = ((in[inOfs + 3] & 0xff)      )
                              | ((in[inOfs + 2] & 0xff) <<  8)
                              | ((in[inOfs + 1] & 0xff) << 16)
                              | ((in[inOfs    ]       ) << 24);
                inOfs += 4;
            }
        }
    }
    static void b2iBig64(byte[] in, int inOfs, int[] out) {
        if (littleEndianUnaligned) {
            inOfs += byteArrayOfs;
            out[ 0] = reverseBytes(unsafe.getInt(in, (long)(inOfs     )));
            out[ 1] = reverseBytes(unsafe.getInt(in, (long)(inOfs +  4)));
            out[ 2] = reverseBytes(unsafe.getInt(in, (long)(inOfs +  8)));
            out[ 3] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 12)));
            out[ 4] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 16)));
            out[ 5] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 20)));
            out[ 6] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 24)));
            out[ 7] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 28)));
            out[ 8] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 32)));
            out[ 9] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 36)));
            out[10] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 40)));
            out[11] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 44)));
            out[12] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 48)));
            out[13] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 52)));
            out[14] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 56)));
            out[15] = reverseBytes(unsafe.getInt(in, (long)(inOfs + 60)));
        } else if (bigEndian && ((inOfs & 3) == 0)) {
            inOfs += byteArrayOfs;
            out[ 0] = unsafe.getInt(in, (long)(inOfs     ));
            out[ 1] = unsafe.getInt(in, (long)(inOfs +  4));
            out[ 2] = unsafe.getInt(in, (long)(inOfs +  8));
            out[ 3] = unsafe.getInt(in, (long)(inOfs + 12));
            out[ 4] = unsafe.getInt(in, (long)(inOfs + 16));
            out[ 5] = unsafe.getInt(in, (long)(inOfs + 20));
            out[ 6] = unsafe.getInt(in, (long)(inOfs + 24));
            out[ 7] = unsafe.getInt(in, (long)(inOfs + 28));
            out[ 8] = unsafe.getInt(in, (long)(inOfs + 32));
            out[ 9] = unsafe.getInt(in, (long)(inOfs + 36));
            out[10] = unsafe.getInt(in, (long)(inOfs + 40));
            out[11] = unsafe.getInt(in, (long)(inOfs + 44));
            out[12] = unsafe.getInt(in, (long)(inOfs + 48));
            out[13] = unsafe.getInt(in, (long)(inOfs + 52));
            out[14] = unsafe.getInt(in, (long)(inOfs + 56));
            out[15] = unsafe.getInt(in, (long)(inOfs + 60));
        } else {
            b2iBig(in, inOfs, out, 0, 64);
        }
    }
    static void i2bBig(int[] in, int inOfs, byte[] out, int outOfs, int len) {
        if (littleEndianUnaligned) {
            outOfs += byteArrayOfs;
            len += outOfs;
            while (outOfs < len) {
                unsafe.putInt(out, (long)outOfs, reverseBytes(in[inOfs++]));
                outOfs += 4;
            }
        } else if (bigEndian && ((outOfs & 3) == 0)) {
            outOfs += byteArrayOfs;
            len += outOfs;
            while (outOfs < len) {
                unsafe.putInt(out, (long)outOfs, in[inOfs++]);
                outOfs += 4;
            }
        } else {
            len += outOfs;
            while (outOfs < len) {
                int i = in[inOfs++];
                out[outOfs++] = (byte)(i >> 24);
                out[outOfs++] = (byte)(i >> 16);
                out[outOfs++] = (byte)(i >>  8);
                out[outOfs++] = (byte)(i      );
            }
        }
    }
    static void i2bBig4(int val, byte[] out, int outOfs) {
        if (littleEndianUnaligned) {
            unsafe.putInt(out, (long)(byteArrayOfs + outOfs), reverseBytes(val));
        } else if (bigEndian && ((outOfs & 3) == 0)) {
            unsafe.putInt(out, (long)(byteArrayOfs + outOfs), val);
        } else {
            out[outOfs    ] = (byte)(val >> 24);
            out[outOfs + 1] = (byte)(val >> 16);
            out[outOfs + 2] = (byte)(val >>  8);
            out[outOfs + 3] = (byte)(val      );
        }
    }
    static void b2lBig(byte[] in, int inOfs, long[] out, int outOfs, int len) {
        if (littleEndianUnaligned) {
            inOfs += byteArrayOfs;
            len += inOfs;
            while (inOfs < len) {
                out[outOfs++] = reverseBytes(unsafe.getLong(in, (long)inOfs));
                inOfs += 8;
            }
        } else if (bigEndian && ((inOfs & 3) == 0)) {
            inOfs += byteArrayOfs;
            len += inOfs;
            while (inOfs < len) {
                out[outOfs++] =
                      ((long)unsafe.getInt(in, (long)inOfs) << 32)
                          | (unsafe.getInt(in, (long)(inOfs + 4)) & 0xffffffffL);
                inOfs += 8;
            }
        } else {
            len += inOfs;
            while (inOfs < len) {
                int i1 = ((in[inOfs + 3] & 0xff)      )
                       | ((in[inOfs + 2] & 0xff) <<  8)
                       | ((in[inOfs + 1] & 0xff) << 16)
                       | ((in[inOfs    ]       ) << 24);
                inOfs += 4;
                int i2 = ((in[inOfs + 3] & 0xff)      )
                       | ((in[inOfs + 2] & 0xff) <<  8)
                       | ((in[inOfs + 1] & 0xff) << 16)
                       | ((in[inOfs    ]       ) << 24);
                out[outOfs++] = ((long)i1 << 32) | (i2 & 0xffffffffL);
                inOfs += 4;
            }
        }
    }
    static void b2lBig128(byte[] in, int inOfs, long[] out) {
        if (littleEndianUnaligned) {
            inOfs += byteArrayOfs;
            out[ 0] = reverseBytes(unsafe.getLong(in, (long)(inOfs      )));
            out[ 1] = reverseBytes(unsafe.getLong(in, (long)(inOfs +   8)));
            out[ 2] = reverseBytes(unsafe.getLong(in, (long)(inOfs +  16)));
            out[ 3] = reverseBytes(unsafe.getLong(in, (long)(inOfs +  24)));
            out[ 4] = reverseBytes(unsafe.getLong(in, (long)(inOfs +  32)));
            out[ 5] = reverseBytes(unsafe.getLong(in, (long)(inOfs +  40)));
            out[ 6] = reverseBytes(unsafe.getLong(in, (long)(inOfs +  48)));
            out[ 7] = reverseBytes(unsafe.getLong(in, (long)(inOfs +  56)));
            out[ 8] = reverseBytes(unsafe.getLong(in, (long)(inOfs +  64)));
            out[ 9] = reverseBytes(unsafe.getLong(in, (long)(inOfs +  72)));
            out[10] = reverseBytes(unsafe.getLong(in, (long)(inOfs +  80)));
            out[11] = reverseBytes(unsafe.getLong(in, (long)(inOfs +  88)));
            out[12] = reverseBytes(unsafe.getLong(in, (long)(inOfs +  96)));
            out[13] = reverseBytes(unsafe.getLong(in, (long)(inOfs + 104)));
            out[14] = reverseBytes(unsafe.getLong(in, (long)(inOfs + 112)));
            out[15] = reverseBytes(unsafe.getLong(in, (long)(inOfs + 120)));
        } else {
            b2lBig(in, inOfs, out, 0, 128);
        }
    }
    static void l2bBig(long[] in, int inOfs, byte[] out, int outOfs, int len) {
        len += outOfs;
        while (outOfs < len) {
            long i = in[inOfs++];
            out[outOfs++] = (byte)(i >> 56);
            out[outOfs++] = (byte)(i >> 48);
            out[outOfs++] = (byte)(i >> 40);
            out[outOfs++] = (byte)(i >> 32);
            out[outOfs++] = (byte)(i >> 24);
            out[outOfs++] = (byte)(i >> 16);
            out[outOfs++] = (byte)(i >>  8);
            out[outOfs++] = (byte)(i      );
        }
    }
}
