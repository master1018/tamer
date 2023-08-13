public class Provider1 extends Provider {
    public Provider1() {
        super("Provider1", 1.0, "SecretKeyFactory");
        System.out.println("Creating Provider1");
        put("SecretKeyFactory.DUMMY", "com.p1.P1SecretKeyFactory");
    }
}
