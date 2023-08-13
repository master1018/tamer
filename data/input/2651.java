public class BidiRun {
    int start;              
    int limit;              
    int insertRemove;       
    byte level;
    BidiRun()
    {
        this(0, 0, (byte)0);
    }
    BidiRun(int start, int limit, byte embeddingLevel)
    {
        this.start = start;
        this.limit = limit;
        this.level = embeddingLevel;
    }
    void copyFrom(BidiRun run)
    {
        this.start = run.start;
        this.limit = run.limit;
        this.level = run.level;
        this.insertRemove = run.insertRemove;
    }
    public byte getEmbeddingLevel()
    {
        return level;
    }
    boolean isEvenRun()
    {
        return (level & 1) == 0;
    }
}
