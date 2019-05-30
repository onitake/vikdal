package ch.seto.vikdal.browser;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

class DescriptorRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 1L;
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		String name = ((DescriptorNode) value).getIcon();
		Icon icon = new ImageIcon(getClass().getResource("/images/" + name + ".png"));
		setIcon(icon);
		return this;
	}
}