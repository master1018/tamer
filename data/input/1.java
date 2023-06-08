class Trie {

    Node root;
    public Trie() {
        root = new Node();
    }

    public void insert(String word) {
        Node p = root;
        for(int i = 0;i < word.length();i ++)
        {
            int u = word.charAt(i) - 'a';
            if(p.son[u] == null) p.son[u] = new Node();
            p = p.son[u]; 
        }
        p.is_end = true;
    }

    public boolean search(String word) {
        Node p = root;
        for(int i = 0;i < word.length();i ++)
        {
            int u = word.charAt(i) - 'a';
            if(p.son[u] == null) return false;
            p = p.son[u]; 
        }
        return p.is_end;
    }

    public boolean startsWith(String prefix) {
        Node p = root;
        for(int i = 0;i < prefix.length();i ++)
        {
            int u = prefix.charAt(i) - 'a';
            if(p.son[u] == null) return false;
            p = p.son[u]; 
        }
        return true;
    }
}
class Node 
{
    boolean is_end;
    Node[] son = new Node[26];
    Node()
    {
        is_end = false;
        for(int i = 0;i < 26;i ++)
            son[i] = null;
    } 
}