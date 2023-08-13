public class JavaObjectRef extends JavaThing {
    private long id;
    public JavaObjectRef(long id) {
        this.id = id;
    }
    public long getId() {
        return id;
    }
    public boolean isHeapAllocated() {
        return true;
    }
    public JavaThing dereference(Snapshot snapshot, JavaField field) {
        return dereference(snapshot, field, true);
    }
    public JavaThing dereference(Snapshot snapshot, JavaField field, boolean verbose) {
        if (field != null && !field.hasId()) {
            return new JavaLong(id);
        }
        if (id == 0) {
            return snapshot.getNullThing();
        }
        JavaThing result = snapshot.findThing(id);
        if (result == null) {
            if (!snapshot.getUnresolvedObjectsOK() && verbose) {
                String msg = "WARNING:  Failed to resolve object id "
                                + Misc.toHex(id);
                if (field != null) {
                    msg += " for field " + field.getName()
                            + " (signature " + field.getSignature() + ")";
                }
                System.out.println(msg);
            }
            result = new HackJavaValue("Unresolved object "
                                        + Misc.toHex(id), 0);
        }
        return result;
    }
    public int getSize() {
        return 0;
    }
    public String toString() {
        return "Unresolved object " + Misc.toHex(id);
    }
}
