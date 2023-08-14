final class RuntimeMemorySpy extends AbstractMemorySpy {
    public RuntimeMemorySpy() {
        super();
    }
    public void alloc(PlatformAddress address) {
        Reference ref = notifyQueue.poll(); 
        while (ref != null) {
            orphanedMemory(ref);
            ref = notifyQueue.poll();
        }
        super.alloc(address);
    }
}
