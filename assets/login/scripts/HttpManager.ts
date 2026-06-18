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
    /**插屏广告 */
    static showInterstAdMulti() {
        if (sys.platform != sys.Platform.ANDROID) return
        this.adInfos = [];
        this.InterstialAdInfo = []

        this.getAllAdInfo(2, (res) => {
            if (res && res.code == 1) {
                let array = res.data;
                console.log("数据广告信息", JSON.stringify(res.data));
                if (array.length) {
                    array.forEach(element => {
                        this.adInfos.push(element)
                        this.InterstialAdInfo.push({
                            provider: element.as == "pangle" ? 'csj' : element.as,
                            unitId: element.rewarded,
                            event: 'interstitialAd'
                        })
                    });
                }

                if (this.InterstialAdInfo.length == 0) {
                    //   if (data?.fail) data.fail();
                    console.log('没有广告位')
                    return;
                }

                yr.inst.createMultipleAd({
                    unitIds: this.InterstialAdInfo,
                    event: "interstitialAd",

                    success: (resp) => {
                        console.log('--插屏广告加载成功', resp);
                        // resp: 1 激励广告加载成功, 这里是加载成功, 不代表显示成功; 是否获得奖励在 rewarded 回调
                    },
                    showAd: (resp) => {
                        console.log('network 插屏看广告成功  ', typeof resp, resp);
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
                        console.log('------插屏广告获取奖励');
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
        if (sys.platform != sys.Platform.ANDROID) {
            callback && callback();
            return
        }
        this.playAd(callback)
    }

    /**播放广告 */
    private static playAd(callback?) {

        this.adInfos = [];
        this.rewardInfos = [];
        console.log("开始观看广告");
        this.getAllAdInfo(1, (res) => {
            if (res && res.code == 1) {
                let array = res.data;
                console.log("数据广告信息", JSON.stringify(res.data));
                if (array.length) {
                    array.forEach(element => {
                        this.adInfos.push(element)
                        this.rewardInfos.push({
                            provider: element.as == "pangle" ? 'csj' : element.as,
                            unitId: element.rewarded,
                            event: 'rewardedVideoAd'
                        })
                    });
                }
                if (this.rewardInfos.length == 0) {
                    // if (data?.fail) data.fail();
                    console.log('没有广告位')
                    return;
                }
                yr.inst.createMultipleAd({
                    unitIds: this.rewardInfos.sort(() => { return Math.random() > 0.5 ? 1 : -1 }),
                    success: (resp) => {
                        console.log('network success  ', typeof resp, resp);
                        // if (data?.success) data.success(resp);
                    },
                    rewarded: (resp) => {
                        // game.resume()
                        // resp: 1 激励广告获得奖励
                        console.log('network 看广告成功  ', typeof resp, resp);
                        // if (data?.rewarded) data.rewarded(resp);

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
                                this.postCollectPacketOne(ecpm, type, ilk, id, target_money, adv_no);
                            }
                            callback && callback();
                        }
                    },
                    fail: () => {
                        console.log('network fail  ');
                        // if (data?.fail) data.fail("广告播放失败");
                    }
                })
            }
            else {
                if (res && res.msg) {
                    // data.fail(res.msg);
                    console.log(res.msg);
                }
                else {
                    // data.fail();
                    console.log("fail");

                }
            }
        })
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
