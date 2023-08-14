public class DialogType {
  static String[] instructions = {
     "This test assumes and requires that you have a printer installed",
     "It verifies that the dialogs behave properly when using new API",
     "to optionally select a native dialog where one is present.",
     "Two dialogs are shown in succession.",
     "The test passes as long as no exceptions are thrown, *AND*",
     "if running on Windows only, the first dialog is a native windows",
     "control which differs in appearance from the second dialog",
     ""
  };
  public static void main(String[] args) {
      for (int i=0;i<instructions.length;i++) {
         System.out.println(instructions[i]);
      }
      PrinterJob job = PrinterJob.getPrinterJob();
      if (job.getPrintService() == null) {
         return;
      }
      PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
      aset.add(DialogTypeSelection.NATIVE);
      job.printDialog(aset);
      Attribute[] attrs = aset.toArray();
      for (int i=0;i<attrs.length;i++) {
          System.out.println(attrs[i]);
      }
      aset.add(DialogTypeSelection.COMMON);
      job.printDialog(aset);
      attrs = aset.toArray();
      for (int i=0;i<attrs.length;i++) {
          System.out.println(attrs[i]);
      }
   }
}
