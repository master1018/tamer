public class ModificationWatchpointSpec extends WatchpointSpec {
    ModificationWatchpointSpec(EventRequestSpecList specs,
                         ReferenceTypeSpec refSpec, String fieldId) {
        super(specs, refSpec,  fieldId);
    }
    @Override
    void resolve(ReferenceType refType) throws InvalidTypeException,
                                             NoSuchFieldException {
        if (!(refType instanceof ClassType)) {
            throw new InvalidTypeException();
        }
        Field field = refType.fieldByName(fieldId);
        if (field == null) {
            throw new NoSuchFieldException(fieldId);
        }
        setRequest(refType.virtualMachine().eventRequestManager()
                   .createModificationWatchpointRequest(field));
    }
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ModificationWatchpointSpec) &&
            super.equals(obj);
    }
}
