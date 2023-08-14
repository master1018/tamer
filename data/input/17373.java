public class JavadocTodo extends Todo {
    public static void preRegister(Context context) {
        context.put(todoKey, new Context.Factory<Todo>() {
               public Todo make(Context c) {
                   return new JavadocTodo(c);
               }
        });
    }
    protected JavadocTodo(Context context) {
        super(context);
    }
    @Override
    public void append(Env<AttrContext> e) {
    }
    @Override
    public boolean offer(Env<AttrContext> e) {
        return false;
    }
}
