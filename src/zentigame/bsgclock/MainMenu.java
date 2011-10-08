package zentigame.bsgclock;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;

import zentigame.AboutDialog;
import zentigame.SwingFunctions;

public class MainMenu {
	private static final String version = "1.1.2.68";
	
	private JFrame mainMenu;
	private Container cPane;
	
	private JTextField hours, minutes, seconds;
	private JLabel colon1, colon2;
	
	private JLabel hLabel, mLabel, sLabel;
	
	private JLabel statusLabel;
	
	private JCheckBox playSound, playTick, keepRunning;
	
	private JButton startButton, aboutButton;
	
	private AboutDialog aboutDialog;
	
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
		
		initMainMenu();
		aboutDialog = new AboutDialog(mainMenu, "BSGClock", version, "2011");
	}
	
	private void initMainMenu() {
		mainMenu = new JFrame("BSGClock");
		mainMenu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainMenu.setResizable(false);
		mainMenu.setLocationByPlatform(true);
		mainMenu.setIconImage(retrieveIcon());
		
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
		
		startButton = new JButton("Mr. Gaeta, start the clock!");
		aboutButton = new JButton("About this program");
		
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
				Thread t1 = new Thread(new BSGClock(hS, mS, sS, playSound.isSelected(), playTick.isSelected(), keepRunning.isSelected()));
				t1.start();
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
	
	private void setLayout() {
		cPane = mainMenu.getContentPane();
		cPane.setLayout(new GridBagLayout());
		
		//Add the window components to the content pane
		
		//Countdown time fields
		SwingFunctions.addGridBagContraintsToContentPane(cPane, hours, 0,0,1,GridBagConstraints.CENTER,GridBagConstraints.NONE);
		SwingFunctions.addGridBagContraintsToContentPane(cPane, colon1, 1,0);
		SwingFunctions.addGridBagContraintsToContentPane(cPane, minutes, 2,0,1,GridBagConstraints.CENTER,GridBagConstraints.NONE);
		SwingFunctions.addGridBagContraintsToContentPane(cPane, colon2, 3,0);
		SwingFunctions.addGridBagContraintsToContentPane(cPane, seconds, 4,0,1,GridBagConstraints.CENTER,GridBagConstraints.NONE);
		
		//Field descriptors
		SwingFunctions.addGridBagContraintsToContentPane(cPane, hLabel, 0,1);
		SwingFunctions.addGridBagContraintsToContentPane(cPane, mLabel, 2,1);
		SwingFunctions.addGridBagContraintsToContentPane(cPane, sLabel, 4,1);

		//Checkboxes
		SwingFunctions.addGridBagContraintsToContentPane(cPane, playSound, 0,2,6);
		SwingFunctions.addGridBagContraintsToContentPane(cPane, playTick, 0,3,6);
		SwingFunctions.addGridBagContraintsToContentPane(cPane, keepRunning, 0,4,6);
		
		//Buttons
		SwingFunctions.addGridBagContraintsToContentPane(cPane, startButton, 0,5,6);
		SwingFunctions.addGridBagContraintsToContentPane(cPane, aboutButton, 0,6,6);

		//Status label
		SwingFunctions.addGridBagContraintsToContentPane(cPane, statusLabel, 0,7,6);
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

	private Image retrieveIcon() {
		File file;
		try {
			file = new File(System.getProperty("launch4j.exefile"));
		} catch (Exception e) {
			return null;
		}
	
	    // Get metadata and create an icon
	    sun.awt.shell.ShellFolder sf = null;
		try {
			sf = sun.awt.shell.ShellFolder.getShellFolder(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return sf.getIcon(true);
	}

}
