public class TestMultiplePresent {
    public static void main(String[] args) throws Exception {
        Utils.setLibrary(args);
        TerminalFactory factory = TerminalFactory.getInstance("PC/SC", null);
        System.out.println(factory);
        CardTerminals terminals = factory.terminals();
        List<CardTerminal> list = terminals.list();
        System.out.println("Terminals: " + list);
        boolean multipleReaders = true;
        if (list.size() < 2) {
            if (list.isEmpty()) {
                throw new Exception("no terminals");
            }
            System.out.println("Only one reader present, using simplified test");
            multipleReaders = false;
        }
        while (true) {
            boolean present = false;
            for (CardTerminal terminal : list) {
                present |= terminal.isCardPresent();
            }
            if (present == false) {
                break;
            }
            System.out.println("*** Remove all cards!");
            Thread.sleep(1000);
        }
        System.out.println("OK");
        List<CardTerminal> result;
        result = terminals.list(CARD_PRESENT);
        if (result.size() != 0) {
            throw new Exception("List not empty: " + result);
        }
        if (terminals.waitForChange(1000)) {
            throw new Exception("no timeout");
        }
        if (terminals.waitForChange(1000)) {
            throw new Exception("no timeout");
        }
        result = terminals.list(CARD_PRESENT);
        if (result.size() != 0) {
            throw new Exception("List not empty: " + result);
        }
        System.out.println("*** Insert card!");
        terminals.waitForChange();
        result = terminals.list(CARD_INSERTION);
        System.out.println(result);
        if (result.size() != 1) {
            throw new Exception("no card present");
        }
        CardTerminal t1 = result.get(0);
        result = terminals.list(CARD_INSERTION);
        System.out.println(result);
        if ((result.size() != 1) || (result.get(0) != t1)) {
            throw new Exception("no card present");
        }
        if (terminals.list(CARD_REMOVAL).size() != 0) {
            throw new Exception("List not empty: " + result);
        }
        if (terminals.list(CARD_REMOVAL).size() != 0) {
            throw new Exception("List not empty: " + result);
        }
        if (terminals.waitForChange(1000)) {
            throw new Exception("no timeout");
        }
        if (terminals.list(CARD_REMOVAL).size() != 0) {
            throw new Exception("List not empty: " + result);
        }
        if (multipleReaders) {
            System.out.println("*** Insert card into other reader!");
            terminals.waitForChange();
            result = terminals.list(CARD_INSERTION);
            System.out.println(result);
            if (result.size() != 1) {
                throw new Exception("no card present");
            }
            CardTerminal t2 = result.get(0);
            if (t1.getName().equals(t2.getName())) {
                throw new Exception("same terminal");
            }
            System.out.println("*** Remove card from 2nd reader!");
            terminals.waitForChange();
            if (terminals.list(CARD_INSERTION).size() != 0) {
                throw new Exception("List not empty: " + result);
            }
            result = terminals.list(CARD_REMOVAL);
            if ((result.size() != 1) || (result.get(0) != t2)) {
                throw new Exception("no or wrong terminal");
            }
        }
        System.out.println("*** Remove card from 1st reader!");
        terminals.waitForChange();
        if (terminals.list(CARD_INSERTION).size() != 0) {
            throw new Exception("List not empty: " + result);
        }
        result = terminals.list(CARD_REMOVAL);
        if ((result.size() != 1) || (result.get(0) != t1)) {
            throw new Exception("no or wrong terminal");
        }
        System.out.println("OK.");
    }
}
