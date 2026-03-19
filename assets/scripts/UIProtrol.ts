import { _decorator, Component, Label, Node, RichText } from 'cc';
const { ccclass, property } = _decorator;

@ccclass('UIProtrol')
export class UIProtrol extends Component {
    @property(Label)
    titleLabel: Label;

    @property(RichText)
    richText: RichText;

    start() {

    }

    update(deltaTime: number) {

    }

    init(title, content) {
        this.node.active = true;
        this.titleLabel.string = title;
        this.richText.string = content;
    }

    onClose() {
        this.node.active = false;
    }
}


