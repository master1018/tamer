public class ORBStreamObjectCopierImpl implements ObjectCopier {
    public ORBStreamObjectCopierImpl( ORB orb )
    {
        this.orb = orb ;
    }
    public Object copy(Object obj) {
        if (obj instanceof Remote) {
            return Utility.autoConnect(obj,orb,true);
        }
        OutputStream out = (OutputStream)orb.create_output_stream();
        out.write_value((Serializable)obj);
        InputStream in = (InputStream)out.create_input_stream();
        return in.read_value();
    }
    private ORB orb;
}
