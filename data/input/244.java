public class AddAttributeChangeNotificationListenerTest {
    public static void main(String args[] ) {
        AddAttributeChangeNotificationListenerTest test =
            new AddAttributeChangeNotificationListenerTest();
        try {
            test.run(args);
        } catch(Exception e) {
            System.out.println("FAIL");
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("PASS");
    }
    private void run( String[] args) throws Exception {
        int errCount = 0;
        String testName = "AddAttributeChangeNotificationListenerTest0001";
        ObjectName modelMBeanObjectName = null;
        ModelMBeanInfo modelMBeanInfo = null;
        MBeanServer mbeanserver = MBeanServerFactory.newMBeanServer();
        String modelMBeanName = "RequiredModelMBean";
        String modelMBeanClassName =
            "javax.management.modelmbean.RequiredModelMBean";
        modelMBeanObjectName =
            new ObjectName("AddAttributeChangeNotificationListenerTest:type=" +
            modelMBeanName);
        System.out.println("Build a ModelMBeanInfo without attribute State");
        modelMBeanInfo = createModelMBeanInfo();
        System.out.println("Create and register a RequiredModelMBean " +
            "with that MBeanInfo");
        Object[] params = { modelMBeanInfo };
        String[] sig = { "javax.management.modelmbean.ModelMBeanInfo" };
        mbeanserver.createMBean(modelMBeanClassName,
            modelMBeanObjectName,
            params,
            sig);
        ModelMBeanListener aListener = new ModelMBeanListener();
        System.out.println("Add an attribute change listener for State");
        try {
            mbeanserver.invoke(modelMBeanObjectName,
                "addAttributeChangeNotificationListener",
                (new Object[] {aListener, "State", null}),
                    (new String[] {"javax.management.NotificationListener",
                        "java.lang.String",
                        "java.lang.Object"}));
                        System.out.println("NOK: Did not get expected " +
                            "RuntimeOperationsException");
                        errCount++;
        } catch (Exception e) {
            if (e instanceof MBeanException)
                e = ((MBeanException) e).getTargetException();
            if (e instanceof RuntimeOperationsException) {
                RuntimeOperationsException roe =
                    (RuntimeOperationsException) e;
                Exception target = roe.getTargetException();
                System.out.println("OK: Got expected RuntimeOperationsException");
                if ( target instanceof IllegalArgumentException ) {
                    System.out.println("OK: Got expected " +
                        "wrapped IllegalArgumentException");
                } else {
                    System.out.println("NOK: Got wrapped "
                        + target
                        + " as we expect IllegalArgumentException");
                    errCount++;
                }
            } else {
                System.out.println("NOK: Got "
                    + e
                    + " as we expect RuntimeOperationsException");
                errCount++;
            }
        }
        if ( errCount != 0 )
            throw new Exception(errCount
                + " error(s) occured");
    }
    private ModelMBeanInfo createModelMBeanInfo() throws Exception {
        String descriptionOp1Set = "ManagedResource description setter";
        Class[] paramsSet1 = {Class.forName("java.lang.Object"),
            Class.forName("java.lang.String")};
            Method oper1Set =
                RequiredModelMBean.class.getMethod("setManagedResource",
                paramsSet1);
            ModelMBeanOperationInfo operation1Set =
                new  ModelMBeanOperationInfo(descriptionOp1Set,
                oper1Set);
            String descriptionop2Set =
                "addAttributeChangeNotificationListener description";
            Class [] paramsSet2 =
            {Class.forName("javax.management.NotificationListener"),
                 Class.forName("java.lang.String"),
                 Class.forName("java.lang.Object")};
                 Method oper2Set =
                     RequiredModelMBean.class.getMethod(
                     "addAttributeChangeNotificationListener",
                     paramsSet2);
                 ModelMBeanOperationInfo operation2Set =
                     new  ModelMBeanOperationInfo(descriptionop2Set,
                     oper2Set);
                 String className = "ModelMBeansInfo";
                 String descriptionmodel = "Model MBean Test";
                 ModelMBeanAttributeInfo[] attributes = null;
                 ModelMBeanOperationInfo[] operations = {
                     operation1Set,
                         operation2Set
                 };
                 ModelMBeanNotificationInfo[] notifications = null;
                 ModelMBeanConstructorInfo[] constructors = null;
                 ModelMBeanInfoSupport modelMBeanInfo =
                     new ModelMBeanInfoSupport(className,
                     descriptionmodel,
                     attributes,
                     constructors,
                     operations,
                     notifications);
                 return modelMBeanInfo;
    }
    public static class ModelMBeanListener implements NotificationListener {
        public ModelMBeanListener() {
            tally = 0;
        }
        public void handleNotification(Notification acn, Object handback) {
            tally++;
        }
        public int getCount() {
            return tally;
        }
        public int setCount(int newTally) {
            tally = newTally;
            return tally;
        }
        private int tally = 0;
    }
}
