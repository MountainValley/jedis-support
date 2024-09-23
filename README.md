**This project uses Jedis, which is licensed under the MIT License. For more information, see the LICENSE file.**

## Features
1. A Redis client program based on Jedis and a properties configuration file.
2. Provides distributed lock functionality (the lock reentry logic is implemented using ThreadLocal, differing from Redisson's approach). It supports lock reentry and automatic renewal. Compared to Redisson’s solution, it reduces the dependency on Redis features, such as not relying on Redis' publish/subscribe functionality. Additionally, it minimizes the amount of cache information stored in Redis and reduces the number of interactions.
   Usage can be referenced in the unit test code.


## 实现功能
1. 基于jedis+属性配置文件的redis客户端程序
2. 提供分布式锁功能（锁重入逻辑使用ThreadLocal实现与redisson方案不同），支持锁重入和自动续期。相较于redisson方案减少对redis功能的依赖，比如不依赖Redis订阅发布功能。同时redis缓存信息量较少且交互次数较少。

**使用方法参考单元测试代码。**