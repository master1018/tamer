public class ReturnNullIfNoDefault {
    public static void main(String[] args) throws Exception {
        long larg = 1234567890L;
        GetLongAction ac = new GetLongAction("test");
        if (ac.run() != null)
            throw new Exception("Returned value is not null");
        ac = new GetLongAction("test", larg);
        long ret = ((Long)ac.run()).longValue();
        if (ret != larg)
            throw new Exception("Returned value differs from default");
        System.setProperty("test", Long.toString(larg));
        ac = new GetLongAction("test");
        ret = ((Long)ac.run()).longValue();
        if (ret != larg)
            throw new Exception("Returned value differs from property");
    }
}
