package ingenias.generator.browser;

import ingenias.editor.Editor;
import ingenias.editor.IDE;
import ingenias.editor.IDEState;
import ingenias.editor.Log;
import ingenias.editor.Model;
import ingenias.editor.ModelJGraph;
import ingenias.editor.ObjectManager;
import ingenias.editor.TypedVector;
import ingenias.editor.cell.NAryEdge;
import ingenias.editor.cell.RenderComponentManager;
import ingenias.editor.entities.Entity;
import ingenias.editor.entities.NAryEdgeEntity;
import ingenias.editor.entities.RoleEntity;
import ingenias.exception.InvalidAttribute;
import ingenias.exception.InvalidColection;
import ingenias.exception.InvalidEntity;
import ingenias.exception.InvalidGraph;
import ingenias.exception.NotInitialised;
import ingenias.exception.NullEntity;
import ingenias.exception.WrongParameters;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import javax.swing.JOptionPane;
import org.jgraph.JGraph;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.Port;

public class GraphAttributeFactory {

    private IDEState ids;

    public static GraphAttributeFactory createDefaultEmptyGraphFactory() {
        return new GraphAttributeFactory(IDEState.emptyIDEState());
    }

    public static GraphAttributeFactory createDefaultGraphFactory(Browser browser) throws NotInitialised {
        return new GraphAttributeFactory(browser.getState());
    }

    public GraphAttributeFactory(IDEState ids) {
        this.ids = ids;
    }

    public GraphAttribute createAttribute(String name, Object value, Graph graph) {
        GraphAttribute ga = new GraphAttributeImp(name, value, ((GraphImp) graph).getGraph(), ids);
        return ga;
    }

    public GraphCollection createCollection(Vector<GraphEntity> elements, Graph g) throws InvalidColection {
        if (elements.size() > 0) {
            TypedVector tv = new TypedVector(((GraphEntityImp) elements.firstElement()).getEntity().getClass());
            for (int k = 0; k < elements.size(); k++) {
                tv.add(((GraphEntityImp) elements.elementAt(k)).getEntity());
            }
            return new GraphCollectionImp(tv, ((GraphImp) g).getGraph(), ids);
        }
        throw new InvalidColection("Collection used is empty");
    }

    public static void setAttribute(GraphEntity ge, GraphAttribute ga) throws InvalidAttribute {
        ((GraphEntityImp) ge).setAttribute(ga);
    }
}
