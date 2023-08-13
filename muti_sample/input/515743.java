public final class ResourceHelper {
    private final static Pattern sFloatPattern = Pattern.compile("(-?[0-9]+(?:\\.[0-9]+)?)(.*)");
    private final static float[] sFloatOut = new float[1];
    private final static TypedValue mValue = new TypedValue();
    static int getColor(String value) {
        if (value != null) {
            if (value.startsWith("#") == false) {
                throw new NumberFormatException();
            }
            value = value.substring(1);
            if (value.length() > 8) {
                throw new NumberFormatException();
            }
            if (value.length() == 3) { 
                char[] color = new char[8];
                color[0] = color[1] = 'F';
                color[2] = color[3] = value.charAt(0);
                color[4] = color[5] = value.charAt(1);
                color[6] = color[7] = value.charAt(2);
                value = new String(color);
            } else if (value.length() == 4) { 
                char[] color = new char[8];
                color[0] = color[1] = value.charAt(0);
                color[2] = color[3] = value.charAt(1);
                color[4] = color[5] = value.charAt(2);
                color[6] = color[7] = value.charAt(3);
                value = new String(color);
            } else if (value.length() == 6) {
                value = "FF" + value;
            }
            return (int)Long.parseLong(value, 16);
        }
        throw new NumberFormatException();
    }
    public static Drawable getDrawable(IResourceValue value, BridgeContext context, boolean isFramework) {
        Drawable d = null;
        String stringValue = value.getValue();
        String lowerCaseValue = stringValue.toLowerCase();
        if (lowerCaseValue.endsWith(NinePatch.EXTENSION_9PATCH)) {
            File file = new File(stringValue);
            if (file.isFile()) {
                NinePatch ninePatch = Bridge.getCached9Patch(stringValue,
                        isFramework ? null : context.getProjectKey());
                if (ninePatch == null) {
                    try {
                        ninePatch = NinePatch.load(file.toURL(), false );
                        Bridge.setCached9Patch(stringValue, ninePatch,
                                isFramework ? null : context.getProjectKey());
                    } catch (MalformedURLException e) {
                    } catch (IOException e) {
                    }
                }
                if (ninePatch != null) {
                    return new NinePatchDrawable(ninePatch);
                }
            }
            return null;
        } else if (lowerCaseValue.endsWith(".xml")) {
            File f = new File(stringValue);
            if (f.isFile()) {
                try {
                    KXmlParser parser = new KXmlParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                    parser.setInput(new FileReader(f));
                    d = Drawable.createFromXml(context.getResources(),
                            new BridgeXmlBlockParser(parser, context, false));
                    return d;
                } catch (XmlPullParserException e) {
                    context.getLogger().error(e);
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                    context.getLogger().error(e);
                }
            }
            return null;
        } else {
            File bmpFile = new File(stringValue);
            if (bmpFile.isFile()) {
                try {
                    Bitmap bitmap = Bridge.getCachedBitmap(stringValue,
                            isFramework ? null : context.getProjectKey());
                    if (bitmap == null) {
                        bitmap = new Bitmap(bmpFile);
                        try {
                            bitmap.setDensity(Density.MEDIUM.getValue());
                        } catch (NoClassDefFoundError error) {
                        }
                        Bridge.setCachedBitmap(stringValue, bitmap,
                                isFramework ? null : context.getProjectKey());
                    }
                    try {
                        if (value instanceof IDensityBasedResourceValue) {
                            Density density = ((IDensityBasedResourceValue)value).getDensity();
                            if (density != Density.MEDIUM) {
                                bitmap = Bitmap.createBitmap(bitmap);
                                bitmap.setDensity(density.getValue());
                            }
                        }
                    } catch (NoClassDefFoundError error) {
                    }
                    return new BitmapDrawable(context.getResources(), bitmap);
                } catch (IOException e) {
                }
            } else {
                try {
                    int color = getColor(stringValue);
                    return new ColorDrawable(color);
                } catch (NumberFormatException e) {
                }
            }
        }
        return null;
    }
    private static final class UnitEntry {
        String name;
        int type;
        int unit;
        float scale;
        UnitEntry(String name, int type, int unit, float scale) {
            this.name = name;
            this.type = type;
            this.unit = unit;
            this.scale = scale;
        }
    }
    private final static UnitEntry[] sUnitNames = new UnitEntry[] {
        new UnitEntry("px", TypedValue.TYPE_DIMENSION, TypedValue.COMPLEX_UNIT_PX, 1.0f),
        new UnitEntry("dip", TypedValue.TYPE_DIMENSION, TypedValue.COMPLEX_UNIT_DIP, 1.0f),
        new UnitEntry("dp", TypedValue.TYPE_DIMENSION, TypedValue.COMPLEX_UNIT_DIP, 1.0f),
        new UnitEntry("sp", TypedValue.TYPE_DIMENSION, TypedValue.COMPLEX_UNIT_SP, 1.0f),
        new UnitEntry("pt", TypedValue.TYPE_DIMENSION, TypedValue.COMPLEX_UNIT_PT, 1.0f),
        new UnitEntry("in", TypedValue.TYPE_DIMENSION, TypedValue.COMPLEX_UNIT_IN, 1.0f),
        new UnitEntry("mm", TypedValue.TYPE_DIMENSION, TypedValue.COMPLEX_UNIT_MM, 1.0f),
        new UnitEntry("%", TypedValue.TYPE_FRACTION, TypedValue.COMPLEX_UNIT_FRACTION, 1.0f/100),
        new UnitEntry("%p", TypedValue.TYPE_FRACTION, TypedValue.COMPLEX_UNIT_FRACTION_PARENT, 1.0f/100),
    };
    public static TypedValue getValue(String s) {
        if (stringToFloat(s, mValue)) {
            return mValue;
        }
        return null;
    }
    public static boolean stringToFloat(String s, TypedValue outValue) {
        s.trim();
        int len = s.length();
        if (len <= 0) {
            return false;
        }
        char[] buf = s.toCharArray();
        for (int i = 0 ; i < len ; i++) {
            if (buf[i] > 255) {
                return false;
            }
        }
        if (buf[0] < '0' && buf[0] > '9' && buf[0] != '.') {
            return false;
        }
        Matcher m = sFloatPattern.matcher(s);
        if (m.matches()) {
            String f_str = m.group(1);
            String end = m.group(2);
            float f;
            try {
                f = Float.parseFloat(f_str);
            } catch (NumberFormatException e) {
                return false;
            }
            if (end.length() > 0 && end.charAt(0) != ' ') {
                if (parseUnit(end, outValue, sFloatOut)) {
                    f *= sFloatOut[0];
                    boolean neg = f < 0;
                    if (neg) {
                        f = -f;
                    }
                    long bits = (long)(f*(1<<23)+.5f);
                    int radix;
                    int shift;
                    if ((bits&0x7fffff) == 0) {
                        radix = TypedValue.COMPLEX_RADIX_23p0;
                        shift = 23;
                    } else if ((bits&0xffffffffff800000L) == 0) {
                        radix = TypedValue.COMPLEX_RADIX_0p23;
                        shift = 0;
                    } else if ((bits&0xffffffff80000000L) == 0) {
                        radix = TypedValue.COMPLEX_RADIX_8p15;
                        shift = 8;
                    } else if ((bits&0xffffff8000000000L) == 0) {
                        radix = TypedValue.COMPLEX_RADIX_16p7;
                        shift = 16;
                    } else {
                        radix = TypedValue.COMPLEX_RADIX_23p0;
                        shift = 23;
                    }
                    int mantissa = (int)(
                        (bits>>shift) & TypedValue.COMPLEX_MANTISSA_MASK);
                    if (neg) {
                        mantissa = (-mantissa) & TypedValue.COMPLEX_MANTISSA_MASK;
                    }
                    outValue.data |=
                        (radix<<TypedValue.COMPLEX_RADIX_SHIFT)
                        | (mantissa<<TypedValue.COMPLEX_MANTISSA_SHIFT);
                    return true;
                }
                return false;
            }
            end = end.trim();
            if (end.length() == 0) {
                if (outValue != null) {
                    outValue.type = TypedValue.TYPE_FLOAT;
                    outValue.data = Float.floatToIntBits(f);
                    return true;
                }
            }
        }
        return false;
    }
    private static boolean parseUnit(String str, TypedValue outValue, float[] outScale) {
        str = str.trim();
        for (UnitEntry unit : sUnitNames) {
            if (unit.name.equals(str)) {
                outValue.type = unit.type;
                outValue.data = unit.unit << TypedValue.COMPLEX_UNIT_SHIFT;
                outScale[0] = unit.scale;
                return true;
            }
        }
        return false;
    }
}
