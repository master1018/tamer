public class ListConnectors {
    public void list() {
        List<Connector> l = Bootstrap.virtualMachineManager().allConnectors();
        for(Connector c: l) {
            System.out.println(c.name());
        }
    }
}
