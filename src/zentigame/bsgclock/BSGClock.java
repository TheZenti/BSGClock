package zentigame.bsgclock;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import org.lwjgl.input.Keyboard;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.util.ResourceLoader;

import zentigame.sound.SoundPlayer;
import de.matthiasmann.twl.utils.PNGDecoder;


//TODO: Overlay clock, which minimizes on a key.
public class BSGClock implements Runnable {
	
	private static float ANGLE = (float) (0.5 % 360);
	private static float ANGLER = (float) (ANGLE * (Math.PI / 180));
	private static float DEGREES90R = (float) (90 * (Math.PI / 180));
	
	private static final String GAME_TITLE = "BSGClock";
	private static final int FRAMERATE = 60;
	
	private MainMenu parent;

	private Audio clockRunning;
	private Audio clockExpired;
	private Audio clockTick;
	
	private int width, height;
	private float bitSize;
	private float partHeight, sliceLength;
	
	private long countdown;
	private boolean playSound;
	private boolean playTick;
	private boolean keepRunning;
	private boolean finished;
	private boolean fullScreen;
	
	private boolean showSign;

	private float[] currentColor;

	private String initialHours, initialMinutes, initialSeconds;

	private Timer countdownSetter, counter, colorFlash;

	public BSGClock(MainMenu parentWindow, String hours, String minutes, String seconds,
			boolean playSoundOnExpiration, boolean playTickSoundEverySecond,
			boolean keepClockRunningAfterExpiration, boolean fullscreen) {
		parent = parentWindow;
		initialHours = hours;
		initialMinutes = minutes;
		initialSeconds = seconds;
		playSound = playSoundOnExpiration;
		playTick = playTickSoundEverySecond;
		keepRunning = keepClockRunningAfterExpiration;
		fullScreen = fullscreen;
		countdown = 0L;
		showSign = false;
		currentColor = new float[] {1f, 0.67058823529411764705882352941176f, 0.06666666666666666666666666666667f};
		countdownSetter = new Timer();
		counter = new Timer();
		colorFlash = new Timer();
	}

	public void init() throws Exception {
		// Create a fullscreen window with 1:1 orthographic 2D projection
		// (default)
		Display.setTitle(GAME_TITLE);
		Display.setFullscreen(fullScreen);
		Display.setResizable(!fullScreen);
		try {
			Display.setIcon(retrieveIcon());
		} catch (Exception e) {}
		
		// Enable vsync if we can (due to how OpenGL works, it cannot be
		// guarenteed to always work)
		Display.setVSyncEnabled(true);
	
		try{
			Display.create(new PixelFormat(8,0,0,16));
		}
		catch(Exception e)
		{
			Display.create();
		}
		System.out.println("Initializing countdown window, " + Display.getWidth() + "x" + Display.getHeight() + ", " + ((Display.isFullscreen()) ? "fullscreen mode" : "window mode"));
		height = Display.getDisplayMode().getHeight();
		width = Display.getDisplayMode().getWidth();
		partHeight = height / 2;
		sliceLength = partHeight / 152;
		bitSize = width / 120;
		
		while(true) {
			try {
				clockRunning = AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("clockRunning.ogg"));
				clockExpired = AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("clockExpired.ogg"));
				clockTick = AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("clockTick.ogg"));
				break;
			} catch (IOException e) {
				e.printStackTrace();
				AL.create();
				AudioLoader.update();
			}
		}
	}

	@Override
	public void run() {
		try {
			init();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		countdownSetter.scheduleAtFixedRate(new SetStartingCountdown(), 100, 150);
		while (!finished) {
			// Always call Window.update(), all the time - it does some behind
			// the
			// scenes work, and also displays the rendered output
			Display.update();
			
			if(Display.wasResized())
			{
				height = Display.getHeight();
				width = Display.getWidth();
				// Debug call for Display resolution
				// System.out.println("Resolution: " + height + "x" + width);
				partHeight = height / 2;
				sliceLength = partHeight / 152;
				bitSize = width / 120;
				GL11.glViewport(0, 0, width, height);
			}
	
			if (Display.isCloseRequested()) {
				finished = true;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
				finished = true;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_F12))
				saveFrameAsPNG(Long.toString(countdown) + ".png");
	
			// The window is in the foreground, so we should play the game
			if (Display.isActive()) {
				render();
				Display.sync(FRAMERATE);
			}
	
			// The window is not in the foreground, so we can allow other stuff
			// to run and
			// infrequently update
			else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
	
				// Only bother rendering if the window is visible or dirty
				if (Display.isVisible() || Display.isDirty()) {
					render();
				}
			}
		}
		System.out.println("Finalizing countdown window");
		Display.destroy();
		AL.destroy();
		counter.cancel();
		parent.setStartButtonState(true);
		parent.cleanClockThread();
	}

	public void render() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, Display.getWidth(), 0, Display
				.getHeight(), -1, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		// clear the screen
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
		GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
		GL11.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_NICEST);
		GL11.glColor3f(0.2078431372549019607843137254902f, 0.10196078431372549019607843137255f, 0.06274509803921568627450980392157f);

		GL11.glPushMatrix();
		GL11.glTranslated(width / 2, partHeight * 0.8, 0.0f);
		renderFullAnalogClock();
		GL11.glPopMatrix();
		
		GL11.glColor3f(currentColor[0], currentColor[1], currentColor[2]);
		
		GL11.glPushMatrix();
		GL11.glTranslated(width / 50, partHeight * 1.65, 0.0f);
		renderColons();
		renderDigitalCounter();
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		GL11.glTranslated(width / 2, partHeight * 0.8, 0.0f);
		renderAnalogCounter();
		GL11.glPopMatrix();
	}

	public int[] getFormattedCountdown() {
		long countdown = Math.abs(this.countdown);
		int[] time = { (int) (countdown / 360000),
				(int) ((countdown % 360000) / 6000),
				(int) ((countdown % 6000) / 100),
				(int) (countdown % 100) };
		return time;
	}

	public int[] getOneDigitCountdown() {
		int[] intermediatetime = getFormattedCountdown();
		String[] intermediateTime = { Integer.toString(intermediatetime[0]),
				Integer.toString(intermediatetime[1]),
				Integer.toString(intermediatetime[2]),
				Integer.toString(intermediatetime[3]) };
		for (int i = 0, j = intermediateTime.length; i < j; i++) {
			if (intermediateTime[i].length() < 2)
				intermediateTime[i] = "0" + intermediateTime[i];
		}
		if (intermediateTime[0].length() < 3) {
			intermediateTime[0] = "0" + intermediateTime[0];
		}
		int[] time = { Integer.parseInt(intermediateTime[0].substring(0, 1)),
				Integer.parseInt(intermediateTime[0].substring(1, 2)),
				Integer.parseInt(intermediateTime[0].substring(2, 3)),
				Integer.parseInt(intermediateTime[1].substring(0, 1)),
				Integer.parseInt(intermediateTime[1].substring(1, 2)),
				Integer.parseInt(intermediateTime[2].substring(0, 1)),
				Integer.parseInt(intermediateTime[2].substring(1, 2)),
				Integer.parseInt(intermediateTime[3].substring(0, 1)),
				Integer.parseInt(intermediateTime[3].substring(1, 2)) };
		return time;
	}

	private void renderDigitalCounter() {
		int[] time = getOneDigitCountdown();
		GL11.glTranslatef(bitSize * 3, 0, 0.0f);
		if (showSign) {
			if (countdown != 0) {
				renderNumberPart(3);
				if (countdown > 0) {
					GL11.glBegin(GL11.GL_TRIANGLES);
					GL11.glVertex2f(bitSize * 3, -bitSize);
					GL11.glVertex2f(bitSize * 4, -(bitSize * 3));
					GL11.glVertex2f(bitSize * 5, -bitSize);
					GL11.glEnd();
					GL11.glBegin(GL11.GL_TRIANGLES);
					GL11.glVertex2f(bitSize * 3, bitSize);
					GL11.glVertex2f(bitSize * 4, bitSize * 3);
					GL11.glVertex2f(bitSize * 5, bitSize);
					GL11.glEnd();
				}
			}
		}
		GL11.glTranslatef(bitSize * 10, 0.0f, 0.0f);
		renderNumber(time[0]);
		GL11.glTranslatef(bitSize * 10, 0.0f, 0.0f);
		renderNumber(time[1]);
		GL11.glTranslatef(bitSize * 10, 0.0f, 0.0f);
		renderNumber(time[2]);
		GL11.glTranslatef(bitSize * 14, 0.0f, 0.0f);
		renderNumber(time[3]);
		GL11.glTranslatef(bitSize * 10, 0.0f, 0.0f);
		renderNumber(time[4]);
		GL11.glTranslatef(bitSize * 14, 0.0f, 0.0f);
		renderNumber(time[5]);
		GL11.glTranslatef(bitSize * 10, 0.0f, 0.0f);
		renderNumber(time[6]);
		GL11.glTranslatef(bitSize * 14, 0.0f, 0.0f);
		renderNumber(time[7]);
		GL11.glTranslatef(bitSize * 10, 0.0f, 0.0f);
		renderNumber(time[8]);
	}
	
	private void renderNumber(int number) {
		GL11.glPushMatrix();
		switch (number) {
		case 0:
			renderNumberPart(0);
			renderNumberPart(1);
			renderNumberPart(2);
			renderNumberPart(4);
			renderNumberPart(5);
			renderNumberPart(6);
			break;
		case 1:
			renderNumberPart(2);
			renderNumberPart(5);
			break;
		case 2:
			renderNumberPart(0);
			renderNumberPart(2);
			renderNumberPart(3);
			renderNumberPart(4);
			renderNumberPart(6);
			break;
		case 3:
			renderNumberPart(0);
			renderNumberPart(2);
			renderNumberPart(3);
			renderNumberPart(5);
			renderNumberPart(6);
			break;
		case 4:
			renderNumberPart(1);
			renderNumberPart(2);
			renderNumberPart(3);
			renderNumberPart(5);
			break;
		case 5:
			renderNumberPart(0);
			renderNumberPart(1);
			renderNumberPart(3);
			renderNumberPart(5);
			renderNumberPart(6);
			break;
		case 6:
			renderNumberPart(0);
			renderNumberPart(1);
			renderNumberPart(3);
			renderNumberPart(4);
			renderNumberPart(5);
			renderNumberPart(6);
			break;
		case 7:
			renderNumberPart(0);
			renderNumberPart(2);
			renderNumberPart(5);
			break;
		case 8:
			renderNumberPart(0);
			renderNumberPart(1);
			renderNumberPart(2);
			renderNumberPart(3);
			renderNumberPart(4);
			renderNumberPart(5);
			renderNumberPart(6);
			break;
		case 9:
			renderNumberPart(0);
			renderNumberPart(1);
			renderNumberPart(2);
			renderNumberPart(3);
			renderNumberPart(5);
			renderNumberPart(6);
		}
		GL11.glPopMatrix();
	}
	
	/**
	 * Renders a part of the digital number.
	 * 
	 * @param part
	 *            - The part to render: <br>
	 * 
	 *            <pre>
	 *      0
	 *      _
	 *    1| |2
	 *      - 3
	 *    4|_|5
	 *      6
	 * </pre>
	 */
	private void renderNumberPart(int part) {
		switch (part) {
		case 0:
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(bitSize + bitSize / 10, bitSize * 6);
			GL11.glVertex2f(bitSize / 10, bitSize * 7);
			GL11.glVertex2f(bitSize * 8 - bitSize / 10, bitSize * 7);
			GL11.glVertex2f(bitSize * 7 - bitSize / 10, bitSize * 6);
			GL11.glEnd();
			break;
		case 1:
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(0, bitSize * 7 - bitSize / 10);
			GL11.glVertex2d(0, bitSize / 2 - bitSize / 3.3);
			GL11.glVertex2d(bitSize, bitSize - bitSize / 3.3);
			GL11.glVertex2f(bitSize, bitSize * 6 - bitSize / 10);
			GL11.glEnd();
			break;
		case 2:
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(bitSize * 7, bitSize * 6 - bitSize / 10);
			GL11.glVertex2d(bitSize * 7, bitSize - bitSize / 3.3);
			GL11.glVertex2d(bitSize * 8, bitSize / 2 - bitSize / 3.3);
			GL11.glVertex2f(bitSize * 8, bitSize * 7 - bitSize / 10);
			GL11.glEnd();
			break;
		case 3:
			GL11.glBegin(GL11.GL_POLYGON);
			GL11.glVertex2f(bitSize,  -bitSize / 2);
			GL11.glVertex2f(0, 0);
			GL11.glVertex2f(bitSize,  bitSize / 2);
			GL11.glVertex2f(bitSize * 7,  bitSize / 2);
			GL11.glVertex2f(bitSize * 8, 0);
			GL11.glVertex2f(bitSize * 7,  -bitSize / 2);
			GL11.glEnd();
			break;
		case 4:
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(0, -(bitSize * 7 - bitSize / 10));
			GL11.glVertex2d(0, -(bitSize / 2 - bitSize / 3.3));
			GL11.glVertex2d(bitSize, -(bitSize - bitSize / 3.3));
			GL11.glVertex2f(bitSize, -(bitSize * 6 - bitSize / 10));
			GL11.glEnd();
			break;
		case 5:
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(bitSize * 7, -(bitSize * 6 - bitSize / 10));
			GL11.glVertex2d(bitSize * 7, -(bitSize - bitSize / 3.3));
			GL11.glVertex2d(bitSize * 8, -(bitSize / 2 - bitSize / 3.3));
			GL11.glVertex2f(bitSize * 8, -(bitSize * 7 - bitSize / 10));
			GL11.glEnd();
			break;
		case 6:
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(bitSize + bitSize / 10, -(bitSize * 6));
			GL11.glVertex2f(bitSize / 10, -(bitSize * 7));
			GL11.glVertex2f(bitSize * 8 - bitSize / 10, -(bitSize * 7));
			GL11.glVertex2f(bitSize * 7 - bitSize / 10, -(bitSize * 6));
			GL11.glEnd();
		}
	}
	

	private void renderColons() {
		GL11.glPushMatrix();
		
		GL11.glTranslated(this.bitSize * 44, bitSize * 1.5, 0.0f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(-bitSize / 2, -bitSize / 2);
		GL11.glVertex2f(bitSize / 2, -bitSize / 2);
		GL11.glVertex2f(bitSize / 2, bitSize / 2);
		GL11.glVertex2f(-bitSize / 2, bitSize / 2);
		GL11.glEnd();

		GL11.glTranslated(0.0f, -(bitSize * 4.5), 0.0f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(-bitSize / 2, -bitSize / 2);
		GL11.glVertex2f(bitSize / 2, -bitSize / 2);
		GL11.glVertex2f(bitSize / 2, bitSize / 2);
		GL11.glVertex2f(-bitSize / 2, bitSize / 2);
		GL11.glEnd();

		GL11.glTranslated(this.bitSize * 24, bitSize * 4.5, 0.0f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(-bitSize / 2, -bitSize / 2);
		GL11.glVertex2f(bitSize / 2, -bitSize / 2);
		GL11.glVertex2f(bitSize / 2, bitSize / 2);
		GL11.glVertex2f(-bitSize / 2, bitSize / 2);
		GL11.glEnd();

		GL11.glTranslated(0.0f, -(bitSize * 4.5), 0.0f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(-bitSize / 2, -bitSize / 2);
		GL11.glVertex2f(bitSize / 2, -bitSize / 2);
		GL11.glVertex2f(bitSize / 2, bitSize / 2);
		GL11.glVertex2f(-bitSize / 2, bitSize / 2);
		GL11.glEnd();

		GL11.glTranslated(this.bitSize * 24, bitSize * 4.5, 0.0f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(-bitSize / 2, -bitSize / 2);
		GL11.glVertex2f(bitSize / 2, -bitSize / 2);
		GL11.glVertex2f(bitSize / 2, bitSize / 2);
		GL11.glVertex2f(-bitSize / 2, bitSize / 2);
		GL11.glEnd();

		GL11.glTranslated(0.0f, -(bitSize * 4.5), 0.0f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(-bitSize / 2, -bitSize / 2);
		GL11.glVertex2f(bitSize / 2, -bitSize / 2);
		GL11.glVertex2f(bitSize / 2, bitSize / 2);
		GL11.glVertex2f(-bitSize / 2, bitSize / 2);
		GL11.glEnd();

		GL11.glPopMatrix();
	}

	private void renderFullAnalogClock() {
		//seconds
		GL11.glPushMatrix();
		for (int i = 0; i < 60; i++) {
			GL11.glRotatef(ANGLE, 0, 0, 1.0f);
			GL11.glBegin(GL11.GL_POLYGON);
			//As simply rotating along the center doesn't work so well, 
			//working with sine and cosine in order to get appropriate co-ordinates was necessary.
			for (double t = DEGREES90R; t < DEGREES90R + 10 * ANGLER ; t += 0.001D) {
				double circleX = java.lang.Math.cos(t);
				double circleY = java.lang.Math.sin(t);
				GL11.glVertex2d(circleX * sliceLength * 62, circleY * sliceLength * 62);
			}
			for (double t = DEGREES90R + 10 * ANGLER; t > DEGREES90R; t -= 0.001D) {
				double circleX = java.lang.Math.cos(t);
				double circleY = java.lang.Math.sin(t);
				GL11.glVertex2d(circleX * sliceLength * 52, circleY * sliceLength * 52);
			}
			GL11.glEnd();
			GL11.glRotatef(11 * ANGLE, 0, 0, 1.0f);
		}
		GL11.glPopMatrix();
		
		//minutes
		GL11.glPushMatrix();
		for (int i = 0; i < 60; i++) {
			GL11.glRotatef(ANGLE, 0, 0, 1.0f);
			GL11.glBegin(GL11.GL_POLYGON);
			for (double t = DEGREES90R; t < DEGREES90R + 10 * ANGLER ; t += 0.001D) {
				double circleX = java.lang.Math.cos(t);
				double circleY = java.lang.Math.sin(t);
				GL11.glVertex2d(circleX * sliceLength * 49, circleY * sliceLength * 49);
			}
			for (double t = DEGREES90R + 10 * ANGLER; t > DEGREES90R; t -= 0.001D) {
				double circleX = java.lang.Math.cos(t);
				double circleY = java.lang.Math.sin(t);
				GL11.glVertex2d(circleX * sliceLength * 29, circleY * sliceLength * 29);
			}
			GL11.glEnd();
			GL11.glRotatef(11 * ANGLE, 0, 0, 1.0f);
		}
		GL11.glPopMatrix();
		
		//hours
		GL11.glPushMatrix();
		for (int i = 0; i < 12; i++) {
			GL11.glRotatef(ANGLE, 0, 0, 1.0f);
			GL11.glBegin(GL11.GL_POLYGON);
			for (double t = DEGREES90R; t < DEGREES90R + 58 * ANGLER ; t += 0.001D) {
				double circleX = java.lang.Math.cos(t);
				double circleY = java.lang.Math.sin(t);
				GL11.glVertex2d(circleX * sliceLength * 26, circleY * sliceLength * 26);
			}
			for (double t = DEGREES90R + 58 * ANGLER; t > DEGREES90R; t -= 0.001D) {
				double circleX = java.lang.Math.cos(t);
				double circleY = java.lang.Math.sin(t);
				GL11.glVertex2d(circleX * sliceLength * 6, circleY * sliceLength * 6);
			}
			GL11.glEnd();
			GL11.glRotatef(59 * ANGLE, 0, 0, 1.0f);
		}
		GL11.glPopMatrix();
	}
	
	private void renderAnalogCounter() {
		//seconds
		GL11.glPushMatrix();
		for (int i = 0, j = getFormattedCountdown()[2]; i < j && i < 60; i++) {
			GL11.glRotatef(ANGLE, 0, 0, 1.0f);
			GL11.glBegin(GL11.GL_POLYGON);
			//As simple rotating along the center doesn't work so well, 
			//I had to work with sine and cosine in order to get appropriate co-ordinates.
			for (double t = DEGREES90R; t < DEGREES90R + 10 * ANGLER ; t += 0.001D) {
				double circleX = java.lang.Math.cos(t);
				double circleY = java.lang.Math.sin(t);
				GL11.glVertex2d(circleX * sliceLength * 62, circleY * sliceLength * 62);
			}
			for (double t = DEGREES90R + 10 * ANGLER; t > DEGREES90R; t -= 0.001D) {
				double circleX = java.lang.Math.cos(t);
				double circleY = java.lang.Math.sin(t);
				GL11.glVertex2d(circleX * sliceLength * 52, circleY * sliceLength * 52);
			}
			GL11.glEnd();
			GL11.glRotatef(11 * ANGLE, 0, 0, 1.0f);
		}
		GL11.glPopMatrix();
		
		//minutes
		GL11.glPushMatrix();
		for (int i = 0, j = getFormattedCountdown()[1]; i < j && i < 60; i++) {
			GL11.glRotatef(ANGLE, 0, 0, 1.0f);
			GL11.glBegin(GL11.GL_POLYGON);
			for (double t = DEGREES90R; t < DEGREES90R + 10 * ANGLER ; t += 0.001D) {
				double circleX = java.lang.Math.cos(t);
				double circleY = java.lang.Math.sin(t);
				GL11.glVertex2d(circleX * sliceLength * 49, circleY * sliceLength * 49);
			}
			for (double t = DEGREES90R + 10 * ANGLER; t > DEGREES90R; t -= 0.001D) {
				double circleX = java.lang.Math.cos(t);
				double circleY = java.lang.Math.sin(t);
				GL11.glVertex2d(circleX * sliceLength * 29, circleY * sliceLength * 29);
			}
			GL11.glEnd();
			GL11.glRotatef(11 * ANGLE, 0, 0, 1.0f);
		}
		GL11.glPopMatrix();
		
		//hours
		GL11.glPushMatrix();
		for (int i = 0, j = getFormattedCountdown()[0]; i < j && i < 12; i++) {
			GL11.glRotatef(ANGLE, 0, 0, 1.0f);
			GL11.glBegin(GL11.GL_POLYGON);
			for (double t = DEGREES90R; t < DEGREES90R + 58 * ANGLER ; t += 0.001D) {
				double circleX = java.lang.Math.cos(t);
				double circleY = java.lang.Math.sin(t);
				GL11.glVertex2d(circleX * sliceLength * 26, circleY * sliceLength * 26);
			}
			for (double t = DEGREES90R + 58 * ANGLER; t > DEGREES90R; t -= 0.001D) {
				double circleX = java.lang.Math.cos(t);
				double circleY = java.lang.Math.sin(t);
				GL11.glVertex2d(circleX * sliceLength * 6, circleY * sliceLength * 6);
			}
			GL11.glEnd();
			GL11.glRotatef(59 * ANGLE, 0, 0, 1.0f);
		}
		GL11.glPopMatrix();
	}

	class SetStartingCountdown extends TimerTask {
		private int step;
		private int[] numbers;

		public SetStartingCountdown() {
			numbers = new int[9];
			numbers[0] = Integer.parseInt(initialHours.substring(0, 1));
			numbers[1] = Integer.parseInt(initialHours.substring(1, 2));
			numbers[2] = Integer.parseInt(initialHours.substring(2, 3));
			numbers[3] = Integer.parseInt(initialMinutes.substring(0, 1));
			numbers[4] = Integer.parseInt(initialMinutes.substring(1, 2));
			numbers[5] = Integer.parseInt(initialSeconds.substring(0, 1));
			numbers[6] = Integer.parseInt(initialSeconds.substring(1, 2));
			numbers[7] = 0;
			numbers[8] = 0;
			step = 0;
		}

		@Override
		public void run() {
			switch (step) {
			case 0:
				if (playSound) SoundPlayer.playEffect(clockRunning);
				countdown = numbers[0];
				break;
			case 1:
				countdown = (numbers[0] * 10) + numbers[1];
				break;
			case 2:
				countdown = (numbers[0] * 100) + (numbers[1] * 10) + numbers[2];
				break;
			case 3:
				countdown = (numbers[0] * 1000) + (numbers[1] * 100)
						+ (numbers[2] * 10) + numbers[3];
				break;
			case 4:
				countdown = (numbers[0] * 6000) + (numbers[1] * 1000)
						+ (numbers[2] * 100) + (numbers[3] * 10) + numbers[4];
				break;
			case 5:
				countdown = (numbers[0] * 60000) + (numbers[1] * 6000)
						+ (numbers[2] * 1000) + (numbers[3] * 100)
						+ (numbers[4] * 10) + numbers[5];
				break;
			case 6:
				countdown = (numbers[0] * 360000) + (numbers[1] * 60000)
						+ (numbers[2] * 6000) + (numbers[3] * 1000)
						+ (numbers[4] * 100) + (numbers[5] * 10) + numbers[6];
				break;
			case 7:
				countdown = (numbers[0] * 3600000) + (numbers[1] * 360000)
						+ (numbers[2] * 60000) + (numbers[3] * 6000)
						+ (numbers[4] * 1000) + (numbers[5] * 100)
						+ (numbers[6] * 10) + numbers[7];
				break;
			case 8:
				countdown = (numbers[0] * 36000000) + (numbers[1] * 3600000)
						+ (numbers[2] * 360000) + (numbers[3] * 60000)
						+ (numbers[4] * 6000) + (numbers[5] * 1000)
						+ (numbers[6] * 100) + (numbers[7] * 10) + numbers[8];
				break;
			case 9:
				countdown = -countdown;
				showSign = true;
				break;
			case 10:
				try {
					counter.scheduleAtFixedRate(new CountDown(), 1000, 10);
					countdownSetter.cancel();	
				} catch (Exception e) {}
				cancel();
				break;
			}
			step++;
		}
	}
	
	class CountDown extends TimerTask {
		@Override
		public void run() {
			countdown++;
			if (playTick) {
				if ((countdown % 100) == 0) {
					if (playSound) SoundPlayer.playEffect(clockTick);
				}
			}
			if (countdown == 0) {
				colorFlash.scheduleAtFixedRate(new FlashingDigitalClock(), 0, 8);
				if (!keepRunning) {
					counter.cancel();
				}
				if (playSound)
				{
					new Thread(){
						public void run() {
							for(int i = 0; i < 3; i++) {
								SoundPlayer.playEffect(clockExpired);
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}.start();
				}
			}
		}
	}
	
	class FlashingDigitalClock extends TimerTask {
		private int step;
		private int iteration;
		
		public FlashingDigitalClock() {
			step = 0;
			iteration = 0;
		}
		
		@Override
		public void run() {
			step++;
			if (step <= 20) {
				currentColor[0] -= 0.03960784313725490196078431372549f;
				currentColor[1] -= 0.02843137254901960784313725490196f;
				currentColor[2] -= 0.00196078431372549019607843137255f;
			}
			else if (step <= 40) {
				currentColor[0] += 0.03960784313725490196078431372549f;
				currentColor[1] += 0.02843137254901960784313725490196f;
				currentColor[2] += 0.00196078431372549019607843137255f;
			}
			if (step > 40) {
				step = 0;
				iteration++;
			}
			if (iteration == 9) {
				colorFlash.cancel();
				cancel();
			}
		}
	}
	
	private ByteBuffer[] retrieveIcon() throws IOException {
		InputStream is = parent.getClass().getResource("/zentigame/bsgclock/BSGClock.png").openStream();
		ByteBuffer b32 = null, b16 = null;
		
        try {
            PNGDecoder decoder = new PNGDecoder(is);
            ByteBuffer bb = ByteBuffer.allocateDirect(decoder.getWidth()*decoder.getHeight()*4);
            decoder.decode(bb, decoder.getWidth()*4, PNGDecoder.Format.RGBA);
            bb.flip();
            b32 = bb;
        } finally {
            is.close();
        }
        
        is = parent.getClass().getResource("/zentigame/bsgclock/BSGClock16.png").openStream();
        try {
            PNGDecoder decoder = new PNGDecoder(is);
            ByteBuffer bb = ByteBuffer.allocateDirect(decoder.getWidth()*decoder.getHeight()*4);
            decoder.decode(bb, decoder.getWidth()*4, PNGDecoder.Format.RGBA);
            bb.flip();
            b16 = bb;
        } finally {
            is.close();
        }
        
        return new ByteBuffer[] {b16, b32};
	}
	
	private void saveFrameAsPNG(String fileName ) {
        
        // Open File
        if( fileName == null ) {
            
            fileName = new String( "Screenshot.png" ); 
        }
        
        File outputFile = new File( fileName );             
        
        try {
            javax.imageio.ImageIO.write( takeScreenshot(), "PNG", outputFile );
        
        } catch (Exception e) {
            System.out.println( "Error: ImageIO.write." );
            e.printStackTrace();
        }
    }
    
    
    private BufferedImage takeScreenshot(){
        
        int frameWidth = Display.getDisplayMode().getWidth();
        int frameHeight = Display.getDisplayMode().getHeight();
        
        BufferedImage screenshot = null;
         // allocate space for RBG pixels
        ByteBuffer fb = ByteBuffer.allocateDirect(frameWidth*frameHeight*3);    
            
         int[] pixels = new int[frameWidth * frameHeight];
         int bindex;
        
         // grab a copy of the current frame contents as RGB
         GL11.glReadPixels(0, 0, frameWidth, frameHeight, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, fb);
         
                 
         // convert RGB data in ByteBuffer to integer array
         for (int i=0; i < pixels.length; i++) {
             bindex = i * 3;
             pixels[i] = ((fb.get(bindex) << 16))  + ((fb.get(bindex+1) << 8))  + ((fb.get(bindex+2) << 0));
         }
        
         // Create a BufferedImage with the RGB pixels then save as PNG
         try {
             screenshot = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
             screenshot.setRGB(0, 0, frameWidth, frameHeight, pixels, 0, frameWidth);     
             
             
             // * Flip Image Y Axis *
             AffineTransform tx = AffineTransform.getScaleInstance(1, -1); 
             tx.translate(0, -screenshot.getHeight(null)); 
             AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR); 
             screenshot = op.filter(screenshot, null); 
             
         }
         catch (Exception e) {
             System.out.println("ScreenShot() exception: " +e);
         }
         return screenshot;
    }
}
