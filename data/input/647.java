public class TransformLinear {
    private static boolean checkLinearity(ModelStandardTransform transform)
    {
        double lastx = 0;
        for (int p = 0; p < 2; p++)
        for (int d = 0; d < 2; d++)
        for (double i = 0; i < 1.0; i+=0.001) {
            if(p == 0)
                transform.setPolarity(ModelStandardTransform.POLARITY_UNIPOLAR);
            else
                transform.setPolarity(ModelStandardTransform.POLARITY_BIPOLAR);
            if(d == 0)
                transform.setDirection(ModelStandardTransform.DIRECTION_MIN2MAX);
            else
                transform.setDirection(ModelStandardTransform.DIRECTION_MAX2MIN);
            double x = transform.transform(i);
            if(i == 0)
                lastx = x;
            else
            {
                if(lastx - x > 0.2) return false;
                lastx = x;
            }
        }
        return true;
    }
    private static void assertTrue(boolean value) throws Exception
    {
        if(!value)
            throw new RuntimeException("assertTrue fails!");
    }
    public static void main(String[] args) throws Exception {
        ModelStandardTransform transform = new ModelStandardTransform();
        transform.setTransform(ModelStandardTransform.TRANSFORM_LINEAR);
        assertTrue(checkLinearity(transform));
        transform.setDirection(ModelStandardTransform.DIRECTION_MIN2MAX);
        transform.setPolarity(ModelStandardTransform.POLARITY_UNIPOLAR);
        assertTrue(Math.abs(transform.transform(0.2f) - 0.2f) < 0.0001f);
        assertTrue(Math.abs(transform.transform(0.8f) - 0.8f) < 0.0001f);
        transform.setDirection(ModelStandardTransform.DIRECTION_MAX2MIN);
        transform.setPolarity(ModelStandardTransform.POLARITY_UNIPOLAR);
        assertTrue(Math.abs(transform.transform(0.2f) - 0.8f) < 0.0001f);
        assertTrue(Math.abs(transform.transform(0.8f) - 0.2f) < 0.0001f);
        transform.setDirection(ModelStandardTransform.DIRECTION_MIN2MAX);
        transform.setPolarity(ModelStandardTransform.POLARITY_BIPOLAR);
        assertTrue(Math.abs(transform.transform(0.2f) - (-0.6f)) < 0.0001f);
        assertTrue(Math.abs(transform.transform(0.8f) - (0.6f)) < 0.0001f);
        transform.setDirection(ModelStandardTransform.DIRECTION_MAX2MIN);
        transform.setPolarity(ModelStandardTransform.POLARITY_BIPOLAR);
        assertTrue(Math.abs(transform.transform(0.2f) - (0.6f)) < 0.0001f);
        assertTrue(Math.abs(transform.transform(0.8f) - (-0.6f)) < 0.0001f);
    }
}
