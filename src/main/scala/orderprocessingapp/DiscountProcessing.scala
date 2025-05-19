import java.io.{FileOutputStream, PrintWriter}
import java.io.{File, FileOutputStream, PrintWriter}
import scala.io.{BufferedSource, Source}
import java.time.{LocalDate, Period}
import java.time.format.DateTimeFormatter
import java.sql.{Connection, DriverManager, PreparedStatement}


//This is my First trial- All in one file 
object DiscountProcessing extends App {

  //First read from the csv file
  val source: BufferedSource = Source.fromFile("src/main/resources/TRX1000.csv")
  val lines: List[String] = source.getLines().drop(1).toList.tail // drop header
  //lines.take(10).foreach(println)


  def readCSV(filepath: String): List[String] = {
    val source: BufferedSource = Source.fromFile(filepath)
    val lines: List[String] = source.getLines().drop(1).toList.tail // drop header
    source.close() // Important for avoiding file handle leaks
    lines // return the lines (list of strings)
  }

  // Create a case class of the order record
  case class Order (timestamp: LocalDate, product_name: String, expiry_date: LocalDate,
                    quantity: Int, unit_price: Double, channel: String, payment_method: String,
                    avgDiscount: Double= 0.0, new_price: Double = 0.0, discList: List[Double] = List()
                   )

  //Now split the line into array elements and assign each to a variable that will be assigned to the Order Record
  def toOrder(line: String) : Order = {
    val orderArray = line.split(",") // This split the line into an array of strings
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

  // Now let's create a qualifier function and a discount function to be applied on each Order Instance
  // Create the Cheese and Wine qualifier
  // A qualifier's input is an instance of Order class and output is boolean

  // will create one just for checking the output of the discount value
  //  def productNameQualifier(order: Order) : Double = order.product_name.toLowerCase.split(" - ")(0) match {
  //    case "wine" => 0.05
  //    case "cheese" => 0.1
  //    case _ => 0.0
  //  }

  /////////////////////////////////////////////
  // 1st
  // now create the same  qualifier with the output as boolean
  def productNameQualifier(order: Order): Boolean = {
    if (order.product_name.toLowerCase.startsWith("cheese") || order.product_name.toLowerCase.startsWith("wine")) true
    else false
    }

  def productNameDisc(order: Order): Double = order.product_name.split(" - ")(0).toLowerCase() match {
    case "wine" => 0.05
    case "cheese" => 0.1
    case _ => 0.0
  }

  /////////////////////////////////////////////
  // 2nd
  def dateQualifier(order: Order): Boolean = {
    if (order.timestamp.getMonthValue == 6 & order.timestamp.getDayOfMonth == 3)
      true
    else false
  }
  def dateDisc(order: Order): Double = 0.5

  /////////////////////////////////////////////
  // 3rd
  def quantityQualifier (order: Order): Boolean = {
    if (order.quantity > 5) true
    else false
  }
  def quantityDisc(order: Order): Double = {
    if (order.quantity <= 5) 0.0
    else if ((order.quantity >= 6) & (order.quantity <= 9 )) 0.05
    else if ((order.quantity >= 10) & (order.quantity <= 14 )) 0.07
    else 0.1
  }

  /////////////////////////////////////////////
  // 4th
  def expiryQualifier(order: Order): Boolean = {
    val period: Period = Period.between(order.timestamp, order.expiry_date)
    val daysLeft = period.getDays + (period.getMonths * 30).toInt
    if (daysLeft < 30) true
    else false
  }

  def expiryDisc(order: Order): Double = {
    val period: Period = Period.between(order.timestamp, order.expiry_date)
    val daysLeft = period.getDays + (period.getMonths * 30).toInt
    val expiryDisc = (30 - daysLeft)/10

    expiryDisc
  }

  /////////////////////////////////////////////
  // 5th

  def channelQualifier(order: Order): Boolean = {
    if (order.channel == "App") true
    else false
  }

  def channelDisc(order: Order): Double = {
    //quantity rounded up to the nearest multiple of 5
    // if quantity = 7 then discount percentage = 10%
    //  For example, 17 mod 5 = 2, since if we divide 17 by 5, we get 3 with remainder 2
    // Therefore discount will be 17 + ( 5 - 2 )
    // if quantity = 22 --> 22 % 5 = 2 --> discount = 22 + (5 -2) = 25
    val discount = if (order.quantity % 5 == 0) order.quantity / 10
    else order.quantity + (5 - order.quantity % 5)/10

    discount
  }

  /////////////////////////////////////////////
  // 6th
  def paymentQualifier(order: Order): Boolean = {
    if (order.payment_method.toLowerCase == "visa") true
    else false
  }

  def paymentDisc(order: Order): Double = 0.05


  // What I need is to match each qualifier to a specific discount
  // Let's try it on multiple qualifiers
  //  getDiscount takes an Order Record and returns an new instance of the Order class but with a specific functions result in the new columns
  def getDiscount(order: Order): Order = {
    // These functions take order Record as in input and the output is the discount value which is a double
    // Therefore this list includes list of functions that consists of a tuple
    // Each tuple consists of two function (one that takes order instance and produce boolean output)
    // the other ( takes order instance and produces a double output)
    // which takes the Order Record as in Input and a Double value as their output
    val matchDiscounts : List[(Order => Boolean, Order => Double)] = List(
      (productNameQualifier,productNameDisc),
      (quantityQualifier,quantityDisc),
      (dateQualifier,dateDisc),
      (expiryQualifier,expiryDisc),
      (channelQualifier,channelDisc),
      (paymentQualifier, paymentDisc)
    ) // using this list of tuples you can simply create any amount of qualifiers and discount functions and mix an match between them easily

    // Iterate on each tuple and filter
    val sortedDisc = matchDiscounts.filter(tuple => tuple._1(order)).map(tuple => tuple._2(order)).sorted.reverse.take(2)
    val avgDiscount = if (sortedDisc.nonEmpty) sortedDisc.sum / sortedDisc.length else 0.0
    val new_price = order.unit_price *(1-avgDiscount)

    Order(order.timestamp, order.product_name, order.expiry_date, order.quantity,
          order.unit_price, order.channel, order.payment_method,avgDiscount,new_price,sortedDisc
    )
  }


  // Now use the map function to iterate in each line in the lines(list of string) and apply the function (toOrder) on it
  // Doing that will work on each line, split each line and convert each line to an Order class type
  //lines.take(10).map(toOrder).foreach(println)
  //lines.map(toOrder).map(productNameQualifier).take(10).foreach(println)

  // what if I want to return the whole record plus the productNameQualifier
  // for each line:
  // First I will apply the toOrder function which will convert each line to an instance of the Order class
  // Then for each Order instance will apply the getDiscount function which check the qualifier and discount match
  // lines.map(toOrder).map(order => (getDiscount(order))).take(20).foreach(println)





  //lines.take(10).foreach(println)


}