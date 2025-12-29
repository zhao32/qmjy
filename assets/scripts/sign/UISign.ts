import { _decorator, Component, Label, Node } from 'cc';
import { SqlUtil } from '../SqlUtil';
import { Engine } from '../Engine';
const { ccclass, property } = _decorator;


var key = "signData";
var RewardDatas = [50, 100, 150, 200, 250, 300, 350, 400, 450, 500];

@ccclass('UISign')
export class UISign extends Component {

    @property({
        type: Node
    })
    content: Node = null;

    @property({
        type: Node
    })
    btnSign: Node = null;

    signData: any = null;


    protected onLoad(): void {
        let signData = SqlUtil.get(key, {
            signNum: 0,// 签到次数
            cumulativeSignNum: 0, //累计签到次数
            lastSignTime: 0,//   上次签到时间
            rewardCumulativeNum: 0,  //累计签到奖励领取次数
        })
        this.signData = signData;

        //查看是否签到
        let lastSignTime = this.signData.lastSignTime;
        this.btnSign.active = Engine.DaysDiff(Date.now(),lastSignTime) > 0;

        this.updateContent();
    }

    updateContent() {
        for (let i = 0; i < this.content.children.length; i++) {
            let child = this.content.children[i];
            const dayLabel = child.getChildByName("dayLabel");
            const numLabel = child.getChildByName("numLabel");
            dayLabel.getComponent(Label).string = "第" + (i + 1) + "天";
            numLabel.getComponent(Label).string = RewardDatas[i] + "";
            const mask = child.getChildByName("mask");
            if (this.signData.cumulativeSignNum % 7 <= i) {
                mask.active = false;
            } else {
                mask.active = true;
            }
        }
    }

    onItemClick(e, day) {
        // console.log(e, day);
        // let n = parseInt(day);

    }

    onSignClick() {
        let rewardCoin = RewardDatas[this.signData.signNum];
        this.signData.signNum++;
        this.signData.cumulativeSignNum++;
        this.signData.lastSignTime = Date.now();
        this.signData.rewardCumulativeNum++;
        this.btnSign.active = false;
        this.updateContent();
        console.log("签到成功，获得" + rewardCoin + "金币");
        //发放奖励
        Engine.instance.addCoin(rewardCoin);
        //保存数据
        this.saveData();
    }

    onClose() {
        this.node.destroy();
    }


    saveData() {
        SqlUtil.set(key, this.signData);
    }

}


