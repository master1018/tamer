public class RenderContext implements Cloneable {
    AffineTransform transform;
    Shape aoi;
    RenderingHints hints;
    public RenderContext(AffineTransform usr2dev, Shape aoi, RenderingHints hints) {
        this.transform = (AffineTransform)usr2dev.clone();
        this.aoi = aoi;
        this.hints = hints;
    }
    public RenderContext(AffineTransform usr2dev, Shape aoi) {
        this(usr2dev, aoi, null);
    }
    public RenderContext(AffineTransform usr2dev, RenderingHints hints) {
        this(usr2dev, null, hints);
    }
    public RenderContext(AffineTransform usr2dev) {
        this(usr2dev, null, null);
    }
    @Override
    public Object clone() {
        return new RenderContext(transform, aoi, hints);
    }
    public void setTransform(AffineTransform newTransform) {
        transform = (AffineTransform)newTransform.clone();
    }
    @Deprecated
    public void preConcetenateTransform(AffineTransform modTransform) {
        preConcatenateTransform(modTransform);
    }
    public void preConcatenateTransform(AffineTransform modTransform) {
        transform.preConcatenate(modTransform);
    }
    @Deprecated
    public void concetenateTransform(AffineTransform modTransform) {
        concatenateTransform(modTransform);
    }
    public void concatenateTransform(AffineTransform modTransform) {
        transform.concatenate(modTransform);
    }
    public AffineTransform getTransform() {
        return (AffineTransform)transform.clone();
    }
    public void setAreaOfInterest(Shape newAoi) {
        aoi = newAoi;
    }
    public Shape getAreaOfInterest() {
        return aoi;
    }
    public void setRenderingHints(RenderingHints hints) {
        this.hints = hints;
    }
    public RenderingHints getRenderingHints() {
        return hints;
    }
}
