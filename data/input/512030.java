public class MyLoadStoreParams implements 
        KeyStore.LoadStoreParameter {
    KeyStore.ProtectionParameter protPar;
    public MyLoadStoreParams(KeyStore.ProtectionParameter p) {
        this.protPar = p;
    }
    public KeyStore.ProtectionParameter getProtectionParameter() {
        return protPar;
    }
}
