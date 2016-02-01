/**
 * The BSD License
 *
 * Copyright (c) 2010-2012 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package main

import rtr.RTRServer
import models.RtrPrefix
import akka.actor.Props
import net.ripe.ipresource._
import scala.util.Random
object Main {

  def main(args: Array[String]): Unit = {
    println("RTRTestSuite")
    new Main(args)
  }
}

class Main(args: Array[String]) {
  implicit val actorSystem = akka.actor.ActorSystem()
  var port : Int = 8282
  if (args.length >= 2 && args(0) == "-p"){
    port = args(1).toInt
  }
  private def runRtrServer(prefStore : RtrPrefixStore): RTRServer = {
    var sessionID = Random.nextInt(65536).toShort
    val rtrServer = new RTRServer(
      port = port,
      closeOnError = false,
      sendNotify = false,
      getCurrentCacheSerial = {
        () => RTRServer.getSerialNumber;
      },
      getCurrentRtrPrefixes = {
        prefStore.getCurrentPrefixes
      },
      getCurrentSessionId = {
        () => sessionID
      },
      hasTrustAnchorsEnabled = {
        () => false
      })
    rtrServer.startServer()
    rtrServer
  }
  
  val prefixStore = new RtrPrefixStore();
  val rtrServer = runRtrServer(prefixStore)
  prefixStore.setServer(rtrServer)
  val userInterface = actorSystem.actorOf(Props(new UserInterface(prefixStore)), "userInteface")
}


