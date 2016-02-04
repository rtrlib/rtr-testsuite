package main.scala

import java.nio.file.Files
import java.nio.file.Paths
import java.io.File
import main.scala.models.RtrPrefix
import main.scala.rtr.RTRServer
import main.scala.rtr.Pdu
import io.Source
import net.ripe.ipresource._

import main.scala.models.RtrPrefix
import main.scala.rtr.IPv4PrefixPdu
import main.scala.rtr.IPv6PrefixPdu
class RtrPrefixStore {
   private val prefixSet : collection.mutable.Set[RtrPrefix] = collection.mutable.Set()
   private val currentPrefixSet : collection.mutable.Set[RtrPrefix] = collection.mutable.Set()
   private var rtrServer : Option[RTRServer]= None
   
   private def addAnnouncement(prefix : RtrPrefix) : Boolean = {
     var legit = currentPrefixSet.add(new RtrPrefix(prefix.asn,prefix.prefix,prefix.maxPrefixLength,1,0))
     if(legit)
       prefixSet.add(prefix)
     return legit
   }
   
   private def addWithdrawal(prefix : RtrPrefix) : Boolean = {
     val legit = currentPrefixSet.remove(new RtrPrefix(prefix.asn,prefix.prefix,prefix.maxPrefixLength,1,0))
     if(legit)
       prefixSet.add(prefix)
     return legit
   }

   def clear() {
     prefixSet.clear()
     currentPrefixSet.clear()
   }
   
   /* CLI FUNCTIONS */
   def addPrefixString(asn_str: String, prefix_str: String, maxLen_str: String) {
     var sN = RTRServer.getSerialNumber()
     var prefix :RtrPrefix = readPrefix(asn_str, prefix_str, maxLen_str,1,sN)
     if(prefix == null)
       return 
     if(addAnnouncement(prefix)){
       RTRServer.incSerialNumber()
       rtrServer.get.serialNotify()
     }
   }
   def removePrefixString(asn_str: String, prefix_str: String, maxLen_str: String) {
     var sN = RTRServer.getSerialNumber() + 1
     var prefix :RtrPrefix = readPrefix(asn_str, prefix_str, maxLen_str,0,sN)
     if(prefix == null)
       return 
     if(addWithdrawal(prefix)){
       RTRServer.incSerialNumber()
       rtrServer.get.serialNotify()
     }
   }

   /*  CLI FUNCTIONS BULK */
   def addPrefixesFromFile(filename: String) = {
     var sN = RTRServer.getSerialNumber() + 1
     if(readPrefixesFromFile(filename,1,sN)){
       RTRServer.incSerialNumber()
       rtrServer.get.serialNotify()
     }
   }
   
   def removePrefixesFromFile(filename: String) = {
     var sN = RTRServer.getSerialNumber() + 1
     if(readPrefixesFromFile(filename,0,sN)){
       RTRServer.incSerialNumber()
       rtrServer.get.serialNotify()
     }
   }

   private def readPrefixesFromFile(filename: String,flags : Byte, sN : Int) = {
    val lines = Source.fromFile(convertFilepathTilde(filename)).getLines
    var legit : Boolean = false
    while(lines.hasNext){
      var line = lines.next()
      if(line.length() > 0 && line(0) != '#'){
        val prefix = readPrefixLine(line, flags, sN)
        if(flags == 1){
          legit = addAnnouncement(prefix) || legit
        }
        else{
          legit = addWithdrawal(prefix) || legit
        }
      }
    }
    legit
   } 
   
   def addPrefixes(prefixes : List[Pdu]) {
     RTRServer.incSerialNumber()
     var sN = RTRServer.getSerialNumber()
     prefixes.foreach { pdu =>
       pdu match {
         case pdu : IPv4PrefixPdu => addAnnouncement(convertIPv4ToRtrPrefix(pdu,sN))
         case pdu : IPv6PrefixPdu => addAnnouncement(convertIPv6ToRtrPrefix(pdu,sN))
         case _ =>
       }
     }
     rtrServer.get.serialNotify()
   }

  def convertIPv4ToRtrPrefix(pdu: IPv4PrefixPdu, sN : Int): RtrPrefix = {
    var asn: Asn = pdu.asn
    var prefix : IpRange = IpRange.prefix(pdu.ipv4PrefixStart, pdu.prefixLength)
    var maxLen: Byte = pdu.maxLength
    new RtrPrefix(asn,prefix,Some(maxLen),pdu.flags,sN)
  }
  
  def convertIPv6ToRtrPrefix(pdu: IPv6PrefixPdu, sN : Int): RtrPrefix = {
    var asn: Asn = pdu.asn
    var prefix : IpRange = IpRange.prefix(pdu.ipv6PrefixStart, pdu.prefixLength)
    var maxLen: Byte = pdu.maxLength
    new RtrPrefix(asn,prefix,Some(maxLen),pdu.flags,sN)
  }
   
  
   def getPrefixes() : Set[RtrPrefix] = {
     return prefixSet.toSet
   }
   def getCurrentPrefixes() : Set[RtrPrefix] = {
     return currentPrefixSet.toSet
   }
   
   def printPrefixes() = {
     println(String.format("%-10s\t%-35s\t%-6s","ASN","Prefix", "MaxLen"))
     currentPrefixSet.foreach { prefix => println("%-10s\t%-35s\t%-6s".format(prefix.asn,prefix.prefix,prefix.maxPrefixLength.getOrElse("Missing")))}
     
   }
   
   def searchAsn(search_str: String) = {
     println("ASN\tPrefix\tMax. Length")
     var asn : Asn = new Asn(search_str.toLong)
     currentPrefixSet.foreach {
       prefix => if (asn == prefix.asn) {
         println(prefix.asn + "\t" + prefix.prefix + "\t" + prefix.maxPrefixLength.getOrElse("Missing"))
       }
     }
   }
   def searchPrefix(search_str: String) = {
     println("ASN\tPrefix\tMax. Length")
     var pref : IpRange = IpRange.parse(search_str)
     currentPrefixSet.foreach {
       prefix => if (pref == prefix.prefix) {
         println("%-10s\t%-35s\t%7s".format(prefix.asn,prefix.prefix,prefix.maxPrefixLength.getOrElse("Missing")))
       }
     }
   }
   
   // Prefix format in file is: [asn] [prefix] [maxlen]
   private def readPrefixLine(line: String, flags : Byte, sN : Int) : RtrPrefix = {
        var prefix_parts : Array[String] = line.split(" ")
        readPrefix(prefix_parts(0), prefix_parts(1), prefix_parts(2),flags,sN)
   }
   
   private def readPrefix(asn_str: String, prefix_str: String, maxLen_str: String, flags : Byte, serialNum : Int) : RtrPrefix = {
     try {
     var asn : Asn = new Asn(asn_str.toLong)
     var prefix : IpRange = IpRange.parse(prefix_str)
     var maxLen : Int = maxLen_str.toInt
     new RtrPrefix(asn, prefix, Some(maxLen),flags,serialNum)
     }
     catch {
       case e: IllegalArgumentException => println("Illegal argument");null
     }
   }
   
  def convertFilepathTilde(path: String) : String = {
      if(path.startsWith("~" + File.separator)) {
        return System.getProperty("user.home") + path.substring(1);
      }
      return path
  }
  
  def setServer(rtrServ : RTRServer){
    rtrServer = Some(rtrServ)
  }
   
}