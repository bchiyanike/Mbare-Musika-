package com.lionico.mbaremusika;

import android.content.Context;
import android.widget.Toast;
import java.util.*;
import java.util.regex.*;

public class CommodityParser {

    public void parse(String html, List<Commodity> outList, Map<String, PriceHistory> outHist, Context ctx) {
        Pattern p = Pattern.compile(
            "<tr[^>]*><td[^>]*>([^<]+)</td>\\s*<td[^>]*>([^<]+)</td>\\s*<td[^>]*>([^<]+)</td>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );

        Matcher m = p.matcher(html);
        outList.clear();
        int count = 0;

        while (m.find()) {
            String name = clean(m.group(1));
            String quantity = clean(m.group(2));
            String usdPrice = clean(m.group(3));

            if (name.isEmpty() || name.equalsIgnoreCase("item")) continue;

            Commodity c = new Commodity(name, quantity, usdPrice);
            outList.add(c);
            updateHistory(c, outHist);
            count++;
        }

        Toast.makeText(ctx, "Updated " + count + " items", Toast.LENGTH_SHORT).show();
    }

    public static List<Commodity> parseJson(String raw) {
        List<Commodity> list = new ArrayList<Commodity>();
        String[] items = raw.split("\\|ITEM\\|");
        for (String item : items) {
            String[] parts = item.split("\\|FIELD\\|");
            if (parts.length >= 3) {
                list.add(new Commodity(parts[0].trim(), parts[1].trim(), parts[2].trim()));
            }
        }
        return list;
    }

    public static String toJson(List<Commodity> list) {
        StringBuilder sb = new StringBuilder();
        for (Commodity c : list) {
            if (sb.length() > 0) sb.append("|ITEM|");
            sb.append(c.getName()).append("|FIELD|")
				.append(c.getQuantity()).append("|FIELD|")
				.append(c.getUsdPrice());
        }
        return sb.toString();
    }

    public static PriceHistory parseHistory(String raw) {
        try {
            String[] parts = raw.split("\\|HIST\\|");
            if (parts.length < 1) return null;
            PriceHistory h = new PriceHistory(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                String[] pair = parts[i].split("\\|REC\\|");
                if (pair.length == 2) {
                    double price = Double.parseDouble(pair[0]);
                    long time = Long.parseLong(pair[1]);
                    h.addRecord(price, time);
                }
            }
            return h;
        } catch (Exception e) {
            return null;
        }
    }

    public static String historyToString(PriceHistory h) {
        if (h == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(h.getName());
        for (PriceHistory.PriceRecord r : h.getRecords()) {
            sb.append("|HIST|").append(r.getPrice()).append("|REC|").append(r.getTimestamp());
        }
        return sb.toString();
    }

    private void updateHistory(Commodity c, Map<String, PriceHistory> map) {
        try {
            double price = Double.parseDouble(c.getUsdPrice().replaceAll("[^\\d.]", ""));
            PriceHistory h = map.get(c.getName());
            if (h == null) {
                h = new PriceHistory(c.getName());
                map.put(c.getName(), h);
            }
            h.addRecord(price, System.currentTimeMillis());
        } catch (Exception ignored) {}
    }

    private String clean(String text) {
        return text == null ? "" : text.replace("&nbsp;", " ").replaceAll("\\s+", " ").trim();
    }
}
