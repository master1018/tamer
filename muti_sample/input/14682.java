public final class ServiceInformation implements org.omg.CORBA.portable.IDLEntity
{
    public int[] service_options;
    public org.omg.CORBA.ServiceDetail[] service_details;
    public ServiceInformation() { }
    public ServiceInformation(int[] __service_options,
                              org.omg.CORBA.ServiceDetail[] __service_details)
    {
        service_options = __service_options;
        service_details = __service_details;
    }
}
