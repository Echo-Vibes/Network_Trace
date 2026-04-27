# Network_Trace
利用计算机上网记录跟踪软件，可以方便系统管理员掌握现有系统的性能情况， 随时掌握各种人员的上网记录，防止非法网站的访问，甚至避免恶意的攻击情况出现。 


## 各模块作用：  
+ 数据库工具 database.utility.java
+ 数据库访问对象 history_DAO.java
+ 代理管理器（备份、更改、恢复） proxy_manager.java
+ 代理服务器（监听端口、接受连接、分配线程） proxy_server.java


## 代码特征
+ 使用C/S架构，浏览器是客户端，代理服务器作为服务端，数据库是数据存储层
+ 使用了try-with-resources语法来节省资源，避免资源泄露
+ 使用ExecutorService线程池来管理浏览器连接，避免了频繁创建销毁线程的操作



## 遇见问题：  
+ 在实现黑名单拦截时，对于http类型的网址可以顺利拦截，但是对于https类型的网址无法正常实现拦截。
+ 原因在于is_blacklisted方法中得到的url是带有端口和协议的，但是数据库中的url没有端口
+ 解决办法：读入is_blacklisted方法中的url只留下中间部分，不用端口、协议等内容