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
      case "show" => true
      case "quit" => true
      case "addfile" => checkReadCommand(args)
      case "removefile" => checkReadCommand(args)
      case "add" => checkPrefixParams(args)
      case "remove" => checkPrefixParams(args)
      case "clear" => true
      case "search" => checkSearchCommand(args)
      case "load" => true
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
    return (args.length == 4)
  }
  def checkSearchCommand(args: Array[String]): Boolean = {
    if (args.length != 3) {
      return false
    } else {
      var success = args(1) match {
        case "asn" => true
        case "prefix" => true
      }
      return success
    }
  }
  
  def printReadError(line: String) = {
    println("Invalid command: " + line)
  }
  
  def executeCommand(line: String) = {
    var args = line.split(" ")
    var success = args(0) match {
      case "show" => RtrPrefixStore.printPrefixes()
      case "quit" => System.exit(1)
      case "addfile" => RtrPrefixStore.addPrefixesFromFile(args(1))
      case "removefile" => RtrPrefixStore.removePrefixesFromFile(args(1))
      case "add" => RtrPrefixStore.addPrefixString(args(1), args(2), args(3))
      case "remove" => RtrPrefixStore.removePrefixString(args(1), args(2), args(3))
      case "clear" => RtrPrefixStore.clear()
      case "search" => 
        var _ = args(1) match {
          case "asn" => RtrPrefixStore.searchAsn(args(2))
          case "prefix" => RtrPrefixStore.searchPrefix(args(2))
        }
      case "load" =>
        //var port : Int = args(2).toInt
        //val rtrclient = new rtr.RTRClient(args(1), port)
        val rtrclient = new rtr.RTRClient("rpki-validator.realmv6.org", 8282)
        rtrclient.sendPdu(new rtr.ResetQueryPdu())
        var pdus : List[rtr.Pdu] = rtrclient.getResponse()
        pdus.foreach { x => println(x) }
        
    }
  }
  
}