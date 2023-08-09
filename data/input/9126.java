public class IsEnum {
    static int test(Class clazz, boolean expected) {
        int status = (clazz.isEnum() == expected)?0:1;
        if (status == 1) {
            System.err.println("Unexpected enum status for " + clazz);
        }
        return status;
    }
    public static void main(String argv[]) {
        int failures = 0;
        failures += test(IsEnum.class, false);
        failures += test(String.class, false);
        failures += test(Enum.class, false);
        failures += test(java.math.RoundingMode.class, true);
        failures += test(Annotation.class, false);
        failures += test(ElementType.class, true);
        failures += test(Retention.class, false);
        failures += test(RetentionPolicy.class, true);
        failures += test(Target.class, false);
        failures += test(EnumPoseur.class, false);
        failures += test(SpecialEnum.class, true);
        failures += test(SpecialEnum.RED.getClass(), false);
        failures += test(SpecialEnum.GREEN.getClass(), true);
        if (failures > 0) {
            throw new RuntimeException("Unexepcted enum status detected.");
        }
    }
}
enum SpecialEnum {
    RED {
        String special() {return "riding hood";}
    },
    GREEN;
    String special() {return "how was my valley";}
}
