public class SelectNowWhenEmpty {
    public static void main(String[] args) throws Exception {
        Selector s = SelectorProvider.provider().openSelector();
        s.selectNow();
    }
}
