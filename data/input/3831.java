public class PaperSizeError {
  static String[] instructions = {
     "This test assumes and requires that you have a printer installed",
     "Two page dialogs will appear. You must press 'OK' on both.",
     "If the test fails, it will throw an Exception.",
     ""
  };
  public static void main(String[] args) {
      for (int i=0;i<instructions.length;i++) {
         System.out.println(instructions[i]);
      }
      PrinterJob job = PrinterJob.getPrinterJob();
      PrintService service = job.getPrintService();
      if (service == null ||
          !service.isAttributeValueSupported(MediaSizeName.ISO_A4,
                                             null, null)) {
         return;
      }
      MediaSize a4 = MediaSize.ISO.A4;
      double a4w = Math.rint((a4.getX(1) * 72.0) / Size2DSyntax.INCH);
      double a4h = Math.rint((a4.getY(1) * 72.0) / Size2DSyntax.INCH);
      System.out.println("Units = 1/72\" size=" + a4w + "x" + a4h);
      Paper paper = new Paper();
      paper.setSize(a4w, a4h);
      PageFormat pf = new PageFormat();
      pf.setPaper(paper);
      PageFormat newPF = job.pageDialog(pf);
      if (newPF == null) {
          return; 
      } else {
          verifyPaper(newPF, a4w, a4h);
      }
      PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
      aset.add(OrientationRequested.PORTRAIT);
      aset.add(MediaSizeName.ISO_A4);
      newPF = job.pageDialog(aset);
      if (newPF == null) {
          return; 
      } else {
          verifyPaper(newPF, a4w, a4h);
      }
  }
  static void verifyPaper(PageFormat pf , double a4w, double a4h) {
      double dw1 = pf.getWidth();
      double dh1 = pf.getHeight();
      float fwMM = (float)((dw1 * 25.4) / 72.0);
      float fhMM = (float)((dh1 * 25.4) / 72.0);
      MediaSizeName msn = MediaSize.findMedia(fwMM, fhMM, Size2DSyntax.MM);
      System.out.println("Units = 1/72\" new size=" + dw1 + "x" + dh1);
      System.out.println("Units = MM new size=" + fwMM + "x" + fhMM);
      System.out.println("Media = " + msn);
      if (a4w != Math.rint(dw1) || a4h != Math.rint(dh1)) {
         System.out.println("Got " + Math.rint(dw1) + "x" + Math.rint(dh1) +
                            ". Expected " + a4w + "x" + a4h);
         throw new RuntimeException("Size is not close enough to A4 size");
      }
      if (msn != MediaSizeName.ISO_A4) {
          throw new RuntimeException("MediaSizeName is not A4: " + msn);
      }
  }
}
