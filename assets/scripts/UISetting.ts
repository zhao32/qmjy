import { _decorator, Component, Node } from 'cc';
import { Engine } from './Engine';
const { ccclass, property } = _decorator;

@ccclass('UISetting')
export class UISetting extends Component {

    @property(Node)
    btnAgain: Node;
    @property(Node)
    btnBack: Node;
    @property(Node)
    btnRefresh: Node;


    show(isgame: boolean) {
        this.btnAgain.active = isgame;
        this.btnBack.active = isgame;
        this.btnRefresh.active = isgame;
        this.node.active = true;


        Engine.instance.jsb.Jsb.showInstertView('');
    }

    hide() {
        this.node.active = false;
    }

    onAgainClick() {
        this.hide();
    }

    onBackClick() {
        Engine.instance.onExitClick();
        this.hide();
    }

    onRefreshClick() {
        Engine.instance.refreshLevel();
        this.hide();
    }
}


