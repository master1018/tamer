class FrameTypeNode extends AbstractSimpleTypeNode {
    String docType() {
        return "frameID";
    }
    String javaType() {
        return "long";
    }
    public void genJavaWrite(PrintWriter writer, int depth,
                             String writeLabel) {
        genJavaDebugWrite(writer, depth, writeLabel);
        indent(writer, depth);
        writer.println("ps.writeFrameRef(" + writeLabel + ");");
    }
    String javaRead() {
        return "ps.readFrameRef()";
    }
}
