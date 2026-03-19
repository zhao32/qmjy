package com.cocos.game

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult
import com.alibaba.sdk.android.oss.model.MultipartUploadRequest
import com.bumptech.glide.Glide
import com.cocos.service.SDKWrapper
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.mx.imgpicker.MXImagePicker
import com.mx.imgpicker.R
import com.mx.imgpicker.builder.MXCaptureBuilder
import com.mx.imgpicker.builder.MXPickerBuilder
import com.mx.imgpicker.builder.MXPickerBuilder.Companion.getPickerResult
import com.mx.imgpicker.models.MXPickerType
import java.io.File

object MXPickerUtil {
    private var _mxCaptureBuilder: MXCaptureBuilder? = null

    private var useOss: Boolean = false
    private var ak = ""
    private var sk = ""
    private var endpoint = ""
    private var bucket = ""

    private var type: String = "Image"
    private var url: String? = null
    private var fileKey: String = "fileImg"
    private var tokenKey: String = "Authorization"
    private var token: String? = null
    private var application: Application? = null

    fun init(application: Application) {
        this.application = application
        MXImagePicker.init(application)
        _mxCaptureBuilder = MXCaptureBuilder().setType(MXPickerType.Image)
        registerImageLoader()
    }

    fun registerImageLoader() {
        MXImagePicker.registerImageLoader { item, imageView ->
            if (File(item.path).exists()) {
                Glide.with(imageView).load(File(item.path))
                    .placeholder(R.drawable.mx_icon_picker_image_place_holder)
                    .into(imageView)
            } else if (item.path.startsWith("http")) {
                Glide.with(imageView).load(item.path)
                    .placeholder(R.drawable.mx_icon_picker_image_place_holder)
                    .into(imageView)
            } else {
                Glide.with(imageView).load(Uri.parse(item.path))
                    .placeholder(R.drawable.mx_icon_picker_image_place_holder)
                    .into(imageView)
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0x22) {
                val picker = getPickerResult(data)
                if (picker.isNotEmpty()) {
                    val image = picker[0]
                    // image: /data/user/0/com.yourong.cocos/cache/MX_IMG.jpg
                    this.uploadImg(image)
                }
            } else if (requestCode == 0x33) {
                if (_mxCaptureBuilder != null) {
                    val image = _mxCaptureBuilder!!.getCaptureFile().absolutePath
                    // image: /storage/emulated/0/Android/data/com.yourong.cocos/cache/MX_IMG.jpg
                    this.uploadImg(image)
                }
            }
        }
    }

    private fun uploadImg(image: String) {
        Logger.i("MXPickerUtil", "uploadImg image = $image")
        if (url.isNullOrBlank() || token.isNullOrBlank()) {
            val data = JSONObject()
            resultForChooseImage(JSON.toJSONString(data))
            return
        }

        if (useOss) {
            val credentialProvider = OSSPlainTextAKSKCredentialProvider(ak, sk)
            val oss = OSSClient(application, endpoint, credentialProvider)

            val objectName = File(image).name

            OssService.asyncMultipartUpload(
                oss, bucket, objectName, image,
                object : OSSProgressCallback<MultipartUploadRequest<*>?> {
                    override fun onProgress(
                        request: MultipartUploadRequest<*>?,
                        currentSize: Long,
                        totalSize: Long
                    ) {
                        val progress = (100 * currentSize / totalSize).toInt()
                        JSONObject().also {
                            it["totalSize"] = totalSize
                            it["currentSize"] = currentSize
                            it["progress"] = progress
                        }.apply {
                            resultForChooseImageProgress(JSON.toJSONString(this))
                        }

                        Logger.i(
                            "OssService",
                            String.format(
                                "Upload total：%s, current：%s, progress：%s",
                                totalSize,
                                currentSize,
                                progress
                            )
                        )
                    }
                },
                object :
                    OSSCompletedCallback<MultipartUploadRequest<*>?, CompleteMultipartUploadResult?> {
                    override fun onSuccess(
                        request: MultipartUploadRequest<*>?,
                        result: CompleteMultipartUploadResult?
                    ) {
                        Logger.i("OssService", String.format("request：%s", request))
                        Logger.i("OssService", String.format("result：%s", result))
                        resultForChooseImage(objectName)
                    }

                    override fun onFailure(
                        request: MultipartUploadRequest<*>?,
                        clientException: ClientException,
                        serviceException: ServiceException
                    ) {
                        Logger.i("OssService", String.format("clientException：%s", clientException))
                        Logger.i(
                            "OssService",
                            String.format("serviceException：%s", serviceException)
                        )
                        resultForChooseImage("")
                    }
                }
            )
        } else {
            if (url.isNullOrBlank() || token.isNullOrBlank()) {
                resultForChooseImage("")
                return
            }

            val files = mutableListOf(File(image))
            OkGo.post<String>(url)
                .params("type", type.lowercase())
                .addFileParams(fileKey, files)
                .headers(tokenKey, token)
                .isMultipart(true)
                .execute(object : StringCallback() {
                    override fun onSuccess(resp: Response<String>?) {
                        url = ""
                        token = ""
                        val res = resp?.body() ?: "{}"
                        resultForChooseImage(res)

                        /*val obj = JSON.parseObject(res)
                        if (obj.getIntValue("code") == 1) {
                            resultForChooseImage(obj.getString("data"))
                        } else {
                            val data = JSONObject()
                            resultForChooseImage(JSON.toJSONString(data))
                        }*/
                    }
                })
        }
    }

    private fun resultForChooseImage(image: String?) {
        Logger.i("MXPickerUtil", "resultForChooseImage image = $image")
        JsbBridgeCallback.getInstance().sendToScript("chooseMedia", image)
    }

    private fun resultForChooseImageProgress(progress: String?) {
        Logger.i("MXPickerUtil", "resultForChooseImageProgress image = $progress")
        JsbBridgeCallback.getInstance().sendToScript("chooseMediaProgress", progress)
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ) {
        val allMatch = grantResults.all { grant: Int -> grant == PackageManager.PERMISSION_GRANTED }
        if (allMatch) {
            if (requestCode == 0x44) {
                chooseImage(type, "album")
            } else if (requestCode == 0x55) {
                chooseImage(type, "camera")
            }
        }
    }

    fun openAlbum(
        useOss: Boolean,
        ak: String?,
        sk: String?,
        endpoint: String?,
        bucket: String?,

        type: String?,
        url: String?,
        fileKey: String?,
        tokenKey: String?,
        token: String?
    ) {
        this.useOss = useOss
        if (!ak.isNullOrBlank()) this.ak = ak
        if (!sk.isNullOrBlank()) this.sk = sk
        if (!endpoint.isNullOrBlank()) this.endpoint = endpoint
        if (!bucket.isNullOrBlank()) this.bucket = bucket

        if (!type.isNullOrBlank()) this.type = type
        if (!url.isNullOrBlank()) this.url = url
        if (!fileKey.isNullOrBlank()) this.fileKey = fileKey
        if (!tokenKey.isNullOrBlank()) this.tokenKey = tokenKey
        if (!token.isNullOrBlank()) this.token = token
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!hasPermission(SDKWrapper.shared().activity, permissions)) {
            SDKWrapper.shared().activity.runOnUiThread {
                Toast.makeText(
                    SDKWrapper.shared().activity,
                    SDKWrapper.shared().activity.getString(R.string.mx_picker_string_need_permission_storage),
                    Toast.LENGTH_SHORT
                ).show()
            }
            ActivityCompat.requestPermissions(
                SDKWrapper.shared().activity,
                permissions,
                0x44
            )
        } else {
            chooseImage(this.type, "album")
        }
    }

    fun openCamera(
        useOss: Boolean,
        ak: String?,
        sk: String?,
        endpoint: String?,
        bucket: String?,

        type: String?,
        url: String?,
        fileKey: String?,
        tokenKey: String?,
        token: String?
    ) {
        this.useOss = useOss
        if (!ak.isNullOrBlank()) this.ak = ak
        if (!sk.isNullOrBlank()) this.sk = sk
        if (!endpoint.isNullOrBlank()) this.endpoint = endpoint
        if (!bucket.isNullOrBlank()) this.bucket = bucket

        if (!type.isNullOrBlank()) this.type = type
        if (!url.isNullOrBlank()) this.url = url
        if (!fileKey.isNullOrBlank()) this.fileKey = fileKey
        if (!tokenKey.isNullOrBlank()) this.tokenKey = tokenKey
        if (!token.isNullOrBlank()) this.token = token
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (!hasPermission(SDKWrapper.shared().activity, permissions)) {
            SDKWrapper.shared().activity.runOnUiThread {
                Toast.makeText(
                    SDKWrapper.shared().activity,
                    SDKWrapper.shared().activity.getString(R.string.mx_picker_string_need_permission_camera),
                    Toast.LENGTH_SHORT
                ).show()
            }
            ActivityCompat.requestPermissions(
                SDKWrapper.shared().activity,
                permissions,
                0x55
            )
        } else {
            chooseImage(this.type, "camera")
        }
    }

    private fun hasPermission(context: Context, array: Array<out String>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        val list = ArrayList<String>()
        array.forEach {
            if (ContextCompat.checkSelfPermission(
                    context, it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                list.add(it)
            }
        }
        return list.isEmpty()
    }

    private fun chooseImage(type: String, source: String) {
        if (source == "album") {
            val intent = MXPickerBuilder()
                .setMaxSize(1).setMaxListSize(500)
                .setType(MXPickerType.valueOf(type)).setCameraEnable(false)
                .createIntent(SDKWrapper.shared().activity)
            SDKWrapper.shared().activity.startActivityForResult(intent, 0x22)
        } else if (source == "camera") {
            val intent = _mxCaptureBuilder!!.createIntent(SDKWrapper.shared().activity)
            SDKWrapper.shared().activity.startActivityForResult(intent, 0x33)
        }
    }
}