public class KSHell extends Process {
    private UserInterface userInterface;
    private int commandIndex;
    private ArrayList<String> commandHistory;
    public int getCommandIndex() {
        return commandIndex;
    }
    public void incCommandIndex() {
        commandIndex++;
    }
    public void decCommandIndex() {
        commandIndex--;
    }
    public ArrayList<String> getCommandHistory() {
        return commandHistory;
    }
    public void setUserInterface(UserInterface ui) {
        this.userInterface = ui;
    }
    public UserInterface getUserInterface() {
        return this.userInterface;
    }
    public void processLine(String line) {
        if (!line.equals("")) {
            commandHistory.add(line);
            commandIndex = commandHistory.size();
        } else {
            return;
        }
        if (line.contains("kshell")) {
            if (line.equals("kshell")) ProcessManager.instance().createShell(getUserInterface(), this.getPID()); else this.getErr().stdWriteln("Error: kshell can not be piped!");
            return;
        }
        if (line.contains("exit")) {
            if (line.equals("exit")) processSignal(0); else this.getErr().stdWriteln("Error: exit can not be piped!");
            return;
        }
        OSVM_grammarLexer lex = new OSVM_grammarLexer(new ANTLRStringStream(line));
        CommonTokenStream tokens = new CommonTokenStream(lex);
        OSVM_grammarParser g = new OSVM_grammarParser(tokens);
        try {
            g.parse();
        } catch (RecognitionException e) {
            this.getErr().stdWriteln("Warning: Mismatched input!");
            this.getErr().stdWriteln("Usage: cmd args < in [ | cmd next]* > out");
            return;
        }
        if (g.containsInvalid()) {
            this.getErr().stdWriteln("Warning: Command contains invalid symbols!");
        }
        ProcessManager.instance().createProcess(this, userInterface, g);
    }
    public void initShell() {
        this.setIn(userInterface);
        this.setOut(userInterface);
        this.setErr(userInterface);
        this.setWorkingDir(new File(""));
        commandIndex = -1;
        commandHistory = new ArrayList<String>();
    }
    @Override
    public void tick() {
        initShell();
    }
    @Override
    public void processSignal(int type) {
        switch(type) {
            case 0:
                this.getOut().stdWriteln("Good bye :-)");
                if (this.getParent().getPID() == 1 && ProcessManager.instance().getLastShell(getUserInterface().getUser()).equals(this)) {
                    getUserInterface().close();
                } else {
                    while (this.getAllChilds().size() > 0) {
                        this.getChild(this.getAllChilds().firstKey()).setParent(this.getParent());
                        this.getParent().addChild(this.getChild(this.getAllChilds().firstKey()));
                        this.removeChild(this.getAllChilds().firstKey());
                    }
                    this.getIn().stdCloseIn();
                    this.getOut().stdCloseOut();
                    this.getParent().removeChild(this.getPID());
                    ProcessManager.instance().removeProcess(this.getPID());
                }
                break;
            default:
                break;
        }
    }
}
