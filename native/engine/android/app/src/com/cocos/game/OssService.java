package com.cocos.game;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.MultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.OSSRequest;

public class OssService {
    private OssService() {
    }

    public static void asyncMultipartUpload(
            OSSClient oss, String bucketName, String objectKey, String uploadFilePath,
            OSSProgressCallback<MultipartUploadRequest> progressCallback,
            OSSCompletedCallback<MultipartUploadRequest, CompleteMultipartUploadResult> completedCallback
    ) {
        MultipartUploadRequest request = new MultipartUploadRequest(bucketName, objectKey, uploadFilePath);
        request.setCRC64(OSSRequest.CRC64Config.YES);

        request.setProgressCallback(new OSSProgressCallback<MultipartUploadRequest>() {
            @Override
            public void onProgress(MultipartUploadRequest request, long currentSize, long totalSize) {
                if (progressCallback != null)
                    progressCallback.onProgress(request, currentSize, totalSize);
            }
        });

        oss.asyncMultipartUpload(request, new OSSCompletedCallback<MultipartUploadRequest, CompleteMultipartUploadResult>() {
            @Override
            public void onSuccess(MultipartUploadRequest request, CompleteMultipartUploadResult result) {
                if (completedCallback != null) completedCallback.onSuccess(request, result);
            }

            @Override
            public void onFailure(MultipartUploadRequest request, ClientException clientException, ServiceException serviceException) {
                if (completedCallback != null)
                    completedCallback.onFailure(request, clientException, serviceException);
            }
        });
    }
}
