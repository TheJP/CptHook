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
import javazoom.jl.player.Player
import javazoom.jl.player.advanced.AdvancedPlayer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import ch.fhnw.ether.audio.URLAudioSource
import javax.sound.sampled.DataLine
import scala.collection.mutable.Stack

/**
 * Object that manages, which sounds are currently played.
 */
object SoundManager {
  
  val MaxSounds = 25
  private val soundCount = new AtomicInteger(0)

  val AmbientSound = "ambient"
  val BlockPlaceSound = "place"
  val BlockRemoveSound = "remove"
  val BumpSound = "bump"
  val BumpLoopSound = "bumploop"
  val JumpSound = "jump"
  val LevelSound = "level"
  
  private val sounds = Map(
    AmbientSound -> loadSound("./sounds/ambient.mp3"),
    BlockPlaceSound -> loadSound("./sounds/blockplace.mp3"),
    BlockRemoveSound -> loadSound("./sounds/blockremove.mp3"),
    BumpSound -> loadSound("./sounds/bump.mp3"),
    BumpLoopSound -> loadSound("./sounds/bumploop.mp3"),
    JumpSound -> loadSound("./sounds/jump.mp3"),
    LevelSound -> loadSound("./sounds/level.mp3")
  )
  
  private var clipsLock = new ReentrantLock()
  private var clips: Map[String, MutableList[Clip]] = sounds.map { case (key, value) => (key, MutableList[Clip]()) }
  private var clipCache: Map[String, Stack[Clip]] = sounds.map { case (key, value) => (key, Stack[Clip]()) }
  
  clipCache.keys.foreach { k => clipCache(k).push(getClip(k)) }
  
  private def loadSound(path: String): Array[Byte] = {
    try {
       return Files.readAllBytes(Paths.get(getClass.getResource(path).toURI()))
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    return null
  }
  
  def getClip(sound: String): Clip = {
    var clip: Clip = null
    clipsLock.lock()
    if (clipCache(sound).length > 0) {
      clip = clipCache(sound).pop
      clip.setFramePosition(0)
      clip.loop(0)
    } else {
      val audio = URLAudioSource.getStream(new ByteArrayInputStream(sounds(sound)))
      val info = new DataLine.Info(classOf[Clip], audio.getFormat())
      clip = AudioSystem.getLine(info).asInstanceOf[Clip]
      clip.open(audio)
    }
    clipsLock.unlock()
 
    clip
  }

  def playEffect(sound: String) = playSound(sound, 1.0f, false, false)

  def playSound(sound: String, loop: Boolean, stopOthers: Boolean): Unit = playSound(sound, 1.0f, loop, stopOthers)

  def playSound(sound: String, gain: Float, loop: Boolean, stopOthers: Boolean): Unit = {

    cleanUp()

    var lastCount = soundCount.get
    if (lastCount >= MaxSounds) {
      println("sound limit reached")
      return
    }

    while (!soundCount.compareAndSet(lastCount, lastCount + 1)) {
      var lastCount = soundCount.get
      if (lastCount >= MaxSounds) {
        println("sound limit reached")
        return
      }
    }
    
    val clip = getClip(sound)

    clipsLock.lock()
    clips(sound) += clip
    clipsLock.unlock()

    if (loop) {
      clip.loop(Clip.LOOP_CONTINUOUSLY)
    }

    val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl];
    gainControl.setValue(toDb(gain))

    clip.start()
  }
  
  def toDb(gain: Float) = {
    (Math.log(gain)/Math.log(10.0)*20.0).toFloat;
  }
  
  def cleanUp(): Unit = {
    clipsLock.lock()
    clips.keys.foreach {k => 
      val done = clips(k).filter(!_.isRunning())
      clips(k) = clips(k).filter(_.isRunning())
      done.foreach {c =>
        clipCache(k).push(c)
        soundCount.decrementAndGet()
      }
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