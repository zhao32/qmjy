
export enum HttpType {
    POST,   //post
    GET,        //get
}

export default class NetMsg {

    /**登录 */
    static login = { url: "index.php//api/login/allLogin", type: HttpType.POST, isToken: false };

    /**广告商列表 */
    static getGS = { url: "index.php//api/Advertisement/gs", type: HttpType.POST, isToken: false };

    /**获取所有广告位 */
    static getAllAdInfo = { url: "index.php//api/Advertisement/selectYymd", type: HttpType.POST, isToken: true };

    /**观看广告领积分 */
    static collectPacket = { url: "index.php//api/Advertisement/collectPacket", type: HttpType.POST, isToken: true };
    /**游戏帮助 */
    static getHelpTxt = { url: "index.php//api/game/getGameplay", type: HttpType.POST, isToken: true };

}
