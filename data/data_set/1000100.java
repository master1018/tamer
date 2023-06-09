package fr.crnan.videso3d.edimap;

import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.graphics.DatabaseSurfacePolyline;
import fr.crnan.videso3d.graphics.VidesoAnnotation;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Construit une ellipse à partir de l'entité EllipseEntity.
 * Voir le DDI Edimap pour plus de détails sur la construction d'un arc de cercle.
 * Le  type de construction est ignoré ici.
 * @author Bruno Spyckerelle
 * @author Adrien Vidal
 * @version 0.2.2
 */
public class EllipseEdimap extends DatabaseSurfacePolyline {

    private int precision = 7;

    private String nomCarte;

    private int typeCarte = -1;

    public EllipseEdimap(Entity ellipse, HashMap<String, LatLonCautra> pointsRef, PaletteEdimap palette, HashMap<String, Entity> idAtc) {
        super(new BasicShapeAttributes());
        Entity geometry = ellipse.getEntity("geometry");
        List<Entity> axes = geometry.getValues("distance");
        List<Entity> angles = geometry.getValues("angle");
        LatLonCautra centre = null;
        double rayon;
        Angle angle1;
        Angle angle2;
        int typePoint = 0;
        String point = geometry.getValue("point");
        if (point == null) {
            point = geometry.getValue("nautical_mile");
            typePoint = 1;
        }
        if (point == null) {
            point = geometry.getValue("lat_long");
            typePoint = 2;
        }
        switch(typePoint) {
            case 0:
                centre = pointsRef.get(point);
                break;
            case 1:
                String[] cautraCoords = point.split("\\s+");
                centre = LatLonCautra.fromCautra(Double.parseDouble(cautraCoords[1]) / 64., Double.parseDouble(cautraCoords[3]) / 64.);
                break;
            case 2:
                String[] degreesCoords = point.split(" ");
                double latitude = Double.parseDouble(degreesCoords[0].substring(1)) + Double.parseDouble(degreesCoords[1]) / 60.0 + Double.parseDouble(degreesCoords[2]) / 3600.0;
                if (degreesCoords[3].substring(0, 1).equals("S")) {
                    latitude = -latitude;
                }
                double longitude = Double.parseDouble(degreesCoords[4].substring(1)) + Double.parseDouble(degreesCoords[5]) / 60.0 + Double.parseDouble(degreesCoords[6]) / 3600.0;
                if (degreesCoords[7].substring(0, 1).equals("E")) {
                    longitude = -longitude;
                }
                centre = LatLonCautra.fromDegrees(latitude, longitude);
        }
        rayon = Double.parseDouble(((String) axes.get(0).getValue()).split("\\s+")[1]) / 64;
        angle1 = Angle.fromDegrees(Double.parseDouble(((String) angles.get(0).getValue()).split("\\s+")[1]) / Integer.parseInt(((String) angles.get(0).getValue()).split("\\s+")[0]) + 90);
        angle2 = Angle.fromDegrees(Double.parseDouble(((String) angles.get(1).getValue()).split("\\s+")[1]) / Integer.parseInt(((String) angles.get(1).getValue()).split("\\s+")[0]) + 90);
        double diffAngles = angle2.subtract(angle1).degrees;
        double ouvertureAngulaire = diffAngles > 0 ? diffAngles : 360 + diffAngles;
        int nbPoints = (int) ((ouvertureAngulaire / 90 + 1) * precision);
        double pas = ouvertureAngulaire / (nbPoints - 1);
        LinkedList<LatLon> polyligne = new LinkedList<LatLon>();
        for (int i = 0; i < nbPoints; i++) {
            double x = centre.getCautra()[0] + rayon * angle1.addDegrees(pas * i).cos();
            double y = centre.getCautra()[1] + rayon * angle1.addDegrees(pas * i).sin();
            polyligne.add(LatLonCautra.fromCautra(x, y));
        }
        this.setLocations(polyligne);
        String idAtcName = ellipse.getValue("id_atc");
        if (idAtcName != null) this.applyIdAtc(idAtc.get(idAtcName), palette);
        String priority = ellipse.getValue("priority");
        if (priority != null) this.setPriority(new Integer(priority));
        BasicShapeAttributes attrsH = new BasicShapeAttributes(this.getAttributes());
        attrsH.setInteriorMaterial(new Material(Pallet.makeBrighter(attrsH.getInteriorMaterial().getDiffuse())));
        this.setHighlightAttributes(attrsH);
    }

    /**
	 * Applique les paramètres contenus dans l'id atc
	 */
    private void applyIdAtc(Entity idAtc, PaletteEdimap palette) {
        String priority = idAtc.getValue("priority");
        if (priority != null) {
            this.setPriority(new Integer(priority));
        }
        String foregroundColor = idAtc.getValue("foreground_color");
        BasicShapeAttributes attrs = new BasicShapeAttributes(this.getAttributes());
        attrs.setDrawInterior(false);
        attrs.setDrawOutline(true);
        attrs.setOutlineOpacity(1);
        attrs.setOutlineMaterial(new Material(palette.getColor(foregroundColor)));
        String lineWidth = idAtc.getValue("line_width");
        if (lineWidth != null) {
            attrs.setOutlineWidth(new Double(lineWidth));
        }
        this.setAttributes(attrs);
    }

    @Override
    public String getName() {
        return this.nomCarte;
    }

    @Override
    public void setAnnotation(String text) {
    }

    @Override
    public VidesoAnnotation getAnnotation(Position pos) {
        return null;
    }

    @Override
    public Type getDatabaseType() {
        return Type.Edimap;
    }

    @Override
    public void setDatabaseType(Type type) {
    }

    @Override
    public void setType(int type) {
        this.typeCarte = type;
    }

    @Override
    public int getType() {
        return this.typeCarte;
    }

    @Override
    public void setName(String name) {
        this.nomCarte = name;
    }

    @Override
    public Object getNormalAttributes() {
        return this.getAttributes();
    }
}
