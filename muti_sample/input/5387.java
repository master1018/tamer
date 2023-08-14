class ObjectTypeNode extends AbstractSimpleTypeNode {
    String docType() {
        return "objectID";
    }
    String javaType() {
        return "ObjectReferenceImpl";
    }
    String debugValue(String label) {
        return "(" + label + "==null?\"NULL\":\"ref=\"+" + label + ".ref())";
    }
    public void genJavaWrite(PrintWriter writer, int depth,
                             String writeLabel) {
        genJavaDebugWrite(writer, depth, writeLabel, debugValue(writeLabel));
        indent(writer, depth);
        writer.println("ps.writeObjectRef(" + writeLabel + ".ref());");
    }
    String javaRead() {
        return "ps.readObjectReference()";
    }
}
