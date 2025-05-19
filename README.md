# Order Discount Processing Engine (Scala)

This Scala project implements a functional rule engine that processes retail transaction data from a CSV file, applies business discount rules, and outputs the results along with detailed logs.

Retail companies often run complex promotional campaigns where different discounts apply to different products based on quantity, product type, order channel, expiry date, and more. Manually managing these rules becomes error-prone and inflexible.

This engine:
- Automates applying up to **two matching discounts per order**
- **Calculates the average of applicable discounts**
- **Updates the order's final price**
- Logs every step to a structured log file for traceability
- Outputs final results to a clean CSV file for analysis or further processing


---

##  Features

-  Reads transaction data from `TRX1000.csv`
-  Applies up to 2 matching discount rules per order
-  Calculates average discount and final price
-  Logs each action (including skipped discounts) to `rules_engine.log`
-  Writes processed orders to `final_orders.csv`

## ✅ Core Functionalities

- **Read & Parse:** Input transactions are read from a CSV file (`TRX1000.csv`) and parsed into `Order` objects.
- **Discount Qualification:** Each order is evaluated against six predefined business rules.
- **Top Discount Selection:** If multiple discounts apply, the top two (by value) are selected and averaged.
- **Pure Logging:** Each applied discount, final price, or missing qualification is logged as a `LogEntry`.
- **Output Generation:**
  - All logs are written to `rules_engine.log`
  - Final orders are exported to `final_orders.csv`

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

  ### ✅ CSV Output (`final_orders.csv`)
| product_name    | unit_price | avg_discount | new_price | discounts_list       |
|----------------|------------|---------------|------------|----------------------|
| Cheese - Gouda | 9.9        | 0.075         | 8.91       | [0.1; 0.05]          |
| Soap - Basic   | 48.05      | 0.0           | 48.05      | []                   |



