public class TestDefault {
    public static void main(String[] args) throws Exception {
        TerminalFactory factory = TerminalFactory.getDefault();
        System.out.println("Type: " + factory.getType());
        List<CardTerminal> terminals = factory.terminals().list();
        System.out.println("Terminals: " + terminals);
        if (terminals.isEmpty()) {
            throw new Exception("no terminals");
        }
        System.out.println("OK.");
    }
}
