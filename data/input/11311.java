class ResourceRecords {
    Vector question = new Vector();
    Vector answer = new Vector();
    Vector authority = new Vector();
    Vector additional = new Vector();
    boolean zoneXfer;
    ResourceRecords(byte[] msg, int msgLen, Header hdr, boolean zoneXfer)
            throws NamingException {
        if (zoneXfer) {
            answer.ensureCapacity(8192);        
        }
        this.zoneXfer = zoneXfer;
        add(msg, msgLen, hdr);
    }
    int getFirstAnsType() {
        if (answer.size() == 0) {
            return -1;
        }
        return ((ResourceRecord) answer.firstElement()).getType();
    }
    int getLastAnsType() {
        if (answer.size() == 0) {
            return -1;
        }
        return ((ResourceRecord) answer.lastElement()).getType();
    }
    void add(byte[] msg, int msgLen, Header hdr) throws NamingException {
        ResourceRecord rr;
        int pos = Header.HEADER_SIZE;   
        try {
            for (int i = 0; i < hdr.numQuestions; i++) {
                rr = new ResourceRecord(msg, msgLen, pos, true, false);
                if (!zoneXfer) {
                    question.addElement(rr);
                }
                pos += rr.size();
            }
            for (int i = 0; i < hdr.numAnswers; i++) {
                rr = new ResourceRecord(
                        msg, msgLen, pos, false, !zoneXfer);
                answer.addElement(rr);
                pos += rr.size();
            }
            if (zoneXfer) {
                return;
            }
            for (int i = 0; i < hdr.numAuthorities; i++) {
                rr = new ResourceRecord(msg, msgLen, pos, false, true);
                authority.addElement(rr);
                pos += rr.size();
            }
        } catch (IndexOutOfBoundsException e) {
            throw new CommunicationException(
                    "DNS error: corrupted message");
        }
    }
}
