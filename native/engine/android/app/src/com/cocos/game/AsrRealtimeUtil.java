package com.cocos.game;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.tencent.aai.AAIClient;
import com.tencent.aai.audio.data.AudioRecordDataSource;
import com.tencent.aai.auth.LocalCredentialProvider;
import com.tencent.aai.exception.ClientException;
import com.tencent.aai.exception.ServerException;
import com.tencent.aai.listener.AudioRecognizeResultListener;
import com.tencent.aai.listener.AudioRecognizeStateListener;
import com.tencent.aai.model.AudioRecognizeConfiguration;
import com.tencent.aai.model.AudioRecognizeRequest;
import com.tencent.aai.model.AudioRecognizeResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AsrRealtimeUtil {
    private AAIClient aaiClient;
    private final LinkedHashMap<String, String> resMap = new LinkedHashMap<>();
    private Context context;
    private String event;
    private JSONObject data;

    private AsrRealtimeUtil() {
    }

    private static final class InstanceHolder {
        private static final AsrRealtimeUtil mInstance = new AsrRealtimeUtil();
    }

    public static AsrRealtimeUtil getInstance() {
        return AsrRealtimeUtil.InstanceHolder.mInstance;
    }

    public void handler(Context context, String event, JSONObject data) {
        String[] permissions = {Manifest.permission.RECORD_AUDIO};
        if (hasPermission(context, permissions)) {
            String provider = data.getString("provider");
            if (TextUtils.equals(provider, "tencent")) {
                // https://cloud.tencent.com/document/product/1093/35722
                resMap.clear();
                stopAsrRealtimeTencent();
                String action = data.getString("action");
                if (TextUtils.equals("start", action)) {
                    int appId = data.getIntValue("appId"); // 1300039572
                    int projectId = data.getIntValue("projectId", 0); // 此参数固定为 0
                    String secretId = data.getString("secretId"); // AKIDDKI4e2HLFySxHXBpJk6QgyWAlUyTs495
                    String secretKey = data.getString("secretKey"); // vc7Uefgx60fQ7dy78zTlqqeAbpqhLvLd
                    String token = data.getString("token");
                    String engineModelType = data.getString("engineModelType");
                    int filterDirty = data.getIntValue("filterDirty", 0);
                    int filterModal = data.getIntValue("filterModal", 0);
                    int filterPunc = data.getIntValue("filterPunc", 0);
                    int convertNumMode = data.getIntValue("convertNumMode", 1);
                    int needvad = data.getIntValue("needvad", 0);

                    startAsrRealtimeTencent(context, event, appId, projectId, secretId, secretKey, token,
                            engineModelType, filterDirty, filterModal, filterPunc, convertNumMode, needvad);
                } else if (TextUtils.equals("stop", action)) {
                    stopAsrRealtimeTencent();
                }
            }
        } else {
            this.context = context;
            this.event = event;
            this.data = data;
            ActivityCompat.requestPermissions((Activity) context, permissions, 0x66);
        }
    }

    private void startAsrRealtimeTencent(Context context, String event, int appId, int projectId,
                                         String secretId, String secretKey, String token,
                                         String engineModelType, int filterDirty, int filterModal,
                                         int filterPunc, int convertNumMode, int needvad) {
        try {
            if (TextUtils.isEmpty(token)) {
                // 直接鉴权
                aaiClient = new AAIClient(context, appId, projectId, secretId, new LocalCredentialProvider(secretKey));
            } else {
                // 使用临时密钥鉴权
                aaiClient = new AAIClient(context, appId, projectId, secretId, secretKey, token);
            }

            AudioRecognizeRequest.Builder builder = new AudioRecognizeRequest.Builder();
            AudioRecognizeRequest audioRecognizeRequest = builder
                    // 设置数据源, 数据源要求实现 PcmAudioDataSource 接口, 您可以自己实现此接口来定制您的自定义数据源, 例如第三方推流中
                    // 使用 SDK 内置录音器作为数据源, false:不保存音频
                    .pcmAudioDataSource(new AudioRecordDataSource(false))
                    // 设置引擎参数("16k_zh" 通用引擎, 支持中文普通话+英文)
                    // https://cloud.tencent.com/document/product/1093/48982
                    .setEngineModelType(!TextUtils.isEmpty(engineModelType) ? engineModelType : "16k_zh")
                    // 0:默认状态, 不过滤脏话; 1:过滤脏话
                    .setFilterDirty(filterDirty)
                    // 0:默认状态, 不过滤语气词; 1:过滤部分语气词; 2:严格过滤
                    .setFilterModal(filterModal)
                    // 0:默认状态, 不过滤句末的句号; 1:滤句末的句号
                    .setFilterPunc(filterPunc)
                    // 1:默认状态, 根据场景智能转换为阿拉伯数字; 0:全部转为中文数字
                    .setConvert_num_mode(convertNumMode)
                    // 0:关闭 vad; 1:默认状态 开启 vad, 语音时长超过一分钟需要开启, 如果对实时性要求较高, 并且时间较短的输入, 建议关闭
                    .setNeedvad(needvad)
                    // 热词 id, 用于调用对应的热词表, 如果在调用语音识别服务时, 不进行单独的热词 id 设置, 自动生效默认热词; 如果进行了单独的热词 id 设置, 那么将生效单独设置的热词 id
                    // .setHotWordId("")
                    // 自学习模型 id, 如果设置了该参数, 那么将生效对应的自学习模型
                    // .setCustomizationId("")
                    // 是否显示词级别时间戳, 0:不显示; 1:显示, 不包含标点时间戳; 2:显示, 包含标点时间戳
                    // .setWordInfo(0)
                    .build();

            AudioRecognizeResultListener audioRecognizeResultlistener = new AudioRecognizeResultListener() {
                @Override
                public void onSliceSuccess(AudioRecognizeRequest request, AudioRecognizeResult result, int seq) {
                    // 返回分片的识别结果，此为中间态结果，会被持续修正
                    Logger.e("", "识别中 ~ 分片");
                    resMap.put(String.valueOf(seq), result.getText());
                    String text = buildMessage(resMap);
                    Logger.e("", String.format("分片：%s", text));
                    JSONObject obj = new JSONObject();
                    obj.put("state", "slice");
                    obj.put("text", text);
                    JsbBridgeCallback.getInstance().sendToScript(event, JSON.toJSONString(obj));
                }

                @Override
                public void onSegmentSuccess(AudioRecognizeRequest request, AudioRecognizeResult result, int seq) {
                    // 返回语音流的识别结果，此为稳定态结果，可做为识别结果用与业务
                    Logger.e("", "识别中 ~ 语音流");
                    resMap.put(String.valueOf(seq), result.getText());
                    String text = buildMessage(resMap);
                    Logger.e("", String.format("语音流：%s", text));
                    JSONObject obj = new JSONObject();
                    obj.put("state", "segment");
                    obj.put("text", text);
                    JsbBridgeCallback.getInstance().sendToScript(event, JSON.toJSONString(obj));
                }

                @Override
                public void onSuccess(AudioRecognizeRequest request, String result) {
                    // 识别结束回调，返回所有的识别结果
                    Logger.e("", "识别完成");
                    Logger.e("", String.format("识别结束：%s", result));
                    JSONObject obj = new JSONObject();
                    obj.put("state", "success");
                    obj.put("text", result);
                    JsbBridgeCallback.getInstance().sendToScript(event, JSON.toJSONString(obj));
                }

                @Override
                public void onFailure(AudioRecognizeRequest request, final ClientException clientException,
                                      final ServerException serverException, String response) {
                    // 识别失败
                    Logger.e("", "识别失败");
                    if (clientException != null) {
                        Logger.e("", String.format("客户端异常: %s", clientException.getMessage()));
                    }
                    if (serverException != null) {
                        Logger.e("", String.format("服务端异常: %s", serverException.getMessage()));
                    }
                    if (!TextUtils.isEmpty(response)) {
                        Logger.e("", String.format("服务端 response: %s", response));
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("state", "fail");
                    obj.put("text", response);
                    JsbBridgeCallback.getInstance().sendToScript(event, JSON.toJSONString(obj));
                }
            };

            AudioRecognizeStateListener audioRecognizeStateListener = new AudioRecognizeStateListener() {
                @Override
                public void onStartRecord(AudioRecognizeRequest audioRecognizeRequest) {
                    // 开始录音
                    Logger.e("", "开始录音");
                    JSONObject obj = new JSONObject();
                    obj.put("state", "start");
                    JsbBridgeCallback.getInstance().sendToScript(event, JSON.toJSONString(obj));
                }

                @Override
                public void onStopRecord(AudioRecognizeRequest audioRecognizeRequest) {
                    // 结束录音
                    Logger.e("", "结束录音");
                    JSONObject obj = new JSONObject();
                    obj.put("state", "stop");
                    JsbBridgeCallback.getInstance().sendToScript(event, JSON.toJSONString(obj));
                }

                @Override
                public void onVoiceVolume(AudioRecognizeRequest audioRecognizeRequest, int i) {
                }

                @Override
                public void onVoiceDb(float v) {
                    // 音量分贝（取值范围：0~100，集中分布在40~80）
                }

                @Override
                public void onNextAudioData(short[] shorts, int i) {
                    // 返回音频流，用于返回宿主层做录音缓存业务。new AudioRecordDataSource(true) 传递 true 时生效
                }

                @Override
                public void onSilentDetectTimeOut() {
                    // 静音检测超时回调，此时任务还未中止，仍会等待最终识别结果
                }
            };

            AudioRecognizeConfiguration audioRecognizeConfiguration = new AudioRecognizeConfiguration.Builder()
                    // 分片默认 40ms, 可设置 40-5000, 如果您不了解此参数不建议更改
                    .sliceTime(40)
                    // 是否开启静音检测
                    .setSilentDetectTimeOut(false)
                    // 静音检测超时时间, 可设置 >2000ms, 默认值 5000ms, setSilentDetectTimeOut 为 true有效, 超过指定时间没有说话将关闭识别;
                    // 需要大于等于 sliceTime，实际时间为 sliceTime 的倍数, 如果小于 sliceTime, 则按 sliceTime 的时间为准;
                    .audioFlowSilenceTimeOut(3000)
                    // 音量检测回调时间, 默认值 80ms, 需要大于等于 sliceTime, 实际时间为 sliceTime 的倍数, 如果小于 sliceTime, 则按 sliceTime 的时间为准
                    .minVolumeCallbackTime(80)
                    .build();

            aaiClient.startAudioRecognize(audioRecognizeRequest, audioRecognizeResultlistener,
                    audioRecognizeStateListener, audioRecognizeConfiguration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopAsrRealtimeTencent() {
        if (aaiClient != null) aaiClient.stopAudioRecognize();
    }

    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == 0x66) {
            List<Integer> list = new ArrayList<>();
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) list.add(grantResult);
            }
            if (list.isEmpty()) {
                handler(context, event, data);
            }
        }
    }

    private boolean hasPermission(Context context, String[] array) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        List<String> list = new ArrayList<>();
        for (String s : array) {
            if (ContextCompat.checkSelfPermission(context, s) != PackageManager.PERMISSION_GRANTED) {
                list.add(s);
            }
        }
        return list.isEmpty();
    }

    private String buildMessage(Map<String, String> msg) {
        StringBuilder stringBuffer = new StringBuilder();
        for (Map.Entry<String, String> entry : msg.entrySet()) {
            String value = entry.getValue();
            stringBuffer.append(value).append("\r\n");
        }
        return stringBuffer.toString();
    }
}
