# 远程协助
这是应用于安卓端的远程协助例子，它包含
application2_clien和application2_service
两个包，客户端和服务端均已在安卓12和安卓7平台上
测试正常，另外此版本代码还不够健壮，还有许多优化空间。
# 存在问题
1.客户端的textureview双指放大时，未做画面保持处理。
2.客户端的返回指令与系统返回冲突。
3.客户端输入框未做数据类型过滤，类型错误必导致崩溃。
4.服务端因大量创建和销毁对象，导致内存抖动。
5.服务端截屏后仅简单采用位图压缩处理，导致宽带消耗巨大。
6.服务端未做录音处理。
7.服务端的ip在不依赖服务商的情况无法被客户端获知。
8.服务端进程未做保活极易被kill。
# 解决方案
1.内存抖动，建立对象池。
2.视频可以采用h265视频流格式处理。
3.无法获知服务端IP，在ipv6下可以直接发送短信方式获知。
ipv4需要内网穿透，如花生壳等，再通过短信或服务器发送。
# 忠告
因本人精力有限，不再对此版本进行更新维护，且此仓库所有
代码仅供学习参考之用。如有使用此软件或代码造成的任何法律
纠纷本人概不负责。


