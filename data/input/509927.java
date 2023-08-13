abstract class AbstractMemorySpy implements IMemorySpy {
    protected Map<PlatformAddress,AddressWrapper> memoryInUse = new HashMap<PlatformAddress, AddressWrapper>(); 
    protected Map<Reference,PlatformAddress> refToShadow = new HashMap<Reference, PlatformAddress>(); 
    protected ReferenceQueue<Object> notifyQueue = new ReferenceQueue<Object>();
       final class AddressWrapper {
        final PlatformAddress shadow;
        final PhantomReference<PlatformAddress> wrAddress;
        volatile boolean autoFree = false;
        AddressWrapper(PlatformAddress address) {
            super();
            this.shadow = address.duplicate();
            this.wrAddress = new PhantomReference<PlatformAddress>(address, notifyQueue);
        }
    }
    public AbstractMemorySpy() {
        super();
    }
    public void alloc(PlatformAddress address) {
        AddressWrapper wrapper = new AddressWrapper(address);
        synchronized (this) {
            memoryInUse.put(wrapper.shadow, wrapper);
            refToShadow.put(wrapper.wrAddress, wrapper.shadow);
        }
    }
    public boolean free(PlatformAddress address) {
        AddressWrapper wrapper;
        synchronized (this) {
            wrapper = memoryInUse.remove(address);
            if (wrapper != null) {
                refToShadow.remove(wrapper.wrAddress);
            }
		}
        if (wrapper == null) {
            System.err
                    .println("Memory Spy! Fixed attempt to free memory that was not allocated " + address); 
        }
        return wrapper != null;
    }
    public void rangeCheck(PlatformAddress address, int offset, int length)
            throws IndexOutOfBoundsException {
    }
    public void autoFree(PlatformAddress address) {
        AddressWrapper wrapper;
        synchronized (this) {
            wrapper = memoryInUse.get(address);
        }
        if (wrapper != null) {
            wrapper.autoFree = true;
        }
    }
    protected void orphanedMemory(Reference ref) {
        AddressWrapper wrapper;
        synchronized (this) {
            PlatformAddress shadow = refToShadow.remove(ref);
            wrapper = memoryInUse.get(shadow);
            if (wrapper != null) {
                if (!wrapper.autoFree) {
                    System.err
                            .println("Memory Spy! Fixed memory leak by freeing " + wrapper.shadow); 
                }
                wrapper.shadow.free();
            }
        }
        ref.clear();
    }
}
