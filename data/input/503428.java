public class Queries {
    private static final String DEFAULT_PACKAGE = "<default>";
    public static Map<String, Set<ClassObj>> allClasses(State state) {
        return classes(state, null);
    }
    public static Map<String, Set<ClassObj>> classes(State state, 
            String[] excludedPrefixes) {
        TreeMap<String, Set<ClassObj>> result =
        new TreeMap<String, Set<ClassObj>>();
        Set<ClassObj> classes = new TreeSet<ClassObj>();
        for (Heap heap: state.mHeaps.values()) {
            classes.addAll(heap.mClassesById.values());
        }
        if (excludedPrefixes != null) {
            final int N = excludedPrefixes.length;
            Iterator<ClassObj> iter = classes.iterator();
            while (iter.hasNext()) {
                ClassObj theClass = iter.next();
                String classPath = theClass.toString();
                for (int i = 0; i < N; i++) {
                    if (classPath.startsWith(excludedPrefixes[i])) {
                        iter.remove();
                        break;
                    }
                }
            }
        }
        for (ClassObj theClass: classes) {
            String packageName = DEFAULT_PACKAGE;
            int lastDot = theClass.mClassName.lastIndexOf('.');
            if (lastDot != -1) {
                packageName = theClass.mClassName.substring(0, lastDot);
            }
            Set<ClassObj> classSet = result.get(packageName);
            if (classSet == null) {
                classSet = new TreeSet<ClassObj>();
                result.put(packageName, classSet);
            }
            classSet.add(theClass);
        }
        return result;
    }
    public static ClassObj findClass(State state, String name) {
        return state.findClass(name);
    }
     public static Instance[] instancesOf(State state, String baseClassName) {
         ClassObj theClass = state.findClass(baseClassName);
         if (theClass == null) {
             throw new IllegalArgumentException("Class not found: "
                + baseClassName);
         }
         Instance[] instances = new Instance[theClass.mInstances.size()];
         return theClass.mInstances.toArray(instances);
     }
    public static Instance[] allInstancesOf(State state, String baseClassName) {
        ClassObj theClass = state.findClass(baseClassName);
        if (theClass == null) {
            throw new IllegalArgumentException("Class not found: "
                + baseClassName);
        }
        ArrayList<ClassObj> classList = new ArrayList<ClassObj>();
        classList.add(theClass);
        classList.addAll(traverseSubclasses(theClass));
        ArrayList<Instance> instanceList = new ArrayList<Instance>();
        for (ClassObj someClass: classList) {
            instanceList.addAll(someClass.mInstances);
        }
        Instance[] result = new Instance[instanceList.size()];
        instanceList.toArray(result);
        return result;
    }
    private static ArrayList<ClassObj> traverseSubclasses(ClassObj base) {
        ArrayList<ClassObj> result = new ArrayList<ClassObj>();
        for (ClassObj subclass: base.mSubclasses) {
            result.add(subclass);
            result.addAll(traverseSubclasses(subclass));
        }
        return result;
    }
    public static Instance findObject(State state, String id) {
        long id2 = Long.parseLong(id, 16);
        return state.findReference(id2);
    }
    public static Collection<RootObj> getRoots(State state) {
        HashSet<RootObj> result = new HashSet<RootObj>();
        for (Heap heap: state.mHeaps.values()) {
            result.addAll(heap.mRoots);
        }
        return result;
    }
    public static final Instance[] newInstances(State older, State newer) {
        ArrayList<Instance> resultList = new ArrayList<Instance>();
        for (Heap newHeap: newer.mHeaps.values()) {
            Heap oldHeap = older.getHeap(newHeap.mName);
            if (oldHeap == null) {
                continue;
            }
            for (Instance instance: newHeap.mInstances.values()) {
                Instance oldInstance = oldHeap.getInstance(instance.mId);
                if ((oldInstance == null)
                        || (instance.mClassId != oldInstance.mClassId)) {
                    resultList.add(instance);
                }
            }
        }
        Instance[] resultArray = new Instance[resultList.size()];
        return resultList.toArray(resultArray);
    }
}
