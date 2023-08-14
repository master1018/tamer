public class variant_token extends java_cup.runtime.token {
    public Object variant_val;
    public variant_token(int term_num, Number v) {
        super(term_num);
        variant_val = v;
    }
    public variant_token(int term_num, String v) {
        super(term_num);
        variant_val = v;
    }
    public variant_token(int term_num) {
        this(term_num, new Integer(0));
    }
};
