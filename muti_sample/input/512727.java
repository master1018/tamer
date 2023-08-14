public class MethodLogger {
    private List<String> methods = new ArrayList<String>();
    private List<Object[]> argLists = new ArrayList<Object[]>();
    public void add(String method, Object ... args) {
        Object[] argsCopy = new Object[args.length];
        System.arraycopy(args, 0, argsCopy, 0, args.length);
        methods.add(method);
        argLists.add(argsCopy);
    }
    public int size() {
        return methods.size();
    }
    public String getMethod(int index) {
        return methods.get(index);
    }
    public String getMethod() {
        return (size() == 0 ? null : getMethod(size() - 1));
    }
    public Object[] getArgs(int index) {
        return argLists.get(index);
    }
    public Object[] getArgs() {
        return (size() == 0 ? null : getArgs(size() - 1));
    }
    public void clear() {
        methods.clear();
        argLists.clear();
    }
}
