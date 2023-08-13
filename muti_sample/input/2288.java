public class NoInstalledProviders {
    public static void main(String[] argv) {
        Provider[] provs = Security.getProviders();
        for(int i = 0; i < provs.length; i++ ) {
            Security.removeProvider( provs[i].getName());
        }
        String filter = "Signature.SHA1withDSA";
        provs = Security.getProviders(filter);
    }
}
