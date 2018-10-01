## intro
order taking app for employees  

## guide
### Prerequisites

```jdk``` ```android sdk``` ```gradle``` ```android studio```   

### Installation    
##### Installation jdk   
1、go to（www.oracle.com）download JDK，click “Java”-“Java（JDK）for Developers”   
2、After the download is complete, click directly to install
3, configure environment variables  
##### down load android sdk   
1、（android.developer.com）download android SDK   
2、configure environment variables
##### Installation gradle   
1、download radle3.4 
2、configure environment variables
##### Installation android studio   
1、（android.developer.com）download android studio 3.0   
  
### how to run
##### Import project 
run android studio，click File-Open，Select the project root directory and confirm 
Import devilfish module，Set the app to depend on devilfish
##### run 
android studio-Build-Run   
## Build deployment   
On the command line, cd to the app directory of the project, execute the following command
```gradle clean assembleRelease```    
After the completion, there are already installed installation packages in the project directory /app/build/outputs,  install 
##### Network request
This software uses OkHttp3+GSON to process network requests.
OkHttpClientManager - engineering network request class, all network requests of the software are implemented in this class
WtNetWorkListener - network request callback, this class provides the following three methods for processing network request results  

```void onSuccess(RemoteReturnData<T> data)```   

```void onError(String status, String msg_cn, String msg_en)```   
   
```void onFinished()```




