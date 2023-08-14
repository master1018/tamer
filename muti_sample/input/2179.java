public class Provider2 extends Provider {
    public Provider2() {
        super("Provider2", 1.0, "SecretKeyFactory");
        System.out.println("Creating Provider2");
        put("SecretKeyFactory.DUMMY", "com.p2.P2SecretKeyFactory");
    }
}
