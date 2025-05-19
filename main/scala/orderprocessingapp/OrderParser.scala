package orderprocessingapp

import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Create a case class of the order record
case class Order(
                  timestamp: LocalDate,
                  product_name: String,
                  expiry_date: LocalDate,
                  quantity: Int,
                  unit_price: Double,
                  channel: String,
                  payment_method: String,
                  avgDiscount: Option[Double] = None,
                  new_price: Double = 0.0,
                  discList: List[Double] = List()
                )

//Now split the line into array elements and assign each to a variable that will be assigned to the Order Record
object OrderParser {
  def toOrder(line: String): Order = {
    val orderArray = line.split(",")
    val timestamp = LocalDate.parse(orderArray(0).split("T")(0), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val product_name = orderArray(1)
    val expiry_date = LocalDate.parse(orderArray(2).split("T")(0), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val quantity = orderArray(3).toInt
    val unit_price = orderArray(4).toDouble
    val channel = orderArray(5)
    val payment_method = orderArray(6)

    //Now return the OrderRecord
    Order(timestamp, product_name, expiry_date, quantity, unit_price, channel, payment_method)
  }
}
