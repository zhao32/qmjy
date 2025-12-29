import { _decorator, Component, Node, NodeSpace, Vec3 } from 'cc';
import { Engine } from './Engine';
import { Obstacle } from './Obstacle';
const { ccclass, property } = _decorator;

// 障碍物 乌龟
@ccclass('ObsTurtle')
export class ObsTurtle extends Obstacle {
    @property
    moveSpeed: number = 100; // 移动速度
    @property(Node)
    smoke: Node;
    start() {
        super.start();
        this.smoke.active = false;
    }
    isMoving: boolean = false; // 是否正在移动

    init(index: number, isCheck: boolean): void {
        super.init(index, isCheck);
        if(isCheck){
            this.node.on(Node.EventType.TOUCH_END, this.onTouchEnd, this);
        }
    }

    onTouchEnd() {
        //查看当前方向是否可以移动出去
        let body = Engine.instance.canMoveTowardObs(this, this.node.angle);
        if (!body) {
            //可以移动--
            console.log("可以移动--", this.node.angle);

            this.move();
        } else {
            body.playCollisonAnimation();
        }
    }

    move(): void {
        super.move();
        this.isMoving = true;
        Engine.instance.onPutRecordObs(this);
        Engine.instance.clearArrowTip();
        this.smoke.active = true;
        Engine.instance.playShake();
    }

    protected update(dt: number): void {
        if (this.isMoving) {
            this.node.translate(Vec3.RIGHT.clone().multiplyScalar(this.moveSpeed * dt), NodeSpace.LOCAL);
        }
    }
}


