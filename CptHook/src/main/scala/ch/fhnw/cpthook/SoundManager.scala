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
case class PlayEffect(val sound: String, val hearingDistance: Float, val x: Float, val y: Float)
case class StopSound(val sound: String)
case class StopAll()
case class VolumeAdjust(val gain: Float)
case class CenterUpdate(val x: Float, val y: Float)
/**
 * MappedClip is used to save the currently playing clips and keep a mapping to what sound it was
 */
case class MappedClip(val sound: String, val gain: Float, val clip: Clip)

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

  private var volumeAdjustment = 1.0f;
  private var center = (0f, 0f)
  
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
  
  def getVolumeAdjustment(): Float = volumeAdjustment

  def playEffect(sound: String, x: Float, y: Float) = {
    this.synchronized {
      queue = queue.enqueue(PlayEffect(sound, 20, x, y))
    }
  }
  
  def playSound(sound: String): Unit = playSound(sound,0.6f, false, false)

  def playSound(sound: String, loop: Boolean, stopOthers: Boolean): Unit = playSound(sound, 0.6f, loop, stopOthers)

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
  
  def volumeAdjust(gain: Float): Unit = {
    this.synchronized {
      queue = queue.enqueue(VolumeAdjust(gain))
    }
  }
  
  def updateCenter(x: Float, y: Float) {
    center = (x, y)
    /*this.synchronized {
      queue = queue.enqueue(CenterUpdate(x, y))
    }*/
  }
  
  private def getClip(sound: String): Clip = {
    var clip: Clip = null
    if (!cache(sound).isEmpty) {
      clip = cache(sound).pop()
      clip.setFramePosition(0)
    } else {
      clip = createClip(sound)
    }
    clip
  }

  private def playSound(c: PlaySound): Unit = {
    
    val clip = getClip(c.sound)
    
    if (c.loop) {
      clip.loop(Clip.LOOP_CONTINUOUSLY)
    } else {
      clip.loop(0)
    }
    
    if (c.stopOthers) {
      stopSound(StopSound(c.sound))
    }
    
    val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl];
    gainControl.setValue(toDb(c.gain * volumeAdjustment))
    
    playing ::= MappedClip(c.sound, c.gain, clip)
    clip.start()
  }
  
  private def playEffect(c: PlayEffect): Unit = {
    
    val distance = Math.abs(Math.sqrt(Math.pow(center._1 - c.x, 2) + Math.pow(center._2 - c.y, 2))).toFloat
    
    if (distance > 2 * c.hearingDistance) {
      return
    }
    
    val clip = getClip(c.sound)
    var gain: Float = Math.max(0f, 1f - distance / c.hearingDistance)
    
    val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl];
    gainControl.setValue(toDb(gain * volumeAdjustment))
    
    playing ::= MappedClip(c.sound, gain, clip)
    clip.start()
  }
  
  private def stopSound(c: StopSound): Unit = {
    playing.filter(m => c.sound == m.sound).foreach(_.clip.stop())
  }
  
  private def stopAll(c: StopAll  ): Unit = {
    playing.foreach(_.clip.stop())
  }
  
  private def toDb(gain: Float) = {
    (Math.log(gain) / Math.log(10.0) * 20.0).toFloat;
  }
  
  private def volumeAdjust(c: VolumeAdjust): Unit = {
    volumeAdjustment = c.gain
    playing.foreach { m => 
      val gainControl = m.clip.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl];
      gainControl.setValue(toDb(m.gain * volumeAdjustment))
    }
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
            case c: PlayEffect if playing.length < MaxSounds => playEffect(c)
            case c: StopSound => stopSound(c)
            case c: StopAll => stopAll(c)
            case c: VolumeAdjust => volumeAdjust(c)
            case CenterUpdate(x, y) => center = (x, y)
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