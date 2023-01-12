package com.qqs.mapreduce.wordcount;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    private final IntWritable outValue = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        int sum = 0;
        // 1.根据K进行单词出现次数的汇总。
        for (IntWritable value : values) {
            sum += value.get();
        }
        // 2.输出当前K汇总后的总次数
        outValue.set(sum);
        context.write(key, outValue);
    }
}
