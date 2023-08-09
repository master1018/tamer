public class ConcurrentLocksPrinter {
    private Map locksMap = new HashMap(); 
    public ConcurrentLocksPrinter() {
        fillLocks();
    }
    public void print(JavaThread jthread, PrintStream tty) {
        List locks = (List) locksMap.get(jthread);
        tty.println("Locked ownable synchronizers:");
        if (locks == null || locks.isEmpty()) {
            tty.println("    - None");
        } else {
            for (Iterator itr = locks.iterator(); itr.hasNext();) {
                Oop oop = (Oop) itr.next();
                tty.println("    - <" + oop.getHandle() + ">, (a " +
                       oop.getKlass().getName().asString() + ")");
            }
        }
    }
    private JavaThread getOwnerThread(Oop oop) {
        Oop threadOop = OopUtilities.abstractOwnableSynchronizerGetOwnerThread(oop);
        if (threadOop == null) {
            return null;
        } else {
            return OopUtilities.threadOopGetJavaThread(threadOop);
        }
    }
    private void fillLocks() {
        VM vm = VM.getVM();
        SystemDictionary sysDict = vm.getSystemDictionary();
        Klass absOwnSyncKlass = sysDict.getAbstractOwnableSynchronizerKlass();
        ObjectHeap heap = vm.getObjectHeap();
        if (absOwnSyncKlass != null) {
            heap.iterateObjectsOfKlass(new DefaultHeapVisitor() {
                    public boolean doObj(Oop oop) {
                        JavaThread thread = getOwnerThread(oop);
                        if (thread != null) {
                            List locks = (List) locksMap.get(thread);
                            if (locks == null) {
                                locks = new LinkedList();
                                locksMap.put(thread, locks);
                            }
                            locks.add(oop);
                        }
                        return false;
                    }
                }, absOwnSyncKlass, true);
        }
    }
}
