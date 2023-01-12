package com.qqs.mapreduce.wordcount;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final Text outKey = new Text();
    private final IntWritable outValue = new IntWritable(1);

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        // 1、获取一行文本数据并转成String类型
        String line = value.toString();

        // 2、根据空格将每一行切分成一个个独立的单词
        String[] words = line.split(" ");

        // 3、将单词封装成<单词，1>的形式输出
        for (String word : words) {
            outKey.set(word);
            context.write(outKey, outValue);
        }
    }
}
