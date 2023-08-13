public class ICC_ColorSpace extends ColorSpace {
    private static final long serialVersionUID = 3455889114070431483L;
    private static final ObjectStreamField[]
      serialPersistentFields = {
        new ObjectStreamField("thisProfile", ICC_Profile.class), 
        new ObjectStreamField("minVal", float[].class), 
        new ObjectStreamField("maxVal", float[].class), 
        new ObjectStreamField("diffMinMax", float[].class), 
        new ObjectStreamField("invDiffMinMax", float[].class), 
        new ObjectStreamField("needScaleInit", Boolean.TYPE) 
    };
    private static final float MAX_XYZ = 1f + (32767f/32768f);
    private static final float MAX_SHORT = 65535f;
    private static final float INV_MAX_SHORT = 1f/MAX_SHORT;
    private static final float SHORT2XYZ_FACTOR = MAX_XYZ/MAX_SHORT;
    private static final float XYZ2SHORT_FACTOR = MAX_SHORT/MAX_XYZ;
    private ICC_Profile profile = null;
    private float minValues[] = null;
    private float maxValues[] = null;
    private ICC_Transform toRGBTransform = null;
    private ICC_Transform fromRGBTransform = null;
    private ICC_Transform toXYZTransform = null;
    private ICC_Transform fromXYZTransform = null;
    private final ColorConverter converter = new ColorConverter();
    private final ColorScaler scaler = new ColorScaler();
    private boolean scalingDataLoaded = false;
    private ICC_ColorSpace resolvedDeserializedInst;
    public ICC_ColorSpace(ICC_Profile pf) {
        super(pf.getColorSpaceType(), pf.getNumComponents());
        int pfClass = pf.getProfileClass();
        switch (pfClass) {
            case ICC_Profile.CLASS_COLORSPACECONVERSION:
            case ICC_Profile.CLASS_DISPLAY:
            case ICC_Profile.CLASS_OUTPUT:
            case ICC_Profile.CLASS_INPUT:
                break; 
            default:
                throw new IllegalArgumentException(Messages.getString("awt.168")); 
        }
        profile = pf;
        fillMinMaxValues();
    }
    public ICC_Profile getProfile() {
        if (profile instanceof ICC_ProfileStub) {
            profile = ((ICC_ProfileStub) profile).loadProfile();
        }
        return profile;
    }
    @Override
    public float[] toRGB(float[] colorvalue) {
        if (toRGBTransform == null) {
            ICC_Profile sRGBProfile =
                ((ICC_ColorSpace) ColorSpace.getInstance(CS_sRGB)).getProfile();
            ICC_Profile[] profiles = {getProfile(), sRGBProfile};
            toRGBTransform = new ICC_Transform(profiles);
            if (!scalingDataLoaded) {
                scaler.loadScalingData(this);
                scalingDataLoaded = true;
            }
        }
        short[] data = new short[getNumComponents()];
        scaler.scale(colorvalue, data, 0);
        short[] converted =
            converter.translateColor(toRGBTransform, data, null);
        float[] res = new float[3];
        res[0] = ((converted[0] & 0xFFFF)) * INV_MAX_SHORT;
        res[1] = ((converted[1] & 0xFFFF)) * INV_MAX_SHORT;
        res[2] = ((converted[2] & 0xFFFF)) * INV_MAX_SHORT;
        return res;
    }
    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        if (toXYZTransform == null) {
            ICC_Profile xyzProfile =
                ((ICC_ColorSpace) ColorSpace.getInstance(CS_CIEXYZ)).getProfile();
            ICC_Profile[] profiles = {getProfile(), xyzProfile};
            try {
                int[] intents = {
                        ICC_Profile.icRelativeColorimetric,
                        ICC_Profile.icPerceptual};
                toXYZTransform = new ICC_Transform(profiles, intents);
            } catch (CMMException e) { 
                toXYZTransform = new ICC_Transform(profiles);
            }
            if (!scalingDataLoaded) {
                scaler.loadScalingData(this);
                scalingDataLoaded = true;
            }
        }
        short[] data = new short[getNumComponents()];
        scaler.scale(colorvalue, data, 0);
        short[] converted =
            converter.translateColor(toXYZTransform, data, null);
        float[] res = new float[3];
        res[0] = ((converted[0] & 0xFFFF)) * SHORT2XYZ_FACTOR;
        res[1] = ((converted[1] & 0xFFFF)) * SHORT2XYZ_FACTOR;
        res[2] = ((converted[2] & 0xFFFF)) * SHORT2XYZ_FACTOR;
        return res;
    }
    @Override
    public float[] fromRGB(float[] rgbvalue) {
        if (fromRGBTransform == null) {
            ICC_Profile sRGBProfile =
                ((ICC_ColorSpace) ColorSpace.getInstance(CS_sRGB)).getProfile();
            ICC_Profile[] profiles = {sRGBProfile, getProfile()};
            fromRGBTransform = new ICC_Transform(profiles);
            if (!scalingDataLoaded) {
                scaler.loadScalingData(this);
                scalingDataLoaded = true;
            }
        }
        short[] scaledRGBValue = new short[3];
        scaledRGBValue[0] = (short)(rgbvalue[0] * MAX_SHORT + 0.5f);
        scaledRGBValue[1] = (short)(rgbvalue[1] * MAX_SHORT + 0.5f);
        scaledRGBValue[2] = (short)(rgbvalue[2] * MAX_SHORT + 0.5f);
        short[] converted =
            converter.translateColor(fromRGBTransform, scaledRGBValue, null);
        float[] res = new float[getNumComponents()];
        scaler.unscale(res, converted, 0);
        return res;
    }
    @Override
    public float[] fromCIEXYZ(float[] xyzvalue) {
        if (fromXYZTransform == null) {
            ICC_Profile xyzProfile =
                ((ICC_ColorSpace) ColorSpace.getInstance(CS_CIEXYZ)).getProfile();
            ICC_Profile[] profiles = {xyzProfile, getProfile()};
            try {
                int[] intents = {
                        ICC_Profile.icPerceptual,
                        ICC_Profile.icRelativeColorimetric};
                fromXYZTransform = new ICC_Transform(profiles, intents);
            } catch (CMMException e) { 
                fromXYZTransform = new ICC_Transform(profiles);
            }
            if (!scalingDataLoaded) {
                scaler.loadScalingData(this);
                scalingDataLoaded = true;
            }
        }
        short[] scaledXYZValue = new short[3];
        scaledXYZValue[0] = (short)(xyzvalue[0] * XYZ2SHORT_FACTOR + 0.5f);
        scaledXYZValue[1] = (short)(xyzvalue[1] * XYZ2SHORT_FACTOR + 0.5f);
        scaledXYZValue[2] = (short)(xyzvalue[2] * XYZ2SHORT_FACTOR + 0.5f);
        short[] converted =
            converter.translateColor(fromXYZTransform, scaledXYZValue, null);
        float[] res = new float[getNumComponents()];
        scaler.unscale(res, converted, 0);
        return res;
    }
    @Override
    public float getMinValue(int component) {
        if ((component < 0) || (component > this.getNumComponents() - 1)) {
            throw new IllegalArgumentException(Messages.getString("awt.169")); 
        }
        return minValues[component];
    }
    @Override
    public float getMaxValue(int component) {
        if ((component < 0) || (component > this.getNumComponents() - 1)) {
            throw new IllegalArgumentException(Messages.getString("awt.169")); 
        }
        return maxValues[component];
    }
    private void fillMinMaxValues() {
        int n = getNumComponents();
        maxValues = new float[n];
        minValues = new float[n];
        switch (getType()) {
            case ColorSpace.TYPE_XYZ:
                minValues[0] = 0;
                minValues[1] = 0;
                minValues[2] = 0;
                maxValues[0] = MAX_XYZ;
                maxValues[1] = MAX_XYZ;
                maxValues[2] = MAX_XYZ;
                break;
            case ColorSpace.TYPE_Lab:
                minValues[0] = 0;
                minValues[1] = -128;
                minValues[2] = -128;
                maxValues[0] = 100;
                maxValues[1] = 127;
                maxValues[2] = 127;
                break;
            default:
                for(int i=0; i<n; i++) {
                    minValues[i] = 0;
                    maxValues[i] = 1;
                }
        }
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("thisProfile", profile); 
        fields.put("minVal", null); 
        fields.put("maxVal", null); 
        fields.put("diffMinMax", null); 
        fields.put("invDiffMinMax", null); 
        fields.put("needScaleInit", true); 
        out.writeFields();
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        resolvedDeserializedInst =
                new ICC_ColorSpace((ICC_Profile) fields.get("thisProfile", null)); 
    }
    Object readResolve() throws ObjectStreamException {
        return resolvedDeserializedInst;
    }
}
