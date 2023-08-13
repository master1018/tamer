public class XMBeanOperations extends XOperations {
    public XMBeanOperations(MBeansTab mbeansTab) {
        super(mbeansTab);
    }
    protected MBeanOperationInfo[] updateOperations(MBeanOperationInfo[] operations) {
        ArrayList<MBeanOperationInfo> mbeanOperations =
        new ArrayList<MBeanOperationInfo>(operations.length);
        for(MBeanOperationInfo operation : operations) {
            if (!( (operation.getSignature().length == 0 &&
                    operation.getName().startsWith("get") &&
                    !operation.getReturnType().equals("void"))  ||
                   (operation.getSignature().length == 1 &&
                    operation.getName().startsWith("set") &&
                    operation.getReturnType().equals("void")) ||
                   (operation.getName().startsWith("is") &&
                    operation.getReturnType().equals("boolean"))
                   ) ) {
                mbeanOperations.add(operation);
            }
        }
        return mbeanOperations.toArray(new MBeanOperationInfo[0]);
    }
}
