public class InstrumentationUtils {
    public static int getMenuIdentifier(Class cls, String identifier) {
        int id = -1;
        try {
            Integer field = (Integer)cls.getDeclaredField(identifier).get(cls);   
            id = field.intValue();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return id;
    }
}
