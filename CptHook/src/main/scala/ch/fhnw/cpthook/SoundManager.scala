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
import java.io.File
import java.io.DataInputStream
import java.io.FileInputStream
import javax.sound.sampled.LineEvent
import javax.sound.sampled.LineListener
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import scala.collection.mutable.MutableList
import scala.collection.mutable.Map
import javax.sound.sampled.FloatControl
import javafx.scene.media.AudioClip
import com.sun.media.jfxmediaimpl.AudioClipProvider
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer

/**
 * Object that manages, which sounds are currently played.
 */
object SoundManager {
  
  val AmbientSound = "ambient"
  val LevelSound = "level"
  val JumpSound = "jump"
  
  private val sounds = Map(
    AmbientSound -> "./sounds/ambient.mp3",
    LevelSound -> "./sounds/level.mp3",
    JumpSound -> "./sounds/jump.mp3"
  )
  
  private var clipsLock = new ReentrantLock()
  private var clips: Map[String, MutableList[AudioClip]] = Map()
  
  def playSound(sound: String, loop: Boolean, stopOthers: Boolean): Unit = playSound(sound, 1.0f, loop, stopOthers)
  
  def playSound(sound: String, gain: Float, loop: Boolean, stopOthers: Boolean): Unit = {
    
    if (!sounds.contains(sound)) {
      return
    }
    
    if (!clips.contains(sound)) {
      clipsLock.lock()
      clips += (sound -> MutableList())
      clipsLock.unlock()
    }
    
    if (stopOthers) {
      stopSound(sound)
    }
    
    val audioClip =  new AudioClip(getClass.getResource(sounds(sound)).toString())
    if (loop) {
      audioClip.setCycleCount(MediaPlayer.INDEFINITE)
    }
    audioClip.setVolume(gain)
    audioClip.play()
    

    clipsLock.lock()
    clips(sound) += audioClip
    // cleanup old
    clips.keys.foreach { sound =>
      clips(sound) = clips(sound).filter(_.isPlaying())
    }
    clipsLock.unlock()
    

  }
  
  def stopSound(sound: String): Unit = {
    clipsLock.lock()
    clips(sound).foreach { _.stop() }
    clipsLock.unlock()
  }
  
  def stopAll(): Unit = {
    clipsLock.lock()
    clips.values.foreach { c => c.foreach { _.stop() } }
    clipsLock.unlock()
  }
}