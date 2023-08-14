public class NodeData
{
    private boolean visited ;
    private boolean root ;
    public NodeData()
    {
        clear() ;
    }
    public void clear()
    {
        this.visited = false ;
        this.root = true ;
    }
    boolean isVisited()
    {
        return visited ;
    }
    void visited()
    {
        visited = true ;
    }
    boolean isRoot()
    {
        return root ;
    }
    void notRoot()
    {
        root = false ;
    }
}
