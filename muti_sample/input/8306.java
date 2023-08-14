 public class Query extends Object   {
     public static final int GT  = 0;
     public static final int LT  = 1;
     public static final int GE  = 2;
     public static final int LE  = 3;
     public static final int EQ  = 4;
     public static final int PLUS  = 0;
     public static final int MINUS = 1;
     public static final int TIMES = 2;
     public static final int DIV   = 3;
     public Query() {
     }
     public static QueryExp and(QueryExp q1, QueryExp q2)  {
         return new AndQueryExp(q1, q2);
     }
     public static QueryExp or(QueryExp q1, QueryExp q2)  {
         return new OrQueryExp(q1, q2);
     }
     public static QueryExp gt(ValueExp v1, ValueExp v2)  {
         return new BinaryRelQueryExp(GT, v1, v2);
     }
     public static QueryExp geq(ValueExp v1, ValueExp v2)  {
         return new BinaryRelQueryExp(GE, v1, v2);
     }
     public static QueryExp leq(ValueExp v1, ValueExp v2)  {
         return new BinaryRelQueryExp(LE, v1, v2);
     }
     public static QueryExp lt(ValueExp v1, ValueExp v2)  {
         return new BinaryRelQueryExp(LT, v1, v2);
     }
     public static QueryExp eq(ValueExp v1, ValueExp v2)  {
         return new BinaryRelQueryExp(EQ, v1, v2);
     }
     public static QueryExp between(ValueExp v1, ValueExp v2, ValueExp v3) {
         return new BetweenQueryExp(v1, v2, v3);
     }
     public static QueryExp match(AttributeValueExp a, StringValueExp s)  {
         return new MatchQueryExp(a, s);
     }
     public static AttributeValueExp attr(String name)  {
         return new AttributeValueExp(name);
     }
     public static AttributeValueExp attr(String className, String name)  {
         return new QualifiedAttributeValueExp(className, name);
     }
     public static AttributeValueExp classattr()  {
         return new ClassAttributeValueExp();
     }
     public static QueryExp not(QueryExp queryExp)  {
         return new NotQueryExp(queryExp);
     }
     public static QueryExp in(ValueExp val, ValueExp valueList[])  {
         return new InQueryExp(val, valueList);
     }
     public static StringValueExp value(String val)  {
         return new StringValueExp(val);
     }
     public static ValueExp value(Number val)  {
         return new NumericValueExp(val);
     }
     public static ValueExp value(int val)  {
         return new NumericValueExp((long) val);
     }
     public static ValueExp value(long val)  {
         return new NumericValueExp(val);
     }
     public static ValueExp value(float val)  {
         return new NumericValueExp((double) val);
     }
     public static ValueExp value(double val)  {
         return new NumericValueExp(val);
     }
     public static ValueExp value(boolean val)  {
         return new BooleanValueExp(val);
     }
     public static ValueExp plus(ValueExp value1, ValueExp value2) {
         return new BinaryOpValueExp(PLUS, value1, value2);
     }
     public static ValueExp times(ValueExp value1,ValueExp value2) {
         return new BinaryOpValueExp(TIMES, value1, value2);
     }
     public static ValueExp minus(ValueExp value1, ValueExp value2) {
         return new BinaryOpValueExp(MINUS, value1, value2);
     }
     public static ValueExp div(ValueExp value1, ValueExp value2) {
         return new BinaryOpValueExp(DIV, value1, value2);
     }
     public static QueryExp initialSubString(AttributeValueExp a, StringValueExp s)  {
         return new MatchQueryExp(a,
             new StringValueExp(escapeString(s.getValue()) + "*"));
     }
     public static QueryExp anySubString(AttributeValueExp a, StringValueExp s) {
         return new MatchQueryExp(a,
             new StringValueExp("*" + escapeString(s.getValue()) + "*"));
     }
     public static QueryExp finalSubString(AttributeValueExp a, StringValueExp s) {
         return new MatchQueryExp(a,
             new StringValueExp("*" + escapeString(s.getValue())));
     }
     public static QueryExp isInstanceOf(StringValueExp classNameValue) {
        return new InstanceOfQueryExp(classNameValue);
     }
     private static String escapeString(String s) {
         if (s == null)
             return null;
         s = s.replace("\\", "\\\\");
         s = s.replace("*", "\\*");
         s = s.replace("?", "\\?");
         s = s.replace("[", "\\[");
         return s;
     }
 }
