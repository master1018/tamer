public class DensityBasedResourceValue extends ResourceValue implements IDensityBasedResourceValue {
    private Density mDensity;
    public DensityBasedResourceValue(String type, String name, String value, Density density,
            boolean isFramework) {
        super(type, name, value, isFramework);
        mDensity = density;
    }
    public Density getDensity() {
        return mDensity;
    }
}
