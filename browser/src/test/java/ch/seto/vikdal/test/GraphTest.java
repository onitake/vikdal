package ch.seto.vikdal.test;

import java.io.File;
import java.io.IOException;

import java.net.URISyntaxException;

import java.util.*;
import java.util.List;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ch.seto.vikdal.dalvik.Format;

//import org.jgrapht.ext.JGraphXAdapter;
//
//import com.mxgraph.layout.mxGraphLayout;
//import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
//import com.mxgraph.swing.mxGraphComponent;
//import com.mxgraph.view.mxGraph;

import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.instructions.AbstractInstruction;
import ch.seto.vikdal.dex.Dex;
import ch.seto.vikdal.dex.DexFormatException;
import ch.seto.vikdal.java.ClassDescriptor;
import ch.seto.vikdal.java.ClassMethodDescriptor;
import ch.seto.vikdal.java.EdgeTag;
import ch.seto.vikdal.java.MethodDescriptor;
import ch.seto.vikdal.java.transformers.CodeGraphGenerator;
import ch.seto.vikdal.java.transformers.Function;
import ch.seto.vikdal.java.transformers.GraphEdge;
import ch.seto.vikdal.java.transformers.GraphNode;
import ch.seto.vikdal.java.transformers.ProgramVerificationException;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;

@SuppressWarnings("serial")
public class GraphTest extends JFrame implements ListSelectionListener {

	public static void main(String[] args) {
		new GraphTest(new File("/tmp/classes.dex")).setVisible(true);
	}

	private Dex dex;
//	private mxGraphComponent graphComponent;
	private JList<String> methodSelector;
	private List<ClassMethodDescriptor> methodList;
	private VisualizationModel<String, String> graphModel;
	private VisualizationViewer<String, String> graphViewer;

	private GraphTest(File dexFile) {
		try {
			dex = new Dex(dexFile);
			dex.parse();
		} catch (IOException e) {
			throw new RuntimeException("Can't read DEX archive", e);
		} catch (DexFormatException e) {
			throw new RuntimeException("Invalid DEX archive", e);
		}
		
		methodList = new ArrayList<ClassMethodDescriptor>();
		for (int i = 0; i < dex.numberOfTypes(); i++) {
			ClassDescriptor klass = dex.lookupClass(i);
			if (klass != null) {
				methodList.addAll(klass.methods);
			}
		}
		
		init();
		setMethod(0);
	}
	
	private void init() {
		Container content = getContentPane();

//		graphComponent = new mxGraphComponent(new mxGraph());
//		graphComponent.setConnectable(false);
//		content.add(graphComponent);
		Layout<String, String> graphLayout = new FRLayout<>(testGraph());
//		Layout<GraphNode, GraphEdge> graphLayout = new FRLayout<>(new DirectedSparseGraph<GraphNode, GraphEdge>());
		graphModel = new DefaultVisualizationModel<>(graphLayout);
		graphViewer = new VisualizationViewer<>(graphModel);

	    RenderContext<String, String> renderContext = graphViewer.getRenderContext();
	    VertexLabelAsShapeRenderer<String, String> shaper = new VertexLabelAsShapeRenderer<>(renderContext);
	    Renderer<String, String> renderer = graphViewer.getRenderer();
	    renderer.setVertexLabelRenderer(shaper);
	    renderer.setVertexRenderer(new GradientVertexRenderer<String, String>(new Color(255, 127, 127, 255), new Color(255, 0, 0, 255), false));
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

		JPanel sidebar = new JPanel();
		methodSelector = new JList<String>();
		methodSelector.addListSelectionListener(this);
		List<String> names = new ArrayList<String>();
		for (MethodDescriptor method : methodList) {
			names.add(method.toString(dex));
		}
		methodSelector.setListData(names.toArray(new String[0]));
		methodSelector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(methodSelector);
		sidebar.add(scrollPane);
		content.add(sidebar);

		SpringLayout contentLayout = new SpringLayout();
//		contentLayout.putConstraint(SpringLayout.NORTH, graphComponent, 0, SpringLayout.NORTH, content);
//		contentLayout.putConstraint(SpringLayout.WEST, graphComponent, 0, SpringLayout.WEST, content);
//		contentLayout.putConstraint(SpringLayout.SOUTH, graphComponent, 0, SpringLayout.SOUTH, content);
//		contentLayout.putConstraint(SpringLayout.EAST, graphComponent, 0, SpringLayout.WEST, sidebar);
		contentLayout.putConstraint(SpringLayout.NORTH, graphViewer, 0, SpringLayout.NORTH, content);
		contentLayout.putConstraint(SpringLayout.WEST, graphViewer, 0, SpringLayout.WEST, content);
		contentLayout.putConstraint(SpringLayout.SOUTH, graphViewer, 0, SpringLayout.SOUTH, content);
		contentLayout.putConstraint(SpringLayout.EAST, graphViewer, 0, SpringLayout.WEST, sidebar);
		contentLayout.putConstraint(SpringLayout.NORTH, sidebar, 0, SpringLayout.NORTH, content);
		contentLayout.putConstraint(SpringLayout.SOUTH, sidebar, 0, SpringLayout.SOUTH, content);
		contentLayout.putConstraint(SpringLayout.EAST, sidebar, 0, SpringLayout.EAST, content);
		contentLayout.putConstraint(SpringLayout.WEST, sidebar, -300, SpringLayout.EAST, content);
		content.setLayout(contentLayout);
		
		SpringLayout sidebarLayout = new SpringLayout();
		sidebarLayout.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, sidebar);
		sidebarLayout.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, sidebar);
		sidebarLayout.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, sidebar);
		sidebarLayout.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, sidebar);
		sidebar.setLayout(sidebarLayout);
		
		setTitle("DEX Method Viewer");
		setMinimumSize(new Dimension(500, 200));
		setSize(new Dimension(800, 600));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private DirectedSparseGraph<String, String> testGraph() {
		DirectedSparseGraph<String, String> graph = new DirectedSparseGraph<>();
		graph.addVertex("A");
		graph.addVertex("B");
		graph.addEdge("A -> B", "A", "B");
		return graph;
	}

	private void setMethod(int methodIndex) {
		SortedMap<Integer, Instruction> code = null;
	
		MethodDescriptor desc = null;
		for (int i = methodIndex; code == null && i < methodList.size(); i++) {
			code = dex.getCode(methodList.get(i).methodid);
			desc = methodList.get(i);
		}

		CodeGraphGenerator generator = new CodeGraphGenerator(dex);

		if (code != null) {
			try {
				Function fn = generator.symbolicate(generator.transformToPseudoCode(code, (ClassMethodDescriptor) desc));
//				mxGraph graph = new JGraphXAdapter<GraphNode, GraphEdge>(fn.code);
		
//				graph.setCellsDeletable(false);
//				graph.setCellsDisconnectable(false);
//				graph.setCellsEditable(false);
				/*Map<String, Object> styles = graph.getStylesheet().getDefaultEdgeStyle();
				styles.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CURVE);
				graph.getStylesheet().setDefaultEdgeStyle(styles);*/
		
//				mxGraphLayout layout = new mxHierarchicalLayout(graph);
//				layout.execute(graph.getDefaultParent());
				
//				graphComponent.setGraph(graph);
				//graphModel.getGraphLayout().setGraph(fn.code);
			} catch (ProgramVerificationException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent item) {
		setMethod(item.getFirstIndex());
	}

}
