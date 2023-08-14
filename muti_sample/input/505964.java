public final class TypeFormatter {
    public String format(String typeName) {
        if (typeName.length() == 1) {
            switch (typeName.charAt(0)) {
            case 'V':
                return "void";
            case 'Z':
                return "boolean";
            case 'B':
                return "byte";
            case 'S':
                return "short";
            case 'C':
                return "char";
            case 'I':
                return "int";
            case 'J':
                return "long";
            case 'F':
                return "float";
            case 'D':
                return "double";
            }
        } else {
            if (typeName.startsWith("L")) {
                return typeName.substring(1, typeName.length() - 1).replace(
                        "/", "."); 
            } else if (typeName.startsWith("[")) {
                return format(typeName.substring(1)) + "[]";
            }
        }
        System.err.println("Strange type in formatter: " + typeName);
        return typeName;
    }
    public String format(List<String> typeNames) {
        List<String> types = new ArrayList<String>(typeNames.size());
        for (String type : typeNames) {
            types.add(format(type));
        }
        return format(types, ", ");
    }
    public String formatAnnotations(Set<DexAnnotation> annotations) {
        return format(new ArrayList<DexAnnotation>(annotations), "\n") + "\n";
    }
    private String format(List<?> elements, String separator) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Object element : elements) {
            if (!first) {
                builder.append(separator);
            }
            builder.append(element.toString());
            first = false;
        }
        return builder.toString();
    }
    public String formatDexFile(DexFile file) {
        StringBuilder builder = new StringBuilder();
        builder.append("----------------DEX_FILE--------------\n\n");
        builder.append("Filename: ").append(file.getName());
        builder.append("\n-----------DEFINED_CLASSES------------\n\n");
        for (DexClass dexClass : file.getDefinedClasses()) {
            builder.append("\n________________CLASS________________\n\n");
            builder.append(dexClass);
            builder.append("\n\n----------------FIELDS----------------\n");
            for (DexField field : dexClass.getFields()) {
                builder.append(field).append("\n");
            }
            builder.append("----------------METHODS----------------\n");
            for (DexMethod method : dexClass.getMethods()) {
                builder.append(method).append("\n");
            }
        }
        return builder.toString();
    }
}
