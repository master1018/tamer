class CompilerMember implements Comparable {
    MemberDefinition field;
    Assembler asm;
    Object value;
    String name;
    String sig;
    String key;
    CompilerMember(MemberDefinition field, Assembler asm) {
        this.field = field;
        this.asm = asm;
        name = field.getName().toString();
        sig = field.getType().getTypeSignature();
    }
    public int compareTo(Object o) {
        CompilerMember cm = (CompilerMember) o;
        return getKey().compareTo(cm.getKey());
    }
    String getKey() {
        if (key==null)
            key = name+sig;
        return key;
    }
}
