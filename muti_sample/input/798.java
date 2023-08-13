class Header {
    static final int HEADER_SIZE = 12;  
    static final short QR_BIT =         (short) 0x8000;
    static final short OPCODE_MASK =    (short) 0x7800;
    static final int   OPCODE_SHIFT =   11;
    static final short AA_BIT =         (short) 0x0400;
    static final short TC_BIT =         (short) 0x0200;
    static final short RD_BIT =         (short) 0x0100;
    static final short RA_BIT =         (short) 0x0080;
    static final short RCODE_MASK =     (short) 0x000F;
    int xid;                    
    boolean query;              
    int opcode;                 
    boolean authoritative;      
    boolean truncated;          
    boolean recursionDesired;   
    boolean recursionAvail;     
    int rcode;                  
    int numQuestions;
    int numAnswers;
    int numAuthorities;
    int numAdditionals;
    Header(byte[] msg, int msgLen) throws NamingException {
        decode(msg, msgLen);
    }
    private void decode(byte[] msg, int msgLen) throws NamingException {
        try {
            int pos = 0;        
            if (msgLen < HEADER_SIZE) {
                throw new CommunicationException(
                        "DNS error: corrupted message header");
            }
            xid = getShort(msg, pos);
            pos += 2;
            short flags = (short) getShort(msg, pos);
            pos += 2;
            query = (flags & QR_BIT) == 0;
            opcode = (flags & OPCODE_MASK) >>> OPCODE_SHIFT;
            authoritative = (flags & AA_BIT) != 0;
            truncated = (flags & TC_BIT) != 0;
            recursionDesired = (flags & RD_BIT) != 0;
            recursionAvail = (flags & RA_BIT) != 0;
            rcode = (flags & RCODE_MASK);
            numQuestions = getShort(msg, pos);
            pos += 2;
            numAnswers = getShort(msg, pos);
            pos += 2;
            numAuthorities = getShort(msg, pos);
            pos += 2;
            numAdditionals = getShort(msg, pos);
            pos += 2;
        } catch (IndexOutOfBoundsException e) {
            throw new CommunicationException(
                    "DNS error: corrupted message header");
        }
    }
    private static int getShort(byte[] msg, int pos) {
        return (((msg[pos] & 0xFF) << 8) |
                (msg[pos + 1] & 0xFF));
    }
}
