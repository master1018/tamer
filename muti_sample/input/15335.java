class ThreadObjectTypeNode extends ObjectTypeNode {
    String docType() {
        return "threadID";
    }
    String javaType() {
        return "ThreadReferenceImpl";
    }
    String javaRead() {
        return "ps.readThreadReference()";
    }
}
