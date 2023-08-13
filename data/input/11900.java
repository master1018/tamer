public class UnicastRef2 extends UnicastRef {
    public UnicastRef2()
    {}
    public UnicastRef2(LiveRef liveRef) {
        super(liveRef);
    }
    public String getRefClass(ObjectOutput out)
    {
        return "UnicastRef2";
    }
    public void writeExternal(ObjectOutput out) throws IOException
    {
        ref.write(out, true);
    }
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        ref = LiveRef.read(in, true);
    }
}
