import { _decorator, Component, Node } from 'cc';
import { Engine } from './Engine';
const { ccclass, property } = _decorator;

@ccclass('UIFriend')
export class UIFriend extends Component {
    start() {
        // Engine.jsb.showRankView('level');
        Engine.instance.jsb.showRankView('level');
    }

    update(deltaTime: number) {

    }

    close() {
        this.node.destroy();
    }
}


