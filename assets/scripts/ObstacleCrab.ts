import { _decorator, Component, director, EventTouch, game, Node, NodeSpace, Sprite, SpriteAtlas, Vec2, Vec3 } from 'cc';
import { Obstacle } from './Obstacle';
import { Engine } from './Engine';
import { Circle } from './Circle';
import { Utils } from './CocosUtils';
const { ccclass, property } = _decorator;

//障碍物 螃蟹
@ccclass('ObstacleCrab')
export class ObstacleCrab extends Obstacle {
    highlightCircle: Circle;

    @property(SpriteAtlas)
    norAtlas: SpriteAtlas = null;

    @property(SpriteAtlas)
    selAtlas: SpriteAtlas = null;

    @property(SpriteAtlas)
    collisonAtlas: SpriteAtlas = null;

    putCircle: Circle; //放置的圆圈
    lastTouch: Vec2; //上次触摸的位置

    init(index: number, isCheck: boolean): void {
        super.init(index, isCheck);
        if (isCheck) {
            this.node.on(Node.EventType.TOUCH_START, this.onTouchStart, this);
            this.node.on(Node.EventType.TOUCH_MOVE, this.onTouchMove, this);
            this.node.on(Node.EventType.TOUCH_END, this.onTouchEnd, this);
            this.node.on(Node.EventType.TOUCH_CANCEL, this.onTouchEnd, this);
        }
    }

    onTouchStart(e: EventTouch) {
        this.putCircle = Engine.instance.getPointCircle(this.node.getWorldPosition());
        this.lastTouch = e.getLocation();
        this.playMove();
    }

    onTouchMove(e: EventTouch) {
        //螃蟹的移动方式只能当前方向或者反方向移动
        //获取移动方向
        let touch = e.getLocation();
        let lastTouch = this.lastTouch;

        //计算移动方向
        // let moveDir = touch.subtract(lastTouch);//.normalize();
        // if (Math.abs(moveDir.x) == 0 && Math.abs(moveDir.y) == 0) {
        //     return;
        // }
        if (Math.abs(touch.x - lastTouch.x) == 0 && Math.abs(touch.y - lastTouch.y) == 0) {
            return;
        }

        //获取当前方向角度
        let touchAngle = Utils.calculateTouchAngle360(touch, lastTouch);

        //计算touchAngle角度处于哪个象限
        let dir = 0;
        if (touchAngle >= 0 && touchAngle < 90) {
            dir = 0;
        } else if (touchAngle >= 90 && touchAngle < 180) {
            dir = 1;
        } else if (touchAngle >= 180 && touchAngle < 270) {
            dir = 2;
        } else if (touchAngle >= 270 && touchAngle < 360) {
            dir = 3;
        }

        let angle = this.node.angle;
        console.log("angle ", angle, "touchAngle ", touchAngle, "dir ", dir);
        let dirPos = Vec3.RIGHT.clone();
        //通过dir象限 决定螃蟹朝左右哪个方向移动
        if (dir == 0 || dir == 3) {
            dirPos = new Vec3(-1, 0, 0);
            if (dir == 0) {
                if (angle == 90) {
                    dirPos.x *= -1;
                } else if (angle == 29  ) {
                    dirPos.x *= -1;
                }

            } else if (dir == 3) {
                if (angle == -90|| angle == -28) {
                    dirPos.x *= -1;
                }
            }
        } else if (dir == 1 || dir == 2) {
            dirPos = new Vec3(1, 0, 0);
            if (dir == 2) {
                if (angle == 90) {
                    dirPos.x *= -1;
                } else if (angle == 29 ) {
                    dirPos.x *= -1;
                }
            } else if (dir == 1) {
                if (angle == -90|| angle == -28) {
                    dirPos.x *= -1;
                } else if (angle == 90) {
                    // dirPos.x *= -1;
                }
            }
        }
        if(angle == -27 ||angle == -29 || angle == 31|| angle == -31){
            dirPos.x *= -1;
        }

        // l

        // //当前方向角度
        // if (angle == -90) {
        //     moveDir.y *= -1;
        // } else if (angle == -156 || angle == 149 || angle == -153 || angle == -151 || angle == 151 || angle == 152 || angle == -152 || angle == 209 || angle == 158 || angle == -149) {
        //     moveDir.x *= -1;
        // }
        // console.log(moveDir, angle);
        // // 反向向角度
        let reverseAngle = angle + 180;


        let opos = this.node.getPosition();

        // let dirPos = Vec3.RIGHT.clone();
        // //通过moveDir方向 决定螃蟹朝左右哪个方向移动
        // if (Math.abs(moveDir.x) < Math.abs(moveDir.y)) {
        //     moveDir.x = moveDir.y;
        // }

        this.node.translate(dirPos.multiplyScalar(Math.min(40, Vec2.squaredDistance(touch, lastTouch))), NodeSpace.LOCAL);


        let collison = Engine.instance.canMoveObsToPoint(this);

        if (collison) {
            collison.playCollisonAnimation();
            this.node.setPosition(opos);
        } else {
            //检测当前方向是否存在点
            let circle = Engine.instance.checkPointDirCircle(this.node.getWorldPosition(), dirPos.x > 0 ? angle : reverseAngle);
            if (circle == null) {
                this.node.setPosition(opos);
            }
        }

        if (this.highlightCircle) {
            this.highlightCircle.removeHighlight();
            this.highlightCircle = null;
        }

        let circle = Engine.instance.getPointCircle(this.node.getWorldPosition());
        this.highlightCircle = circle;

        if (circle) {
            circle.highlightCircle();

            //查看当前点上是否存在箭头或者障碍物
            // let o = Engine.instance.checkIsPointArrowOrObs(circle, this);
            let o = Engine.instance.canMoveTowardCircle(this, circle);

            if (o == null) {
                this.putCircle = circle;
            }
        }

        this.animation.play("drag");
        this.lastTouch = touch
    }

    onTouchEnd(e: EventTouch) {
        // console.log('onTouchEnd', e.getLocation(), e.getPreviousLocation());
        //查看当前方向是否可以移动出去
        this.node.setWorldPosition(this.putCircle.node.getWorldPosition());
        this.putCircle = null;
        Engine.instance.hideAllCircle();
        this.playIdle();
    }

    playIdle(): void {
        super.playIdle();
        this.changeSpColor(1);
    }

    playMove() {
        this.changeSpColor(2);
    }

    playCollisonAnimation() {
        super.playCollisonAnimation();
        this.changeSpColor(3);
    }

    changeSpColor(index: number) {
        super.changeSpColor(index);
        let sps = this.animation.node.getComponentsInChildren(Sprite);
        for (let i = 0; i < sps.length; i++) {
            let sp = sps[i];
            let atlas: SpriteAtlas = null;
            if (index == 1) {
                atlas = this.norAtlas;
            } else if (index == 2) {
                atlas = this.selAtlas;
            } else if (index == 3) {
                atlas = this.collisonAtlas;
            }
            sp.spriteAtlas = atlas;
            let frame = atlas.getSpriteFrame(`${atlas.name}/${sp.spriteFrame.name.split('/')[1]}`);
            if (frame) {
                sp.spriteFrame = frame;
            } else {
                console.log("sp.spriteFrame.name ", sp.spriteFrame.name, "atlas ", atlas.name, "index ", index);
            }
        }
    }
}


