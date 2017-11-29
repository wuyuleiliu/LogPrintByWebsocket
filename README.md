# LogPrintByWebsocket
使用websocket+ssh 实时打印后台日志输出到页面上

之前做的一个系统里需要在页面直接查看后台服务器上的tail -f 日志 这里记录下
使用ssh进行免密登录 

首先导入相关的jar包  搭建spring环境
如果要在java中使用ssh免密登录需要添加JSch包 pom文件中进行添加

导入websocket jar包 使用pom文件导入的而不是自带的tomcat里的包
然后导入JSch相关的包

另外关于ssh连接所使用的的公钥私钥请在服务器上自行配置好 否则无法使用（当然 你要使用账号密码登录也可以）

环境配置完成之后开始代码部分
这里ssh免密连接 将服务器上生成的公钥放到了本地文件夹下 是可以测试的 如果放到linux环境下 需处理目录以及对应的用户（权限不同）

页面部分  
服务端js里直接进行websocket的连接  每个方法的作用不做解释 自行百度 页面部分还是比较简单的

后台部分
首先Spring里对websocket连接处理 代码部分都在src/main/java/com/gcdemo/util/websocket/WsLogHandle.java里
基本上是固定的模板

在onOpen方法里进行连接时做的一些处理 页面将服务器连接的相关信息发送过来 异步线程池里开启tail -f 打印的日志 将sessionId和对应的线程关联
这是因为tail -f命令的处理有些特殊，我们这里使用readline操作按行读取 如果日志打印没有出现最新的一行 程序将阻塞在这里
因此必须要强行关闭执行中的线程 这里和sessionId关联起来 页面关闭时通过onClose方法找到对应的打印线程强行终止进程即可释放资源
这里还发现一个问题，即便终止了线程，在服务器上tail -f线程还在 所以这里添加了一个kill命令 终止之后先kill掉tail命令 再关闭ssh连接

到这里基本结束，如有问题请留言，谢谢



