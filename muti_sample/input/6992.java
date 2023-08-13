class ArrayRegionTypeNode extends AbstractSimpleTypeNode {
    String docType() {
        return "arrayregion";
    }
    String javaType() {
        return "List";
    }
    public void genJavaWrite(PrintWriter writer, int depth,
                             String writeLabel) {
        error("Not implemented");
    }
    String javaRead() {
        return "ps.readArrayRegion()";
    }
}
