package com.lionico.mbaremusika;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PriceHistory {
    private String commodityName;
    private List<PriceRecord> records;

    public PriceHistory() {
        this("");
    }

    public PriceHistory(String commodityName) {
        this.commodityName = commodityName != null ? commodityName : "";
        this.records = new ArrayList<>();
    }

    public void addRecord(double price, long timestamp) {
        records.add(new PriceRecord(price, timestamp));
    }

    public void addRecord(double price) {
        records.add(new PriceRecord(price));
    }

    public String getName() {
        return commodityName;
    }

    public List<PriceRecord> getRecords() {
        return records;
    }

    public PriceRecord getLatestRecord() {
        if (records.isEmpty()) return null;
        return records.get(records.size() - 1);
    }

    public boolean hasPriceDrop() {
        if (records.size() < 2) return false;
        double latest = records.get(records.size() - 1).getPrice();
        double previous = records.get(records.size() - 2).getPrice();
        return latest < previous;
    }

    public static class PriceRecord {
        private double price;
        private long timestamp;

        public PriceRecord(double price) {
            this(price, System.currentTimeMillis());
        }

        public PriceRecord(double price, long timestamp) {
            this.price = price;
            this.timestamp = timestamp;
        }

        public double getPrice() {
            return price;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getFormattedDate() {
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date(timestamp));
        }

        public String getFormattedDateTime() {
            return new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                .format(new Date(timestamp));
        }
    }
}package com.lionico.mbaremusika;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PriceHistory {
    private String commodityName;
    private List<PriceRecord> records;

    public PriceHistory() {
        this("");
    }

    public PriceHistory(String commodityName) {
        this.commodityName = commodityName != null ? commodityName : "";
        this.records = new ArrayList<>();
    }

    public void addRecord(double price, long timestamp) {
        records.add(new PriceRecord(price, timestamp));
    }

    public void addRecord(double price) {
        records.add(new PriceRecord(price));
    }

    public String getName() {
        return commodityName;
    }

    public List<PriceRecord> getRecords() {
        return records;
    }

    public PriceRecord getLatestRecord() {
        if (records.isEmpty()) return null;
        return records.get(records.size() - 1);
    }

    public boolean hasPriceDrop() {
        if (records.size() < 2) return false;
        double latest = records.get(records.size() - 1).getPrice();
        double previous = records.get(records.size() - 2).getPrice();
        return latest < previous;
    }

    public static class PriceRecord {
        private double price;
        private long timestamp;

        public PriceRecord(double price) {
            this(price, System.currentTimeMillis());
        }

        public PriceRecord(double price, long timestamp) {
            this.price = price;
            this.timestamp = timestamp;
        }

        public double getPrice() {
            return price;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getFormattedDate() {
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date(timestamp));
        }

        public String getFormattedDateTime() {
            return new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                .format(new Date(timestamp));
        }
    }
}
