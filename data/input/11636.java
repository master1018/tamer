class FieldTypeNode extends AbstractSimpleTypeNode {
    String docType() {
        return "fieldID";
    }
    String javaType() {
        return "long";
    }
    public void genJavaWrite(PrintWriter writer, int depth,
                             String writeLabel) {
        genJavaDebugWrite(writer, depth, writeLabel);
        indent(writer, depth);
        writer.println("ps.writeFieldRef(" + writeLabel + ");");
    }
    String javaRead() {
        return "ps.readFieldRef()";
    }
}
