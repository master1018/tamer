public class KeyTabIndex {
    public static void main(String[] args) throws Exception {
        KeyTab kt = KeyTab.create("ktab");
        kt.addEntry(new PrincipalName(
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@A"),
                "x".toCharArray(), 1, true);
        kt.addEntry(new PrincipalName("a@A"), "x".toCharArray(), 1, true);
        kt.save();
        Runnable t = new Runnable() {
            @Override
            public void run() {
                KeyTab.getInstance("ktab").getClass();
            }
        };
        for (int i=0; i<10; i++) {
            new Thread(t).start();
        }
    }
}
