import { _decorator, CCFloat, Component, Node, Vec3 } from 'cc';
import { Obstacle } from './Obstacle';
import { Engine } from './Engine';
const { ccclass, property } = _decorator;

@ccclass('ObstacleFish')
export class ObstacleFish extends Obstacle {
    @property({ type: CCFloat })
    moveSpeed: number = 200;
    isMove: boolean = false;
    targetPoint: Vec3;

    init(index: number, isCheck: boolean): void {
        super.init(index, isCheck);
        if (isCheck) {
            this.node.on(Node.EventType.TOUCH_START, this.onTouchStart, this);
        }
    }

    onTouchStart() {
        // console.log("------------ckcj")
        Engine.instance.checkFishMove(this);
    }

    toFunc:Function;
    moveToPoint(point: Vec3,func:Function) {
        this.isMove = true;
        this.toFunc = func;
        this.targetPoint = point;
    }

    protected update(dt: number): void {
        if (this.isMove) {
            let p = new Vec3();
            Vec3.moveTowards(p, this.node.worldPosition, this.targetPoint, this.moveSpeed * dt);
            this.node.worldPosition = p;
            if (Vec3.distance(this.node.worldPosition, this.targetPoint) < 1) {
                this.isMove = false;
                this.isOver = true;
                this.node.active = false;
                this.toFunc && this.toFunc();
                this.toFunc = null;
                Engine.instance.checkIsGameOVer();
                console.log("到达目标点");
            }
        }
    }
}


