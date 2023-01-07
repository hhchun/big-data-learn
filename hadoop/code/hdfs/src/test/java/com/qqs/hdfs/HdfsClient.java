package com.qqs.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.junit.Test;

import java.net.URI;
import java.util.Arrays;


public class HdfsClient {

    public static final String DEV_INPUT = "D:/learn/大数据/big-data/hadoop/input/";
    public static final String DEV_OUTPUT = "D:/learn/大数据/big-data/hadoop/output/";

    @Test
    public void mkdir() throws Exception {
        Configuration config = new Configuration();
        FileSystem fs = FileSystem.get(new URI("hdfs://hp-node1:8020"), config, "hp");
        fs.mkdirs(new Path("/xiyou"));
        fs.close();
    }

    @Test
    public void copyFromLocalFile() throws Exception {
        Configuration config = new Configuration();
        config.set("dfs.replication", "2");
        FileSystem fs = FileSystem.get(new URI("hdfs://hp-node1:8020"), config, "hp");
        fs.copyFromLocalFile(new Path(DEV_INPUT + "sunwukong.txt"), new Path("/xiyou"));
        fs.close();
    }


    @Test
    public void copyToLocalFile() throws Exception {
        Configuration config = new Configuration();
        FileSystem fs = FileSystem.get(new URI("hdfs://hp-node1:8020"), config, "hp");
        fs.copyToLocalFile(new Path("/xiyou/sunwukong.txt"), new Path(DEV_OUTPUT + "sunwukong.txt"));
        fs.close();
    }

    @Test
    public void delete() throws Exception {
        Configuration config = new Configuration();
        FileSystem fs = FileSystem.get(new URI("hdfs://hp-node1:8020"), config, "hp");
        fs.delete(new Path("/xiyou"), true);
        fs.close();
    }

    @Test
    public void listFileInfo() throws Exception {
        Configuration config = new Configuration();
        FileSystem fs = FileSystem.get(new URI("hdfs://hp-node1:8020"), config, "hp");
        // 文件详情
        RemoteIterator<LocatedFileStatus> files = fs.listFiles(new Path("/"), true);
        while (files.hasNext()) {
            LocatedFileStatus fileStatus = files.next();
            System.out.println("======" + fileStatus.getPath() + "======");
            // 权限
            System.out.println(fileStatus.getPermission());
            // 所有者
            System.out.println(fileStatus.getOwner());
            // 组
            System.out.println(fileStatus.getGroup());
            // 文件大小
            System.out.println(fileStatus.getLen());
            // 最后一次修改时间
            System.out.println(fileStatus.getModificationTime());
            // 副本数
            System.out.println(fileStatus.getReplication());
            // 块大小
            System.out.println(fileStatus.getBlockSize());
            // 文件名
            System.out.println(fileStatus.getPath().getName());
            // 块信息
            BlockLocation[] blockLocations = fileStatus.getBlockLocations();
            System.out.println(Arrays.toString(blockLocations));
        }
        fs.close();
    }


    @Test
    public void fileJudge() throws Exception {
        Configuration config = new Configuration();
        FileSystem fs = FileSystem.get(new URI("hdfs://hp-node1:8020"), config, "hp");
        FileStatus[] fileStatuses = fs.listStatus(new Path("/"));
        for (FileStatus fileStatus : fileStatuses) {
            if (fileStatus.isFile()) {
                System.out.println("f:" + fileStatus.getPath().getName());
            }else {
                System.out.println("d:" + fileStatus.getPath().getName());
            }
        }
        fs.close();
    }


}
