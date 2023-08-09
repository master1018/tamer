public class EnvClone extends InitialContext {
    EnvClone(Hashtable env) throws NamingException{
        super(env);
    }
    EnvClone(boolean lazy) throws NamingException{
        super(lazy);
    }
    public static void main(String[] args) throws Exception {
        Hashtable env = new Hashtable(5);
        EnvClone ctx = new EnvClone(env);
        if (env == ctx.myProps) {
            throw new Exception(
                    "Test failed:  constructor didn't clone environment");
        }
        ctx = new EnvClone(true);
        ctx.init(env);
        if (env != ctx.myProps) {
            throw new Exception("Test failed:  init() cloned environment");
        }
    }
}
