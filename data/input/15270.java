public class TestEC {
    public static void main(String[] args) throws Exception {
        Provider p = new sun.security.ec.SunEC();
        System.out.println("Running tests with " + p.getName() +
            " provider...\n");
        long start = System.currentTimeMillis();
        new TestECDH().main(p);
        new TestECDSA().main(p);
        new TestCurves().main(p);
        new TestKeyFactory().main(p);
        new TestECGenSpec().main(p);
        new ReadPKCS12().main(p);
        new ReadCertificates().main(p);
        new ClientJSSEServerJSSE().main(p);
        long stop = System.currentTimeMillis();
        System.out.println("\nCompleted tests with " + p.getName() +
            " provider (" + ((stop - start) / 1000.0) + " seconds).");
    }
}
