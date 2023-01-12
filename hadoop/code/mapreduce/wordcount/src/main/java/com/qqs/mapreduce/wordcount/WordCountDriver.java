package com.qqs.mapreduce.wordcount;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class WordCountDriver {
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        // 1. 获取配置信息，获取Job对象实例
        Configuration config = new Configuration();
        Job job = Job.getInstance(config);

        // 2. 指定当前程序的Jar包所在的本地路径
        job.setJarByClass(WordCountDriver.class);

        // 3. 关联Mapper、Reducer业务类
        job.setMapperClass(WordCountMapper.class);
        job.setReducerClass(WordCountReducer.class);

        // 4. 指定Mapper的输出数据的类型（KV的类型）
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        // 5. 指定最终输出的数据类型（KV的类型）
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // 6. 指定Job的输入源文件所在的目录
        FileInputFormat.setInputPaths(job, new Path(args[0]));

        // 7. 指定Job最终输出结果的目录
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // 8. 提交作业
        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);
    }
}
