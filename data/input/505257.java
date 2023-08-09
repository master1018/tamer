public final class AnnotationsDirectoryItem extends OffsettedItem {
    private static final int ALIGNMENT = 4;
    private static final int HEADER_SIZE = 16;
    private static final int ELEMENT_SIZE = 8;
    private AnnotationSetItem classAnnotations;
    private ArrayList<FieldAnnotationStruct> fieldAnnotations;
    private ArrayList<MethodAnnotationStruct> methodAnnotations;
    private ArrayList<ParameterAnnotationStruct> parameterAnnotations;
    public AnnotationsDirectoryItem() {
        super(ALIGNMENT, -1);
        classAnnotations = null;
        fieldAnnotations = null;
        methodAnnotations = null;
        parameterAnnotations = null;
    }
    @Override
    public ItemType itemType() {
        return ItemType.TYPE_ANNOTATIONS_DIRECTORY_ITEM;
    }
    public boolean isEmpty() {
        return (classAnnotations == null) &&
            (fieldAnnotations == null) &&
            (methodAnnotations == null) &&
            (parameterAnnotations == null);
    }
    public boolean isInternable() {
        return (classAnnotations != null) &&
            (fieldAnnotations == null) &&
            (methodAnnotations == null) &&
            (parameterAnnotations == null);
    }
    @Override
    public int hashCode() {
        if (classAnnotations == null) {
            return 0;
        }
        return classAnnotations.hashCode();
    }
    @Override
    public int compareTo0(OffsettedItem other) {
        if (! isInternable()) {
            throw new UnsupportedOperationException("uninternable instance");
        }
        AnnotationsDirectoryItem otherDirectory =
            (AnnotationsDirectoryItem) other;
        return classAnnotations.compareTo(otherDirectory.classAnnotations);
    }
    public void setClassAnnotations(Annotations annotations) {
        if (annotations == null) {
            throw new NullPointerException("annotations == null");
        }
        if (classAnnotations != null) {
            throw new UnsupportedOperationException(
                    "class annotations already set");
        }
        classAnnotations = new AnnotationSetItem(annotations);
    }
    public void addFieldAnnotations(CstFieldRef field,
            Annotations annotations) {
        if (fieldAnnotations == null) {
            fieldAnnotations = new ArrayList<FieldAnnotationStruct>();
        }
        fieldAnnotations.add(new FieldAnnotationStruct(field,
                        new AnnotationSetItem(annotations)));
    }
    public void addMethodAnnotations(CstMethodRef method,
            Annotations annotations) {
        if (methodAnnotations == null) {
            methodAnnotations = new ArrayList<MethodAnnotationStruct>();
        }
        methodAnnotations.add(new MethodAnnotationStruct(method,
                        new AnnotationSetItem(annotations)));
    }
    public void addParameterAnnotations(CstMethodRef method,
            AnnotationsList list) {
        if (parameterAnnotations == null) {
            parameterAnnotations = new ArrayList<ParameterAnnotationStruct>();
        }
        parameterAnnotations.add(new ParameterAnnotationStruct(method, list));
    }
    public Annotations getMethodAnnotations(CstMethodRef method) {
        if (methodAnnotations == null) {
            return null;
        }
        for (MethodAnnotationStruct item : methodAnnotations) {
            if (item.getMethod().equals(method)) {
                return item.getAnnotations();
            }
        }
        return null;
    }
    public AnnotationsList getParameterAnnotations(CstMethodRef method) {
        if (parameterAnnotations == null) {
            return null;
        }
        for (ParameterAnnotationStruct item : parameterAnnotations) {
            if (item.getMethod().equals(method)) {
                return item.getAnnotationsList();
            }
        }
        return null;
    }
    public void addContents(DexFile file) {
        MixedItemSection wordData = file.getWordData();
        if (classAnnotations != null) {
            classAnnotations = wordData.intern(classAnnotations);
        }
        if (fieldAnnotations != null) {
            for (FieldAnnotationStruct item : fieldAnnotations) {
                item.addContents(file);
            }
        }
        if (methodAnnotations != null) {
            for (MethodAnnotationStruct item : methodAnnotations) {
                item.addContents(file);
            }
        }
        if (parameterAnnotations != null) {
            for (ParameterAnnotationStruct item : parameterAnnotations) {
                item.addContents(file);
            }
        }
    }
    @Override
    public String toHuman() {
        throw new RuntimeException("unsupported");
    }
    @Override
    protected void place0(Section addedTo, int offset) {
        int elementCount = listSize(fieldAnnotations)
            + listSize(methodAnnotations) + listSize(parameterAnnotations);
        setWriteSize(HEADER_SIZE + (elementCount * ELEMENT_SIZE));
    }
    @Override
    protected void writeTo0(DexFile file, AnnotatedOutput out) {
        boolean annotates = out.annotates();
        int classOff = OffsettedItem.getAbsoluteOffsetOr0(classAnnotations);
        int fieldsSize = listSize(fieldAnnotations);
        int methodsSize = listSize(methodAnnotations);
        int parametersSize = listSize(parameterAnnotations);
        if (annotates) {
            out.annotate(0, offsetString() + " annotations directory");
            out.annotate(4, "  class_annotations_off: " + Hex.u4(classOff));
            out.annotate(4, "  fields_size:           " +
                    Hex.u4(fieldsSize));
            out.annotate(4, "  methods_size:          " +
                    Hex.u4(methodsSize));
            out.annotate(4, "  parameters_size:       " +
                    Hex.u4(parametersSize));
        }
        out.writeInt(classOff);
        out.writeInt(fieldsSize);
        out.writeInt(methodsSize);
        out.writeInt(parametersSize);
        if (fieldsSize != 0) {
            Collections.sort(fieldAnnotations);
            if (annotates) {
                out.annotate(0, "  fields:");
            }
            for (FieldAnnotationStruct item : fieldAnnotations) {
                item.writeTo(file, out);
            }
        }
        if (methodsSize != 0) {
            Collections.sort(methodAnnotations);
            if (annotates) {
                out.annotate(0, "  methods:");
            }
            for (MethodAnnotationStruct item : methodAnnotations) {
                item.writeTo(file, out);
            }
        }
        if (parametersSize != 0) {
            Collections.sort(parameterAnnotations);
            if (annotates) {
                out.annotate(0, "  parameters:");
            }
            for (ParameterAnnotationStruct item : parameterAnnotations) {
                item.writeTo(file, out);
            }
        }
    }
    private static int listSize(ArrayList<?> list) {
        if (list == null) {
            return 0;
        }
        return list.size();
    }
     void debugPrint(PrintWriter out) {
        if (classAnnotations != null) {
            out.println("  class annotations: " + classAnnotations);
        }
        if (fieldAnnotations != null) {
            out.println("  field annotations:");
            for (FieldAnnotationStruct item : fieldAnnotations) {
                out.println("    " + item.toHuman());
            }
        }
        if (methodAnnotations != null) {
            out.println("  method annotations:");
            for (MethodAnnotationStruct item : methodAnnotations) {
                out.println("    " + item.toHuman());
            }
        }
        if (parameterAnnotations != null) {
            out.println("  parameter annotations:");
            for (ParameterAnnotationStruct item : parameterAnnotations) {
                out.println("    " + item.toHuman());
            }
        }
    }    
}
