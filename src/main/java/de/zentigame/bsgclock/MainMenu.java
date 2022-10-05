package de.zentigame.bsgclock;

import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import de.zentigame.AboutDialog;
import de.zentigame.SF;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;

public class MainMenu {
	private static final String version = "1.4.0";

	//Workaround for ActionListeners
	private MainMenu self;

	private Image icon;

	private JFrame mainMenu;

	private JTextField hours, minutes, seconds;
	private JLabel colon1, colon2;

	private JLabel hLabel, mLabel, sLabel;

	private JLabel statusLabel;

	private JCheckBox playSound, playTick, keepRunning, fullscreen;

	private JButton startButton, aboutButton;

	private final AboutDialog aboutDialog;

	private Thread clockThread;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MainMenu().run();
	}

	private void run() {
		//retrieveIcon();

		mainMenu.pack();
		mainMenu.setVisible(true);
	}

	public MainMenu() {
		try {
		    // Set System L&F
	        UIManager.setLookAndFeel(
	            UIManager.getSystemLookAndFeelClassName());
	    }
	    catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
	       // handle exception
	    }

		icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/BSGClock.png"));
		initMainMenu();
		String aboutDialogMessage = "This program is using LWJGL, http://lwjgl.org";
		aboutDialog = new AboutDialog(mainMenu, "BSGClock", version, "2011", aboutDialogMessage);
	}

	private void initMainMenu() {
		mainMenu = new JFrame("BSGClock");
		mainMenu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainMenu.setResizable(false);
		mainMenu.setLocationByPlatform(true);
		mainMenu.setIconImage(icon);

		hours = new JTextField("000", 3);
		minutes = new JTextField("33", 2);
		seconds = new JTextField("00", 2);

		colon1 = new JLabel(":");
		colon2 = new JLabel(":");

		hLabel = new JLabel("hours");
		mLabel = new JLabel("minutes");
		sLabel = new JLabel("seconds");

		statusLabel = new JLabel("Ready.");

		playSound = new JCheckBox("Play sounds", true);
		playTick = new JCheckBox("Play tick sound every second", true);
		keepRunning = new JCheckBox("Keep clock running");
		fullscreen = new JCheckBox("Fullscreen", true);

		startButton = new JButton("Mr. Gaeta, start the clock!");
		aboutButton = new JButton("About this program");

		self = this;

		addActionListeners();

		setLayout();
	}

	private void addActionListeners() {
		addKeyListener(hours, 2);

		addKeyListener(minutes, 1);

		addKeyListener(seconds, 1);

		//startButton
		startButton.addActionListener(e -> {
			@SuppressWarnings("unused")
			int h, m, s;
			try {
				h = Integer.parseInt(hours.getText());
				m = Integer.parseInt(minutes.getText());
				s = Integer.parseInt(seconds.getText());
			} catch (Exception e1) {
				statusLabel.setText("You've not entered plain numbers");
				hours.setText("000");
				minutes.setText("33");
				seconds.setText("00");
				mainMenu.pack();
				return;
			}
			String hS, mS, sS;
			hS = hours.getText();
			mS = minutes.getText();
			sS = seconds.getText();

			if (hS.length() < 1)
				hS = "000";
			if (hS.length() < 2)
				hS = "00" + hS;
			if (hS.length() < 3)
				hS = "0" + hS;

			if (mS.length() < 1)
				mS = "00";
			if (mS.length() < 2)
				mS = "0" + mS;

			if (sS.length() < 1)
				sS = "00";
			if (sS.length() < 2)
				sS = "0" + sS;
			clockThread = new Thread(new BSGClock(self, hS, mS, sS, playSound.isSelected(), playTick.isSelected(), keepRunning.isSelected(), fullscreen.isSelected()));
			setStartButtonState(false);
			clockThread.start();
		});

		//aboutButton
		aboutButton.addActionListener(e -> aboutDialog.setVisible(true));

		mainMenu.addWindowStateListener(e -> {
			if (e.getID() == WindowEvent.WINDOW_CLOSED || e.getID() == WindowEvent.WINDOW_CLOSING) {
				try {
					Display.destroy();
					AL.destroy();
				} catch (Exception ignored) {}
			}
		});
	}

	private void addKeyListener(JTextField field, int maxLength) {
		field.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				int length = field.getText().length();
				if (length > maxLength)
					field.setText(field.getText().substring(length - 1, length));
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

		});
	}

	void cleanClockThread(){
		clockThread = null;
		System.gc();
	}

	private void setLayout() {
		Container cPane = mainMenu.getContentPane();
		cPane.setLayout(new GridBagLayout());

		//Add the window components to the content pane

		//Countdown time fields
		SF.addGBCToCP(cPane, hours, 0, 0, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE);
		SF.addGBCToCP(cPane, colon1, 1, 0);
		SF.addGBCToCP(cPane, minutes, 2, 0, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE);
		SF.addGBCToCP(cPane, colon2, 3, 0);
		SF.addGBCToCP(cPane, seconds, 4, 0, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE);

		//Field descriptors
		SF.addGBCToCP(cPane, hLabel, 0, 1);
		SF.addGBCToCP(cPane, mLabel, 2, 1);
		SF.addGBCToCP(cPane, sLabel, 4, 1);

		//Checkboxes
		SF.addGBCToCP(cPane, playSound, 0, 2, 6);
		SF.addGBCToCP(cPane, playTick, 0, 3, 6);
		SF.addGBCToCP(cPane, keepRunning, 0, 4, 6);
		SF.addGBCToCP(cPane, fullscreen, 0, 5, 6);

		//Buttons
		SF.addGBCToCP(cPane, startButton, 0, 6, 6);
		SF.addGBCToCP(cPane, aboutButton, 0, 7, 6);

		//Status label
		SF.addGBCToCP(cPane, statusLabel, 0, 8, 6);
	}

	void setStartButtonState(boolean state) {
		startButton.setEnabled(state);
	}

}
