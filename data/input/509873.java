public class FontRenderContext {
    private AffineTransform transform;
    private boolean fAntiAliased;
    private boolean fFractionalMetrics;
    public FontRenderContext(AffineTransform trans, boolean antiAliased, 
            boolean usesFractionalMetrics) {
        if (trans != null){
            transform = new AffineTransform(trans);
        }
        fAntiAliased = antiAliased;
        fFractionalMetrics = usesFractionalMetrics;
    }
    protected FontRenderContext() {
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null) {
            try {
                return equals((FontRenderContext) obj);
            } catch (ClassCastException e) {
                return false;
            }
        }
        return false;
    }
    public AffineTransform getTransform() {
        if (transform != null){
            return new AffineTransform(transform);
        }
        return new AffineTransform();
    }
    public boolean equals(FontRenderContext frc) {
        if (this == frc){
            return true;
        }
        if (frc == null){
            return false;
        }
        if (!frc.getTransform().equals(this.getTransform()) &&
            !frc.isAntiAliased() == this.fAntiAliased &&
            !frc.usesFractionalMetrics() == this.fFractionalMetrics){
            return false;
        }
        return true;
    }
    public boolean usesFractionalMetrics() {
        return this.fFractionalMetrics;
    }
    public boolean isAntiAliased() {
        return this.fAntiAliased;
    }
    @Override
    public int hashCode() {
        return this.getTransform().hashCode() ^
                new Boolean(this.fFractionalMetrics).hashCode() ^
                new Boolean(this.fAntiAliased).hashCode();
    }
}
