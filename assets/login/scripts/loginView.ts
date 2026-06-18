import { _decorator, Component, EditBox, Label, Node, sys, tween, UITransform } from 'cc';
import HttpManager from './HttpManager';
import { yr } from './yr/yr';
const { ccclass, property } = _decorator;

@ccclass('loginView')
export class loginView extends Component {

    @property(EditBox)
    box1: EditBox = null;
    @property(EditBox)
    box2: EditBox = null;

    private callback = null;
    onLoad() { }

    start() {

    }

    setCallBack(callback?) {
        this.callback = callback;
    }

    onClickLogin() {
        let phone = this.box1.string
        let passWord = this.box2.string
        if (!phone || phone == '') {
            this.showTip("请输入帐号");
            return;
        }
        if (!passWord || passWord == '') {
            this.showTip("请输入密码");
            return;
        }

        HttpManager.login(phone, passWord, (res) => {
            if (res && res.code == 1) {
                this.callback && this.callback();
            } else {
                this.showTip(res.msg);
            }
        });
    }

    showTip(str) {
        if (sys.platform == sys.Platform.ANDROID) {
            yr.inst.showToast({ text: str })
        } else {
            alert(str)
        }
    }
}


