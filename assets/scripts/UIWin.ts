import { _decorator, Color, Component, Label, Node, randomRange, randomRangeInt, tween, UITransform, Vec3 } from 'cc';
import { Engine } from './Engine';
const { ccclass, property } = _decorator;

@ccclass('UIWin')
export class UIWin extends Component {

    protected start(): void {
        // Engine.instance.jsb.showRankView('level');
        // Engine.instance.jsb.Jsb.showInstertView('');

        this.show();
    }

    show() {
        this.node.active = true;

        //做一个弹幕系统  胜利的时候显示
        let ut = this.node.getComponent(UITransform);

        let tips = [
            '轻松通关啦哈',
            '真是太棒了',
            '游戏被我拿捏',
            '游戏被我玩坏了',
            '我真是个天才',
            '通关像闹着玩',
            '轻松搞定此关',
            '通关超爽耶！',
            '终于通关啦哇',
            '通关兴奋到爆',
            '通关开心飞起',
            '耶！成功通关',
            '坚持终能通关',
            '通关学会坚持',
            '挑战终获胜利',
            '通关见证成长',
            '努力换来通关',
            '通关真是太棒',
            '通关真是太牛',
        ];

        for (let i = 1; i <= 10; i++) {
            let labelNode = this.node.getChildByName('tip' + i);

            labelNode.setWorldPosition(new Vec3(ut.width * randomRange(1.2, 3), ut.height * randomRange(0.7, 0.9), 0));

            let label = labelNode.getComponent(Label);
            let index = randomRangeInt(0, tips.length);
            label.string = tips[index];
            tips.splice(index, 1);
        }

        Engine.instance.jsb.Jsb.showInstertView('');
    }

    protected update(dt: number): void {
        for (let i = 1; i <= 10; i++) {
            let labelNode = this.node.getChildByName('tip' + i);
            let pos = labelNode.getPosition();
            labelNode.setPosition(pos.x -= dt * 200, pos.y, pos.z);
        }
    }

    hide() {
        // this.node.active = false;
        this.node.destroy();
    }

    onNextClick() {
        this.hide();
        Engine.instance.nextLevel()
    }



}


