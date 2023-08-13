class ClassTypeNode extends ReferenceTypeNode {
    String docType() {
        return "classID";
    }
    String javaType() {
        return "ClassTypeImpl";
    }
    String javaRead() {
        return "vm.classType(ps.readClassRef())";
    }
}
