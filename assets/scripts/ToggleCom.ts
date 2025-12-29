import { _decorator, Component, EventHandler, Label, Node, Vec3 } from 'cc';
import { Engine } from './Engine';
const { ccclass, property } = _decorator;

@ccclass('ToggleCom')
export class ToggleCom extends Component {
    @property(Node)
    bar: Node;
    @property(Label)
    tag: Label;
    @property
    isCheck: boolean = false;

    @property
    localKey: string = "";


    @property({
        type: [EventHandler]
    })
    events: Array<EventHandler> = [];



    start() {
        if (this.localKey != '') {
            //获取本地的值
            let key = localStorage.getItem(this.localKey);
            if (key && key != "") {
                this.isCheck = parseInt(key) == 1;
            }
        }

        this.updateToggle();

        this.node.on(Node.EventType.TOUCH_END, this.onToggleClick, this);
    }

    updateToggle() {
        this.bar.position = new Vec3(this.isCheck ? 60 : -60, this.bar.position.y, this.bar.position.z);
        this.tag.string = this.isCheck ? '开' : '关';
    }

    onToggleClick() {
        this.isCheck = this.isCheck ? false : true;
        if (this.localKey != '') {
            //获取本地的值
            // let key = localStorage.getItem(this.localKey);
            // if (key && key != "") {
            //     this.isCheck = parseInt(key) == 1;
            // }
            localStorage.setItem(this.localKey, this.isCheck ? '1' : '2');
            if (this.localKey == 'music') {
                Engine.instance.changeMusic(this.isCheck);
            } else if (this.localKey == 'sound') {

            } else if (this.localKey == 'shake') {

            }
        }
        this.updateToggle();
        this.sendEvent();
    }

    sendEvent() {
        EventHandler.emitEvents(this.events);
    }
}


