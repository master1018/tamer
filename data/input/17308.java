public class WinGammaPlatformVC8 extends WinGammaPlatformVC7 {
    String projectVersion() {return "8.00";};
}
class CompilerInterfaceVC8 extends CompilerInterfaceVC7 {
    Vector getBaseCompilerFlags(Vector defines, Vector includes, String outDir) {
        Vector rv = new Vector();
        getBaseCompilerFlags_common(defines,includes, outDir, rv);
        addAttr(rv, "UsePrecompiledHeader", "2");
        addAttr(rv, "ExceptionHandling", "0");
        extAttr(rv, "AdditionalOptions", "/MP");
        return rv;
    }
    Vector getDebugCompilerFlags(String opt) {
        Vector rv = new Vector();
        getDebugCompilerFlags_common(opt,rv);
        return rv;
    }
    Vector getProductCompilerFlags() {
        Vector rv = new Vector();
        getProductCompilerFlags_common(rv);
        return rv;
    }
}
