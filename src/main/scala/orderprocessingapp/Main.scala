package orderprocessingapp

import java.io.PrintWriter


// src/main/scala/orderprocessingapp/Main.scala
object Main extends App {
  val filepath = "src/main/resources/TRX1000.csv"

  val lines: List[String] = CSVReader.readCSV(filepath)  // gets raw lines

  val orders: List[Order] = lines.map(OrderParser.toOrder)  // parse lines into Order instances

  val orderProcLogs: List[(Order, List[LogEntry])] = orders.map { order =>
    DiscountProcessor.getDiscountWithLogs(order) match {
      case Right(result) => result
      case Left(log)     => (order, List(log)) // no discounts applied
    }
  }

  // Step 3: Extract final orders and all logs
  val ordersProcessed: List[Order] = orders.map(DiscountProcessor.getDiscount)
  val allLogs: List[LogEntry] = orderProcLogs.flatMap(_._2)

  // Step 4: Write logs to file
  val logWriter = new PrintWriter("src/main/resources/rules_engine.log")
  allLogs.foreach { log =>
    logWriter.println(s"${log.timestamp} [${log.level}] - ${log.message}")
  }
  logWriter.close()

  // Step 5: Print top 5 processed orders
  ordersProcessed.take(5).foreach(println)  // test output

  // Step 6: Write output to a csv File
  CSVWriter.writeOrders("src/main/resources/TRX1000_output.csv", ordersProcessed)
}
