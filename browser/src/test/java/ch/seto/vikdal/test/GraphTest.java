package ch.seto.vikdal.test;

import java.io.File;
import java.io.IOException;

import java.net.URISyntaxException;

import java.util.*;
import java.util.List;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jgrapht.ext.JGraphXAdapter;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dex.Dex;
import ch.seto.vikdal.dex.DexFormatException;
import ch.seto.vikdal.java.ClassDescriptor;
import ch.seto.vikdal.java.ClassMethodDescriptor;
import ch.seto.vikdal.java.MethodDescriptor;
import ch.seto.vikdal.java.transformers.CodeGraphGenerator;
import ch.seto.vikdal.java.transformers.Function;
import ch.seto.vikdal.java.transformers.GraphEdge;
import ch.seto.vikdal.java.transformers.GraphNode;
import ch.seto.vikdal.java.transformers.ProgramVerificationException;

@SuppressWarnings("serial")
public class GraphTest extends JFrame implements ListSelectionListener {

	public static void main(String[] args) {
		new GraphTest("/classes.dex").setVisible(true);
	}

	private Dex dex;
	private mxGraphComponent graphComponent;
	private JList<String> methodSelector;
	private List<ClassMethodDescriptor> methodList;

	private GraphTest(String fileName) {
		try {
			File dexFile = new File(getClass().getResource(fileName).toURI());
			dex = new Dex(dexFile);
			dex.parse();
		} catch (URISyntaxException e) {
			throw new RuntimeException("Invalid resource URI", e);
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

		graphComponent = new mxGraphComponent(new mxGraph());
		graphComponent.setConnectable(false);
		content.add(graphComponent);
		
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
		contentLayout.putConstraint(SpringLayout.NORTH, graphComponent, 0, SpringLayout.NORTH, content);
		contentLayout.putConstraint(SpringLayout.WEST, graphComponent, 0, SpringLayout.WEST, content);
		contentLayout.putConstraint(SpringLayout.SOUTH, graphComponent, 0, SpringLayout.SOUTH, content);
		contentLayout.putConstraint(SpringLayout.EAST, graphComponent, 0, SpringLayout.WEST, sidebar);
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
				mxGraph graph = new JGraphXAdapter<GraphNode, GraphEdge>(fn.code);
		
				graph.setCellsDeletable(false);
				graph.setCellsDisconnectable(false);
				graph.setCellsEditable(false);
				/*Map<String, Object> styles = graph.getStylesheet().getDefaultEdgeStyle();
				styles.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CURVE);
				graph.getStylesheet().setDefaultEdgeStyle(styles);*/
		
				mxGraphLayout layout = new mxHierarchicalLayout(graph);
				layout.execute(graph.getDefaultParent());
				
				graphComponent.setGraph(graph);
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
