public class TransformSwitch {
    private static void assertTrue(boolean value) throws Exception
    {
        if(!value)
            throw new RuntimeException("assertTrue fails!");
    }
    public static void main(String[] args) throws Exception {
        ModelStandardTransform transform = new ModelStandardTransform();
        transform.setTransform(ModelStandardTransform.TRANSFORM_SWITCH);
        transform.setDirection(ModelStandardTransform.DIRECTION_MIN2MAX);
        transform.setPolarity(ModelStandardTransform.POLARITY_UNIPOLAR);
        assertTrue(Math.abs(transform.transform(0.2f) - 0.0f) < 0.0001f);
        assertTrue(Math.abs(transform.transform(0.8f) - 1.0f) < 0.0001f);
        transform.setDirection(ModelStandardTransform.DIRECTION_MAX2MIN);
        transform.setPolarity(ModelStandardTransform.POLARITY_UNIPOLAR);
        assertTrue(Math.abs(transform.transform(0.2f) - 1.0f) < 0.0001f);
        assertTrue(Math.abs(transform.transform(0.8f) - 0.0f) < 0.0001f);
        transform.setDirection(ModelStandardTransform.DIRECTION_MIN2MAX);
        transform.setPolarity(ModelStandardTransform.POLARITY_BIPOLAR);
        assertTrue(Math.abs(transform.transform(0.2f) + 1.0f) < 0.0001f);
        assertTrue(Math.abs(transform.transform(0.8f) - 1.0f) < 0.0001f);
        transform.setDirection(ModelStandardTransform.DIRECTION_MAX2MIN);
        transform.setPolarity(ModelStandardTransform.POLARITY_BIPOLAR);
        assertTrue(Math.abs(transform.transform(0.2f) - 1.0f) < 0.0001f);
        assertTrue(Math.abs(transform.transform(0.8f) + 1.0f) < 0.0001f);
    }
}
