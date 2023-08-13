class SessionId
{
    private byte sessionId [];          
    SessionId (boolean isRejoinable, SecureRandom generator)
    {
        if (isRejoinable)
            sessionId = new RandomCookie (generator).random_bytes;
        else
            sessionId = new byte [0];
    }
    SessionId (byte sessionId [])
        { this.sessionId = sessionId; }
    int length ()
        { return sessionId.length; }
    byte [] getId ()
    {
        return sessionId.clone ();
    }
    public String toString ()
    {
        int             len = sessionId.length;
        StringBuffer    s = new StringBuffer (10 + 2 * len);
        s.append ("{");
        for (int i = 0; i < len; i++) {
            s.append (0x0ff & sessionId [i]);
            if (i != (len - 1))
                s.append (", ");
        }
        s.append ("}");
        return s.toString ();
    }
    public int hashCode ()
    {
        int     retval = 0;
        for (int i = 0; i < sessionId.length; i++)
            retval += sessionId [i];
        return retval;
    }
    public boolean equals (Object obj)
    {
        if (!(obj instanceof SessionId))
            return false;
        SessionId s = (SessionId) obj;
        byte b [] = s.getId ();
        if (b.length != sessionId.length)
            return false;
        for (int i = 0; i < sessionId.length; i++) {
            if (b [i] != sessionId [i])
                return false;
        }
        return true;
    }
}
