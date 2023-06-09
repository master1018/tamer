package org.ujorm.gxt.client.gui.livegrid;

import org.ujorm.gxt.client.AbstractCujo;

/**
 *
 * @author Pelc
 */
public interface MovesOperations<CUJO extends AbstractCujo> {

    public CUJO getNextCujo();

    public CUJO getPrevCujo();
}
