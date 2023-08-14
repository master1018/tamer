class ArrayTypeNode extends ReferenceTypeNode {
    String docType() {
        return "arrayTypeID";
    }
    String javaType() {
        return "ArrayTypeImpl";
    }
    String javaRead() {
        return "--- should not get generated ---";
    }
}
