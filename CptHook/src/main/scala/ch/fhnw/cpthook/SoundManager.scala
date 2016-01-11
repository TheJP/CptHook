package ch.fhnw.cpthook

import java.io.ByteArrayInputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import net.java.truecommons.io.Loan.loan

/**
 * Object that manages, which sounds are currently played.
 */
object SoundManager {

  //Sounds constants
//  val Ambient = getClass.getResource("./sounds/ambient.mp3")
  val Level = getClass.getResource("./sounds/level.wav")

  //Song, which is currently playing (null otherwise)
  private var currentSong: Clip = null

  /**
   * Plays the song with the given url.
   */
  def playSong(url: URL): Unit = {
    try {
      val byteStream = new ByteArrayInputStream(Files.readAllBytes(Paths.get(url.toURI())))
      val audioIn = AudioSystem.getAudioInputStream(byteStream)
      if(currentSong != null) { currentSong.stop }
      currentSong = AudioSystem.getClip
      currentSong.open(audioIn)
      currentSong.loop(Clip.LOOP_CONTINUOUSLY)
      currentSong.start
    } catch{ case _ : Exception => }
  }

  /**
   * Stops the currently playing song.
   */
  def stopSong: Unit = {
    if(currentSong != null) { currentSong.stop }
    currentSong = null
  }
}