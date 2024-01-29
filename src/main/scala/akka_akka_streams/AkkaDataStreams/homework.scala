package akka_akka_streams.AkkaDataStreams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{
  Broadcast,
  Flow,
  GraphDSL,
  RunnableGraph,
  Sink,
  Source,
  Zip,
  ZipWith
}
import akka.stream.{ActorMaterializer, ClosedShape, UniformFanInShape}

/*написать граф дсл, где есть какой то входной поток(целочисленный), он должен быть разделен на 3 (broadcast).
первый поток - все элементы умножаем на 10
второй поток - все элементы умножаем на 2
третий поток - все элементы умножаем на 3
потом собираем это все в один поток (zip) в котором эти 3 подпотока должны быть сложены
1 2 3 4 5 -> 1 2 3 4 5-> 10 20 30 40 50
-> 1 2 3 4 5-> 2 4 6 8 10 -> (10,2,3), (20,4,6),(30,6,9),(40,8,12),(50,10,15)-> 15, 30, 45, 60, 75
-> 1 2 3 4 5 -> 3 6 9 12 15
2) не обязательно, задача со *. входной поток должен идти из кафки (тоесть написать продьюсер, и
в консьюмере реализовать логику из пункта 1)*/

object homework {
  implicit val system = ActorSystem("fusion")
  implicit val materializer = ActorMaterializer()

  val sum = GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val zip1 = b.add(ZipWith[Int, Int, Int](math.addExact _))
    val zip2 = b.add(ZipWith[Int, Int, Int](math.addExact _))
    zip1.out ~> zip2.in0

    UniformFanInShape(zip2.out, zip1.in0, zip1.in1, zip2.in1)
  }

  val graph = GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    //1. source
    val input = builder.add(Source(1 to 5))

    val broadcast = builder.add(Broadcast[Int](3))

    val first = builder.add(Flow[Int].map(x => x * 10))
    val second = builder.add(Flow[Int].map(x => x * 2))
    val third = builder.add(Flow[Int].map(x => x * 3))

    val output = builder.add(Sink.foreach[(Int)](println))

    val result = builder.add(sum)

    //shape
    input ~> broadcast

    broadcast.out(0) ~> first ~> result.in(0)
    broadcast.out(1) ~> second ~> result.in(1)
    broadcast.out(2) ~> third ~> result.in(2)

    result.out ~> output

    //close shape
    ClosedShape
  }

  def main(args: Array[String]): Unit = {
    RunnableGraph.fromGraph(graph).run()

  }
}
