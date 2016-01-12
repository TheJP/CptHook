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

/**
 * Object that manages, which sounds are currently played.
 */
object SoundManager {
  
  private var sounds = Map(
    "level" -> loadSound("./sounds/level.wav"),
    "jump" -> loadSound("./sounds/jump.wav")
  )
  
  private var clipsLock = new ReentrantLock()
  private var clips: Map[String, MutableList[Clip]] = Map()
  
  private def loadSound(path: String): Array[Byte] = {
     try {
       println("loading: " + path)
       return Files.readAllBytes(Paths.get(getClass.getResource(path).toURI()))
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
    return null
  }
  
  def playSound(sound: String, loop: Boolean, stopOthers: Boolean): Unit = playSound(sound, 1.0f, loop, stopOthers)
  
  def playSound(sound: String, gain: Float, loop: Boolean, stopOthers: Boolean): Unit = {
    
    if (!sounds.contains(sound)) {
      return
    }
    
    if (!clips.contains(sound)) {
      clipsLock.lock()
      clips += (sound -> MutableList[Clip]())
      clipsLock.unlock()
    }
    
    if (stopOthers) {
      stopSound(sound)
    }
    
    var in = new ByteArrayInputStream(sounds(sound))
    var audioIn = AudioSystem.getAudioInputStream(in)
    var clip = AudioSystem.getClip
    clip.open(audioIn)
    if (loop) {
      clip.loop(Clip.LOOP_CONTINUOUSLY)
    }
    val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl];
    val dB = (Math.log(gain)/Math.log(10.0)*20.0).toFloat;
    gainControl.setValue(dB)
    clip.start
    
    clipsLock.lock()
    clips(sound) += clip
    clipsLock.unlock()
    
    clip.addLineListener(new LineListener() {
      def update(event: LineEvent): Unit = {
        if (event.getType == LineEvent.Type.STOP) {
          println("removing: " + sound)
          clipsLock.lock()
          clips(sound) = clips(sound).filter { c => c != clip }
          clipsLock.unlock()
        }
      }
    })
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