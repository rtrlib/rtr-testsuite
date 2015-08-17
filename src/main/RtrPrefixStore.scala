package main

import models.RtrPrefix
import scala.io.Source
import net.ripe.ipresource._
import java.nio.file.{Paths, Files}
import java.io.File
object RtrPrefixStore {
   val prefixSet : collection.mutable.Set[RtrPrefix] = collection.mutable.Set()
  
   def readPrefixesFromFile(filename: String) = {
    val lines = Source.fromFile(convertFilepathTilde(filename)).getLines
    while(lines.hasNext){
      var line = lines.next()
      if(line.length() > 0 && line(0) != '#'){
        val prefix = readPrefix(line)
        line(0) match {
          case '+' => prefixSet.add(prefix)
          case '-' => prefixSet.remove(prefix)
        }
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
   
   // Prefix format in file is: [+,-] [asn] [prefix] [maxlen]
   def readPrefix(line: String) : RtrPrefix = {
        var prefix_parts : Array[String] = line.split(" ")
        var asn : Asn = new Asn(prefix_parts(1).toLong)
        var prefix : IpRange = IpRange.parse(prefix_parts(2))
        var maxLen : Int = prefix_parts(3).toInt
        new RtrPrefix(asn, prefix, Some(maxLen))
   }
   
  def convertFilepathTilde(path: String) : String = {
      if(path.startsWith("~" + File.separator)) {
        return System.getProperty("user.home") + path.substring(1);
      }
      return path
  }
   
}