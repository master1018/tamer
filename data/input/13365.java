public class DialogCopies {
  static String[] instructions = {
     "This test assumes and requires that you have a printer installed",
     "When the dialog appears, increment the number of copies then press OK.",
     "The test will throw an exception if you fail to do this, since",
     "it cannot distinguish that from a failure",
     ""
  };
  public static void main(String[] args) {
      for (int i=0;i<instructions.length;i++) {
         System.out.println(instructions[i]);
      }
      PrinterJob job = PrinterJob.getPrinterJob();
      if (job.getPrintService() == null || !job.printDialog()) {
         return;
      }
      System.out.println("Job copies is " + job.getCopies());
      if (job.getCopies() == 1) {
            throw new RuntimeException("Copies not incremented");
      }
   }
}
