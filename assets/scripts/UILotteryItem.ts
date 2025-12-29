import { _decorator, Animation, Color, Component, Label, Node, Sprite, tween, Vec3 } from 'cc';
import { Engine } from './Engine';
const { ccclass, property } = _decorator;

@ccclass('UILotteryItem')
export class UILotteryItem extends Component {
    @property({
        type: Sprite,
        tooltip: '圆圈的精灵'
    })
    circleSp: Sprite = null!;

    @property({
        type: Sprite,
        tooltip: '圆圈的精灵'
    })
    circleSp2: Sprite = null!;

    @property({
        type: Label,
        tooltip: '数字的标签'
    })
    numLabel: Label = null!;
    @property({
        type: Sprite,
        tooltip: '圆圈的精灵'
    })
    lightSp: Sprite = null!;

    ani: Animation;

    coin: number = 0; //金币数量
    isPose: boolean = false; //是否处于pose状态
    isOver: boolean = false; //是否处于结束状态

    protected onEnable(): void {
        this.ani = this.node.getComponent(Animation);
        this.playPose();
    }

    show(num: number) {
        this.coin = num;
        this.numLabel.string = this.coin.toString();
        this.isOver = false;
    }

    playPose(){
        this.ani.play('pose');
        this.ani.on(Animation.EventType.FINISHED, this.onPoseEnd, this);
    }

    onPoseEnd(state: string, s: Animation) {
        // console.log("onPoseEnd", state, s);
        this.ani.off(Animation.EventType.FINISHED, this.onPoseEnd, this);
        this.isPose = true;
    }

    lotteryCall: Function = null!;
    lotteryAni(call: Function) {
        this.ani.on(Animation.EventType.FINISHED, this.onLotteryEnd, this);
        this.lotteryCall = call;
        this.ani.play('close');
    }

    lotterySuccess() {
        this.ani.on(Animation.EventType.FINISHED, this.onLotterySuccessEnd, this);
        this.ani.play('open');
        // console.log("lotterySuccess", this.ani);
    }

    onLotterySuccessEnd() {
        // console.log("onLotterySuccessEnd", this.isOver);

        this.ani.off(Animation.EventType.FINISHED, this.onLotterySuccessEnd, this);
        this.circleSp2.node.active = true;
        this.lightSp.node.active = true;
        this.lightSp.color = new Color(255, 235, 0, 170);
        this.circleSp2.color = new Color(255, 235, 0, 87);

        tween(this.lightSp.node).to(5, {
            angle: 360
        }).call(() => {
            this.lightSp.node.angle = 0;
        }).union().repeatForever().start();

        Engine.instance.addCoin(this.coin); //添加金币
        //circleSp2做一个循环播放的呼吸动画
        tween(this.circleSp2.node).to(0.5, { scale: new Vec3(1.8, 1.8, 1.8) }).to(0.5, { scale: new Vec3(1.5, 1.5, 1.5) }).union().repeatForever().start();
        this.isOver = true;
    }

    lotteryFail() {
        this.ani.on(Animation.EventType.FINISHED, this.onLotteryFailEnd, this);
        this.ani.play('openmi');
    }

    onLotteryFailEnd() {
        this.ani.off(Animation.EventType.FINISHED, this.onLotteryFailEnd, this);
        this.isOver = true;
    }

    radius: number = 0; //半径
    angle: number = 0; //角度
    isLottery: boolean = false; //是否在抽奖中
    onLotteryEnd() {
        //使用tween做一个以绕圈的方式移动到中间  中间的为0,0
        this.radius = this.node.getPosition().length();
        this.angle = Math.atan2(this.node.position.y, this.node.position.x) * 180 / Math.PI;
        this.isLottery = true;
        this.ani.off(Animation.EventType.FINISHED, this.onLotteryEnd, this);
    }


    protected update(dt: number): void {
        //通过半径控制位置  围绕着圆心旋转
        if (this.isLottery) {
            this.angle += 720 * dt;
            this.node.setPosition(this.radius * Math.cos(this.angle * Math.PI / 180), this.radius * Math.sin(this.angle * Math.PI / 180), 0);

            this.radius -= 360 * dt; //每帧减少100的半径
            if (this.radius <= 0) {
                this.isLottery = false;
                this.lotteryCall();
                this.lotteryCall = null!;
            }
        }
    }

}


