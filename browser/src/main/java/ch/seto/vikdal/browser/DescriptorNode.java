package ch.seto.vikdal.browser;

import javax.swing.tree.DefaultMutableTreeNode;

import ch.seto.vikdal.java.ClassDescriptor;
import ch.seto.vikdal.java.FieldDescriptor;
import ch.seto.vikdal.java.MethodDescriptor;

class DescriptorNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 2L;
	
	public enum Type {
		UNKNOWN,
		PACKAGE,
		CLASS,
		METHOD,
		FIELD,
	}
	
	private String description;
	private String qualified;
	private DescriptorNode.Type type;
	private String icon;
	
	public DescriptorNode(String description) {
		super();
		this.description = description;
		setType(Type.PACKAGE);
	}
	
	public DescriptorNode.Type getType() {
		return type;
	}

	public void setType(DescriptorNode.Type type) {
		this.type = type;
		switch (type) {
		case CLASS:
			setIcon("class");
			break;
		case FIELD:
			setIcon("variablePublic");
			break;
		case METHOD:
			setIcon("methodPublic");
			break;
		case PACKAGE:
			setIcon("package");
			break;
		default:
			setIcon(null);
			break;
		}
	}

	public void setIcon(String name) {
		this.icon = name;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public void setQualifiedDescription(String qualified) {
		this.qualified = qualified;
	}
	
	public String getQualifiedDescription() {
		if (qualified == null) {
			StringBuilder builder = new StringBuilder();
			builder.insert(0, toString());
			DescriptorNode current = (DescriptorNode) getParent();
			while (current != null) {
				if (current.getParent() != null) {
					builder.insert(0, '.');
					builder.insert(0, current.toString());
				}
				current = (DescriptorNode) current.getParent();
			}
			return builder.toString();
		} else {
			return qualified;
		}
	}

	@Override
	public void setUserObject(Object userObject) {
		super.setUserObject(userObject);
		// TODO add static/private/protected evaluation
		if (userObject == null) {
			setType(Type.PACKAGE);
		} else if (userObject instanceof ClassDescriptor) {
			setType(Type.CLASS);
		} else if (userObject instanceof FieldDescriptor) {
			setType(Type.FIELD);
		} else if (userObject instanceof MethodDescriptor) {
			setType(Type.METHOD);
		} else {
			setType(Type.UNKNOWN);
		}
	}

	@Override
	public String toString() {
		return description;
	}
}