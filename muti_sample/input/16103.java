class ExtendedTextSourceLabel extends ExtendedTextLabel implements Decoration.Label {
  TextSource source;
  private Decoration decorator;
  private Font font;
  private AffineTransform baseTX;
  private CoreMetrics cm;
  Rectangle2D lb;
  Rectangle2D ab;
  Rectangle2D vb;
  Rectangle2D ib;
  StandardGlyphVector gv;
  float[] charinfo;
  public ExtendedTextSourceLabel(TextSource source, Decoration decorator) {
    this.source = source;
    this.decorator = decorator;
    finishInit();
  }
  public ExtendedTextSourceLabel(TextSource source, ExtendedTextSourceLabel oldLabel, int offset) {
    this.source = source;
    this.decorator = oldLabel.decorator;
    finishInit();
  }
  private void finishInit() {
    font = source.getFont();
    Map<TextAttribute, ?> atts = font.getAttributes();
    baseTX = AttributeValues.getBaselineTransform(atts);
    if (baseTX == null){
        cm = source.getCoreMetrics();
    } else {
      AffineTransform charTX = AttributeValues.getCharTransform(atts);
      if (charTX == null) {
          charTX = new AffineTransform();
      }
      font = font.deriveFont(charTX);
      LineMetrics lm = font.getLineMetrics(source.getChars(), source.getStart(),
          source.getStart() + source.getLength(), source.getFRC());
      cm = CoreMetrics.get(lm);
    }
  }
  public Rectangle2D getLogicalBounds() {
    return getLogicalBounds(0, 0);
  }
  public Rectangle2D getLogicalBounds(float x, float y) {
    if (lb == null) {
      lb = createLogicalBounds();
    }
    return new Rectangle2D.Float((float)(lb.getX() + x),
                                 (float)(lb.getY() + y),
                                 (float)lb.getWidth(),
                                 (float)lb.getHeight());
  }
    public float getAdvance() {
        if (lb == null) {
            lb = createLogicalBounds();
        }
        return (float)lb.getWidth();
    }
  public Rectangle2D getVisualBounds(float x, float y) {
    if (vb == null) {
      vb = decorator.getVisualBounds(this);
    }
    return new Rectangle2D.Float((float)(vb.getX() + x),
                                 (float)(vb.getY() + y),
                                 (float)vb.getWidth(),
                                 (float)vb.getHeight());
  }
  public Rectangle2D getAlignBounds(float x, float y) {
    if (ab == null) {
      ab = createAlignBounds();
    }
    return new Rectangle2D.Float((float)(ab.getX() + x),
                                 (float)(ab.getY() + y),
                                 (float)ab.getWidth(),
                                 (float)ab.getHeight());
  }
  public Rectangle2D getItalicBounds(float x, float y) {
    if (ib == null) {
      ib = createItalicBounds();
    }
    return new Rectangle2D.Float((float)(ib.getX() + x),
                                 (float)(ib.getY() + y),
                                 (float)ib.getWidth(),
                                 (float)ib.getHeight());
  }
  public Rectangle getPixelBounds(FontRenderContext frc, float x, float y) {
      return getGV().getPixelBounds(frc, x, y);
  }
  public boolean isSimple() {
      return decorator == Decoration.getPlainDecoration() &&
             baseTX == null;
  }
  public AffineTransform getBaselineTransform() {
      return baseTX; 
  }
  public Shape handleGetOutline(float x, float y) {
    return getGV().getOutline(x, y);
  }
  public Shape getOutline(float x, float y) {
    return decorator.getOutline(this, x, y);
  }
  public void handleDraw(Graphics2D g, float x, float y) {
    g.drawGlyphVector(getGV(), x, y);
  }
  public void draw(Graphics2D g, float x, float y) {
    decorator.drawTextAndDecorations(this, g, x, y);
  }
  protected Rectangle2D createLogicalBounds() {
    return getGV().getLogicalBounds();
  }
  public Rectangle2D handleGetVisualBounds() {
    return getGV().getVisualBounds();
  }
  protected Rectangle2D createAlignBounds() {
    float[] info = getCharinfo();
    float al = 0f;
    float at = -cm.ascent;
    float aw = 0f;
    float ah = cm.ascent + cm.descent;
    boolean lineIsLTR = (source.getLayoutFlags() & 0x8) == 0;
    int rn = info.length - numvals;
    if (lineIsLTR) {
      while (rn > 0 && info[rn+visw] == 0) {
        rn -= numvals;
      }
    }
    if (rn >= 0) {
      int ln = 0;
      while (ln < rn && ((info[ln+advx] == 0) || (!lineIsLTR && info[ln+visw] == 0))) {
        ln += numvals;
      }
      al = Math.max(0f, info[ln+posx]);
      aw = info[rn+posx] + info[rn+advx] - al;
    }
    return new Rectangle2D.Float(al, at, aw, ah);
  }
  public Rectangle2D createItalicBounds() {
    float ia = cm.italicAngle;
    Rectangle2D lb = getLogicalBounds();
    float l = (float)lb.getMinX();
    float t = -cm.ascent;
    float r = (float)lb.getMaxX();
    float b = cm.descent;
    if (ia != 0) {
        if (ia > 0) {
            l -= ia * (b - cm.ssOffset);
            r -= ia * (t - cm.ssOffset);
        } else {
            l -= ia * (t - cm.ssOffset);
            r -= ia * (b - cm.ssOffset);
        }
    }
    return new Rectangle2D.Float(l, t, r - l, b - t);
  }
  private final StandardGlyphVector getGV() {
    if (gv == null) {
      gv = createGV();
    }
    return gv;
  }
  protected StandardGlyphVector createGV() {
    FontRenderContext frc = source.getFRC();
    int flags = source.getLayoutFlags();
    char[] context = source.getChars();
    int start = source.getStart();
    int length = source.getLength();
    GlyphLayout gl = GlyphLayout.get(null); 
    gv = gl.layout(font, frc, context, start, length, flags, null); 
    GlyphLayout.done(gl);
    return gv;
  }
  private static final int posx = 0,
    posy = 1,
    advx = 2,
    advy = 3,
    visx = 4,
    visy = 5,
    visw = 6,
    vish = 7;
  private static final int numvals = 8;
  public int getNumCharacters() {
    return source.getLength();
  }
  public CoreMetrics getCoreMetrics() {
    return cm;
  }
  public float getCharX(int index) {
    validate(index);
    return getCharinfo()[l2v(index) * numvals + posx];
  }
  public float getCharY(int index) {
    validate(index);
    return getCharinfo()[l2v(index) * numvals + posy];
  }
  public float getCharAdvance(int index) {
    validate(index);
    return getCharinfo()[l2v(index) * numvals + advx];
  }
  public Rectangle2D handleGetCharVisualBounds(int index) {
    validate(index);
    float[] charinfo = getCharinfo();
    index = l2v(index) * numvals;
    return new Rectangle2D.Float(
                                 charinfo[index + visx],
                                 charinfo[index + visy],
                                 charinfo[index + visw],
                                 charinfo[index + vish]);
  }
  public Rectangle2D getCharVisualBounds(int index, float x, float y) {
    Rectangle2D bounds = decorator.getCharVisualBounds(this, index);
    if (x != 0 || y != 0) {
        bounds.setRect(bounds.getX()+x,
                       bounds.getY()+y,
                       bounds.getWidth(),
                       bounds.getHeight());
    }
    return bounds;
  }
  private void validate(int index) {
    if (index < 0) {
      throw new IllegalArgumentException("index " + index + " < 0");
    } else if (index >= source.getLength()) {
      throw new IllegalArgumentException("index " + index + " < " + source.getLength());
    }
  }
  public int logicalToVisual(int logicalIndex) {
    validate(logicalIndex);
    return l2v(logicalIndex);
  }
  public int visualToLogical(int visualIndex) {
    validate(visualIndex);
    return v2l(visualIndex);
  }
  public int getLineBreakIndex(int start, float width) {
    float[] charinfo = getCharinfo();
    int length = source.getLength();
    --start;
    while (width >= 0 && ++start < length) {
      float adv = charinfo[l2v(start) * numvals + advx];
      width -= adv;
    }
    return start;
  }
  public float getAdvanceBetween(int start, int limit) {
    float a = 0f;
    float[] charinfo = getCharinfo();
    --start;
    while (++start < limit) {
      a += charinfo[l2v(start) * numvals + advx];
    }
    return a;
  }
  public boolean caretAtOffsetIsValid(int offset) {
      if (offset == 0 || offset == source.getLength()) {
          return true;
      }
      char c = source.getChars()[source.getStart() + offset];
      if (c == '\t' || c == '\n' || c == '\r') { 
          return true;
      }
      int v = l2v(offset);
      return getCharinfo()[v * numvals + advx] != 0;
  }
  private final float[] getCharinfo() {
    if (charinfo == null) {
      charinfo = createCharinfo();
    }
    return charinfo;
  }
  protected float[] createCharinfo() {
    StandardGlyphVector gv = getGV();
    float[] glyphinfo = null;
    try {
        glyphinfo = gv.getGlyphInfo();
    }
    catch (Exception e) {
        System.out.println(source);
    }
    int numGlyphs = gv.getNumGlyphs();
    int[] indices = gv.getGlyphCharIndices(0, numGlyphs, null);
    boolean DEBUG = false;
    if (DEBUG) {
      System.err.println("number of glyphs: " + numGlyphs);
      for (int i = 0; i < numGlyphs; ++i) {
        System.err.println("g: " + i +
            ", x: " + glyphinfo[i*numvals+posx] +
            ", a: " + glyphinfo[i*numvals+advx] +
            ", n: " + indices[i]);
      }
    }
    int minIndex = indices[0];  
    int maxIndex = minIndex;    
    int nextMin = 0;            
    int cp = 0;                 
    int cx = 0;                 
    int gp = 0;                 
    int gx = 0;                 
    int gxlimit = numGlyphs;    
    int pdelta = numvals;       
    int xdelta = 1;             
    boolean ltr = (source.getLayoutFlags() & 0x1) == 0;
    if (!ltr) {
        minIndex = indices[numGlyphs - 1];
        maxIndex = minIndex;
        nextMin  = 0; 
        cp = glyphinfo.length - numvals;
        cx = 0; 
        gp = glyphinfo.length - numvals;
        gx = numGlyphs - 1;
        gxlimit = -1;
        pdelta = -numvals;
        xdelta = -1;
    }
    float cposl = 0, cposr = 0, cvisl = 0, cvist = 0, cvisr = 0, cvisb = 0;
    float baseline = 0;
    boolean mustCopy = false;
    while (gx != gxlimit) {
        boolean haveCopy = false;
        int clusterExtraGlyphs = 0;
        minIndex = indices[gx];
        maxIndex = minIndex;
        gx += xdelta;
        gp += pdelta;
        while (gx != gxlimit &&
               ((glyphinfo[gp + advx] == 0) ||
               (minIndex != nextMin) ||
               (indices[gx] <= maxIndex) ||
               (maxIndex - minIndex > clusterExtraGlyphs))) {
            if (!haveCopy) {
                int gps = gp - pdelta;
                cposl = glyphinfo[gps + posx];
                cposr = cposl + glyphinfo[gps + advx];
                cvisl = glyphinfo[gps + visx];
                cvist = glyphinfo[gps + visy];
                cvisr = cvisl + glyphinfo[gps + visw];
                cvisb = cvist + glyphinfo[gps + vish];
                haveCopy = true;
            }
            ++clusterExtraGlyphs;
            float radvx = glyphinfo[gp + advx];
            if (radvx != 0) {
                float rposx = glyphinfo[gp + posx];
                cposl = Math.min(cposl, rposx);
                cposr = Math.max(cposr, rposx + radvx);
            }
            float rvisw = glyphinfo[gp + visw];
            if (rvisw != 0) {
                float rvisx = glyphinfo[gp + visx];
                float rvisy = glyphinfo[gp + visy];
                cvisl = Math.min(cvisl, rvisx);
                cvist = Math.min(cvist, rvisy);
                cvisr = Math.max(cvisr, rvisx + rvisw);
                cvisb = Math.max(cvisb, rvisy + glyphinfo[gp + vish]);
            }
            minIndex = Math.min(minIndex, indices[gx]);
            maxIndex = Math.max(maxIndex, indices[gx]);
            gx += xdelta;
            gp += pdelta;
        }
        if (DEBUG) {
            System.out.println("minIndex = " + minIndex + ", maxIndex = " + maxIndex);
        }
        nextMin = maxIndex + 1;
        glyphinfo[cp + posy] = baseline;
        glyphinfo[cp + advy] = 0;
        if (haveCopy) {
            glyphinfo[cp + posx] = cposl;
            glyphinfo[cp + advx] = cposr - cposl;
            glyphinfo[cp + visx] = cvisl;
            glyphinfo[cp + visy] = cvist;
            glyphinfo[cp + visw] = cvisr - cvisl;
            glyphinfo[cp + vish] = cvisb - cvist;
            if (maxIndex - minIndex < clusterExtraGlyphs) {
                mustCopy = true;
            }
            if (minIndex < maxIndex) {
                if (!ltr) {
                    cposr = cposl;
                }
                cvisr -= cvisl; 
                cvisb -= cvist;
                int iMinIndex = minIndex, icp = cp / 8;
                while (minIndex < maxIndex) {
                    ++minIndex;
                    cx += xdelta;
                    cp += pdelta;
                    if (cp < 0 || cp >= glyphinfo.length) {
                        if (DEBUG) System.out.println("minIndex = " + iMinIndex + ", maxIndex = " + maxIndex + ", cp = " + icp);
                    }
                    glyphinfo[cp + posx] = cposr;
                    glyphinfo[cp + posy] = baseline;
                    glyphinfo[cp + advx] = 0;
                    glyphinfo[cp + advy] = 0;
                    glyphinfo[cp + visx] = cvisl;
                    glyphinfo[cp + visy] = cvist;
                    glyphinfo[cp + visw] = cvisr;
                    glyphinfo[cp + vish] = cvisb;
                }
            }
            haveCopy = false;
        } else if (mustCopy) {
            int gpr = gp - pdelta;
            glyphinfo[cp + posx] = glyphinfo[gpr + posx];
            glyphinfo[cp + advx] = glyphinfo[gpr + advx];
            glyphinfo[cp + visx] = glyphinfo[gpr + visx];
            glyphinfo[cp + visy] = glyphinfo[gpr + visy];
            glyphinfo[cp + visw] = glyphinfo[gpr + visw];
            glyphinfo[cp + vish] = glyphinfo[gpr + vish];
        }
        cp += pdelta;
        cx += xdelta;
    }
    if (mustCopy && !ltr) {
        cp -= pdelta; 
        System.arraycopy(glyphinfo, cp, glyphinfo, 0, glyphinfo.length - cp);
    }
    if (DEBUG) {
      char[] chars = source.getChars();
      int start = source.getStart();
      int length = source.getLength();
      System.out.println("char info for " + length + " characters");
      for(int i = 0; i < length * numvals;) {
        System.out.println(" ch: " + Integer.toHexString(chars[start + v2l(i / numvals)]) +
                           " x: " + glyphinfo[i++] +
                           " y: " + glyphinfo[i++] +
                           " xa: " + glyphinfo[i++] +
                           " ya: " + glyphinfo[i++] +
                           " l: " + glyphinfo[i++] +
                           " t: " + glyphinfo[i++] +
                           " w: " + glyphinfo[i++] +
                           " h: " + glyphinfo[i++]);
      }
    }
    return glyphinfo;
  }
  protected int l2v(int index) {
    return (source.getLayoutFlags() & 0x1) == 0 ? index : source.getLength() - 1 - index;
  }
  protected int v2l(int index) {
    return (source.getLayoutFlags() & 0x1) == 0 ? index : source.getLength() - 1 - index;
  }
  public TextLineComponent getSubset(int start, int limit, int dir) {
    return new ExtendedTextSourceLabel(source.getSubSource(start, limit-start, dir), decorator);
  }
  public String toString() {
    if (true) {
        return source.toString(source.WITHOUT_CONTEXT);
    }
    StringBuffer buf = new StringBuffer();
    buf.append(super.toString());
    buf.append("[source:");
    buf.append(source.toString(source.WITHOUT_CONTEXT));
    buf.append(", lb:");
    buf.append(lb);
    buf.append(", ab:");
    buf.append(ab);
    buf.append(", vb:");
    buf.append(vb);
    buf.append(", gv:");
    buf.append(gv);
    buf.append(", ci: ");
    if (charinfo == null) {
      buf.append("null");
    } else {
      buf.append(charinfo[0]);
      for (int i = 1; i < charinfo.length;) {
        buf.append(i % numvals == 0 ? "; " : ", ");
        buf.append(charinfo[i]);
      }
    }
    buf.append("]");
    return buf.toString();
  }
  public int getNumJustificationInfos() {
    return getGV().getNumGlyphs();
  }
  public void getJustificationInfos(GlyphJustificationInfo[] infos, int infoStart, int charStart, int charLimit) {
    StandardGlyphVector gv = getGV();
    float[] charinfo = getCharinfo();
    float size = gv.getFont().getSize2D();
    GlyphJustificationInfo nullInfo =
      new GlyphJustificationInfo(0,
                                 false, GlyphJustificationInfo.PRIORITY_NONE, 0, 0,
                                 false, GlyphJustificationInfo.PRIORITY_NONE, 0, 0);
    GlyphJustificationInfo spaceInfo =
      new GlyphJustificationInfo(size,
                                 true, GlyphJustificationInfo.PRIORITY_WHITESPACE, 0, size,
                                 true, GlyphJustificationInfo.PRIORITY_WHITESPACE, 0, size / 4f);
    GlyphJustificationInfo kanjiInfo =
      new GlyphJustificationInfo(size,
                                 true, GlyphJustificationInfo.PRIORITY_INTERCHAR, size, size,
                                 false, GlyphJustificationInfo.PRIORITY_NONE, 0, 0);
    char[] chars = source.getChars();
    int offset = source.getStart();
    int numGlyphs = gv.getNumGlyphs();
    int minGlyph = 0;
    int maxGlyph = numGlyphs;
    boolean ltr = (source.getLayoutFlags() & 0x1) == 0;
    if (charStart != 0 || charLimit != source.getLength()) {
      if (ltr) {
        minGlyph = charStart;
        maxGlyph = charLimit;
      } else {
        minGlyph = numGlyphs - charLimit;
        maxGlyph = numGlyphs - charStart;
      }
    }
    for (int i = 0; i < numGlyphs; ++i) {
      GlyphJustificationInfo info = null;
      if (i >= minGlyph && i < maxGlyph) {
        if (charinfo[i * numvals + advx] == 0) { 
          info = nullInfo;
        } else {
          int ci = v2l(i); 
          char c = chars[offset + ci];
          if (Character.isWhitespace(c)) {
            info = spaceInfo;
          } else if (c >= 0x4e00 &&
                     (c < 0xa000) ||
                     (c >= 0xac00 && c < 0xd7b0) ||
                     (c >= 0xf900 && c < 0xfb00)) {
            info = kanjiInfo;
          } else {
            info = nullInfo;
          }
        }
      }
      infos[infoStart + i] = info;
    }
  }
  public TextLineComponent applyJustificationDeltas(float[] deltas, int deltaStart, boolean[] flags) {
    float[] newCharinfo = (float[])getCharinfo().clone();
    flags[0] = false;
    StandardGlyphVector newgv = (StandardGlyphVector)getGV().clone();
    float[] newPositions = newgv.getGlyphPositions(null);
    int numGlyphs = newgv.getNumGlyphs();
    char[] chars = source.getChars();
    int offset = source.getStart();
    float deltaPos = 0;
    for (int i = 0; i < numGlyphs; ++i) {
      if (Character.isWhitespace(chars[offset + v2l(i)])) {
        newPositions[i*2] += deltaPos;
        float deltaAdv = deltas[deltaStart + i*2] + deltas[deltaStart + i*2 + 1];
        newCharinfo[i * numvals + posx] += deltaPos;
        newCharinfo[i * numvals + visx] += deltaPos;
        newCharinfo[i * numvals + advx] += deltaAdv;
        deltaPos += deltaAdv;
      } else {
        deltaPos += deltas[deltaStart + i*2];
        newPositions[i*2] += deltaPos;
        newCharinfo[i * numvals + posx] += deltaPos;
        newCharinfo[i * numvals + visx] += deltaPos;
        deltaPos += deltas[deltaStart + i*2 + 1];
      }
    }
    newPositions[numGlyphs * 2] += deltaPos;
    newgv.setGlyphPositions(newPositions);
    ExtendedTextSourceLabel result = new ExtendedTextSourceLabel(source, decorator);
    result.gv = newgv;
    result.charinfo = newCharinfo;
    return result;
  }
}
