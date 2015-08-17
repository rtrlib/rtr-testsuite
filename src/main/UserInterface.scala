package main
import akka.actor.Actor
import akka.actor.Props
import jline.console.ConsoleReader
import jline.console.completer.FileNameCompleter
import jline.console.completer.ArgumentCompleter
import jline.console.completer.StringsCompleter
import java.nio.file.{Paths, Files}
import java.io.File

class UserInterface extends Actor {
  
  interactiveShell()

  def receive = {
    case value: String => println("Msg received: " + value)
    case _ => println("Unknown msg")
  }

  def interactiveShell() {
    var finished = false
    val consoleReader = createReader()
    
    while (!finished) {
      val line = consoleReader.readLine("rtr-testsuite> ")
      if (line != null) {
        if(isValidCommand(line)){
          executeCommand(line)
        } else {
          printReadError(line)
        }
      }
    }
  }
  
  def createReader() : ConsoleReader = {
    val consoleReader = new jline.console.ConsoleReader()
    val completer = new ArgumentCompleter(new StringsCompleter("read"), new FileNameCompleter())
    consoleReader.addCompleter(completer)
    consoleReader.addCompleter(new StringsCompleter("show"))
    consoleReader
  }
  
  def isValidCommand(line: String) : Boolean = {
    var args = line.split(" ")
    val isValid = args(0) match {
      case "read" => checkReadCommand(args)
      case "show" => true
      case _ => false
    }
    return isValid
  }
  
  def checkReadCommand(args : Array[String]): Boolean = {
    if(args.length != 2){
      return false
    } else {
      if(args(1).startsWith("~" + File.separator)) {
        args(1) = System.getProperty("user.home") + args(1).substring(1);
      }
      return Files.exists(Paths.get(args(1)))
    }
  }
  
  def printReadError(line: String) = {
    println("Invalid command: " + line)
  }
  
  def executeCommand(line: String) = {
    var args = line.split(" ")
    var success = args(0) match {
      case "read" => RtrPrefixStore.readPrefixesFromFile(args(1))
      case "show" => RtrPrefixStore.printPrefixes()
    }
    
  }
}