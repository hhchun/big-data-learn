package com.qqs.mapreduce.serialize;

import com.qqs.mapreduce.serialize.writable.FlowBean;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class FlowMapper extends Mapper<LongWritable, Text, Text, FlowBean> {
    private final Text outKey = new Text();
    private final FlowBean outValue = new FlowBean();

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, FlowBean>.Context context) throws IOException, InterruptedException {
        String line = value.toString();
        String[] fields = line.split("\t");
        String phone = fields[1];
        String up = fields[fields.length -3];
        String down = fields[fields.length - 2];

        outKey.set(phone);
        outValue.setUpFlow(Long.parseLong(up));
        outValue.setDownFlow(Long.parseLong(down));
        outValue.setSumFlow();
        context.write(outKey, outValue);
    }
}
