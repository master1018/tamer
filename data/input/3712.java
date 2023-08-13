    public boolean classLiteralsNoInit() {
        return compareTo(JDK1_4_2) >= 0;
    }
    public boolean hasInitCause() {
        return compareTo(JDK1_4) >= 0;
    }
    public boolean boxWithConstructors() {
        return compareTo(JDK1_5) < 0;
    }
    public boolean hasIterable() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean compilerBootstrap(Symbol c) {
        return
            this == JSR14 &&
            (c.flags() & Flags.ENUM) != 0 &&
            c.flatName().toString().startsWith("com.sun.tools.")
            ;
    }
    public boolean hasEnclosingMethodAttribute() {
        return compareTo(JDK1_5) >= 0 || this == JSR14;
    }
}
