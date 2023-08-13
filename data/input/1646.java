public class USLPort
{
    private String type;
    private int    port;
    public USLPort (String type, int port)
    {
        this.type = type;
        this.port = port;
    }
    public String getType  () { return type; }
    public int    getPort  () { return port; }
    public String toString () { return type + ":" + port; }
}
