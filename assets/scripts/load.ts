import { _decorator, Component, Label, Node, ProgressBar, resources, Sprite } from 'cc';
const { ccclass, property } = _decorator;

@ccclass('load')
export class load extends Component {
    // @property({ type: Node })
    // loadNode: Node = null;

    @property({
        type: ProgressBar,
        tooltip: "进度条",
    })
    pro: ProgressBar = null

    @property({
        type: Label,
        tooltip: "进度条Label",
    })
    proLabel: Label = null
    start() {
        this.pro.progress = 0;
        resources.loadDir('', (finished: number, total: number) => {
            let curRange = finished / total;
            this.pro.progress = curRange;
            this.proLabel.string = "加载中......" + (curRange * 100).toFixed(2) + '%';
        }, (err: Error | null, data: any[]) => {
            if (err) {
                console.error(err);
            } else {
                // this.loadNode.active = false;
                this.node.active = false;
            }
        })
    }

    update(deltaTime: number) {

    }


}


