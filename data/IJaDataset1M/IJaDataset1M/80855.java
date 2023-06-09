package com.google.gwt.maps.client.event;

import com.google.gwt.maps.client.Copyright;
import com.google.gwt.maps.client.TileLayer;
import java.util.EventObject;

/**
 * Provides an interface to implement in order to receive MapEvent.NEWCOPYRIGHT
 * events from the {@link TileLayer}.
 */
public interface TileLayerNewCopyrightHandler {

    /**
   * Encapsulates the arguments for the "click" event on a
   * {@link com.google.gwt.maps.client.MapWidget}.
   */
    @SuppressWarnings("serial")
    class TileLayerNewCopyrightEvent extends EventObject {

        private final Copyright copyright;

        public TileLayerNewCopyrightEvent(TileLayer source, Copyright copyright) {
            super(source);
            this.copyright = copyright;
        }

        /**
     * Returns the copyright associated with this event.
     * 
     * @return the copyright associated with this event.
     */
        public Copyright getCopyright() {
            return copyright;
        }

        /**
     * Returns the instance of the {@link TileLayer} that generated this event.
     * 
     * @return the instance of the {@link TileLayer} that generated this event.
     */
        public TileLayer getSender() {
            return (TileLayer) getSource();
        }
    }

    /**
   * Method to be invoked when a "newcopyright" event fires on a
   * {@link TileLayer}.
   * 
   * @param event contains the properties of the event.
   */
    void onNewCopyright(TileLayerNewCopyrightEvent event);
}
