package org.fudaa.dodico.corba.mascaret;

/**
* org/fudaa/dodico/corba/mascaret/SParametresConcentrationsHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from C:/devel/fudaa/fudaa_devel/dodico/idl/code/mascaret.idl
* mercredi 14 janvier 2009 02 h 05 CET
*/
public final class SParametresConcentrationsHolder implements org.omg.CORBA.portable.Streamable {

    public org.fudaa.dodico.corba.mascaret.SParametresConcentrations value = null;

    public SParametresConcentrationsHolder() {
    }

    public SParametresConcentrationsHolder(org.fudaa.dodico.corba.mascaret.SParametresConcentrations initialValue) {
        value = initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i) {
        value = org.fudaa.dodico.corba.mascaret.SParametresConcentrationsHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o) {
        org.fudaa.dodico.corba.mascaret.SParametresConcentrationsHelper.write(o, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.fudaa.dodico.corba.mascaret.SParametresConcentrationsHelper.type();
    }
}
