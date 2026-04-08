import { native, sys } from 'cc';

interface Callback {
  success?: Function | null | undefined;
  showAd?: Function | null | undefined;
  onProgress?: Function | null | undefined;
  rewarded?: Function | null | undefined;
  fail?: Function | null | undefined;
}

// 3.8.x
export class yr {
  private constructor() { }

  private static _inst?: yr | null | undefined = null;
  public static get inst(): yr {
    if (this._inst == null) this._inst = new yr();
    return this._inst;
  }

  rewardedAdList: any = [];
  interstitialAdList: any = [];
  splashAdList: any = [];
  feedAdList: any = [];
  bannerAdList: any = [];

  public ads: any = {
    provider: 'csj', // 广告商
    appid: '5571516', // 广告应用 id
    splash: '', // 开屏广告 id
    banner: '', // 横幅广告 id
    interstitial: '', // 插屏广告 id
    rewarded: '102985035', // 激励广告 id
  };
  public ums: any = {
    umid: '', // 友盟 appid, 可不传
    wx: 'wx0274bd3250d1da22', // 微信 appid
    wxs: '687026d2fc83f13650dc13c5697255f2', // 微信 secret
    qq: '', // QQ appid
    qqs: '', // QQ secret
    sina: '', // 新浪 appid
    sinas: '', // 新浪 secret
  };

  private callback: Map<string, Callback | null | undefined> = new Map();
  private setCallback(event: string, callback: Callback) {
    this.callback.set(event, callback);
  }
  private getCallback(event: string) {
    return this.callback.has(event) ? this.callback.get(event) : null;
  }

  private windowInfo: any = null;
  private customAd: any = null;
  private bannerAd: any = null;
  private interstitialAd: any = null;
  private rewardedVideoAd: any = null;

  private isAndroid() {
    return sys.isNative && sys.os == sys.OS.ANDROID;
  }

  private isIos() {
    return sys.isNative && sys.os == sys.OS.IOS;
  }

  private isBytedanceGame() {
    return sys.platform == sys.Platform.BYTEDANCE_MINI_GAME;
  }

  private isWechatGame() {
    return sys.platform == sys.Platform.WECHAT_GAME;
  }

  private bridge: 'native' | 'window' = 'window';
  public init() {
    console.log('sys.isNative', sys.isNative);
    console.log('sys.os', sys.os, JSON.stringify(sys.OS));
    console.log('sys.platform', sys.platform, JSON.stringify(sys.Platform));

    if (this.isAndroid() || this.isIos()) {
      native.bridge.onNative = (res: string, arg?: string | null | undefined) => {
        if (this.bridge == 'native') this.onNative(res, arg);
      };

      window['onNative'] = (res: any, arg?: string | null | undefined) => {
        if (this.bridge == 'window') this.onNative(JSON.stringify(res), arg);
      };
    } else if (this.isBytedanceGame()) {
      tt.onShareAppMessage(() => {
        // { channel: 'article' | 'video' }
        return {
          title: '',
          imageUrl: '',
        };
      });

      this.windowInfo = tt.getSystemInfoSync();
    } else if (this.isWechatGame()) {
      wx.onShareAppMessage(() => {
        return {
          title: '',
          imageUrl: '',
        };
      });

      this.windowInfo = wx.getWindowInfo();
    } else {
    }
  }

  private onNative(res: string, _arg?: string | null | undefined) {
    console.log('yr onNative', res);
    let resp = JSON.parse(res);
    let { event, data } = resp;
    console.log('yr onNative event', event, 'data', data);

    if (event == 'rewarded') {
      event = 'rewardedVideoAd';
      if (data && this.getCallback(event)?.rewarded)
        this.getCallback(event).rewarded(data);
    } else if (event == 'chooseMediaProgress') {
      event = 'chooseMedia';
      if (data && this.getCallback(event)?.onProgress)
        this.getCallback(event).onProgress(data);
    } else if (event == 'showAd') {
      let adv_show = JSON.parse(data || '{}');
      if (data && this.getCallback(adv_show.adv_event)?.showAd)
        this.getCallback(adv_show.adv_event).showAd(data);
    } else {
      if (data && this.getCallback(event)?.success)
        this.getCallback(event).success(data);
      else if (this.getCallback(event)?.fail)
        this.getCallback(event).fail(data);
    }
  }

  private sendToNative(event: string, data?: any) {
    this.setCallback(event, { success: data?.success, showAd: data?.showAd, onProgress: data?.onProgress, rewarded: data?.rewarded, fail: data?.fail });

    let obj: Object = { event: event, data: data };
    let req = JSON.stringify(obj);
    console.log('------this.bridg:', this.bridge);
    if (this.bridge == 'native') {
      native.bridge.sendToNative(req, '');
    }

    if (this.bridge == 'window') {
      if (this.isAndroid()) {
        let typeVoid = 'V';
        let typeString = 'Ljava/lang/String;';
        let className = 'com/cocos/game/AppActivity';
        let methodName = 'sendToNative';
        let methodSignature = `(${typeString}${typeString})${typeVoid}`;
        native.reflection.callStaticMethod(className, methodName, methodSignature, req, '');

        console.log('------yr sendToNative', req);

      } else if (this.isIos()) {
        let className = 'AppController';
        let methodName = 'sendToNative:andArg:';
        native.reflection.callStaticMethod(className, methodName, req, '');
      }
    }
  }

  /**
   * 初始化友盟
   * {
   *   umid: '', // 友盟 appid, 可不传
   *   wx: '', // 微信 appid
   *   wxs: '', // 微信 secret
   *   qq: '', // QQ appid
   *   qqs: '', // QQ secret
   *   sina: '', // 新浪 appid
   *   sinas: '', // 新浪 secret
   * }
   */
  public initUM(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('initUM', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 登录
   * {
   *   provider: 'wx' | 'qq' | 'sina', // app 必填
   * }
   */
  public login(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('login', data);
    } else if (this.isBytedanceGame()) {
      tt.login({
        success: (res: any) => {
          if (data?.success) data.success(res);
        },
        fail: (res: any) => {
          if (data?.fail) data.fail(res);
        },
      });
    } else if (this.isWechatGame()) {
      wx.login({
        success: (res: any) => {
          if (data?.success) data.success(res);
        },
        fail: (res: any) => {
          if (data?.fail) data.fail(res);
        },
      });
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 分享
   * {
   *   provider: 'wx' | 'qq' | 'sina', // app 必填
   *   type: 0, // 0 图文web | 1 纯文字 | 2 纯图 | 3 海报
   *   href: '', // type 为 0 时 必填
   *   title: '', // type 为 0 时 必填
   *   summary: '', // type 为 1 时 必填
   *   image: '', // type 为 2 时 必填, 'http://' | 'https://'
   *   avatar: '', // 头像, type 为 3 时 必填
   *   inviteCode: '', // 邀请码, type 为 3 时 必填
   *   nickname: '', // 昵称, type 为 3 时 必填
   *   content: '', // 二维码内容, type 为 3 时 必填
   * }
   */
  public share(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('share', data);
    } else if (this.isBytedanceGame()) {
      // 主动调用转发相关方法（拉起发布器、好友邀请、录屏分享等）
      tt.shareAppMessage({
        title: data?.title ?? '',
        imageUrl: data?.image ?? '',
        success: () => {
          if (data?.success) data.success();
        },
        fail: () => {
          if (data?.fail) data.fail();
        },
      });
    } else if (this.isWechatGame()) {
      // 主动拉起转发, 进入选择通讯录界面
      wx.shareAppMessage({
        title: data?.title ?? '',
        imageUrl: data?.image ?? '',
        success: () => {
          if (data?.success) data.success();
        },
        fail: () => {
          if (data?.fail) data.fail();
        },
      });
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 支付
   * {
   *   provider: 'wxpay' | 'alipay', // app 必填
   *   data: '',
   * }
   */
  public payment(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('payment', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 微信-商家转账用户确认模式-用户确认收款
   * {
   *   mchId: '',
   *   appId: '',
   *   package: '',
   * }
   */
  public requestMerchantTransfer(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('requestMerchantTransfer', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 初始化广告
   * {
   *   provider: 'adset' | 'bd' | 'csj' | 'gdt' | 'iqy' | 'ks' | 'oppo' | 'sigmob' | 'sigmobMill' | 'taku' | 'tap', // app 必填
   *   appId: '',
   *   appKey: '', // sigmob、taku、tap 必填
   *   appName: '', // tap 有效, 选填, 媒体名称, 不传时使用原生端配置的 app_name
   *   appChannel: '', // tap 有效, 选填, 渠道（如果在 TapTap 上架填写 "taptap", 其它渠道上架填写商店拼音小写字母, eg. 小米 -> "xiaomi"）, 不传时使用 taptap
   *   appClientID: '', // tap 有效, 选填, TapTap 开发者中心的游戏 Client ID
   *   oaid: '', // iqy 有效, 选填, 可用 getOaid() 方法获取
   * }
   */
  public initAd(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('initAd', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 初始化短剧, 文档 https://www.csjplatform.com/supportcenter/28147, https://www.csjplatform.com/supportcenter/28439
   * * 在 '初始化广告' 之后使用
   * {
   *   provider: 'csj', // app 必填
   *   newUser: true, // 是否新用户, true 是（默认值）, false 不是
   *   teenagerMode: false, // 是否青少年模式, 青少年开启后不返回内容, true 是, false 不是（默认值）
   *   sdkSettingFile: '', // 配置文件（带文件后缀, 如 'SDK_Setting_xxxxxxx.json'）, 放在目录 /cocos-3.x/native/engine/android.multiple/assets/ 里
   * }
   */
  public initDjx(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('initDjx', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 短剧解锁记录绑定, 文档 https://www.csjplatform.com/supportcenter/28439
   * * 在 '初始化短剧' 之后使用
   * * 如果开发者有自己的账号体系, 需要实现解锁记录绑定功能, 否则用户解锁权益无法保障
   * * 如果开发者没有账号体系, 可以忽略
   * {
   *   provider: 'csj', // app 必填
   *   sign: '', // 签名; 或者 uid + serverKey; 两种方式二选一
   *   uid: '', // 账号id, 用于计算签名
   *   serverKey: '', // 密钥, 用于计算签名
   * }
   */
  public loginDjx(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('loginDjx', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 短剧解锁记录解绑, 文档 https://www.csjplatform.com/supportcenter/28439
   * * 与 '短剧解锁记录绑定' 对应
   * {
   *   provider: 'csj', // app 必填
   * }
   */
  public logoutDjx(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('logoutDjx', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 打开短剧, 文档 https://www.csjplatform.com/supportcenter/28148, https://www.csjplatform.com/supportcenter/28151
   * * 在 '初始化短剧' 之后使用
   * {
   *   provider: 'csj', // app 必填
   *   mode: 'home', // 'home' 短剧聚合页（默认值）
   *   freeSet: 1, // 短剧免费集数, 默认值 1
   *   lockSet: false, // 短剧广告解锁集数, 默认值 1
   * }
   */
  public openDjx(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('openDjx', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 初始化短故事, 文档 https://www.csjplatform.com/supportcenter/28147
   * * 在 '初始化广告' 之后使用
   * {
   *   provider: 'csj', // app 必填
   *   newUser: true, // 是否新用户, true 是（默认值）, false 不是
   *   teenagerMode: false, // 是否青少年模式, 青少年开启后不返回内容, true 是, false 不是（默认值）
   *   sdkSettingFile: '', // 配置文件（带文件后缀, 如 'SDK_Setting_xxxxxxx.json'）, 放在目录 /cocos-3.x/native/engine/android.multiple/assets/ 里
   * }
   */
  public initNov(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('initNov', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 短故事阅读记录绑定, 文档 https://www.csjplatform.com/supportcenter/28440
   * * 在 '初始化短故事' 之后使用
   * * 如果开发者有自己的账号体系, 需要实现解锁记录绑定功能, 否则用户解锁权益无法保障
   * * 如果开发者没有账号体系, 可以忽略
   * {
   *   provider: 'csj', // app 必填
   *   sign: '', // 签名; 或者 uid + serverKey; 两种方式二选一
   *   uid: '', // 账号id, 用于计算签名
   *   serverKey: '', // 密钥, 用于计算签名
   * }
   */
  public loginNov(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('loginNov', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 短故事阅读记录解绑, 文档 https://www.csjplatform.com/supportcenter/28440
   * * 与 '短故事阅读记录绑定' 对应
   * {
   *   provider: 'csj', // app 必填
   * }
   */
  public logoutNov(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('logoutNov', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 打开短故事, 文档 https://www.csjplatform.com/supportcenter/28349, https://www.csjplatform.com/supportcenter/28350
   * * 在 '初始化短故事' 之后使用
   * {
   *   provider: 'csj', // app 必填
   *   mode: 'home', // 'home' 短故事聚合页（默认值、固定值）
   *   endPageRecSize: 3, // 文末推荐页推荐个数, 默认值 3
   *   pageTurnMode: 'TURN_LEFT_RIGHT_SIMULATE', // 阅读器默认翻页模式,
   *                                             // 'TURN_LEFT_RIGHT_TRANSLATE' 平移（左右）,
   *                                             // 'TURN_LEFT_RIGHT_SIMULATE' 仿真（默认值）,
   *                                             // 'TURN_LEFT_RIGHT_SLIP' 覆盖,
   *                                             // 'TURN_UP_DOWN' 上下（滑动）
   * }
   */
  public openNov(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('openNov', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 开屏广告
   * {
   *   provider: 'adset' | 'bd' | 'csj' | 'gdt' | 'iqy' | 'ks' | 'oppo' | 'sigmob' | 'sigmobMill' | 'taku' | 'tap', // app 必填
   *   unitId: '',
   *   userId: '', // tap 有效, 选填
   * }
   */
  public createSplashAd(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('splashAd', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 信息流广告（模板渲染）
   * {
   *   provider: 'adset' | 'bd' | 'csj' | 'gdt' | 'ks', // app 必填
   *   unitId: '',
   *   style: {
   *     left: 0, // 注意: 是 <左右> 边距
   *     bottom: 0, // 底边距
   *   }
   * }
   */
  public createFeedAd(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('feedAd', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 横幅广告
   * {
   *   provider: 'adset' | 'csj' | 'gdt' | 'iqy' | 'ks' | 'oppo' | 'sigmobMill' | 'taku' | 'tap', // app 必填
   *   unitId: '',
   *   userId: '', // tap 有效, 选填
   *   style: {
   *     top: 0, // app 'adset' | 'csj' | 'gdt' | 'iqy' | 'ks' | 'oppo' | 'sigmobMill' | 'taku' | 'tap' 有效, 选填, top>0是屏幕顶部间距, <=0是屏幕底部间距, 默认top=0
   *     left: 0,
   *     width: 0,
   *     height: 0, // app 'csj' | 'gdt' | 'ks' | 'oppo' | 'sigmobMill' | 'taku' 有效, 选填, 默认（screenWidth / 6.4）
   *   }
   * }
   */
  public createBannerAd(data?: any) {
    let event = 'bannerAd';
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative(event, data);
    } else if (this.isBytedanceGame()) {
      if (!data.unitId) {
        if (data.success) data.success();
      } else {
        if (this.bannerAd != null) this.bannerAd.destroy();

        let style = {
          left: data?.style?.left ?? 0,
          top: data?.style?.top ?? 0,
          width: data?.style?.width ?? this.windowInfo?.windowWidth ?? 750,
        };
        this.bannerAd = tt.createBannerAd({ adUnitId: data.unitId, adIntervals: 30, style: style });
        this.bannerAd.onError(this.onError.bind(this, event));

        this.setCallback(event, { success: data?.success, rewarded: data?.rewarded, fail: data?.fail });

        this.bannerAd.show();
      }
    } else if (this.isWechatGame()) {
      if (!data.unitId) {
        if (data?.success) data.success();
      } else {
        if (this.bannerAd != null) this.bannerAd.destroy();

        let style = {
          left: data?.style?.left ?? 0,
          top: data?.style?.top ?? 0,
          width: data?.style?.width ?? this.windowInfo?.windowWidth ?? 750,
        };
        this.bannerAd = wx.createBannerAd({ adUnitId: data.unitId, adIntervals: 30, style: style });
        this.bannerAd.onError(this.onError.bind(this, event));
        this.bannerAd.onResize(this.onBannerResize.bind(this));

        this.setCallback(event, { success: data?.success, rewarded: data?.rewarded, fail: data?.fail });

        this.bannerAd.show();
      }
    } else {
      if (data?.success) data.success();
    }
  }

  public hideBanner() {
    if (this.bannerAd != null) {
      this.bannerAd.hide();
      this.bannerAd.destroy();
    }
  }

  private onBannerResize(res: any) {
    if (this.windowInfo) {
      this.bannerAd.style.top = this.windowInfo.windowHeight - res.height;
    }
  }

  /**
   * 原生模板（格子）广告: 微信小游戏
   * {
   *   unitId: '',
   *   style: {
   *     top: 0,
   *     left: 0,
   *     width: 0,
   *   }
   * }
   */
  public createCustomAd(data?: any) {
    let event = 'customAd';
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative(event, data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
      if (!data.unitId) {
        if (data?.success) data.success();
      } else {
        if (this.customAd != null) {
          if (this.customAd.isShow()) return;
          this.customAd.destroy();
        }

        let style = {
          left: data?.style?.left ?? 35,
          top: data?.style?.top ?? 135,
          width: data?.style?.width ?? (this.windowInfo ? Math.floor(this.windowInfo.windowWidth * 0.7) : 300),
          fixed: data?.style?.fixed ?? true
        };
        this.customAd = wx.createCustomAd({ adUnitId: data.unitId, adIntervals: 30, style: style });
        this.customAd.onError(this.onError.bind(this, event));

        this.setCallback(event, { success: data?.success, rewarded: data?.rewarded, fail: data?.fail });

        this.customAd.show();
      }
    } else {
      if (data?.success) data.success();
    }
  }

  public hideCustom() {
    if (this.customAd != null) {
      this.customAd.hide();
      this.customAd.destroy();
    }
  }

  /**
   * 插屏广告
   * {
   *   provider: 'adset' | 'bd' | 'csj' | 'gdt' | 'iqy' | 'ks' | 'oppo' | 'sigmob' | 'sigmobMill' | 'taku' | 'tap', // app 必填
   *   unitId: '',
   *   userId: '', // tap 有效, 选填
   * }
   */
  public createInterstitialAd(data?: any) {
    let event = 'interstitialAd';
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative(event, data);
    } else if (this.isBytedanceGame()) {
      if (!data.unitId) {
        if (data?.success) data.success();
      } else {
        if (this.interstitialAd != null) this.interstitialAd.destroy();

        this.interstitialAd = tt.createInterstitialAd({ adUnitId: data.unitId });
        this.interstitialAd.onError(this.onError.bind(this, event));

        this.setCallback(event, { success: data?.success, rewarded: data?.rewarded, fail: data?.fail });

        this.interstitialAd.show();
      }
    } else if (this.isWechatGame()) {
      if (!data.unitId) {
        if (data?.success) data.success();
      } else {
        if (this.interstitialAd != null) this.interstitialAd.destroy();

        this.interstitialAd = wx.createInterstitialAd({ adUnitId: data.unitId });
        this.interstitialAd.onError(this.onError.bind(this, event));

        this.setCallback(event, { success: data?.success, rewarded: data?.rewarded, fail: data?.fail });

        this.interstitialAd.show();
      }
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 多个广告
   * {
   *   unitIds: [
   *     {
   *       provider: '', // app 必填
   *       unitId: '',
   *       userId: '', // tap （激励、插屏、横幅）有效, 选填
   *       event: '', // 'feedAd' 时 provider 支持 'adset' | 'bd' | 'csj' | 'gdt' | 'ks'
   *                  // 'bannerAd' 时 provider 支持 'adset' | 'csj' | 'gdt' | 'ks' | 'oppo' | 'taku' | 'tap'
   *                  // 'interstitialAd' 时 provider 支持 'adset' | 'bd' | 'csj' | 'gdt' | 'ks' | 'oppo' | 'sigmob' | 'taku' | 'tap'
   *                  // 'rewardedVideoAd' 时 provider 支持 'adset' | 'bd' | 'csj' | 'gdt' | 'ks' | 'oppo' | 'sigmob' | 'taku' | 'tap'
   *                  // 'drawAd' 时 provider 支持 'adset' | 'csj' | 'ks'
   *     }
   *   ]
   * }
   */
  public createMultipleAd(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('multipleAd', data);
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 激励视频广告
   * {
   *   provider: 'adset' | 'bd' | 'csj' | 'gdt' | 'iqy' | 'ks' | 'oppo' | 'sigmob' | 'sigmobMill' | 'taku' | 'tap', // app 必填
   *   unitId: '',
   *   userId: '', // tap 有效, 选填
   * }
   */
  public createRewardedVideoAd(data?: any) {
    let event = 'rewardedVideoAd';
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative(event, data);
    } else if (this.isBytedanceGame()) {
      if (!data.unitId) {
        if (data?.success) data.success();
      } else {
        if (this.rewardedVideoAd == null) {
          this.rewardedVideoAd = tt.createRewardedVideoAd({ adUnitId: data.unitId });
          this.rewardedVideoAd.onClose(this.onRewardedClose.bind(this));
          this.rewardedVideoAd.onError(this.onError.bind(this, event));
        }

        this.setCallback(event, { success: data?.success, rewarded: data?.rewarded, fail: data?.fail });

        this.rewardedVideoAd.show();
      }
    } else if (this.isWechatGame()) {
      if (!data.unitId) {
        if (data?.success) data.success();
      } else {
        if (this.rewardedVideoAd == null) {
          this.rewardedVideoAd = wx.createRewardedVideoAd({ adUnitId: data.unitId });
          this.rewardedVideoAd.onClose(this.onRewardedClose.bind(this));
          this.rewardedVideoAd.onError(this.onError.bind(this, event));
        }

        this.setCallback(event, { success: data?.success, rewarded: data?.rewarded, fail: data?.fail });

        this.rewardedVideoAd.show();
      }
    } else {
      if (data?.success) data.success();
    }
  }

  private onRewardedClose(res: any) {
    let event = 'rewardedVideoAd';
    if (res.isEnded && this.getCallback(event)?.success)
      this.getCallback(event).success();
    else if (this.getCallback(event)?.fail)
      this.getCallback(event).fail();
  }

  private onError(event: string, res: any) {
    if (this.getCallback(event)?.fail) this.callback.get(event).fail(res);
  }

  /**
   * draw信息流广告（模板渲染）
   * {
   *   provider: 'adset' | 'csj' | 'ks', // app 必填
   *   unitId: '',
   *   style: {
   *     bottom: 0, // bottom > 0, 则展示view上下居中（默认）, 展示view的高度是（screenHeight * 0.8）
   *                   bottom == 0, 则展示view在底部, 展示view的高度是（screenHeight * 0.9）
   *                   注: 由于有些广告没有关闭按钮, 无法关闭广告; 上下留出空白, 并设置点击空白关闭广告
   *   }
   * }
   */
  public createDrawAd(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('drawAd', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 防沉迷服务
   * {
   *   provider: 'oppo' | 'ysdk' | 'taptap' | 'hykb' | 'm4399',
   *   appId: '', // taptap、hykb、m4399 必填
   *   appToken: '', // taptap 必填
   *   appSecret: '', // oppo 必填
   *   login: false, // 'oppo' | 'ysdk' 有效, 选填, 初始化后立即登录
   * }
   */
  public initFcm(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('initFcm', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 火山引擎-设备安全
   * {
   *   appId: '',
   *   license: '',
   *   channel: '', // 选填, 默认值 'game'
   *   scene: '', // 选填, 默认值 'game'
   * }
   */
  public initVolcengine(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('initVolcengine', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 相册选图、相机拍照
   * {
   *   mediaType: 'Image' | 'Video' | 'ImageAndVideo',
   *   sourceType: 'album' | 'camera',
   *   useOss: true | false, // 是否使用云存储, 当前支持阿里云 OSS
   *
   *   ak: '', // AccessKey ID, useOss为true时必填
   *   sk: '', // AccessKey Secret, useOss为true时必填
   *   endpoint: '', // 地域节点, useOss为true时必填
   *   bucket: '', // 存储空间, useOss为true时必填
   *
   *   url: '', // 图片上传地址, useOss为false时必填
   *   fileKey: '', // fileImg
   *   tokenKey: '', // Authorization
   *   token: '', // authorization, useOss为false时必填
   * }
   */
  public chooseMedia(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('chooseMedia', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 保存图片
   * {
   *   image: '', // 图片地址, 或者base64
   * }
   */
  public saveImage(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('saveImage', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 拨打电话
   * {
   *   phoneNumber: '', // 需要拨打的电话号码
   *   action: 'dial' | 'call', // 'dial' 拨号（默认值） | 'call' 呼叫
   * }
   */
  public makePhoneCall(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('makePhoneCall', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 扫码
   * {
   *   beep: true, // 是否播放声音, 默认为true
   *   shake: true, // 是否震动 , 默认为true
   *   bar: true, // 是否扫描条形码, 默认为true
   *   full: true, // 是否全屏扫描, 默认为true, 设为false则只会在扫描框中扫描
   *   album: true, // 是否显示相册, 默认为true
   *   bottom: true, // 是否显示下方的其他功能（闪光灯（设备支持就显示, 否则不显示）、相册）, 默认为true
   * }
   */
  public scanCode(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('scanCode');
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 显示二维码
   * {
   *   href: '', // 文本内容, 必填
   * }
   */
  public showQRCode(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('showQRCode', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 复制到剪贴板
   * {
   *   label: '', // 标签
   *   text: '', // 文本内容, 必填
   * }
   */
  public setClipboardData(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('setClipboardData', data);
    } else if (this.isBytedanceGame()) {
      if (data?.text) {
        tt.setClipboardData({
          data: data.text,
          success: (res: any) => {
            if (data?.success) data.success(res);
          },
          fail: (res: any) => {
            if (data?.fail) data.fail(res);
          },
        });
      }
    } else if (this.isWechatGame()) {
      if (data?.text) {
        wx.setClipboardData({
          data: data.text,
          success: (res: any) => {
            if (data?.success) data.success(res);
          },
          fail: (res: any) => {
            if (data?.fail) data.fail(res);
          },
        });
      }
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 从剪贴板读取
   */
  public getClipboardData(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('getClipboardData');
    } else if (this.isBytedanceGame()) {
      if (data?.text) {
        tt.getClipboardData({
          success: (res: any) => {
            if (data?.success) data.success(res?.data ?? '');
          },
          fail: (res: any) => {
            if (data?.fail) data.fail(res);
          },
        });
      }
    } else if (this.isWechatGame()) {
      if (data?.text) {
        wx.getClipboardData({
          success: (res: any) => {
            if (data?.success) data.success(res?.data ?? '');
          },
          fail: (res: any) => {
            if (data?.fail) data.fail(res);
          },
        });
      }
    } else {
      if (data?.success) data.success();
    }
  }

  /**
   * 打开网页 WebView
   * {
   *   external: false, // true 使用外部浏览器打开
   *   blank: false, // false 弹窗 | true 独立窗口
   *   title: '', // 网页/窗口标题, 可为空, blank: true 时有效
   *   url: '', // 网页地址
   * }
   */
  public openWebView(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('openWebView', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 跳转 APP
   * {
   *   scheme: '',
   * }
   */
  public launchApp(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('launchApp', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 退出应用
   */
  public exit() {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('exit');
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 显示消息提示框
   * {
   *   text: '',
   * }
   */
  public showToast(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('showToast', data);
    } else if (this.isBytedanceGame()) {
      if (data?.text) {
        tt.showToast({
          title: data.text,
          icon: 'none',
        });
      }
    } else if (this.isWechatGame()) {
      if (data?.text) {
        wx.showToast({
          title: data.text,
          icon: 'none',
        });
      }
    } else {
    }
  }

  /**
   * 获取手机 OAID
   */
  public getOaid(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('getOaid', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 获取手机 AndroidID
   */
  public getAndroidID(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('getAndroidID', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 获取手机品牌
   */
  public getBrand(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('getBrand', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 获取手机制造商
   */
  public getManufacturer(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('getManufacturer', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 获取手机型号
   */
  public getModel(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('getModel', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 是否运行在模拟器中
   */
  public isRunningInEmulator(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('isEmulator', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 是否开启 无障碍服务
   */
  public isAccessibilityServiceEnabled(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('isAccessibilityServiceEnabled', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 是否开启 开发者模式
   */
  public isDeveloperModeEnabled(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('isDeveloperModeEnabled', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 获取应用包名
   */
  public getPackageName(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('getPackageName', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 获取设备信息
   * package: 当前应用的包名,
   * versionCode: 当前应用的版本号,
   * versionName: 当前应用的版本名,
   * manufacturer: 设备制造商,
   * model: 设备型号,
   * brand: 设备品牌,
   * product: 产品名称,
   * uiOsVersion: 系统版本,
   * apiVersion: SDK版本号,
   * cpu: cpu名字, // X
   * baseBand: 基带版本,
   * pixels: 设备分辨率,
   * dpi: 屏幕密度,
   * tablet: 是否平板,
   * androidVersion: 安卓版本号,
   * androidId: 安卓ID,
   * oaid: 安卓OAID,
   * mac: mac地址, // 可能是虚拟的
   * sim: 是否有手机卡以及手机号,
   * location: 用户位置坐标, // X
   * emulator: 是否模拟器,
   * accessibility: 是否开启无障碍服务,
   * developer: 是否开启开发者模式,
   * userAgent: UserAgent,
   * <br>
   * {"package":"com.yourong.game.***","versionCode":1,"versionName":"1.0.1","manufacturer":"HUAWEI","model":"CLT-AL00",
   * "brand":"HUAWEI","product":"CLT-AL00","uiOsVersion":"EMUI EmotionUI_9.1.0","apiVersion":"28","cpu":"",
   * "baseBand":"21C20B369S009C000,21C20B369S009C000","pixels":"1080:2037","dpi":"408","tablet":"false",
   * "androidVersion":"9","androidId":"d116b44ae814d0ff","oaid":"2f9fcf7f-fe7f-c9c6-ff75-dff6dd718ad6",
   * "mac":"02:00:00:00:00:00","sim":"false","location":"","emulator":"false","accessibility":"false","developer":"true",
   * "userAgent":"Mozilla/5.0 (Linux; Android 9; CLT-AL00 Build/HUAWEICLT-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36"}
   */
  public getPhone(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('getPhone', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 爱奇艺奇麟
   * 文档: https://tuiguang.iqiyi.com/platform/portal/help#9000004
   * {
   *   event: 'init' | 'trans', // 二选一, 必填: 初始化 | 上报行为数据
   *   appId: '', // init 必填
   *   channelId: '', // init 选填
   *   oaid: '', // init 选填
   *
   *   type: 'register' | 'login' | 'logout' | 'share' | 'search', // trans 必填, 预定义的行为, 更多选项参考文档
   *   param: {
   *     account: '', // 选填
   *     content: '', // 选填
   *   } // trans 选填, 更多选项参考文档
   * }
   */
  public iqyQiLin(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('iqyQiLin', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }

  /**
   * 获取网络类型
   * success 回调函数 参数:
   * {
   *   networkType: string, // 'none' | 'unknown' | 'wifi' | '5g' | '4g' | '3g' | '2g'
   * }
   */
  public getNetworkType(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('getNetworkType', data);
    } else if (this.isBytedanceGame()) {
      tt.getNetworkType({
        success: (res: any) => {
          if (data?.success) data.success(res);
        },
        fail: (res: any) => {
          if (data?.fail) data.fail(res);
        },
      })
    } else if (this.isWechatGame()) {
      wx.getNetworkType({
        success: (res: any) => {
          if (data?.success) data.success(res);
        },
        fail: (res: any) => {
          if (data?.fail) data.fail(res);
        },
      })
    } else {
    }
  }

  /**
   * 语音识别
   * 腾讯云文档, 可查看详细参数 https://cloud.tencent.com/document/product/1093/48982
   *
   * {
   *   provider: 'tencent',
   *   action: 'start' | 'stop',
   *   appId: 0, // 注意是数字, action为start时必填
   *   projectId: 0, // 固定为 0, action为start时必填
   *   secretId: '', // action为start时必填
   *   secretKey: '', // action为start时必填
   *   engineModelType: '16k_zh', // 引擎模型类型
   *   filterDirty: 0, // 0:默认状态, 不过滤脏话; 1:过滤脏话
   *   filterModal: 0, // 0:默认状态, 不过滤语气词; 1:过滤部分语气词; 2:严格过滤
   *   filterPunc: 0, // 0:默认状态, 不过滤句末的句号; 1:滤句末的句号
   *   convertNumMode: 1, // 1:默认状态, 根据场景智能转换为阿拉伯数字; 0:全部转为中文数字
   *   needvad: 0, // 0:关闭 vad; 1:默认状态 开启 vad, 语音时长超过一分钟需要开启, 如果对实时性要求较高, 并且时间较短的输入, 建议关闭
   *
   *   success 回调函数 参数:
   *   {
   *     state: 'start' | 'stop' | 'slice' | 'segment' | 'success' | 'fail', // 开始识别 | 停止识别 | 分片识别 | 语音流识别 | 识别完成 | 识别失败
   *     text: '', // state为'slice'是中间态结果, 会被持续修正
   *               // state为'segment'是稳定态结果, 可做为识别结果用与业务
   *               // state为'success'是所有的识别结果
   *               // state为'fail'是失败信息
   *   }
   * }
   */
  public asrRealtime(data?: any) {
    if (this.isAndroid() || this.isIos()) {
      this.sendToNative('asrRealtime', data);
    } else if (this.isBytedanceGame()) {
    } else if (this.isWechatGame()) {
    } else {
    }
  }
}