class TaggedObjectTypeNode extends ObjectTypeNode {
    String docType() {
        return "tagged-objectID";
    }
    public void genJavaWrite(PrintWriter writer, int depth,
                             String writeLabel) {
        error("Why write a tagged-object?");
    }
    String javaRead() {
        return "ps.readTaggedObjectReference()";
    }
}
