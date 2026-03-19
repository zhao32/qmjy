package com.cocos.game.csjAdManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.bytedance.sdk.djx.DJXSdk;
import com.bytedance.sdk.djx.IDJXWidget;
import com.bytedance.sdk.nov.api.INovWidgetFactory;
import com.bytedance.sdk.nov.api.NovSdk;
import com.bytedance.sdk.nov.api.params.NovReaderConfig;
import com.bytedance.sdk.nov.api.params.NovWidgetHomeParams;
import com.yourong.game.jydhm.R;

public class NovHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_djx_nov_activity);

        if (DJXSdk.isStartSuccess()) initWidget();
    }

    private void initWidget() {
        INovWidgetFactory factory = NovSdk.factory();
        if (factory != null) {
            Intent intent = getIntent();
            int endPageRecSize = intent.getIntExtra("endPageRecSize", 3);
            String pageTurnMode = intent.getStringExtra("pageTurnMode");
            if (TextUtils.isEmpty(pageTurnMode)) pageTurnMode = "TURN_LEFT_RIGHT_SIMULATE";

            NovReaderConfig readerConfig = new NovReaderConfig();
            // readerConfig.setRewardCodeId("954640435");
            readerConfig.setRewardAdMode(NovReaderConfig.NovRewardAdMode.MODE_SDK);
            readerConfig.setEndPageCardStyle(NovReaderConfig.NovEndPageCardStyle.STYLE_MIX);
            readerConfig.setEndPageRecSize(endPageRecSize);
            readerConfig.setDefaultPageTurnMode(NovReaderConfig.NovPageTurnMode.valueOf(pageTurnMode));

            NovWidgetHomeParams homeParams = new NovWidgetHomeParams(readerConfig);
            homeParams.setCanPullRefresh(true);

            IDJXWidget novWidget = factory.createStoryHome(homeParams);
            if (novWidget != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, novWidget.getFragment()).commit();
            }
        }
    }
}
