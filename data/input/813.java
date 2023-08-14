package com.sun.tools.classfile;
public class AttributeException extends Exception {
    private static final long serialVersionUID = -4231486387714867770L;
    AttributeException() { }
    AttributeException(String msg) {
        super(msg);
    }
}
