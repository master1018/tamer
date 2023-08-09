public class JulesRenderingEngine extends PiscesRenderingEngine {
    @Override
    public AATileGenerator
         getAATileGenerator(Shape s, AffineTransform at, Region clip,
                            BasicStroke bs, boolean thin,
                            boolean normalize, int[] bbox) {
        if (JulesPathBuf.isCairoAvailable()) {
            return new JulesAATileGenerator(s, at, clip, bs, thin,
                                            normalize, bbox);
        } else {
            return super.getAATileGenerator(s, at, clip, bs, thin,
                                            normalize, bbox);
        }
    }
    public float getMinimumAAPenSize() {
        return 0.5f;
    }
}
