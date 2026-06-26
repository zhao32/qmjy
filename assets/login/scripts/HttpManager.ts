import { sys } from "cc";
import HttpUtils from "./HttpUtils";
import NetMsg from "./MsgCfg";
// import Cryptojs from 'crypto-js';
import Cryptojs from 'crypto-js';
import { yr } from "./yr/yr";


export default class HttpManager {
    static game_id = 7;
    private static ads = [];
    private static adInfos = [];
    private static rewardInfos = [];

    private static SplashAdInfo = [];
    private static InterstialAdInfo = [];
    private static index = 0;

    static nickname = "";
    static headUrl = "";
    /**登录 */
    static login(phone, password, callBack) {
        let data = {
            type: "loginSn",
            account: phone,
            code: password,
        };

        HttpUtils.httpPost(NetMsg.login, data, (res) => {
            callBack && callBack(res);
            if (res && res.code == 1) {
                let token = res.data.token;
                HttpUtils.token = token;
                console.log("数据登录成功", token, JSON.stringify(res.data));
                sys.localStorage.setItem("token_jydhm", token);
                this.postGS();
            }
        });
    }

    static getHelp(callBack) {
        let data = {
            game_id: this.game_id,
        };

        HttpUtils.httpPost(NetMsg.getHelpTxt, data, (res) => {
            callBack && callBack(res);
        });
    }

    /**广告商信息初始化 */
    static postGS(callBack?) {
        this.ads = [];
        let data = {
            game_id: this.game_id,
        };

        HttpUtils.httpPost(NetMsg.getGS, data, (res) => {
            if (res && res.code == 1) {
                if (res.data == null) res.data = [];
                this.ads = res.data;
                console.log("数据广告商", JSON.stringify(res.data));
                this.index = 0;
                this.initAd();
            }
        });
    }

    /**初始化广告 */
    private static initAd() {
        if (this.index >= this.ads.length) {
            console.log("---初始化广告结束,播放开屏---");
            HttpManager.showSplashAdMulti()
            return;
        }

        const as = this.ads[this.index].as;
        const appid = this.ads[this.index].appid;
        yr.inst.initAd({
            provider: as == 'pangle' ? 'csj' : as,
            appId: appid,
            success: (resp) => {

                this.index++;
                this.initAd();

                console.log(as + '  初始化广告  ', resp);
            },
            fail: () => {
                console.log(as + '  初始化失败  ');
            }
        });

    }



    /** 开屏广告 */
    static showSplashAdMulti() {

        if (sys.platform != sys.Platform.ANDROID) return
        this.adInfos = [];
        this.SplashAdInfo = []
        this.getAllAdInfo(4, (res) => {
            if (res && res.code == 1) {
                let array = res.data;
                console.log("数据广告信息", JSON.stringify(res.data));
                if (array.length) {
                    array.forEach(element => {
                        this.adInfos.push(element)
                        this.SplashAdInfo.push({
                            provider: element.as == "pangle" ? 'csj' : element.as,
                            unitId: element.rewarded,
                            event: 'splashAd'
                        })
                    });
                }

                if (this.SplashAdInfo.length == 0) {
                    //   if (data?.fail) data.fail();
                    console.log('没有广告位')
                    return;
                }

                yr.inst.createMultipleAd({
                    unitIds: this.SplashAdInfo,
                    event: "splashAd",

                    success: (resp) => {
                        console.log('--开屏广告加载成功', resp);
                        // resp: 1 激励广告加载成功, 这里是加载成功, 不代表显示成功; 是否获得奖励在 rewarded 回调
                    },
                    showAd: (resp) => {
                        console.log('network 开屏看广告成功  ', typeof resp, resp);
                        let adv = JSON.parse(resp || '{}');
                        if (adv.adv_status) {
                            const ecpm = adv.adv_ecpm
                            const adv_id = adv.adv_id;
                            const adv_no = adv.adv_no;
                            const data = this.getOneAdInfo(adv_id);
                            if (data) {
                                const type = data.as;
                                const ilk = data.ilk;
                                const id = data.id;
                                const target_money = data.target_money;
                                if (ecpm) this.postCollectPacketOne(ecpm, type, ilk, id, target_money, adv_no);
                            }
                        }
                    },
                    rewarded: (resp) => {
                        console.log('------开屏广告获取奖励');
                    },
                    fail: (e) => {
                        // cc.audioEngine.resumeAll()
                    }
                })
            }
        })


    }


    /**获取广告位信息 */
    static getAllAdInfo(type, callBack?) {

        let data = {
            type: type,
            game_id: this.game_id
        }

        HttpUtils.httpPost(NetMsg.getAllAdInfo, data, callBack);
        // this.postCollectPacketOne("111", "", "", "", "123233", "");

    }
    /**播放广告列表 */
    static createMultipleAd(callback) {
        // if (Global.isCeshi) {
        //     let d = {adv_status: 1};
        //     let res = JSON.stringify(d);
        //     if (data?.rewarded) data.rewarded(res);
        //     return;
        // }
        if (sys.platform != sys.Platform.BYTEDANCE_MINI_GAME && sys.platform != sys.Platform.WECHAT_GAME) {
            callback && callback();
            return
        }
        this.playAd(callback)
    }


    static _adVideo

    static _lastError = 0
    /**播放广告 */
    private static playAd(data?) {

        // this.adInfos = [];
        // this.rewardInfos = [];
        // console.log("开始观看广告");

        // @ts-ignore
        this._adVideo = wx.createRewardedVideoAd({ adUnitId: "2300052341_01" });

        this._adVideo.onError((err: { errMsg: string, errCode: number }) => {
            this._lastError = err.errCode;

        });

        this._adVideo.onLoad(() => { this._lastError = 0; });
        this._adVideo.onClose((res) => {
            if ((res && res.isEnded) || res === undefined) {
                data && data();

                tt.request({
                    url: "https://box.tv888.cn/index.php//api/Advertisement/addKdLog",
                    data: {
                        type: "1",
                        game_id: this.game_id,
                        nickname: this.nickname,
                    },
                    header: {
                        "content-type": "application/json",
                        cookie: "ba-user-token" + localStorage.getItem("JYDHMTOKEN")

                    },
                    method: "POST",
                    dataType: "JSON", // 指定返回数据的类型为 json
                    responseType: "text",
                    success(res) {
                        console.log("调用成功", res.data)
                    },
                    fail(res) {
                        console.log("调用失败", res.errMsg)
                    },
                })
            }

        });
        this._adVideo.show().catch(() => {
            this._adVideo.load()
                .then(() => this._adVideo.show())
                .catch((err: { errMsg: string, errCode: number }) => {
                    this._lastError = err.errCode;
                });
        });
    }

    /**插屏广告 */
    static showInterstAdMulti() {
        if (sys.platform != sys.Platform.BYTEDANCE_MINI_GAME && sys.platform != sys.Platform.WECHAT_GAME) return
        // if (cc.sys.platform != cc.sys.ANDROID) return
        // this.adInfos = [];
        // this.InterstialAdInfo = []
        // @ts-ignore
        let interstitialAd = wx.createInterstitialAd({ adUnitId: "2300052341_02" });
        // interstitialAd.onLoad(() => {
        //     console.log("插屏 广告加载成功");
        // });
        interstitialAd.onError(err => {
            console.log(err);
        });
        interstitialAd.onClose(res => {
            console.log("插屏 广告关闭");
            tt.request({
                url: "https://box.tv888.cn/index.php//api/Advertisement/addKdLog",
                data: {
                    type: "2",
                    game_id: this.game_id,
                    nickname: this.nickname,

                },
                header: {
                    "content-type": "application/json",
                    cookie: "ba-user-token" + localStorage.getItem("JYDHMTOKEN")

                },
                method: "POST",
                dataType: "JSON", // 指定返回数据的类型为 json
                responseType: "text",
                success(res) {
                    console.log("调用成功", res.data)
                },
                fail(res) {
                    console.log("调用失败", res.errMsg)
                },
            })
        });



        interstitialAd.show().catch(err => {
            console.error(err);
        })

    }

    _bannerAd: any = null;
    public showBannerAd() {
        // if (!BYTEDANCE) return
        if (!this._bannerAd) {
            // @ts-ignore
            this._bannerAd = wx.createBannerAd({
                adUnitId: "fcfkgegbie51070nco",
                style: {
                    left: 0,
                    top: 0,
                    width: 320
                }
            });
        }


        this._bannerAd.show();
        // this._bannerAd.style.width = 400;
    }

    public hideBannerAd() {
        // if (!BYTEDANCE) return
        this._bannerAd && this._bannerAd.hide();
    }


    /**获得后台配置的该代码位的信息 */
    private static getOneAdInfo(id: string) {
        let array = this.adInfos;
        for (let i = 0; i < array.length; i++) {
            const element = array[i];
            if (element.rewarded == id) {
                return element;
            }
        }
        return null;
    }

    /**看广告领积分 */
    private static postCollectPacketOne(money, advertiser, ilk, yydmw_id, target_money, adv_no, callback?) {
        const thomasarmstrong = Math.floor(new Date().getTime() / 1000);
        let data = {
            money: money,
            advertiser: advertiser,
            ilk: ilk,
            yydmw_id: yydmw_id,
            target_money: target_money,
            thomasarmstrong: thomasarmstrong,
            adv_no: adv_no,
            game_id: this.game_id

        }

        let apr = JSON.stringify(data)

        const key = 'fktc12fdgerteGHdsfsdewred4345341';
        const iv = '4387438hfdhfdjhg';
        console.log('加密前  ', apr)
        var encrypt = Cryptojs.AES.encrypt(apr, Cryptojs.enc.Utf8.parse(key), {
            mode: Cryptojs.mode.ECB,
            padding: Cryptojs.pad.Pkcs7
        }).toString();
        console.log('加密后  ', encrypt);

        let jiami = {
            data: encrypt,
        }

        HttpUtils.httpPost(NetMsg.collectPacket, jiami, (res) => {
            if (res && res.code == 1) {
                console.log("数据增加积分成功", JSON.stringify(res.data));
            }
            callback && callback();
        });
    }

}
