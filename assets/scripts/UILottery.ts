import { _decorator, Component, EventTouch, Node, randomRangeInt, tween, UITransform } from 'cc';
import { UILotteryItem } from './UILotteryItem';
import { Engine } from './Engine';
import { SwitchType } from './jsb/JSB';
const { ccclass, property } = _decorator;

@ccclass('UILottery')
export class UILottery extends Component {

    @property({
        type: Node,
    })
    btnNext: Node = null; // 下一步按钮

    @property({
        type: Node,
    })
    btnAgain: Node = null; // 再来一次按钮

    @property({
        type: Node,
    })
    clickTip: Node = null; // 再来一次按钮

    @property({
        type: [UILotteryItem]
    })
    items: UILotteryItem[] = []; // 存储点的数组

    curState: number = 0; // 当前状态
    protected start(): void {
        this.items.forEach((item, index) => {
            item.node.active = false;
        });
        console.log("UILottery start", this.items.length);

        setTimeout(() => {
            this.startLottery(false);
        }, 2000);

        this.node.on(Node.EventType.TOUCH_END, this.onTouchEnd, this);
        this.btnNext.active = false;
        this.btnAgain.active = false;
    }

    onTouchEnd(e: EventTouch) {
        console.log("onTouchEnd", e);
        if (this.curState == 0) {
            let isp = true;
            for (let i = 0; i < this.items.length; i++) {
                if (!this.items[i].isPose) {
                    isp = false;
                    break;
                }
            }
            if (isp) {
                this.curState = 1;
                let pos = [];
                let count = 0;
                for (let i = 0; i < this.items.length; i++) {
                    let item = this.items[i];;
                    item.lotteryAni(() => {
                        count++;
                        if (count == this.items.length) {
                            for (let j = 0; j < this.items.length; j++) {
                                let toItem = this.items[j];
                                let p = randomRangeInt(0, pos.length);
                                let ps = pos[p];
                                pos.splice(p, 1);
                                tween(toItem.node).to(0.5, { worldPosition: ps }, { easing: 'sineInOut' }).call(() => {
                                    this.intoLottery();
                                }).start();
                            }
                        }
                    });
                    pos.push(item.node.getWorldPosition());
                }
                //做一个动画三个宝箱 以绕圈的方式移动到中间  中间的为0,0
            }
        }

        if (this.curState == 2) {
            //查看当前点击的那个宝箱
            let lotteryBox: UILotteryItem;
            for (let i = 0; i < this.items.length; i++) {
                if (this.items[i].getComponent(UITransform).getBoundingBoxToWorld().contains(e.getUILocation())) {
                    lotteryBox = this.items[i];
                    break;
                }
            }

            if (lotteryBox) {
                // lotteryBox.lotterySuccess();
                // lotteryBox.lotteryFail();
                this.clickTip.active = false;
                this.curState = 3;
                for (let i = 0; i < this.items.length; i++) {
                    if (this.items[i] != lotteryBox) {
                        this.items[i].lotteryFail();
                    } else {
                        this.items[i].lotterySuccess();
                    }
                }

                this.btnAgain.active = true;
                this.btnNext.active = true;

            }
        }

        // if (this.curState == 3) {
        //     let isClose = true;
        //     for (let i = 0; i < this.items.length; i++) {
        //         if (!this.items[i].isOver) {
        //             isClose = false;
        //             break;
        //         }
        //     }

        //     if (isClose) {
        //         this.node.destroy();
        //         Engine.instance.showWin();
        //     }
        // }
    }

    intoLottery() {
        if (this.curState == 1) {
            console.log("intoLottery");
            this.curState = 2;
        }
    }


    startLottery(play: boolean) {
        for (let i = 0; i < this.items.length; i++) {
            let index = i;
            let coin = randomRangeInt(5, 100);
            setTimeout(() => {
                this.items[index].node.active = true;
                this.items[index].show(coin);
                if (play) {
                    this.items[index].playPose();
                }
            }, i * 200);
        }
    }

    onNextClick() {
        Engine.instance.nextLevel()
        this.node.destroy();
    }

    onAgainClick() {
        Engine.instance.jsb.playRewardVideo('', {}).then((code: SwitchType) => {
            if (code == SwitchType.On) {
                this.btnAgain.active = false;
                this.btnNext.active = false;
                this.clickTip.active = true;
                this.curState = 0;
                this.startLottery(true);
            }
        })
    }

}


