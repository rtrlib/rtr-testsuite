package main

import models.RtrPrefix
import scala.io.Source
import net.ripe.ipresource._
object RtrPrefixStore {
   val prefixSet : collection.mutable.Set[RtrPrefix] = collection.mutable.Set()
  
   def readPrefixesFromFile(filename: String) = {
    val lines = Source.fromFile(filename).getLines
    while(lines.hasNext){
      var line = lines.next()
      if(!(line(0) == '#')){
        var prefix_parts : Array[String] = line.split(" ")
        var asn : Asn = new Asn(prefix_parts(0).toLong)
        var prefix : IpRange = IpRange.parse(prefix_parts(1))
        var maxLen : Int = prefix_parts(2).toInt
        prefixSet += new RtrPrefix(asn, prefix, Some(maxLen))
      }
    }
   } 
   
   def getCurrentPrefixes() : Set[RtrPrefix] = {
     return prefixSet.toSet
   }
   
}