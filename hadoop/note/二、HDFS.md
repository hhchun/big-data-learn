- [1、HDFS概述](#1hdfs概述)
  - [1.1、HDFS定义](#11hdfs定义)
  - [1.2、HDFS优点](#12hdfs优点)
  - [1.3、HDFS缺点](#13hdfs缺点)
  - [1.4、HDFS组成架构](#14hdfs组成架构)
  - [1.5、文件块大小](#15文件块大小)
- [2、HDFS命令操作](#2hdfs命令操作)
  - [2.1、基本语法](#21基本语法)
  - [2.2、常用大全](#22常用大全)
  - [2.3、上传](#23上传)
  - [2.4、下载](#24下载)
  - [2.5、操作HDFS](#25操作hdfs)
- [3、HDFS API操作](#3hdfs-api操作)
  - [3.1、客户端环境准备](#31客户端环境准备)
  - [3.2、上传（测试参数优先级）](#32上传测试参数优先级)
  - [3.3、下载](#33下载)
  - [3.4、删除文件和目录](#34删除文件和目录)
  - [3.5、文件详情查看](#35文件详情查看)
  - [3.6、文件和文件夹判断](#36文件和文件夹判断)
- [4、HDFS读写流程](#4hdfs读写流程)
  - [4.1、HDFS数据写流程](#41hdfs数据写流程)
  - [4.2、节点距离计算（网络拓扑）](#42节点距离计算网络拓扑)
  - [4.3、副本存储节点选择（机架感知）](#43副本存储节点选择机架感知)
  - [4.4、HDFS的数据读流程](#44hdfs的数据读流程)
- [5、NameNode](#5namenode)
  - [5.1、NN和2NN的工作机制](#51nn和2nn的工作机制)
  - [5.2、Fsimage和Edits解析](#52fsimage和edits解析)
  - [5.3、CheckPoint检查点](#53checkpoint检查点)
- [6、DataNode](#6datanode)
  - [6.1、DataNode的工作机制](#61datanode的工作机制)
  - [6.2、数据完整性](#62数据完整性)
  - [6.3、DataNode掉线时限参数设置](#63datanode掉线时限参数设置)

# 1、HDFS概述

## 1.1、HDFS定义

* HDFS（Hadoop Distributed File System），它是一个文件系统；其次，它是分布式的。
* HDFS的使用场景：适合一次写入，多次读出的场景。

## 1.2、HDFS优点

1. 高容错性

   ![](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/%E9%AB%98%E5%AE%B9%E9%94%99%E6%80%A7.svg)

2. 适合处理大数据

   1. 数据规模：GB、TB、甚至PB级别的数据。
   2. 文件规模：百万规模以上的文件数量。

3. 可构建在廉价机器上，通过多副本机制，提高可用性。

## 1.3、HDFS缺点

1. 不适合低延迟数据访问，比如毫秒级无法做到。

2. 无法高效的对大量小文件进行存储。

   * 存储大量小文件会占用NameNode大量的内存来存储文件目录和块信息。
   * 小文件存储的寻址时间会超过读取文件内容的时间，违背了HDFS的设计理念。

3. 不支持并发写入、文件随机修改。

   * 同时只能有一个文件写入，不能多线程多文件同时写入。

   * 只支持数据的追加，不支持文件的随机修改。

     ![不支持文件修改](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/%E4%B8%8D%E6%94%AF%E6%8C%81%E6%96%87%E4%BB%B6%E4%BF%AE%E6%94%B9.svg)

## 1.4、HDFS组成架构

![HDFS组成架构](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/HDFS%E7%BB%84%E6%88%90%E6%9E%B6%E6%9E%84.svg)

1. NameNode（nn）：Master，HDFS的管理者。
   1. 管理HDFS的名称空间。
   2. 配置副本策略。
   3. 管理数据块（Block）映射信息。
   4. 处理客户端读写请求。
2. DataNode：Slave，NameNode下达命令，DataNode执行实际的操作。
   1. 存储实际的数据块。
   2. 执行数据块的读/写操作。
3. Client：客户端。
   1. 文件切分；文件上传HDFS时，Client将文件切分成一个或多个Block，然后进行上传。
   2. 与NameNode交互，获取文件的位置信息。
   3. 与DataNode交互，读取或写入数据。
   4. 通过命令管理HDFS，比如NameNode格式化。
   5. Client通过命令访问HDFS，比如对HDFS增删改查操作等。
4. Secondary NameNode：并非NameNode的热备；与NameNode挂掉时，它并不能马上替换NameNode并提供服务。
   1. 辅助NameNode，分担其工作量，比如定期合并Fsimages和Edits，并推送给NameNode。
   2. 在紧急情况下，可辅助恢复NameNode。

## 1.5、文件块大小

* HDFS的文件在物理上是分块存储（Block），块的大小可以通过配置参数（dfis.blcksize）来设置，默认大小在Hadoop2.x/3.x版本中是128M，1.x版本中是64M。
* 如果寻址时间约为10ms，即查找目标Block的时间为10ms。
* 寻址时间为传输时间的1%时为最佳状态。因此，传输时间=10ms /0.01=1000ms=1s。
* 目前磁盘的传输速率普遍为100Mb/s。

* **思考：为什么块的大小不能设置太大或者太小。**

  * 如果块设置太小，会增加寻址时间。

  * 如果块设置太大，从磁盘传输数据的时间会明显大于定位这个块开始位置所需的时间（寻址时间）。导致在处理块数据时，会非常慢。
  * **总结：HDFS块的大小设置主要取决于磁盘传输速率**。

# 2、HDFS命令操作

## 2.1、基本语法

* hadoop fs 或 hdfs dfs 两个都是完成相同的。

## 2.2、常用大全

* 通过bin/hadoop fs命令查看所有命令。

| 序号 | 命令                    | 作用                                        |
| :--- | :---------------------- | :------------------------------------------ |
| 1    | -help                   | 查看命令帮助信息，如参数等                  |
| 2    | -ls                     | 显示目录信息                                |
| 3    | -mkdir                  | 在HDFS上创建目录                            |
| 4    | -moveFromLocal          | 从本地剪切粘贴到HDFS                        |
| 5    | -appendToFile           | 追加一个文件到已经存在的文件末尾            |
| 6    | -cat                    | 显示文件内容                                |
| 7    | -chgrp 、-chmod、-chown | Linux文件系统中的用法一样，修改文件所属权限 |
| 8    | -copyFromLocal          | 从本地文件系统中拷贝文件到HDFS路径去        |
| 9    | -copyToLocal            | 从HDFS拷贝到本地                            |
| 10   | -cp                     | 从HDFS的一个路径拷贝到HDFS的另一个路径      |
| 11   | -mv                     | 在HDFS目录中移动文件                        |
| 12   | -get                    | 等同于copyToLocal，就是从HDFS下载文件到本地 |
| 13   | -getmerge               | 合并下载多个文件                            |
| 14   | -put                    | 等同于copyFromLocal                         |
| 15   | -tail                   | 显示一个文件的末尾                          |
| 16   | -rm                     | 删除文件或文件夹                            |
| 17   | -rmdir                  | 删除空目录                                  |
| 18   | -du                     | 统计文件夹的大小信息                        |
| 19   | -setrep                 | 设置HDFS中文件的副本数量                    |
| 20   | - expunge               | 清空HDFS垃圾桶                              |

## 2.3、上传

* -moveFromLocal：从本地剪切粘贴到HDFS

  ```shell
  # 准备文件
  echo "shuguo" >> shuguo.txt
  # 剪切
  hadoop fs  -moveFromLocal  ./shuguo.txt  /sanguo
  ```

* -copyFromLocal：本地文件系统中拷贝文件到HDFS

  ```shell
  # 准备文件
  echo "weiguo" >> weiguo.txt
  # 拷贝
  hadoop fs  -copyFromLocal  ./weiguo.txt  /sanguo
  ```

* -put：等同于copyFromLocal，一般使用put

  ```shell
  # 准备文件
  echo "wuguo" >> wuguo.txt
  # put
  hadoop fs -put ./wuguo.txt /sanguo
  ```

* -appendToFile：追加一个文件到已存在文件末尾

  ```shell
  # 准备文件
  echo "liubei" >> liubei.txt
  # 追加
  hadoop fs -appendToFile liubei.txt /sanguo/shuguo.txt
  ```

## 2.4、下载

* -copyToLocal：从HDFS拷贝到本地

  ```shell
  hadoop fs -copyToLocal /sanguo/shuguo.txt ./
  ```

* -get：等同于copyToLocal，一般使用put

  ```shell
  hadoop fs -get /sanguo/shuguo.txt ./shuguo2.txt
  ```

## 2.5、操作HDFS

* -ls：查看目录信息

  ```shell
  hadoop fs -ls /sanguo
  ```

* -cat：查看文件内容

  ```shell
  hadoop fs -cat /sanguo/shuguo.txt
  ```

* -chgrp、-chmod、-chown：与linux文件系统中的用法一样，修改文件所属权限

  ```shell
  # 修改文件权限
  hadoop fs -chmod 666 /sanguo/shuguo.txt
  
  # 修改文件
  hadoop fs -chown qqs:qqs /sanguo/shuguo.txt
  ```

  > 只有超级用户和属于组的文件所有者才能变更文件关联组。非超级用户如需要设置关联组可能需要使用 [chgrp](https://www.runoob.com/linux/linux-comm-chgrp.html) 命令。

* -mkdir：创建目录

  ```shell
  hadoop fs -mkdir /jinguo
  ```

* -cp：拷贝文件

  ```shell
  hadoop fs -cp /sanguo/shuguo.txt /jinguo
  ```

* -mv：移动文件

  ```shell
  hadoop fs -mv /sanguo/wuguo.txt /jinguo
  ```

* -tail：从文件末尾查看1kb的数据

  ```shell
  hadoop fs -tail /jinguo/shuguo.txt 
  ```

* -rm：删除文件或文件夹

  ```shell
  hadoop fs -rm /sanguo/shuguo.txt
  ```

* -rm -r：递归删除目录及目录里的所有内容

  ```shell
  hadoop fs -rm -r sanguo
  ```

* -du：统计文件夹的大小信息

  ```shell
  hadoop fs -du -s -h /jinguo
  
  hadoop fs -du -h /jinguo
  ```

​		![image-20230107224250970](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/image-20230107224250970.png)

​		说明：18表示文件大小；54 = 18 * 3（副本数）。

* -setrep：设置文件的副本数量

  ```shell
  hadoop fs -setrep 5 /sanguo/shuguo.txt
  ```

  > 注意：设置的副本数只是记录在NameNode的元数据中，是否真会有这么多副本，需要看DataNode的数量。

# 3、HDFS API操作

* 本章节的代码：[hdfs](../code\hdfs)

## 3.1、客户端环境准备

* 下载依赖文件：https://github.com/cdarlint/winutils

* 配置环境变量

  ![image-20230107234250679](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/image-20230107234250679.png)

![](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/image-20230107234358478.png)

* 编写测试类

  ```java
  public class HdfsClient {
      
      public static final String DEV_INPUT = "D:/learn/大数据/big-data/hadoop/input/";
      public static final String DEV_OUTPUT = "D:/learn/大数据/big-data/hadoop/output/";
      
      @Test
      public void mkdir() throws Exception {
          Configuration config = new Configuration();
          // hp: 指定访问HDFS的用户,防止权限报错
          FileSystem fs = FileSystem.get(new URI("hdfs://hp-node1:8020"), config, "hp");
          fs.mkdirs(new Path("/xiyou"));
          fs.close();
      }
  }
  ```

## 3.2、上传（测试参数优先级）

* code

```java
@Test
public void copyFromLocalFile() throws Exception {
    Configuration config = new Configuration();
    config.set("dfs.replication", "2");
    FileSystem fs = FileSystem.get(new URI("hdfs://hp-node1:8020"), config, "hp");
    fs.copyFromLocalFile(new Path(DEV_INPUT + "sunwukong.txt"), new Path("/xiyou"));
    fs.close();
}
```

* 将hdfs-site.xml拷贝到项目的resources资源目录下。

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
  <configuration>
      <property>
      <name>dfs.replication</name>
       <value>1</value>
      </property>
  </configuration>
  ```

* **参数优先级：**

  1. 客户端代码中设置的参数值。
  2. ClassPath下的用户自定义配置文件。
  3. 服务器的自定义配置（xxx-site.xml）。
  4. 服务器的默认配置（xxx-default.xml）。

## 3.3、下载

```java
@Test
public void copyToLocalFile() throws Exception {
    Configuration config = new Configuration();
    FileSystem fs = FileSystem.get(new URI("hdfs://hp-node1:8020"), config, "hp");
    fs.copyToLocalFile(new Path("/xiyou/sunwukong.txt"), new Path(DEV_OUTPUT + "sunwukong.txt"));
    fs.close();
}
```

## 3.4、删除文件和目录

```java
@Test
public void delete() throws Exception {
    Configuration config = new Configuration();
    FileSystem fs = FileSystem.get(new URI("hdfs://hp-node1:8020"), config, "hp");
    fs.delete(new Path("/xiyou"), true);
    fs.close();
}
```

## 3.5、文件详情查看

```java
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
```

## 3.6、文件和文件夹判断

```java
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
```

# 4、HDFS读写流程

## 4.1、HDFS数据写流程

![HDFS写数据流程](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/HDFS%E5%86%99%E6%95%B0%E6%8D%AE%E6%B5%81%E7%A8%8B.svg)

1. 客户端通过Distributed FileSystem模块向NameNode请求上传文件，NameNode检查目标文件是否已存在，父目录是否存在。
2. NameNode返回是否可以上传。
3. 客户端请求第一个 Block上传到哪几个DataNode服务器上。
4. NameNode返回3个DataNode节点，分别为dn1、dn2、dn3。
5. 客户端通过FSDataOutputStream模块请求dn1上传数据，dn1收到请求会继续调用dn2，然后dn2调用dn3，将这个通信管道建立完成。
6. dn1、dn2、dn3逐级应答客户端。
7. 客户端开始往dn1上传第一个Block（先从磁盘读取数据放到一个本地内存缓存），以Packet为单位，dn1收到一个Packet就会传给dn2，dn2传给dn3；dn1**每传一个packet会放入一个应答队列等待应答**。
8. 当一个Block传输完成之后，客户端再次请求NameNode上传第二个Block（重复执行3-7步）。

## 4.2、节点距离计算（网络拓扑）

问：在HDFS写数据的过程中，NameNode会选择距离待上传数据最近距离的DataNode接收数据。那么这个最近距离怎么计算呢？

答：两个节点到达最近的共同祖先的距离总和（节点距离）。

![节点距离计算](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/%E8%8A%82%E7%82%B9%E8%B7%9D%E7%A6%BB%E8%AE%A1%E7%AE%97.svg)

* Distance（data center1/rack1/node1，data center1/rack1/node1）= 1（同一节点）。
* Distance（data center1/rack1/node2，data center1/rack1/node3）= 2（同一机架不同节点）。
* Distance（data center1/rack2/node1，data center1/rack3/node3）= 4（同一数据中心的节点）。
* Distance（data center1/rack2/node2，data center2/rack1/node2）= 6（不同数据中心的节点）。

## 4.3、副本存储节点选择（机架感知）

* 官方说明：http://hadoop.apache.org/docs/r3.1.3/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html#Data_Replication

* 源码说明：BlockPlacementPolicyDefault#chooseTargetInOrder

* 副本节点选择：

  ![副本节点选择](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/%E5%89%AF%E6%9C%AC%E8%8A%82%E7%82%B9%E9%80%89%E6%8B%A9.svg)

  1. 第一个副本在client所在的节点上，如果客户端在集群外则随机选择一个。
  2. 第二个副本在另一个机架的随机一个节点上。
  3. 第三个副本在第二个副本所在机架的随机节点上。

## 4.4、HDFS的数据读流程

![HDFS读数据流程](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/HDFS%E8%AF%BB%E6%95%B0%E6%8D%AE%E6%B5%81%E7%A8%8B.svg)

1. 客户端通过DistributedFileSystem向NameNode请求下载文件，NameNode通过查询元数据，找到文件块所在的DataNode地址。
2. 挑选一个DataNode（就近原则，然后随机）请求读取数据。
3. DataNode开始传输数据给客户端（从磁盘里面读取数据输入流，以Packet为单位来做校验）。
4. 客户端以Packet为单位接收，先存储到本地缓存，然后写入目标文件。

# 5、NameNode

## 5.1、NN和2NN的工作机制

**思考：NameNode中的元数据是存储在哪里的？**

1. 首先，我们做个假设，如果存储在NameNode节点的磁盘中，因为经常需要进行随机访问，还有响应客户请求，必然是效率过低。因此，元数据需要存放在内存中。但如果只存在内存中，一旦断电，元数据丢失，整个集群就无法在工作。**因此产生在磁盘中备份元数据的FsImage。**

2. 这样又会带来新的问题，当在内存中的元数据更新时，如果同时更新FsImage，就会导致效率过低，但如果不更新，就会发生一致性问题，一旦NameNode节点断电，就会产生数据丢失。因此，引入Edits文件（只进行追加操作，效率很高）。**每当元数据有更新或者添加元数据时，修改内存中的元数据并追加到Edits中。**这样，一旦NameNode节点断电，可以通过FsImage和Edits合成元数据。

3. 如果长时间添加数据到Edits中，会导致该文件数据过大，效率降低，而且一旦断电，恢复元数据需要的时间过长。而且还需要定期进行FsImage和Edits的合并，如果这个操作由NameNode节点完成，又会占用NameNode的资源。**因此，引入一个新的节点SecondaryNamenode，专门用于FsImage和Edits的合并。**

![NameNode工作机制](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/NameNode%E5%B7%A5%E4%BD%9C%E6%9C%BA%E5%88%B6.svg)

NameNode和SecondaryNameNode的工作流程可大致分为两个阶段：

1. 第一阶段：NameNode启动
   1. 第一次格式化后启动NameNode，会创建Fsimage和Edits文件。如果不是第一次启动，直接加载编辑日志和镜像文件到内存。
   2. 处理客户端对元数据进行增删改查的请求。
   3. Name距离操作日志、更新滚动日志。
   4. NameNode在内存中对元数据进行增删改。
2. 第二阶段：：SecondaryNameNode工作
   1. Secondary NameNode询问NameNode是否需要CheckPoint，NameNode应答。
   2. 当NameNode应答需要CheckPoint，SecondaryNameNode就会请求执行CheckPoint。
   3. NameNode滚动正在写的Edits日志。
   4. 将NameNode中的Fsimage和Edits拷贝到SecondaryNameNode。
   5. SecondaryNameNode加载编辑日志和镜像文件到内存，并进行合并。
   6. 合并完成后生成新的镜像文件fsimage.chkpoint。
   7. 将fsimage.chkpoint拷贝到NameNode。
   8. NameNode将fsimage.chkpoint重新命名成fsimage，旧的fsimage重新命名加上版本后缀。

## 5.2、Fsimage和Edits解析

* 概念

  * Name格式化后，会在$HADOOP_HOME/data/dfs/name/current目录下产生如下文件：

    ![image-20230109235655370](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/image-20230109235655370.png)

  * fsimage：HDFS元数据的永久性检查点，其中包含HDFS的所有目录和文件inode的序列化信息。

  * edits：存放HDFS的所有更新操作的路径，客户端对HDFS的所有写操作首先会被记录到Edits中。

  * seen_txid：存放最新的edits的事务id，就是最后一个edits_的数字。

  * 每次NameNode启动时都会将fsimage和edits加载到内存中并进行合并；可以看到每次重新启动NameNodde时都会将fsimage和edits进行合并。

* 查看Fsimage文件

  ```shell
  # 命令语法
  hdfs oiv -p 转换的文件类型 -i fsimage文件 -o 转换后文件输出路径
  
  # 例子
  hdfs oiv -p xml -i fsimage_0000000000000000209 -o /opt/software/fsimage_209.xml
  ```

  > 将输出的文件下载到windows下使用idea进行格式化查看，主要关注INodeSection和INodeDirectorySection标签中的内容即可，具体的含义可通过看标签名猜测就差不多了，也可以百度查阅相关资料，这里就不细说了。

* 查看Edits文件

  ```shell
  # 命令语法
  hdfs oev -p 转换的文件类型 -i edits文件 -o 转换后文件输出路径
  
  # 例子
  hdfs oev -p xml -i edits_0000000000000000208-0000000000000000209 -o edits_209.xml
  ```

  > 想看到具体的内容最好是先对hdfs进行一些增删改查操作之后在查看edits文件。

* 思考：NameNode如何确定下次开机启动的时候合并哪些Edits？
  1. NameNode启动之后会加载最新的fsimage（fsimage_ 后面数字最大的）。
  2. 将大于当前fsimage的所有edits加载到内存中（edits_ 后面也是数字）。
  3. fsimag和edits进行合并，合并成功之后生成fsimage，fsimage文件名后缀为最大的edits_后面的数字。

## 5.3、CheckPoint检查点

* 通常情况下，SecondaryNameNode每隔一小时执行一次。

  ```xml
  # hdfs-default.xml
  <property>
    <name>dfs.namenode.checkpoint.period</name>
    <value>3600s</value>
  </property>
  ```

  > 我们也可以修改配置来进行调整。

* 一分钟检查一次操作次数，当操作次数达到1百万时，SecondaryNameNode执行一次。

  ```xml
  # hdfs-default.xml
  <property>
    <name>dfs.namenode.checkpoint.txns</name>
    <value>1000000</value>
  <description>操作动作次数</description>
  </property>
  <property>
    <name>dfs.namenode.checkpoint.check.period</name>
    <value>60s</value>
  <description> 1分钟检查一次操作次数</description>
  </property>
  ```

# 6、DataNode

## 6.1、DataNode的工作机制

![DataNode工作机制](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/DataNode%E5%B7%A5%E4%BD%9C%E6%9C%BA%E5%88%B6.svg)

1. 一个数据块在DataNode上以文件形式存储在磁盘上，包括两个文件，一个是数据本身，一个是元数据包括数据块的长度，块数据的校验和以及时间戳。

2. DataNode启动后向NameNode注册，注册成功之后，周期性（默认6小时）的向NameNode上报所有的块信息。

   ```xml
   <!-- hdfs-default.xml -->
   
   <!-- DN向NN汇报当前解读信息的时间间隔，默认6小时 -->
   <property>
   	<name>dfs.blockreport.intervalMsec</name>
   	<value>21600000</value>
   </property>
   
   <!-- DN扫描自己节点块信息列表的时间，默认6小时 -->
   <property>
   	<name>dfs.datanode.directoryscan.interval</name>
   	<value>21600s</value>
   </property>
   ```

3. DataNode每3秒向NameNode发送一次心跳，心跳返回结果带有NameNode给当前DataNode的命令（如：复制块数据到另一台机器、删除某个数据块等等）。如果超过10分钟没有收到某个DataNode的心跳，则认为该节点不可用。

4. 集群运行中可以安全加入和退出节点。

## 6.2、数据完整性

**思考：数据存储在HDFS中，是会有可能出现损坏或丢失部分数据的情况，那么如何保证数据的完整性呢？**

**答：**DataNode保证数据完整性的方法：

1. 当DataNode读取Block的时计算checksum。
2. 计算后的checksum与Block创建时的checksum进行对比，如果不相同则说明Block已经损坏。
3. DataNode在文件创建后会周期验证的checksum，保证数据的完整性。
4. 数据在网络传输中也可以出现数据的损坏和丢失的可能性，通常情况下网络传输前后也都会进行checksum，前后的checksum相同则说明数据完整，否则反之。
5. 常见的校验算法crc（32），md5（128），sha1（160）。

![数据完整性](https://qqs-images.oss-cn-shenzhen.aliyuncs.com/%E6%95%B0%E6%8D%AE%E5%AE%8C%E6%95%B4%E6%80%A7.svg)

## 6.3、DataNode掉线时限参数设置

* DataNode进程死亡或网络故障造成DataNode无法与NameNode通信，NameNode不会立即判定此节点为死亡，而是需要经过一段时间，这段时间称作超时时长（TimeOut）。

* 超时时长的计算公式为：TimeOut = 2 * dfs.namenode.heartbeat.recheck-interval + 10 * dfs.heartbeat.interval

  ```xml
  <!-- hdfs-site.xml -->
  <property>
      <name>dfs.namenode.heartbeat.recheck-interval</name>
      <value>300000</value>
  </property>
  
  <property>
      <name>dfs.heartbeat.interval</name>
      <value>3</value>
  </property>
  ```

  > 注意：
  >
  > 1. heartbeat.recheck.interval的单位为毫秒。
  > 2. dfs.heartbeat.interval的单位为秒。

