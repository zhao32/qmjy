export class Jsb {
    public isPlayMusic: boolean = true;



    public init() {
    };

    public initAd() {

    }

    /**
     * 打开 开屏
     */
    public openSplashAd() {

    }

    public getIsCachedVideo(): boolean {
        return true;
    }

    public showRewardVideo() {
        // setTimeout(() => {
        //     Engine.event.dispatchEvent("AdvertisementVideo", SwitchType.On);
        // }, 1000);
    }

    /**
     * 使手机发生较短时间的振动。
     * 某些机型在不支持短振动时会
     */
    public openVibrateShort() {

    }

    /**
     * 使手机发生较长时间的振动。
     */
    public openVibrateLong() {

    }

    public openVibrate() {

    }

    /**
     * 发送到桌面
     */
    public sendDesktop(func: Function) {

    }

    public showInstertView(adid: string) {

    }

    public clearBanner() {

    }

    /**
     * 获取是否支持创建桌面快捷方式
     */
    public getIsDesktop() {
        return false;
    }

    public share() {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                resolve(1);
            }, 3000);
        })
    }


    completeLevel(level: number, maxLevel: number) {

    }


    public showRankView(key: string) {

    }

    public playRewardVideo(adid: string, params: any) {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                resolve(1);
            }, 100);
        })
    }

    reportAnalytics(eventName: string, data: any) {

    }

    public login(params: any): Promise<any> {
        return new Promise((resolve, reject) => {
            
            resolve(1);
        });
    }

    public showToast(msg: string) {

    }
   
    //#region  开关区域
    /**
     * 是否可以显示朋友
     * @returns 
     */
    isFriend() {
        return true;
    }

    /**
     * 是否可以添加入口
     * @returns 
     */
    isBtnInto() {
        return true;
    }

    /**
     *  是否可以添加到桌面
     * @returns 
     */
    isDestop() {
        return true;
    }

    /**
     * 是否开启城市
     * @returns 
     */
    isCity() {
        return true;
    }
    /**
     * 是否可以分享
     * @returns 
     */
    isShare() {
        return true
    }

    /**
     * 
     * @returns 
     */
    isCollect(){
        return true;
    }
    //#endregion    
}


export const enum AdvertType {
    None,

    /**
     * 原生信息流
     */
    NativeMsgFlow,

    /**
     * banner 广告
     */
    Banner,

    /**
     * 插屏广告
     */
    TableScreen,

    /**
     * 原生广告
     */
    Native,

    /**
     * 激励视频广告
     */
    ExcitationVideo,

    /**
     * 全屏视频广告
     */
    FullScreenVideo,

    /**
     * Draw竖版视频信息流广告
     */
    DrawFeedVideo,

    /**
     * 开屏广告
     */
    OpenScreen,

    HideBanner,

    NativeInsert,

    BoxAd
}


export const enum SwitchType {
    None,
    /**
     * 开
     */
    On,
    /**
     * 关
     */
    Off
}