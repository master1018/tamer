public class Todo extends AbstractQueue<Env<AttrContext>> {
    protected static final Context.Key<Todo> todoKey =
        new Context.Key<Todo>();
    public static Todo instance(Context context) {
        Todo instance = context.get(todoKey);
        if (instance == null)
            instance = new Todo(context);
        return instance;
    }
    protected Todo(Context context) {
        context.put(todoKey, this);
    }
    public void append(Env<AttrContext> env) {
        add(env);
    }
    @Override
    public Iterator<Env<AttrContext>> iterator() {
        return contents.iterator();
    }
    @Override
    public int size() {
        return contents.size();
    }
    public boolean offer(Env<AttrContext> e) {
        if (contents.add(e)) {
            if (contentsByFile != null)
                addByFile(e);
            return true;
        } else {
            return false;
        }
    }
    public Env<AttrContext> poll() {
        if (size() == 0)
            return null;
        Env<AttrContext> env = contents.remove(0);
        if (contentsByFile != null)
            removeByFile(env);
        return env;
    }
    public Env<AttrContext> peek() {
        return (size() == 0 ? null : contents.get(0));
    }
    public Queue<Queue<Env<AttrContext>>> groupByFile() {
        if (contentsByFile == null) {
            contentsByFile = new LinkedList<Queue<Env<AttrContext>>>();
            for (Env<AttrContext> env: contents) {
                addByFile(env);
            }
        }
        return contentsByFile;
    }
    private void addByFile(Env<AttrContext> env) {
        JavaFileObject file = env.toplevel.sourcefile;
        if (fileMap == null)
            fileMap = new HashMap<JavaFileObject, FileQueue>();
        FileQueue fq = fileMap.get(file);
        if (fq == null) {
            fq = new FileQueue();
            fileMap.put(file, fq);
            contentsByFile.add(fq);
        }
        fq.fileContents.add(env);
    }
    private void removeByFile(Env<AttrContext> env) {
        JavaFileObject file = env.toplevel.sourcefile;
        FileQueue fq = fileMap.get(file);
        if (fq == null)
            return;
        if (fq.fileContents.remove(env)) {
            if (fq.isEmpty()) {
                fileMap.remove(file);
                contentsByFile.remove(fq);
            }
        }
    }
    LinkedList<Env<AttrContext>> contents = new LinkedList<Env<AttrContext>>();
    LinkedList<Queue<Env<AttrContext>>> contentsByFile;
    Map<JavaFileObject, FileQueue> fileMap;
    class FileQueue extends AbstractQueue<Env<AttrContext>> {
        @Override
        public Iterator<Env<AttrContext>> iterator() {
            return fileContents.iterator();
        }
        @Override
        public int size() {
            return fileContents.size();
        }
        public boolean offer(Env<AttrContext> e) {
            if (fileContents.offer(e)) {
                contents.add(e);
                return true;
            }
            return false;
        }
        public Env<AttrContext> poll() {
            if (fileContents.size() == 0)
                return null;
            Env<AttrContext> env = fileContents.remove(0);
            contents.remove(env);
            return env;
        }
        public Env<AttrContext> peek() {
            return (fileContents.size() == 0 ? null : fileContents.get(0));
        }
        LinkedList<Env<AttrContext>> fileContents = new LinkedList<Env<AttrContext>>();
    }
}
