public class KeyStoreLoadStoreParameterTest {
    class MyLoadStoreParameter implements KeyStore.LoadStoreParameter {
       public KeyStore.ProtectionParameter getProtectionParameter() {
            return null;
       }
    }
}
