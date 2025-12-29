import { _decorator, Component, Node, Vec3, resources, JsonAsset, Prefab, instantiate, Label, math, UITransform, EPhysics2DDrawFlags, PhysicsSystem2D, BoxCollider2D, Intersection2D, Mask, Canvas, AudioSource, AudioClip, UIOpacity, tween, Tween, Animation, Enum, Vec2, CCBoolean, ParticleSystem2D } from 'cc';
import { Arrow } from './Arrow';
import { Utils } from './CocosUtils';
import { Circle } from './Circle';
import { UIWin } from './UIWin';
import { UISetting } from './UISetting';
import { UISelectLevel } from './UISelectLevel';
import { GuideStep, UIGuide } from '../resources/guide/UIGuide';
import { JsbManager, PlatformType } from './jsb/JSBManager';
import { Obstacle } from './Obstacle';
import { SwitchType } from './jsb/JSB';
import { GlobalConfig } from './CocosGlobalConfig';
import { SqlUtil } from './SqlUtil';
import { ObstacleFish } from './ObstacleFish';
const { ccclass, property } = _decorator;

@ccclass('Engine')
export class Engine extends Component {
    private levelDatas: Array<levels> = null;
    public arrows: Arrow[] = [];
    private levelData: levels = null; // 关卡数据
    circles: Circle[] = []; // 存储所有圆点的数组

    @property({
        type: Enum(PlatformType)
    })
    platformType: PlatformType = PlatformType.None;

    @property(Node)
    board: Node;

    @property(Node)
    dir: Node;

    @property(Node)
    mask: Node;

    @property(Canvas)
    canvas: Canvas;

    @property(Prefab)
    circleFab: Prefab;

    @property(Prefab)
    arrowFab: Prefab;

    @property(Node)
    game: Node;

    @property(Node)
    uigame: Node;

    @property(Node)
    uimain: Node;

    @property(Label)
    levelLabel: Label;
    @property(Label)
    mainLabel: Label;


    @property(Node)
    btnRemove: Node;
    @property(Node)
    btnTip: Node;

    @property(Node)
    adTipPlane: Node;

    @property(UISetting)
    setting: UISetting;

    @property(JsonAsset)
    levelDataAsset: JsonAsset;

    // @property(JsonAsset)
    // levelGuideDataAsset: JsonAsset;

    @property(Node)
    tipNode: Node;

    @property([Node])
    adNodes: Array<Node> = [];

    @property
    cellSize: number = 60; // 每个格子的大小为 100x100


    @property(Label)
    timeLabel: Label;

    @property(Node)
    cblNode: Node;

    canvasUt: UITransform;

    static instance: Engine;

    curLevel: number;

    musicSource: AudioSource;


    touchId: number = -1;

    records: Array<Arrow | Obstacle> = [];
    obsAry: Array<Obstacle> = [];

    jsb: JsbManager;

    curGuideData: Array<any> = null; // 当前的引导数据
    guides = [];
    isGameOver: boolean = false;

    frogCircles: Array<Circle> = []; // 存储青蛙的圆点的数组
    countDown: number = 0;
    protected onLoad(): void {
        Engine.instance = this;
        this.canvasUt = this.canvas.getComponent(UITransform);
        this.jsb = new JsbManager();
        this.jsb.init(this.platformType);

    }

    start() {
        // this.win.hide();
        this.setting.hide();
        this.uigame.active = false;
        // this.hideMask();
        this.musicSource = this.node.addComponent(AudioSource);
        this.musicSource.loop = true;

        this.levelDatas = this.levelDataAsset.json as Array<levels>; // 获取所有关卡数据
        this.tipNode.active = false;
        this.adTipPlane.active = false;
        this.changeAdVisible(false);
        this.playMusic();
        this.jsb.Jsb.initAd();

        GlobalConfig.DB = {
            level: SqlUtil.get('curLevel', 1),
            accid: SqlUtil.get('accid', ''),
            coin: SqlUtil.get('coin', 0),
            nickName: '',
            openid: '',
            avatarUrl: '',
        }

        this.cblNode.active = false;
        this.refreshLevelLabel();

    }

    showCbl() {
        this.cblNode.active = true;
    }

    hideCbl() {
        this.cblNode.active = false;
    }

    goCbl() {
        let that = this;
        // @ts-ignore
        tt.navigateToScene({
            scene: "sidebar",
            success: (res) => {
                console.log("侧边栏打开成功");
                that.hideCbl();
            },
            fail: (res) => {
                console.log("侧边栏打开失败: ", res);
            },
        });

    }


    refreshLevelLabel() {
        //从本地获取当前的关卡
        this.mainLabel.string = GlobalConfig.DB.level.toString();
    }

    loadLevelData(lv: number) {
        this.generateLevel(lv);
    }

    generateLevel(lv: number) {
        this.levelData = this.levelDatas.find(p => p.id == lv);
        if (!this.levelData) {
            console.error('Invalid level data or level name.');
            return;
        }
        this.clearArrowTip();
        this.curGuideData = JSON.parse(this.levelData.guide) || [];
        // console.log(JSON.stringify(this.curGuideData));
        this.isGameOver = false;
        this.records = [];
        this.guides = [];
        this.frogCircles = [];
        const cols = this.levelData.cols || []; // 每列的点数
        const scale = this.levelData.layoutscale || 1; // 父节点缩放
        this.game.setScale(this.levelData.scale, this.levelData.scale, 1)
        // 设置棋盘缩放
        this.board.setScale(scale, scale, 1);

        // 清空棋盘上的子节点
        this.board.removeAllChildren();
        this.arrows = []; // 清空箭头数组
        /**
         * 创建格子
         */
        this.createGrids(cols); // 创建格子
        // 初始化箭头
        const info = JSON.parse(this.levelData.pigs) || []; // 假设 `pigs_xy` 是箭头的初始位置
        info.forEach((info: Array<number>) => {
            this.createArrow(info[0], info[1], info[2], true);
        });

        let obs = JSON.parse(this.levelData.obs) || [];
        for (let o of obs) {
            resources.load("prefabs/" + o[2], Prefab, (err, fab) => {
                if (err) {
                    console.error(err);
                    return;
                }
                const scale = this.levelData.layoutscale || 1; // 父节点缩放
                let c = this.board.children[o[0]];
                const pointNode = instantiate(fab);
                this.dir.addChild(pointNode);
                pointNode.setPosition(c.getPosition().multiplyScalar(scale))
                pointNode.angle = o[1];
                let obs = pointNode.getComponent(Obstacle);
                obs.init(o[0], true);
                this.obsAry.push(obs); // 将障碍物添加到数组中
            })
        }


        this.levelLabel.string = lv.toString();
        this.hideMask();


        if (lv > 2) {
            // this.countDown = 10;
            this.countDown = 300;
            this.timeLabel.node.parent.active = true;
        } else {
            this.timeLabel.node.parent.active = false;
        }


        if (GlobalConfig.DB.level == 1) {
            let isguide = localStorage.getItem('guide1');
            if (isguide != null && isguide != '') {
                return;
            }
            let cellSize = this.cellSize; // 每个格子的大小为 100x100

            let steps: Array<GuideStep> = []
            //查找10的位置
            let grid = this.board.children[10];
            let grid2 = this.board.children[5];
            steps.push(
                {
                    path: 'SpriteSplash',
                    text: '若前方无任何遮挡 /n 可点击小狗直接离开',
                    root: grid,
                    func: () => {
                        this.dir.getChildByName("arrow_6").getComponent(Arrow).move(null);
                    },
                    radius: 200
                },
                {
                    path: 'SpriteSplash',
                    text: '若前方挡住时 /n 可拖动小狗旋转离开',
                    // root: grid2,
                    pos: grid2.getWorldPosition().subtract(new Vec3(cellSize, 0, 0)),
                    radius: 200,
                    initFunc: (node: Node) => {
                        let a = this.dir.getChildByName("arrow_0");
                        let ta = a.getComponent(Arrow);
                        let arrow = this.createArrow(0, a.angle, ta.width, false);
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

                        let pos1 = this.board.children[2].getWorldPosition();
                        hand.setWorldPosition(pos1);
                        let pos2 = this.board.children[9].getWorldPosition();
                        tween(hand).to(0.5, {
                            worldPosition: pos2.clone()
                        }).delay(0.5).call(() => {
                            hand.setWorldPosition(pos1)
                        }).union().repeatForever().start();
                    },
                    touchFunc: (e) => {
                        let a = this.dir.getChildByName("arrow_0");
                        a.emit(Node.EventType.TOUCH_START, e);
                    },

                    moveFunc: (e) => {
                        let a = this.dir.getChildByName("arrow_0");
                        a.emit(Node.EventType.TOUCH_MOVE, e);
                    },

                    endFunc: (e, node: Node) => {
                        let a = this.dir.getChildByName("arrow_0");
                        a.emit(Node.EventType.TOUCH_END, e);
                        let com = a.getComponent(Arrow);
                        if (com.isOver) {
                            // node.destroy();
                            UIGuide.guide.close();

                            this.clearArrowTip();
                            localStorage.setItem('guide1', '1');
                        }
                    }
                }
            )
            UIGuide.create(steps)
        }

        // console.log(this.curGuideData.length, this.arrows.length);

    }

    createGrids(cols: number[]) {
        //格子x间隔
        let cellSizeX = this.levelData.cellSizeX || this.cellSize; // 每个格子的大小为 100x100
        //格子y间隔
        let cellSizeY = this.levelData.cellSizeY || this.cellSize; // 每个格子的大小为 100x100

        // 计算最大行数
        const maxRows = Math.max(...cols);

        // for (let colIndex = 0; colIndex < cols.length; colIndex++) {
        //     // 计算当前列的水平位置
        //     const colX = (colIndex - (cols.length - 1) / 2) * cellSizeX; // 水平位置，保持水平间隔一致
        //     // 当前列的点数
        //     const numPoints = cols[colIndex]; // 当前列的点数

        //     // 垂直分布在中间
        //     const offsetY = (numPoints - 1) * cellSizeX / 2; // 垂直偏移量，保持垂直间隔一致

        //     // 遍历每列的点
        //     for (let rowIndex = 0; rowIndex < numPoints; rowIndex++) {

        //         // 计算点的垂直位置
        //         // const posY = rowIndex * cellSizeY - offsetY; // 垂直位置，保持垂直间隔一致

        //         // this.createGridPoint(colX, posY);
        //         //计算点的垂直位置
        //         const posY = rowIndex * cellSizeY - offsetY; // 垂直位置，保持垂直间隔一致

        //         this.createGridPoint(colX, posY);

        //     }

        // }

        for (let colIndex = 0; colIndex < cols.length; colIndex++) {
            const colX = (colIndex - (cols.length - 1) / 2) * cellSizeX;
            const numPoints = cols[colIndex];

            for (let rowIndex = 0; rowIndex < numPoints; rowIndex++) {
                // 让每列的点以 y=0 为中心对称分布
                let posY = 0;
                if (numPoints % 2 === 1) {
                    // 奇数个点，中间点在 y=0
                    posY = (rowIndex - Math.floor(numPoints / 2)) * cellSizeY;
                } else {
                    // 偶数个点，中间两个点对称分布在 y=0 上下
                    posY = (rowIndex - (numPoints / 2 - 0.5)) * cellSizeY;
                }
                this.createGridPoint(colX, posY);
            }
        }
    }

    createGridPoint(x: number, y: number) {
        const pointNode = instantiate(this.circleFab);
        pointNode.setPosition(new Vec3(x, y, 0));
        let com = pointNode.getComponent(Circle);
        this.board.addChild(pointNode);
        com.init(pointNode.getSiblingIndex()); // 初始化圆点
        this.circles.push(com); // 将圆点添加到数组中

        // pointNode.getChildByName("Label").active = HTML5 ? true : false;
        pointNode.getChildByName("Label").active = false;
    }

    createArrow(slibling: number, angle: number, width: number, put: boolean) {
        const scale = this.levelData.layoutscale || 1; // 父节点缩放

        const arrowNode = instantiate(this.arrowFab);
        let p = this.board.children[slibling];
        if (!p) return null;

        arrowNode.setPosition(p.getPosition().multiplyScalar(scale))
        // arrowNode.setRotationFromEuler(0, 0, this.getRotationFromDirection(direction));
        this.dir.addChild(arrowNode);
        arrowNode.name = `arrow_${slibling}`; // 设置箭头的名称
        let arrow = arrowNode.getComponent(Arrow);
        arrow.index = slibling;
        if (put) {
            this.arrows.push(arrow);
        }

        arrow.init(angle, width, slibling, put); // 初始化箭头
        return arrow;
    }

    checkFutureCollision(arrow: Arrow, targetAngle: number): Arrow | Obstacle {
        // 模拟箭头旋转到目标角度
        const originalAngle = arrow.node.angle;
        arrow.node.angle = targetAngle;

        // 获取旋转后的顶点
        const futureVertices = Utils.getRotatedVecticesArrow(arrow);

        // 恢复原始角度
        arrow.node.angle = originalAngle;

        if (!futureVertices) return null;

        // 遍历所有箭头，检测是否发生碰撞
        for (const otherArrow of this.arrows) {
            if (otherArrow === arrow) continue; // 跳过自身
            if (otherArrow.isOver) continue;

            const otherVertices = Utils.getRotatedVecticesArrow(otherArrow);
            if (!otherVertices) continue;

            if (Intersection2D.polygonPolygon(futureVertices, otherVertices)) {
                // console.log(`Collision detected between ${arrow.node.name} and ${otherArrow.node.name}`);
                return otherArrow; // 检测到碰撞
            }
        }

        for (const obs of this.obsAry) {
            if (obs.isOver) continue;
            const otherVertices = Utils.getRotatedVecticesArrow(obs);
            if (!otherVertices) continue;

            if (Intersection2D.polygonPolygon(futureVertices, otherVertices)) {
                // console.log(`Collision detected between ${arrow.node.name} and ${otherArrow.node.name}`);
                return obs; // 检测到碰撞
            }
        }

        return null; // 没有碰撞

    }

    getFarthestCircle(arrow: Arrow): Circle {
        let farthestCircle: Circle = null;
        let maxDistance = -Infinity;

        const arrowPos = arrow.node.getWorldPosition();

        // 遍历所有圆点，计算距离
        for (const circle of Engine.instance.circles) { // 假设 `Engine` 中有所有圆点的引用

            if (Engine.instance.checkFutureCircleCollision(arrow, circle)) {
                let c = false;
                //获取当前圆形是否与别的矩形由碰撞------ 没有才继续计算
                for (const otherArrow of this.arrows) {
                    if (otherArrow == arrow) continue; // 跳过自身
                    if (Engine.instance.checkFutureCircleCollision(otherArrow, circle)) {
                        //如果这个圆形与别的矩形发生碰撞 则跳过检测
                        c = true;
                        break;
                    }
                }

                if (c) {
                    continue; // 跳过与其他箭头发生碰撞的圆点
                }

                const circlePos = circle.node.getWorldPosition();
                const distance = Vec3.distance(arrowPos, circlePos);
                if (distance == 0) {
                    continue; // 如果距离为0，跳过
                }
                if (distance > maxDistance) {
                    maxDistance = distance;
                    farthestCircle = circle;
                }
            }
        }

        return farthestCircle;
    }

    getPointCircle(pos: Vec3): Circle {
        for (const circle of this.circles) {
            const circlePos = circle.node.getWorldPosition();
            const distance = Vec3.distance(pos, circlePos);
            let radius = this.levelData.radius || 30;
            // if (this.levelData.radius == 2) {
            //     if (distance < 50) {
            //         return circle; // 返回碰撞的圆点
            //     }
            // } else {
            if (distance < radius) {
                return circle; // 返回碰撞的圆点
            }
            // }
        }
        return null; // 没有碰撞
    }

    checkArrowCollision(arrow: Arrow): Arrow {
        for (const otherArrow of this.arrows) {
            if (arrow == otherArrow) continue; // 跳过自身
            if (otherArrow.isOver) continue;
            if (this.checkArrowIsCollison(arrow, otherArrow)) {
                return otherArrow; // 返回碰撞的箭头
            }
        }
        return null;
    }

    /**
     * 检测两个箭头是否发生碰撞
     * @param arrow1 箭头1
     * @param arrow2 箭头2
     * @returns 
     */
    checkArrowIsCollison(arrow1: Arrow, arrow2: Arrow): boolean {
        let vertices1 = Utils.getRotatedVecticesArrow(arrow1);
        let vertices2 = Utils.getRotatedVecticesArrow(arrow2);
        if (Intersection2D.polygonPolygon(vertices1, vertices2)) {
            return true; // 返回碰撞的箭头
        }
        return false; // 没有碰撞
    }

    checkFutureCircleCollision(arrow: Arrow, circle: Circle): boolean {
        // 获取箭头的旋转后的顶点
        const futureVertices = Utils.getRotatedVecticesArrow(arrow);
        if (!futureVertices) return null;

        const circlePos = new math.Vec2(circle.node.getWorldPosition().x, circle.node.getWorldPosition().y); // 圆点的世界坐标
        const circleRadius = 30; // 圆点的半径

        // 检测圆点与箭头的碰撞
        if (Intersection2D.polygonCircle(futureVertices, circlePos, circleRadius)) {
            // console.log(`Collision detected between ${arrow.node.name} and ${circle.node.name}`);
            return true; // 检测到碰撞
        }
        return false; // 没有碰撞
    }

    getCircleArrow(circle: Circle): Arrow {
        let _otherArrow: Arrow = null;
        const circleRadius = 30; // 圆点的半径  
        const circlePos = circle.node.getWorldPosition();
        for (const otherArrow of this.arrows) {
            if (otherArrow.isOver) continue;
            let distance = Vec3.distance(circlePos, otherArrow.node.getWorldPosition());
            if (distance < circleRadius) {
                _otherArrow = otherArrow;
                break;
            }
        }
        return _otherArrow;
    }

    getCircleCollisons(circle: Circle): Arrow | Obstacle {
        const circlePos = new math.Vec2(circle.node.getWorldPosition().x, circle.node.getWorldPosition().y); // 圆点的世界坐标
        const circleRadius = 30; // 圆点的半径
        for (const otherArrow of this.arrows) {
            if (otherArrow.isOver) continue;
            const vertices = Utils.getRotatedVecticesArrow(otherArrow);
            if (Intersection2D.polygonCircle(vertices, circlePos, circleRadius)) {
                return otherArrow; // 检测到碰撞
            }
        }

        for (const obs of this.obsAry) {
            if (obs.isOver) continue;
            const vertices = Utils.getRotatedVecticesArrow(obs);
            if (Intersection2D.polygonCircle(vertices, circlePos, circleRadius)) {
                return obs; // 检测到碰撞
            }
        }
        return null; // 没有碰撞
    }

    checkCircleIsCollisonArrow(arrow: Arrow, circle: Circle) {
        const circlePos = new math.Vec2(circle.node.getWorldPosition().x, circle.node.getWorldPosition().y); // 圆点的世界坐标
        const circleRadius = 30; // 圆点的半径
        for (const otherArrow of this.arrows) {
            if (arrow == otherArrow) continue; // 跳过自身
            if (otherArrow.isOver) continue;
            const vertices = Utils.getRotatedVecticesArrow(otherArrow);
            if (Intersection2D.polygonCircle(vertices, circlePos, circleRadius)) {
                return otherArrow; // 检测到碰撞
            }
        }
        return null; // 没有碰撞
    }

    canMoveTowardsHighlightedCircleOld(arrow: Arrow, highlightedCircle: Circle, first: boolean): Arrow {
        if (!highlightedCircle) return null;

        const arrowPos = arrow.node.getWorldPosition();
        const circlePos = highlightedCircle.node.getWorldPosition();
        // 计算箭头朝小球方向的单位向量
        const direction = new Vec3(circlePos.x - arrowPos.x, circlePos.y - arrowPos.y, 0).normalize();

        const targetPos = new Vec3(
            arrowPos.x + direction.x * 1000, // 假设移动到足够远的位置
            arrowPos.y + direction.y * 1000
        );
        let _otherArrow: Arrow = null;

        for (const circle of this.circles) {
            if (circle == highlightedCircle) continue; // 跳过自身
            const circlePos = circle.node.getWorldPosition();
            const circleRadius = 30; // 圆点的半径  
            //计算当前方向的线段与圆点的碰撞

            if (Utils.lineCircle(new math.Vec2(arrowPos.x, arrowPos.y), new math.Vec2(targetPos.x, targetPos.y), new math.Vec2(circlePos.x, circlePos.y), circleRadius)) {
                // console.log("碰撞到圆点不能移动----------------");
                // return circle; // 返回碰撞的圆点
                //获取当前点上的箭头
                for (const otherArrow of this.arrows) {
                    if (arrow == otherArrow) continue; // 跳过自身
                    if (otherArrow.isOver) continue;

                    if (this.checkFutureCircleCollision(otherArrow, circle)) {
                        // return otherArrow; // 返回碰撞的箭头
                        if (_otherArrow == null) {
                            _otherArrow = otherArrow;
                        } else {
                            //计算距离
                            let d1 = Vec3.distance(arrow.circleNode.getWorldPosition(), otherArrow.circleNode.getWorldPosition());
                            let d2 = Vec3.distance(arrow.circleNode.getWorldPosition(), _otherArrow.circleNode.getWorldPosition());
                            if (d2 > d1) {
                                _otherArrow = otherArrow;
                            }
                        }
                        if (first) {
                            break;
                        }
                    }
                }
            }
            if (first) {
                if (_otherArrow) {
                    break;
                }
            }
        }

        return _otherArrow;
    }

    canMoveTowardsHighlightedCircle(arrow: Arrow, angle: number, first: boolean): Arrow | Obstacle {
        if (!arrow) return null;

        // const arrowPos = arrow.node.getWorldPosition();
        //将角度转换为方向
        const direction = Utils.getDirectionFromRotation(angle).normalize();
        const lineStart1 = new Vec2(arrow.points[0].getWorldPosition().x, arrow.points[0].getWorldPosition().y);
        const lineStart2 = new Vec2(arrow.points[1].getWorldPosition().x, arrow.points[1].getWorldPosition().y);
        const lineEnd1 = new Vec2(lineStart1.x + direction.x * 1000, lineStart1.y + direction.y * 1000);
        const lineEnd2 = new Vec2(lineStart2.x + direction.x * 1000, lineStart2.y + direction.y * 1000);
        const lineStart = new Vec2(arrow.node.getWorldPosition().x, arrow.node.getWorldPosition().y);
        const lineEnd = new Vec2(lineStart.x + direction.x * 1000, lineStart.y + direction.y * 1000);


        //先计算当前方向的线段与圆点的碰撞
        //计算当前方向的线段与圆点的碰撞
        let _otherArrow: Arrow = null;
        const circleRadius = 30; // 圆点的半径  
        for (const circle of this.circles) {
            const circlePos = circle.node.getWorldPosition();
            //计算当前方向的线段与圆点的碰撞
            if (Utils.lineCircle(lineStart, lineEnd, new math.Vec2(circlePos.x, circlePos.y), circleRadius)) {
                //检测圆点上是否存在箭头
                for (const otherArrow of this.arrows) {
                    if (arrow == otherArrow) continue; // 跳过自身
                    if (otherArrow.isOver) continue;
                    let distance = Vec3.distance(arrow.circleNode.getWorldPosition(), otherArrow.circleNode.getWorldPosition());
                    if (distance < circleRadius) {
                        if (!first) {
                            if (_otherArrow == null) {
                                _otherArrow = otherArrow;
                            } else {
                                //计算距离
                                let d1 = Vec3.distance(arrow.circleNode.getWorldPosition(), otherArrow.circleNode.getWorldPosition());
                                let d2 = Vec3.distance(arrow.circleNode.getWorldPosition(), _otherArrow.circleNode.getWorldPosition());
                                if (d2 > d1) {
                                    _otherArrow = otherArrow;
                                }
                            }
                        } else {
                            _otherArrow = otherArrow;
                            break;
                        }
                    }
                }
            }
            if (first) {
                if (_otherArrow) {
                    break;
                }
            }
        }

        //计算当前方向线段碰撞到的所有箭头
        for (const otherArrow of this.arrows) {
            if (arrow == otherArrow) continue; // 跳过自身
            if (otherArrow.isOver) continue;

            const vertices = Utils.getRotatedVecticesArrow(otherArrow);
            let isCollison: boolean = false;
            if (Intersection2D.linePolygon(lineStart1, lineEnd1, vertices)) {
                isCollison = true;

            } else if (Intersection2D.linePolygon(lineStart2, lineEnd2, vertices)) {
                isCollison = true;
            }
            if (isCollison) {
                // console.log("碰撞到箭头不能移动----------------", otherArrow.node.name);
                // return otherArrow; // 返回碰撞的箭头
                if (_otherArrow == null) {
                    _otherArrow = otherArrow;
                } else {
                    //计算距离
                    let d1 = Vec3.distance(arrow.circleNode.getWorldPosition(), otherArrow.circleNode.getWorldPosition());
                    let d2 = Vec3.distance(arrow.circleNode.getWorldPosition(), _otherArrow.circleNode.getWorldPosition());
                    if (d2 > d1) {
                        _otherArrow = otherArrow;
                    }
                }

                if (first) {
                    break;
                }
            }
        }


        if (first) {
            if (_otherArrow) {
                return _otherArrow;
            }
        }
        let _obs: Obstacle = null;
        //计算当前方向线段碰撞到的所有障碍物
        for (const obs of this.obsAry) {
            if (obs.isOver) continue;
            const vertices = Utils.getRotatedVecticesArrow(obs);
            let isCollison: boolean = false;
            if (Intersection2D.linePolygon(lineStart1, lineEnd1, vertices)) {
                // _otherArrow = obs; // 返回碰撞的箭头
                isCollison = true;
            } else if (Intersection2D.linePolygon(lineStart2, lineEnd2, vertices)) {
                // _otherArrow = obs; // 返回碰撞的箭头
                //break;
                isCollison = true;
            }
            if (isCollison) {
                // console.log("碰撞到箭头不能移动----------------", otherArrow.node.name);
                // return otherArrow; // 返回碰撞的箭头
                if (_obs == null) {
                    _obs = obs;
                } else {
                    //计算距离
                    let d1 = Vec3.distance(arrow.circleNode.getWorldPosition(), obs.node.getWorldPosition());
                    let d2 = Vec3.distance(arrow.circleNode.getWorldPosition(), _obs.node.getWorldPosition());
                    if (d2 > d1) {
                        _obs = obs;
                    }
                }

                if (first) {
                    break;
                }
            }
        }

        if (_otherArrow && _obs) {
            let d1 = Vec3.distance(arrow.circleNode.getWorldPosition(), _otherArrow.node.getWorldPosition());
            let d2 = Vec3.distance(arrow.circleNode.getWorldPosition(), _obs.node.getWorldPosition());
            if (d1 > d2) {
                return _obs;
            } else {
                return _otherArrow;
            }
        } else if (_obs) {
            return _obs;
        }
        return _otherArrow;
    }

    /**
     * 查看箭头是否可以放置在当前圆点上
     * @param arrow 箭头
     * @param highlightedCircle 高亮点
     */
    canPutArrow(arrow: Arrow, highlightedCircle: Circle): Arrow | Obstacle {
        if (!highlightedCircle) return null;

        const circlePos = highlightedCircle.node.getWorldPosition();
        const circleRadius = 30; // 圆点的半径

        //查看当前是否会碰撞到其他的箭头

        let arrowVertices = Utils.getRotatedVecticesArrow(arrow);

        for (const otherArrow of this.arrows) {
            if (arrow == otherArrow) continue; // 跳过自身
            if (otherArrow.isOver) continue;

            const vertices = Utils.getRotatedVecticesArrow(otherArrow);
            if (Intersection2D.polygonCircle(vertices, new math.Vec2(circlePos.x, circlePos.y), circleRadius)) {
                return otherArrow; // 返回碰撞的箭头
            }

            if (Intersection2D.polygonPolygon(arrowVertices, vertices)) {
                return otherArrow; // 返回碰撞的箭头
            }
        }

        //查看当前是否会碰撞到其他的障碍物
        for (const obs of this.obsAry) {
            if (obs.isOver) continue; // 跳过自身
            const vertices = Utils.getRotatedVecticesArrow(obs);
            if (Intersection2D.polygonCircle(vertices, new math.Vec2(circlePos.x, circlePos.y), circleRadius)) {
                return obs; // 返回碰撞的箭头
            }

            if (Intersection2D.polygonPolygon(arrowVertices, vertices)) {
                return obs; // 返回碰撞的箭头
            }
        }

        return null; // 没有碰撞
    }

    /**
     * 检测当前角度是否可以移动出去
     * @param arrow 箭头
     * @param highlightedCircle 元旦
     * @returns 
     */
    canMoveTowardArrow(arrow: Arrow, highlightedCircle: Circle): Arrow | Obstacle {
        if (!highlightedCircle) return null;

        const arrowPos = new math.Vec2(arrow.node.worldPosition.x, arrow.node.worldPosition.y);
        const circlePos = highlightedCircle.node.getWorldPosition();
        // 计算箭头朝小球方向的单位向量
        const direction = new math.Vec2(circlePos.x - arrowPos.x, circlePos.y - arrowPos.y).normalize();

        const targetPos = new math.Vec2(
            arrowPos.x + direction.x * 1000, // 假设移动到足够远的位置
            arrowPos.y + direction.y * 1000
        );

        for (const otherArrow of this.arrows) {
            if (arrow == otherArrow) continue;
            if (otherArrow.isOver) continue;


            if (Intersection2D.linePolygon(arrowPos, targetPos, Utils.getRotatedVecticesArrow(otherArrow))) {
                return otherArrow;
            }
        }

        for (const obs of this.obsAry) {
            if (obs.isOver) continue;
            if (Intersection2D.linePolygon(arrowPos, targetPos, Utils.getRotatedVecticesArrow(obs))) {
                return obs;
            }
        }


        return null;
    }

    /**
     * 圆点是否已经被箭头碰撞
     * @param arrow 箭头
     * @param highlightedCircle 高亮的圆点
     * @returns 
     */
    canMoveTowardCircle(arrow: Arrow | Obstacle, highlightedCircle: Circle) {
        if (!highlightedCircle) return null;

        const circlePos = highlightedCircle.node.getWorldPosition();

        for (const otherArrow of this.arrows) {
            if (arrow == otherArrow) continue;
            if (otherArrow.isOver) continue;

            const vertices = Utils.getRotatedVecticesArrow(otherArrow);

            if (Intersection2D.polygonCircle(vertices, new Vec2(circlePos.x, circlePos.y), 30)) {
                return otherArrow;
            }
        }

        return null;
    }

    /**
     * 查看当前障碍物是否可以从当前方向移动出去
     * @param obs 障碍物
     * @param angle 角度
     * @returns 
     */
    canMoveTowardObs(obs: Obstacle, angle: number): Arrow | Obstacle {
        if (!obs) return null;
        const obsPos = obs.node.getWorldPosition();
        const direction = Utils.getDirectionFromRotation(angle).normalize();
        const lineStart = new Vec2(obsPos.x, obsPos.y);
        const lineEnd = new Vec2(lineStart.x + direction.x * 1000, lineStart.y + direction.y * 1000);
        //获取最近的物体

        let _otherArrow: Arrow = null;
        let _obs: Obstacle = null;
        //检测是否与箭头发生碰撞
        for (const otherArrow of this.arrows) {
            if (otherArrow.isOver) continue;
            if (Intersection2D.linePolygon(lineStart, lineEnd, Utils.getRotatedVecticesArrow(otherArrow))) {

                if (_otherArrow == null) {
                    _otherArrow = otherArrow;
                } else {
                    //计算距离
                    let d1 = Vec3.distance(obs.node.getWorldPosition(), otherArrow.node.getWorldPosition());
                    let d2 = Vec3.distance(obs.node.getWorldPosition(), _otherArrow.node.getWorldPosition());
                    if (d2 > d1) {
                        _otherArrow = otherArrow;
                    }
                }
            }
        }

        //检测是否与障碍物发生碰撞
        for (const otherObs of this.obsAry) {
            if (obs == otherObs) continue; // 跳过自身
            if (otherObs.isOver) continue;
            if (Intersection2D.linePolygon(lineStart, lineEnd, Utils.getRotatedVecticesArrow(otherObs))) {
                if (_obs == null) {
                    _obs = otherObs;
                } else {
                    //计算距离
                    let d1 = Vec3.distance(obs.node.getWorldPosition(), otherObs.node.getWorldPosition());
                    let d2 = Vec3.distance(obs.node.getWorldPosition(), _obs.node.getWorldPosition());
                    if (d2 > d1) {
                        _obs = otherObs;
                    }
                }
            }
        }

        if (_otherArrow && _obs) {
            let d1 = Vec3.distance(obs.node.getWorldPosition(), _otherArrow.node.getWorldPosition());
            let d2 = Vec3.distance(obs.node.getWorldPosition(), _obs.node.getWorldPosition());
            if (d1 > d2) {
                return _obs;
            } else {
                return _otherArrow;
            }
        } else if (_obs) {
            return _obs;
        } else if (_otherArrow) {
            return _otherArrow;
        }
        return null;

    }

    /**
     * 查看当前障碍物是否可以移动
     * @param obs 障碍物
     */
    canMoveObsToPoint(obs: Obstacle): Arrow | Obstacle {
        if (!obs) return null;
        let obsArray = Utils.getRotatedVecticesArrow(obs);

        //检测是否碰撞到箭头
        let _otherArrow: Arrow = null;
        for (const otherArrow of this.arrows) {
            if (otherArrow.isOver) continue;
            if (Intersection2D.polygonPolygon(obsArray, Utils.getRotatedVecticesArrow(otherArrow))) {
                _otherArrow = otherArrow;
                break;
            }
        }

        if (_otherArrow) {
            return _otherArrow;
        }

        //检测是否碰撞到障碍物
        let _obs: Obstacle = null;
        for (const otherObs of this.obsAry) {
            if (obs == otherObs) continue; // 跳过自身
            if (otherObs.isOver) continue;
            if (Intersection2D.polygonPolygon(obsArray, Utils.getRotatedVecticesArrow(otherObs))) {
                _obs = otherObs;
                break;
            }
        }
        if (_obs) {
            return _obs;
        }
        return null;
    }

    camMoveObsForgPoint(pos: Vec3): Array<Circle> {
        //当前青蛙所在的圆
        let circle = this.getPointCircle(pos);
        //所有青蛙可以移动的圆点
        let circles = this.getAllFrogCircle();
        let list: Array<Circle> = [];
        for (let i = 0; i < circles.length; i++) {
            let c = circles[i];
            if (c == circle) continue; // 跳过自身
            //获取当前圆点是否与别的矩形由碰撞------ 没有才继续计算
            if (this.getCircleCollisons(c)) {
                continue;
            }

            list.push(c);
        }
        let radius = this.levelData.radius || 30;

        // //使用递归的方式 检测当前点指定方向的圆
        // function checkPointDirCircles(c: Circle) {
        //     let angles = [0, 180, 60, 120, -122, -60];
        //     let ciscles: Circle[] = [];
        //     for (let i = 0; i < angles.length; i++) {
        //         //获取当前角度方向
        //         let dir = Utils.getDirectionFromRotation(angles[i]);
        //         let pos = c.node.getWorldPosition();
        //         const targetPos = new Vec2(
        //             pos.x + dir.x * 200, // 假设移动到足够远的位置
        //             pos.y + dir.y * 200
        //         );


        //         for (let j = 0; j < list.length; j++) {
        //             let nc = list[j];
        //             if (nc == c) continue; // 跳过自身


        //             if (Utils.lineCircle(new math.Vec2(pos.x, pos.y), targetPos, new math.Vec2(nc.node.worldPositionX, nc.node.worldPositionY), radius)) {
        //                 //这里可以移动
        //                 ciscles.push(nc);
        //                 break;
        //             }
        //         }
        //     }
        //     return ciscles;
        // }

        // /**
        // * 递归查找所有可移动路径
        // * @param c 当前圆点
        // * @param list 可移动的圆点集合
        // * @param radius 圆点半径
        // * @param path 当前路径（递归用）
        // * @param visited 已访问集合（递归用）
        // * @returns 所有可移动路径（每条路径为数组）
        // */
        // function findAllMovePaths(
        //     c: Circle,
        //     list: Circle[],
        //     radius: number,
        //     path: Circle[] = [],
        //     visited: Set<number> = new Set(),
        //     depth: number = 0,
        //     maxDepth: number = 10 // 可根据实际情况调整
        // ): Circle[][] {
        //     if (depth > maxDepth) return [];
        //     let angles = [0, 180, 60, 120, -122, -60];
        //     let result: Circle[][] = [];
        //     path.push(c);
        //     visited.add(c.index);

        //     let foundNext = false;
        //     for (let i = 0; i < angles.length; i++) {
        //         let dir = Utils.getDirectionFromRotation(angles[i]);
        //         let pos = c.node.getWorldPosition();
        //         const targetPos = new Vec2(
        //             pos.x + dir.x * 200,
        //             pos.y + dir.y * 200
        //         );

        //         for (let j = 0; j < list.length; j++) {
        //             let nc = list[j];
        //             if (nc == c || visited.has(nc.index)) continue;

        //             if (Utils.lineCircle(new math.Vec2(pos.x, pos.y), targetPos, new math.Vec2(nc.node.worldPositionX, nc.node.worldPositionY), radius)) {
        //                 foundNext = true;
        //                 result.push(...findAllMovePaths(nc, list, radius, path, visited, depth + 1, maxDepth));
        //             }
        //         }
        //     }

        //     if (!foundNext && path.length > 1) {
        //         result.push([...path]);
        //     }

        //     path.pop();
        //     visited.delete(c.index);

        //     return result;
        // }

        // function findAllMovePathsIterative(
        //     start: Circle,
        //     list: Circle[],
        //     radius: number,
        //     maxDepth: number = 15
        // ): Circle[][] {
        //     let angles = [0, 180, 60, 120, -122, -60];
        //     let result: Circle[][] = [];
        //     // 栈元素：{ path, visited }
        //     let stack: { path: Circle[], visited: Set<number> }[] = [
        //         { path: [start], visited: new Set([start.index]) }
        //     ];

        //     while (stack.length > 0) {
        //         let { path, visited } = stack.pop();
        //         let c = path[path.length - 1];
        //         let foundNext = false;

        //         for (let i = 0; i < angles.length; i++) {
        //             let dir = Utils.getDirectionFromRotation(angles[i]);
        //             let pos = c.node.getWorldPosition();
        //             const targetPos = new Vec2(
        //                 pos.x + dir.x * 200,
        //                 pos.y + dir.y * 200
        //             );

        //             for (let j = 0; j < list.length; j++) {
        //                 let nc = list[j];
        //                 if (nc == c || visited.has(nc.index)) continue;

        //                 if (Utils.lineCircle(new math.Vec2(pos.x, pos.y), targetPos, new math.Vec2(nc.node.worldPositionX, nc.node.worldPositionY), radius)) {
        //                     foundNext = true;
        //                     if (path.length < maxDepth) {
        //                         let newPath = [...path, nc];
        //                         let newVisited = new Set(visited);
        //                         newVisited.add(nc.index);
        //                         stack.push({ path: newPath, visited: newVisited });
        //                     }
        //                 }
        //             }
        //         }

        //         if (!foundNext && path.length > 1) {
        //             result.push(path);
        //         }
        //     }
        //     return result;
        // }

        // function findAllMovePathsIterativeLimited(
        //     start: Circle,
        //     list: Circle[],
        //     radius: number,
        //     maxDepth: number = 20,
        //     maxPaths: number = 1000
        // ): Circle[][] {
        //     let angles = [0, 180, 60, 120, -122, -60];
        //     let result: Circle[][] = [];
        //     let stack: { path: Circle[], visited: Set<number> }[] = [
        //         { path: [start], visited: new Set([start.index]) }
        //     ];

        //     while (stack.length > 0 && result.length < maxPaths) {
        //         let { path, visited } = stack.pop();
        //         let c = path[path.length - 1];
        //         let foundNext = false;

        //         for (let i = 0; i < angles.length; i++) {
        //             let dir = Utils.getDirectionFromRotation(angles[i]);
        //             let pos = c.node.getWorldPosition();
        //             const targetPos = new Vec2(
        //                 pos.x + dir.x * 200,
        //                 pos.y + dir.y * 200
        //             );

        //             for (let j = 0; j < list.length; j++) {
        //                 let nc = list[j];
        //                 if (nc == c || visited.has(nc.index)) continue;

        //                 if (Utils.lineCircle(new math.Vec2(pos.x, pos.y), targetPos, new math.Vec2(nc.node.worldPositionX, nc.node.worldPositionY), radius)) {
        //                     foundNext = true;
        //                     if (path.length < maxDepth) {
        //                         let newPath = [...path, nc];
        //                         let newVisited = new Set(visited);
        //                         newVisited.add(nc.index);
        //                         stack.push({ path: newPath, visited: newVisited });
        //                     }
        //                 }
        //             }
        //         }

        //         if (!foundNext && path.length > 1) {
        //             result.push(path);
        //         }
        //     }
        //     return result;
        // }
        // let allPaths = findAllMovePathsIterativeLimited(circle, list, radius);
        //         /**
        //  * 六方向BFS查找所有点的最短路径（每个点只访问一次）
        //  * @param start 起始圆点
        //  * @param list 可达圆点集合
        //  * @param radius 判定半径
        //  * @param maxDepth 最大深度
        //  * @returns Map<点index, 路径数组>
        //  */
        //         function findShortestPathsSixDirections(
        //             start: Circle,
        //             list: Circle[],
        //             radius: number,
        //             maxDepth: number = 20
        //         ): Map<number, Circle[]> {
        //             let angles = [0, 180, 60, 120, -120, -60];
        //             let queue: { path: Circle[] }[] = [{ path: [start] }];
        //             let visited = new Set<number>([start.index]);
        //             let shortestPaths = new Map<number, Circle[]>();
        //             shortestPaths.set(start.index, [start]);

        //             while (queue.length > 0) {
        //                 let { path } = queue.shift();
        //                 let cur = path[path.length - 1];
        //                 if (path.length > maxDepth) continue;

        //                 for (let i = 0; i < angles.length; i++) {
        //                     let dir = Utils.getDirectionFromRotation(angles[i]);
        //                     let pos = cur.node.getWorldPosition();
        //                     const targetPos = new Vec2(
        //                         pos.x + dir.x * 2000,
        //                         pos.y + dir.y * 2000
        //                     );
        //                     let nearest: Circle = null;
        //                     let minDist = Infinity;
        //                     for (let j = 0; j < list.length; j++) {
        //                         let nc = list[j];
        //                         if (nc == cur || visited.has(nc.index)) continue;
        //                         let ncPos = nc.node.getWorldPosition();
        //                         if (Utils.lineCircle(new math.Vec2(pos.x, pos.y), targetPos, new math.Vec2(ncPos.x, ncPos.y), radius)) {
        //                             let dist = Vec3.distance(pos, ncPos);
        //                             if (dist < minDist) {
        //                                 minDist = dist;
        //                                 nearest = nc;
        //                             }
        //                         }
        //                     }
        //                     if (nearest) {
        //                         visited.add(nearest.index);
        //                         let newPath = [...path, nearest];
        //                         shortestPaths.set(nearest.index, newPath);
        //                         queue.push({ path: newPath });
        //                     }
        //                 }
        //             }
        //             return shortestPaths;
        //         }
        //         let allPaths = findShortestPathsSixDirections(circle, list, radius) as any;

        let allPaths = this.spreadReachableCirclesArray(circle, list, radius);
        return allPaths;
    }

    /**
    * 六方向扩散查找所有可达点，返回数组形式的 Circle
    * @param start 起始圆点
    * @param list 可达圆点集合
    * @param radius 判定半径
    * @returns Circle[] 所有可达点
    */
    spreadReachableCirclesArray(
        start: Circle,
        list: Circle[],
        radius: number
    ): Circle[] {
        let angles = [0, 180, 60, 120, -120, -60];
        let visited = new Set<number>();
        let queue: Circle[] = [start];
        visited.add(start.index);

        while (queue.length > 0) {
            let cur = queue.shift();
            let pos = cur.node.getWorldPosition();

            for (let i = 0; i < angles.length; i++) {
                let dir = Utils.getDirectionFromRotation(angles[i]);
                const targetPos = new Vec2(
                    pos.x + dir.x * 200,
                    pos.y + dir.y * 200
                );
                let nearest: Circle = null;
                let minDist = Infinity;
                for (let j = 0; j < list.length; j++) {
                    let nc = list[j];
                    if (nc == cur || visited.has(nc.index)) continue;
                    let ncPos = nc.node.getWorldPosition();
                    if (Utils.lineCircle(new math.Vec2(pos.x, pos.y), targetPos, new math.Vec2(ncPos.x, ncPos.y), radius)) {
                        let dist = Vec3.distance(pos, ncPos);
                        if (dist < minDist) {
                            minDist = dist;
                            nearest = nc;
                        }
                    }
                }
                if (nearest && !visited.has(nearest.index)) {
                    visited.add(nearest.index);
                    queue.push(nearest);
                }
            }
        }
        // 返回所有可达点的 Circle 数组（不含起点）
        return list.filter(c => visited.has(c.index) && c !== start);
    }

    findPathToTarget(pos: Vec3,
        targetIndex: number,
        list: Circle[],
        maxDepth: number = 20) {
        //当前青蛙所在的圆
        let start = this.getPointCircle(pos);
        let angles = [0, 180, 60, 120, -120, -60];
        let queue: { path: Circle[], visited: Set<number> }[] = [
            { path: [start], visited: new Set([start.index]) }
        ];
        let radius = this.levelData.radius || 30;


        while (queue.length > 0) {
            let { path, visited } = queue.shift();
            let cur = path[path.length - 1];
            if (cur.index === targetIndex) {
                return path;
            }
            if (path.length > maxDepth) continue;

            let pos = cur.node.getWorldPosition();
            for (let i = 0; i < angles.length; i++) {
                let dir = Utils.getDirectionFromRotation(angles[i]);
                const targetPos = new Vec2(
                    pos.x + dir.x * 200,
                    pos.y + dir.y * 200
                );
                let nearest: Circle = null;
                let minDist = Infinity;
                for (let j = 0; j < list.length; j++) {
                    let nc = list[j];
                    if (nc == cur || visited.has(nc.index)) continue;
                    let ncPos = nc.node.getWorldPosition();
                    if (Utils.lineCircle(new math.Vec2(pos.x, pos.y), targetPos, new math.Vec2(ncPos.x, ncPos.y), radius)) {
                        let dist = Vec3.distance(pos, ncPos);
                        if (dist < minDist) {
                            minDist = dist;
                            nearest = nc;
                        }
                    }
                }
                if (nearest && !visited.has(nearest.index)) {
                    let newPath = [...path, nearest];
                    let newVisited = new Set(visited);
                    newVisited.add(nearest.index);
                    queue.push({ path: newPath, visited: newVisited });
                }
            }
        }
        return null;
    }


    checkPointDirCircles(point: Vec3, angle: number, fillc: Circle = null, list: Array<Circle> = []) {
        //获取当前角度方向
        let dir = Utils.getDirectionFromRotation(angle);

        const targetPos = new Vec2(
            point.x + dir.x * 1000, // 假设移动到足够远的位置
            point.y + dir.y * 1000
        );
        let radius = this.levelData.radius || 30;

        let circles: Circle[] = list || [];
        for (const circle of this.circles) {
            if (circle == fillc) continue; // 跳过自身

            const circlePos = circle.node.getWorldPosition();
            //计算当前方向的线段与圆点的碰撞



            if (Utils.lineCircle(new math.Vec2(point.x, point.y), targetPos, new math.Vec2(circlePos.x, circlePos.y), radius)) {
                if (this.getCircleCollisons(circle)) {
                    //如果当前圆点上有箭头或者障碍物则跳过
                    continue;
                }
                circles.push(circle);
            }
        }

        return circles;
    }

    //查看当前点对应角度方向上的圆点
    checkPointDirCircle(point: Vec3, angle: number, fillc: Circle = null, first: boolean = false) {
        //获取当前角度方向
        let dir = Utils.getDirectionFromRotation(angle);

        const targetPos = new Vec2(
            point.x + dir.x * 1000, // 假设移动到足够远的位置
            point.y + dir.y * 1000
        );
        let radius = this.levelData.radius || 30;

        let _circle: Circle;
        for (const circle of this.circles) {
            if (circle == fillc) continue; // 跳过自身
            const circlePos = circle.node.getWorldPosition();
            //计算当前方向的线段与圆点的碰撞

            if (Utils.lineCircle(new math.Vec2(point.x, point.y), targetPos, new math.Vec2(circlePos.x, circlePos.y), radius)) {
                if (first) {
                    //查找最近的
                    if (_circle == null) {
                        _circle = circle;
                    } else {
                        let d1 = Vec3.distance(point, _circle.node.getWorldPosition());
                        let d2 = Vec3.distance(point, circlePos);
                        if (d1 > d2) {
                            _circle = circle;
                        }
                    }
                } else {
                    _circle = circle;
                    break;
                }
            }
        }
        return _circle;
    }
    //查看点上是否存在箭头或者障碍物
    checkIsPointArrowOrObs(circle: Circle, f: Arrow | Obstacle): Arrow | Obstacle {
        let radius = this.levelData.radius || 30;

        //查看点上是否存在箭头
        for (const otherArrow of this.arrows) {
            if (otherArrow == f) {
                continue;
            }
            if (otherArrow.isOver) continue;
            let distance = Vec3.distance(circle.node.getWorldPosition(), otherArrow.node.getWorldPosition());
            if (distance < radius) {
                return otherArrow; // 返回碰撞的箭头
            }
        }

        //查看点上是否存在障碍物
        for (const obs of this.obsAry) {
            if (obs == f) {
                continue;
            }
            if (obs.isOver) continue; // 跳过自身
            let distance = Vec3.distance(circle.node.getWorldPosition(), obs.node.getWorldPosition());
            if (distance < radius) {
                return obs; // 返回碰撞的箭头
            }
        }



        return null;
    }

    /**
     * 检查障碍鱼是否可以移动
     * @param fish 障碍鱼
     */
    checkFishMove(fish: ObstacleFish) {
        let point = fish.node.getWorldPosition();

        let dir = Utils.getDirectionFromRotation(fish.node.angle);
        let targetPos = new Vec2(
            point.x + dir.x * 1000, // 假设移动到足够远的位置
            point.y + dir.y * 1000
        );

        let otherFish: ObstacleFish = null;

        //查找和鱼相对应的鱼 鱼头对鱼头的可以移动
        for (const obs of this.obsAry) {
            if (obs == fish) continue; // 跳过自身
            if (obs.isOver) continue; // 跳过自身
            if (obs.node.angle == fish.node.angle) continue; // 角度一致的跳过
            if (Intersection2D.linePolygon(new Vec2(point.x, point.y), targetPos, Utils.getRotatedVecticesArrow(obs))) {
                //如果碰撞到其他障碍物 则不能移动
                if (Math.abs(fish.node.angle) + Math.abs(obs.node.angle) == 180) {
                    if (otherFish == null) {
                        otherFish = obs as ObstacleFish; // 找到对应的鱼
                    } else {
                        //计算距离 获取最短的鱼
                        let d1 = Vec3.distance(point, otherFish.node.getWorldPosition());
                        let d2 = Vec3.distance(point, obs.node.getWorldPosition());
                        if (d1 > d2) {
                            otherFish = obs as ObstacleFish;
                        }
                    }
                    // console.log("可以移动");
                    // break;
                }
            }
        }

        if (otherFish) {
            let distance = Vec3.distance(point, otherFish.node.getWorldPosition());
            targetPos.x = point.x + dir.x * distance;
            targetPos.y = point.y + dir.y * distance;

            let isMove = true;
            //查看当前方向与其他障碍物是否碰撞
            for (const obs of this.obsAry) {
                if (obs == fish || obs == otherFish) continue; // 跳过自身
                if (obs.isOver) continue; // 跳过自身
                if (Intersection2D.linePolygon(new Vec2(point.x, point.y), targetPos, Utils.getRotatedVecticesArrow(obs))) {
                    isMove = false; // 不能移动
                    break;
                }
            }

            if (isMove) {
                //查看当前方向与其他箭头是否碰撞
                for (const arrow of this.arrows) {
                    if (arrow.isOver) continue; // 跳过自身
                    if (Intersection2D.linePolygon(new Vec2(point.x, point.y), targetPos, Utils.getRotatedVecticesArrow(arrow))) {
                        isMove = false; // 不能移动
                        break;
                    }
                }

                //可以移动
                if (isMove) {
                    console.log("可以移动");
                    //计算两条鱼中间点
                    let midPoint = new Vec3(
                        (point.x + otherFish.node.getWorldPosition().x) / 2,
                        (point.y + otherFish.node.getWorldPosition().y) / 2
                    );
                    let p = midPoint.clone();
                    //分别将两条鱼移动到中间点
                    fish.moveToPoint(midPoint, () => {
                        this.playHurtEffect(p);
                    });
                    otherFish.moveToPoint(midPoint, () => {

                    });

                } else {
                    console.log("碰撞到箭头");
                }
            } else {
                console.log("碰撞到障碍物");
            }
        }

    }

    playHurtEffect(pos: Vec3) {
        resources.load("prefabs/EffectHurt", Prefab, (err, prefab) => {
            if (err) {
                console.error(err);
                return;
            }
            let hurtEffect = instantiate(prefab);
            this.dir.addChild(hurtEffect);
            hurtEffect.setWorldPosition(pos);
        });
    }

    checkIsPut(arrow: Arrow, circle: Circle) {
        let angle = Utils.calculateTouchAngle(circle.node.getWorldPosition(), arrow.node.getWorldPosition());
        let otherArrow = Engine.instance.canMoveTowardsHighlightedCircle(arrow, angle, false);

        if (!otherArrow) {
            //没有圆点可以拦住---
            //检测当前方向线段是否有碰撞到箭头
            otherArrow = Engine.instance.canMoveTowardArrow(arrow, circle);
            if (!otherArrow) {
                return otherArrow;
            }
        }
        return otherArrow;
    }

    showMask(ismask: boolean, msg: string = "") {
        this.mask.active = true;
        this.mask.children[0].active = ismask;
        this.mask.children[1].active = ismask;
        if (msg != "") {
            this.mask.children[1].getComponent(Label).string = msg;
        }
    }

    hideMask() {
        this.mask.active = false;
    }

    checkIsGameOVer() {
        if (this.isGameOver) return;
        let isOver = true;
        for (const arrow of this.arrows) {
            if (!arrow.isOver) {
                isOver = false;
                break;
            }
        }

        for (const obs of this.obsAry) {
            if (obs instanceof ObstacleFish && !obs.isOver) {
                isOver = false;
                break;
            }
        }

        if (isOver) {
            this.isGameOver = true;
            console.log("游戏结束  引导数据");
            console.log(JSON.stringify(this.guides));



            // //查看当前关卡是否大于等于当前最大关卡
            if (this.curLevel >= GlobalConfig.DB.level) {

                //更新最大关卡
                localStorage.setItem('curLevel', (this.curLevel + 1).toString());
                Engine.instance.jsb.completeLevel(this.curLevel, GlobalConfig.maxLevel);
                // this.showLottery();
                this.showWin();

            } else {
                this.showWin();
            }

        }
    }

    clearAllArrow() {
        for (let i = 0; i < this.arrows.length; i++) {
            this.arrows[i].clear();
        }
        this.arrows = [];
    }

    clearAllCircle() {
        for (let i = 0; i < this.circles.length; i++) {
            this.circles[i].clear();
        }
        this.circles = [];
    }

    clearAllObs() {
        for (let i = 0; i < this.obsAry.length; i++) {
            this.obsAry[i].node.destroy();
        }
        this.obsAry = [];
    }

    hideAllCircle() {
        for (let i = 0; i < this.circles.length; i++) {
            this.circles[i].removeHighlight();
        }
    }

    hideAllFrog() {
        for (let i = 0; i < this.obsAry.length; i++) {
            this.obsAry[i].playIdle();
        }
    }

    nextLevel() {
        this.clearAllArrow();
        this.clearAllCircle();
        this.clearAllObs();

        let l = this.curLevel + 1;
        if (l >= GlobalConfig.maxLevel) {
            l = GlobalConfig.maxLevel;
        }
        this.curLevel = l;

        this.showMask(true);
        this.refreshLevelLabel();

        setTimeout(() => {
            this.generateLevel(this.curLevel);
        }, 1);
    }

    startLevel() {
        this.onStartLevel(GlobalConfig.DB.level);
    }

    refreshLevel() {
        this.clearAllArrow();
        this.clearAllCircle();
        this.clearAllObs();
        this.onStartLevel(this.curLevel);
    }

    onStartLevel(l: number) {
        this.curLevel = l;
        this.loadLevelData(this.curLevel);
        this.uimain.active = false;
        this.uigame.active = true;
    }

    onSettingClick() {
        this.setting.show(false);
    }

    onGameSettingClick() {
        this.setting.show(true);
    }

    onSelectLevelClick() {
        let l = this.getComponentInChildren(UISelectLevel);
        l.show();
    }

    onExitClick() {
        this.clearAllArrow();
        this.clearAllCircle();
        this.clearAllObs();

        this.uigame.active = false;
        this.uimain.active = true;

        Engine.instance.jsb.Jsb.showInstertView('');
    }

    changeMusic(s: boolean) {
        // if (s) {
        //     //播放音效
        // } else {
        //     //停止音效
        // }
        //获取本地的值
        let key = localStorage.getItem('music');
        if (key && key != "") {
            let flag = parseInt(key) == 1;
            if (flag) {
                // this.musicSource.play();
                this.playMusic();
            } else {
                this.musicSource.stop();
            }
        }
    }

    playMusic() {
        let key = localStorage.getItem('music');
        if (key && key != "") {
            let flag = parseInt(key) == 1;
            if (flag) {
                resources.load('audio/bgm', AudioClip, (err, clip) => {
                    if (err) return;
                    let key = localStorage.getItem('music');
                    if (key && key != "") {
                        let flag = parseInt(key) == 1;
                        if (!flag) {
                            this.musicSource.stop();
                            return;
                        }
                    }

                    this.musicSource.clip = clip;
                    this.musicSource.play();
                });
            } else {
                this.musicSource.stop();
            }
        } else {
            resources.load('audio/bgm', AudioClip, (err, clip) => {
                if (err) return;
                let key = localStorage.getItem('music');
                if (key && key != "") {
                    let flag = parseInt(key) == 1;
                    if (!flag) {
                        this.musicSource.stop();
                        return;
                    }
                }

                this.musicSource.clip = clip;
                this.musicSource.play();
            });
        }
    }

    playSound(sound: string) {
        let key = localStorage.getItem('sound');
        if (key && key != "") {
            let flag = parseInt(key) == 1;
            if (flag) {
                resources.load("audio/" + sound, AudioClip, (err, clip) => {
                    if (err) return;
                    this.musicSource.playOneShot(clip);
                });
            }
        } else {
            resources.load("audio/" + sound, AudioClip, (err, clip) => {
                if (err) return;
                this.musicSource.playOneShot(clip);
            });
        }
    }

    playShake() {
        let key = localStorage.getItem('shake');
        if (key && key != "") {
            let flag = parseInt(key) == 1;
            if (flag) {
                Engine.instance.jsb.Jsb.openVibrateShort();
            }
        } else {
            Engine.instance.jsb.Jsb.openVibrateShort();
        }
    }

    onFriendClick() {
        resources.load('prefabs/UIFriend', Prefab, (err, fab) => {
            if (err) {
                return;
            }

            this.node.addChild(instantiate(fab));
        })
    }

    changeAdVisible(v: boolean) {
        for (let i = 0; i < this.adNodes.length; i++) {
            this.adNodes[i].active = v;
        }
    }

    showTipNode(pos: Vec3) {
        Tween.stopAllByTarget(this.tipNode);
        this.tipNode.active = true;
        this.tipNode.setWorldPosition(pos);
        // tween(this.tipNode).delay(2).call(() => {
        //     this.tipNode.active = false;
        // })
        setTimeout(() => {
            this.tipNode.active = false;
        }, 2000);
    }

    getAllFrogCircle() {
        // if (this.frogCircles.length > 0) {
        //     return this.frogCircles;
        // }
        // for (let i = 0; i < this.circles.length; i++) {
        //     let circle = this.circles[i];
        //     if (this.circles.length == 95) {
        //         if ((circle.index + 1) % 3 == 0) {
        //             this.frogCircles.push(circle);
        //         }
        //     } else if (this.circles.length == 149) {
        //         if ((circle.index) % 3 == 0) {
        //             this.frogCircles.push(circle);
        //         }
        //     }
        // }

        // return this.frogCircles;
        return this.circles;
    }

    // isClickClear: boolean = false;
    isTip: boolean = false;
    onHelpClick() {
        //播放广告-----
        // this.isClickClear = true;
        this.changeAdVisible(true);
    }

    onCheckClick() {

        this.jsb.playRewardVideo("", null).then((code: SwitchType) => {
            if (code == SwitchType.On) {
                if (!this.isTip) {
                    this.isTip = true;
                    // let datas = [];


                    //获取当前关卡引导索引表中 最靠前的箭头
                    for (let i = 0; i < this.curGuideData.length; i++) {
                        let guideData = this.curGuideData[i];
                        if (guideData[2] == 'obs') {
                            //属于障碍物
                            let obsid = guideData[0];
                            let obs = this.obsAry.find((o) => o.index == obsid);
                            if (obs == null) {
                                continue;
                            }
                            if (obs.isOver) {
                                continue;
                            }
                            //let angle = guideData[1];
                            resources.load('prefabs/' + guideData[3], Prefab, (err, fab) => {
                                if (err) {
                                    return;
                                }
                                let obsNode = instantiate(fab);
                                obsNode.setPosition(obs.node.getPosition());
                                this.dir.addChild(obsNode);
                                obsNode.angle = guideData[1];
                                //计算当前角度对应的方向
                                let direction = Utils.getDirectionFromRotation(guideData[1]);
                                let to = direction.normalize().multiplyScalar(300);
                                tween(obsNode).to(1, {
                                    position: to.add(obsNode.position)
                                }).call(() => {
                                    obsNode.position = obs.node.getPosition()
                                }).union().repeatForever().start();
                                obsNode.name = 'guideArrow';
                            });
                            this.showAdTipPlane(obs.node.getWorldPosition());
                            break;
                        } else {
                            let arrowid = guideData[0];
                            let circleid = guideData[1];
                            let arrow = this.arrows.find((a) => a.circleId == arrowid);
                            if (arrow == null) {
                                continue;
                            }

                            if (arrow.isOver) {
                                continue;
                            }
                            if (arrow) {
                                let circle = this.circles.find((c) => c.index == circleid);

                                let angle = Utils.calculateTouchAngle(circle.node.getWorldPosition(), arrow.node.getWorldPosition());

                                let newArrow = this.createArrow(arrow.circleId, angle, arrow.width, false);
                                //移除newArrow节点上所有点击事件

                                newArrow.node.addComponent(UIOpacity).opacity = 255 * 0.5;
                                newArrow.node.name = 'guideArrow';
                                let pos = arrow.node.getPosition();

                                newArrow.node.angle = angle;

                                console.log(angle);
                                //计算当前角度对应的方向
                                let direction = Utils.getDirectionFromRotation(angle);
                                let to = direction.normalize().multiplyScalar(300);
                                tween(newArrow.node).to(1, {
                                    position: to.add(newArrow.node.position)
                                }).call(() => {
                                    newArrow.node.position = pos;
                                }).union().repeatForever().start();

                                arrow.changeSpColor(2);

                                this.showAdTipPlane(arrow.touSp.node.getWorldPosition());
                                break;
                            }
                        }
                    }

                    // this.btnTipLabel.string = '移除';
                    this.btnTip.active = false;
                    this.btnRemove.active = true;
                } else {
                    //移除当前的箭头
                    //获取当前关卡引导索引表中 最靠前的箭头
                    for (let i = 0; i < this.curGuideData.length; i++) {
                        let guideData = this.curGuideData[i];
                        if (guideData[2] == 'obs') {
                            //属于障碍物
                            let obsid = guideData[0];
                            let obs = this.obsAry.find((o) => o.index == obsid);
                            if (obs == null) {
                                continue;
                            }
                            if (obs.isOver) {
                                continue;
                            }
                            obs.node.angle = guideData[1];
                            obs.move();
                            break;
                        } else {
                            let arrowid = guideData[0];
                            let circleid = guideData[1];
                            let arrow = this.arrows.find((a) => a.circleId == arrowid);
                            if (arrow == null) {
                                continue;
                            }
                            if (arrow.isOver) {
                                continue;
                            }
                            let circle = this.circles.find((c) => c.index == circleid);

                            let angle = Utils.calculateTouchAngle(circle.node.getWorldPosition(), arrow.node.getWorldPosition());
                            arrow.node.angle = angle;
                            arrow.move(null);
                            break;
                        }
                    }

                    this.clearArrowTip();
                }
            }
        })
    }

    showAdTipPlane(pos: Vec3) {
        this.adTipPlane.active = true;
        this.adTipPlane.setWorldPosition(pos.add3f(0, 50, 0));
        setTimeout(() => {
            this.adTipPlane.active = false;
        }, 3000);
    }

    clearArrowTip() {
        this.isTip = false;
        this.btnTip.active = true;
        this.btnRemove.active = false;
        // this.btnTipLabel.string = '提示';
        for (let i = 0; i < this.arrows.length; i++) {
            this.arrows[i].changeSpColor(0);
        }

        this.hideAllCircle();
        let arrow = this.dir.getChildByName('guideArrow');
        if (arrow) {
            arrow.destroy();
        }
        // this.changeAdVisible(false);
    }

    onPutRecord(arrow: Arrow) {
        this.records.push(arrow);
        if (arrow) {
            if (arrow.moveCircle) {
                this.guides.push([
                    arrow.circleId,
                    arrow.moveCircle.index
                ])
            } else {
                console.log("记录错误")
            }
        }
        // console.log("记录当前的箭头", arrow.circleId, arrow.node.angle);

        // console.log(JSON.stringify(this.guides));

    }

    onPutRecordObs(obs: Obstacle) {
        this.records.push(obs);

        this.guides.push([
            //位置索引
            obs.index,
            //障碍物旋转角度
            obs.node.angle,
            //类型
            'obs',
            obs.node.name
        ])

        // console.log(JSON.stringify(this.guides));

    }

    addCoin(coin: number) {

        GlobalConfig.DB.coin += coin;
        SqlUtil.set('coin', GlobalConfig.DB.coin);
    }

    saveDB() {
    }

    showLottery() {
        console.log("showLottery")
        resources.load('prefabs/UILottery', Prefab, (err, prefab) => {
            if (err) return;
            let node = instantiate(prefab)
            this.node.addChild(node);

        });
    }

    showWin() {
        resources.load('prefabs/UIWin', Prefab, (err, prefab) => {
            if (err) return;
            let node = instantiate(prefab)
            this.node.addChild(node);
            let win = node.getComponent(UIWin);
            win.show();
        });
    }


    static OneDay = 1000 * 60 * 60 * 24;

    /**
     * 计算天数间隔  比如2022年11月22日20时10分和2022年11月23日01时是算一个间隔天
     * @param d1 
     * @param d2 
     */
    static DaysDiff(d1: number, d2: number) {
        let t1 = new Date(d1);
        let t2 = new Date(d2);
        t1.setHours(0, 0, 0, 0);
        t2.setHours(0, 0, 0, 0);


        // 计算时间差
        const timeDiff = Math.abs(t1.getTime() - t2.getTime());

        return Math.ceil(timeDiff / this.OneDay)
    }

    protected update(dt: number): void {
        if (!this.isGameOver) {
            if (this.curLevel > 2) {
                this.countDown -= dt;
                if (this.countDown <= 0) {
                    this.countDown = 0;
                    this.isGameOver = true;
                    resources.load('prefabs/UITimeOut', Prefab, (err, fab) => {
                        if (err) {
                            console.error(err);
                            return;
                        }
                        const gameOverNode = instantiate(fab);
                        this.node.addChild(gameOverNode);
                    });
                }
                //将倒计时秒转换为00:00格式
                let minutes = Math.floor(this.countDown / 60);
                let seconds = Math.floor(this.countDown % 60);
                this.timeLabel.string = (minutes < 10 ? '0' + minutes : minutes) + ':' + (seconds < 10 ? '0' + seconds : seconds);
            }
        }
    }

    /**
   * 复活
   */
    revive() {
        this.isGameOver = false;
        this.countDown = 300;
    }

}

