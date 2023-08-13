public class ResetModule implements LoginModule {
        public ResetModule() { }
        public void initialize(Subject s, CallbackHandler h,
                Map<String,?> ss, Map<String,?> options) {
            throw new SecurityException("INITIALIZE");
        }
        public boolean login() throws LoginException { return true; }
        public boolean commit() throws LoginException { return true; }
        public boolean abort() throws LoginException { return true; }
        public boolean logout() throws LoginException { return true; }
}
