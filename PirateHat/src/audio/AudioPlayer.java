package audio;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlayer {

	public static int MENU_1 = 0;
	public static int LEVEL_1 = 1;
	public static int LEVEL_2 = 2;

	public static int DIE = 0;
	public static int JUMP = 1;
	public static int GAMEOVER = 2;
	public static int LVL_COMPLETED = 3;
	public static int ATTACK_ONE = 4;
	public static int ATTACK_TWO = 5;
	public static int ATTACK_THREE = 6;

	private Clip[] songs, effects;
	private int currentSongId;
	private float volume = 0.5f;
	private boolean songMute, effectMute;
	private boolean audioLoadWarningPrinted;
	private Random rand = new Random();

	public AudioPlayer() {
		loadSongs();
		loadEffects();
		playSong(MENU_1);
	}

	private void loadSongs() {
		String[] names = { "menu", "level1", "level2" };
		songs = new Clip[names.length];
		for (int i = 0; i < songs.length; i++)
			songs[i] = getClip(names[i]);
	}

	private void loadEffects() {
		String[] effectNames = { "die", "jump", "gameover", "lvlcompleted", "attack1", "attack2", "attack3" };
		effects = new Clip[effectNames.length];
		for (int i = 0; i < effects.length; i++)
			effects[i] = getClip(effectNames[i]);

		updateEffectsVolume();

	}

	private Clip getClip(String name) {
		try (AudioInputStream audio = getAudioStream(name)) {
			DataLine.Info info = new DataLine.Info(Clip.class, audio.getFormat());
			Clip c = (Clip) AudioSystem.getLine(info);
			c.open(audio);
			return c;

		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException | IllegalArgumentException | IllegalStateException e) {
			reportAudioLoadFailure(name, e);
		}

		return null;

	}

	private AudioInputStream getAudioStream(String name) throws UnsupportedAudioFileException, IOException {
		URL url = getClass().getResource("/audio/" + name + ".wav");
		if (url != null)
			return AudioSystem.getAudioInputStream(url);
		return AudioSystem.getAudioInputStream(getAudioFile(name));
	}

	private void reportAudioLoadFailure(String name, Exception e) {
		if (!audioLoadWarningPrinted) {
			System.out.println("Audio unavailable; continuing without missing sounds.");
			audioLoadWarningPrinted = true;
		}
		System.out.println("Could not load audio/" + name + ".wav: " + e.getMessage());
	}

	private File getAudioFile(String name) {
		String fileName = name + ".wav";
		File[] candidates = { new File("res/audio", fileName), new File("PirateHat/res/audio", fileName), new File("Platformer/res/audio", fileName), new File("../res/audio", fileName), new File("../../res/audio", fileName) };

		for (File file : candidates)
			if (file.isFile())
				return file;

		throw new IllegalStateException("Could not load audio: " + fileName);
	}

	public void setVolume(float volume) {
		this.volume = volume;
		updateSongVolume();
		updateEffectsVolume();
	}

	public void stopSong() {
		Clip currentSong = getLoadedClip(songs, currentSongId);
		if (currentSong != null && currentSong.isActive())
			currentSong.stop();
	}

	public void setLevelSong(int lvlIndex) {
		if (lvlIndex % 2 == 0)
			playSong(LEVEL_1);
		else
			playSong(LEVEL_2);
	}

	public void lvlCompleted() {
		stopSong();
		playEffect(LVL_COMPLETED);
	}

	public void playAttackSound() {
		int start = 4;
		start += rand.nextInt(3);
		playEffect(start);
	}

	public void playEffect(int effect) {
		Clip clip = getLoadedClip(effects, effect);
		if (clip == null)
			return;
		if (clip.getMicrosecondPosition() > 0)
			clip.setMicrosecondPosition(0);
		clip.start();
	}

	public void playSong(int song) {
		stopSong();

		currentSongId = song;
		Clip nextSong = getLoadedClip(songs, currentSongId);
		if (nextSong == null)
			return;
		updateSongVolume();
		nextSong.setMicrosecondPosition(0);
		nextSong.loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void toggleSongMute() {
		this.songMute = !songMute;
		for (Clip c : songs)
			setClipMute(c, songMute);
	}

	public void toggleEffectMute() {
		this.effectMute = !effectMute;
		for (Clip c : effects)
			setClipMute(c, effectMute);
		if (!effectMute)
			playEffect(JUMP);
	}

	private void updateSongVolume() {
		setClipVolume(getLoadedClip(songs, currentSongId));
	}

	private void updateEffectsVolume() {
		for (Clip c : effects)
			setClipVolume(c);
	}

	private Clip getLoadedClip(Clip[] clips, int index) {
		if (clips == null || index < 0 || index >= clips.length)
			return null;
		return clips[index];
	}

	private void setClipMute(Clip clip, boolean muted) {
		if (clip == null || !clip.isControlSupported(BooleanControl.Type.MUTE))
			return;
		BooleanControl booleanControl = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
		booleanControl.setValue(muted);
	}

	private void setClipVolume(Clip clip) {
		if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
			return;
		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float range = gainControl.getMaximum() - gainControl.getMinimum();
		float gain = (range * volume) + gainControl.getMinimum();
		gainControl.setValue(gain);
	}

}
