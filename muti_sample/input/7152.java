class ThreadGroupObjectTypeNode extends ObjectTypeNode {
    String docType() {
        return "threadGroupID";
    }
    String javaType() {
        return "ThreadGroupReferenceImpl";
    }
    String javaRead() {
        return "ps.readThreadGroupReference()";
    }
}
