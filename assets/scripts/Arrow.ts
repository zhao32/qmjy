import { _decorator, Component, math, Node, Size, UITransform, Vec3, Collider2D, Contact2DType, IPhysics2DContact, Animation, BoxCollider2D, Vec2, PhysicsSystem2D, Label, Line, Intersection2D, NodeSpace, game, view, SpriteFrame, Sprite, random, tween, UIOpacity, randomRangeInt, EventTouch, Tween } from 'cc';
import { Utils } from './CocosUtils';
import { Engine } from './Engine';
// import { Line2D } from './Line';
import { Circle } from './Circle';
import { Editor } from './Editor';
import { AnimationDelegate } from './AnimationDelegate';
import { GuideStep, UIGuide } from '../resources/guide/UIGuide';
import { Obstacle } from './Obstacle';
// import { Editor } from './Editor';
const { ccclass, property } = _decorator;


@ccclass('Arrow')
export class Arrow extends Component {

    @property(Node)
    circleNode: Node; // 圆圈节点
    @property(Node)
    arrowNode: Node = null; // 箭头节点
    @property(Node)
    smoke: Node;
    @property({
        type: [Node]
    })
    points: Node[] = []; // 存储点的数组
    @property(Label)
    label: Label = null; // 标签组件

    @property(Node)
    testNode: Node;

    @property(Sprite)
    touSp: Sprite;
    @property(Sprite)
    bodySp: Sprite;
    // @property(Sprite)
    // weiSp: Sprite;
    // @property(Sprite)
    // eyeSp: Sprite;
    // @property([SpriteFrame])
    // touFrames: Array<SpriteFrame> = [];

    // @property([SpriteFrame])
    // bodyFrames: Array<SpriteFrame> = [];

    // @property([SpriteFrame])
    // weiFrames: Array<SpriteFrame> = [];


    @property([SpriteFrame])
    eyeFrames: Array<SpriteFrame> = [];
    //正在移动时的移动速度
    @property
    moveSpeed: number = 100;

    isMoving: boolean = false; // 是否正在移动
    // isRotating: boolean = false; // 是否正在旋转

    startRotation: number = 0; // 初始旋转角度

    isOver: boolean = false;
    index: number;
    minW: number = 150;
    width: number;

    animaton: Animation;
    moveCircle: Circle = null; // 当前移动的圆点
    protected start(): void {
        this.label.string = this.node.name; // 设置标签文本为节点名称
        this.changeEye();

        this.reset();
    }

    angle: number;
    wid: number;
    circleId: number;
    isClick: boolean;
    init(angle: number, width: number, index: number, isClick: boolean) {
        this.angle = angle;
        this.wid = width;
        this.circleId = index;
        this.isClick = isClick;
    }

    reset() {
        this.node.angle = this.angle;
        this.isOver = false;
        this.animaton = this.arrowNode.getComponent(Animation);
        this.smoke.active = false;
        this.resetWidth(this.wid);
        if (this.isClick) {
            // 添加触摸事件监听
            this.addArrowTouchEvents(this.node);
        }
        this.changeSpColor(0);
        this.arrowNode.getComponent(AnimationDelegate).target = this;
        this.playIdle();
    }


    playIdle() {
        let idle = 'idlemax';
        if (this.width <= 200) {
            idle = 'idlemin';
        }
        setTimeout(() => {
            this.animaton.play(idle);

            this.arrowNode.scale = new Vec3(0.5, 0.5, 1)

        }, random() * 100);
    }

    resetWidth(width: number) {
        this.width = math.absMax(width, this.minW);
        let ut = this.getComponent(UITransform);

        ut.setContentSize(new math.Size(this.width, ut.height))
        let at = this.arrowNode.getComponent(UITransform);
        at.setContentSize((this.width) * 2, at.height); // 设置箭头的大小
        this.points[2].setPosition(new Vec3(this.width, -25, 0)); // 设置点的初始位置
        this.points[3].setPosition(new Vec3(this.width, 25, 0)); // 设置点的初始位置
        this.circleNode.position = new Vec3(at.width / 2, 0, 0); // 设置箭头的初始位置

        this.testNode.position = new Vec3(this.width - 20, 0, 0);
    }

    move(circle: Circle) {
        this.isOver = true;
        this.isMoving = true; // 开始移动
        this.smoke.active = true;
        this.moveCircle = circle;
        Engine.instance.onPutRecord(this);
        Engine.instance.clearArrowTip();
        Engine.instance.playShake();
    }

    stop() {
        this.isMoving = false; // 停止移动
    }

    addArrowTouchEvents(arrowNode: Node) {
        if (Editor.instance) return;

        let initialAngle = 0; // 初始角度
        let startTouchAngle = 0; // 触摸开始时的角度
        let lastDeltaAngle = 0; // 上一次的角度增量
        let highlightedCircle: Circle = null; // 当前高亮的圆点
        let direction = -1; // 旋转方向
        let isColliding = false; // 是否发生碰撞
        let lastTouchAngle = 0; // 上一次触摸的角度
        let collisonAngle = 0;
        let collisonOffect = new math.Vec2();
        let self = this;
        let inputCircle: Circle = null;
        let moveCircle: Circle = null;
        let _collisonArrow: Arrow = null;

        let oc2 = 0;
        let oc3 = 0;
        let isTouch = false;
        function checkMove(event, start: boolean) {
            // if (!self.isRotating) return;
            if (self.isOver) return;

            // 计算当前触摸点相对于箭头中心的角度
            const touchPos = event.getUILocation();
            const arrowPos = arrowNode.getWorldPosition();

            if (start) {
                startTouchAngle = (Utils.calculateTouchAngle(touchPos, arrowPos) + 360) % 360;
                oc2 = Utils.calculateTouchAngle(touchPos, arrowPos);


                lastTouchAngle = startTouchAngle;
                initialAngle = arrowNode.angle;
                lastDeltaAngle = 0;
                direction = -1;
                isColliding = false;
                highlightedCircle = null; // 重置高亮圆点
                collisonAngle = 0;
                // self.isRotating = true;
            } else {

                if (highlightedCircle) {
                    highlightedCircle.removeHighlight();
                }
                highlightedCircle = null; // 重置高亮圆点

                // 获取当前触摸点的角度
                let currentTouchAngle = (Utils.calculateTouchAngle(touchPos, arrowPos) + 360) % 360;
                //currentTouchAngle 角度会从360变成0 需要处理一下
                if (Math.abs(currentTouchAngle - lastTouchAngle) > 300) {
                    //表示当前触摸点已经从360变成0了 
                    if (currentTouchAngle > 180) {
                        if (arrowNode.angle < 360) {
                            arrowNode.angle += 360;
                        }
                    } else {

                        arrowNode.angle -= 360;
                        if (arrowNode.angle == -360) {
                            arrowNode.angle = 360;
                        }
                        if (arrowNode.angle < 0) {
                            currentTouchAngle += 360;
                        }
                    }
                }
                // 计算将要旋转的角度 的增量
                let arrowAngle = arrowNode.angle;
                if (arrowNode.angle < 0) {
                    arrowAngle = (arrowNode.angle + 360) % 360;
                } else if (arrowNode.angle > 360) {
                    if (currentTouchAngle < 180) {
                        arrowAngle = arrowNode.angle % 360;
                    }
                } else {
                    if (arrowAngle == 0) {
                        if (currentTouchAngle > 300) {
                            arrowAngle = 360;
                        }
                    }
                }

                if (!isColliding) {
                    while (true) {
                        let angle = Utils.moveToTarget(arrowAngle, currentTouchAngle, 1);
                        let a = arrowNode.angle;
                        arrowNode.angle = angle;
                        //检测是否碰到了其他的箭头
                        let collisonArrow = Engine.instance.checkFutureCollision(self, angle);
                        if (collisonArrow) {
                            arrowNode.angle = a;
                            // collisonAngle = a;
                            lastDeltaAngle = currentTouchAngle - startTouchAngle;
                            oc3 = Utils.calculateTouchAngle360(touchPos, arrowPos);
                            collisonAngle = arrowNode.angle;
                            isColliding = true;
                            collisonArrow.playCollisonAnimation();
                            Engine.instance.showTipNode(collisonArrow.touSp.node.getWorldPosition());

                            if (_collisonArrow != collisonArrow) {
                                Engine.instance.playSound('collison');
                            }

                            break;
                        }

                        //查看当前点上是否存在箭头
                        const farthestCircle = Engine.instance.getPointCircle(self.testNode.getWorldPosition());
                        if (farthestCircle) {
                            let collisonArrow = Engine.instance.getCircleArrow(farthestCircle)
                            //检测当前点是否可以放下箭头
                            if (collisonArrow) {
                                arrowNode.angle = a;
                                // collisonAngle = a;
                                lastDeltaAngle = currentTouchAngle - startTouchAngle;
                                oc3 = Utils.calculateTouchAngle360(touchPos, arrowPos);
                                collisonAngle = arrowNode.angle;
                                isColliding = true;
                                collisonArrow.playCollisonAnimation();
                                Engine.instance.showTipNode(collisonArrow.touSp.node.getWorldPosition());

                                if (_collisonArrow != collisonArrow) {
                                    Engine.instance.playSound('collison');
                                }

                                break;
                            }
                        }

                        arrowAngle = angle;
                        lastTouchAngle = currentTouchAngle;

                        if (arrowAngle == currentTouchAngle) {
                            break;
                        }
                    }
                } else {
                    //当前发生了碰撞---查看当前箭头是否可以旋转到触摸的角度
                    // console.log("collisonAngle", arrowAngle, currentTouchAngle);
                    while (true) {
                        let angle = Utils.moveToTarget(arrowAngle, currentTouchAngle, 1);
                        //检测是否碰到了其他的箭头
                        let collisonArrow = Engine.instance.checkFutureCollision(self, angle);
                        if (!collisonArrow) {
                            isColliding = false;
                            break;
                        }
                        arrowAngle = angle;
                        if (angle == currentTouchAngle) {
                            break;
                        }
                    }
                }
            }

            // 移除之前的高亮
            if (highlightedCircle) {
                highlightedCircle.removeHighlight();
            }
            highlightedCircle = null; // 重置高亮圆点

            // 获取距离最远的圆点
            const farthestCircle = Engine.instance.getPointCircle(self.testNode.getWorldPosition());
            if (farthestCircle) {
                // 高亮新的圆点
                farthestCircle.highlightCircle();
                highlightedCircle = farthestCircle;

                //检测当前点是否可以放下箭头
                let otherArrow = Engine.instance.canPutArrow(self, farthestCircle);
                if (!otherArrow) {
                    let angle = arrowNode.angle;
                    let inputAngle = Utils.getRotationFromDirection(farthestCircle.node.getWorldPosition(), arrowNode.getWorldPosition());
                    arrowNode.angle = inputAngle;

                    let inputOtherArrow = Engine.instance.canPutArrow(self, inputCircle);
                    if (!inputOtherArrow) {
                        inputCircle = farthestCircle;
                    }

                    arrowNode.angle = angle;

                    // console.log("可以放下箭头----------------", farthestCircle.label.string);
                }
            }
        }

        // 触摸开始事件
        arrowNode.on(Node.EventType.TOUCH_START, (event) => {
            // if (Engine.instance.touchId == -1) {
            //     Engine.instance.touchId = event.getID();
            // } else {
            //     if (Engine.instance.touchId != event.getID()) {
            //         return;
            //     }
            // }
            isTouch = true;

            this.startRotation = arrowNode.angle; // 记录初始角度

            initialAngle = arrowNode.angle;
            // 计算触摸点相对于箭头中心的角度
            const touchPos = event.getUILocation();
            const arrowPos = arrowNode.getWorldPosition();
            startTouchAngle = Utils.calculateTouchAngle(touchPos, arrowPos);

            checkMove(event, true);
            this.animaton.play('touch');
            this.changeSpColor(1);
        });

        // 触摸移动事件
        arrowNode.on(Node.EventType.TOUCH_MOVE, (event: EventTouch) => {
            if (Vec2.distance(event.getUIStartLocation(), event.getUILocation()) < 5) {
                return;
            }
            // if (Engine.instance.touchId != event.getID()) {
            //     return;
            // }

            checkMove(event, false);
        });


        function checkIsGuide() {

            if (Engine.instance.curLevel == 2) {
                //查看当前是否需要提示引导
                let isguide = localStorage.getItem('guide2');
                isguide = "true"
                if (isguide == null || isguide == '') {

                    // let arrow = Engine.instance.dir.getChildByName("arrow_34");
                    let grid = Engine.instance.board.children[10];
                    let grid9 = Engine.instance.board.children[9];
                    let grid3 = Engine.instance.board.children[3];
                    let grid16 = Engine.instance.board.children[16];

                    let arrow3 = Engine.instance.dir.getChildByName("arrow_34");
                    let toangle3 = Utils.calculateTouchAngle360(grid9.getWorldPosition(), arrow3.getWorldPosition());

                    let arrow4 = Engine.instance.dir.getChildByName('arrow_17');
                    let toangle4 = Utils.calculateTouchAngle360(grid16.getWorldPosition(), arrow4.getWorldPosition());

                    let arrow5 = Engine.instance.dir.getChildByName('arrow_6');
                    let toangle5 = Utils.calculateTouchAngle(grid.getWorldPosition(), arrow5.getWorldPosition());

                    if (self.node.name == 'arrow_21') {
                        // console.log("开始引导--------");
                        let steps: Array<GuideStep> = [];
                        //查找10的位置
                        steps.push(
                            {
                                path: 'btnTip',
                                text: '找不出可移动的河马时 /n 可以试试使用提示',
                                root: Engine.instance.uigame,
                                initFunc() {
                                    let btn = Engine.instance.uigame.getChildByName("btnTip");
                                    btn.children[0].active = true;
                                },
                                func: () => {

                                },
                                radius: 200
                            },
                            {
                                pos: Engine.instance.board.children[20].getWorldPosition(),
                                radius: 200,
                                text: "提示时，可以直接消除一个河马！",
                                path: '',

                                initFunc: (node: Node) => {
                                    let a = Engine.instance.dir.getChildByName("arrow_13");
                                    let ta = a.getComponent(Arrow);
                                    let arrow = Engine.instance.createArrow(0, a.angle, ta.width, false);
                                    arrow.node.name = 'guideArrow'
                                    arrow.node.setWorldPosition(a.getWorldPosition());
                                    let op = arrow.node.addComponent(UIOpacity);
                                    op.opacity = 255 * 0.5;
                                    tween(arrow.node).to(0.5, {
                                        angle: 0
                                    }).delay(0.5).call(() => {
                                        arrow.node.angle = a.angle;
                                    }).union().repeatForever().start();

                                    // //手指的动作
                                    let handParent = node.getChildByName('clickBtn');
                                    handParent.getComponent(Animation).stop();
                                    let hand = handParent.children[0];

                                    let pos1 = Engine.instance.board.children[15].getWorldPosition();
                                    hand.setWorldPosition(pos1);
                                    let pos2 = Engine.instance.board.children[26].getWorldPosition();
                                    tween(hand).to(0.5, {
                                        worldPosition: pos2.clone()
                                    }).delay(0.5).call(() => {
                                        hand.setWorldPosition(pos1)
                                    }).union().repeatForever().start();
                                },
                                touchFunc: (e) => {
                                    let a = Engine.instance.dir.getChildByName("arrow_13");
                                    a.emit(Node.EventType.TOUCH_START, e);
                                },

                                moveFunc: (e) => {
                                    let a = Engine.instance.dir.getChildByName("arrow_13");
                                    a.emit(Node.EventType.TOUCH_MOVE, e);
                                },

                                endFunc: (e, node: Node, guide) => {
                                    let a = Engine.instance.dir.getChildByName("arrow_13");
                                    a.emit(Node.EventType.TOUCH_END, e);
                                    if (a.angle == 0 || node.angle == 360) {
                                        //进入下一个提示
                                        console.log("-----------")
                                        Engine.instance.clearArrowTip();
                                        guide.onClick();
                                    }
                                    console.log(node);
                                }

                            },
                            {
                                pos: Engine.instance.board.children[16].getWorldPosition(),
                                radius: 200,
                                text: "提示时，可以直接消除一个河马！",
                                path: '',
                                initFunc: (node: Node) => {
                                    // let a = Engine.instance.dir.getChildByName("arrow_13");
                                    let ta = arrow3.getComponent(Arrow);
                                    let arrow = Engine.instance.createArrow(0, arrow3.angle, ta.width, false);
                                    arrow.node.name = 'guideArrow'
                                    arrow.node.setWorldPosition(arrow3.getWorldPosition());
                                    let op = arrow.node.addComponent(UIOpacity);
                                    op.opacity = 255 * 0.5;
                                    Tween.stopAllByTarget(arrow.node);
                                    tween(arrow.node).to(0.5, {
                                        angle: toangle3
                                    }).delay(0.5).call(() => {
                                        arrow.node.angle = arrow3.angle;
                                    }).union().repeatForever().start();

                                    // //手指的动作
                                    let handParent = node.getChildByName('clickBtn');
                                    handParent.getComponent(Animation).stop();
                                    let hand = handParent.children[0];
                                    Tween.stopAllByTarget(hand);

                                    let pos1 = Engine.instance.board.children[10].getWorldPosition();
                                    hand.setWorldPosition(pos1);
                                    let pos2 = Engine.instance.board.children[9].getWorldPosition();
                                    tween(hand).to(0.5, {
                                        worldPosition: pos2.clone()
                                    }).delay(0.5).call(() => {
                                        hand.setWorldPosition(pos1)
                                    }).union().repeatForever().start();
                                },
                                touchFunc: (e) => {
                                    arrow3.emit(Node.EventType.TOUCH_START, e);
                                },

                                moveFunc: (e) => {
                                    arrow3.emit(Node.EventType.TOUCH_MOVE, e);
                                },

                                endFunc: (e, node: Node, guide) => {
                                    arrow3.emit(Node.EventType.TOUCH_END, e);
                                    if (arrow3.angle == toangle3) {
                                        //进入下一个提示
                                        console.log("-----------222222")
                                        Engine.instance.clearArrowTip();
                                        guide.onClick();
                                    }
                                    console.log(node);
                                }
                            },
                            {
                                pos: grid.getWorldPosition(),
                                radius: 200,
                                text: "提示时，可以直接消除一个河马！",
                                path: '',
                                initFunc: (node: Node) => {
                                    // let a = Engine.instance.dir.getChildByName("arrow_13");
                                    let angle4 = arrow4.angle;
                                    let ta = arrow4.getComponent(Arrow);
                                    let arrow = Engine.instance.createArrow(0, arrow4.angle, ta.width, false);
                                    arrow.node.name = 'guideArrow'
                                    arrow.node.setWorldPosition(arrow4.getWorldPosition());
                                    let op = arrow.node.addComponent(UIOpacity);
                                    op.opacity = 255 * 0.5;
                                    Tween.stopAllByTarget(arrow.node);
                                    tween(arrow.node).to(0.5, {
                                        angle: toangle4
                                    }).delay(0.5).call(() => {
                                        arrow.node.angle = angle4;
                                    }).union().repeatForever().start();

                                    // //手指的动作
                                    let handParent = node.getChildByName('clickBtn');
                                    handParent.getComponent(Animation).stop();
                                    let hand = handParent.children[0];
                                    Tween.stopAllByTarget(hand);

                                    let pos1 = Engine.instance.board.children[11].getWorldPosition();
                                    hand.setWorldPosition(pos1);
                                    let pos2 = Engine.instance.board.children[16].getWorldPosition();
                                    tween(hand).to(0.5, {
                                        worldPosition: pos2.clone()
                                    }).delay(0.5).call(() => {
                                        hand.setWorldPosition(pos1)
                                    }).union().repeatForever().start();
                                },
                                touchFunc: (e) => {
                                    arrow4.emit(Node.EventType.TOUCH_START, e);
                                },

                                moveFunc: (e) => {
                                    arrow4.emit(Node.EventType.TOUCH_MOVE, e);
                                },

                                endFunc: (e, node: Node, guide) => {
                                    arrow4.emit(Node.EventType.TOUCH_END, e);
                                    if (arrow4.angle == toangle4 || arrow4.angle == -90) {
                                        //进入下一个提示
                                        console.log("-----------3333333333333")
                                        Engine.instance.clearArrowTip();
                                        guide.onClick();
                                    }
                                    console.log(node);
                                }
                            },
                            {
                                pos: grid.getWorldPosition(),
                                radius: 200,
                                text: "提示时，可以直接消除一个河马！",
                                path: '',
                                initFunc: (node: Node) => {
                                    // let a = Engine.instance.dir.getChildByName("arrow_13");
                                    let angle5 = arrow5.angle;
                                    let ta = arrow5.getComponent(Arrow);
                                    let arrow = Engine.instance.createArrow(0, arrow5.angle, ta.width, false);
                                    arrow.node.name = 'guideArrow'
                                    arrow.node.setWorldPosition(arrow5.getWorldPosition());
                                    let op = arrow.node.addComponent(UIOpacity);
                                    op.opacity = 255 * 0.5;
                                    Tween.stopAllByTarget(arrow.node);
                                    tween(arrow.node).to(0.5, {
                                        angle: toangle5
                                    }).delay(0.5).call(() => {
                                        arrow.node.angle = angle5;
                                    }).union().repeatForever().start();

                                    // //手指的动作
                                    let handParent = node.getChildByName('clickBtn');
                                    handParent.getComponent(Animation).stop();
                                    let hand = handParent.children[0];
                                    Tween.stopAllByTarget(hand);

                                    let pos1 = Engine.instance.board.children[3].getWorldPosition();
                                    hand.setWorldPosition(pos1);
                                    let pos2 = Engine.instance.board.children[10].getWorldPosition();
                                    tween(hand).to(0.5, {
                                        worldPosition: pos2.clone()
                                    }).delay(0.5).call(() => {
                                        hand.setWorldPosition(pos1)
                                    }).union().repeatForever().start();
                                },
                                touchFunc: (e) => {
                                    arrow5.emit(Node.EventType.TOUCH_START, e);
                                },

                                moveFunc: (e) => {
                                    arrow5.emit(Node.EventType.TOUCH_MOVE, e);
                                },

                                endFunc: (e, node: Node, guide) => {
                                    arrow5.emit(Node.EventType.TOUCH_END, e);
                                    if (arrow5.angle == toangle5) {
                                        //进入下一个提示
                                        console.log("-----------3333333333333")
                                        Engine.instance.clearArrowTip();
                                        guide.onClick();
                                    }
                                    console.log(node);
                                }
                            },
                            {
                                pos: grid9.getWorldPosition(),
                                radius: 100,
                                text: "提示时，可以直接消除一个河马！",
                                path: '',
                                initFunc: (node: Node) => {
                                    // //手指的动作
                                    let handParent = node.getChildByName('clickBtn');
                                    let hand = handParent.children[0];
                                    Tween.stopAllByTarget(hand);
                                    handParent.getComponent(Animation).play();

                                },
                                func: () => {
                                    arrow3.getComponent(Arrow).move(null);
                                    Engine.instance.clearArrowTip();
                                    localStorage.setItem('guide2', '1');
                                    let btn = Engine.instance.uigame.getChildByName("btnTip");
                                    btn.children[0].active = false;
                                }
                            }
                        )
                        UIGuide.create(steps)
                    }
                }

            }
        }

        function checkEnd() {

            self.playIdle();


            if (self.isOver) {
                return;
            }

            if (Engine.instance.curLevel == 190) {
                if (self.index == 45) {
                    let toa = Engine.instance.arrows.find(p => p.index == 84);
                    if (toa.node.angle == 180) {
                        if (highlightedCircle && highlightedCircle.index == 22) {
                            console.log("直接消除1")
                            const targetAngle = Utils.getRotationFromDirection(highlightedCircle.node.getWorldPosition(), arrowNode.getWorldPosition());
                            arrowNode.angle = targetAngle;

                            self.move(highlightedCircle);
                        }

                        if (!self.isOver) {
                            if (inputCircle && inputCircle.index == 22) {
                                let inputAngle = Utils.getRotationFromDirection(inputCircle.node.getWorldPosition(), arrowNode.getWorldPosition());
                                arrowNode.angle = inputAngle;
                                console.log("直接消除2")

                                self.move(inputCircle);
                            }
                        }
                    }
                } else if (self.index == 49) {
                    let toa = Engine.instance.arrows.find(p => p.index == 10);
                    if (toa.node.angle == 0) {
                        if (highlightedCircle && highlightedCircle.index == 72) {
                            console.log("直接消除1")
                            const targetAngle = Utils.getRotationFromDirection(highlightedCircle.node.getWorldPosition(), arrowNode.getWorldPosition());
                            arrowNode.angle = targetAngle;

                            self.move(highlightedCircle);
                        }

                        if (!self.isOver) {
                            if (inputCircle && inputCircle.index == 72) {
                                let inputAngle = Utils.getRotationFromDirection(inputCircle.node.getWorldPosition(), arrowNode.getWorldPosition());
                                arrowNode.angle = inputAngle;
                                console.log("直接消除2")

                                self.move(inputCircle);
                            }
                        }
                    }
                }

            }

            if (!self.isOver) {
                // 移除高亮
                do {
                    let otherArrow: Arrow | Obstacle = null;
                    if (highlightedCircle) {
                        //当前箭头旋转到高亮圆点的角度
                        const targetAngle = Utils.getRotationFromDirection(highlightedCircle.node.getWorldPosition(), arrowNode.getWorldPosition());
                        // 设置当前箭头的角度

                        otherArrow = Engine.instance.canMoveTowardsHighlightedCircle(self, targetAngle, false);

                        if (!otherArrow) {
                            //没有圆点可以拦住---
                            //检测当前方向线段是否有碰撞到箭头
                            otherArrow = Engine.instance.canMoveTowardArrow(self, highlightedCircle);
                            if (!otherArrow) {
                                arrowNode.angle = targetAngle;

                                self.move(highlightedCircle);
                                checkIsGuide();
                                break;
                            }
                        }
                    }
                    if (inputCircle) {
                        let inputAngle = Utils.getRotationFromDirection(inputCircle.node.getWorldPosition(), arrowNode.getWorldPosition());
                        arrowNode.angle = inputAngle;

                        let inputOtherArrow = Engine.instance.canPutArrow(self, inputCircle);

                        if (inputOtherArrow) {
                            arrowNode.angle = self.startRotation;
                            otherArrow = inputOtherArrow;
                        }
                        otherArrow = Engine.instance.canMoveTowardsHighlightedCircle(self, inputAngle, false);

                        if (!otherArrow) {
                            otherArrow = Engine.instance.canMoveTowardArrow(self, inputCircle);
                            if (!otherArrow) {
                                self.move(inputCircle);
                                checkIsGuide();
                            }
                        }
                    }
                    else {
                        arrowNode.angle = self.startRotation;
                    }

                    if (otherArrow) {
                        //被碰撞的箭头做一个动画
                        otherArrow.playCollisonAnimation();
                    }
                } while (false);
            }

            isColliding = false;
            console.log("angle " + self.node.angle);
            self.changeSpColor(0);
            self.changeEye();
            inputCircle = null;
            Engine.instance.hideAllCircle();
        }

        // 触摸结束事件
        arrowNode.on(Node.EventType.TOUCH_END, (event) => {
            // if (Engine.instance.touchId != event.getID()) {
            //     return;
            // }
            // Engine.instance.touchId = null;
            // console.log('Touch End', event.getID(), isTouch, self.isOver);
            if (this.isOver) return;

            // if (Vec2.distance(event.getUIStartLocation(), event.getUILocation()) < 5) {
            //     return;
            // }

            if (isTouch) {
                isTouch = false;
            } else {
                return;
            }

            checkEnd();
        });

        // 触摸取消事件
        arrowNode.on(Node.EventType.TOUCH_CANCEL, (event) => {
            console.log('Touch End', event.getID(), isTouch, self.isOver);

            if (this.isOver) return;
            // if (Vec2.distance(event.getUIStartLocation(), event.getUILocation()) < 5) {
            //     return;
            // }

            if (isTouch) {
                isTouch = false;
            } else {
                return;
            }
            checkEnd();

            // this.isRotating = false;
            // // return;
            // // 移除高亮
            // if (highlightedCircle) {
            //     //当前箭头旋转到高亮圆点的角度
            //     const targetAngle = Utils.getRotationFromDirection(highlightedCircle.node.getWorldPosition(), arrowNode.getWorldPosition());
            //     // 设置当前箭头的角度

            //     let otherArrow = Engine.instance.canMoveTowardsHighlightedCircle(this, highlightedCircle);
            //     //查看当前是否可以移动出去
            //     if (!otherArrow) {
            //         console.log("可以移动----------------");
            //         this.move();
            //         Engine.instance.showMask(false);
            //         arrowNode.angle = targetAngle;
            //     } else {
            //         //不能移动出去
            //         console.log("不能移动----------------");

            //         if (!isColliding) {
            //             arrowNode.angle = targetAngle;
            //         } else {
            //             arrowNode.angle = this.startRotation;
            //         }

            //         //被碰撞的箭头做一个动画
            //         otherArrow.playCollisonAnimation();
            //     }


            //     highlightedCircle.removeHighlight();
            //     highlightedCircle = null;
            // } else {
            //     arrowNode.angle = this.startRotation; // 恢复初始角度
            // }
            // isColliding = false;

        });
    }

    playCollisonAnimation() {
        this.changeSpColor(2);
        this.animaton.play('collison');
        setTimeout(() => {
            this.changeSpColor(0);
        }, 3 * 1000);
        Engine.instance.playShake();
    }

    changeSpColor(index: number) {
        if (this.isValid) {
            // this.touSp.spriteFrame = this.touFrames[index];
            // this.bodySp.spriteFrame = this.bodyFrames[index];
            // this.weiSp.spriteFrame = this.weiFrames[index];
        }
    }

    protected update(dt: number): void {
        if (this.isMoving) {
            this.node.translate(Vec3.RIGHT.clone().multiplyScalar(this.moveSpeed * dt), NodeSpace.LOCAL);
            // if (!this.isOver) {
            if (this.node.worldPosition.x >= Engine.instance.canvasUt.width * 1.5 || this.node.worldPosition.x <= -Engine.instance.canvasUt.width / 2) {
                this.isMoving = false;
                this.node.active = false;
                this.isOver = true;
                Engine.instance.hideMask();
                Engine.instance.checkIsGameOVer();
            }
            if (this.node.worldPosition.y >= Engine.instance.canvasUt.height * 1.5 || this.node.worldPosition.y <= -Engine.instance.canvasUt.height / 2) {
                this.isMoving = false;
                this.isOver = true;
                this.node.active = false;
                Engine.instance.hideMask();
                Engine.instance.checkIsGameOVer();
            }
            // }
        }
    }

    clear() {
        this.node.destroy();
    }

    changeEye() {
        // this.eyeSp.spriteFrame = this.eyeFrames[randomRangeInt(0, this.eyeFrames.length)];
    }

    onCollisonEnd() {
        // console.log('onCollisonEnd');
        this.changeSpColor(0);

        this.playIdle();
    }
}