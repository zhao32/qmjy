import { _decorator, Component, Node } from 'cc';
const { ccclass, property } = _decorator;

@ccclass('AnimationDelegate')
export class AnimationDelegate extends Component {

    target: any;

    changeSpColor(index: number) {
        // console.log('index  ' + index);
        if (this.target) {
            this.target.changeSpColor(index);
        }
    }

    onCollisonEnd() {
        // console.log('onCollisonEnd');
        if (this.target) {
            this.target.onCollisonEnd();
        }
    }

}


