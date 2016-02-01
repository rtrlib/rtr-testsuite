package main

import models.RtrPrefix
import rtr.RTRServer
import scala.io.Source
import net.ripe.ipresource._
import java.nio.file.{Paths, Files}
import java.io.File
import models.RtrPrefix
import rtr.IPv4PrefixPdu
import rtr.IPv6PrefixPdu
class RtrPrefixStore {
   private val prefixSet : collection.mutable.Set[RtrPrefix] = collection.mutable.Set()
   private var rtrServer : Option[RTRServer]= None
   
   private def addPrefix(prefix: RtrPrefix) {
     prefixSet.add(prefix)
   }
   private def removePrefix(prefix: RtrPrefix) {
     addPrefix(prefix)
   }
   def clear() {
     prefixSet.clear()
   }
   
   /* CLI FUNCTIONS */
   def addPrefixString(asn_str: String, prefix_str: String, maxLen_str: String) {
     RTRServer.incSerialNumber()
     var sN = RTRServer.getSerialNumber()
     var prefix :RtrPrefix = readPrefix(asn_str, prefix_str, maxLen_str,1,sN)
     addPrefix(prefix)
     rtrServer.get.serialNotify()
   }
   def removePrefixString(asn_str: String, prefix_str: String, maxLen_str: String) {
     RTRServer.incSerialNumber()
     var sN = RTRServer.getSerialNumber()
     var prefix :RtrPrefix = readPrefix(asn_str, prefix_str, maxLen_str,0,sN)
     removePrefix(prefix)
     rtrServer.get.serialNotify()
   }
   
   def addPrefixes(prefixes : List[rtr.Pdu]) {
     RTRServer.incSerialNumber()
     var sN = RTRServer.getSerialNumber()
     prefixes.foreach { pdu =>
       pdu match {
         case pdu : IPv4PrefixPdu => addPrefix(convertIPv4ToRtrPrefix(pdu,sN))
         case pdu : IPv6PrefixPdu => addPrefix(convertIPv6ToRtrPrefix(pdu,sN))
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
   
   /*  CLI FUNCTIONS BULK */
   def addPrefixesFromFile(filename: String) = {
     RTRServer.incSerialNumber()
     var sN = RTRServer.getSerialNumber()
     readPrefixesFromFile(filename,1,sN)
   }
   
   def removePrefixesFromFile(filename: String) = {
     RTRServer.incSerialNumber()
     var sN = RTRServer.getSerialNumber()
     readPrefixesFromFile(filename,0,sN)
   }
  
   private def readPrefixesFromFile(filename: String,flags : Byte, sN : Int) = {
    val lines = Source.fromFile(convertFilepathTilde(filename)).getLines
    while(lines.hasNext){
      var line = lines.next()
      if(line.length() > 0 && line(0) != '#'){
        val prefix = readPrefixLine(line, flags, sN)
        addPrefix(prefix)
      }
    }
   } 
   
   def getCurrentPrefixes() : Set[RtrPrefix] = {
     return prefixSet.toSet
   }
   
   def printPrefixes() = {
     println("ASN\tPrefix\tMax. Length")
     prefixSet.foreach { prefix => println(prefix.asn + "\t" + prefix.prefix + "\t" + prefix.maxPrefixLength.getOrElse("Missing"))}
     
   }
   
   def searchAsn(search_str: String) = {
     println("ASN\tPrefix\tMax. Length")
     var asn : Asn = new Asn(search_str.toLong)
     prefixSet.foreach {
       prefix => if (asn == prefix.asn) {
         println(prefix.asn + "\t" + prefix.prefix + "\t" + prefix.maxPrefixLength.getOrElse("Missing"))
       }
     }
   }
   def searchPrefix(search_str: String) = {
     println("ASN\tPrefix\tMax. Length")
     var pref : IpRange = IpRange.parse(search_str)
     prefixSet.foreach {
       prefix => if (pref == prefix.prefix) {
         println(prefix.asn + "\t" + prefix.prefix + "\t" + prefix.maxPrefixLength.getOrElse("Missing"))
       }
     }
   }
   
   // Prefix format in file is: [asn] [prefix] [maxlen]
   private def readPrefixLine(line: String, flags : Byte, sN : Int) : RtrPrefix = {
        var prefix_parts : Array[String] = line.split(" ")
        readPrefix(prefix_parts(0), prefix_parts(1), prefix_parts(2),flags,sN)
   }
   
   private def readPrefix(asn_str: String, prefix_str: String, maxLen_str: String, flags : Byte, serialNum : Int) : RtrPrefix = {
     var asn : Asn = new Asn(asn_str.toLong)
     var prefix : IpRange = IpRange.parse(prefix_str)
     var maxLen : Int = maxLen_str.toInt
     new RtrPrefix(asn, prefix, Some(maxLen),flags,serialNum)
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