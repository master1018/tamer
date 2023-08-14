public final class ConflictInFlush{
    public static void main(String args[]) {
        Preferences root = Preferences.userRoot();
        try {
            Preferences node = root.node("1/2/3");
            node.flush();
            System.out.println("Node "+node+" has been created");
            System.out.println("Removing node "+node);
            node.removeNode();
            node.flush();
        }catch (BackingStoreException bse){
            bse.printStackTrace();
        }
    }
}
