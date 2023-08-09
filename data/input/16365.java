class MismatchedRead implements Serializable {
    int i;
    float f;
    MismatchedRead(int i, float f) {
        this.i = i;
        this.f = f;
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(i);
        out.writeFloat(f);
        out.writeUTF("skip me");
    }
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        i = in.readInt();
        f = in.readFloat();
    }
    public boolean equals(Object obj) {
        if (! (obj instanceof MismatchedRead))
            return false;
        MismatchedRead other = (MismatchedRead) obj;
        return (i == other.i && f == other.f);
    }
}
class MismatchedReadExternal implements Externalizable {
    int i;
    float f;
    public MismatchedReadExternal() {
        this(0, (float) 0.0);
    }
    MismatchedReadExternal(int i, float f) {
        this.i = i;
        this.f = f;
    }
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(i);
        out.writeFloat(f);
        out.writeUTF("skip another");
    }
    public void readExternal(ObjectInput in) throws IOException {
        i = in.readInt();
        f = in.readFloat();
    }
    public boolean equals(Object obj) {
        if (! (obj instanceof MismatchedReadExternal))
            return false;
        MismatchedReadExternal other = (MismatchedReadExternal) obj;
        return (i == other.i && f == other.f);
    }
}
class InnocentBystander implements Serializable {
    String s;
    InnocentBystander(String s) {
        this.s = s;
    }
    public boolean equals(Object obj) {
        if (! (obj instanceof InnocentBystander))
            return false;
        InnocentBystander other = (InnocentBystander) obj;
        if (s != null)
            return s.equals(other.s);
        return (s == other.s);
    }
}
public class SkipToEndOfBlockData {
    public static void main(String[] args) throws Exception {
        ObjectOutputStream oout;
        ObjectInputStream oin;
        ByteArrayOutputStream bout;
        ByteArrayInputStream bin;
        MismatchedRead mr, mrcopy;
        MismatchedReadExternal mre, mrecopy;
        InnocentBystander ib, ibcopy;
        bout = new ByteArrayOutputStream();
        oout = new ObjectOutputStream(bout);
        mr = new MismatchedRead(1, (float) 2.34);
        mre = new MismatchedReadExternal(5, (float) 6.78);
        ib = new InnocentBystander("foo");
        oout.writeObject(mr);
        oout.writeObject(mre);
        oout.writeObject(ib);
        oout.flush();
        bin = new ByteArrayInputStream(bout.toByteArray());
        oin = new ObjectInputStream(bin);
        mrcopy = (MismatchedRead) oin.readObject();
        mrecopy = (MismatchedReadExternal) oin.readObject();
        ibcopy = (InnocentBystander) oin.readObject();
        if (! (mr.equals(mrcopy) && mre.equals(mrecopy) && ib.equals(ibcopy)))
            throw new Error("copy not equal to original");
    }
}
