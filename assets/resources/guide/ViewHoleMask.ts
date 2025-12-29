import { _decorator, Color, Component, Material, Vec2, Sprite, Node, Vec4, math } from 'cc';
import { Engine } from '../../scripts/Engine';
const { ccclass, property } = _decorator;

@ccclass('ViewHoleMask')
export class ViewHoleMask extends Component {

    @property(Color)
    maskColor: Color = new Color(255, 255, 255, 255);

    @property(Vec2)
    holePosition: Vec2 = new Vec2(0.5, 0.5); // 镂空区域默认居中

    holeRadius: number = 0; // 镂空半径默认值

    // @property
    // aspectRatio: number = 0.56; // 宽高比校正

    private material: Material | null = null;

    protected start(): void {
        this.holeRadius = Engine.instance.canvasUt.width;
        // this.targetRadius = Engine.instance.canvasUt.width;
    }

    // 动态更新 Shader 的属性
    updateShaderProperties() {
        // if (!this.material) return;
        // 将 maskColor 转为 Vec4 并传递给 Shader
        const colorVec = new Vec4(
            this.maskColor.r / 255,
            this.maskColor.g / 255,
            this.maskColor.b / 255,
            this.maskColor.a / 255
        );

        if (this.material == null) {
            // 获取当前节点的 Sprite 组件的材质
            const sprite = this.node.getComponent(Sprite);
            if (sprite) {
                this.material = sprite.customMaterial;
            }
        }

        this.material.setProperty('maskColor', colorVec);

        const pos = new Vec2(this.holePosition.x / Engine.instance.canvasUt.width, this.holePosition.y / Engine.instance.canvasUt.height);
        // 传递镂空位置（Vec2 类型）
        this.material.setProperty('holePosition', pos);
        // 传递镂空半径
        this.material.setProperty('holeRadius', this.holeRadius / Engine.instance.canvasUt.width);

        this.material.setProperty('screenSize', new Vec2(Engine.instance.canvasUt.width, Engine.instance.canvasUt.height));
        // 传递宽高比
        // this.material.setProperty('aspectRatio', this.aspectRatio);
    }

    // 外部可以通过这些方法动态修改属性
    public setMaskColor(newColor: Color) {
        this.maskColor = newColor;
        this.updateShaderProperties();
    }

    public setHolePosition(newPosition: Vec2) {
        // console.log('------------')
        this.holePosition = newPosition;
        this.updateShaderProperties();
    }

    targetRadius: number = 1;
    public setHoleRadius(newRadius: number) {
        this.targetRadius = newRadius;
        this.updateShaderProperties();
    }

    protected update(dt: number): void {
        this.holeRadius = math.lerp(this.holeRadius, this.targetRadius, dt * 10);
        this.updateShaderProperties();
    }

}
