import { _decorator, Component, instantiate, Node, Prefab, resources } from 'cc';
import { SqlUtil } from '../SqlUtil';
import { Engine } from '../Engine';
const { ccclass, property } = _decorator;

@ccclass('SignCom')
export class SignCom extends Component {
    onClick() {
        resources.load('prefabs/UISign', Prefab, (err, prefab) => {
            if(err) {
                console.error(err);
                return;
            }
            const signNode = instantiate(prefab);
            Engine.instance.node.addChild(signNode);
        })
    }
}


