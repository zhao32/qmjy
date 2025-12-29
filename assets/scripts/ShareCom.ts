import { _decorator, Component, Node } from 'cc';
import { Engine } from './Engine';
const { ccclass, property } = _decorator;

@ccclass('ShareCom')
export class ShareCom extends Component {
    start() {
        if (!Engine.instance.jsb.Jsb.isShare()) {
            this.node.active = false;
            return;
        }
    }

    onClick() {
        Engine.instance.jsb.share();
    }
}


