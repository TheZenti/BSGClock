package de.zentigame;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class AboutDialog extends JDialog {
	@Serial
	private static final long serialVersionUID = 1L;

	private final String programName;
	private final String programVersion;
	private final String copyrightYear;
	private final String customMessage;

   public AboutDialog(JFrame parentFrame, String programName, String programVersion, String copyrightYear, String customMessage) {
		super(parentFrame, "About " + programName, true);
		this.programName = programName;
		this.programVersion = programVersion;
		this.copyrightYear = copyrightYear;
		this.customMessage = customMessage;
		initAboutDialog();
	}

	private void initAboutDialog() {
	    Container aboutDialogPane = getContentPane();
	    aboutDialogPane.setLayout(new GridBagLayout());
		JLabel programNameLabel = new JLabel("ZentiGame's " + programName);
	    programNameLabel.setFont(new Font(programNameLabel.getFont().getFontName(), Font.BOLD, 16));
		JLabel programVersionLabel = new JLabel("Version " + programVersion);
		JLabel programCopyrightLabel = new JLabel("Copyright " + copyrightYear + " Christopher \"ZentiGame\" Zentgraf");
		JLabel customMessageLabel = new JLabel((customMessage != null) ? customMessage : "");
		JButton okButton = new JButton("OK");
	    okButton.addActionListener(e -> dispose());

	    //Define layout for dialog
	    SwingFunctions.addGBCToCP(aboutDialogPane, programNameLabel, 0, 0, new Insets(5, 5, 0, 5));
	    SwingFunctions.addGBCToCP(aboutDialogPane, programVersionLabel, 0, 1, new Insets(0, 5, 0, 5));
	    SwingFunctions.addGBCToCP(aboutDialogPane, programCopyrightLabel, 0, 2, new Insets(0, 5, 5, 5));
	    SwingFunctions.addGBCToCP(aboutDialogPane, customMessageLabel, 0, 3);
	    SwingFunctions.addGBCToCP(aboutDialogPane, okButton, 0, 4);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		setLocationByPlatform(true);
		pack();
	}


}
