package com.akanksha.fraud.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Batch analytics over newline-delimited transaction JSON.
 * Input format: one JSON object per line, containing accountId and amount fields.
 * This intentionally uses a lightweight field extractor for the demo; use Jackson
 * with a custom InputFormat/RecordReader for escaped or arbitrary JSON in production.
 */
public final class TransactionAggregationJob {
    public static class AccountMapper extends Mapper<LongWritable, Text, Text, Text> {
        @Override protected void map(LongWritable key, Text line, Context context) throws IOException, InterruptedException {
            String json = line.toString();
            String account = field(json, "accountId");
            String amount = field(json, "amount");
            if (account == null || amount == null) {
                context.getCounter("FRAUD_BATCH", "INVALID_LINES").increment(1);
                return;
            }
            try {
                new BigDecimal(amount);
                context.write(new Text(account), new Text(amount));
            } catch (NumberFormatException e) {
                context.getCounter("FRAUD_BATCH", "INVALID_AMOUNTS").increment(1);
            }
        }
        private String field(String json, String name) {
            String key = "\"" + name + "\"";
            int at = json.indexOf(key);
            if (at < 0) return null;
            int colon = json.indexOf(':', at + key.length());
            if (colon < 0) return null;
            int start = colon + 1;
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
            if (start >= json.length()) return null;
            if (json.charAt(start) == '"') {
                int end = json.indexOf('"', start + 1);
                return end < 0 ? null : json.substring(start + 1, end);
            }
            int end = start;
            while (end < json.length() && ",}".indexOf(json.charAt(end)) < 0) end++;
            return json.substring(start, end).trim();
        }
    }

    public static class AccountReducer extends Reducer<Text, Text, Text, Text> {
        @Override protected void reduce(Text account, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            long count = 0;
            BigDecimal total = BigDecimal.ZERO;
            BigDecimal max = BigDecimal.ZERO;
            for (Text value : values) {
                BigDecimal amount = new BigDecimal(value.toString());
                count++;
                total = total.add(amount);
                if (amount.compareTo(max) > 0) max = amount;
            }
            context.write(account, new Text("transactionCount=" + count + "\ttotalAmount=" + total
                    + "\tmaxAmount=" + max));
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: TransactionAggregationJob <input-path> <output-path>");
            System.exit(2);
        }
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "transaction-account-aggregation");
        job.setJarByClass(TransactionAggregationJob.class);
        job.setMapperClass(AccountMapper.class);
        job.setReducerClass(AccountReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}