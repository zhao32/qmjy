import { _decorator, Component, Node } from 'cc';
const { ccclass, property } = _decorator;

@ccclass('UIShouChang')
export class UIShouChang extends Component {
    
    
    onClose(){
        this.node.destroy();
    }
}


