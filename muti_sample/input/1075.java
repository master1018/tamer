public class PrimitivesTest implements java.io.Serializable {
    byte b = 1;
    char c = 'c';
    float f = 3.14159f;
    long l = 3;
    double d = 1.740;
    int i = 4;
    boolean z = true;
    short s = 2;
    transient int trans = 89;
    PrimitivesTest self = this;
    public boolean equals(PrimitivesTest other) {
        if (b != other.b ||
            c != other.c ||
            f != other.f ||
            l != other.l ||
            d != other.d ||
            i != other.i ||
            s != other.s ||
            z != other.z )
        {
            return false;
        }
        return true;
    }
}
