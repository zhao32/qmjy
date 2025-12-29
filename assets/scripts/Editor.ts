import { _decorator, Component, EditBox, EventTouch, instantiate, Intersection2D, JsonAsset, math, Node, Prefab, resources, TextAsset, Toggle, UITransform, Vec2, Vec3, Widget } from 'cc';
import { Arrow } from './Arrow';
import { Circle } from './Circle';
import { Utils } from './CocosUtils';
const { ccclass, property } = _decorator;

@ccclass('Editor')
export class Editor extends Component {
    static instance: Editor;
    public arrows: Arrow[] = [];
    public circles: Circle[] = []; // 存储所有圆点的数组

    @property(Toggle)
    toggleWidth: Toggle;

    @property(Toggle)
    toggleDelete: Toggle;

    @property(EditBox)
    inputLevel: EditBox;

    @property(JsonAsset)
    levelDatasAsset: JsonAsset;
    @property(Node)
    board: Node;

    @property(Node)
    dir: Node;

    @property(Prefab)
    circleFab: Prefab;

    @property(Prefab)
    arrowFab: Prefab;

    @property(Node)
    game: Node;

    @property(Node)
    itemContent: Node;

    @property(EditBox)
    inputScale: EditBox;

    @property([Prefab])
    obsFabs: Prefab[] = []; // 物体预制体数组

    @property
    cellSize: number = 60; // 每个格子的大小为 100x100
    levelDatas: Array<levels> = []; // 存储所有关卡数据的数组
    levelData: levels | null = null;

    itemIndex: number = 0;


    protected onLoad(): void {
        Editor.instance = this;
        this.levelDatas = this.levelDatasAsset.json as Array<levels>;

        this.node.on(Node.EventType.TOUCH_START, this.onTouchStart, this);
        this.node.on(Node.EventType.TOUCH_MOVE, this.onTouchMove, this);
        this.node.on(Node.EventType.TOUCH_END, this.onTouchEnd, this);

        this.onItemClick(null, 0);
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


    loadLevel() {
        //获取输入的关卡
        this.clearAllArrow();
        this.clearAllCircle();
        for (let i = 0; i < this.obsAry.length; i++) {
            this.obsAry[i].destroy();
        }
        this.obsAry = [];

        let lv = parseInt(this.inputLevel.string);
        this.generateLevel(lv)
    }

    onnextLevel(){
        let lv = parseInt(this.inputLevel.string) + 1;
        this.inputLevel.string = lv.toString();
        this.loadLevel();
    }

    exportClick() {
        let obj = [];

        for (let i = 0; i < this.arrows.length; i++) {
            let arrow = this.arrows[i];
            obj.push([
                arrow.index,
                Math.round(arrow.node.angle),
                Math.round(arrow.width)
            ])
        }
        obj.sort((a, b) => {
            return a[0] - b[0]
        })
        console.log("lv ", this.inputLevel.string);
        console.log("scale   ", this.game.getScale().x);
        // console.log(this.game.getScale().x);
        console.log("cellSizeX    ", this.levelData.cellSizeX);
        console.log("cellSizeY    ", this.levelData.cellSizeY);
        console.log("radius   ", this.levelData.radius || 30);
        console.log("layoutscale   ", this.levelData.layoutscale);
        console.log("cols   ", JSON.stringify(this.levelData.cols).substring(1, JSON.stringify(this.levelData.cols).length - 1));

        console.log("pigs 数据")
        console.log(JSON.stringify(obj));
        let obsData = [];

        for (let i = 0; i < this.obsAry.length; i++) {
            let obs = this.obsAry[i];
            obsData.push([
                obs['circle'].node.getSiblingIndex(),
                Math.round(obs.angle),
                obs.name
            ])
        }
        console.log("obs 数据")
        console.log(JSON.stringify(obsData));
    }

    isWidth() {
        return this.toggleWidth.isChecked;
    }


    generateLevel(lv: number) {
        this.levelData = this.levelDatas.find(p=>p.id == lv); // 获取当前关卡数据
        if (!this.levelData) {
            console.error('Invalid level data or level name.');
            return;
        }

        const cols = this.levelData.cols || []; // 每列的点数
        const scale = this.levelData.layoutscale || 1; // 父节点缩放

        this.inputScale.string = this.levelData.scale.toString(); // 设置缩放值
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


        let obs = JSON.parse(this.levelData.obs) || [];
        for (let o of obs) {
            resources.load("prefabs/" + o[2], Prefab, (err, fab) => {
                if (err) {
                    console.error(err);
                    return;
                }
                const scale = this.levelData.layoutscale || 1; // 父节点缩放
                let c = this.board.children[o[0]];
                // if(c){
                const pointNode = instantiate(fab);
                pointNode.setPosition(new Vec3(o[0], o[1], 0));
                this.dir.addChild(pointNode);
                pointNode.setPosition(c.getPosition().multiplyScalar(scale))
                pointNode.angle = o[1]; // 设置角度
                this.obsAry.push(pointNode);
                pointNode['circle'] = c.getComponent(Circle); // 将圆点组件赋值给障碍物
                // }
            })
        }

        // 初始化箭头
        const info = JSON.parse(this.levelData.pigs) || []; // 假设 `pigs_xy` 是箭头的初始位置
        info.forEach((info: Array<number>) => {
            this.createArrow(info[0], info[1], info[2]);
        });

    }

    onChangeScale() {
        console.log("onChangeScale", this.inputScale.string);

        let scale = parseFloat(this.inputScale.string);
        if (isNaN(scale)) {
            console.error('Invalid scale value.');
            return;
        }
        this.game.setScale(scale, scale, 1)
        // this.board.setScale(scale, scale, 1);
    }

    createGrids(cols: number[]) {
        //格子x间隔
        let cellSizeX = this.levelData.cellSizeX || this.cellSize; // 每个格子的大小为 100x100
        //格子y间隔
        let cellSizeY = this.levelData.cellSizeY || this.cellSize; // 每个格子的大小为 100x100

        for (let colIndex = 0; colIndex < cols.length; colIndex++) {
            // 计算当前列的水平位置
            const colX = (colIndex - (cols.length - 1) / 2) * cellSizeX; // 水平位置，保持水平间隔一致
            // 当前列的点数
            const numPoints = cols[colIndex]; // 当前列的点数
            // 垂直分布在中间
            const offsetY = (numPoints - 1) * cellSizeX / 2; // 垂直偏移量，保持垂直间隔一致
            // 遍历每列的点
            for (let rowIndex = 0; rowIndex < numPoints; rowIndex++) {
                // 计算点的垂直位置
                const posY = rowIndex * cellSizeY - offsetY; // 垂直位置，保持垂直间隔一致
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
        pointNode.children[2].active = true;
    }

    createArrow(slibling: number, angle: number, width: number) {
        // console.log(slibling)
        const scale = this.levelData.layoutscale || 1; // 父节点缩放

        const arrowNode = instantiate(this.arrowFab);
        let p = this.board.children[slibling];
        if (!p) return;

        arrowNode.setPosition(p.getPosition().multiplyScalar(scale))
        // arrowNode.setRotationFromEuler(0, 0, this.getRotationFromDirection(direction));
        this.dir.addChild(arrowNode);
        arrowNode.name = `arrow_${slibling}`; // 设置箭头的名称
        let arrow = arrowNode.getComponent(Arrow);
        arrow.index = slibling;
        this.arrows.push(arrow);
        arrow.points[0].active = true;
        arrow.points[1].active = true;
        arrow.points[2].active = true;
        arrow.points[3].active = true;
        arrow.node.children[0].active = true;

        arrow.init(angle, width, slibling, false); // 初始化箭头
        p.getComponent(Circle)['arrow'] = arrow;
        arrow['circle'] = p.getComponent(Circle);

        arrow.touSp.getComponent(Widget).alignMode = Widget.AlignMode.ALWAYS;
        return arrow;
    }


    collisonArrow: Arrow;
    collisonCircle: Circle;
    obsAry: Array<Node> = [];

    obs: Node;
    //查看点击碰撞到的物体

    removeArrow(arrow: Arrow) {
        let index = this.arrows.findIndex(p => p == arrow);
        if (index != -1) {
            this.arrows.splice(index, 1);
        }
        arrow['circle']['arrow'] = null;
        arrow.node.destroy();
    }

    onTouchStart(e: EventTouch) {
        //检测是否碰撞到箭头
        // let collisonArrow: Arrow;
        this.collisonArrow = null;
        this.collisonCircle = null;
        for (const arrow of this.arrows) {
            let vs = Utils.getRotatedVecticesArrow(arrow);
            if (Intersection2D.polygonCircle(vs, e.getUILocation(), 5)) {
                if (this.toggleDelete.isChecked) {
                    this.removeArrow(arrow);
                    return;
                }
                this.collisonArrow = arrow;
                return;
            }
        }

        //查看是否点击到了障碍物
        for (const obs of this.obsAry) {
            let ut = obs.getComponent(UITransform);
            if (ut.getBoundingBoxToWorld().contains(e.getUILocation())) {
                if (this.toggleDelete.isChecked) {
                    obs.destroy();
                    this.obsAry.splice(this.obsAry.indexOf(obs), 1);
                    return;
                } else {
                    this.obs = obs;
                }
            }
        }

        //检测是否碰撞到点
        this.collisonCircle = this.getCollisonCircle(e.getUILocation());
        if (this.collisonCircle) {
            if (this.collisonCircle['arrow']) {
                return;
            }
            if (this.obs) {
                return;
            }
            if (this.itemIndex == 0) {
                //创建箭头-----
                this.collisonArrow = this.createArrow(this.collisonCircle.node.getSiblingIndex(), 0, 0);
                this.collisonCircle['arrow'] = this.collisonArrow;
            } else {
                let fab = this.obsFabs[this.itemIndex - 1];
                this.obs = instantiate(fab);
                this.dir.addChild(this.obs);
                this.obsAry.push(this.obs);
                this.obs.setWorldPosition(this.collisonCircle.node.getWorldPosition());
                this.obs['circle'] = this.collisonCircle;
            }
        }
    }

    getCollisonCircle(pos: Vec2) {
        //检测是否碰撞到点
        for (const circle of this.circles) {
            let p = new math.Vec2(circle.node.getWorldPosition().x, circle.node.getWorldPosition().y)
            if (Intersection2D.circleCircle(pos, 5, p, 30)) {
                return circle;
            }
        }
        return null;
    }

    onTouchMove(e: EventTouch) {
        let d = e.getStartLocation().subtract(e.getLocation()).length();
        if (d < 5) return;
        if (this.collisonArrow) {
            const cols = this.levelData.cols || []; // 每列的点数
            const scale = this.levelData.layoutscale || 1; // 父节点缩放
            //编辑 箭头
            const arrowPos = this.collisonArrow.node.getWorldPosition();

            this.collisonArrow.node.angle = Utils.calculateTouchAngle(e.getUILocation(), arrowPos);

            if (this.toggleWidth.isChecked) {
                //查看当前是否碰撞到了圆点
                let c = this.getCollisonCircle(e.getUILocation());
                let c2 = this.getCollisonCircle(new Vec2(arrowPos.x, arrowPos.y));
                if (c) {
                    //将宽度修改为当前两个点的距离
                    let d = Vec3.distance(c.node.getPosition(), c2.node.getPosition());
                    console.log(d);
                    this.collisonArrow.resetWidth(d * scale);
                }
            }
        }

        if (this.obs) {
            let c = this.getCollisonCircle(e.getUILocation());
            if (c) {
                this.obs.angle = Utils.calculateTouchAngle(c.node.getWorldPosition(), this.obs.getWorldPosition());
            }
        }

        if (this.collisonCircle) {
            //如果是点击到了圆点--则看创建箭头

        }
    }

    onTouchEnd(e: EventTouch) {
        if (this.collisonArrow) {
            //编辑 箭头
            const arrowPos = this.collisonArrow.node.getWorldPosition();

            //查看当前是否碰撞到了圆点
            let c = this.getCollisonCircle(e.getUILocation());
            if (c) {
                this.collisonArrow.node.angle = Utils.calculateTouchAngle(c.node.getWorldPosition(), arrowPos);
            }
        }

        this.collisonArrow = null;
        this.collisonCircle = null;
        this.obs = null;
    }

    onItemClick(e, index: number) {
        for (let i = 0; i < this.itemContent.children.length; i++) {
            let item = this.itemContent.children[i];
            item.getChildByName("light").active = false;
        }

        this.itemIndex = index;

        this.itemContent.children[index].getChildByName("light").active = true;
    }
}


