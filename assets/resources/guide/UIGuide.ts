import { Color, instantiate, Label, Node, Prefab, resources, Size, Tween, tween, UITransform, Vec2, Vec3 } from "cc";
import { ViewHoleMask } from "./ViewHoleMask";
import { Engine } from "../../scripts/Engine";


export class GuideStep {
    path: string;

    /**
     * 提示文本
     */
    text: string;

    /**
     * 回调
     */
    func?: Function;

    initFunc?: Function;

    touchFunc?: Function;
    moveFunc?: Function;
    endFunc?: Function;

    radius?: number;
    pos?: Vec3;
    root?: Node;

}

/**
 * 新手引导镂空组件
 */
export class UIGuide {
    /**
     * 引导步骤
     */
    guidsSteps: Array<GuideStep> = [];
    /**
     * 当前步骤索引
     */
    stepIndex: number = 0;
    /**
     * 当前查找到的节点
     */
    targetNode: Node;
    /**
     * 当前时间索引
     */
    // timeIndex: number = -1;

    clickBtn: Node;

    mask: ViewHoleMask;

    label: Label;

    node: Node;
    static guide: UIGuide;

    public static create(step: Array<GuideStep>): UIGuide {
        if (this.guide == null) {
            let guide = new UIGuide();
            guide.init(step);
            this.guide = guide;
        }
        return this.guide;
    }

    radius: number;
    pos: Vec2;

    init(step: Array<GuideStep>) {
        this.guidsSteps = step;
        // this.loadUIPrefab('UIGuide', BundleConfig.Com);
        resources.load('guide/UIGuide', Prefab, (err, fab: Prefab) => {
            let node = instantiate(fab);
            Engine.instance.canvas.node.addChild(node);
            this.initPrefabUI(node);
        })
    }

    protected initPrefabUI(node: Node): void {
        this.node = node;
        //获取mask 
        const mask = node.getChildByName('mask').getComponent(ViewHoleMask);
        //设置mask的颜色    
        this.mask = mask;
        this.mask.getComponent(UITransform).setContentSize(Engine.instance.canvasUt.width, Engine.instance.canvasUt.height);
        this.clickBtn = node.getChildByName('clickBtn');
        this.label = node.getChildByName('tipLabel').getComponent(Label);

        this.startGuideStep();
    }

    reload(step: Array<GuideStep>, radius: number, pos: Vec2, color: Color = Color.WHITE) {
        this.guidsSteps = step;
        this.stepIndex = 0;
    }

    step: GuideStep;
    startGuideStep() {
        if (this.stepIndex >= this.guidsSteps.length) {
            this.close();
        } else {
            let step: GuideStep = this.guidsSteps[this.stepIndex];
            // console.log(step.root);
            let pos = step.pos;
            let target: Node;
            let targetUt: UITransform;
            if (pos == null) {
                target = step.root.getChildByPath(step.path);
                pos = target.getWorldPosition();
                targetUt = target.getComponent(UITransform);
            }
            // let target = step.root.getChildByName(step.path);// (step.root instanceof PrefabUIWnd ? step.root.getNode() : step.root, step.path);
            // if (target) {
            //     this.targetNode = target;

            // let uitranf = target.getComponent(UITransform);
            let vec2 = new Vec2();
            vec2.set(pos.x, pos.y);

            this.mask.setHolePosition(vec2);
            if (step.radius) {
                this.mask.setHoleRadius(step.radius);
                this.clickBtn.getComponent(UITransform).setContentSize(new Size(step.radius * 2, step.radius * 2));
            } else {
                if (targetUt) {
                    this.mask.setHoleRadius(targetUt.width / 2);
                    this.clickBtn.getComponent(UITransform).setContentSize(new Size(targetUt.contentSize.width, targetUt.contentSize.width));
                }
            }
            this.clickBtn.active = true;
            this.clickBtn.setWorldPosition(pos);
            this.clickBtn.off(Node.EventType.TOUCH_START, this.onTouchStart, this);
            this.clickBtn.off(Node.EventType.TOUCH_MOVE, this.onTouchMove, this);
            this.clickBtn.off(Node.EventType.TOUCH_END, this.onTouchEnd, this);
            if (step.touchFunc) {
                this.step = step;


                this.clickBtn.on(Node.EventType.TOUCH_START, this.onTouchStart, this);
                this.clickBtn.on(Node.EventType.TOUCH_MOVE, this.onTouchMove, this);
                this.clickBtn.on(Node.EventType.TOUCH_END, this.onTouchEnd, this);

            } else {
                this.clickBtn.once(Node.EventType.TOUCH_END, () => {
                    this.onClick();
                }, this)
            }
            if (step.initFunc) {
                step.initFunc(this.node);
            }
            // }
        }
    }

    onTouchStart(e) {
        this.step.touchFunc(e, this.node, this);
    }
    onTouchMove(e) {
        this.step.moveFunc(e, this.node, this);
    }
    onTouchEnd(e) {
        this.step.endFunc(e, this.node, this);
    }


    onClick() {
        this.mask.setHolePosition(Vec2.ZERO);
        this.mask.setHoleRadius(0);
        this.clickBtn.active = false;

        let step = this.guidsSteps[this.stepIndex];
        this.stepIndex++;
        //先执行步骤回调 
        if (step.func) {
            step.func();
        }

        // //处理点击事件，例如打开设置界面等
        // if (this.targetNode) {
        //     if (!ApiUtils.isDestroyed(this.targetNode)) {
        //         ButtonUtl.callListener(this.targetNode, 'click', null);
        //     }
        // }
        this.startGuideStep();
    }

    close() {
        console.log("close guide step");
        Tween.stopAllByTarget(this.node.getChildByName("arrowPrefab"));

        this.node.destroy();
        UIGuide.guide = null;
    }
}