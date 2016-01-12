package ch.fhnw.cpthook

import ch.fhnw.cpthook.model.Level
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import ch.fhnw.ether.controller.event.IEventScheduler.IAction
import java.util.concurrent.CountDownLatch
import ch.fhnw.cpthook.json.JsonSerializer

object LevelLoader {

  def loadFromFile(): Level = {

    val countDownLatch = new CountDownLatch(1)
    var level: Level = null

    SwingUtilities.invokeLater(new Runnable() {
      def run(): Unit = {
        val fileChooser = new JFileChooser
        fileChooser.setDialogTitle("Open Level")
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          level = JsonSerializer.readLevel(fileChooser.getSelectedFile.getAbsolutePath)
        }
        countDownLatch.countDown()
      }
    })

    countDownLatch.await()
    level
  }

  def saveToFile(level: Level): Unit = {

    val countDownLatch = new CountDownLatch(1)

    SwingUtilities.invokeLater(new Runnable() {
      def run(): Unit = {
        val fileChooser = new JFileChooser
        fileChooser.setDialogTitle("Save Level")
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
          JsonSerializer.writeLevel(fileChooser.getSelectedFile.getAbsolutePath, level)
        }
        countDownLatch.countDown()
      }
    })

    countDownLatch.await()
  }

}