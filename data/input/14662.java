public class NewModelStandardTransformBoolean {
    public static void main(String[] args) throws Exception {
        ModelStandardTransform transform = new ModelStandardTransform(ModelStandardTransform.DIRECTION_MAX2MIN);
        if(transform.getDirection() != ModelStandardTransform.DIRECTION_MAX2MIN)
            throw new RuntimeException("transform.getDirection() doesn't return ModelStandardTransform.DIRECTION_MAX2MIN!");
        if(transform.getPolarity() != ModelStandardTransform.POLARITY_UNIPOLAR)
            throw new RuntimeException("transform.getPolarity() doesn't return ModelStandardTransform.POLARITY_UNIPOLAR!");
        if(transform.getTransform() != ModelStandardTransform.TRANSFORM_LINEAR)
            throw new RuntimeException("transform.getTransform() doesn't return ModelStandardTransform.TRANSFORM_LINEAR!");
    }
}
