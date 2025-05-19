package orderprocessingapp

import java.io.{File, FileOutputStream, PrintWriter}


// I had a problem connecting to a db due to memory problems and resources
// I will try to solve this problem and maybe do it before the discussion
// For now I will write the output to a csv file and will submit this file
  object CSVWriter {

    def writeOrders(outputPath: String, order: List[Order]): Unit = {
      val file = new File(outputPath)
      val writer = new PrintWriter(new FileOutputStream(file, false))

      // Write CSV header
      writer.println("timestamp,product_name,expiry_date,quantity,unit_price,channel,payment_method,avg_discount,new_price,discounts_list")

      // Write each order
      order.foreach { order =>
        writer.println(
          s"${order.timestamp},${order.product_name},${order.expiry_date},${order.quantity},${order.unit_price}," +
            s"${order.channel},${order.payment_method},${order.avgDiscount},${order.new_price}," +
            s""""${order.discList.mkString(" & ")}""""  // Wrap in quotes!
        )
      }

      writer.close()
    }
  }
