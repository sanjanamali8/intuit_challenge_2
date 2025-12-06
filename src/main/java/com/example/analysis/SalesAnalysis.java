package com.example.analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Demonstrates aggregation and grouping with the Java Stream API using a CSV
 * sales dataset.
 */
public class SalesAnalysis {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) throws IOException {
        Path csvPath = args.length > 0 ? Path.of(args[0]) : Path.of("data", "sales.csv");
        List<SalesRecord> records = loadCsv(csvPath);

        System.out.println("Loaded " + records.size() + " sales rows from " + csvPath.toAbsolutePath());
        System.out.println();

        double totalRevenue = records.stream()
                .mapToDouble(SalesRecord::revenue)
                .sum();
        System.out.printf(Locale.US, "Total revenue: $%.2f%n%n", totalRevenue);

        System.out.println("Revenue by region:");
        revenueByRegion(records)
                .forEach((region, revenue) -> System.out.printf(Locale.US, "  %-5s -> $%.2f%n", region, revenue));
        System.out.println();

        System.out.println("Average unit price by product:");
        averageUnitPriceByProduct(records)
                .forEach((product, average) -> System.out.printf(Locale.US, "  %-18s -> $%.2f%n", product, average));
        System.out.println();

        System.out.println("Units sold per sales rep (descending):");
        unitsSoldByRep(records).forEach((rep, units) -> System.out.printf("  %-12s -> %d units%n", rep, units));
        System.out.println();

        mostProfitableProduct(records).ifPresent(product -> System.out.printf("Top product by revenue: %s ($%.2f)%n%n",
                product.name(), product.revenue()));

        System.out.println("Monthly revenue:");
        monthlyRevenue(records)
                .forEach((month, revenue) -> System.out.printf(Locale.US, "  %s -> $%.2f%n", month, revenue));
    }

    private static List<SalesRecord> loadCsv(Path csvPath) throws IOException {
        try (var lines = Files.lines(csvPath)) {
            return lines.skip(1)
                    .map(SalesAnalysis::parseRecord)
                    .toList();
        }
    }

    private static SalesRecord parseRecord(String line) {
        String[] columns = line.split(",");
        if (columns.length != 7) {
            throw new IllegalArgumentException("Invalid CSV row: " + line);
        }

        int orderId = Integer.parseInt(columns[0]);
        LocalDate date = LocalDate.parse(columns[1], DATE_FORMAT);
        String region = columns[2];
        String salesRep = columns[3];
        String product = columns[4];
        int unitsSold = Integer.parseInt(columns[5]);
        double unitPrice = Double.parseDouble(columns[6]);

        return new SalesRecord(orderId, date, region, salesRep, product, unitsSold, unitPrice);
    }

    private static Map<String, Double> revenueByRegion(List<SalesRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::region,
                        Collectors.summingDouble(SalesRecord::revenue)));
    }

    private static Map<String, Double> averageUnitPriceByProduct(List<SalesRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::product,
                        Collectors.averagingDouble(SalesRecord::unitPrice)));
    }

    private static Map<String, Integer> unitsSoldByRep(List<SalesRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::salesRep,
                        Collectors.summingInt(SalesRecord::unitsSold)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        java.util.LinkedHashMap::new));
    }

    private static Optional<ProductRevenue> mostProfitableProduct(List<SalesRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        SalesRecord::product,
                        Collectors.summingDouble(SalesRecord::revenue)))
                .entrySet()
                .stream()
                .map(entry -> new ProductRevenue(entry.getKey(), entry.getValue()))
                .max(Comparator.comparingDouble(ProductRevenue::revenue));
    }

    private static Map<String, Double> monthlyRevenue(List<SalesRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.date().getYear() + "-" + String.format("%02d", r.date().getMonthValue()),
                        Collectors.summingDouble(SalesRecord::revenue)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        java.util.LinkedHashMap::new));
    }

    private record SalesRecord(int orderId, LocalDate date, String region, String salesRep, String product,
            int unitsSold, double unitPrice) {
        double revenue() {
            return unitsSold * unitPrice;
        }
    }

    private record ProductRevenue(String name, double revenue) {
    }
}