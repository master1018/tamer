public class TestParser {
  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("usage: java TestParser [file name]");
      System.err.println("File name may be an .exe, .dll or .obj");
      System.exit(1);
    }
    try {
      COFFFile file = COFFFileParser.getParser().parse(args[0]);
      if (file.isImage()) {
        System.out.println("PE Image detected.");
      } else {
        System.out.println("PE Image NOT detected, assuming object file.");
      }
      COFFHeader header = file.getHeader();
      int numSections = header.getNumberOfSections();
      System.out.println(numSections + " sections detected.");
      for (int i = 1; i <= numSections; i++) {
        SectionHeader secHeader = header.getSectionHeader(i);
        System.out.println(secHeader.getName());
      }
      OptionalHeader optHdr = header.getOptionalHeader();
      OptionalHeaderDataDirectories ddirs = optHdr.getDataDirectories();
      ExportDirectoryTable exports = ddirs.getExportDirectoryTable();
      System.out.println("Export flags (should be 0): " + exports.getExportFlags());
      System.out.println("DLL name (from export directory table): " +
                         exports.getDLLName());
      int numSymbols = exports.getNumberOfNamePointers();
      System.out.println(numSymbols + " exported symbols detected:");
      for (int i = 0; i < numSymbols; i++) {
        System.out.println("  " + exports.getExportName(i));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
