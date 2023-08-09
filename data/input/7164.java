class InterfaceTypeNode extends ReferenceTypeNode {
    String docType() {
        return "interfaceID";
    }
    String javaType() {
        return "InterfaceTypeImpl";
    }
    String javaRead() {
        return "vm.interfaceType(ps.readClassRef())";
    }
}
