 class NativeStrike extends PhysicalStrike {
     NativeFont nativeFont;
     int numGlyphs;
     AffineTransform invertDevTx;
     AffineTransform fontTx;
     private int getNativePointSize() {
         double[] mat = new double[4];
         desc.glyphTx.getMatrix(mat);
         fontTx = new AffineTransform(mat);
         if (!desc.devTx.isIdentity() &&
             desc.devTx.getType() != AffineTransform.TYPE_TRANSLATION) {
             try {
                 invertDevTx = desc.devTx.createInverse();
                 fontTx.concatenate(invertDevTx);
             } catch (NoninvertibleTransformException e) {
                 e.printStackTrace();
             }
         }
         Point2D.Float pt = new Point2D.Float(1f,1f);
         fontTx.deltaTransform(pt, pt);
         double ptSize = Math.abs(pt.y);
         int ttype = fontTx.getType();
         if ((ttype & ~AffineTransform.TYPE_UNIFORM_SCALE) != 0 ||
             fontTx.getScaleY() <= 0) {
             fontTx.scale(1/ptSize, 1/ptSize);
         } else {
             fontTx = null; 
         }
         return (int)ptSize;
     }
     NativeStrike(NativeFont nativeFont, FontStrikeDesc desc) {
         super(nativeFont, desc);
         this.nativeFont = nativeFont;
         if (nativeFont.isBitmapDelegate) {
             int ttype = desc.glyphTx.getType();
             if ((ttype & ~AffineTransform.TYPE_UNIFORM_SCALE) != 0 ||
                 desc.glyphTx.getScaleX() <= 0) {
             numGlyphs = 0;
             return;
             }
         }
         int ptSize = getNativePointSize();
         byte [] nameBytes = nativeFont.getPlatformNameBytes(ptSize);
         double scale = Math.abs(desc.devTx.getScaleX());
         pScalerContext = createScalerContext(nameBytes, ptSize, scale);
         if (pScalerContext == 0L) {
             SunFontManager.getInstance().deRegisterBadFont(nativeFont);
             pScalerContext = createNullScalerContext();
             numGlyphs = 0;
             if (FontUtilities.isLogging()) {
                 FontUtilities.getLogger()
                                   .severe("Could not create native strike " +
                                           new String(nameBytes));
             }
             return;
         }
         numGlyphs = nativeFont.getMapper().getNumGlyphs();
         this.disposer = new NativeStrikeDisposer(nativeFont, desc,
                                                  pScalerContext);
     }
     private boolean usingIntGlyphImages() {
         if (intGlyphImages != null) {
            return true;
        } else if (longAddresses) {
            return false;
        } else {
            int glyphLenArray = getMaxGlyph(pScalerContext);
            if (glyphLenArray < numGlyphs) {
                glyphLenArray = numGlyphs;
            }
            intGlyphImages = new int[glyphLenArray];
            this.disposer.intGlyphImages = intGlyphImages;
            return true;
        }
     }
     private long[] getLongGlyphImages() {
        if (longGlyphImages == null && longAddresses) {
            int glyphLenArray = getMaxGlyph(pScalerContext);
            if (glyphLenArray < numGlyphs) {
                glyphLenArray = numGlyphs;
            }
            longGlyphImages = new long[glyphLenArray];
            this.disposer.longGlyphImages = longGlyphImages;
        }
        return longGlyphImages;
     }
     NativeStrike(NativeFont nativeFont, FontStrikeDesc desc,
                  boolean nocache) {
         super(nativeFont, desc);
         this.nativeFont = nativeFont;
         int ptSize = (int)desc.glyphTx.getScaleY();
         double scale = desc.devTx.getScaleX(); 
         byte [] nameBytes = nativeFont.getPlatformNameBytes(ptSize);
         pScalerContext = createScalerContext(nameBytes, ptSize, scale);
         int numGlyphs = nativeFont.getMapper().getNumGlyphs();
     }
     StrikeMetrics getFontMetrics() {
         if (strikeMetrics == null) {
             if (pScalerContext != 0) {
                 strikeMetrics = nativeFont.getFontMetrics(pScalerContext);
             }
             if (strikeMetrics != null && fontTx != null) {
                 strikeMetrics.convertToUserSpace(fontTx);
             }
         }
         return strikeMetrics;
     }
     private native long createScalerContext(byte[] nameBytes,
                                             int ptSize, double scale);
     private native int getMaxGlyph(long pScalerContext);
     private native long createNullScalerContext();
     void getGlyphImagePtrs(int[] glyphCodes, long[] images,int  len) {
         for (int i=0; i<len; i++) {
             images[i] = getGlyphImagePtr(glyphCodes[i]);
         }
     }
     long getGlyphImagePtr(int glyphCode) {
         long glyphPtr;
         if (usingIntGlyphImages()) {
             if ((glyphPtr = intGlyphImages[glyphCode] & INTMASK) != 0L) {
                 return glyphPtr;
             } else {
                 glyphPtr = nativeFont.getGlyphImage(pScalerContext,glyphCode);
                 synchronized (this) {
                     if (intGlyphImages[glyphCode] == 0) {
                         intGlyphImages[glyphCode] = (int)glyphPtr;
                         return glyphPtr;
                     } else {
                         StrikeCache.freeIntPointer((int)glyphPtr);
                         return intGlyphImages[glyphCode] & INTMASK;
                     }
                 }
             }
         }
         else if ((glyphPtr = getLongGlyphImages()[glyphCode]) != 0L) {
             return glyphPtr;
         } else {
             glyphPtr = nativeFont.getGlyphImage(pScalerContext, glyphCode);
             synchronized (this) {
                 if (longGlyphImages[glyphCode] == 0L) {
                     longGlyphImages[glyphCode] = glyphPtr;
                     return glyphPtr;
                 } else {
                     StrikeCache.freeLongPointer(glyphPtr);
                     return longGlyphImages[glyphCode];
                 }
             }
         }
     }
     long getGlyphImagePtrNoCache(int glyphCode) {
         return nativeFont.getGlyphImageNoDefault(pScalerContext, glyphCode);
     }
     void getGlyphImageBounds(int glyphcode, Point2D.Float pt,
                              Rectangle result) {
     }
     Point2D.Float getGlyphMetrics(int glyphCode) {
         Point2D.Float pt = new Point2D.Float(getGlyphAdvance(glyphCode), 0f);
         return pt;
     }
     float getGlyphAdvance(int glyphCode) {
         return nativeFont.getGlyphAdvance(pScalerContext, glyphCode);
     }
     Rectangle2D.Float getGlyphOutlineBounds(int glyphCode) {
         return nativeFont.getGlyphOutlineBounds(pScalerContext, glyphCode);
     }
     GeneralPath getGlyphOutline(int glyphCode, float x, float y) {
         return new GeneralPath();
     }
     GeneralPath getGlyphVectorOutline(int[] glyphs, float x, float y) {
         return new GeneralPath();
     }
}
class DelegateStrike extends NativeStrike {
    private FontStrike delegateStrike;
    DelegateStrike(NativeFont nativeFont, FontStrikeDesc desc,
                   FontStrike delegate) {
        super(nativeFont, desc);
        this.delegateStrike = delegate;
    }
   StrikeMetrics getFontMetrics() {
       if (strikeMetrics == null) {
           if (pScalerContext != 0) {
               strikeMetrics = super.getFontMetrics();
           }
            if (strikeMetrics == null) {
                strikeMetrics = delegateStrike.getFontMetrics();
            }
        }
        return strikeMetrics;
    }
    void getGlyphImagePtrs(int[] glyphCodes, long[] images,int  len) {
        delegateStrike.getGlyphImagePtrs(glyphCodes, images, len);
    }
    long getGlyphImagePtr(int glyphCode) {
        return delegateStrike.getGlyphImagePtr(glyphCode);
    }
    void getGlyphImageBounds(int glyphCode,
                             Point2D.Float pt, Rectangle result) {
        delegateStrike.getGlyphImageBounds(glyphCode, pt, result);
    }
    Point2D.Float getGlyphMetrics(int glyphCode) {
        return delegateStrike.getGlyphMetrics(glyphCode);
    }
    float getGlyphAdvance(int glyphCode) {
        return delegateStrike.getGlyphAdvance(glyphCode);
    }
     Point2D.Float getCharMetrics(char ch) {
        return delegateStrike.getCharMetrics(ch);
    }
    float getCodePointAdvance(int cp) {
        if (cp < 0 || cp >= 0x10000) {
            cp = 0xffff;
        }
        return delegateStrike.getGlyphAdvance(cp);
    }
    Rectangle2D.Float getGlyphOutlineBounds(int glyphCode) {
        return delegateStrike.getGlyphOutlineBounds(glyphCode);
    }
    GeneralPath getGlyphOutline(int glyphCode, float x, float y) {
        return delegateStrike.getGlyphOutline(glyphCode, x, y);
    }
    GeneralPath getGlyphVectorOutline(int[] glyphs, float x, float y) {
        return delegateStrike.getGlyphVectorOutline(glyphs, x, y);
    }
}
