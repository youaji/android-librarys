package com.youaji.example.librarys.extensions

import com.tencent.mmkv.MMKV
import com.youaji.libs.util.mmkv.MMKVOwner
import com.youaji.libs.util.mmkv.mmkvStringSet

object SocketHistory : MMKVOwner {

    override val kv: MMKV = MMKV.mmkvWithID("SocketHistory")

    private var historyInfo by mmkvStringSet<MutableSet<String>>(mutableSetOf())

    fun insertHistory(ip: String, port: Int) {
        val historyValue = historyInfo as MutableSet<String>
        historyValue.add("${ip}:${port}")
        historyInfo = historyValue
    }

    fun getHistory(): Set<String> = historyInfo
}

data class MediaUrl(val name: String, val url: String)

val MediaUrls = listOf(
    MediaUrl("trailer.mp4", "https://media.w3.org/2010/05/sintel/trailer.mp4"),
    MediaUrl("局域31", "rtsp://192.168.254.31:8554/"),
    MediaUrl("局域101", "rtsp://192.168.254.101:554/test.264"),
    MediaUrl("W01红外", "rtsp://192.168.1.10:554/test.264"),
    MediaUrl("W01可见", "rtsp://192.168.1.10:654/test.264"),
    MediaUrl("apple-1", "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8"),
    MediaUrl("apple-2", "http://devimages.apple.com/iphone/samples/bipbop/gear3/prog_index.m3u8"),
    MediaUrl("apple-3", "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8"),
    MediaUrl("时间短", "https://qywapp.oss-cn-beijing.aliyuncs.com/2022/7/18/oss06ac675d-c493-2121-cfba-e5d627210036.mp4"),
    MediaUrl("加载慢", "rtmp://ns8.indexforce.com/home/mystream"),
    MediaUrl("测缓冲-海洋", "http://vjs.zencdn.net/v/oceans.mp4"),
    MediaUrl("测缓冲-雄兔", "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"),
    MediaUrl("t1-ui.mp4", "https://static.smartisanos.cn/common/video/t1-ui.mp4"),
    MediaUrl("jgpro.mp4", "https://static.smartisanos.cn/common/video/video-jgpro.mp4"),
    MediaUrl("漳浦综合HD", "http://220.161.87.62:8800/hls/0/index.m3u8"),
    MediaUrl("CCTV-1综合", "http://183.63.15.42:9901/tsfile/live/0001_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-2财经", "http://183.63.15.42:9901/tsfile/live/0002_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-3综艺", "http://183.63.15.42:9901/tsfile/live/0003_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-4中文", "http://183.63.15.42:9901/tsfile/live/0004_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-5体育", "http://183.63.15.42:9901/tsfile/live/0005_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-5赛事", "http://183.63.15.42:9901/tsfile/live/0127_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-6电影", "http://183.63.15.42:9901/tsfile/live/0006_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-7国防", "http://183.63.15.42:9901/tsfile/live/0007_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-8电视", "http://183.63.15.42:9901/tsfile/live/0008_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-9纪录 ", "http://183.63.15.42:9901/tsfile/live/0009_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-10科教", "http://183.63.15.42:9901/tsfile/live/0010_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-11戏曲", "http://183.63.15.42:9901/tsfile/live/0011_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-12社会", "http://183.63.15.42:9901/tsfile/live/0012_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-13新闻", "http://183.63.15.42:9901/tsfile/live/0013_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-14少儿", "http://183.63.15.42:9901/tsfile/live/0014_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-15音乐", "http://183.63.15.42:9901/tsfile/live/0015_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-16奥林", "http://183.63.15.42:9901/tsfile/live/0128_1.m3u8"),// [1920*1080]
    MediaUrl("CCTV-17农业", "http://183.63.15.42:9901/tsfile/live/0019_1.m3u8"),// [1920*1080]
    MediaUrl("广西卫视", "http://183.63.15.42:9901/tsfile/live/0113_1.m3u8"),// [1920*1080]
    MediaUrl("吉林卫视", "http://183.63.15.42:9901/tsfile/live/0116_1.m3u8"),// [1920*1080]
    MediaUrl("河北卫视", "http://183.63.15.42:9901/tsfile/live/0117_1.m3u8"),// [1920*1080]
    MediaUrl("云南卫视", "http://183.63.15.42:9901/tsfile/live/0119_1.m3u8"),// [1920*1080]
    MediaUrl("贵州卫视", "http://183.63.15.42:9901/tsfile/live/0120_1.m3u8"),// [1920*1080]
    MediaUrl("辽宁卫视", "http://183.63.15.42:9901/tsfile/live/0121_1.m3u8"),// [1920*1080]
    MediaUrl("东南卫视", "http://183.63.15.42:9901/tsfile/live/0123_1.m3u8"),// [1920*1080]
    MediaUrl("海南卫视", "http://183.63.15.42:9901/tsfile/live/0131_1.m3u8"),// [1920*1080]
    MediaUrl("湖北卫视", "http://183.63.15.42:9901/tsfile/live/0132_1.m3u8"),// [1920*1080]
    MediaUrl("江西卫视", "http://183.63.15.42:9901/tsfile/live/0138_1.m3u8"),// [1920*1080]
    MediaUrl("浙江卫视", "http://183.63.15.42:9901/tsfile/live/0139_1.m3u8"),// [1920*1080]
    MediaUrl("河南卫视", "http://183.63.15.42:9901/tsfile/live/0141_1.m3u8"),// [1920*1080]
    MediaUrl("重庆卫视", "http://183.63.15.42:9901/tsfile/live/0142_1.m3u8"),// [1920*1080]
    MediaUrl("广东卫视", "http://183.63.15.42:9901/tsfile/live/0125_1.m3u8"),// [1920*1080]
    MediaUrl("广东珠江", "http://183.63.15.42:9901/tsfile/live/0109_1.m3u8"),// [1920*1080]
    MediaUrl("广东体育", "http://183.63.15.42:9901/tsfile/live/0136_1.m3u8"),// [1920*1080]
    MediaUrl("广东新闻", "http://183.63.15.42:9901/tsfile/live/1004_1.m3u8"),// [1920*1080]
    MediaUrl("广东公共", "http://183.63.15.42:9901/tsfile/live/1005_1.m3u8"),// [1920*1080]
    MediaUrl("广东经济", "http://183.63.15.42:9901/tsfile/live/1006_1.m3u8"),// [1920*1080]
    MediaUrl("广东影视", "http://183.63.15.42:9901/tsfile/live/1007_1.m3u8"),// [1920*1080]
    MediaUrl("广东少儿", "http://183.63.15.42:9901/tsfile/live/1008_1.m3u8"),// [1920*1080]
    MediaUrl("嘉佳卡通", "http://183.63.15.42:9901/tsfile/live/1009_1.m3u8"),// [1920*1080]
    MediaUrl("南方卫视", "http://183.63.15.42:9901/tsfile/live/1012_1.m3u8"),// [1920*1080]
    MediaUrl("梨园频道", "http://183.63.15.42:9901/tsfile/live/1013_1.m3u8"),// [1920*1080]
    MediaUrl("武术世界", "http://183.63.15.42:9901/tsfile/live/1014_1.m3u8"),// [1920*1080]
    MediaUrl("文物宝库", "http://183.63.15.42:9901/tsfile/live/1015_1.m3u8"),// [1920*1080]
)