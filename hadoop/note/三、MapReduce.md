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

## 2.1、官方入门案例源码：WordCount

## 2.2、常用数据序列化类型

## 2.3、MapReduce编程规范

## 2.3、WordCount案例实操

# 3、Hadoop序列化

# 4、MapReduce核心原理

## 4.1、数据输入：InputFormat

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

