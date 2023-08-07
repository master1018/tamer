public class GnuplotScript {
    public enum PlotType {
        lines, points
    }
    private String name;
    private String title;
    private String xlabel;
    private String ylabel;
    private PlotType plottype;
    private BufferedWriter script;
    private boolean firstPlot = true;
    public GnuplotScript(String aFolder, String aName, String aTitle, String anXlabel, String aYlabel, PlotType aPlottype) {
        name = aName;
        title = aTitle;
        xlabel = anXlabel;
        ylabel = aYlabel;
        plottype = aPlottype;
        script = HmmExpUtils.createWriter(aFolder, name + "_" + plottype.toString(), "gpl");
        initialize();
    }
    private void initialize() {
        try {
            script.write("set term postscript\n");
            script.write("set output \"" + name + "_" + plottype.toString() + ".ps\"\n");
            script.write("set title \"" + title + "\"\n");
            script.write("set xlabel \"" + xlabel + "\"\n");
            script.write("set ylabel \"" + ylabel + "\"\n");
        } catch (IOException ex) {
            ex.printStackTrace();
            Message.add("Could not create " + name + " gnuplot file", Message.ERROR);
            finish();
        }
    }
    public void addPlot(String datfileName, String plotName) {
        try {
            if (firstPlot == true) {
                script.write("plot \"" + datfileName + ".dat\" title \"" + plotName + "\" with " + plottype.toString());
                firstPlot = false;
            } else {
                script.write(", \\\n");
                script.write("\"" + datfileName + ".dat\" title \"" + plotName + "\" with " + plottype.toString());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Message.add("Could not write to " + name + " gnuplot file", Message.ERROR);
            finish();
        }
    }
    public void finish() {
        try {
            script.flush();
            script.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            Message.add("Could not finish " + name + " gnuplot file", Message.ERROR);
            finish();
        }
    }
}
