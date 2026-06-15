package ui;

import static utilz.Constants.UI.PauseButtons.SOUND_SIZE;
import static utilz.Constants.UI.VolumeButtons.SLIDER_WIDTH;
import static utilz.Constants.UI.VolumeButtons.VOLUME_HEIGHT;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

import main.Game;

public class AudioOptions {

	private VolumeButton volumeButton;
	private SoundButton musicButton, sfxButton;

	private Game game;

	public AudioOptions(Game game) {
		this.game = game;
		createSoundButtons();
		createVolumeButton();
	}

	private void createVolumeButton() {
		int vX = (int) (309 * Game.SCALE);
		int vY = (int) (278 * Game.SCALE);
		volumeButton = new VolumeButton(vX, vY, SLIDER_WIDTH, VOLUME_HEIGHT);
	}

	private void createSoundButtons() {
		int soundX = (int) (450 * Game.SCALE);
		int musicY = (int) (140 * Game.SCALE);
		int sfxY = (int) (186 * Game.SCALE);
		musicButton = new SoundButton(soundX, musicY, SOUND_SIZE, SOUND_SIZE);
		sfxButton = new SoundButton(soundX, sfxY, SOUND_SIZE, SOUND_SIZE);
	}

	public void update() {
		musicButton.update();
		sfxButton.update();

		volumeButton.update();
	}

	public void draw(Graphics g) {
		// Sound buttons
		musicButton.draw(g);
		sfxButton.draw(g);

		// Volume Button
		volumeButton.draw(g);
	}

	public void applySavedSettings() {
		float volume = game.getSaveManager().getAudioVolume();
		boolean musicMuted = game.getSaveManager().isMusicMuted();
		boolean sfxMuted = game.getSaveManager().isSfxMuted();

		volumeButton.setFloatValue(volume);
		musicButton.setMuted(musicMuted);
		sfxButton.setMuted(sfxMuted);

		game.getAudioPlayer().setVolume(volume);
		game.getAudioPlayer().setSongMute(musicMuted);
		game.getAudioPlayer().setEffectMute(sfxMuted);
	}

	public void mouseDragged(MouseEvent e) {
		if (volumeButton.isMousePressed()) {
			applyVolumeAt(e.getX());
		}
	}

	public void mousePressed(MouseEvent e) {
		if (isIn(e, musicButton))
			musicButton.setMousePressed(true);
		else if (isIn(e, sfxButton))
			sfxButton.setMousePressed(true);
		else if (isInVolumeSlider(e)) {
			volumeButton.setMousePressed(true);
			applyVolumeAt(e.getX());
		}
	}

	public void mouseReleased(MouseEvent e) {
		boolean settingsChanged = false;
		if (isIn(e, musicButton)) {
			if (musicButton.isMousePressed()) {
				musicButton.setMuted(!musicButton.isMuted());
				game.getAudioPlayer().toggleSongMute();
				settingsChanged = true;
			}

		} else if (isIn(e, sfxButton)) {
			if (sfxButton.isMousePressed()) {
				sfxButton.setMuted(!sfxButton.isMuted());
				game.getAudioPlayer().toggleEffectMute();
				settingsChanged = true;
			}
		}

		if (settingsChanged)
			saveAudioSettings();

		musicButton.resetBools();
		sfxButton.resetBools();

		volumeButton.resetBools();
	}

	public void mouseMoved(MouseEvent e) {
		musicButton.setMouseOver(false);
		sfxButton.setMouseOver(false);

		volumeButton.setMouseOver(false);

		if (isIn(e, musicButton))
			musicButton.setMouseOver(true);
		else if (isIn(e, sfxButton))
			sfxButton.setMouseOver(true);
		else if (isInVolumeSlider(e))
			volumeButton.setMouseOver(true);
	}

	private boolean isIn(MouseEvent e, PauseButton b) {
		return b.getBounds().contains(e.getX(), e.getY());
	}

	private boolean isInVolumeSlider(MouseEvent e) {
		return volumeButton.isInSlider(e.getX(), e.getY()) || isIn(e, volumeButton);
	}

	private void applyVolumeAt(int mouseX) {
		float valueBefore = volumeButton.getFloatValue();
		volumeButton.changeX(mouseX);
		float valueAfter = volumeButton.getFloatValue();
		if (Math.abs(valueBefore - valueAfter) > 0.001f) {
			game.getAudioPlayer().setVolume(valueAfter);
			saveAudioSettings();
		}
	}

	private void saveAudioSettings() {
		game.getSaveManager().saveAudioSettings(volumeButton.getFloatValue(), musicButton.isMuted(), sfxButton.isMuted());
	}

}
