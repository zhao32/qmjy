import { _decorator, Animation, AnimationState, Color, Component, Node, randomRange, Sprite, UITransform, Vec3 } from 'cc';
import { Engine } from './Engine';
const { ccclass, property } = _decorator;

// 障碍物 基类
// 其他障碍物继承这个类
@ccclass('Obstacle')
export class Obstacle extends Component {

    @property({
        type: [Node]
    })
    points: Node[] = []; // 存储点的数组

    @property(Sprite)
    touSp: Sprite;

    @property(Animation)
    animation: Animation = null!;


    isOver: boolean = false;
    isMoving: boolean = false; // 是否正在移动
    index: number;
    isCheck: boolean = false; // 是否被选中
    init(index: number, isCheck: boolean) {
        this.index = index;
        this.isOver = false;
        this.isCheck = isCheck;

    }

    protected start(): void {

        if (this.animation) {
            this.animation.on(Animation.EventType.FINISHED, this.onCollisonEnd, this);

            this.playIdle();
        }
    }

    playCollisonAnimation() {
        // console.log("playCollisonAnimation")
        if (this.animation) {
            this.animation.play("collison");
            // this.touSp.color = Color.RED;
            clearTimeout(this.idleIndex);
        }
        Engine.instance.playShake();
    }

    onCollisonEnd(state: string, s: AnimationState) {
        // console.log("onCollisonEnd", s);
        if (state == Animation.EventType.FINISHED) {
            this.playIdle();
        }
    }

    changeSpColor(index: number) {
        // console.log("changeSpColor", index);
        // this.touSp.spriteFrame = this.touFrames[index];
        // this.bodySp.spriteFrame = this.bodyFrames[index];
        // this.weiSp.spriteFrame = this.weiFrames[index];
    }

    idleIndex = 0;
    playIdle() {
        if (this.animation) {
            let randomTime = randomRange(500, 1200);
            // this.touSp.color = Color.WHITE;

            this.idleIndex = setTimeout(() => {
                if (this.isValid && this.animation && this.animation.isValid)
                    this.animation.play("idle");
            }, randomTime);;
        }
    }

    move() {

        this.isOver = true;
    }
}


