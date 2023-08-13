public class Region implements Parcelable {
    public enum Op {
        DIFFERENCE(0),
        INTERSECT(1),
        UNION(2),
        XOR(3),
        REVERSE_DIFFERENCE(4),
        REPLACE(5);
        Op(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }
    public Region() {
        this(nativeConstructor());
    }
    public Region(Region region) {
        this(nativeConstructor());
        nativeSetRegion(mNativeRegion, region.mNativeRegion);
    }
    public Region(Rect r) {
        mNativeRegion = nativeConstructor();
        nativeSetRect(mNativeRegion, r.left, r.top, r.right, r.bottom);
    }
    public Region(int left, int top, int right, int bottom) {
        mNativeRegion = nativeConstructor();
        nativeSetRect(mNativeRegion, left, top, right, bottom);
    }
    public void setEmpty() {
        nativeSetRect(mNativeRegion, 0, 0, 0, 0);
    }
    public boolean set(Region region) {
        return nativeSetRegion(mNativeRegion, region.mNativeRegion);
    }
    public boolean set(Rect r) {
        return nativeSetRect(mNativeRegion, r.left, r.top, r.right, r.bottom);
    }
    public boolean set(int left, int top, int right, int bottom) {
        return nativeSetRect(mNativeRegion, left, top, right, bottom);
    }
    public boolean setPath(Path path, Region clip) {
        return nativeSetPath(mNativeRegion, path.ni(), clip.mNativeRegion);
    }
    public native boolean isEmpty();
    public native boolean isRect();
    public native boolean isComplex();
    public Rect getBounds() {
        Rect r = new Rect();
        nativeGetBounds(mNativeRegion, r);
        return r;
    }
    public boolean getBounds(Rect r) {
        if (r == null) {
            throw new NullPointerException();
        }
        return nativeGetBounds(mNativeRegion, r);
    }
    public Path getBoundaryPath() {
        Path path = new Path();
        nativeGetBoundaryPath(mNativeRegion, path.ni());
        return path;
    }
    public boolean getBoundaryPath(Path path) {
        return nativeGetBoundaryPath(mNativeRegion, path.ni());
    }
    public native boolean contains(int x, int y);
    public boolean quickContains(Rect r) {
        return quickContains(r.left, r.top, r.right, r.bottom);
    }
    public native boolean quickContains(int left, int top, int right,
                                        int bottom);
    public boolean quickReject(Rect r) {
        return quickReject(r.left, r.top, r.right, r.bottom);
    }
    public native boolean quickReject(int left, int top, int right, int bottom);
    public native boolean quickReject(Region rgn);
    public void translate(int dx, int dy) {
        translate(dx, dy, null);
    }
    public native void translate(int dx, int dy, Region dst);
    public void scale(float scale) {
        scale(scale, null);
    }
    public native void scale(float scale, Region dst);
    public final boolean union(Rect r) {
        return op(r, Op.UNION);
    }
    public boolean op(Rect r, Op op) {
        return nativeOp(mNativeRegion, r.left, r.top, r.right, r.bottom,
                        op.nativeInt);
    }
    public boolean op(int left, int top, int right, int bottom, Op op) {
        return nativeOp(mNativeRegion, left, top, right, bottom,
                        op.nativeInt);
    }
    public boolean op(Region region, Op op) {
        return op(this, region, op);
    }
    public boolean op(Rect rect, Region region, Op op) {
        return nativeOp(mNativeRegion, rect, region.mNativeRegion,
                        op.nativeInt);
    }
    public boolean op(Region region1, Region region2, Op op) {
        return nativeOp(mNativeRegion, region1.mNativeRegion,
                        region2.mNativeRegion, op.nativeInt);
    }
    public static final Parcelable.Creator<Region> CREATOR
        = new Parcelable.Creator<Region>() {
            public Region createFromParcel(Parcel p) {
                int ni = nativeCreateFromParcel(p);
                if (ni == 0) {
                    throw new RuntimeException();
                }
                return new Region(ni);
            }
            public Region[] newArray(int size) {
                return new Region[size];
            }
    };
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel p, int flags) {
        if (!nativeWriteToParcel(mNativeRegion, p)) {
            throw new RuntimeException();
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Region)) {
            return false;
        }
        Region peer = (Region) obj;
        return nativeEquals(mNativeRegion, peer.mNativeRegion);
    }
    protected void finalize() throws Throwable {
        nativeDestructor(mNativeRegion);
    }
     Region(int ni) {
        if (ni == 0) {
            throw new RuntimeException();
        }
        mNativeRegion = ni;
    }
    private Region(int ni, int dummy) {
        this(ni);
    }
     final int ni() {
        return mNativeRegion;
    }
    private static native int nativeConstructor();
    private static native void nativeDestructor(int native_region);
    private static native boolean nativeSetRegion(int native_dst,
                                                  int native_src);
    private static native boolean nativeSetRect(int native_dst, int left,
                                                int top, int right, int bottom);
    private static native boolean nativeSetPath(int native_dst, int native_path,
                                                int native_clip);
    private static native boolean nativeGetBounds(int native_region, Rect rect);
    private static native boolean nativeGetBoundaryPath(int native_region,
                                                        int native_path);
    private static native boolean nativeOp(int native_dst, int left, int top,
                                           int right, int bottom, int op);
    private static native boolean nativeOp(int native_dst, Rect rect,
                                           int native_region, int op);
    private static native boolean nativeOp(int native_dst, int native_region1,
                                           int native_region2, int op);
    private static native int nativeCreateFromParcel(Parcel p);
    private static native boolean nativeWriteToParcel(int native_region,
                                                      Parcel p);
    private static native boolean nativeEquals(int native_r1, int native_r2);
    private final int mNativeRegion;
}
