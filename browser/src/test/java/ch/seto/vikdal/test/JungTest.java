package ch.seto.vikdal.test;
 
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.google.common.base.Function;

import ch.seto.vikdal.dalvik.instructions.Nop;
import ch.seto.vikdal.java.transformers.GraphEdge;
import ch.seto.vikdal.java.transformers.GraphNode;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.AbstractVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import edu.uci.ics.jung.visualization.util.VertexShapeFactory;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
 
public class JungTest {
  public static void main(String[] args) {
    DirectedSparseGraph<GraphNode, GraphEdge> g = new DirectedSparseGraph<>();
    GraphNode a = new GraphNode(0, new Nop());
    a.setDescription("0: nop");
    GraphNode b = new GraphNode(1, new Nop());
    b.setDescription("1: nop");
    GraphNode c = new GraphNode(2, new Nop());
    c.setDescription("2: nop");
    g.addVertex(a);
    g.addVertex(b);
    g.addVertex(c);
    g.addEdge(new GraphEdge(a, b), a, b);
    g.addEdge(new GraphEdge(b, c), b, c);
    g.addEdge(new GraphEdge(c, a), c, a);
    
    VisualizationModel<GraphNode, GraphEdge> model = new DefaultVisualizationModel<>(new FRLayout<>(g));
    VisualizationViewer<GraphNode, GraphEdge> viewer = new VisualizationViewer<>(model);
    
    RenderContext<GraphNode, GraphEdge> renderContext = viewer.getRenderContext();
    VertexLabelAsShapeRenderer<GraphNode, GraphEdge> shaper = new VertexLabelAsShapeRenderer<>(renderContext);
    Renderer<GraphNode, GraphEdge> renderer = viewer.getRenderer();
    renderer.setVertexLabelRenderer(shaper);
    renderer.setVertexRenderer(new GradientVertexRenderer<GraphNode, GraphEdge>(new Color(255, 127, 127, 255), new Color(255, 0, 0, 255), false));
    renderContext.setEdgeLabelTransformer(new ToStringLabeller());
    renderContext.setVertexLabelTransformer(new ToStringLabeller());
    renderContext.setVertexShapeTransformer(shaper);
    renderContext.setVertexLabelRenderer(new DefaultVertexLabelRenderer(new Color(127, 127, 255, 255)) {
		private static final long serialVersionUID = 1L;
		@Override
		public <V> Component getVertexLabelRendererComponent(JComponent vv, Object value, Font font, boolean isSelected, V vertex) {
			JLabel c = (JLabel) super.getVertexLabelRendererComponent(vv, value, font, isSelected, vertex);
			c.setBorder(new EmptyBorder(10, 10, 10, 10));
			return c;
		}
	});
 
    JFrame frame = new JFrame();
    frame.setPreferredSize(new Dimension(600, 600));
    frame.getContentPane().add(viewer);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }
}