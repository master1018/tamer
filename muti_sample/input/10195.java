public class ICC_ColorSpace extends ColorSpace {
    static final long serialVersionUID = 3455889114070431483L;
    private ICC_Profile    thisProfile;
    private float[] minVal;
    private float[] maxVal;
    private float[] diffMinMax;
    private float[] invDiffMinMax;
    private boolean needScaleInit = true;
    private transient ColorTransform this2srgb;
    private transient ColorTransform srgb2this;
    private transient ColorTransform this2xyz;
    private transient ColorTransform xyz2this;
    public ICC_ColorSpace (ICC_Profile profile) {
        super (profile.getColorSpaceType(), profile.getNumComponents());
        int profileClass = profile.getProfileClass();
        if ((profileClass != ICC_Profile.CLASS_INPUT) &&
            (profileClass != ICC_Profile.CLASS_DISPLAY) &&
            (profileClass != ICC_Profile.CLASS_OUTPUT) &&
            (profileClass != ICC_Profile.CLASS_COLORSPACECONVERSION) &&
            (profileClass != ICC_Profile.CLASS_NAMEDCOLOR) &&
            (profileClass != ICC_Profile.CLASS_ABSTRACT)) {
            throw new IllegalArgumentException("Invalid profile type");
        }
        thisProfile = profile;
        setMinMax();
    }
    public ICC_Profile getProfile() {
        return thisProfile;
    }
    public float[]    toRGB (float[] colorvalue) {
        if (this2srgb == null) {
            ColorTransform[] transformList = new ColorTransform [2];
            ICC_ColorSpace srgbCS =
                (ICC_ColorSpace) ColorSpace.getInstance (CS_sRGB);
            PCMM mdl = CMSManager.getModule();
            transformList[0] = mdl.createTransform(
                thisProfile, ColorTransform.Any, ColorTransform.In);
            transformList[1] = mdl.createTransform(
                srgbCS.getProfile(), ColorTransform.Any, ColorTransform.Out);
            this2srgb = mdl.createTransform(transformList);
            if (needScaleInit) {
                setComponentScaling();
            }
        }
        int nc = this.getNumComponents();
        short tmp[] = new short[nc];
        for (int i = 0; i < nc; i++) {
            tmp[i] = (short)
                ((colorvalue[i] - minVal[i]) * invDiffMinMax[i] + 0.5f);
        }
        tmp = this2srgb.colorConvert(tmp, null);
        float[] result = new float [3];
        for (int i = 0; i < 3; i++) {
            result[i] = ((float) (tmp[i] & 0xffff)) / 65535.0f;
        }
        return result;
    }
    public float[]    fromRGB(float[] rgbvalue) {
        if (srgb2this == null) {
            ColorTransform[] transformList = new ColorTransform [2];
            ICC_ColorSpace srgbCS =
                (ICC_ColorSpace) ColorSpace.getInstance (CS_sRGB);
            PCMM mdl = CMSManager.getModule();
            transformList[0] = mdl.createTransform(
                srgbCS.getProfile(), ColorTransform.Any, ColorTransform.In);
            transformList[1] = mdl.createTransform(
                thisProfile, ColorTransform.Any, ColorTransform.Out);
            srgb2this = mdl.createTransform(transformList);
            if (needScaleInit) {
                setComponentScaling();
            }
        }
        short tmp[] = new short[3];
        for (int i = 0; i < 3; i++) {
            tmp[i] = (short) ((rgbvalue[i] * 65535.0f) + 0.5f);
        }
        tmp = srgb2this.colorConvert(tmp, null);
        int nc = this.getNumComponents();
        float[] result = new float [nc];
        for (int i = 0; i < nc; i++) {
            result[i] = (((float) (tmp[i] & 0xffff)) / 65535.0f) *
                        diffMinMax[i] + minVal[i];
        }
        return result;
    }
    public float[]    toCIEXYZ(float[] colorvalue) {
        if (this2xyz == null) {
            ColorTransform[] transformList = new ColorTransform [2];
            ICC_ColorSpace xyzCS =
                (ICC_ColorSpace) ColorSpace.getInstance (CS_CIEXYZ);
            PCMM mdl = CMSManager.getModule();
            try {
                transformList[0] = mdl.createTransform(
                    thisProfile, ICC_Profile.icRelativeColorimetric,
                    ColorTransform.In);
            } catch (CMMException e) {
                transformList[0] = mdl.createTransform(
                    thisProfile, ColorTransform.Any, ColorTransform.In);
            }
            transformList[1] = mdl.createTransform(
                xyzCS.getProfile(), ColorTransform.Any, ColorTransform.Out);
            this2xyz = mdl.createTransform (transformList);
            if (needScaleInit) {
                setComponentScaling();
            }
        }
        int nc = this.getNumComponents();
        short tmp[] = new short[nc];
        for (int i = 0; i < nc; i++) {
            tmp[i] = (short)
                ((colorvalue[i] - minVal[i]) * invDiffMinMax[i] + 0.5f);
        }
        tmp = this2xyz.colorConvert(tmp, null);
        float ALMOST_TWO = 1.0f + (32767.0f / 32768.0f);
        float[] result = new float [3];
        for (int i = 0; i < 3; i++) {
            result[i] = (((float) (tmp[i] & 0xffff)) / 65535.0f) * ALMOST_TWO;
        }
        return result;
    }
    public float[]    fromCIEXYZ(float[] colorvalue) {
        if (xyz2this == null) {
            ColorTransform[] transformList = new ColorTransform [2];
            ICC_ColorSpace xyzCS =
                (ICC_ColorSpace) ColorSpace.getInstance (CS_CIEXYZ);
            PCMM mdl = CMSManager.getModule();
            transformList[0] = mdl.createTransform (
                xyzCS.getProfile(), ColorTransform.Any, ColorTransform.In);
            try {
                transformList[1] = mdl.createTransform(
                    thisProfile, ICC_Profile.icRelativeColorimetric,
                    ColorTransform.Out);
            } catch (CMMException e) {
                transformList[1] = CMSManager.getModule().createTransform(
                thisProfile, ColorTransform.Any, ColorTransform.Out);
            }
            xyz2this = mdl.createTransform(transformList);
            if (needScaleInit) {
                setComponentScaling();
            }
        }
        short tmp[] = new short[3];
        float ALMOST_TWO = 1.0f + (32767.0f / 32768.0f);
        float factor = 65535.0f / ALMOST_TWO;
        for (int i = 0; i < 3; i++) {
            tmp[i] = (short) ((colorvalue[i] * factor) + 0.5f);
        }
        tmp = xyz2this.colorConvert(tmp, null);
        int nc = this.getNumComponents();
        float[] result = new float [nc];
        for (int i = 0; i < nc; i++) {
            result[i] = (((float) (tmp[i] & 0xffff)) / 65535.0f) *
                        diffMinMax[i] + minVal[i];
        }
        return result;
    }
    public float getMinValue(int component) {
        if ((component < 0) || (component > this.getNumComponents() - 1)) {
            throw new IllegalArgumentException(
                "Component index out of range: + component");
        }
        return minVal[component];
    }
    public float getMaxValue(int component) {
        if ((component < 0) || (component > this.getNumComponents() - 1)) {
            throw new IllegalArgumentException(
                "Component index out of range: + component");
        }
        return maxVal[component];
    }
    private void setMinMax() {
        int nc = this.getNumComponents();
        int type = this.getType();
        minVal = new float[nc];
        maxVal = new float[nc];
        if (type == ColorSpace.TYPE_Lab) {
            minVal[0] = 0.0f;    
            maxVal[0] = 100.0f;
            minVal[1] = -128.0f; 
            maxVal[1] = 127.0f;
            minVal[2] = -128.0f; 
            maxVal[2] = 127.0f;
        } else if (type == ColorSpace.TYPE_XYZ) {
            minVal[0] = minVal[1] = minVal[2] = 0.0f; 
            maxVal[0] = maxVal[1] = maxVal[2] = 1.0f + (32767.0f/ 32768.0f);
        } else {
            for (int i = 0; i < nc; i++) {
                minVal[i] = 0.0f;
                maxVal[i] = 1.0f;
            }
        }
    }
    private void setComponentScaling() {
        int nc = this.getNumComponents();
        diffMinMax = new float[nc];
        invDiffMinMax = new float[nc];
        for (int i = 0; i < nc; i++) {
            minVal[i] = this.getMinValue(i); 
            maxVal[i] = this.getMaxValue(i); 
            diffMinMax[i] = maxVal[i] - minVal[i];
            invDiffMinMax[i] = 65535.0f / diffMinMax[i];
        }
        needScaleInit = false;
    }
}
