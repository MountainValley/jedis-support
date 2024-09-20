This project uses Jedis, which is licensed under the MIT License. For more information, see the LICENSE file.

实现功能
1. 基于jedis+属性配置文件的redis客户端程序
2. 提供分布式锁功能（锁重入逻辑使用ThreadLocal实现与redisson方案不同），支持锁重入和自动续期。相较于redisson方案减少对redis功能的依赖，比如不依赖Redis订阅发布功能。同时redis缓存信息量较少且交互次数较少。

使用方法参考单元测试代码。