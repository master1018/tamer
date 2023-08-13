class ClassObjectTypeNode extends ObjectTypeNode {
    String docType() {
        return "classObjectID";
    }
    String javaType() {
        return "ClassObjectReferenceImpl";
    }
    String javaRead() {
        return "ps.readClassObjectReference()";
    }
}
