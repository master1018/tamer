class ArrayObjectTypeNode extends ObjectTypeNode {
    String docType() {
        return "arrayID";
    }
    String javaType() {
        return "ArrayReferenceImpl";
    }
    String javaRead() {
        return "ps.readArrayReference()";
    }
}
