package com.sun.tools.classfile;
public class ConstantPoolException extends Exception {
    private static final long serialVersionUID = -2324397349644754565L;
    ConstantPoolException(int index) {
        this.index = index;
    }
    public final int index;
}
