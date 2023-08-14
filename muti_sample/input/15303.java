public final class MediaPrintableArea
      implements DocAttribute, PrintRequestAttribute, PrintJobAttribute {
    private int x, y, w, h;
    private int units;
    private static final long serialVersionUID = -1597171464050795793L;
    public static final int INCH = 25400;
    public static final int MM = 1000;
    public MediaPrintableArea(float x, float y, float w, float h, int units) {
        if ((x < 0.0) || (y < 0.0) || (w <= 0.0) || (h <= 0.0) ||
            (units < 1)) {
            throw new IllegalArgumentException("0 or negative value argument");
        }
        this.x = (int) (x * units + 0.5f);
        this.y = (int) (y * units + 0.5f);
        this.w = (int) (w * units + 0.5f);
        this.h = (int) (h * units + 0.5f);
    }
    public MediaPrintableArea(int x, int y, int w, int h, int units) {
        if ((x < 0) || (y < 0) || (w <= 0) || (h <= 0) ||
            (units < 1)) {
            throw new IllegalArgumentException("0 or negative value argument");
        }
        this.x = x * units;
        this.y = y * units;
        this.w = w * units;
        this.h = h * units;
    }
    public float[] getPrintableArea(int units) {
        return new float[] { getX(units), getY(units),
                             getWidth(units), getHeight(units) };
    }
     public float getX(int units) {
        return convertFromMicrometers(x, units);
     }
     public float getY(int units) {
        return convertFromMicrometers(y, units);
     }
     public float getWidth(int units) {
        return convertFromMicrometers(w, units);
     }
     public float getHeight(int units) {
        return convertFromMicrometers(h, units);
     }
    public boolean equals(Object object) {
        boolean ret = false;
        if (object instanceof MediaPrintableArea) {
           MediaPrintableArea mm = (MediaPrintableArea)object;
           if (x == mm.x &&  y == mm.y && w == mm.w && h == mm.h) {
               ret = true;
           }
        }
        return ret;
    }
    public final Class<? extends Attribute> getCategory() {
        return MediaPrintableArea.class;
    }
    public final String getName() {
        return "media-printable-area";
    }
    public String toString(int units, String unitsName) {
        if (unitsName == null) {
            unitsName = "";
        }
        float []vals = getPrintableArea(units);
        String str = "("+vals[0]+","+vals[1]+")->("+vals[2]+","+vals[3]+")";
        return str + unitsName;
    }
    public String toString() {
        return(toString(MM, "mm"));
    }
    public int hashCode() {
        return x + 37*y + 43*w + 47*h;
    }
    private static float convertFromMicrometers(int x, int units) {
        if (units < 1) {
            throw new IllegalArgumentException("units is < 1");
        }
        return ((float)x) / ((float)units);
    }
}
