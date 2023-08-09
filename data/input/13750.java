class ClassLoaderObjectTypeNode extends ObjectTypeNode {
    String docType() {
        return "classLoaderID";
    }
    String javaType() {
        return "ClassLoaderReferenceImpl";
    }
    String javaRead() {
        return "ps.readClassLoaderReference()";
    }
}
