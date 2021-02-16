# XAudio

### XAudio：音频播放器，支持多种格式音频录制
---------------------------------


* 一行播放网络音频
* 可快速实现音乐播放器功能
* 自带通知和服务，支持自定义
* 支持mp3、pcm、wav、aac格式音频录制
* 支持音波单双边显示（自动根据音频和控件高度调整波形高度）
* 支持获取声音大小
* 支持录制和播放的波形根据特征变颜色。
* 支持自定义音波图的线大小、方向和绘制偏移。



#### 在你的项目project下的build.gradle添加
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
#### 在module下的build.gradle添加依赖
```
dependencies {
     implementation 'com.lucas.xaudio:xaudio:0.9.1'
}

```
　

## 效果显示
<img src="./01.jpg" width="240px" height="426px"/>
<img src="./02.jpg" width="240px" height="426px"/>
　


### QQ群，有兴趣的可以进来，有问题可以提问交流：317643862

----------------------------------------------------

### 0.9.0 (2020-02-16)

* 编译lame库，添加mp3、aac、wav、pcm格式录音，并增加录音音频波形图

### 0.8.0 (20120-01-15)

* 完成XAudio音频播放器功能

### 使用方法请参考demo





