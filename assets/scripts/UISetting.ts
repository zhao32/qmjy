import { _decorator, Component, Node, Toggle, UITransform } from 'cc';
import { Engine } from './Engine';
const { ccclass, property } = _decorator;

@ccclass('UISetting')
export class UISetting extends Component {
    @property(Node)
    bgNode: Node;

    @property(Node)
    btnAgain: Node;
    @property(Node)
    btnBack: Node;
    @property(Node)
    btnRefresh: Node;

    @property(Toggle)
    toggleMusic: Toggle;

    @property(Toggle)
    toggleSound: Toggle;

    @property(Toggle)
    toggleShake: Toggle;

    show(isgame: boolean) {
        // this.btnAgain.active = isgame;
        this.btnBack.active = isgame;
        this.btnRefresh.active = isgame;
        this.node.active = true;
        this.bgNode.getComponent(UITransform).height = isgame ? 640 : 504;


        this.toggleSound.isChecked = parseInt(localStorage.getItem("sound")) == 1;
        this.toggleMusic.isChecked = parseInt(localStorage.getItem("music")) == 1;
        this.toggleShake.isChecked = parseInt(localStorage.getItem("shake")) == 1;

        this.toggleSound.node.on('toggle', () => {
            localStorage.setItem("sound", this.toggleSound.isChecked ? "1" : "0");
        }, this);
        this.toggleMusic.node.on('toggle', () => {
            localStorage.setItem("music", this.toggleMusic.isChecked ? "1" : "0");
        }, this);
        this.toggleShake.node.on('toggle', () => {
            localStorage.setItem("shake", this.toggleShake.isChecked ? "1" : "0");
        }, this);

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


