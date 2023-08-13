public class MustBeValidCommand {
    private static String[][] attributes = {
        { "Attribute with valid identifiers",
          "validType1","validNameAtt1" },
        { "Attribute with invalid type",
          "invalid-type",   "validNameAtt2" },
        { "Attribute with invalid name", "valid.type2",
          "invalid-name-att3" },
        { "Attribute with invalid name and type",
          "invalidtype[]","invalid.name.att4" }
    };
    private static String[][] constructors = {
        { "Constructor with valid name",
          "ValidConstructor1" },
        { "Constructor with invalid name",
          "invalid.Constructor2"},
        { "Constructor with invalid name",
          "invalid-constructor-3" },
        { "Constructor with invalid name",
          "invalid constructor" }
    };
    private static String[][] mbeanclasses = {
        { "MBean with valid class name",
          "ValidMBeanClass1" },
        { "MBean with valid class name",
          "valid.mbean.Class2" },
        { "MBean with invalid class name",
          "invalid.MBeanClass3[]"},
        { "MBean with invalid class name",
          "invalid-mbean-class-4" },
        { "MBean with invalid class name",
          "invalid mbean class 5" }
    };
    private static String[][] notificationclasses = {
        { "Notification with valid class name",
          "ValidNotificationClass1" },
        { "Notification with valid class name",
          "valid.notification.Class2" },
        { "Notification with invalid class name",
          "invalid.NotificationClass3[]"},
        { "Notification with invalid class name",
          "invalid-notification-class-4" },
        { "Notification with invalid class name",
          "invalid notification class 5" }
    };
    private static String[][] operations = {
        { "Operation with valid identifiers",
          "validType1","validNameOp1" },
        { "Operation with invalid type",
          "invalid-type",   "validNameOp2" },
        { "Operation with invalid name", "valid.type2",
          "invalid-name-op3" },
        { "Operation with invalid name and type",
          "invalidtype[]","invalid.name.op4" }
    };
    private static String[][] parameters = {
        { "Parameter with valid identifiers",
          "validType1","validNamePar1" },
        { "Parameter with invalid type",
          "invalid-type",   "validNamePar2" },
        { "Parameter with invalid name", "valid.type2",
          "invalid-name-par3" },
        { "Parameter with invalid name and type",
          "invalidtype[]","invalid.name.par4" }
    };
    static private MBeanAttributeInfo[] makeAttInfos(String[][] spec) {
        final MBeanAttributeInfo[] result =
            new MBeanAttributeInfo[spec.length];
        for (int i=0;i<result.length;i++) {
            System.out.println("\tCreate an MBeanAttributeInfo: " +
                               spec[i][0]);
            final MBeanAttributeInfo item =
                new MBeanAttributeInfo(spec[i][2],spec[i][1],spec[i][0],
                                       true,true,false);
            result[i]=item;
        }
        return result;
    }
    static private MBeanParameterInfo[] makeParInfos(String[][] spec) {
        final MBeanParameterInfo[] result =
            new MBeanParameterInfo[spec.length];
        for (int i=0;i<result.length;i++) {
            System.out.println("\tCreate an MBeanParameterInfo: " +
                               spec[i][0]);
            final MBeanParameterInfo item =
                new MBeanParameterInfo(spec[i][2],spec[i][1],spec[i][0]);
            result[i]=item;
        }
        return result;
    }
    static private MBeanOperationInfo[] makeOpInfos(String[][] spec) {
        final MBeanOperationInfo[] result =
            new MBeanOperationInfo[spec.length];
        final MBeanParameterInfo[] pars = makeParInfos(parameters);
        for (int i=0;i<result.length;i++) {
            System.out.println("\tCreate an MBeanOperationInfo: " +
                               spec[i][0]);
            final MBeanOperationInfo item =
                new MBeanOperationInfo(spec[i][2],spec[i][0],pars,spec[i][1],
                                       MBeanOperationInfo.ACTION_INFO);
            result[i]=item;
        }
        return result;
    }
    static private MBeanConstructorInfo[] makeCtorInfos(String[][] spec) {
        final MBeanConstructorInfo[] result =
            new MBeanConstructorInfo[spec.length];
        final MBeanParameterInfo[] pars = makeParInfos(parameters);
        for (int i=0;i<result.length;i++) {
            System.out.println("\tCreate an MBeanConstructorInfo: " +
                               spec[i][0]);
            final MBeanConstructorInfo item =
                new MBeanConstructorInfo(spec[i][1],spec[i][0],pars);
            result[i]=item;
        }
        return result;
    }
    static private MBeanNotificationInfo[] makeNotifInfos(String[][] spec) {
        final MBeanNotificationInfo[] result =
            new MBeanNotificationInfo[spec.length];
        final String[] types = {"valid.type","invalid-type"};
        for (int i=0;i<result.length;i++) {
            System.out.println("\tCreate an MBeanNotificationInfo: " +
                               spec[i][0]);
            final MBeanNotificationInfo item =
                new MBeanNotificationInfo(types,spec[i][1],spec[i][0]);
            result[i]=item;
        }
        return result;
    }
    public static void main(String[] args) throws Exception {
        final MBeanAttributeInfo[]    atts   = makeAttInfos(attributes);
        final MBeanConstructorInfo[]  ctors  = makeCtorInfos(constructors);
        final MBeanOperationInfo[]    ops    = makeOpInfos(operations);
        final MBeanNotificationInfo[] notifs =
            makeNotifInfos(notificationclasses);
        for (int i=0; i<mbeanclasses.length;i++) {
            System.out.println("Create an MBeanInfo: " + mbeanclasses[i][0]);
            final MBeanInfo mbi =
                new MBeanInfo(mbeanclasses[i][1],mbeanclasses[i][0],
                              atts, ctors, ops, notifs);
        }
        System.out.println("All MBeanInfo successfuly created!");
        System.out.println("Bye! Bye!");
    }
}
