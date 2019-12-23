package ch.seto.vikdal.browser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.jgrapht.ext.JGraphXAdapter;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import ch.seto.vikdal.ProgressListener;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dex.Dex;
import ch.seto.vikdal.dex.DexFormatException;
import ch.seto.vikdal.java.ClassDescriptor;
import ch.seto.vikdal.java.ClassMethodDescriptor;
import ch.seto.vikdal.java.Descriptor;
import ch.seto.vikdal.java.DescriptorUtils;
import ch.seto.vikdal.java.FieldDescriptor;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.Decompiler;
import ch.seto.vikdal.java.transformers.Function;
import ch.seto.vikdal.java.transformers.GraphEdge;
import ch.seto.vikdal.java.transformers.GraphNode;
import ch.seto.vikdal.java.transformers.ProgramVerificationException;

// TODO Static value lookups return the type of the
// value, not the containing class. Fix that!
// Ex.: java.lang.String.MANUFACTURER instead of
// java.lang.String android.os.Build.MANUFACTURER
public class Browser {
	// if true, load a test file from a fixed location
	private static final boolean LOAD_DEBUG = false;

	private Dex dex;
	private List<ClassDescriptor> classList;

	private JFrame frame;
	private mxGraphComponent graphComponent;
	private JMenuBar menuBar;
	private JLabel statusBarLabel;
	private JProgressBar progressBar;
	private JTree classTree;
	private JLabel objectDescription;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// Force some modern style settings
					System.setProperty("awt.useSystemAAFontSettings", "on");
					System.setProperty("swing.aatext", "true");
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					// Ignore - if we can't find a native L&F, just use the default 
				}
				try {
					Browser window = new Browser();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Browser() {
		classList = new ArrayList<ClassDescriptor>();
		initialize();
		if (LOAD_DEBUG) {
			// DEBUG try to load a local classes.dex, ignore if non-existent
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						//load(new File(getClass().getResource("/classes.dex").toURI()));
						load(new File("/tmp/app.apk"));
					} catch (Exception e) { }
				}
			});
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("DEX Method Viewer");
		frame.setMinimumSize(new Dimension(500, 400));
		frame.setSize(new Dimension(800, 600));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		SpringLayout contentLayout = new SpringLayout();
		frame.getContentPane().setLayout(contentLayout);
		
		classTree = new JTree(new DefaultMutableTreeNode());
		classTree.setRootVisible(false);
		classTree.setShowsRootHandles(true);
		classTree.setCellRenderer(new DescriptorRenderer());
		classTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent ev) {
				if (ev.isAddedPath()) {
					DescriptorNode node = (DescriptorNode) ev.getPath().getLastPathComponent();
					objectDescription.setText(node.getQualifiedDescription());
					String icon = node.getIcon();
					if (icon != null) {
						objectDescription.setIcon(new ImageIcon(getClass().getResource("/images/" + icon + ".png")));
					}
					switch (node.getType()) {
					case CLASS:
						break;
					case FIELD:
						break;
					case METHOD:
						ClassMethodDescriptor method = (ClassMethodDescriptor) node.getUserObject();
						displayMethod(method);
						break;
					case PACKAGE:
						break;
					default:
						break;
					}
				}
			}
		});
		
		JScrollPane classScrollPane = new JScrollPane(classTree);
		
		objectDescription = new JLabel();
		contentLayout.putConstraint(SpringLayout.NORTH, objectDescription, 6, SpringLayout.NORTH, frame.getContentPane());
		contentLayout.putConstraint(SpringLayout.WEST, objectDescription, 6, SpringLayout.WEST, frame.getContentPane());
		contentLayout.putConstraint(SpringLayout.EAST, objectDescription, 6, SpringLayout.EAST, frame.getContentPane());
		frame.getContentPane().add(objectDescription);
		
		graphComponent = new mxGraphComponent(new mxGraph());
		graphComponent.setConnectable(false);

		JSplitPane splitPane = new JSplitPane();
		contentLayout.putConstraint(SpringLayout.NORTH, splitPane, 26, SpringLayout.NORTH, frame.getContentPane());
		contentLayout.putConstraint(SpringLayout.WEST, splitPane, 6, SpringLayout.WEST, frame.getContentPane());
		contentLayout.putConstraint(SpringLayout.EAST, splitPane, -6, SpringLayout.EAST, frame.getContentPane());
		frame.getContentPane().add(splitPane);
		splitPane.setLeftComponent(classScrollPane);
		splitPane.setRightComponent(graphComponent);
		splitPane.setResizeWeight(0);
		splitPane.setDividerLocation(300);
		
		JPanel statusBarPanel = new JPanel();
		statusBarPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		contentLayout.putConstraint(SpringLayout.SOUTH, splitPane, -6, SpringLayout.NORTH, statusBarPanel);
		contentLayout.putConstraint(SpringLayout.NORTH, statusBarPanel, -20, SpringLayout.SOUTH, frame.getContentPane());
		contentLayout.putConstraint(SpringLayout.WEST, statusBarPanel, 0, SpringLayout.WEST, frame.getContentPane());
		contentLayout.putConstraint(SpringLayout.SOUTH, statusBarPanel, 0, SpringLayout.SOUTH, frame.getContentPane());
		contentLayout.putConstraint(SpringLayout.EAST, statusBarPanel, 0, SpringLayout.EAST, frame.getContentPane());
		frame.getContentPane().add(statusBarPanel);
		statusBarPanel.setLayout(new BorderLayout(0, 0));
		
		SpringLayout statusBarLayout = new SpringLayout();
		statusBarPanel.setLayout(statusBarLayout);
		statusBarLabel = new JLabel("");
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setFont(new Font(Font.DIALOG, Font.BOLD, 9));
		statusBarLayout.putConstraint(SpringLayout.NORTH, statusBarLabel, 0, SpringLayout.NORTH, statusBarPanel);
		statusBarLayout.putConstraint(SpringLayout.SOUTH, statusBarLabel, 0, SpringLayout.SOUTH, statusBarPanel);
		statusBarLayout.putConstraint(SpringLayout.WEST, statusBarLabel, 0, SpringLayout.WEST, statusBarPanel);
		statusBarLayout.putConstraint(SpringLayout.EAST, statusBarLabel, -6, SpringLayout.WEST, progressBar);
		statusBarLayout.putConstraint(SpringLayout.NORTH, progressBar, 0, SpringLayout.NORTH, statusBarPanel);
		statusBarLayout.putConstraint(SpringLayout.SOUTH, progressBar, 0, SpringLayout.SOUTH, statusBarPanel);
		statusBarLayout.putConstraint(SpringLayout.EAST, progressBar, 0, SpringLayout.EAST, statusBarPanel);
		statusBarLayout.putConstraint(SpringLayout.WEST, progressBar, -100, SpringLayout.EAST, statusBarPanel);
		statusBarPanel.add(statusBarLabel);
		statusBarPanel.add(progressBar);
		
		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		menuBar.add(fileMenu);
		
		JMenuItem openMenuItem = new JMenuItem("Open");
		openMenuItem.addActionListener(new ActionListener() {
			private File currentDirectory;
			public void actionPerformed(ActionEvent ev) {
				JFileChooser chooser = new JFileChooser(currentDirectory);
				chooser.setMultiSelectionEnabled(false);
				chooser.setAcceptAllFileFilterUsed(true);
				FileNameExtensionFilter dexfilter = new FileNameExtensionFilter("DEX Archives", "dex");
				chooser.addChoosableFileFilter(dexfilter);
				FileNameExtensionFilter apkfilter = new FileNameExtensionFilter("APK Archives", "apk");
				chooser.addChoosableFileFilter(apkfilter);
				chooser.setFileFilter(dexfilter);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int retval = chooser.showOpenDialog(frame);
				if (retval == JFileChooser.APPROVE_OPTION) {
					final File chosenFile = chooser.getSelectedFile();
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							load(chosenFile);
						}
					});
				}
				currentDirectory = chooser.getCurrentDirectory();
			}
		});
		openMenuItem.setMnemonic('o');
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		fileMenu.add(openMenuItem);
		
		JSeparator separator = new JSeparator();
		fileMenu.add(separator);
		
		JMenuItem quitMenuItem = new JMenuItem("Quit");
		quitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				System.exit(0);
			}
		});
		quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		quitMenuItem.setMnemonic('q');
		fileMenu.add(quitMenuItem);
	}

	/**
	 * Evaluate a method and display the code graph
	 */
	protected void displayMethod(ClassMethodDescriptor method) {
		Decompiler decompiler = new Decompiler(dex);

		mxGraph graph = new mxGraph();
		if (method != null) {
			SortedMap<Integer, Instruction> code = dex.getCode(method.methodid);
			if (code != null) {
				try {
					Function fn = decompiler.graphify(code, method);
					fn = decompiler.transform(fn);
					graph = new JGraphXAdapter<GraphNode, GraphEdge>(fn.code);
			
					graph.setCellsDeletable(false);
					graph.setCellsDisconnectable(false);
					graph.setCellsEditable(false);
					
					// Turn all edges into curves
					// Doesn't work very well, messes up the arrows
					/*Map<String, Object> styles = graph.getStylesheet().getDefaultEdgeStyle();
					styles.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CURVE);
					graph.getStylesheet().setDefaultEdgeStyle(styles);*/
			
					mxGraphLayout layout = new mxHierarchicalLayout(graph);
					layout.execute(graph.getDefaultParent());
				
					statusBarLabel.setText("Showing method " + method.name);
				} catch (ProgramVerificationException e) {
					statusBarLabel.setText("Method " + method.name + " failed verification, can't show code graph: " + e);
				}
			} else {
				statusBarLabel.setText("No code graph found for method " + method.name);
			}
		} else {
			statusBarLabel.setText("No method definition found");
		}
		graphComponent.setGraph(graph);
		graphComponent.refresh();
	}
	
	/**
	 * Load a new DEX or APK file.
	 * @param filename the file reference
	 */
	protected void load(final File filename) {
		final SwingWorker<Dex, Object> worker = new SwingWorker<Dex, Object>() {
			private final Pattern pattern = Pattern.compile(".*\\.apk", Pattern.CASE_INSENSITIVE);
			public Dex doInBackground() {
				final boolean isjar = pattern.matcher(filename.getName()).matches();
				ProgressListener listener = new ProgressListener() {
					public void progressUpdated(float progress) {
						if (isjar) {
							setProgress((int) (progress * 90 + 10));
						} else {
							setProgress((int) (progress * 100));
						}
					}
				};
				Dex dex = null;
				setProgress(0);
				if (isjar) {
					// Try to load as jar/apk first
					JarFile jar = null;
					try {
						jar = new JarFile(filename, false);
						ZipEntry entry = jar.getEntry("classes.dex");
						if (entry == null) {
							throw new RuntimeException("No classes.dex found in APK");
						} else {
							InputStream input = jar.getInputStream(entry);
							long size = entry.getSize();
							if (size > Integer.MAX_VALUE) {
								throw new RuntimeException("DEX entry is too large. Please extract the APK manually and load classes.dex directly.");
							} else {
								OutputStream temp = null;
								try {
									byte[] classes = new byte[(int) size];
									int offset = 0;
									while (size > 0) {
										int rdbytes = input.read(classes, offset, size > 4096 ? 4096 : (int) size);
										if (rdbytes == -1) {
											size = -1;
										} else {
											size -= rdbytes;
											offset += rdbytes;
										}
										//System.out.println("Read " + rdbytes + ", offset is " + offset + ", " + size + " bytes left");
									}
									//temp = new FileOutputStream("/tmp/classes.dex");
									//temp.write(classes);
									dex = new Dex(classes);
								} catch (OutOfMemoryError e) {
									throw new RuntimeException("Out of memory while trying to extract DEX entry. Please extract the APK manually and load classes.dex directly.");
								} finally {
									if (temp != null) {
										temp.close();
									}
								}
							}
						}
					} catch (IOException e) {
						throw new RuntimeException("Can't read APK archive", e);
					} finally {
						if (jar != null) {
							try {
								jar.close();
							} catch (IOException e) { }
						}
					}
				}
				if (dex == null) {
					dex = new Dex(filename);
				}
				dex.addProgressListener(listener);
				try {
					dex.parse();
				} catch (IOException e) {
					throw new RuntimeException("Can't read DEX archive", e);
				} catch (DexFormatException e) {
					throw new RuntimeException("Invalid DEX archive", e);
				} finally {
					dex.removeProgressListener(listener);
				}
				return dex;
			}
		};
		worker.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if ("progress".equals(evt.getPropertyName())) {
					progressBar.setValue((Integer) evt.getNewValue());
				} else if ("state".equals(evt.getPropertyName())) {
					switch ((StateValue) evt.getNewValue()) {
					case DONE:
						try {
							dex = worker.get();
							List<ClassDescriptor> classes = new ArrayList<ClassDescriptor>();
							classList.clear();
							for (int i = 0; i < dex.numberOfTypes(); i++) {
								ClassDescriptor klass = dex.lookupClass(i);
								if (klass != null) {
									// TODO only add class if it contains methods with code
									classList.add(klass);
									classes.add(klass);
								}
							}
							classTree.setModel(new DefaultTreeModel(createTree(classes, dex)));
							statusBarLabel.setText("Loaded " + filename);
						} catch (InterruptedException e) {
							// ignore
						} catch (ExecutionException e) {
							statusBarLabel.setText("Error loading " + filename + ": " + e.getCause().getMessage());
						}
						break;
					case STARTED:
						statusBarLabel.setText("Loading " + filename + "...");
						break;
					default:
						break;
					}
				}
			}
		});
		worker.execute();
	}

	private static TreeNode createTree(List<ClassDescriptor> classes, SymbolTable symbols) {
		class MapNode {
			private Map<String, MapNode> map;
			private DescriptorNode tree;
			public MapNode(String name) {
				tree = new DescriptorNode(name);
			}
			@SuppressWarnings("unused")
			public boolean isLeaf() {
				return map == null;
			}
			public MapNode getChild(String key) {
				if (map == null) {
					map = new HashMap<String, MapNode>();
				}
				if (map.containsKey(key)) {
					return map.get(key);
				} else {
					MapNode child = new MapNode(key);
					map.put(key, child);
					tree.add(child.getTree());
					return child;
				}
			}
			@SuppressWarnings("unused")
			public void removeChild(String key) {
				MapNode child = map.remove(key);
				if (child != null) {
					tree.remove(child.getTree());
				}
			}
			public DefaultMutableTreeNode getTree() {
				return tree;
			}
		}
		MapNode root = new MapNode("");
		for (ClassDescriptor klass : classes) {
			String name = klass.toString(symbols);
			MapNode current = root;
			for (StringTokenizer tokenizer = new StringTokenizer(name, "."); tokenizer.hasMoreTokens();) {
				String key = tokenizer.nextToken();
				current = current.getChild(key);
			}
			current.getTree().setUserObject(klass);
			((DescriptorNode) current.getTree()).setQualifiedDescription(klass.toString(symbols, DescriptorUtils.flagList(Descriptor.EXTENDS, true, Descriptor.SHORT_TYPES, true)));
			for (ClassMethodDescriptor method : klass.methods) {
				// TODO: Short type names may create ambiguous keys, consider using a multimap for the tree.
				// Also note that static and instance methods may have the same signature.
				String methodname = method.toString(symbols, DescriptorUtils.flagList(Descriptor.SHORT_TYPES, true, Descriptor.FULLY_QUALIFIED, false));
				MapNode methodnode = current.getChild(methodname);
				methodnode.getTree().setUserObject(method);
				((DescriptorNode) methodnode.getTree()).setQualifiedDescription(method.toString(symbols));
			}
			for (FieldDescriptor field : klass.fields) {
				String fieldname = field.toString(symbols, DescriptorUtils.flagList(Descriptor.SHORT_TYPES, true, Descriptor.FULLY_QUALIFIED, false, Descriptor.FIELD_VALUE, false));
				MapNode fieldnode = current.getChild(fieldname);
				fieldnode.getTree().setUserObject(field);
				((DescriptorNode) fieldnode.getTree()).setQualifiedDescription(field.toString(symbols));
			}
		}
		return root.getTree();
	}
}
