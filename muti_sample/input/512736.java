public class CommonGlyphVector extends GlyphVector {
    protected AffineTransform[] glsTransforms;
    public char[] charVector;
    public Glyph[] vector;
    float[] defaultPositions;
    float[] logicalPositions;
    public float[] visualPositions;
    protected FontRenderContext vectorFRC;
    protected int layoutFlags = 0;
    protected Shape[] gvShapes;
    FontPeerImpl peer;
    Font font;
    float ascent;
    float height;
    float leading;
    float descent;
    AffineTransform transform;
    @SuppressWarnings("deprecation")
    public CommonGlyphVector(char[] chars, FontRenderContext frc, Font fnt,
            int flags) {
        int len = chars.length;
        this.font = fnt;
        this.transform = fnt.getTransform();
        this.peer = (FontPeerImpl) fnt.getPeer();
        gvShapes = new Shape[len];
        logicalPositions = new float[(len+1)<<1];
        visualPositions = new float[(len+1)<<1];
        defaultPositions = new float[(len+1)<<1];
        glsTransforms = new AffineTransform[len];
        this.charVector = chars;
        this.vectorFRC = frc;
        LineMetricsImpl lmImpl = (LineMetricsImpl)fnt.getLineMetrics(String.valueOf(chars), frc);
        this.ascent = lmImpl.getAscent();
        this.height = lmImpl.getHeight();
        this.leading = lmImpl.getLeading();
        this.descent = lmImpl.getDescent();
        this.layoutFlags = flags;
        if ((flags & Font.LAYOUT_RIGHT_TO_LEFT) != 0){
            char vector[] = new char[len];
            for(int i=0; i < len; i++){
                vector[i] = chars[len-i-1];
            }
            this.vector = peer.getGlyphs(vector);
        } else {
            this.vector = peer.getGlyphs(chars);
        }
        this.glsTransforms = new AffineTransform[len];
        setDefaultPositions();
        performDefaultLayout();
    }
    public CommonGlyphVector(char[] chars, FontRenderContext frc, Font fnt) {
        this(chars, frc, fnt, 0);
    }
    public CommonGlyphVector(String str, FontRenderContext frc, Font fnt) {
        this(str.toCharArray(), frc, fnt, 0);
    }
    public CommonGlyphVector(String str, FontRenderContext frc, Font fnt, int flags) {
        this(str.toCharArray(), frc, fnt, flags);
    }
    void setDefaultPositions(){
        int len = getNumGlyphs();
        for (int i=1; i <= len; i++ ){
                int idx = i << 1;
                float advanceX = vector[i-1].getGlyphPointMetrics().getAdvanceX();
                float advanceY = vector[i-1].getGlyphPointMetrics().getAdvanceY();
                defaultPositions[idx] = defaultPositions[idx-2] + advanceX;
                defaultPositions[idx+1] = defaultPositions[idx-1] + advanceY;
        }
        transform.transform(defaultPositions, 0, logicalPositions, 0, getNumGlyphs()+1);
    }
    @Override
    public Rectangle getPixelBounds(FontRenderContext frc, float x, float y) {
        double xM, yM, xm, ym;
        double minX = 0;
        double minY = 0;
        double maxX = 0;
        double maxY = 0;
        for (int i = 0; i < this.getNumGlyphs(); i++) {
            Rectangle glyphBounds = this.getGlyphPixelBounds(i, frc, 0, 0);
            xm = glyphBounds.getMinX();
            ym = glyphBounds.getMinY();
            xM = glyphBounds.getMaxX();
            yM = glyphBounds.getMaxY();
            if (i == 0) {
                minX = xm;
                minY = ym;
                maxX = xM;
                maxY = yM;
            }
            if (minX > xm) {
                minX = xm;
            }
            if (minY > ym) {
                minY = ym;
            }
            if (maxX < xM) {
                maxX = xM;
            }
            if (maxY < yM) {
                maxY = yM;
            }
        }
        return new Rectangle((int)(minX + x), (int)(minY + y), (int)(maxX - minX), (int)(maxY - minY));
    }
    @Override
    public Rectangle2D getVisualBounds() {
        float xM, yM, xm, ym;
        float minX = 0;
        float minY = 0;
        float maxX = 0;
        float maxY = 0;
        boolean firstIteration = true;
        for (int i = 0; i < this.getNumGlyphs(); i++) {
            Rectangle2D bounds = this.getGlyphVisualBounds(i).getBounds2D();
            if (bounds.getWidth() == 0){
                continue;
            }
            xm = (float)bounds.getX();
            ym = (float)bounds.getY();
            xM = (float)(xm + bounds.getWidth());
            yM = ym + (float) bounds.getHeight();
            if (firstIteration) {
                minX = xm;
                minY = ym;
                maxX = xM;
                maxY = yM;
                firstIteration = false;
            } else {
                if (minX > xm) {
                    minX = xm;
                }
                if (minY > ym) {
                    minY = ym;
                }
                if (maxX < xM) {
                    maxX = xM;
                }
                if (maxY < yM) {
                    maxY = yM;
                }
            }
        }
        return (this.getNumGlyphs() != 0) ? new Rectangle2D.Float(minX, minY,
                (maxX - minX), (maxY - minY)) : null;
    }
    @Override
    public void setGlyphPosition(int glyphIndex, Point2D newPos) {
        if ((glyphIndex > vector.length) || (glyphIndex < 0)) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.43")); 
        }
        float x = (float)newPos.getX();
        float y = (float)newPos.getY();
        int index = glyphIndex << 1;
        if ((x != visualPositions[index]) || (y != visualPositions[index + 1])){
            visualPositions[index] = x;
            visualPositions[index+1] = y;
            layoutFlags = layoutFlags | FLAG_HAS_POSITION_ADJUSTMENTS;
        }
    }
    @Override
    public Point2D getGlyphPosition(int glyphIndex) {
        if ((glyphIndex > vector.length) || (glyphIndex < 0)) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.43")); 
        }
        int index = glyphIndex << 1;
        Point2D pos = new Point2D.Float(visualPositions[index], visualPositions[index+1]);
        if(glyphIndex==vector.length){
            return pos;
        }
        AffineTransform at = getGlyphTransform(glyphIndex);
        if ((at == null) || (at.isIdentity())){
            return pos;
        }
        pos.setLocation(pos.getX() + at.getTranslateX(), pos.getY() + at.getTranslateY());
        return pos;
    }
    @Override
    public void setGlyphTransform(int glyphIndex, AffineTransform trans) {
        if ((glyphIndex >= vector.length) || (glyphIndex < 0)) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.43")); 
        }
        if ((trans == null) || (trans.isIdentity())) {
            glsTransforms[glyphIndex] = null;
        } else {
            glsTransforms[glyphIndex] = new AffineTransform(trans);
            layoutFlags = layoutFlags | FLAG_HAS_TRANSFORMS;
        }
    }
    @Override
    public AffineTransform getGlyphTransform(int glyphIndex) {
        if ((glyphIndex >= this.vector.length) || (glyphIndex < 0)) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.43")); 
        }
        return this.glsTransforms[glyphIndex];
    }
    @Override
    public GlyphMetrics getGlyphMetrics(int glyphIndex) {
        if ((glyphIndex < 0) || ((glyphIndex) >= this.getNumGlyphs())) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.43")); 
        }
        return this.vector[glyphIndex].getGlyphMetrics();
    }
    @Override
    public GlyphJustificationInfo getGlyphJustificationInfo(int glyphIndex) {
        if (true) {
            throw new RuntimeException("Method is not implemented"); 
        }
        return null;
    }
    @Override
    public FontRenderContext getFontRenderContext() {
        return this.vectorFRC;
    }
    @Override
    public Shape getGlyphVisualBounds(int glyphIndex) {
        if ((glyphIndex < 0) || (glyphIndex >= this.getNumGlyphs())) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.43")); 
        }
        int idx  = glyphIndex << 1;
        AffineTransform fontTransform = this.transform;
        double xOffs = fontTransform.getTranslateX();
        double yOffs = fontTransform.getTranslateY();
        if (vector[glyphIndex].getWidth() == 0){
            return new Rectangle2D.Float((float)xOffs, (float)yOffs, 0, 0);
        }
        AffineTransform at = AffineTransform.getTranslateInstance(xOffs, yOffs);
        AffineTransform glyphTransform = getGlyphTransform(glyphIndex);
        if (transform.isIdentity() && ((glyphTransform == null) || glyphTransform.isIdentity())){
            Rectangle2D blackBox = vector[glyphIndex].getGlyphMetrics().getBounds2D();
            at.translate(visualPositions[idx], visualPositions[idx+1]);
            return(at.createTransformedShape(blackBox));
        }
        GeneralPath shape = (GeneralPath)this.getGlyphOutline(glyphIndex);
        shape.transform(at);
        return shape.getBounds2D();
    }
    @Override
    public Rectangle getGlyphPixelBounds(int glyphIndex, FontRenderContext frc,
            float x, float y) {
        if ((glyphIndex < 0) || (glyphIndex >= this.getNumGlyphs())) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.43")); 
        }
        int idx  = glyphIndex << 1;
        if (vector[glyphIndex].getWidth() == 0){
            AffineTransform fontTransform = this.transform;
            double xOffs = x + visualPositions[idx] + fontTransform.getTranslateX();
            double yOffs = y + visualPositions[idx+1] + fontTransform.getTranslateY();
            return new Rectangle((int)xOffs, (int)yOffs, 0, 0);
        }
        GeneralPath shape = (GeneralPath)this.getGlyphOutline(glyphIndex);
        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        if (frc != null){
            at.concatenate(frc.getTransform());
        }
        shape.transform(at);
        Rectangle bounds = shape.getBounds();
        return new Rectangle((int)bounds.getX(), (int)bounds.getY(),
                            (int)bounds.getWidth()-1, (int)bounds.getHeight()-1);
        }
    @Override
    public Shape getGlyphOutline(int glyphIndex) {
        if ((glyphIndex < 0) || (glyphIndex >= this.getNumGlyphs())) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.43")); 
        }
        if (gvShapes[glyphIndex] == null) {
            gvShapes[glyphIndex] = vector[glyphIndex].getShape();
        }
        GeneralPath gp = (GeneralPath)((GeneralPath)gvShapes[glyphIndex]).clone();
        AffineTransform at = (AffineTransform)this.transform.clone();
        AffineTransform glyphAT = getGlyphTransform(glyphIndex);
        if (glyphAT != null){
            at.preConcatenate(glyphAT);
        }
        int idx  = glyphIndex << 1;
        gp.transform(at);
        gp.transform(AffineTransform.getTranslateInstance(visualPositions[idx], visualPositions[idx+1]));
        return gp;
    }
    @Override
    public Shape getOutline(float x, float y) {
        GeneralPath gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        for (int i = 0; i < this.vector.length; i++) {
            GeneralPath outline = (GeneralPath)getGlyphOutline(i);
            outline.transform(AffineTransform.getTranslateInstance(x, y));
            gp.append(outline, false);
        }
        return gp;
    }
    @Override
    public Shape getOutline() {
        return this.getOutline(0, 0);
    }
    @Override
    public int[] getGlyphCodes(int beginGlyphIndex, int numEntries,
            int[] codeReturn) {
        if ((beginGlyphIndex < 0) || ((numEntries + beginGlyphIndex) > this.getNumGlyphs())) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.44")); 
        }
        if (numEntries < 0) {
            throw new IllegalArgumentException(Messages.getString("awt.45")); 
        }
        if (codeReturn == null) {
            codeReturn = new int[numEntries];
        }
        for (int i = beginGlyphIndex; i < beginGlyphIndex + numEntries; i++) {
            codeReturn[i-beginGlyphIndex] = this.vector[i].getGlyphCode();
        }
        return codeReturn;
    }
    @Override
    public int[] getGlyphCharIndices(int beginGlyphIndex, int numEntries,
            int[] codeReturn) {
        if ((beginGlyphIndex < 0) || (beginGlyphIndex >= this.getNumGlyphs())) {
            throw new IllegalArgumentException(Messages.getString("awt.44")); 
        }
        if ((numEntries < 0)
                || ((numEntries + beginGlyphIndex) > this.getNumGlyphs())) {
            throw new IllegalArgumentException(Messages.getString("awt.45")); 
        }
        if (codeReturn == null) {
            codeReturn = new int[numEntries];
        }
        for (int i = 0; i < numEntries; i++) {
            codeReturn[i] = this.getGlyphCharIndex(i + beginGlyphIndex);
        }
        return codeReturn;
    }
    @Override
    public float[] getGlyphPositions(int beginGlyphIndex, int numEntries,
            float[] positionReturn) {
        int len = (this.getNumGlyphs()+1) << 1;
        beginGlyphIndex *= 2;
        numEntries *= 2;
        if ((beginGlyphIndex < 0) || ((numEntries + beginGlyphIndex) > len)) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.44")); 
        }
        if (numEntries < 0) {
            throw new IllegalArgumentException(Messages.getString("awt.45")); 
        }
        if (positionReturn == null) {
            positionReturn = new float[numEntries];
        }
        System.arraycopy(visualPositions, beginGlyphIndex, positionReturn, 0, numEntries);
        return positionReturn;
    }
    public void setGlyphPositions(int beginGlyphIndex, int numEntries,
            float[] setPositions) {
        int len = (this.getNumGlyphs()+1) << 1;
        beginGlyphIndex *= 2;
        numEntries *= 2;
        if ((beginGlyphIndex < 0) || ((numEntries + beginGlyphIndex) > len)) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.44")); 
        }
        if (numEntries < 0) {
            throw new IllegalArgumentException(Messages.getString("awt.45")); 
        }
        System.arraycopy(setPositions, 0, visualPositions, beginGlyphIndex, numEntries);
        layoutFlags = layoutFlags & FLAG_HAS_POSITION_ADJUSTMENTS;
    }
    public void setGlyphPositions(float[] setPositions) {
        int len = (this.getNumGlyphs()+1) << 1;
        if (len != setPositions.length){
            throw new IllegalArgumentException(Messages.getString("awt.46")); 
        }
        System.arraycopy(setPositions, 0, visualPositions, 0, len);
        layoutFlags = layoutFlags & FLAG_HAS_POSITION_ADJUSTMENTS;
    }
    @Override
    public int getGlyphCode(int glyphIndex) {
        if (glyphIndex >= this.vector.length || glyphIndex < 0) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.43")); 
        }
        return this.vector[glyphIndex].getGlyphCode();
    }
    @Override
    public int getGlyphCharIndex(int glyphIndex) {
        if ((glyphIndex < 0) || (glyphIndex >= this.getNumGlyphs())) {
            throw new IllegalArgumentException(Messages.getString("awt.43")); 
        }
        if ((this.layoutFlags & Font.LAYOUT_RIGHT_TO_LEFT) != 0) {
            return this.charVector.length - glyphIndex - 1;
        }
        return glyphIndex;
    }
    public char getGlyphChar(int glyphIndex) {
        if ((glyphIndex < 0) || (glyphIndex >= this.getNumGlyphs())) {
            throw new IllegalArgumentException(Messages.getString("awt.43")); 
        }
        return this.charVector[glyphIndex];
    }
    @Override
    public void performDefaultLayout() {
        System.arraycopy(logicalPositions, 0, visualPositions, 0, logicalPositions.length);
        clearLayoutFlags(GlyphVector.FLAG_HAS_POSITION_ADJUSTMENTS);
    }
    @Override
    public int getNumGlyphs() {
        return vector.length;
    }
    @Override
    public Rectangle2D getLogicalBounds(){
        float x = visualPositions[0];
        float width = visualPositions[visualPositions.length-2];
        double scaleY =  transform.getScaleY();
        Rectangle2D bounds = new Rectangle2D.Float(x, (float)((-this.ascent-this.leading)*scaleY), width, (float)(this.height*scaleY));
        return bounds;
    }
    @Override
    public boolean equals(GlyphVector glyphVector){
        if (glyphVector == this){
            return true;
        }
        if (glyphVector != null) {
            if (!(glyphVector.getFontRenderContext().equals(this.vectorFRC) &&
                      glyphVector.getFont().equals(this.font))){
                return false;
            }
            try {
                boolean eq = true;
                for (int i = 0; i < getNumGlyphs(); i++) {
                    int idx = i*2;
                    eq = (((CommonGlyphVector)glyphVector).visualPositions[idx] == this.visualPositions[idx]) &&
                        (((CommonGlyphVector)glyphVector).visualPositions[idx+1] == this.visualPositions[idx+1]) &&
                        (glyphVector.getGlyphCharIndex(i) == this.getGlyphCharIndex(i));
                    if (eq){
                        AffineTransform trans = glyphVector.getGlyphTransform(i);
                        if (trans == null){
                            eq = (this.glsTransforms[i] == null);
                        }else{
                            eq = this.glsTransforms[i].equals(trans);
                        }
                    }
                    if (!eq){
                        return false;
                    }
                }
                return  eq;
            } catch (ClassCastException e) {
            }
        }
        return false;
    }
    @Override
    public int getLayoutFlags() {
        return layoutFlags;
    }
    public char getChar(int index) {
        return this.charVector[index];
    }
    private void clearLayoutFlags(int clearFlags){
        layoutFlags &= ~clearFlags;
    }
    @Override
    public Shape getGlyphLogicalBounds(int glyphIndex){
        if ((glyphIndex < 0) || (glyphIndex >= this.getNumGlyphs())){
            throw new IndexOutOfBoundsException(Messages.getString("awt.43")); 
        }
        Glyph glyph = this.vector[glyphIndex];
        float x0 = visualPositions[glyphIndex*2];
        float y0 = visualPositions[glyphIndex*2+1];
        float advanceX = glyph.getGlyphPointMetrics().getAdvanceX();
        GeneralPath gp = new GeneralPath();
        gp.moveTo(0, -ascent - leading);
        gp.lineTo(advanceX ,-ascent - leading);
        gp.lineTo(advanceX, descent);
        gp.lineTo(0, descent);
        gp.lineTo(0, -ascent - leading);
        gp.closePath();
        AffineTransform at = (AffineTransform)this.transform.clone();
        AffineTransform glyphTransform = getGlyphTransform(glyphIndex);
        if (glyphTransform != null){
            at.concatenate(glyphTransform);
        }
        at.preConcatenate(AffineTransform.getTranslateInstance(x0, y0));
        gp.transform(at);
        return gp;
    }
    @Override
    public Font getFont(){
        return this.font;
    }
}