public final class ProtoIdItem extends IndexedItem {
    public static final int WRITE_SIZE = 12;
    private final Prototype prototype;
    private final CstUtf8 shortForm;
    private TypeListItem parameterTypes;
    public ProtoIdItem(Prototype prototype) {
        if (prototype == null) {
            throw new NullPointerException("prototype == null");
        }
        this.prototype = prototype;
        this.shortForm = makeShortForm(prototype);
        StdTypeList parameters = prototype.getParameterTypes();
        this.parameterTypes = (parameters.size() == 0) ? null 
            : new TypeListItem(parameters);
    }
    private static CstUtf8 makeShortForm(Prototype prototype) {
        StdTypeList parameters = prototype.getParameterTypes();
        int size = parameters.size();
        StringBuilder sb = new StringBuilder(size + 1);
        sb.append(shortFormCharFor(prototype.getReturnType()));
        for (int i = 0; i < size; i++) {
            sb.append(shortFormCharFor(parameters.getType(i)));
        }
        return new CstUtf8(sb.toString());
    }
    private static char shortFormCharFor(Type type) {
        char descriptorChar = type.getDescriptor().charAt(0);
        if (descriptorChar == '[') {
            return 'L';
        }
        return descriptorChar;
    }
    @Override
    public ItemType itemType() {
        return ItemType.TYPE_PROTO_ID_ITEM;
    }
    @Override
    public int writeSize() {
        return WRITE_SIZE;
    }
    @Override
    public void addContents(DexFile file) {
        StringIdsSection stringIds = file.getStringIds();
        TypeIdsSection typeIds = file.getTypeIds();
        MixedItemSection typeLists = file.getTypeLists();
        typeIds.intern(prototype.getReturnType());
        stringIds.intern(shortForm);
        if (parameterTypes != null) {
            parameterTypes = typeLists.intern(parameterTypes);
        }
    }
    @Override
    public void writeTo(DexFile file, AnnotatedOutput out) {
        int shortyIdx = file.getStringIds().indexOf(shortForm);
        int returnIdx = file.getTypeIds().indexOf(prototype.getReturnType());
        int paramsOff = OffsettedItem.getAbsoluteOffsetOr0(parameterTypes);
        if (out.annotates()) {
            StringBuilder sb = new StringBuilder();
            sb.append(prototype.getReturnType().toHuman());
            sb.append(" proto(");
            StdTypeList params = prototype.getParameterTypes();
            int size = params.size();
            for (int i = 0; i < size; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(params.getType(i).toHuman());
            }
            sb.append(")");
            out.annotate(0, indexString() + ' ' + sb.toString());
            out.annotate(4, "  shorty_idx:      " + Hex.u4(shortyIdx) +
                    " 
            out.annotate(4, "  return_type_idx: " + Hex.u4(returnIdx) +
                    " 
            out.annotate(4, "  parameters_off:  " + Hex.u4(paramsOff));
        }
        out.writeInt(shortyIdx);
        out.writeInt(returnIdx);
        out.writeInt(paramsOff);
    }
}
