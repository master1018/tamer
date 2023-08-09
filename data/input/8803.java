public class LookupNameWithColon {
    public static void main(String[] args) throws Exception {
        String[] names = {
            "fairly:simple", "somewhat:more/complicated",
            "multiple:colons:in:name"
        };
        Registry reg;
        try {
            reg = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        } catch (Exception ex) {
            reg = LocateRegistry.getRegistry();
        }
        for (int i = 0; i < names.length; i++) {
            reg.rebind(names[i], reg);
            Naming.lookup("rmi:
        }
    }
}
