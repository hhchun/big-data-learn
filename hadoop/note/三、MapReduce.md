- [1、MapReduce概述](#1mapreduce概述)
  - [1.1、MapReduce定义](#11mapreduce定义)
  - [1.2、MapReduce优缺点](#12mapreduce优缺点)
  - [1.3、MapReduce核心思想](#13mapreduce核心思想)
  - [1.4、MapReduce进程](#14mapreduce进程)
- [2、MapReduce入门](#2mapreduce入门)
  - [2.1、常用数据序列化类型](#21常用数据序列化类型)
  - [2.2、MapReduce编程规范](#22mapreduce编程规范)
  - [2.3、WordCount案例实操](#23wordcount案例实操)
- [3、Hadoop序列化](#3hadoop序列化)
  - [3.1、序列化概述](#31序列化概述)
  - [3.2、自定义Bean使用Hadoop序列化的步骤](#32自定义bean使用hadoop序列化的步骤)
  - [3.3、Hadoop序列化案例](#33hadoop序列化案例)
- [4、MapReduce核心原理](#4mapreduce核心原理)
  - [4.1、数据输入：InputFormat](#41数据输入inputformat)
    - [4.1.1、切片与MapTask并行度决定机制](#411切片与maptask并行度决定机制)
    - [4.1.2、Job提交流程源码和切片源码分析](#412job提交流程源码和切片源码分析)
    - [4.1.3、FileInputFormat切片源码分析](#413fileinputformat切片源码分析)
    - [4.1.4、FileInputFormat切片大小的参数配置](#414fileinputformat切片大小的参数配置)
  - [4.2、MapReduce工作流程](#42mapreduce工作流程)
  - [4.3、Shuffle机制](#43shuffle机制)
  - [4.4、数据输出：OutputFormat](#44数据输出outputformat)
  - [4.5、MapReduce源码解析](#45mapreduce源码解析)
    - [4.5.1、MapTask工作机制](#451maptask工作机制)
    - [4.5.2、ReduceTask工作机制](#452reducetask工作机制)
    - [4.5.3、ReduceTask并行度决定机制](#453reducetask并行度决定机制)
    - [4.5.4、MapTask \& ReduceTask源码解析](#454maptask--reducetask源码解析)
  - [4.6、Join应用](#46join应用)
  - [4.7、数据清洗（ETL）](#47数据清洗etl)
  - [4.8、MapReduce开发总结](#48mapreduce开发总结)
- [5、Hadoop数据压缩](#5hadoop数据压缩)
  - [5.1、概述](#51概述)
  - [5.2、MapReduce支持的压缩编码](#52mapreduce支持的压缩编码)
  - [5.3、压缩方式选择](#53压缩方式选择)
  - [5.4、压缩参数配置](#54压缩参数配置)
  - [5.5、压缩实操案例](#55压缩实操案例)
- [6、常见错误及解决方案](#6常见错误及解决方案)

# 1、MapReduce概述

## 1.1、MapReduce定义

* MapReduce是一个分布式运算程序的编程框架。
* MapReduce核心功能是将**用户编写的业务逻辑代码**和**自带默认组件**整合成一个完整的分布式运算程序，并发运行在一个Hadoop集群上。

## 1.2、MapReduce优缺点

* 优点
  * 易于编程：简单的实现一些接口，就可以完成一个分布式程序。
  * 良好的扩展性：当计算资源不能得到满足的时候，可以通过简单的增加机器来扩展它的计算能力。
  * 高容错性：MapReduce设计的初衷就是使程序能够部署在廉价的机器上，这就要求它具有很高的容错性。比如**其中一台机器挂了，它可以把上面的计算任务转移到另外一个节点上运行，不至于这个任务运行失败**，而且这个过程不需要人工参与，而完全是由Hadoop内部完成的。
  * 适合PB级以上海量数据的离线处理：以实现上千台服务器集群并发工作，提供数据处理能力。
* 缺点
  * 不擅长实时计算：无法在毫秒或者秒级内返回结果。
  * 不擅长流式计算：流式计算的输入数据是动态的，而MapReduce的输入数据集是静态的，不能动态变化。这是因为MapReduce自身的设计特点决定了数据源必须是静态的。
  * 不擅长DAG（有向无环图）计算：多个应用程序存在依赖关系，后一个应用程序的输入为前一个的输出。在这种情况下，MapReduce并不是不能做，而是使用后，每个MapReduce作业的输出结果都会写入到磁盘，会造成大量的磁盘IO，导致性能非常的低下。

## 1.3、MapReduce核心思想

![MapReduce核心思想](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/MapReduce%E6%A0%B8%E5%BF%83%E6%80%9D%E6%83%B3.svg)

1. 分布式的运算程序往往需要分成至少2个阶段：
   1. 第一个阶段的MapTask并发实例，完全并行运行，互不相干。
   2. 第二个阶段的ReduceTask并发实例互不相干，但是数据依赖于上一个阶段。
2. MapReduce编程模型只能包含一个Map阶段和一个Reduce阶段，如果业务逻辑非常复杂，那就只能多个MapReduce程序，串行运行。

3. 总结：分析WordCount数据流走向深入理解MapReduce核心思想。

## 1.4、MapReduce进程

一个完整的MapReduce程序在分布式运行时有三类实例进程：

1. **MrAppMaster**：负责整个程序的过程调度及状态协调。
2. **MapTask**：负责Map阶段的整个数据处理流程。
3. **ReduceTask**：负责Reduce阶段的整个数据处理流程。

# 2、MapReduce入门

## 2.1、常用数据序列化类型

| Java类型 | Hadoop Writable类型 |
| -------- | ------------------- |
| Boolean  | BooleanWritable     |
| Byte     | ByteWritable        |
| Int      | IntWritable         |
| Float    | FloatWritable       |
| Long     | LongWritable        |
| Double   | DoubleWritable      |
| String   | Text                |
| Map      | MapWritable         |
| Array    | ArrayWritable       |
| Null     | NullWritable        |

## 2.2、MapReduce编程规范

MapReduce的程序分成三部分：

1. Mapper：
   1. 自定义Mapper类继承Mapper父类。
   2. Mapper的输入数据是KV对的形式（KV的类型可自定义）。
   3. Mapper中的业务逻辑代码写在map方法中。
   4. Mapper的输出类型是KV对的形式（KV的类型可自定义）。
   5. **Maptask对每一个KV都调用一次map方法。**
2. Reducer：
   1. 自定义Mapper类继承Reducer父类。
   2. Reducer的输入类型对应Mapper的输出类型（kV对的形式）。
   3. Reducer的业务逻辑代码写在reduce方法中。
   4. **ReduceTask对每一组相同K的KV组调用一次reduce方法。**
3. Driver：
   1. 相当于YARN集群的客户端，用于提交程序到YARN集群，提交的是封装了MapReduce程序相关运行参数的Job。

## 2.3、WordCount案例实操

1. 需求：给定文本文件，统计文件中每个单词出现的总次数。

2. 文件：[wordcount.txt](../input/mapreduce/wordcount/wordcount.txt)

3. 需求分析：

   1. 根据MapReduce编程规范，分别编写Mapper、reducer、Driver。

   2. Mapper：

      1. 从文件中一行一行的读取文本数据转成String类型。
      2. 根据空格将每一行切分成一个个独立的单词。
      3. 将单词封装成<单词，1>的形式输出。

   3. Reducer：

      1. 根据K进行单词出现次数的汇总。
      2. 输出当前K汇总后的总次数。

   4. Driver：

      1. 获取配置信息，获取Job对象实例。
      2. 指定当前程序的Jar包所在的本地路径。
      3. 关联Mapper、Reducer业务类。
      4. 指定Mapper的输出数据的类型（KV的类型）。
      5. 指定最终输出的数据类型（KV的类型）。
      6. 指定Job的输入源文件所在的目录。
      7. 指定Job最终输出结果的目录（目录不能已存在）。
      8. 提交作业。

   5. 环境准备：

      1. 创建maven工程，添加依赖。

         ```xml
         <dependencies>
             <dependency>
                 <groupId>org.apache.hadoop</groupId>
                 <artifactId>hadoop-client</artifactId>
                 <version>3.1.3</version>
             </dependency>
             <dependency>
                 <groupId>junit</groupId>
                 <artifactId>junit</artifactId>
                 <version>4.12</version>
             </dependency>
             <dependency>
                 <groupId>org.slf4j</groupId>
                 <artifactId>slf4j-log4j12</artifactId>
                 <version>1.7.30</version>
             </dependency>
         </dependencies>
         ```

      2. 配置日志文件，在项目resources目录下创建名为：log4j.properties的文件，添加如下配置内容。

         ```properties
         log4j.rootLogger=INFO, stdout
         log4j.appender.stdout=org.apache.log4j.ConsoleAppender
         log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
         log4j.appender.stdout.layout.ConversionPattern=%d %p [%c] - %m%n
         log4j.appender.logfile=org.apache.log4j.FileAppender
         log4j.appender.logfile.File=target/spring.log
         log4j.appender.logfile.layout=org.apache.log4j.PatternLayout
         log4j.appender.logfile.layout.ConversionPattern=%d %p [%c] - %m%n
         ```

   6. 编写Mapper业务类

      ```java
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
      ```

   7. 编写Reducer业务类

      ```java
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
      ```

   8. 编写Driver类

      ```java
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
      
              // 7. 指定Job最终输出结果的目录（目录不能已存在）
              FileOutputFormat.setOutputPath(job, new Path(args[1]));
      
              // 8. 提交作业
              boolean result = job.waitForCompletion(true);
              System.exit(result ? 0 : 1);
          }
      }
      ```

   9. 本地测试

      1. 本地windows本地运行需要配置环境，具体的配置步骤参考：[环境配置](./二、HDFS.md#31客户端环境准备)

      2. 在idea工具中设置运行参数，参数分别是输入源文件所在的目录、最终输出结果的目录（目录不能已存在），如下图：

         ![image-20230112232453247](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/image-20230112232453247.png)

         > 如果不想或不会指定则可以在代码中写死输入、输出路径。
         >
         > ```java
         > // 6. 指定Job的输入源文件所在的目录
         > FileInputFormat.setInputPaths(job, new Path("输入路径"));
         > 
         > // 7. 指定Job最终输出结果的目录（目录不能已存在）
         > FileOutputFormat.setOutputPath(job, new Path("输出路径"));
         > ```

   10. 提交到集群上测试

       1. 添加打包插件

          ```xml
          <build>
              <plugins>
                  <plugin>
                      <artifactId>maven-compiler-plugin</artifactId>
                      <version>3.6.1</version>
                      <configuration>
                          <source>1.8</source>
                          <target>1.8</target>
                      </configuration>
                  </plugin>
                  <plugin>
                      <artifactId>maven-assembly-plugin</artifactId>
                      <configuration>
                          <descriptorRefs>
                              <descriptorRef>jar-with-dependencies</descriptorRef>
                          </descriptorRefs>
                      </configuration>
                      <executions>
                          <execution>
                              <id>make-assembly</id>
                              <phase>package</phase>
                              <goals>
                                  <goal>single</goal>
                              </goals>
                          </execution>
                      </executions>
                  </plugin>
              </plugins>
          </build>
          ```

          > 注意：记得maven刷新（reimport）。

       2. 使用maven进行打包

          ![image-20230112233220696](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/image-20230112233220696.png)

       3. 将不带依赖的jar重命名为wc.jar上传到hadoo集群的目录下（$HADOOP_HOME）。
       
       4. 启动hadoop集群，启动的具体步骤参考：[hadoop环境安装](./一、环境安装.md)。
       
       5. 启动wordcount程序
       
          ```shell
          hadoop jar wc.jar com.qqs.mapreduce.wordcount.WordCountDriver /input/wordcount /output/wordcount
          ```
       
          > hadoop jar：启动程序的核心命令。
          >
          > com.qqs.mapreduce.wordcount.WordCountDriver：Driver全类名。
          >
          > /input/wordcount/ ：输入路径（hdfs上的路径），需要提前上传好测试数据文件。
          >
          > /output/wordcount/ ：输出路径（hdfs上的路径），必须是未存在的目录路径。

# 3、Hadoop序列化

## 3.1、序列化概述

* 什么事序列化？
  * **序列化**就是把**内存中的对象，转换成字节序列**（或其他数据传输协议）以便于存储到磁盘（持久化）和网络传输。
  * **反序列化**就是将收到字节序列（或其他数据传输协议）或者是**磁盘的持久化数据，转换成内存中的对象**。
* 为什么要序列化？
  * 对象是存储在内存中，只能由本地进程进行使用，无法通过网络传输到其他服务器上。然而序列化可以将存储在内存中的对象通过网络传输到其他服务器，提供给其他服务器上的进程进行使用。
* 为什么不使用Java的序列化？
  * Java的序列化是一个重量级序列化框架（Serializable），一个对象被序列化后，会附带很多额外的信息（各种校验信息，Header，继承体系等），不便于在网络中高效传输。所以，Hadoop自己开发了一套序列化机制（Writable）。
* Hadoop序列化特点：
  * **紧凑** ：高效使用存储空间。
  * **快速**：读写数据的额外开销小。
  * **互操作**：支持多语言的交互。

## 3.2、自定义Bean使用Hadoop序列化的步骤

1. 必须实现Writable接口。
2. 必须有空参构造，反序列化时，需要反射调用空参构造函数。
3. 重写序列化write方法。
4. 重写反序列化readFields方法。
5. 注意反序列化的顺序和序列化的顺序完全一致。
6. 如果需要将结果显示在文件中，需要重写toString()方法。
7. 如果需要将自定义的bean放在key中传输，则还需要实现Comparable接口，因为MapReduce中的Shuffle过程要求对key必须能排序。

## 3.3、Hadoop序列化案例

* code：[hadoop序列化案例代码](../code/mapreduce/serialize)
* 案例数据：[案例数据](../input/mapreduce/serialize/phone.txt)
* 注意：代码中的输入路径和输出路径的都是使用参数传入的方式指定的。如果不会指定请参考：[WordCount案例实操](#23wordcount案例实操) 的第9点。

# 4、MapReduce核心原理

## 4.1、数据输入：InputFormat

### 4.1.1、切片与MapTask并行度决定机制

1. 问题引出

   1. MapTask的并行度决定Map阶段的任务处理并发度，进而影响到整个Job的处理速度。
   2. 思考：1G的数据，启动8个MapTask，可以提高集群的并发处理能力。那么1K的数据，也启动8个MapTask，会提高集群性能吗？MapTask并行任务是否越多越好呢？哪些因素影响了MapTask并行度？

2. MapTask并行度决定机制

   > 了解MapTask并行度决定机制之前先了解数据块和数据切片的概念。
   >
   > * **数据块：**HDFS物理存储数据的单位。
   > * **数据切片：**逻辑上对输入数据进行分片，并不会在磁盘上将其切分成片存储。**数据切片是MapReduce输入数据的单位**，一个切片会对应启动一个MapTask。

   1. Map阶段的并行度由客户端在提交Job时的切片数决定的。
   2. 每个切片分配一个MapTask实例来处理。
   3. 切片默认大小=BlockSize（数据块大小）。
   4. 在进行切片时是单独针对每个文件进行切片的，而不是针对整个数据集整体。

### 4.1.2、Job提交流程源码和切片源码分析

> 注意：这里只是简单大致的对源码进行分析，不会探讨太多的细节。

1. Job提交流程源码分析：

   1. Driver中使用**waitForCompletion**触发提交Job。

   2. 在waitForCompletion中在调用**submit**方法处理提交Job。

   3. 在submit方法中调用**connect**方法建立连接。

      ```java
      private synchronized void connect()
          throws IOException, InterruptedException, ClassNotFoundException {
          if (cluster == null) {
              cluster = 
                  ugi.doAs(new PrivilegedExceptionAction<Cluster>() {
                      public Cluster run()
                          throws IOException, InterruptedException, 
                      ClassNotFoundException {
                          // 创建提交Job的代理,调用Cluster构造器创建
                          return new Cluster(getConfiguration());
                      }
                  });
          }
      }
      
      // Cluster构造器
      public Cluster(InetSocketAddress jobTrackAddr, Configuration conf) 
          throws IOException {
          this.conf = conf;
          this.ugi = UserGroupInformation.getCurrentUser();
          // 判断是本地运行环境还是yarn集群运行环境
          initialize(jobTrackAddr, conf);
      }
      ```

   4. 在submit方法中先创建一个**JobSubmitter**（提交工作者），由它来提交Job。

      ```java
      public void submit() 
          throws IOException, InterruptedException, ClassNotFoundException {
          // ...
          connect();
          // 创建提交工作者
          final JobSubmitter submitter = 
              getJobSubmitter(cluster.getFileSystem(), cluster.getClient());
          
          status = ugi.doAs(new PrivilegedExceptionAction<JobStatus>() {
              public JobStatus run() throws IOException, InterruptedException, 
              ClassNotFoundException {
                  // 提交job
                  return submitter.submitJobInternal(Job.this, cluster);
              }
          });
      }
      ```

   5. 提交Job

      ```java
      JobStatus submitJobInternal(Job job, Cluster cluster) 
          throws ClassNotFoundException, InterruptedException, IOException {
          // 创建给集群提交数据的Stag路径
          Path jobStagingArea = JobSubmissionFiles.getStagingDir(cluster, conf);
          
          // 获取jobid ，并创建Job路径
          JobID jobId = submitClient.getNewJobID();
          copyAndConfigureFiles(job, submitJobDir); 
          // copyAndConfigureFiles方法内调用
          rUploader.uploadResources(job, jobSubmitDir);
          
          // 计算切片，生成切片规划文件
         	int maps = writeSplits(job, submitJobDir);
          // writeSplits方法内调用
          maps = writeNewSplits(job, jobSubmitDir);
          // writeNewSplits方法内调用
          List<InputSplit> splits = input.getSplits(job);
          
          // 向Stag路径写XML配置文件
          writeConf(conf, submitJobFile);
          // writeConf方法内调用
          conf.writeXml(out);
          
          // 提交Job,返回提交状态
          status = submitClient.submitJob(
              jobId, submitJobDir.toString(), job.getCredentials());
          
      }
      ```

### 4.1.3、FileInputFormat切片源码分析

> 在MapReduce中有很多InputFormat，这里就简单的拿FileInputFormat来进行分析，其他的都大同小异，思路都差不多。

```java
// 废话少说,直接上源码

// 源码位置：org.apache.hadoop.mapreduce.lib.input.FileInputFormat#getSplits
public List<InputSplit> getSplits(JobContext job) throws IOException {
    // 1、尝试从配置文件中获取于切片大小相关的配置项
    
    // 尝试获取mapreduce.input.fileinputformat.split.minsize的值,
    // 最小值的默认值是1,因为getFormatMinSplitSize返回值是1
    long minSize = Math.max(getFormatMinSplitSize(), getMinSplitSize(job));
    
    // 获取mapreduce.input.fileinputformat.split.maxsize的值,
    // 默认值是 Long.MAX_VALUE
    long maxSize = getMaxSplitSize(job);

    List<InputSplit> splits = new ArrayList<InputSplit>();
    // 2、获取文件信息状态,挨个遍历
    List<FileStatus> files = listStatus(job);
    for (FileStatus file: files) {
        Path path = file.getPath();
        long length = file.getLen();
        if (length != 0) {
            
            // 2.1、获取当前文件所有块的信息
            BlockLocation[] blkLocations;
            if (file instanceof LocatedFileStatus) {
                blkLocations = ((LocatedFileStatus) file).getBlockLocations();
            } else {
             
                FileSystem fs = path.getFileSystem(job.getConfiguration());
                blkLocations = fs.getFileBlockLocations(file, 0, length);
            }
            
            if (isSplitable(job, path)) {
                // 2.2、获取当前文件块大小
                long blockSize = file.getBlockSize();
                // 2.3、计算切片大小 return Math.max(minSize, Math.min(maxSize, blockSize));
                long splitSize = computeSplitSize(blockSize, minSize, maxSize);
                
                long bytesRemaining = length;
                // 2.4、对剩余数量进行切片,每次切片都要判断剩余数量是否大于splitSize的1.1倍
                while (((double) bytesRemaining)/splitSize > SPLIT_SLOP) {
                    // 2.4.1、通过偏移量（offset=length-bytesRemaining）定位块的索引
                    int blkIndex = getBlockIndex(blkLocations, length-bytesRemaining);
                    // 2.4.2、记录切片的元数据信息，比如初始位置、大小、所在节点列表等。
                    splits.add(makeSplit(path, length-bytesRemaining, splitSize,
                                         blkLocations[blkIndex].getHosts(),
                                         blkLocations[blkIndex].getCachedHosts()));
                    // 2.4.3、记录切片后扣减剩余数量
                    bytesRemaining -= splitSize;
                }
                
                // 2.5、如果剩余的大小不足splitSize的1.1倍则单独作为一块切片
                if (bytesRemaining != 0) {
                    int blkIndex = getBlockIndex(blkLocations, length-bytesRemaining);
                    splits.add(makeSplit(path, length-bytesRemaining, bytesRemaining,
                                         blkLocations[blkIndex].getHosts(),
                                         blkLocations[blkIndex].getCachedHosts()));
                }
                
            }
        }
    }
    return splits;
}

// 3、将切片规划文件提交到YARN,YARN上的MrAppMaster根据切片规划文件计算开启MapTask的个数。
```

### 4.1.4、FileInputFormat切片大小的参数配置

* mapreduce.input.fileinputformat.split.maxsize：切片最大值，如果值比BlockSize小，则切片会变小并且值等于该参数的值。
* mapreduce.input.fileinputformat.split.minsize：切片最小值，如果值比BlockSize大，则切片会变大并且值等于该参数的值。
* 如果maxsize比BlockSize小并且minsize比BlockSize大，则最终生效的是minsize，因为源码中是这样判断的`Math.max(minSize, Math.min(maxSize, blockSize))`，具体细节看源码。

## 4.2、MapReduce工作流程

## 4.3、Shuffle机制

## 4.4、数据输出：OutputFormat

## 4.5、MapReduce源码解析

### 4.5.1、MapTask工作机制

### 4.5.2、ReduceTask工作机制

### 4.5.3、ReduceTask并行度决定机制

### 4.5.4、MapTask & ReduceTask源码解析

## 4.6、Join应用

## 4.7、数据清洗（ETL）

## 4.8、MapReduce开发总结

# 5、Hadoop数据压缩

## 5.1、概述

## 5.2、MapReduce支持的压缩编码

## 5.3、压缩方式选择

## 5.4、压缩参数配置

## 5.5、压缩实操案例

# 6、常见错误及解决方案

