package orderprocessingapp


import java.time. Period

object DiscountProcessor {

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


  // 2nd
  def dateQualifier(order: Order): Boolean = {
    if (order.timestamp.getMonthValue == 6 & order.timestamp.getDayOfMonth == 3)
      true
    else false
  }

  def dateDisc(order: Order): Double = 0.5


  // 3rd
  def quantityQualifier(order: Order): Boolean = {
    if (order.quantity > 5) true
    else false
  }

  def quantityDisc(order: Order): Double = {
    if (order.quantity <= 5) 0.0
    else if ((order.quantity >= 6) & (order.quantity <= 9)) 0.05
    else if ((order.quantity >= 10) & (order.quantity <= 14)) 0.07
    else 0.1
  }


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
    val expiryDisc = (30 - daysLeft) / 10

    expiryDisc
  }


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
    // used rounded because if  3 / 100 = 0 not 0.03 but only as an integer
    val rounded = if (order.quantity % 5 == 0)
      order.quantity
    else
      order.quantity + (5 - order.quantity % 5)

    rounded / 100.0

  }

  // 6th
  def paymentQualifier(order: Order): Boolean = {
    if (order.payment_method.toLowerCase == "visa") true
    else false
  }

  def paymentDisc(order: Order): Double = 0.05

  def getDiscount(order: Order): Order = {
    // These functions take order Record as in input and the output is the discount value which is a double
    // Therefore this list includes list of functions that consists of a tuple
    // Each tuple consists of two function (one that takes order instance and produce boolean output)
    // the other ( takes order instance and produces a double output)
    // which takes the Order Record as in Input and a Double value as their output
    val matchDiscounts: List[(Order => Boolean, Order => Double)] = List(
      (productNameQualifier, productNameDisc),
      (quantityQualifier, quantityDisc),
      (dateQualifier, dateDisc),
      (expiryQualifier, expiryDisc),
      (channelQualifier, channelDisc),
      (paymentQualifier, paymentDisc)
    ) // using this list of tuples you can simply create any amount of qualifiers and discount functions and mix an match between them easily

    // Iterate on each tuple and filter
    val sortedDisc = matchDiscounts
      .filter(tuple => tuple._1(order))
      .map(tuple => tuple._2(order))
      .sorted
      .reverse
      .take(2)
    val avgDiscount: Option[Double] =
      if (sortedDisc.nonEmpty) Some(sortedDisc.sum / sortedDisc.length)
      else None
    val new_price = order.unit_price * (1 - avgDiscount.getOrElse(0.0))

    val updatedOrder = order.copy(
      avgDiscount = avgDiscount,
      new_price = new_price,
      discList = sortedDisc
    )

    updatedOrder
  }

  def getDiscountWithLogs(order: Order): Either[LogEntry, (Order, List[LogEntry])] = {
    val updatedOrder = getDiscount(order)

    if (updatedOrder.discList.isEmpty)
      Left(LoggingPure.warn(s"No discounts applied to '${order.product_name}'"))
    else {
      val discountLogs = updatedOrder.discList.zipWithIndex.map {
        case (disc, idx) =>
          LoggingPure.info(f"Applied discount #${idx + 1}: ${disc * 100}%.2f%% to '${order.product_name}'")
      }

      val summaryLog = LoggingPure.info(f"Final price: ${updatedOrder.new_price}%.2f")

      Right((updatedOrder, discountLogs :+ summaryLog))
    }

  }

}
