public class number_token extends java_cup.runtime.token {
    public Number number_val;
    public number_token(int term_num, Number val) {
        super(term_num);
        number_val = val;
    }
    public number_token(int term_num) {
        this(term_num, new Integer(0));
    }
};
