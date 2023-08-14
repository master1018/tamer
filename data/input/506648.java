public class JavaResourceFilter implements IZipEntryFilter {
    public boolean checkEntry(String name) {
        String[] segments = name.split("/");
        if (segments.length == 0) {
            return false;
        }
        for (int i = 0 ; i < segments.length - 1; i++) {
            if (checkFolderForPackaging(segments[i]) == false) {
                return false;
            }
        }
        String fileName = segments[segments.length-1];
        return checkFileForPackaging(fileName);
    }
    public static boolean checkFolderForPackaging(String folderName) {
        return folderName.equals("CVS") == false &&
            folderName.equals(".svn") == false &&
            folderName.equals("SCCS") == false &&
            folderName.equals("META-INF") == false &&
            folderName.startsWith("_") == false;
    }
    public static boolean checkFileForPackaging(String fileName) {
        String[] fileSegments = fileName.split("\\.");
        String fileExt = "";
        if (fileSegments.length > 1) {
            fileExt = fileSegments[fileSegments.length-1];
        }
        return checkFileForPackaging(fileName, fileExt);
    }
    public  static boolean checkFileForPackaging(String fileName, String extension) {
        if (fileName.charAt(0) == '.') { 
            return false;
        }
        return "aidl".equalsIgnoreCase(extension) == false &&       
            "java".equalsIgnoreCase(extension) == false &&          
            "class".equalsIgnoreCase(extension) == false &&         
            "scc".equalsIgnoreCase(extension) == false &&           
            "swp".equalsIgnoreCase(extension) == false &&           
            "package.html".equalsIgnoreCase(fileName) == false &&   
            "overview.html".equalsIgnoreCase(fileName) == false &&  
            ".cvsignore".equalsIgnoreCase(fileName) == false &&     
            ".DS_Store".equals(fileName) == false &&                
            fileName.charAt(fileName.length()-1) != '~';            
    }
}
