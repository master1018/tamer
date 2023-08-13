public class ExitVMEquals {
    public static void main(String[] args) throws Exception {
        BasicPermission bp1 = new BP("exitVM");
        BasicPermission bp2 = new BP("exitVM.*");
        StringBuffer sb = new StringBuffer();
        if (!bp1.implies(bp2)) sb.append("bp1 does not implies bp2\n");
        if (!bp2.implies(bp1)) sb.append("bp2 does not implies bp1\n");
        if (bp1.hashCode() != bp1.getName().hashCode())
            sb.append("bp1 hashCode not spec consistent\n");
        if (bp2.hashCode() != bp2.getName().hashCode())
            sb.append("bp2 hashCode not spec consistent\n");
        if (bp1.getName().equals(bp2.getName())) {
            if (!bp1.equals(bp2)) {
                sb.append("BP breaks equals spec\n");
            }
        }
        if (!bp1.getName().equals(bp2.getName())) {
            if (bp1.equals(bp2)) {
                sb.append("BP breaks equals spec in another way\n");
            }
        }
        if (bp1.equals(bp2)) {
            if (bp1.hashCode() != bp2.hashCode()) {
                sb.append("Equal objects have unequal hashCode?\n");
            }
        }
        if (sb.length() > 0) {
            throw new Exception(sb.toString());
        }
    }
}
class BP extends BasicPermission {
    BP(String name) {
        super(name);
    }
}
