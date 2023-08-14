public final class ClassDataItem extends OffsettedItem {
    private final CstType thisClass;
    private final ArrayList<EncodedField> staticFields;
    private final HashMap<EncodedField, Constant> staticValues;
    private final ArrayList<EncodedField> instanceFields;
    private final ArrayList<EncodedMethod> directMethods;
    private final ArrayList<EncodedMethod> virtualMethods;
    private CstArray staticValuesConstant;
    private byte[] encodedForm;
    public ClassDataItem(CstType thisClass) {
        super(1, -1);
        if (thisClass == null) {
            throw new NullPointerException("thisClass == null");
        }
        this.thisClass = thisClass;
        this.staticFields = new ArrayList<EncodedField>(20);
        this.staticValues = new HashMap<EncodedField, Constant>(40);
        this.instanceFields = new ArrayList<EncodedField>(20);
        this.directMethods = new ArrayList<EncodedMethod>(20);
        this.virtualMethods = new ArrayList<EncodedMethod>(20);
        this.staticValuesConstant = null;
    }
    @Override
    public ItemType itemType() {
        return ItemType.TYPE_CLASS_DATA_ITEM;
    }
    @Override
    public String toHuman() {
        return toString();
    }
    public boolean isEmpty() {
        return staticFields.isEmpty() && instanceFields.isEmpty()
            && directMethods.isEmpty() && virtualMethods.isEmpty();
    }
    public void addStaticField(EncodedField field, Constant value) {
        if (field == null) {
            throw new NullPointerException("field == null");
        }
        if (staticValuesConstant != null) {
            throw new UnsupportedOperationException(
                    "static fields already sorted");
        }
        staticFields.add(field);
        staticValues.put(field, value);
    }
    public void addInstanceField(EncodedField field) {
        if (field == null) {
            throw new NullPointerException("field == null");
        }
        instanceFields.add(field);
    }
    public void addDirectMethod(EncodedMethod method) {
        if (method == null) {
            throw new NullPointerException("method == null");
        }
        directMethods.add(method);
    }
    public void addVirtualMethod(EncodedMethod method) {
        if (method == null) {
            throw new NullPointerException("method == null");
        }
        virtualMethods.add(method);
    }
    public ArrayList<EncodedMethod> getMethods() {
        int sz = directMethods.size() + virtualMethods.size();
        ArrayList<EncodedMethod> result = new ArrayList<EncodedMethod>(sz);
        result.addAll(directMethods);
        result.addAll(virtualMethods);
        return result;
    }
    public void debugPrint(Writer out, boolean verbose) {
        PrintWriter pw = Writers.printWriterFor(out);
        int sz = staticFields.size();
        for (int i = 0; i < sz; i++) {
            pw.println("  sfields[" + i + "]: " + staticFields.get(i));
        }
        sz = instanceFields.size();
        for (int i = 0; i < sz; i++) {
            pw.println("  ifields[" + i + "]: " + instanceFields.get(i));
        }
        sz = directMethods.size();
        for (int i = 0; i < sz; i++) {
            pw.println("  dmeths[" + i + "]:");
            directMethods.get(i).debugPrint(pw, verbose);
        }
        sz = virtualMethods.size();
        for (int i = 0; i < sz; i++) {
            pw.println("  vmeths[" + i + "]:");
            virtualMethods.get(i).debugPrint(pw, verbose);
        }
    }
    @Override
    public void addContents(DexFile file) {
        if (!staticFields.isEmpty()) {
            getStaticValuesConstant(); 
            for (EncodedField field : staticFields) {
                field.addContents(file);
            }
        }
        if (!instanceFields.isEmpty()) {
            Collections.sort(instanceFields);
            for (EncodedField field : instanceFields) {
                field.addContents(file);
            }
        }
        if (!directMethods.isEmpty()) {
            Collections.sort(directMethods);
            for (EncodedMethod method : directMethods) {
                method.addContents(file);
            }
        }
        if (!virtualMethods.isEmpty()) {
            Collections.sort(virtualMethods);
            for (EncodedMethod method : virtualMethods) {
                method.addContents(file);
            }
        }
    }
    public CstArray getStaticValuesConstant() {
        if ((staticValuesConstant == null) && (staticFields.size() != 0)) {
            staticValuesConstant = makeStaticValuesConstant();
        }
        return staticValuesConstant;
    }
    private CstArray makeStaticValuesConstant() {
        Collections.sort(staticFields);
        int size = staticFields.size();
        while (size > 0) {
            EncodedField field = staticFields.get(size - 1);
            Constant cst = staticValues.get(field);
            if (cst instanceof CstLiteralBits) {
                if (((CstLiteralBits) cst).getLongBits() != 0) {
                    break;
                }
            } else if (cst != null) {
                break;
            }
            size--;
        }
        if (size == 0) {
            return null;
        }
        CstArray.List list = new CstArray.List(size);
        for (int i = 0; i < size; i++) {
            EncodedField field = staticFields.get(i);
            Constant cst = staticValues.get(field);
            if (cst == null) {
                cst = Zeroes.zeroFor(field.getRef().getType());
            }
            list.set(i, cst);
        }
        list.setImmutable();
        return new CstArray(list);
    }
    @Override
    protected void place0(Section addedTo, int offset) {
        ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput();
        encodeOutput(addedTo.getFile(), out);
        encodedForm = out.toByteArray();
        setWriteSize(encodedForm.length);
    }
    private void encodeOutput(DexFile file, AnnotatedOutput out) {
        boolean annotates = out.annotates();
        if (annotates) {
            out.annotate(0, offsetString() + " class data for " +
                    thisClass.toHuman());
        }
        encodeSize(file, out, "static_fields", staticFields.size());
        encodeSize(file, out, "instance_fields", instanceFields.size());
        encodeSize(file, out, "direct_methods", directMethods.size());
        encodeSize(file, out, "virtual_methods", virtualMethods.size());
        encodeList(file, out, "static_fields", staticFields);
        encodeList(file, out, "instance_fields", instanceFields);
        encodeList(file, out, "direct_methods", directMethods);
        encodeList(file, out, "virtual_methods", virtualMethods);
        if (annotates) {
            out.endAnnotation();
        }
    }
    private static void encodeSize(DexFile file, AnnotatedOutput out,
            String label, int size) {
        if (out.annotates()) {
            out.annotate(String.format("  %-21s %08x", label + "_size:",
                            size));
        }
        out.writeUnsignedLeb128(size);
    }
    private static void encodeList(DexFile file, AnnotatedOutput out,
            String label, ArrayList<? extends EncodedMember> list) {
        int size = list.size();
        int lastIndex = 0;
        if (size == 0) {
            return;
        }
        if (out.annotates()) {
            out.annotate(0, "  " + label + ":");
        }
        for (int i = 0; i < size; i++) {
            lastIndex = list.get(i).encode(file, out, lastIndex, i);
        }
    }
    @Override
    public void writeTo0(DexFile file, AnnotatedOutput out) {
        boolean annotates = out.annotates();
        if (annotates) {
            encodeOutput(file, out);
        } else {
            out.write(encodedForm);
        }
    }
}
