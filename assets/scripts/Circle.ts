import { _decorator, Color, Component, Label, Node, Sprite } from 'cc';
const { ccclass, property } = _decorator;

@ccclass('Circle')
export class Circle extends Component {
    @property(Sprite)
    circleSprite: Sprite = null; // 圆圈精灵

    @property(Label)
    label: Label = null; // 标签组件

    startColor: Color;

    index: number = 0; // 圆圈的索引
    init(index: number) {
        this.label.string = index.toString(); // 显示坐标
        this.startColor = this.circleSprite.color.clone();
        this.index = index; // 设置索引
    }

    highlightCircle() {
        // this.circleSprite.color = new Color(255, 0, 0); // 设置为红色高亮
        this.node.children[1].active = true;
    }

    removeHighlight() {
        // this.circleSprite.color = this.startColor.clone(); // 恢复为白色
        this.node.children[1].active = false;
        this.removeTouch();
    }

    clear() {
        this.node.destroy();
    }

    onTouchStart() {
        if (this.clickFunc) {
            this.clickFunc(this); // 调用点击函数并传递索引

            this.removeTouch();
        }
    }

    clickFunc: Function;
    openTouch(func: Function) {
        if (this.clickFunc) {
            return;
        }
        // console.log("开启触摸事件", this.index);
        this.node.on(Node.EventType.TOUCH_START, this.onTouchStart, this); // 添加触摸事件监听器
        this.clickFunc = func;
    }

    removeTouch() {
        if (this.clickFunc) {
            this.clickFunc = null;
            this.node.off(Node.EventType.TOUCH_START, this.onTouchStart, this); // 添加触摸事件监听器
            // console.log("移除触摸事件", this.index);
        }
    }
}


