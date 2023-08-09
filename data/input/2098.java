public class IllegalConnectorArgumentsException extends Exception
{
    List<String> names;
    public IllegalConnectorArgumentsException(String s,
                                              String name) {
        super(s);
        names = new ArrayList<String>(1);
        names.add(name);
    }
    public IllegalConnectorArgumentsException(String s, List<String> names) {
        super(s);
        this.names = new ArrayList<String>(names);
    }
    public List<String> argumentNames() {
        return Collections.unmodifiableList(names);
    }
}
