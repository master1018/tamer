public class NewModelStandardTransformBooleanBooleanInt {
    public static void main(String[] args) throws Exception {
        ModelStandardTransform transform = new ModelStandardTransform(
                ModelStandardTransform.DIRECTION_MAX2MIN,
                ModelStandardTransform.POLARITY_BIPOLAR,
                ModelStandardTransform.TRANSFORM_CONVEX);
        if(transform.getDirection() != ModelStandardTransform.DIRECTION_MAX2MIN)
            throw new RuntimeException("transform.getDirection() doesn't return ModelStandardTransform.DIRECTION_MAX2MIN!");
        if(transform.getPolarity() != ModelStandardTransform.POLARITY_BIPOLAR)
            throw new RuntimeException("transform.getPolarity() doesn't return ModelStandardTransform.POLARITY_BIPOLAR!");
        if(transform.getTransform() != ModelStandardTransform.TRANSFORM_CONVEX)
            throw new RuntimeException("transform.getTransform() doesn't return ModelStandardTransform.TRANSFORM_CONVEX!");
    }
}
