# intuit_challenge_2
# Sales Data Stream Analysis

This Java application demonstrates functional programming with the Stream API by performing aggregation and grouping queries on a CSV sales dataset. It loads sample sales data, computes several analytics, and prints the results to the console.

## Dataset

The repository includes a small, fabricated dataset at `data/sales.csv` with these columns:

1. `OrderId` – unique identifier for the order
2. `Date` – ISO-8601 date of the sale
3. `Region` – geographic region of the sale
4. `SalesRep` – salesperson responsible for the order
5. `Product` – product name
6. `UnitsSold` – quantity sold
7. `UnitPrice` – price per unit (USD)


## Running the program

Ensure a JDK (17 or later) is available, then compile and run:

```bash
javac src/main/java/com/example/analysis/SalesAnalysis.java
java -cp src/main/java com.example.analysis.SalesAnalysis
```

To run against a different CSV file, pass the path as the first argument:

```bash
java -cp src/main/java com.example.analysis.SalesAnalysis /path/to/custom.csv
```