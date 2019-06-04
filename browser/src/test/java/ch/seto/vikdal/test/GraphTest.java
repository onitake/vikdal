package ch.seto.vikdal.test;

import java.io.File;
import java.io.IOException;

import java.util.*;
import java.util.List;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ch.seto.vikdal.adapter.GraphvizAdapter;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dex.Dex;
import ch.seto.vikdal.dex.DexFormatException;
import ch.seto.vikdal.java.ClassDescriptor;
import ch.seto.vikdal.java.ClassMethodDescriptor;
import ch.seto.vikdal.java.MethodDescriptor;
import ch.seto.vikdal.java.transformers.CodeGraphGenerator;
import ch.seto.vikdal.java.transformers.Function;
import ch.seto.vikdal.java.transformers.ProgramVerificationException;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

@SuppressWarnings("serial")
public class GraphTest extends JFrame implements ListSelectionListener {

	public static void main(String[] args) {
		File dexFile = new File("/tmp/classes.dex");
		new GraphTest(dexFile).setVisible(true);
	}

	private Dex dex;
	private JLabel graphComponent;
	private JList<String> methodSelector;
	private List<ClassMethodDescriptor> methodList;

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

		graphComponent = new JLabel();
		JScrollPane scrolling = new JScrollPane(graphComponent);
		content.add(scrolling);
		
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
		contentLayout.putConstraint(SpringLayout.NORTH, scrolling, 0, SpringLayout.NORTH, content);
		contentLayout.putConstraint(SpringLayout.WEST, scrolling, 0, SpringLayout.WEST, content);
		contentLayout.putConstraint(SpringLayout.SOUTH, scrolling, 0, SpringLayout.SOUTH, content);
		contentLayout.putConstraint(SpringLayout.EAST, scrolling, 0, SpringLayout.WEST, sidebar);
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
				Graphviz graph = new GraphvizAdapter<>(fn.code).getGraph();
				graphComponent.setIcon(new ImageIcon(graph.render(Format.SVG).toImage()));
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
