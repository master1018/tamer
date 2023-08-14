class VMAction extends EventObject {
    static final int VM_SUSPENDED = 1;
    static final int VM_NOT_SUSPENDED = 2;
    int id;
    ThreadReference resumingThread;
    VMAction(VirtualMachine vm, int id) {
        this(vm, null, id);
    }
     VMAction(VirtualMachine vm,  ThreadReference resumingThread, int id) {
        super(vm);
        this.id = id;
        this.resumingThread = resumingThread;
    }
    VirtualMachine vm() {
        return (VirtualMachine)getSource();
    }
    int id() {
        return id;
    }
    ThreadReference resumingThread() {
        return resumingThread;
    }
}
