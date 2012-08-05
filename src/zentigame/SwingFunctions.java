package zentigame;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class SwingFunctions {

	public static void enableGBC(Container contentPane) {
		GridBagLayout g = new GridBagLayout();
		contentPane.setLayout(g);
	}

	public static void addGBCToCP(Container contentPane, Component component,
			int x, int y, Insets inset) {
		addGBCToCP(contentPane, component, x, y, 1, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, inset);
	}
	
	public static void addGBCToCP(Container contentPane, Component component,
			int x, int y, double weightx, double weighty) {
		addGBCToCP(contentPane, component, x, y, 1, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), weightx, weighty);
	}

	public static void addGBCToCP(Container contentPane, Component component,
			int x, int y) {
		addGBCToCP(contentPane, component, x, y, 1);
	}

	public static void addGBCToCP(Container contentPane, Component component,
			int x, int y, int gridWidth) {
		addGBCToCP(contentPane, component, x, y, gridWidth,
				GridBagConstraints.WEST);
	}

	public static void addGBCToCP(Container contentPane, Component component,
			int x, int y, int gridWidth, int align) {
		addGBCToCP(contentPane, component, x, y, gridWidth, align,
				GridBagConstraints.BOTH);
	}

	public static void addGBCToCP(Container contentPane, Component component,
			int x, int y, int gridWidth, int align, int fillBehaviour) {
		addGBCToCP(contentPane, component, x, y, gridWidth, align,
				fillBehaviour, new Insets(5, 5, 5, 5));
	}

	public static void addGBCToCP(Container contentPane, Component component,
			int x, int y, int gridWidth, int align, int fillBehaviour,
			Insets inset) {
		addGBCToCP(contentPane, component, x, y, gridWidth, align,
				fillBehaviour, inset, 0D, 0D);
	}

	public static void addGBCToCP(Container contentPane, Component component,
			int x, int y, int gridWidth, int align, int fillBehaviour,
			Insets inset, double weightx, double weighty) {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		// With this layout engine, the window is pretty much like a excel table
		gridBagConstraints.gridx = x;
		gridBagConstraints.gridy = y;
		// The component with the highest weight will get unused extra space
		gridBagConstraints.weightx = weightx;
		gridBagConstraints.weighty = weighty;
		// Defines Padding around the object
		gridBagConstraints.insets = inset;
		// Defines spread over multiple cells
		gridBagConstraints.gridwidth = gridWidth;
		// Behaviour when the component is smaller than its display area
		gridBagConstraints.anchor = align;
		// Behaviour when the component's display area is larger than the
		// component's requested size
		gridBagConstraints.fill = fillBehaviour;
		contentPane.add(component, gridBagConstraints);
	}
	
	public static void addGBCToCP(Container contentPane, Component component, GridBagConstraints gbc)
	{
		contentPane.add(component, gbc);
	}
}
