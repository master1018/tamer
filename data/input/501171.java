class LoadedClass implements Serializable, Comparable<LoadedClass> {
    private static final long serialVersionUID = 0;
    final String name;
    final List<Operation> loads = new ArrayList<Operation>();
    final List<Operation> initializations = new ArrayList<Operation>();
    MemoryUsage memoryUsage = MemoryUsage.NOT_AVAILABLE;
    final boolean systemClass;
    boolean preloaded;
    LoadedClass(String name, boolean systemClass) {
        this.name = name;
        this.systemClass = systemClass;
    }
    void measureMemoryUsage() {
        this.memoryUsage = MemoryUsage.forClass(name);
    }
    int mlt = -1;
    int medianLoadTimeMicros() {
        if (mlt != -1) {
            return mlt;
        }
        return mlt = calculateMedian(loads);
    }
    int mit = -1;
    int medianInitTimeMicros() {
        if (mit != -1) {
            return mit;
        }
        return mit = calculateMedian(initializations);
    }
    int medianTimeMicros() {
        return medianInitTimeMicros() + medianLoadTimeMicros();
    }
    private static int calculateMedian(List<Operation> operations) {
        int size = operations.size();
        if (size == 0) {
            return 0;
        }
        int[] times = new int[size];
        for (int i = 0; i < size; i++) {
            times[i] = operations.get(i).exclusiveTimeMicros();
        }
        Arrays.sort(times);
        int middle = size / 2;
        if (size % 2 == 1) {
            return times[middle];
        } else {
            return (times[middle - 1] + times[middle]) / 2;
        }
    }
    Set<String> processNames() {
        Set<String> names = new HashSet<String>();
        addProcessNames(loads, names);
        addProcessNames(initializations, names);
        return names;
    }
    private void addProcessNames(List<Operation> ops, Set<String> names) {
        for (Operation operation : ops) {
            if (operation.process.fromZygote()) {
                names.add(operation.process.name);
            }
        }
    }
    public int compareTo(LoadedClass o) {
        return name.compareTo(o.name);
    }
    @Override
    public String toString() {
        return name;
    }
}
