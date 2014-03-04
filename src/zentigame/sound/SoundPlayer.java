package zentigame.sound;

import org.newdawn.slick.openal.Audio;

public class SoundPlayer {
	
	private SoundPlayer() {}

	public static void playBackgroundMusic(final Audio music) {
		playEffect(music, 1.0f, 1.0f, false);
	}
	
	public static void playBackgroundMusic(final Audio music, final float pitch, final float gain, final boolean loop) {
		new Thread() {
			public void run() {
				music.playAsMusic(pitch, gain, loop);
			}
		}.start();
	}
	
	public static void playEffect(final Audio sound) {
		playEffect(sound, 1.0f, 1.0f, false);
	}
	
	public static void playEffect(final Audio sound, final float pitch, final float gain, final boolean loop) {
		new Thread() {
			public void run() {
				sound.playAsSoundEffect(pitch, gain, loop);
			}
		}.start();
	}
	
	public static void playEffect(final Audio sound, final float pitch, final float gain, final boolean loop, final float x, final float y, final float z) {
		new Thread() {
			public void run() {
				sound.playAsSoundEffect(pitch, gain, loop, x, y, z);
			}
		}.start();
	}
}
