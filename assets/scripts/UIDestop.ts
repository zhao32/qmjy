import { _decorator, Component, Node } from 'cc';
const { ccclass, property } = _decorator;

@ccclass('UIDestop')
export class UIDestop extends Component {
    
    onClose(){

        this.node.destroy();
    }
}


