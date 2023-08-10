public class TurnOnAction extends Action {
    private Node node;
    public TurnOnAction(Node node) {
        super(0);
        this.node = node;
    }
    public void execute() throws Throwable {
        node.turnOn();
    }
}
