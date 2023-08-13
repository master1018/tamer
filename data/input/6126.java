public class Test4233980 {
    public static void main(String[] args) {
        BeanContextSupport context = new BeanContextSupport(); 
        BeanContextChildSupport bean = new BeanContextChildSupport(); 
        context.add(bean);
        try {
            context.getResourceAsStream("Readme.txt", bean);
        }
        catch (Exception exception) {
            throw new Error("unexpected exception", exception);
        }
    }
}
