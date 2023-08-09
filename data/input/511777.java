public class Root implements Serializable {
    private static final long serialVersionUID = 0;
    final Map<Integer, Proc> processes = new HashMap<Integer, Proc>();
    final Map<String, LoadedClass> loadedClasses
            = new HashMap<String, LoadedClass>();
    MemoryUsage baseline = MemoryUsage.baseline();
    void indexClassOperation(Record record) {
        Proc process = processes.get(record.pid);
        if (record.processName.equals("dexopt")) {
            return;
        }
        String name = record.className;
        LoadedClass loadedClass = loadedClasses.get(name);
        Operation o = null;
        switch (record.type) {
            case START_LOAD:
            case START_INIT:
                if (loadedClass == null) {
                    loadedClass = new LoadedClass(
                            name, record.classLoader == 0);
                    if (loadedClass.systemClass) {
                        loadedClass.measureMemoryUsage();
                    }
                    loadedClasses.put(name, loadedClass);
                }
                break;
            case END_LOAD:
            case END_INIT:
                o = process.endOperation(record.tid, record.className,
                        loadedClass, record.time);
                if (o == null) {
                    return;
                }
        }
        switch (record.type) {
            case START_LOAD:
                process.startOperation(record.tid, loadedClass, record.time,
                        Operation.Type.LOAD);
                break;
            case START_INIT:
                process.startOperation(record.tid, loadedClass, record.time,
                        Operation.Type.INIT);
                break;
            case END_LOAD:
                loadedClass.loads.add(o);
                break;
            case END_INIT:
                loadedClass.initializations.add(o);
                break;
        }
    }
    void indexProcess(Record record) {
        Proc proc = processes.get(record.pid);
        if (proc == null) {
            Proc parent = processes.get(record.ppid);
            proc = new Proc(parent, record.pid);
            processes.put(proc.id, proc);
            if (parent != null) {
                parent.children.add(proc);
            }
        }
        proc.setName(record.processName);
    }
    void toFile(String fileName) throws IOException {
        FileOutputStream out = new FileOutputStream(fileName);
        ObjectOutputStream oout = new ObjectOutputStream(
                new BufferedOutputStream(out));
        System.err.println("Writing object model...");
        oout.writeObject(this);
        oout.close();
        System.err.println("Done!");
    }
    static Root fromFile(String fileName)
            throws IOException, ClassNotFoundException {
        FileInputStream fin = new FileInputStream(fileName);
        ObjectInputStream oin = new ObjectInputStream(
                new BufferedInputStream(fin));
        Root root = (Root) oin.readObject();
        oin.close();
        return root;
    }
}
