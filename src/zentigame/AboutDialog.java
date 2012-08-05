package zentigame;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class AboutDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private JLabel programNameLabel, programVersionLabel, programCopyrightLabel, customMessageLabel;
	private JButton okButton;
	
	private String programName;
	private String programVersion;
	private String copyrightYear;
	private String customMessage;

	public AboutDialog(JFrame parentFrame, String programName, String programVersion, String copyrightYear) {
		this(parentFrame, programName, programVersion, copyrightYear, null);
	}
	
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
	    programNameLabel = new JLabel("ZentiGame's " + programName);
	    programNameLabel.setFont(new Font(programNameLabel.getFont().getFontName(), Font.BOLD, 16));
	    programVersionLabel = new JLabel("Version " + programVersion);
	    programCopyrightLabel = new JLabel("Copyright " + copyrightYear + " Christopher \"ZentiGame\" Zentgraf");
	    customMessageLabel = new JLabel((customMessage != null) ? customMessage : "");
	    okButton = new JButton("OK");
	    okButton.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		dispose();
	    	}
	    });

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
