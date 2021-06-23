## XAudio
[![](https://jitpack.io/v/LucasXu01/XAudio.svg)](https://jitpack.io/#LucasXu01/XAudio)
### XAudio：音频一行播放，Android音频录制，支持多种音频格式如mp3音频录制及可视化
---------------------------------

* 一行播放网络音频
* 可快速实现音乐播放器功能
* 支持mp3、pcm、wav、aac格式音频录制
* 支持实时自定义录制音波图展示
* ...


## 快速开始

#### 1. 在module下的build.gradle添加依赖

use Gradle:

Step 1. Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Step 2. Add the dependency
```gradle
dependencies {
            implementation 'com.github.LucasXu01:XAudio:0.9.8'
	}
```


#### 2. 在app中注册

```java
public class myApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //XAudio初始化
        XAudio.getInstance().init(this);
    }
}
```

#### 3. 使用XAudio

播放音频

```java
        XAudio.getInstance()
                .addAudio(new AudioBean("https://sr-sycdn.kuwo.cn/resource/n2/33/25/2629654819.mp3"))
                .playAudio();
```
 录制音频
 ```java
        // 开始录音 默认数mp3格式
        mRecorder = new XRecorder("fileName:录音文件名");
        try {
             mRecorder.start();
             ...
         } catch (IOException e) {
             e.printStackTrace();
             ...
         }

        ...
        //结束录音
        mRecorder.stop();

 ```

 以上就是XAudio最简单最核心的两个功能使用的介绍了，更多地功能比如：通知、服务、音频波形图、录制音频的格式和参数选择等等，可具体参考Demo源码。

<br/>

### 注意点&&常见问题：
<br/> 1 网络音频播放需要网络权限，音频录制需要存储读写和录音权限。权限申请请用户自行解决，所需权限可参考[AndroidManifest.xml](./app/src/main/AndroidManifest.xml)和Demo
<br/> 2 若网络音频无法播放，检查歌曲链接，是否是https链接，或是否配置了network_security_config.xml
  测试歌曲链接 [链接](https://sr-sycdn.kuwo.cn/resource/n2/33/25/2629654819.mp3)


### Demo
[Demo下载](./XAudioDemo.apk)
<br/>
或扫描下面的二维码安装

<br/>

![XAudioDemo](https://www.pgyer.com/app/qrcode/hsVA)


## 效果显示
| 音频播放   | 音频录制  |
|:-----------:|:--------:|
|![](./01.jpg) | ![](./02.jpg) |


### QQ群，有兴趣的可以进来，有问题可以提问交流：317643862

----------------------------------------------------

### 0.9.8 (2021-06-23)
* 精简了依赖，一行导入库
* 修复播放页转盘抖动等bug
* 精简了20%的包体积大小
* 去除了库中主要UI部分，专注于功能逻辑
* 适配安卓11分区,取消了自定义录制路径

### 0.9.0 (2021-02-16)

* 编译lame库，添加mp3、aac、wav、pcm格式录音，并增加录音音频波形图

### 0.8.0 (2021-01-15)

* 完成XAudio音频播放器功能

### 使用方法请参考demo





