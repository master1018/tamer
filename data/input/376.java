final class ColorModelHSV extends ColorModel {
    ColorModelHSV() {
        super("hsv", "Hue", "Saturation", "Value", "Transparency"); 
    }
    @Override
    void setColor(int color, float[] space) {
        super.setColor(color, space);
        RGBtoHSV(space, space);
        space[3] = 1.0f - space[3];
    }
    @Override
    int getColor(float[] space) {
        space[3] = 1.0f - space[3];
        HSVtoRGB(space, space);
        return super.getColor(space);
    }
    @Override
    int getMaximum(int index) {
        return (index == 0) ? 360 : 100;
    }
    @Override
    float getDefault(int index) {
        return (index == 0) ? -1.0f : 1.0f;
    }
    private static float[] HSVtoRGB(float[] hsv, float[] rgb) {
        if (rgb == null) {
            rgb = new float[3];
        }
        float hue = hsv[0];
        float saturation = hsv[1];
        float value = hsv[2];
        rgb[0] = value;
        rgb[1] = value;
        rgb[2] = value;
        if (saturation > 0.0f) {
            hue = (hue < 1.0f) ? hue * 6.0f : 0.0f;
            int integer = (int) hue;
            float f = hue - (float) integer;
            switch (integer) {
                case 0:
                    rgb[1] *= 1.0f - saturation * (1.0f - f);
                    rgb[2] *= 1.0f - saturation;
                    break;
                case 1:
                    rgb[0] *= 1.0f - saturation * f;
                    rgb[2] *= 1.0f - saturation;
                    break;
                case 2:
                    rgb[0] *= 1.0f - saturation;
                    rgb[2] *= 1.0f - saturation * (1.0f - f);
                    break;
                case 3:
                    rgb[0] *= 1.0f - saturation;
                    rgb[1] *= 1.0f - saturation * f;
                    break;
                case 4:
                    rgb[0] *= 1.0f - saturation * (1.0f - f);
                    rgb[1] *= 1.0f - saturation;
                    break;
                case 5:
                    rgb[1] *= 1.0f - saturation;
                    rgb[2] *= 1.0f - saturation * f;
                    break;
            }
        }
        return rgb;
    }
    private static float[] RGBtoHSV(float[] rgb, float[] hsv) {
        if (hsv == null) {
            hsv = new float[3];
        }
        float max = ColorModelHSL.max(rgb[0], rgb[1], rgb[2]);
        float min = ColorModelHSL.min(rgb[0], rgb[1], rgb[2]);
        float saturation = max - min;
        if (saturation > 0.0f) {
            saturation /= max;
        }
        hsv[0] = ColorModelHSL.getHue(rgb[0], rgb[1], rgb[2], max, min);
        hsv[1] = saturation;
        hsv[2] = max;
        return hsv;
    }
}
