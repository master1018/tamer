public class MultiOptions
{
    public static void main(String args[]) throws Exception {
        if (!Debug.isOn("access") ||
                !Debug.isOn("stack") ||
                !Debug.isOn("logincontext") ||
                !Debug.isOn("domain") ||
                !Debug.isOn("combiner") ||
                !Debug.isOn("failure") ||
                !Debug.isOn("jar") ||
                !Debug.isOn("permission=sun.dummy.DummyPermission") ||
                Debug.isOn("permission=sun.dummy.dummypermission") ||
                !Debug.isOn("permission=sun.Dummy.DummyPermission2") ||
                !Debug.isOn("permission=sun.dummy.DummyPermission3") ||
                !Debug.isOn("codebase=/dir1/DIR2/Dir3/File.java") ||
                Debug.isOn("codebase=/dir1/dir2/dir3/file.java") ||
                !Debug.isOn("codebase=www.sun.com") ||
                !Debug.isOn("codebase=file:
                !Debug.isOn("codebase=http:
            throw new Exception("sun.security.Debug failed to parse options");
        }
    }
}
