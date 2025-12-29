import { Engine } from "../Engine";
import { Jsb } from "./JSB";
import { JsbWXMinniGame } from "./JsbWXMinniGame";

export enum PlatformType {
    None,

    Android,

    /**
     * oppo小游戏
     */
    OppoMinGame,

    /**
     * 头条小游戏
     */
    TTMinGame,

    /**
     * 百度小游戏
     */
    BaiDuMinGame,

    /**
     * vivo 小游戏
     */
    VivoMinGame,

    QQMinGame,

    WXMinGame,

    KuaiShou,

    BGo,

    /**
     * 京东
     */
    JD,
    /**
     * 蝴蝶京东渠道
     */
    HuDieJD,
}


export class JsbManager {
    Jsb: Jsb;

    platform: PlatformType;

    init(platform: PlatformType) {
        this.platform = platform;
        let jsb = new Jsb();
        if (this.platform == PlatformType.WXMinGame) {
            jsb = new JsbWXMinniGame();
        }

        jsb.init();
        this.Jsb = jsb;
    }

    isWeChat() {
        return this.platform == PlatformType.WXMinGame;
    }

    isTT() {
        return this.platform == PlatformType.TTMinGame;
    }

    isAndroid() {
        return this.platform == PlatformType.Android;
    }

    isKuaiShou() {
        return this.platform == PlatformType.KuaiShou;
    }

    isGo() {
        return this.platform == PlatformType.BGo;
    }

    isBai() {
        return this.platform == PlatformType.BaiDuMinGame;
    }

    share() {
        return new Promise((resolve, reject) => {
            // Engine.gui.show(ViewID.UIBlockInput);
            Engine.instance.showMask(true);
            this.Jsb.share().then(() => {
                Engine.instance.hideMask();
                resolve(1);
            }).catch(() => {
            })
        })
    }

    completeLevel(level: number, maxLevel: number) {
        this.Jsb.completeLevel(level, maxLevel);
    }

    showRankView(key: string) {
        this.Jsb.showRankView(key);
    }

    playRewardVideo(adid: string, params: any) {
        return new Promise((resolve, reject) => {
            Engine.instance.showMask(true);

            this.Jsb.playRewardVideo(adid, params).then(() => {
                resolve(1);
                Engine.instance.hideMask();
            }).catch((msg) => {
                Engine.instance.hideMask();
            });
        });
    }
}


