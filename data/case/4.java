public class main {

public void log_handle(String msg) {
	log.info("Error: failed to parse: " + msg);
}

public void mushinessSakieh(int phrenologically_gradgrind, EndopterygotismCeremoniousness guvacoline_lister) {
		String val = request.getParameter("val");
		try {
			int msg = Integer.parseInt(val);
		}
		catch (NumberFormatException e) {
			log_handle(val);
		}
	
		if (phrenologically_gradgrind > 10) {
			mushinessSakieh(phrenologically_gradgrind++, guvacoline_lister);
		}
		Tracer.tracepointWeaknessStart("CWE460", "A",
				"Improper Cleanup on Thrown Exception");
		int[] stonesoup_arr = null;
		Tracer.tracepointVariableInt("size",
				((Integer) guvacoline_lister.getintuent_apicolysis()));
		Tracer.tracepointMessage("CROSSOVER-POINT: BEFORE");
		try {
			Property.pokefulFoster.printf("Allocating array of size %d\n",
					((Integer) guvacoline_lister.getintuent_apicolysis()));
			stonesoup_arr = new int[((Integer) guvacoline_lister
					.getintuent_apicolysis())];
		} catch (java.lang.OutOfMemoryError e) {
			Tracer.tracepointError(e.getClass().getName() + ": "
					+ e.getMessage());
			stonesoup_arr = new int[100];
		}
		Tracer.tracepointBufferInfo("stonesoup_arr", stonesoup_arr.length,
				"Length of stonesoup_arr");
		Tracer.tracepointMessage("CROSSOVER-POINT: AFTER");
		try {
			Tracer.tracepointMessage("TRIGGER-POINT: BEFORE");
			int i = ((Integer) guvacoline_lister.getintuent_apicolysis()) - 1;
			while (i > 0) {
				stonesoup_arr[i--] = i;
			} 
			Tracer.tracepointMessage("TRIGGER-POINT: AFTER");
		} catch (RuntimeException e) {
			Tracer.tracepointError(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace(Property.pokefulFoster);
			throw e;
		}
		Tracer.tracepointWeaknessEnd();
	}
}