import * as cc from 'cc';
import { EDITOR } from 'cc/env';
const { ccclass, property } = cc._decorator;

/*
 creator3.6使用2.4微信开放数据域
将2.4的postMessage方法全部移植到3.6，包括开放数据域窗口大小计算方法修改
*/
@ccclass('SubContextViewEx')
export class SubContextViewEx extends cc.SubContextView {


    _fps: number = 60;

    _updateInterval: number = 0;
    get fps() {
        return this._fps;
    }
    set fps(value) {
        if (this._fps === value) {
            return;
        }
        this._fps = value;
        this._updateInterval = 1000 / value;
        this._updateSubContextFrameRate()
    }


    _designResolutionSize :cc.Size = new cc.Size(600,600);
    // 不显示设计尺寸，2.4版本不使用该字段，使用节点尺寸
    get designResolutionSize() {
        return this._designResolutionSize;
    }
    @property({ override: true, visible: false })
    set designResolutionSize(value) {
        if (!EDITOR || value.equals(this._designResolutionSize)) {
            return;
        }
        this._designResolutionSize.set(value);
    }

    private _firstlyEnabled = true

    private _openDataContext: any;

    onEnable() {
        super.onEnable()
        if (this._firstlyEnabled && this._openDataContext) {
            this._openDataContext.postMessage({
                type: 'engine',
                fromEngine: true,
                event: 'boot',
            });
            this._firstlyEnabled = false;
        }
        else {
            this._runSubContextMainLoop();
        }
        this._updateSubContextFrameRate()
    }

    onDisable() {
        super.onDisable()
        this._stopSubContextMainLoop();
    }

    _initSharedCanvas() {
        if (this._openDataContext) {
            const sharedCanvas = this._openDataContext.canvas;
            let size = this.node.getComponent(cc.UITransform).contentSize
            sharedCanvas.width = size.width;
            sharedCanvas.height = size.height;
        }
    }

    update(dt) {
        if (EDITOR) return;

        let calledUpdateMannually = (dt === undefined);
        if (calledUpdateMannually) {
            this._openDataContext && this._openDataContext.postMessage({
                type: 'engine',
                fromEngine: true,
                event: 'step',
            });
            let a = this as any;
            a._updateSubContextTexture();
            return;
        }
        super.update(dt)
    }


    _runSubContextMainLoop() {
        if (this._openDataContext) {
            this._openDataContext.postMessage({
                type: 'engine',
                fromEngine: true,
                event: 'mainLoop',
                value: true,
            });
        }
    }

    _stopSubContextMainLoop() {
        if (this._openDataContext) {
            this._openDataContext.postMessage({
                type: 'engine',
                fromEngine: true,
                event: 'mainLoop',
                value: false,
            });
        }
    }

    _updateSubContextFrameRate() {
        if (this._openDataContext) {
            this._openDataContext.postMessage({
                type: 'engine',
                fromEngine: true,
                event: 'frameRate',
                value: this.fps,
            });
        }
    }

    _updateSubContextView() {

        if(EDITOR)return;

        if (!this._openDataContext) {
            return;
        }

        if (this._openDataContext) {
            //全屏适配
            let rect = cc.view.getViewportRect()
            this._openDataContext.postMessage({
                type: 'engine',
                fromEngine: true,
                event: 'viewport',
                x: rect.x,
                y: rect.y,
                width: rect.width,
                height: rect.height
            });
        }
    }

    

}