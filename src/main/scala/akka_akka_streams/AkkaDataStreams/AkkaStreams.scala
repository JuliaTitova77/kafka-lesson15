package akka_akka_streams.AkkaDataStreams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, Sink, Flow}

object AkkaStreams extends App {
  implicit val system = ActorSystem("dddd")
  implicit val materializer = ActorMaterializer()
  val source: Source[Int, NotUsed] = Source(1 to 10)

  val flow = Flow[Int].map(x => x + 1)
  val sink = Sink.foreach[Int](println)

  val graph1 = source.to(sink)
  val graph2 = source.via(flow).to(sink)

  //graph2.run()

  val simpleSource = (Source(1 to 10))
  val simpleFlow = Flow[Int].map(_ + 1)
  val simpleFlow2 = Flow[Int].map(_ * 10)
  val simpleSink = Sink.foreach[Int](println)

  val graph3 = simpleSource
    .via(simpleFlow)
    .via(simpleFlow2)
    .to(simpleSink)

  //graph3.run()
  val hardflow3 = Flow[Int].map { x =>
    Thread.sleep((1000))
    x + 10
  }
  val hardflow4 = Flow[Int].map { x =>
    Thread.sleep((1000))
    x * 10
  }

  simpleSource.async
    .via(hardflow3).async
    .via(hardflow4).async
    .to(simpleSink)
    .run()

}
