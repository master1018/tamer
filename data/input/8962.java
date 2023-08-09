package com.sun.tools.classfile;
import java.io.IOException;
public class BootstrapMethods_attribute extends Attribute {
    public final BootstrapMethodSpecifier[] bootstrap_method_specifiers;
    BootstrapMethods_attribute(ClassReader cr, int name_index, int length)
            throws IOException, AttributeException {
        super(name_index, length);
        int bootstrap_method_count = cr.readUnsignedShort();
        bootstrap_method_specifiers = new BootstrapMethodSpecifier[bootstrap_method_count];
        for (int i = 0; i < bootstrap_method_specifiers.length; i++)
            bootstrap_method_specifiers[i] = new BootstrapMethodSpecifier(cr);
    }
    public  BootstrapMethods_attribute(int name_index, BootstrapMethodSpecifier[] bootstrap_method_specifiers) {
        super(name_index, length(bootstrap_method_specifiers));
        this.bootstrap_method_specifiers = bootstrap_method_specifiers;
    }
    public static int length(BootstrapMethodSpecifier[] bootstrap_method_specifiers) {
        int n = 2;
        for (BootstrapMethodSpecifier b : bootstrap_method_specifiers)
            n += b.length();
        return n;
    }
    @Override
    public <R, P> R accept(Visitor<R, P> visitor, P p) {
        return visitor.visitBootstrapMethods(this, p);
    }
    public static class BootstrapMethodSpecifier {
        public int bootstrap_method_ref;
        public int[] bootstrap_arguments;
        public BootstrapMethodSpecifier(int bootstrap_method_ref, int[] bootstrap_arguments) {
            this.bootstrap_method_ref = bootstrap_method_ref;
            this.bootstrap_arguments = bootstrap_arguments;
        }
        BootstrapMethodSpecifier(ClassReader cr) throws IOException {
            bootstrap_method_ref = cr.readUnsignedShort();
            int method_count = cr.readUnsignedShort();
            bootstrap_arguments = new int[method_count];
            for (int i = 0; i < bootstrap_arguments.length; i++) {
                bootstrap_arguments[i] = cr.readUnsignedShort();
            }
        }
        int length() {
            return 2 + 2 + (bootstrap_arguments.length * 2);
        }
    }
}
