package main
import akka.actor.Actor
import akka.actor.Props
import jline.console.ConsoleReader
import jline.console.completer.FileNameCompleter
import jline.console.completer.ArgumentCompleter
import jline.console.completer.StringsCompleter
import java.nio.file.{Paths, Files}
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
      case "quit" => true
      case "add" => checkPrefixParams(args)
      case "remove" => checkPrefixParams(args)
      case _ => false
    }
    return isValid
  }
  
  def checkReadCommand(args : Array[String]): Boolean = {
    if(args.length != 2){
      return false
    } else {
      return Files.exists(Paths.get(RtrPrefixStore.convertFilepathTilde(args(1))))
    }
  }
  def checkPrefixParams(args : Array[String]): Boolean = {
    return (args.length != 3)
  }
  
  def printReadError(line: String) = {
    println("Invalid command: " + line)
  }
  
  def executeCommand(line: String) = {
    var args = line.split(" ")
    var success = args(0) match {
      case "read" => RtrPrefixStore.readPrefixesFromFile(args(1))
      case "show" => RtrPrefixStore.printPrefixes()
      case "quit" => System.exit(1)
      case "add" => 
        var prefix = RtrPrefixStore.readPrefix(args(1), args(2), args(3))
        RtrPrefixStore.prefixSet.add(prefix)
      case "remove" => 
        var prefix = RtrPrefixStore.readPrefix(args(1), args(2), args(3))
        if (RtrPrefixStore.prefixSet.contains(prefix)) {
          RtrPrefixStore.prefixSet.remove(prefix)
        } else {
          println("No prefix removed")
        }
    }
  }
  
}