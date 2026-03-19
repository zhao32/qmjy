package com.cocos.game.csjAdManager;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bytedance.sdk.djx.DJXSdk;
import com.bytedance.sdk.djx.IDJXWidget;
import com.bytedance.sdk.djx.IDJXWidgetFactory;
import com.bytedance.sdk.djx.interfaces.listener.IDJXDramaHomeListener;
import com.bytedance.sdk.djx.interfaces.listener.IDJXDramaUnlockListener;
import com.bytedance.sdk.djx.model.DJXDrama;
import com.bytedance.sdk.djx.model.DJXDramaDetailConfig;
import com.bytedance.sdk.djx.model.DJXDramaUnlockAdMode;
import com.bytedance.sdk.djx.model.DJXDramaUnlockInfo;
import com.bytedance.sdk.djx.model.DJXDramaUnlockMethod;
import com.bytedance.sdk.djx.model.DJXUnlockModeType;
import com.bytedance.sdk.djx.params.DJXWidgetDramaHomeParams;
import com.cocos.game.Logger;
import com.yourong.game.jydhm.R;

import java.util.Map;

public class DjxHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_djx_nov_activity);

        if (DJXSdk.isStartSuccess()) initWidget();
    }

    private void initWidget() {
        Intent intent = getIntent();
        int freeSet = intent.getIntExtra("freeSet", 1);
        int lockSet = intent.getIntExtra("lockSet", 1);

        DJXDramaDetailConfig detailConfig = DJXDramaDetailConfig.obtain(DJXDramaUnlockAdMode.MODE_COMMON, freeSet, new IDJXDramaUnlockListener() {
            @Override
            public void unlockFlowStart(@NonNull DJXDrama djxDrama, @NonNull UnlockCallback unlockCallback, @Nullable Map<String, ?> map) {
                Logger.e("", "短剧 解锁链路开始");
                DJXDramaUnlockInfo info = new DJXDramaUnlockInfo(djxDrama.id, lockSet, DJXDramaUnlockMethod.METHOD_AD, false, null, false, DJXUnlockModeType.UNLOCKTYPE_DEFAULT);
                unlockCallback.onConfirm(info);
            }

            @Override
            public void unlockFlowEnd(@NonNull DJXDrama djxDrama, @Nullable UnlockErrorStatus unlockErrorStatus, @Nullable Map<String, ?> map) {
                Logger.e("", "短剧 解锁链路结束");
            }

            @Override
            public void showCustomAd(@NonNull DJXDrama djxDrama, @NonNull CustomAdCallback customAdCallback) {
                Logger.e("", "短剧 解锁链路监听 showCustomAd");
            }
        });

        IDJXWidgetFactory factory = DJXSdk.factory();
        if (factory != null) {
            IDJXWidget djxWidget = factory.createDramaHome(DJXWidgetDramaHomeParams.obtain(detailConfig)
                    .listener(new IDJXDramaHomeListener() {
                        @Override
                        public void onItemClick(DJXDrama drama, @Nullable Map<String, Object> map) {
                            super.onItemClick(drama, map);
                        }
                    }));
            getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, djxWidget.getFragment()).commit();
        }
    }
}
