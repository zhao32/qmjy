import { Jsb, SwitchType } from "./JSB";

export class JsbWXMinniGame extends Jsb {

    // public BannerId: string = 'adunit-c77c92ae21b66732';

    public RewardedVideoId: string = 'adunit-e9e0c772095ace53';
    public InterstitialAdId: string = 'adunit-312e915e1fa555f8';
    // public BoxId: string = "94a2c425b62602e8426efe960f39e4f4";

    public bannerAd: any;

    public videoAd: any;

    public InsertErrCount: number = 0;

    public isCachedVideo: boolean = false;

    // public appId: string = "wx5d423efcca659bd7";

    public init() {


        wx.showShareMenu({
            withShareTicket: true
        });

        wx.onShow(this.ReturnToGame.bind(this));


        wx.showShareMenu({
            withShareTicket: true,
            menus: ['shareAppMessage', 'shareTimeline']
        })

        let opt = wx.getLaunchOptionsSync();
        this.sceneToGame(opt.scene);
    }

    settingResult: WechatMinigame.GetSettingSuccessCallbackResult;

    public initAd(): void {
        // 接口上报示例代码，当基础库版本>=2.26.2才能使用此能力
        wx.reportScene({
            sceneId: 7,  //「必填」sceneId 为「新建场景」后，由系统生成的场景 Id 值，用于区分当前是哪个启动场景的数据
            success(res) {
                // 上报接口执行完成后的回调，用于检查上报数据是否符合预期，也可通过启动调试能力进行验证
                console.log(res);
            },
            fail(res) {
                // 上报报错时的回调，用于查看上报错误的原因：如参数类型错误等
                console.log(res);
            },
        });
    }

    public login() {
        return new Promise((resolve, reject) => {
            wx.login({
                success: (res) => {
                    if (res.code) {
                        console.log('登录成功！' + res.code);
                        resolve(res.code);
                    } else {
                        console.log('登录失败！' + res.errMsg);
                        reject(res.errMsg);
                    }
                },
                fail: (err) => {
                    console.log('登录失败！' + err.errMsg);
                    reject(err.errMsg);
                }
            });
        });
    }

    /** 返回游戏回调*/
    ReturnToGame(result: WechatMinigame.OnShowListenerResult) {
        this.sceneToGame(result.scene);
    }

    sceneToGame(scene: number) {
        console.log("sceneToGame" + scene);
    }

    /** 获取当前时间戳*/
    timestamp() {
        let myDate = new Date();					//当前时间
        let _timestamp = myDate.getTime();
        return _timestamp;
    }

    /**
     * 使手机发生较长时间的振动
     */
    public openVibrateLong() {

        wx.vibrateLong({ /**
                    * 接口调用成功的回调函数
                    */
            success: () => {

            },
            /**
             * 接口调用失败的回调函数
             */
            fail: () => void {

            },
            /**
             * 接口调用结束的回调函数（调用成功、失败都会执行）
             */
            complete: () => void {

            }
        });
        // }
        // }

    }

    /**
     * 使手机发生较短时间的振动。 某些机型在不支持短振动时会
     */
    public openVibrateShort() {
        // let key = localStorage.getItem('shake');
        // if (key && key != "") {
        // let flag = parseInt(key) == 1;
        // if (flag) {
        wx.vibrateShort({
            type: 'heavy',
            /**
            * 接口调用成功的回调函数
            */
            success: () => {

            },
            /**
             * 接口调用失败的回调函数
             */
            fail: () => void {

            },
            /**
             * 接口调用结束的回调函数（调用成功、失败都会执行）
             */
            complete: () => void {

            }
        })
        // }
        // }
    }

    public openBanner() {
    }

    public clearBanner() {
    }

    videoSuccessCallBack: Function;
    videoFailCallBack: Function;
    public playRewardVideo(adid: string, params: any) {
        return new Promise((resolve, reject) => {
            this.videoSuccessCallBack = resolve;
            this.videoFailCallBack = reject;
            let video = wx.createRewardedVideoAd({ adUnitId: this.RewardedVideoId });
            let self = this;

            if (this.videoAd == null) {
                this.videoAd = video;
                video.onClose((res) => {
                    if (res.isEnded) {
                        console.log("发放奖励");
                        this.videoSuccessCallBack(1);
                    } else {
                        this.videoFailCallBack("完整观看视频才可以获得奖励哦！");
                    }
                })

                video.onError((res) => {
                    this.videoFailCallBack("广告加载失败 code=" + res.errCode + "  msg=" + res.errMsg);
                });
            }

            video.load().then((v) => {
                console.log("加载 成功----------------- ");
                video.show().catch((res) => {
                    this.videoFailCallBack("广告加载失败 code=" + res.errCode + "  msg=" + res.errMsg);
                })
            }).catch((msg) => {
                console.log(msg);
            })

            // this.videoFailCallBack("奖励正在准备中，请稍后再试");

        });
    }


    instertCount: number = 0;

    public showInstertView(adid: string) {
        console.log("显示插屏")
        // this.instertCount++;
        // if (this.instertCount % 5 == 0) {
        //     // 定义插屏广告
        //     let interstitialAd = null

        //     // 创建插屏广告实例，提前初始化
        //     if (wx.createInterstitialAd) {
        //         interstitialAd = wx.createInterstitialAd({
        //             adUnitId: this.InterstitialAdId
        //         })
        //     }

        //     // 在适合的场景显示插屏广告
        //     if (interstitialAd) {
        //         interstitialAd.show().catch((err) => {
        //             console.error('插屏广告显示失败', err)
        //         })
        //     }
        // }

    }

    public getIsCachedVideo() {
        return true;
    }

    hideBannder() {
        // console.log("广告隐藏")
        if (this.bannerAd) {
            this.bannerAd.hide()
        }
    }

    showBannder() {
        // console.log("广告显示")
        if (this.bannerAd) {
            this.bannerAd.show()
        }
    }

    shareTime: number;
    public share() {
        return new Promise((resolve, reject) => {
            this.shareTime = new Date().getTime();

            wx.shareAppMessage({

            });

            setTimeout(() => {
                resolve(1);
            }, 3000);
        });
    }


    completeLevel(level: number, maxLevel: number) {
        console.log("completeLevel");
        wx.setUserCloudStorage({ //调用微信接口上报关卡等级信息，用于好友圈排行
            KVDataList: [
                { key: 'level', value: `${level}` }
            ],

            success: () => {
                //listener?.apply(target);
                // let openContext = wx.getOpenDataContext(); // 调用微信接口获取子域句柄，使用时需要检查
                // openContext.postMessage({ type: 'engine', event: 'level' }); // level为自定义key，如果没有特殊需求，建议直接用。否则你的变动比较大，调整wx-sub-project/index.js的对应的key和this._reportUserLevel的key都需要对齐
                console.log("report level success" + level);
            },

            fail: (err: any) => {
                console.log('report level error:', err);
            }
        });
        // }
    }


    public showRankView(key: string) {
        let openContext = wx.getOpenDataContext(); // 调用微信接口获取子域句柄，使用时需要检查
        if (openContext) {
            openContext.postMessage({ type: 'engine', event: key }); // level为自定义key，如果没有特殊需求，建议直接用。否则你的变动比较大，调整wx-sub-project/index.js的对应的key和this._reportUserLevel的key都需要对齐
        }
        // console.log("showRankView " + key);
    }

    /**
     * 是否可以添加入口
     * @returns 
     */
    isBtnInto() {
        return false;
    }



    public showToast(msg: string) {
        wx.showToast({
            title: msg,
            icon: 'none',
            duration: 2000
        });
    }

}