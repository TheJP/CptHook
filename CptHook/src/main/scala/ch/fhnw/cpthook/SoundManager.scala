package ch.fhnw.cpthook

import javax.sound.sampled.FloatControl
import ch.fhnw.ether.audio.URLAudioSource
import java.nio.file.Files
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import java.io.ByteArrayInputStream
import scala.collection.mutable.ListBuffer
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.Stack
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent
import javax.sound.sampled.LineListener
import scala.collection.immutable.Queue

/**
 * PlaySound, StopSound and StopAll are commands that can be queued
 */
case class PlaySound(val sound: String, val gain: Float, val loop: Boolean, val stopOthers: Boolean)
case class StopSound(val sound: String)
case class StopAll()

/**
 * MappedClip is used to save the currently playing clips and keep a mapping to what sound it was
 */
case class MappedClip(val sound: String, val clip: Clip)

/**
 * Object that manages, which sounds are currently played.
 */
object SoundManager {

  val MaxSounds = 10

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
    LevelSound -> loadSound("./sounds/level.mp3"))

  private var playing: List[MappedClip] = List()
  private var cache: Map[String, Stack[Clip]] = sounds.map { case (key, value) => (key, Stack[Clip]()) }
  private var queue: Queue[AnyRef] = Queue()

  // preload
  cache.keys.foreach { k => cache(k).push(createClip(k)) }

  private def loadSound(path: String): Array[Byte] = {
    try {
      return Files.readAllBytes(Paths.get(getClass.getResource(path).toURI()))
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    return null
  }

  private def createClip(sound: String): Clip = {
    val audio = URLAudioSource.getStream(new ByteArrayInputStream(sounds(sound)))
    val info = new DataLine.Info(classOf[Clip], audio.getFormat())
    val clip = AudioSystem.getLine(info).asInstanceOf[Clip]
    clip.open(audio)
    clip
  }

  def playEffect(sound: String) = playSound(sound, 1.0f, false, false)

  def playSound(sound: String, loop: Boolean, stopOthers: Boolean): Unit = playSound(sound, 1.0f, loop, stopOthers)

  def playSound(sound: String, gain: Float, loop: Boolean, stopOthers: Boolean): Unit = {

    this.synchronized {
      queue = queue.enqueue(PlaySound(sound, gain, loop, stopOthers))
    }

  }

  def stopSound(sound: String): Unit = {
    this.synchronized {
      queue = queue.enqueue(StopSound(sound))
    }
  }

  def stopAll(): Unit = {
    this.synchronized {
      queue = queue.enqueue(StopAll())
    }
  }

  private def playSound(c: PlaySound): Unit = {
    
    var clip: Clip = null
    if (!cache(c.sound).isEmpty) {
      clip = cache(c.sound).pop()
      clip.setFramePosition(0)
    } else {
      clip = createClip(c.sound)
    }
    
    if (c.loop) {
      clip.loop(Clip.LOOP_CONTINUOUSLY)
    } else {
      clip.loop(0)
    }
    
    if (c.stopOthers) {
      stopSound(StopSound(c.sound))
    }
    
    val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl];
    gainControl.setValue(toDb(c.gain))
    
    playing ::= MappedClip(c.sound, clip)
    
    clip.start()
  }
  
  private def stopSound(c: StopSound): Unit = {
    playing.filter(m => c.sound == m.sound).foreach(_.clip.stop())
  }
  
  private def stopAll(c: StopAll  ): Unit = {
    playing.foreach(_.clip.stop())
  }
  
  def toDb(gain: Float) = {
    (Math.log(gain) / Math.log(10.0) * 20.0).toFloat;
  }

  private val thread = new Thread(new Runnable() {
    def run(): Unit = {
      while (!Thread.interrupted()) {
        var workingQueue: Queue[AnyRef] = null
        this.synchronized {
          workingQueue = queue
          queue = Queue()
        }
        
        playing.filter(!_.clip.isActive()).foreach { x =>
          cache(x.sound).push(x.clip)
        }
        playing = playing.filter(_.clip.isActive())
        
        workingQueue.foreach { cmd =>
          cmd match {
            case c: PlaySound if playing.length < MaxSounds => playSound(c)
            case c: StopSound => stopSound(c)
            case c: StopAll => stopAll(c)
            case _ => println("overloaded")
          }
        }
        
        Thread.sleep(5)
      }
    }
  })
  thread.setDaemon(true)
  thread.start()
  
}