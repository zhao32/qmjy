import { _decorator, Color, Component, instantiate, JsonAsset, Label, Node, Prefab, resources, Sprite } from 'cc';
import { Engine } from './Engine';
import { GlobalConfig } from './CocosGlobalConfig';
const { ccclass, property } = _decorator;

@ccclass('UISelectLevel')
export class UISelectLevel extends Component {
    @property(Prefab)
    itemFab: Prefab;

    @property(Node)
    content: Node;

    show() {
        this.node.active = true;
        this.content.destroyAllChildren();


        let jsons = Engine.instance.levelDataAsset;
        let keys = Object.keys(jsons.json);
        let maxLevel = GlobalConfig.DB.level;

        let lv = 1;
        for (let i = 0; i < jsons.json.maxLevel; i++) {
            let item = instantiate(this.itemFab);
            this.content.addChild(item);
            let l = lv;
            item.getChildByName("levelLabel").getComponent(Label).string = (l).toString();
            if (l > maxLevel) {
                item.children[0].getComponent(Sprite).color = Color.GRAY;
            }
            item.on(Node.EventType.TOUCH_END, () => {
                if (l <= maxLevel) {
                    Engine.instance.onStartLevel(l);
                    this.hide();
                }
            }, this)
            lv++;
        }
        for (const key of keys) {
            let item = instantiate(this.itemFab);
            this.content.addChild(item);
            let l = lv;
            item.getChildByName("levelLabel").getComponent(Label).string = (l).toString();
            if (l > maxLevel) {
                item.children[0].getComponent(Sprite).color = Color.GRAY;
            }
            item.on(Node.EventType.TOUCH_END, () => {
                if (l <= maxLevel) {
                    Engine.instance.onStartLevel(l);
                    this.hide();
                }
            }, this)
            lv++;
        }
    }

    hide() {
        this.node.active = false;
    }
}


