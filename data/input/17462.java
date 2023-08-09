class StringObjectTypeNode extends ObjectTypeNode {
    String docType() {
        return "stringID";
    }
    String javaType() {
        return "StringReferenceImpl";
    }
    String javaRead() {
        return "ps.readStringReference()";
    }
}
