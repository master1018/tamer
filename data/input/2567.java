public final class GraphicComponent implements TextLineComponent,
                                               Decoration.Label {
    public static final float GRAPHIC_LEADING = 2;
    private GraphicAttribute graphic;
    private int graphicCount;
    private int[] charsLtoV;  
    private byte[] levels; 
    private Rectangle2D visualBounds = null;
    private float graphicAdvance;
    private AffineTransform baseTx;
    private CoreMetrics cm;
    private Decoration decorator;
    public GraphicComponent(GraphicAttribute graphic,
                            Decoration decorator,
                            int[] charsLtoV,
                            byte[] levels,
                            int start,
                            int limit,
                            AffineTransform baseTx) {
        if (limit <= start) {
            throw new IllegalArgumentException("0 or negative length in GraphicComponent");
        }
        this.graphic = graphic;
        this.graphicAdvance = graphic.getAdvance();
        this.decorator = decorator;
        this.cm = createCoreMetrics(graphic);
        this.baseTx = baseTx;
        initLocalOrdering(charsLtoV, levels, start, limit);
    }
    private GraphicComponent(GraphicComponent parent, int start, int limit, int dir) {
        this.graphic = parent.graphic;
        this.graphicAdvance = parent.graphicAdvance;
        this.decorator = parent.decorator;
        this.cm = parent.cm;
        this.baseTx = parent.baseTx;
        int[] charsLtoV = null;
        byte[] levels = null;
        if (dir == UNCHANGED) {
            charsLtoV = parent.charsLtoV;
            levels = parent.levels;
        }
        else if (dir == LEFT_TO_RIGHT || dir == RIGHT_TO_LEFT) {
            limit -= start;
            start = 0;
            if (dir == RIGHT_TO_LEFT) {
                charsLtoV = new int[limit];
                levels = new byte[limit];
                for (int i=0; i < limit; i++) {
                    charsLtoV[i] = limit-i-1;
                    levels[i] = (byte) 1;
                }
            }
        }
        else {
            throw new IllegalArgumentException("Invalid direction flag");
        }
        initLocalOrdering(charsLtoV, levels, start, limit);
    }
    private void initLocalOrdering(int[] charsLtoV,
                                   byte[] levels,
                                   int start,
                                   int limit) {
        this.graphicCount = limit - start; 
        if (charsLtoV == null || charsLtoV.length == graphicCount) {
            this.charsLtoV = charsLtoV;
        }
        else {
            this.charsLtoV = BidiUtils.createNormalizedMap(charsLtoV, levels, start, limit);
        }
        if (levels == null || levels.length == graphicCount) {
            this.levels = levels;
        }
        else {
            this.levels = new byte[graphicCount];
            System.arraycopy(levels, start, this.levels, 0, graphicCount);
        }
    }
    public boolean isSimple() {
        return false;
    }
    public Rectangle getPixelBounds(FontRenderContext frc, float x, float y) {
        throw new InternalError("do not call if isSimple returns false");
    }
    public Rectangle2D handleGetVisualBounds() {
        Rectangle2D bounds = graphic.getBounds();
        float width = (float) bounds.getWidth() +
                                 graphicAdvance * (graphicCount-1);
        return new Rectangle2D.Float((float) bounds.getX(),
                                     (float) bounds.getY(),
                                     width,
                                     (float) bounds.getHeight());
    }
    public CoreMetrics getCoreMetrics() {
        return cm;
    }
    public static CoreMetrics createCoreMetrics(GraphicAttribute graphic) {
        return new CoreMetrics(graphic.getAscent(),
                               graphic.getDescent(),
                               GRAPHIC_LEADING,
                               graphic.getAscent() + graphic.getDescent() + GRAPHIC_LEADING,
                               graphic.getAlignment(),
                               new float[] { 0, -graphic.getAscent() / 2, -graphic.getAscent() },
                               -graphic.getAscent() / 2,
                               graphic.getAscent() / 12,
                               graphic.getDescent() / 3,
                               graphic.getAscent() / 12,
                               0, 
                               0); 
    }
    public float getItalicAngle() {
        return 0;
    }
    public Rectangle2D getVisualBounds() {
        if (visualBounds == null) {
            visualBounds = decorator.getVisualBounds(this);
        }
        Rectangle2D.Float bounds = new Rectangle2D.Float();
        bounds.setRect(visualBounds);
        return bounds;
    }
    public Shape handleGetOutline(float x, float y) {
        double[] matrix = { 1, 0, 0, 1, x, y };
        if (graphicCount == 1) {
            AffineTransform tx = new AffineTransform(matrix);
            return graphic.getOutline(tx);
        }
        GeneralPath gp = new GeneralPath();
        for (int i = 0; i < graphicCount; ++i) {
            AffineTransform tx = new AffineTransform(matrix);
            gp.append(graphic.getOutline(tx), false);
            matrix[4] += graphicAdvance;
        }
        return gp;
    }
    public AffineTransform getBaselineTransform() {
        return baseTx;
    }
    public Shape getOutline(float x, float y) {
        return decorator.getOutline(this, x, y);
    }
    public void handleDraw(Graphics2D g2d, float x, float y) {
        for (int i=0; i < graphicCount; i++) {
            graphic.draw(g2d, x, y);
            x += graphicAdvance;
        }
    }
    public void draw(Graphics2D g2d, float x, float y) {
        decorator.drawTextAndDecorations(this, g2d, x, y);
    }
    public Rectangle2D getCharVisualBounds(int index) {
        return decorator.getCharVisualBounds(this, index);
    }
    public int getNumCharacters() {
        return graphicCount;
    }
    public float getCharX(int index) {
        int visIndex = charsLtoV==null? index : charsLtoV[index];
        return graphicAdvance * visIndex;
    }
    public float getCharY(int index) {
        return 0;
    }
    public float getCharAdvance(int index) {
        return graphicAdvance;
    }
    public boolean caretAtOffsetIsValid(int index) {
        return true;
    }
    public Rectangle2D handleGetCharVisualBounds(int index) {
        Rectangle2D bounds = graphic.getBounds();
        Rectangle2D.Float charBounds = new Rectangle2D.Float();
        charBounds.setRect(bounds);
        charBounds.x += graphicAdvance * index;
        return charBounds;
    }
    public int getLineBreakIndex(int start, float width) {
        int index = (int) (width / graphicAdvance);
        if (index > graphicCount - start) {
            index = graphicCount - start;
        }
        return index;
    }
    public float getAdvanceBetween(int start, int limit) {
        return graphicAdvance * (limit - start);
    }
    public Rectangle2D getLogicalBounds() {
        float left = 0;
        float top = -cm.ascent;
        float width = graphicAdvance * graphicCount;
        float height = cm.descent - top;
        return new Rectangle2D.Float(left, top, width, height);
    }
    public float getAdvance() {
        return graphicAdvance * graphicCount;
    }
    public Rectangle2D getItalicBounds() {
        return getLogicalBounds();
    }
    public TextLineComponent getSubset(int start, int limit, int dir) {
        if (start < 0 || limit > graphicCount || start >= limit) {
            throw new IllegalArgumentException("Invalid range.  start="
                                               +start+"; limit="+limit);
        }
        if (start == 0 && limit == graphicCount && dir == UNCHANGED) {
            return this;
        }
        return new GraphicComponent(this, start, limit, dir);
    }
    public String toString() {
        return "[graphic=" + graphic + ":count=" + getNumCharacters() + "]";
    }
  public int getNumJustificationInfos() {
    return 0;
  }
  public void getJustificationInfos(GlyphJustificationInfo[] infos, int infoStart, int charStart, int charLimit) {
  }
  public TextLineComponent applyJustificationDeltas(float[] deltas, int deltaStart, boolean[] flags) {
    return this;
  }
}
