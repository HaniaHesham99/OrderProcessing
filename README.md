# Order Discount Processing Engine (Scala)

This Scala project implements a functional rule engine that processes retail transaction data from a CSV file, applies business discount rules, and outputs the results along with detailed logs.

---

##  Features

-  Reads transaction data from `TRX1000.csv`
-  Applies up to 2 matching discount rules per order
-  Calculates average discount and final price
-  Logs each action (including skipped discounts) to `rules_engine.log`
-  Writes processed orders to `final_orders.csv`

## Project Structure
src/
├── main/
│ ├── scala/
│ │ └── orderprocessingapp/
│ │ ├── Main.scala # Entry point
│ │ ├── Order.scala # Data model
│ │ ├── OrderParser.scala # CSV line → Order
│ │ ├── DiscountProcessor.scala # Rules and processing logic
│ │ ├── LoggingPure.scala # Pure logging helpers
│ │ ├── LogEntry.scala # Log entry case class
│ │ └── CSVWriter.scala # CSV output writer
│ └── resources/
│ ├── TRX1000.csv # Input file
│ ├── final_orders.csv # Output file
│ └── rules_engine.log # Log file

## Business Rules Implemented

| Rule Type         | Description |
|------------------|-------------|
| **Product Type**  | If name starts with `Cheese` or `Wine`, apply 10% / 5% |
| **Quantity**      | Discounts based on quantity tiers (up to 10%) |
| **Order Date**    | If order is on June 3rd, apply 50% discount |
| **Expiry Date**   | Discount increases as product nears expiry |
| **Channel**       | If ordered via App, discount = rounded quantity to nearest 5, converted to % |
| **Payment Method**| If paid with Visa, apply 5% |


## Output Format
- **`rules_engine.log`**:
2025-05-19 21:15:01 [INFO] - Applied discount #1: 10.00% to 'Cheese - Gouda'
2025-05-19 21:15:01 [INFO] - Final price for 'Cheese - Gouda': 8.91
2025-05-19 21:15:01 [WARN] - No discounts applied to 'Soap - Scented'



