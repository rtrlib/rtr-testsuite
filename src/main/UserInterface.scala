package main
import akka.actor.Actor
import akka.actor.Props
import jline.console.completer.FileNameCompleter

class UserInterface extends Actor {
  
  interactiveShell()

  def receive = {
    case value: String => println("Msg received: " + value)
    case _ => println("Unknown msg")
  }

  def interactiveShell() {
    val consoleReader = new jline.console.ConsoleReader()
    var finished = false
    while (!finished) {
      val line = consoleReader.readLine("> ")
      if (line == null) {
      } else if (isValidLine(line)) {
      } else {
        printReadError(line)
      }
    }
  }
  
  def isValidLine(line: String) : Boolean = {
    return false
  }
  
  def printReadError(line: String) = {
    println("Invalid command: " + line)
  }
}