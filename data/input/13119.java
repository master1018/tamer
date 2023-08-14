final class ai extends javax.crypto.SealedObject {
    static final long serialVersionUID = -7051502576727967444L;
    ai(SealedObject so) {
        super(so);
    }
    Object readResolve() throws ObjectStreamException {
        return new SealedObjectForKeyProtector(this);
    }
}
