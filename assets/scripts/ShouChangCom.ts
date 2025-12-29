import { _decorator, Component, instantiate, Node, Prefab, resources } from 'cc';
import { Engine } from './Engine';
const { ccclass, property } = _decorator;

@ccclass('ShouChangCom')
export class ShouChangCom extends Component {
    start() {
        if (!Engine.instance.jsb.Jsb.isCollect()) {
            this.node.active = false;
            return;
        }

    }


    onClick() {
        resources.load('prefabs/UIShouChang', Prefab, (err, prefab) => {
            if (err) {
                console.error(err);
                return;
            }
            const node = instantiate(prefab);
            Engine.instance.node.addChild(node);
        });
    }
}


