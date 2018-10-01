## 应用介绍
本软件提供一套完整的外卖餐饮、私人订制服务解决方案，充分满足客户随时随地可以享受优质服务的需求。为了给客户带来更清晰、更高品质的服务，本软件分两端：外卖端和快递端，外卖端为客户提供了浏览到下单到购买一条龙服务功能，快递端提供了浏览客户订单、接单、配送、收款等功能。   

## 使用指南   
### 前置条件   
构建本软件需要安装以下环境   
```jdk``` ```android sdk``` ```gradle``` ```android studio```   

### 安装   
##### 安装jdk   
1、官网（www.oracle.com）下载JDK，选择“Java”-“Java（JDK）for Developers”   
2、下载完成后，直接点击安装   
3、配置环境变量   
##### 下载android sdk   
1、官网（android.developer.com）下载android SDK   
2、下载完成后，配置环境变量   
##### 安装gradle   
1、下载gradle，本软件使用gradle版本为3.4   
2、下载完成，解压安装   
3、配置环境变量   
##### 安装android studio   
1、官网（android.developer.com）下载3.0版本的android studio   
2、下载完成，解压使用   
### 如何运行   
##### 导入工程   
打开android studio，选择File-Open，选择工程根目录，确定   
导入devilfish module，将app设置依赖devilfish
##### 运行   
选择android studio-Build-Run   
## 构建部署   
在命令行，cd到工程的app目录下，执行如下命令   
```gradle clean assembleRelease```    
完成后，在工程目录/app/build/outputs中有已经构建的安装包，安装即可   
## 编码介绍  
本软件基于android build tools version 25 编译，支持android4.0+系统
##### 界面设计   
整体界面风格采用google material design设计风格，扁平化设计，更符合系统原生生态要求  
架构使用FragmentActivity+Fragment设计，碎片化处理   
BaseActivity-工程Activity基类，管理Activity生命周期，以及一些通用的方法在该类实现   
BaseFragment-工程Fragment基类，实现一些通用的方法，以及管理Fragment和宿主Activity通信  
##### 网络请求   
本软件使用OkHttp3+GSON处理网络请求   
OkHttpClientManager-工程网络请求类，本软件所有网络请求均在该类中实现   
WtNetWorkListener-网络请求回调，该类提供如下三个方法，用于处理网络请求结果   

```void onSuccess(RemoteReturnData<T> data)```   

```void onError(String status, String msg_cn, String msg_en)```   
   
```void onFinished()```

##### 数据总线处理   
本软件使用观察者模式，满足应用中数据通信需求   
##### 图片处理   
本软件使用Fresco加载图片

##关于作者   
##授权协议  

