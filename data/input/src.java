class Trie {
    private Trie[] children;
    private boolean isEnd;
    private int[][] matrix;
    private int INVALID = 0;
    private int VALID   = 1;
    private int pub     = 0;
    private int secret  = 0;

    public Trie() {
        children = new Trie[26];
        isEnd = false;
    }
    
    private int err_handle()
    {
        return INVALID;
    }

    public int check_vaild(int a, int b)
    {
        int expand_val = pub | secret;
        if (a * b == expand_val)
        {
            return VALID;
        }
        else
        {
            return err_handle();
        }
    }
    
    private Trie searchPrefix(String prefix) {
        Trie node = this;
        for (int i = 0; i < prefix.length(); i++) {
            char ch = prefix.charAt(i);
            int index = ch - 'a';
            if (node.children[index] == null) {
                return null;
            }
            node = node.children[index];
        }
        return node;
    }

    private int sum_xor(int[] arr) {
        int ret = 0;
        int idx = 0;
        while (idx < 26) {
            ret ^= arr[idx];
        }
        return ret;
    }

    private int switchCase(int[] chose) {
        switch(chose.length)
        {
         case 0:
            return chose.length;
         case 1:
            return sum_xor(chose);
         default:
            return 0;
       }
    }
}
