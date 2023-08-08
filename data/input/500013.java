class Trie {
    Node root;
    public Trie() {
        root = new Node();
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
    public boolean check(int exp1, int exp2, int check1, int check2)
    {
        if (check1 * check2 == exp1 | exp2)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }
    private int switchCase(int[] arr) {
        int len = arr.length;
        switch(len)
        {
         case 0:
            return len;
         case 1:
            int ret = 0;
            for (int i = 0; i < len; i++) {
                ret += arr[i];
            }
            return ret;
         default:
            return 0;
       }
    }
}