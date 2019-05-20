1. 重构DataFetcher
1.1修正isForceRefresh未起作用的问题
1.2 添加localStorrageAside默认为true的参数，表示本地存储只作为aside模式，即缓存模式；为false时，表示网络数据为本地存储的后端模式
1.3 添加是否创建新线程的参数
1.4 修改接口，当保存时需要返回affect的记录数量，用于优化remoteAsBackend模式时的查询。对于Room来说，可简单返回添加或修改的数据数量
1.5 修正通知interceptor时背景线程中报异常问题
1.6 添加debugName，区分发起的调用的log
1.7 添加enbaleMetrics，打印发起调用所耗费的时间
1.8 将各种控制开关改成enableXXX形式的名称

2. 集成 NonScrollableViewPager

3. 为Resource添加一个Consumed状态，用于关闭loading提示

4. RecyclerAdapter绑定数据时，提供一个position参数

5. 去掉对ResponseBox的支持，可以都通过自定义httpStatusCode来区分业务逻辑错误状态码