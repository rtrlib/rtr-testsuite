package main.scala
import akka.actor.Actor
import jline.console.ConsoleReader
import jline.console.completer.FileNameCompleter
import jline.console.completer.ArgumentCompleter
import jline.console.completer.StringsCompleter
import java.nio.file.Files
import java.nio.file.Paths
import main.scala.RtrPrefixStore
class UserInterface(prefStore : RtrPrefixStore) extends Actor {
  
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
    consoleReader.addCompleter(new StringsCompleter("add","addfile", "clear", "help", "load", "quit", 
        "remove", "removefile", "search","show"))
    var completer = new ArgumentCompleter(new StringsCompleter("addfile"), new FileNameCompleter())
    consoleReader.addCompleter(completer)
    completer = new ArgumentCompleter(new StringsCompleter("removefile"), new FileNameCompleter())
    consoleReader.addCompleter(completer)
    consoleReader.addCompleter(new StringsCompleter("show"))
    consoleReader.addCompleter(new StringsCompleter("quit"))
    consoleReader.addCompleter(new StringsCompleter("add"))
    consoleReader.addCompleter(new StringsCompleter("remove"))
    consoleReader.addCompleter(new StringsCompleter("clear"))
    completer = new ArgumentCompleter(new StringsCompleter("search"), new StringsCompleter("asn", "prefix"))
    consoleReader.addCompleter(completer)
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
      case "load" => checkLoadCommand(args)
      case "help" => true
      case _ => false
    }
    return isValid
  }
  
  def checkLoadCommand(args : Array[String]) : Boolean = {
    if(args.length != 3){
      return false
    } else {
      return true
    }
  }
  
  def checkReadCommand(args : Array[String]): Boolean = {
    if(args.length != 2){
      return false
    } else {
      return Files.exists(Paths.get(prefStore.convertFilepathTilde(args(1))))
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
    println("Invalid command or parameter: " + line)
  }
  
  def printHelp() = {
    println("%-35s\tFunction\n".format("Command"));
    println("%-35s\tShows the current ROA entries".format("show"))
    println("%-35s\tShuts down the program".format("quit"))
    println("%-35s\tAdds all ROA entries (by line) in the file. Format is '[ASN] [Prefix] [MaxLen]'".format("addfile [filepath]"))
    println("%-35s\tAdds a ROA entry".format("add [ASN] [Prefix] [MaxLen]"))
    println("%-35s\tRemoves all ROA entries (by line) in the file. Format is '[ASN] [Prefix] [MaxLen]'".format("removefile [filepath]"))
    println("%-35s\tRemoves a ROA entry".format("remove [ASN] [Prefix] [MaxLen]"))
    println("%-35s\tRemoves all ROA entries and withdraw history, keeps serial number though".format("clear"))
    println("%-35s\tSearches for all ROA entries with the given asn/prefix".format("search asn/prefix [ASN/Prefix]"))
    println("%-35s\tConnects to the given cache server at the given port and loads all ROA entries from it".format("load [cache_address] [port]"))
    println("%-35s\tPrints this help".format("help"))
  }
  
  def executeCommand(line: String) = {
    var args = line.split(" ")
    var success = args(0) match {
      case "show" => prefStore.printPrefixes()
      case "quit" => System.exit(1)
      case "addfile" => prefStore.addPrefixesFromFile(args(1))
      case "removefile" => prefStore.removePrefixesFromFile(args(1))
      case "add" => prefStore.addPrefixString(args(1), args(2), args(3))
      case "remove" => prefStore.removePrefixString(args(1), args(2), args(3))
      case "clear" => prefStore.clear()
      case "search" => 
        var _ = args(1) match {
          case "asn" => prefStore.searchAsn(args(2))
          case "prefix" => prefStore.searchPrefix(args(2))
        }
      case "load" =>
        var port : Int = args(2).toInt
        val rtrclient = new rtr.RTRClient(args(1), port)
        var pdus : List[rtr.Pdu] = rtrclient.getAllROAs
        prefStore.addPrefixes(pdus)
        rtrclient.close()
      case "help" => printHelp()
    }
  }
  
}