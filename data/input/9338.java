public class FnnClass implements FnnUnmarshal {
    public Object unmarshal(MarshalledObject mobj)
        throws IOException, ClassNotFoundException
    {
        return mobj.get();
    }
}
