# LogPrintByWebsocket
使用websocket+ssh 实时打印后台日志输出到页面上

之前做的一个系统里需要在页面直接查看后台服务器上的tail -f 日志 这里记录下
这里使用ssh进行免密登录 

首先导入相关的jar包 
如果要在java中使用ssh免密登录需要添加JSch包 pom文件中进行添加

首先导入websocket jar包 使用pom文件导入的而不是自带的tomcat里的包
然后导入JSch相关的包

另外关于ssh连接所使用的的公钥私钥请在服务器上自行配置好 否则无法使用（当然 你要使用账号密码登录也可以）

环境配置完成之后开始代码部分

页面部分  
服务端js里直接进行websocket的连接
