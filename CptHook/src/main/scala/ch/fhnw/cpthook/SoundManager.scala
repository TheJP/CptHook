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


class Sound(var data:Array[Byte], var loop: Boolean) extends Runnable {
  
  private var player: Player = null
  private var stopped: AtomicBoolean = new AtomicBoolean(false)
  private var running: AtomicBoolean = new AtomicBoolean(true)
  private var done: Sound => Unit = null
  
  override def run(): Unit = {
    player = createPlayer
    while(!stopped.get) {
      if (!player.play(1)) {
        if (loop) {
          player = createPlayer
        } else {
          stopped.set(true)
        }
      }
    }
    running.set(false)
    if (done != null) {
      done(this)
    }
  }
  
  private def createPlayer(): Player = {
    new Player(new ByteArrayInputStream(data))
  }
  
  def stop(): Unit = {
    stopped.set(true)
  }
  
  def isRunning(): Boolean = {
    running.get
  }
  
  def setDone(f: Sound => Unit) {
    done = f
  }
}

/**
 * Object that manages, which sounds are currently played.
 */
object SoundManager {
  
  val MaxSounds = 25
  private val soundCount = new AtomicInteger(0)
  private val executor = Executors.newFixedThreadPool(MaxSounds)
  
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
  private var clips: Map[String, MutableList[Sound]] = sounds.map { case (key, value) => (key, MutableList[Sound]()) }
  
  private def loadSound(path: String): Array[Byte] = {
    try {
       return Files.readAllBytes(Paths.get(getClass.getResource(path).toURI()))
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    return null
  }

  def playEffect(sound: String) = playSound(sound, 1.0f, false, false)

  def playSound(sound: String, loop: Boolean, stopOthers: Boolean): Unit = playSound(sound, 1.0f, loop, stopOthers)
  
  def playSound(sound: String, gain: Float, loop: Boolean, stopOthers: Boolean): Unit = {
    
    var lastCount = soundCount.get
    if (lastCount >= MaxSounds) {
      println("sound limit reached")
      return
    }
    
    while(!soundCount.compareAndSet(lastCount, lastCount + 1)) {
      var lastCount = soundCount.get
      if (lastCount >= MaxSounds) {
        println("sound limit reached")
        return
      }
    }
    
    val s = new Sound(sounds(sound), loop)
    
    clipsLock.lock()
    clips(sound) += s
    clipsLock.unlock()
    
    s.setDone { s => 
      clipsLock.lock()
      clips(sound) = clips(sound).filter(_ != s)
      soundCount.decrementAndGet()
      clipsLock.unlock()
    }
    
    executor.execute(s)
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