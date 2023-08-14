public class test {
    public void addPort(InputPort<?> port) {
        this.inputs.put(port.getChannel().getId(), port);
    }
}
