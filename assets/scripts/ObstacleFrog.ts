import { _decorator, AnimationState, Component, Node, Vec3 } from 'cc';
import { Obstacle } from './Obstacle';
import { Engine } from './Engine';
import { Circle } from './Circle';
import { Utils } from './CocosUtils';
const { ccclass, property } = _decorator;

// 障碍物 青蛙
@ccclass('ObstacleFrog')
export class ObstacleFrog extends Obstacle {

    init(index: number, isCheck: boolean): void {
        super.init(index, isCheck);
        if (isCheck) {
            this.node.on(Node.EventType.TOUCH_START, this.onTouchStart, this);
        }

        // this.animation.play("idle");
    }

    paths: Circle[] = [];
    circleNum: number = 0; // 圆圈数量
    movePath: Circle[] = []; // 移动路径
    onTouchStart() {
        Engine.instance.hideAllCircle();
        Engine.instance.hideAllFrog();
        // //计算青蛙的可移动点
        // //青蛙可以朝着六个方向移动  正前方  右前方 右后方 后方 左后方 左前方
        // //通过当前点对应获取六个方向上的所有点

        let cis = Engine.instance.camMoveObsForgPoint(this.node.worldPosition);
        this.paths = cis;
        console.log("可移动点", cis);
        for (let i = 0; i < cis.length; i++) {
            let c = cis[i];
            c.highlightCircle();
            c.openTouch(this.onTouchCircle.bind(this));
            this.circleNum++;
        }

        this.animation.stop();
        this.animation.play("drag");
    }

    playIdle(): void {
        if (this.animation) {
            this.animation.play("idle");
        }
    }

    onCollisonEnd(state: string, s: AnimationState) {
        console.log('-------------')
    }

    onTouchCircle(circle: Circle) {
        console.log("点击了", circle.index);

        Engine.instance.hideAllCircle();
        let path = Engine.instance.findPathToTarget(this.node.worldPosition, circle.index, this.paths);
        //获取到路径后 执行移动动画
        this.movePath = path;
        this.moveIndex = 1;
        let angle = Utils.calculateTouchAngle(this.movePath[1].node.worldPosition, this.node.worldPosition);
        this.node.angle = angle;
        this.isMove = true;
        console.log("路径", path);
        // Engine.instance.hideAllFrog();
    }

    moveSpeed: number = 300;
    moveIndex: number = 0;
    isMove: boolean = false;
    protected update(dt: number): void {

        if (this.isMove) {
            //朝着目标移动并旋转
            let p = new Vec3(0, 0, 0);
            let targetPos = this.movePath[this.moveIndex].node.getWorldPosition();
            let pos = Vec3.moveTowards(p, this.node.getWorldPosition(), targetPos, this.moveSpeed * dt);
            this.node.setWorldPosition(pos);
            if (Vec3.distance(this.node.getWorldPosition(), targetPos) < 0.1) {
                this.node.setWorldPosition(targetPos);
                this.moveIndex++;
                if (this.moveIndex >= this.movePath.length) {
                    this.index = this.movePath[this.moveIndex - 1].index;
                    this.isMove = false;
                    this.moveIndex = 0; this.movePath = [];
                    this.playIdle();
                }
                else {
                    let angle = Utils.calculateTouchAngle(this.movePath[this.moveIndex].node.worldPosition, this.node.worldPosition);
                    this.node.angle = angle;
                }
            }
        }

    }

}


