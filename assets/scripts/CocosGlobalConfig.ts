
export class GlobalConfig {

    static maxLevel: number = 300;
    static DB:DBUser;
}

export enum StatusCode {
    /**
     * 失败
     */
    Fail,
    /**
     * 成功
     */
    Success,
}


export class DBUser {
    public accid: string;
    public nickName: string;
    public openid:string;
    /**
     * 头像
     */
    public avatarUrl: string;
    /**
     * 金币
     */
    public coin: number;
    /**
     * 当前关卡
     */
    public level: number;


}