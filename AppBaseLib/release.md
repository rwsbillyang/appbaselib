1.3.1
--------------
1. 升级AndroidStudio至3.5.1
2. 升级依赖库版本
3. 添加个别扩展函数


1.3.0
--------------
1. 重构NetManager，包括修改接口ClientConfiguration，支持双向认证
2. 添加SslWebViewClient支持webview中的请求SSL认证
3. 其它修改改进

1.2.3
--------------
1. dataFetcher中添加enableLog控制开关
2. 修改NetConfiguration接口，将host放入其中，修改https自定义证书支持
3. 改进NetManager中对自定义证书的支持
4. 改进log的tag
5. 添加NumberPickerPreference
6. bugfix RecycleView中的列表数据类型错误

1.2.2
--------------
1. DataFetcher返回对象为Observerable，以方便支持后台线程加载数据；并支持Observerable转换成LiveData，以便UI使用
2. 为Fragment添加showDialog扩展
3. 重构RecycleView的Adapter基类

1.2.1
--------------
1. 添加 Preference PreferencesUtil 方便使用
2. 修复完善pageHandler，当有数据时，才上报新的当前页

1.2.0
--------------
1. 添加PageHandler 用于加载更多和更新
2. 添加onSwipeListener 用于监听ScrollView的横向滑动
3. 添加Spannable DSL 用于简化span的使用
4. 在DataFetcher中添加identifier标识符，用于标示具体的哪一次调用
5. 去掉对logger库的依赖，改用安卓标准Log的扩展用法
6. 改进RecycleView的adapter，支持分页
7. 修复DataFetcher中当从远程加载失败时也保存的bug
8. 去掉业务逻辑错误的Resource类型，由状态码代替

1.1.0
---------------
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