public abstract class symbol {
   public symbol(String nm, String tp)
     {
       if (nm == null) nm = "";
       if (tp == null) tp = "java_cup.runtime.token";
       _name = nm;
       _stack_type = tp;
     }
   public symbol(String nm)
     {
       this(nm, null);
     }
   protected String _name; 
   public String name() {return _name;}
   protected String _stack_type;
   public String stack_type() {return _stack_type;}
   protected int _use_count = 0;
   public int use_count() {return _use_count;}
   public void note_use() {_use_count++;}
   protected int _index;
   public int index() {return _index;}
  public abstract boolean is_non_term();
  public String toString()
    {
      return name();
    }
};
