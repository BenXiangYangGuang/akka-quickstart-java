package com.lightbend.deadLetters

import akka.actor.{ActorSystem, DeadLetter, PoisonPill, Props}
import akka.event.Logging

import scala.io.StdIn

/**
  * Author: fei2
  * Date:  19-4-18 上午11:43
  * Description:
  * Refer To:
  */
class Runner {


  def Run(): Unit = {

  //create the actor system
  val system = ActorSystem("LifeCycleSystem")

  // default Actor constructor
  val lifeCycleActor =
  system.actorOf(Props[LifeCycleActor],
  name = "lifecycleactor")

  val deadLetterMonitorActor =
  system.actorOf(Props[DeadLetterMonitorActor],
  name = "deadlettermonitoractor")

  //subscribe to system wide event bus 'DeadLetter'
  system.eventStream.subscribe(
  deadLetterMonitorActor, classOf[DeadLetter])

  val log = Logging(system, classOf[Runner])
  log.debug("Runner IS UP BABY")

  log.debug("sending lifeCycleActor a few numbers")
  lifeCycleActor ! 100
  lifeCycleActor ! 200
  Thread.sleep(1000)

  log.debug("sending lifeCycleActor a poison pill (kill it)")
  lifeCycleActor ! PoisonPill
  Thread.sleep(1000)
  log.debug("sending lifeCycleActor a few numbers")
  lifeCycleActor ! 100
  lifeCycleActor ! 200


  log.debug("stop lifeCycleActor/deadLetterMonitorActor")
  system.stop(lifeCycleActor)
  system.stop(deadLetterMonitorActor)

  //shutdown the actor system
  log.debug("stop actor system")
  system.terminate()

  StdIn.readLine()
}
}

