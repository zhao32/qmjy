import { _decorator, Component, Node } from 'cc';
import { Engine } from './Engine';
import { SwitchType } from './jsb/JSB';
import HttpManager from '../login/scripts/HttpManager';
const { ccclass, property } = _decorator;

@ccclass('UITimeOut')
export class UITimeOut extends Component {


    onAdClick() {
        // Engine.instance.jsb.playRewardVideo("", {}).then((code: SwitchType) => {
        //     if (code == SwitchType.On) {
        //         Engine.instance.revive();
        //         this.node.destroy();
        //     }
        // })

        let self = this
        HttpManager.createMultipleAd(() => { self.doAd() })
    }

    doAd() {
        Engine.instance.revive();
        this.node.destroy();
    }

    onCloseClick() {
        Engine.instance.refreshLevel();
        this.node.destroy();
    }
}


