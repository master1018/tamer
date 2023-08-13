class UTF_8 extends Unicode
{
    public UTF_8() {
        super("UTF-8", StandardCharsets.aliases_UTF_8);
    }
    public String historicalName() {
        return "UTF8";
    }
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }
    static final void updatePositions(Buffer src, int sp,
                                      Buffer dst, int dp) {
        src.position(sp - src.arrayOffset());
        dst.position(dp - dst.arrayOffset());
    }
    private static class Decoder extends CharsetDecoder
                                 implements ArrayDecoder {
        private Decoder(Charset cs) {
            super(cs, 1.0f, 1.0f);
        }
        private static boolean isNotContinuation(int b) {
            return (b & 0xc0) != 0x80;
        }
        private static boolean isMalformed2(int b1, int b2) {
            return (b1 & 0x1e) == 0x0 || (b2 & 0xc0) != 0x80;
        }
        private static boolean isMalformed3(int b1, int b2, int b3) {
            return (b1 == (byte)0xe0 && (b2 & 0xe0) == 0x80) ||
                   (b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80;
        }
        private static boolean isMalformed4(int b2, int b3, int b4) {
            return (b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80 ||
                   (b4 & 0xc0) != 0x80;
        }
        private static CoderResult lookupN(ByteBuffer src, int n)
        {
            for (int i = 1; i < n; i++) {
               if (isNotContinuation(src.get()))
                   return CoderResult.malformedForLength(i);
            }
            return CoderResult.malformedForLength(n);
        }
        private static CoderResult malformedN(ByteBuffer src, int nb) {
            switch (nb) {
            case 1:
                int b1 = src.get();
                if ((b1 >> 2) == -2) {
                    if (src.remaining() < 4)
                        return CoderResult.UNDERFLOW;
                    return lookupN(src, 5);
                }
                if ((b1 >> 1) == -2) {
                    if (src.remaining() < 5)
                        return CoderResult.UNDERFLOW;
                    return lookupN(src, 6);
                }
                return CoderResult.malformedForLength(1);
            case 2:                    
                return CoderResult.malformedForLength(1);
            case 3:
                b1 = src.get();
                int b2 = src.get();    
                return CoderResult.malformedForLength(
                    ((b1 == (byte)0xe0 && (b2 & 0xe0) == 0x80) ||
                     isNotContinuation(b2))?1:2);
            case 4:  
                b1 = src.get() & 0xff;
                b2 = src.get() & 0xff;
                if (b1 > 0xf4 ||
                    (b1 == 0xf0 && (b2 < 0x90 || b2 > 0xbf)) ||
                    (b1 == 0xf4 && (b2 & 0xf0) != 0x80) ||
                    isNotContinuation(b2))
                    return CoderResult.malformedForLength(1);
                if (isNotContinuation(src.get()))
                    return CoderResult.malformedForLength(2);
                return CoderResult.malformedForLength(3);
            default:
                assert false;
                return null;
            }
        }
        private static CoderResult malformed(ByteBuffer src, int sp,
                                             CharBuffer dst, int dp,
                                             int nb)
        {
            src.position(sp - src.arrayOffset());
            CoderResult cr = malformedN(src, nb);
            updatePositions(src, sp, dst, dp);
            return cr;
        }
        private static CoderResult malformed(ByteBuffer src,
                                             int mark, int nb)
        {
            src.position(mark);
            CoderResult cr = malformedN(src, nb);
            src.position(mark);
            return cr;
        }
        private static CoderResult xflow(Buffer src, int sp, int sl,
                                         Buffer dst, int dp, int nb) {
            updatePositions(src, sp, dst, dp);
            return (nb == 0 || sl - sp < nb)
                   ?CoderResult.UNDERFLOW:CoderResult.OVERFLOW;
        }
        private static CoderResult xflow(Buffer src, int mark, int nb) {
            CoderResult cr = (nb == 0 || src.remaining() < (nb - 1))
                             ?CoderResult.UNDERFLOW:CoderResult.OVERFLOW;
            src.position(mark);
            return cr;
        }
        private CoderResult decodeArrayLoop(ByteBuffer src,
                                            CharBuffer dst)
        {
            byte[] sa = src.array();
            int sp = src.arrayOffset() + src.position();
            int sl = src.arrayOffset() + src.limit();
            char[] da = dst.array();
            int dp = dst.arrayOffset() + dst.position();
            int dl = dst.arrayOffset() + dst.limit();
            int dlASCII = dp + Math.min(sl - sp, dl - dp);
            while (dp < dlASCII && sa[sp] >= 0)
                da[dp++] = (char) sa[sp++];
            while (sp < sl) {
                int b1 = sa[sp];
                if (b1 >= 0) {
                    if (dp >= dl)
                        return xflow(src, sp, sl, dst, dp, 1);
                    da[dp++] = (char) b1;
                    sp++;
                } else if ((b1 >> 5) == -2) {
                    if (sl - sp < 2 || dp >= dl)
                        return xflow(src, sp, sl, dst, dp, 2);
                    int b2 = sa[sp + 1];
                    if (isMalformed2(b1, b2))
                        return malformed(src, sp, dst, dp, 2);
                    da[dp++] = (char) (((b1 << 6) ^ b2)
                                       ^
                                       (((byte) 0xC0 << 6) ^
                                        ((byte) 0x80 << 0)));
                    sp += 2;
                } else if ((b1 >> 4) == -2) {
                    if (sl - sp < 3 || dp >= dl)
                        return xflow(src, sp, sl, dst, dp, 3);
                    int b2 = sa[sp + 1];
                    int b3 = sa[sp + 2];
                    if (isMalformed3(b1, b2, b3))
                        return malformed(src, sp, dst, dp, 3);
                    da[dp++] = (char)
                        ((b1 << 12) ^
                         (b2 <<  6) ^
                         (b3 ^
                          (((byte) 0xE0 << 12) ^
                           ((byte) 0x80 <<  6) ^
                           ((byte) 0x80 <<  0))));
                    sp += 3;
                } else if ((b1 >> 3) == -2) {
                    if (sl - sp < 4 || dl - dp < 2)
                        return xflow(src, sp, sl, dst, dp, 4);
                    int b2 = sa[sp + 1];
                    int b3 = sa[sp + 2];
                    int b4 = sa[sp + 3];
                    int uc = ((b1 << 18) ^
                              (b2 << 12) ^
                              (b3 <<  6) ^
                              (b4 ^
                               (((byte) 0xF0 << 18) ^
                                ((byte) 0x80 << 12) ^
                                ((byte) 0x80 <<  6) ^
                                ((byte) 0x80 <<  0))));
                    if (isMalformed4(b2, b3, b4) ||
                        !Character.isSupplementaryCodePoint(uc)) {
                        return malformed(src, sp, dst, dp, 4);
                    }
                    da[dp++] = Character.highSurrogate(uc);
                    da[dp++] = Character.lowSurrogate(uc);
                    sp += 4;
                } else
                    return malformed(src, sp, dst, dp, 1);
            }
            return xflow(src, sp, sl, dst, dp, 0);
        }
        private CoderResult decodeBufferLoop(ByteBuffer src,
                                             CharBuffer dst)
        {
            int mark = src.position();
            int limit = src.limit();
            while (mark < limit) {
                int b1 = src.get();
                if (b1 >= 0) {
                    if (dst.remaining() < 1)
                        return xflow(src, mark, 1); 
                    dst.put((char) b1);
                    mark++;
                } else if ((b1 >> 5) == -2) {
                    if (limit - mark < 2|| dst.remaining() < 1)
                        return xflow(src, mark, 2);
                    int b2 = src.get();
                    if (isMalformed2(b1, b2))
                        return malformed(src, mark, 2);
                    dst.put((char) (((b1 << 6) ^ b2)
                                    ^
                                    (((byte) 0xC0 << 6) ^
                                     ((byte) 0x80 << 0))));
                    mark += 2;
                } else if ((b1 >> 4) == -2) {
                    if (limit - mark < 3 || dst.remaining() < 1)
                        return xflow(src, mark, 3);
                    int b2 = src.get();
                    int b3 = src.get();
                    if (isMalformed3(b1, b2, b3))
                        return malformed(src, mark, 3);
                    dst.put((char)
                            ((b1 << 12) ^
                             (b2 <<  6) ^
                             (b3 ^
                              (((byte) 0xE0 << 12) ^
                               ((byte) 0x80 <<  6) ^
                               ((byte) 0x80 <<  0)))));
                    mark += 3;
                } else if ((b1 >> 3) == -2) {
                    if (limit - mark < 4 || dst.remaining() < 2)
                        return xflow(src, mark, 4);
                    int b2 = src.get();
                    int b3 = src.get();
                    int b4 = src.get();
                    int uc = ((b1 << 18) ^
                              (b2 << 12) ^
                              (b3 <<  6) ^
                              (b4 ^
                               (((byte) 0xF0 << 18) ^
                                ((byte) 0x80 << 12) ^
                                ((byte) 0x80 <<  6) ^
                                ((byte) 0x80 <<  0))));
                    if (isMalformed4(b2, b3, b4) ||
                        !Character.isSupplementaryCodePoint(uc)) {
                        return malformed(src, mark, 4);
                    }
                    dst.put(Character.highSurrogate(uc));
                    dst.put(Character.lowSurrogate(uc));
                    mark += 4;
                } else {
                    return malformed(src, mark, 1);
                }
            }
            return xflow(src, mark, 0);
        }
        protected CoderResult decodeLoop(ByteBuffer src,
                                         CharBuffer dst)
        {
            if (src.hasArray() && dst.hasArray())
                return decodeArrayLoop(src, dst);
            else
                return decodeBufferLoop(src, dst);
        }
        private static ByteBuffer getByteBuffer(ByteBuffer bb, byte[] ba, int sp)
        {
            if (bb == null)
                bb = ByteBuffer.wrap(ba);
            bb.position(sp);
            return bb;
        }
        public int decode(byte[] sa, int sp, int len, char[] da) {
            final int sl = sp + len;
            int dp = 0;
            int dlASCII = Math.min(len, da.length);
            ByteBuffer bb = null;  
            while (dp < dlASCII && sa[sp] >= 0)
                da[dp++] = (char) sa[sp++];
            while (sp < sl) {
                int b1 = sa[sp++];
                if (b1 >= 0) {
                    da[dp++] = (char) b1;
                } else if ((b1 >> 5) == -2) {
                    if (sp < sl) {
                        int b2 = sa[sp++];
                        if (isMalformed2(b1, b2)) {
                            if (malformedInputAction() != CodingErrorAction.REPLACE)
                                return -1;
                            da[dp++] = replacement().charAt(0);
                            sp--;            
                        } else {
                            da[dp++] = (char) (((b1 << 6) ^ b2)^
                                           (((byte) 0xC0 << 6) ^
                                            ((byte) 0x80 << 0)));
                        }
                        continue;
                    }
                    if (malformedInputAction() != CodingErrorAction.REPLACE)
                        return -1;
                    da[dp++] = replacement().charAt(0);
                    return dp;
                } else if ((b1 >> 4) == -2) {
                    if (sp + 1 < sl) {
                        int b2 = sa[sp++];
                        int b3 = sa[sp++];
                        if (isMalformed3(b1, b2, b3)) {
                            if (malformedInputAction() != CodingErrorAction.REPLACE)
                                return -1;
                            da[dp++] = replacement().charAt(0);
                            sp -=3;
                            bb = getByteBuffer(bb, sa, sp);
                            sp += malformedN(bb, 3).length();
                        } else {
                            da[dp++] = (char)((b1 << 12) ^
                                              (b2 <<  6) ^
                                              (b3 ^
                                              (((byte) 0xE0 << 12) ^
                                              ((byte) 0x80 <<  6) ^
                                              ((byte) 0x80 <<  0))));
                        }
                        continue;
                    }
                    if (malformedInputAction() != CodingErrorAction.REPLACE)
                        return -1;
                    da[dp++] = replacement().charAt(0);
                    return dp;
                } else if ((b1 >> 3) == -2) {
                    if (sp + 2 < sl) {
                        int b2 = sa[sp++];
                        int b3 = sa[sp++];
                        int b4 = sa[sp++];
                        int uc = ((b1 << 18) ^
                                  (b2 << 12) ^
                                  (b3 <<  6) ^
                                  (b4 ^
                                   (((byte) 0xF0 << 18) ^
                                   ((byte) 0x80 << 12) ^
                                   ((byte) 0x80 <<  6) ^
                                   ((byte) 0x80 <<  0))));
                        if (isMalformed4(b2, b3, b4) ||
                            !Character.isSupplementaryCodePoint(uc)) {
                            if (malformedInputAction() != CodingErrorAction.REPLACE)
                                return -1;
                            da[dp++] = replacement().charAt(0);
                            sp -= 4;
                            bb = getByteBuffer(bb, sa, sp);
                            sp += malformedN(bb, 4).length();
                        } else {
                            da[dp++] = Character.highSurrogate(uc);
                            da[dp++] = Character.lowSurrogate(uc);
                        }
                        continue;
                    }
                    if (malformedInputAction() != CodingErrorAction.REPLACE)
                        return -1;
                    da[dp++] = replacement().charAt(0);
                    return dp;
                } else {
                    if (malformedInputAction() != CodingErrorAction.REPLACE)
                        return -1;
                    da[dp++] = replacement().charAt(0);
                    sp--;
                    bb = getByteBuffer(bb, sa, sp);
                    CoderResult cr = malformedN(bb, 1);
                    if (!cr.isError()) {
                        return dp;
                    }
                    sp +=  cr.length();
                }
            }
            return dp;
        }
    }
    private static class Encoder extends CharsetEncoder
                                 implements ArrayEncoder {
        private Encoder(Charset cs) {
            super(cs, 1.1f, 3.0f);
        }
        public boolean canEncode(char c) {
            return !Character.isSurrogate(c);
        }
        public boolean isLegalReplacement(byte[] repl) {
            return ((repl.length == 1 && repl[0] >= 0) ||
                    super.isLegalReplacement(repl));
        }
        private static CoderResult overflow(CharBuffer src, int sp,
                                            ByteBuffer dst, int dp) {
            updatePositions(src, sp, dst, dp);
            return CoderResult.OVERFLOW;
        }
        private static CoderResult overflow(CharBuffer src, int mark) {
            src.position(mark);
            return CoderResult.OVERFLOW;
        }
        private Surrogate.Parser sgp;
        private CoderResult encodeArrayLoop(CharBuffer src,
                                            ByteBuffer dst)
        {
            char[] sa = src.array();
            int sp = src.arrayOffset() + src.position();
            int sl = src.arrayOffset() + src.limit();
            byte[] da = dst.array();
            int dp = dst.arrayOffset() + dst.position();
            int dl = dst.arrayOffset() + dst.limit();
            int dlASCII = dp + Math.min(sl - sp, dl - dp);
            while (dp < dlASCII && sa[sp] < '\u0080')
                da[dp++] = (byte) sa[sp++];
            while (sp < sl) {
                char c = sa[sp];
                if (c < 0x80) {
                    if (dp >= dl)
                        return overflow(src, sp, dst, dp);
                    da[dp++] = (byte)c;
                } else if (c < 0x800) {
                    if (dl - dp < 2)
                        return overflow(src, sp, dst, dp);
                    da[dp++] = (byte)(0xc0 | (c >> 6));
                    da[dp++] = (byte)(0x80 | (c & 0x3f));
                } else if (Character.isSurrogate(c)) {
                    if (sgp == null)
                        sgp = new Surrogate.Parser();
                    int uc = sgp.parse(c, sa, sp, sl);
                    if (uc < 0) {
                        updatePositions(src, sp, dst, dp);
                        return sgp.error();
                    }
                    if (dl - dp < 4)
                        return overflow(src, sp, dst, dp);
                    da[dp++] = (byte)(0xf0 | ((uc >> 18)));
                    da[dp++] = (byte)(0x80 | ((uc >> 12) & 0x3f));
                    da[dp++] = (byte)(0x80 | ((uc >>  6) & 0x3f));
                    da[dp++] = (byte)(0x80 | (uc & 0x3f));
                    sp++;  
                } else {
                    if (dl - dp < 3)
                        return overflow(src, sp, dst, dp);
                    da[dp++] = (byte)(0xe0 | ((c >> 12)));
                    da[dp++] = (byte)(0x80 | ((c >>  6) & 0x3f));
                    da[dp++] = (byte)(0x80 | (c & 0x3f));
                }
                sp++;
            }
            updatePositions(src, sp, dst, dp);
            return CoderResult.UNDERFLOW;
        }
        private CoderResult encodeBufferLoop(CharBuffer src,
                                             ByteBuffer dst)
        {
            int mark = src.position();
            while (src.hasRemaining()) {
                char c = src.get();
                if (c < 0x80) {
                    if (!dst.hasRemaining())
                        return overflow(src, mark);
                    dst.put((byte)c);
                } else if (c < 0x800) {
                    if (dst.remaining() < 2)
                        return overflow(src, mark);
                    dst.put((byte)(0xc0 | (c >> 6)));
                    dst.put((byte)(0x80 | (c & 0x3f)));
                } else if (Character.isSurrogate(c)) {
                    if (sgp == null)
                        sgp = new Surrogate.Parser();
                    int uc = sgp.parse(c, src);
                    if (uc < 0) {
                        src.position(mark);
                        return sgp.error();
                    }
                    if (dst.remaining() < 4)
                        return overflow(src, mark);
                    dst.put((byte)(0xf0 | ((uc >> 18))));
                    dst.put((byte)(0x80 | ((uc >> 12) & 0x3f)));
                    dst.put((byte)(0x80 | ((uc >>  6) & 0x3f)));
                    dst.put((byte)(0x80 | (uc & 0x3f)));
                    mark++;  
                } else {
                    if (dst.remaining() < 3)
                        return overflow(src, mark);
                    dst.put((byte)(0xe0 | ((c >> 12))));
                    dst.put((byte)(0x80 | ((c >>  6) & 0x3f)));
                    dst.put((byte)(0x80 | (c & 0x3f)));
                }
                mark++;
            }
            src.position(mark);
            return CoderResult.UNDERFLOW;
        }
        protected final CoderResult encodeLoop(CharBuffer src,
                                               ByteBuffer dst)
        {
            if (src.hasArray() && dst.hasArray())
                return encodeArrayLoop(src, dst);
            else
                return encodeBufferLoop(src, dst);
        }
        public int encode(char[] sa, int sp, int len, byte[] da) {
            int sl = sp + len;
            int dp = 0;
            int dlASCII = dp + Math.min(len, da.length);
            while (dp < dlASCII && sa[sp] < '\u0080')
                da[dp++] = (byte) sa[sp++];
            while (sp < sl) {
                char c = sa[sp++];
                if (c < 0x80) {
                    da[dp++] = (byte)c;
                } else if (c < 0x800) {
                    da[dp++] = (byte)(0xc0 | (c >> 6));
                    da[dp++] = (byte)(0x80 | (c & 0x3f));
                } else if (Character.isSurrogate(c)) {
                    if (sgp == null)
                        sgp = new Surrogate.Parser();
                    int uc = sgp.parse(c, sa, sp - 1, sl);
                    if (uc < 0) {
                        if (malformedInputAction() != CodingErrorAction.REPLACE)
                            return -1;
                        da[dp++] = replacement()[0];
                    } else {
                        da[dp++] = (byte)(0xf0 | ((uc >> 18)));
                        da[dp++] = (byte)(0x80 | ((uc >> 12) & 0x3f));
                        da[dp++] = (byte)(0x80 | ((uc >>  6) & 0x3f));
                        da[dp++] = (byte)(0x80 | (uc & 0x3f));
                        sp++;  
                    }
                } else {
                    da[dp++] = (byte)(0xe0 | ((c >> 12)));
                    da[dp++] = (byte)(0x80 | ((c >>  6) & 0x3f));
                    da[dp++] = (byte)(0x80 | (c & 0x3f));
                }
            }
            return dp;
        }
    }
}
