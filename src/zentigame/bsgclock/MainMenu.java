package zentigame.bsgclock;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import zentigame.AboutDialog;
import zentigame.SwingFunctions;

public class MainMenu {
	private static final String version = "1.3.0";
	
	//Workaround for ActionListeners
	private MainMenu self;
	
	private Image icon;
	
	private JFrame mainMenu;
	private Container cPane;
	
	private JTextField hours, minutes, seconds;
	private JLabel colon1, colon2;
	
	private JLabel hLabel, mLabel, sLabel;
	
	private JLabel statusLabel;
	
	private JCheckBox playSound, playTick, keepRunning, fullscreen;
	
	private JButton startButton, aboutButton;
	
	private AboutDialog aboutDialog;
	
	private Thread clockThread;
	
	public MainMenu() {
		try {
		    // Set System L&F
	        UIManager.setLookAndFeel(
	            UIManager.getSystemLookAndFeelClassName());
	    } 
	    catch (UnsupportedLookAndFeelException e) {
	       // handle exception
	    }
	    catch (ClassNotFoundException e) {
	       // handle exception
	    }
	    catch (InstantiationException e) {
	       // handle exception
	    }
	    catch (IllegalAccessException e) {
	       // handle exception
	    }
		
		icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/zentigame/bsgclock/BSGClock.png"));
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
		hours.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				if (hours.getText().length() > 2)
					hours.setText(hours.getText().substring(0, 2));
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
			
		});
		
		minutes.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				if (minutes.getText().length() > 1)
					minutes.setText(minutes.getText().substring(0, 1));
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
			
		});
		
		seconds.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (seconds.getText().length() > 1)
					seconds.setText(seconds.getText().substring(0, 1));
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
			
		});
		
		//startButton
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
			}
		});
		
		//aboutButton
		aboutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				aboutDialog.setVisible(true);
			}
		});
	}
	
	void cleanClockThread(){
		clockThread = null;
	}
	
	private void setLayout() {
		cPane = mainMenu.getContentPane();
		cPane.setLayout(new GridBagLayout());
		
		//Add the window components to the content pane
		
		//Countdown time fields
		SwingFunctions.addGBCToCP(cPane, hours, 0,0,1,GridBagConstraints.CENTER,GridBagConstraints.NONE);
		SwingFunctions.addGBCToCP(cPane, colon1, 1,0);
		SwingFunctions.addGBCToCP(cPane, minutes, 2,0,1,GridBagConstraints.CENTER,GridBagConstraints.NONE);
		SwingFunctions.addGBCToCP(cPane, colon2, 3,0);
		SwingFunctions.addGBCToCP(cPane, seconds, 4,0,1,GridBagConstraints.CENTER,GridBagConstraints.NONE);
		
		//Field descriptors
		SwingFunctions.addGBCToCP(cPane, hLabel, 0,1);
		SwingFunctions.addGBCToCP(cPane, mLabel, 2,1);
		SwingFunctions.addGBCToCP(cPane, sLabel, 4,1);

		//Checkboxes
		SwingFunctions.addGBCToCP(cPane, playSound, 0,2,6);
		SwingFunctions.addGBCToCP(cPane, playTick, 0,3,6);
		SwingFunctions.addGBCToCP(cPane, keepRunning, 0,4,6);
		SwingFunctions.addGBCToCP(cPane, fullscreen, 0,5,6);
		
		//Buttons
		SwingFunctions.addGBCToCP(cPane, startButton, 0,6,6);
		SwingFunctions.addGBCToCP(cPane, aboutButton, 0,7,6);

		//Status label
		SwingFunctions.addGBCToCP(cPane, statusLabel, 0,8,6);
	}
	
	void setStartButtonState(boolean state) {
		startButton.setEnabled(state);
	}
	
	Image getIcon() {
		return icon;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MainMenu().run(args);
	}

	private void run(String args[]) {
		//retrieveIcon();
		
		mainMenu.pack();
		mainMenu.setVisible(true);
	}

}
