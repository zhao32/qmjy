import { _decorator, Component, instantiate, Node, Prefab, resources } from 'cc';
import { Engine } from './Engine';
const { ccclass, property } = _decorator;

@ccclass('DestopCom')
export class DestopCom extends Component {

    protected start(): void {
        if (!Engine.instance.jsb.Jsb.isDestop()) {
            this.node.active = false;
            return;
        }
    }
    onClick() {
        resources.load('prefabs/UIDestop', Prefab, (err, prefab) => {
            if (err) {
                console.error(err);
                return;
            }
            const node = instantiate(prefab);
            Engine.instance.node.addChild(node);
        });
    }
}


