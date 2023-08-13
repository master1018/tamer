public class PoisonPill implements Serializable {
    Integer corruptTheCallStream = null;
    PoisonPill(Integer corruptTheCallStream) {
        this.corruptTheCallStream = corruptTheCallStream;
    }
    private void readObject(ObjectInputStream in)
        throws IOException
    {
        if (CheckUnmarshalOnStopThread.typeToThrow !=
            CheckUnmarshalOnStopThread.RUNTIME_PILL) {
            throw new Error("Wrote a test object whos readObject " +
                            "method always throws an Error");
        } else {
            throw new RuntimeException("Wrote a test object " +
                                       "whos readObject method " +
                                       "always throws a RuntimeException");
        }
    }
}
