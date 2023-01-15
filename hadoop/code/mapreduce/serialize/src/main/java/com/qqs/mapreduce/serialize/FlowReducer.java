package com.qqs.mapreduce.serialize;

import com.qqs.mapreduce.serialize.writable.FlowBean;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class FlowReducer extends Reducer<Text, FlowBean, Text, FlowBean> {
    private final FlowBean outValue = new FlowBean();
    @Override
    protected void reduce(Text key, Iterable<FlowBean> values, Reducer<Text, FlowBean, Text, FlowBean>.Context context) throws IOException, InterruptedException {
        long sumUp = 0L;
        long sumDown = 0L;
        for (FlowBean value : values) {
            sumUp += value.getUpFlow();
            sumDown += value.getDownFlow();
        }
        outValue.setUpFlow(sumUp);
        outValue.setDownFlow(sumDown);
        outValue.setSumFlow();
        context.write(key, outValue);
    }
}
