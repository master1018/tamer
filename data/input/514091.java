class Proc implements Serializable {
    private static final long serialVersionUID = 0;
    final Proc parent;
    final int id;
    String name;
    final List<Proc> children = new ArrayList<Proc>();
    transient final Map<Integer, LinkedList<Operation>> stacks
            = new HashMap<Integer, LinkedList<Operation>>();
    int operationCount;
    final List<Operation> operations = new ArrayList<Operation>();
    final List<String> nameHistory = new ArrayList<String>();
    Proc(Proc parent, int id) {
        this.parent = parent;
        this.id = id;
    }
    void setName(String name) {
        if (!name.equals(this.name)) {
            if (this.name != null) {
                nameHistory.add(this.name);
            }
            this.name = name;
        }
    }
    public boolean fromZygote() {
        return parent != null && parent.name.equals("zygote")
                && !name.equals("com.android.development");
    }
    void startOperation(int threadId, LoadedClass loadedClass, long time,
            Operation.Type type) {
        Operation o = new Operation(
                this, loadedClass, time, operationCount++, type);
        operations.add(o);
        LinkedList<Operation> stack = stacks.get(threadId);
        if (stack == null) {
            stack = new LinkedList<Operation>();
            stacks.put(threadId, stack);
        }
        if (!stack.isEmpty()) {
            stack.getLast().subops.add(o);
        }
        stack.add(o);
    }
    Operation endOperation(int threadId, String className,
            LoadedClass loadedClass, long time) {
        LinkedList<Operation> stack = stacks.get(threadId);
        if (stack == null || stack.isEmpty()) {
            didNotStart(className);
            return null;
        }
        Operation o = stack.getLast();
        if (loadedClass != o.loadedClass) {
            didNotStart(className);
            return null;
        }
        stack.removeLast();
        o.endTimeNanos = time;
        return o;
    }
    private static void didNotStart(String name) {
        System.err.println("Warning: An operation ended on " + name
            + " but it never started!");
    }
    void print() {
        print("");
    }
    private void print(String prefix) {
        System.out.println(prefix + "id=" + id + ", name=" + name);
        for (Proc child : children) {
            child.print(prefix + "    ");
        }
    }
    @Override
    public String toString() {
        return this.name;
    }
}
