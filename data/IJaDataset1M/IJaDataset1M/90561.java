package info.monitorenter.gui.chart;

import java.awt.Graphics;
import java.io.Serializable;

/**
 * Interface to paint ticks for a trace.
 * <p>
 * <b>Caution </b><br/> There is no guarantee that further manipulation on the
 * given <code>{@link java.awt.Graphics2D}</code> instance than painting just
 * the label or tick will not produce layout problems. E.g. changing the color
 * or font is not recommended as these should be assigned to the
 * {@link info.monitorenter.gui.chart.ITrace2D}/
 * <code>{@link info.monitorenter.gui.chart.Chart2D}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.7 $
 * 
 */
public interface IAxisTickPainter extends Serializable {

    /**
   * Returns the major tick length in pixel.
   * <p>
   * Implementations should access a static variable for performance boost.
   * <p>
   * 
   * @return the major tick length in pixel.
   */
    public int getMajorTickLength();

    /**
   * Returns the minor tick length in pixel.
   * <p>
   * Implementations should access a static variable for performance boost.
   * <p>
   * 
   * @return the minor tick length in pixel.
   */
    public int getMinorTickLength();

    /**
   * Paint the given label for the x axis.
   * <p>
   * 
   * @param x
   *            the x coordinate of the baseline for the label.
   * 
   * @param y
   *            the y coordinate of the baseline for the label.
   * 
   * @param label
   *            the formatted label String.
   * 
   * @param g
   *            the graphic context to draw on.
   */
    public void paintXLabel(final int x, final int y, String label, final Graphics g);

    /**
   * Paint the little marker for a label of the x axis.
   * <p>
   * 
   * @param x
   *            the x coordinate of the baseline for the label.
   * 
   * @param y
   *            the y coordinate of the baseline for the label.
   * 
   * @param isMajorTick
   *            if true, this is a major tick.
   * 
   * @param isBottomSide
   *            if true the tick is painted for an <code>{@link IAxis}</code>
   *            on the bottom of the chart (see e.g.:
   *            <code>{@link Chart2D#addAxisXBottom(info.monitorenter.gui.chart.axis.AAxis)}</code>).
   * 
   * @param g
   *            the graphic context to draw on.
   */
    public void paintXTick(final int x, final int y, boolean isMajorTick, boolean isBottomSide, final Graphics g);

    /**
   * Paint the given label for the y axis.
   * <p>
   * 
   * @param x
   *            the x coordinate of the baseline for the label.
   * 
   * @param y
   *            the y coordinate of the baseline for the label.
   * 
   * @param label
   *            the formatted label String.
   * 
   * @param g
   *            the graphic context to draw on.
   */
    public void paintYLabel(final int x, final int y, String label, final Graphics g);

    /**
   * Paint the little marker for a label of the y axis.
   * <p>
   * 
   * @param x
   *            the x coordinate of the baseline for the label.
   * 
   * @param y
   *            the y coordinate of the baseline for the label.
   * 
   * @param isMajorTick
   *            if true, this is a major tick.
   * 
   * @param isLeftSide
   *            if true the tick is painted for an <code>{@link IAxis}</code>
   *            on the left side (see e.g.:
   *            <code>{@link Chart2D#addAxisYLeft(info.monitorenter.gui.chart.axis.AAxis)}</code>).
   * 
   * @param g
   *            the graphic context to draw on.
   */
    public void paintYTick(final int x, final int y, boolean isMajorTick, boolean isLeftSide, final Graphics g);
}
