import { Button, EventTouch, isValid, Node } from "cc";
/**触摸回调类型 */
export type BtnCb = (event?: EventTouch) => void;
/**添加完的默认名字 */
const DEF_CBS_FLAG = '~def_button_listen_added~';

/**按钮 */
export abstract class ButtonUtl  {
   
    //--------------------------------------


    /**
     * 通用点击回调
     * @param node 
     * @param click 
     * @param transition 
     * @param start 
     * @param move 
     * @param end 
     * @param cancel 
     */
    public static addDefaultListener(
        node: Node,
        click?: BtnCb,
        transition = Button.Transition.SCALE,
        start?: BtnCb,
        move?: BtnCb,
        end?: BtnCb,
        cancel = end
    ): void {

        if (!node) {
            return;
        }

        this.removeDefaultListener(node);       //现在先统一这边处理，不然应该是各自自己释放

        this.addListenerByName(node, 'start', start);
        this.addListenerByName(node, 'move', move);
        this.addListenerByName(node, 'end', end);
        this.addListenerByName(node, 'cancel', cancel);
        this.addListenerByName(node, 'click', click);

        if (!node[DEF_CBS_FLAG]) {
            node.on(Node.EventType.TOUCH_START, this.startListen, node);
            node.on(Node.EventType.TOUCH_END, this.endListen, node);
            node.on(Node.EventType.TOUCH_CANCEL, this.cancelListen, node);
            node.on(Node.EventType.TOUCH_MOVE, this.moveListen, node);
            node[DEF_CBS_FLAG] = true;
        }

        //默认缩放
        let isAdd = false;
        let btn = node.getComponent(Button);
        if (!btn) {
            btn = node.addComponent(Button);
            isAdd = true;
        }
        btn.enabled = true;
        if (btn.transition !== transition && isAdd) {
            btn.transition = transition;
            if (transition === Button.Transition.SCALE) {
                btn.zoomScale = 1.12;
            }
        }
    }


    /**
     * 移除默认监听
     * @param node 
     */
    public static removeDefaultListener(node: Node): void {
        if (!node) {
            return;
        }
        const btn = node.getComponent(Button);
        if (btn) {
            btn.enabled = false;
        }

        this.removeListenerByName(node, 'start');
        this.removeListenerByName(node, 'move');
        this.removeListenerByName(node, 'end');
        this.removeListenerByName(node, 'cancel');
        this.removeListenerByName(node, 'click');

        node[DEF_CBS_FLAG] = false;

        node.off(Node.EventType.TOUCH_START, this.startListen, node);
        node.off(Node.EventType.TOUCH_END, this.endListen, node);
        node.off(Node.EventType.TOUCH_CANCEL, this.cancelListen, node);
        node.off(Node.EventType.TOUCH_MOVE, this.moveListen, node);
    }


    /**
     * 按下监听
     * @param e 
     */
    private static startListen(e: EventTouch): void {
        const en = ButtonUtl.getEnterDefName();
        const node: Node = e.currentTarget;
        if (!node) {
            return;
        }

        node[en] = true;
        ButtonUtl.callListener(node, 'start', e);
    }


    /**
     * 移动监听
     * @param e 
     */
    private static moveListen(e: EventTouch): void {
        const en = ButtonUtl.getEnterDefName();
        const node: Node = e.currentTarget;
        if (!node) {
            return;
        }
        if (node[en]) {
            ButtonUtl.callListener(node, 'move', e);
        }
    }

    /**回调对象是否已释放 */
    public static isDestroyed(target: any): boolean {
        if (target) {
            if (target.destroyed || !isValid(target, true)) {
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * 松开监听
     * @param e 
     */
    private static endListen(e: EventTouch): void {
        let isPlayDianji = ButtonUtl.isPlayKeybordMusic(e.target);
        if (isPlayDianji) {
            // AudioMgr.play('equip_dianji', null, false, E_Consts.EFFGROUP);
        }
        const en = ButtonUtl.getEnterDefName();
        const node: Node = e.currentTarget;
        if (!node) {
            return;
        }

        node[en] = false;
        ButtonUtl.callListener(node, 'end', e);

        if (!this.isDestroyed(node)) {
            ButtonUtl.callListener(node, 'click', e);
        }
    }

    /**是否播放按键音 */
    private static isPlayKeybordMusic(node: Node) {
        //主界面底部按钮
        if (node.name.indexOf('guide_battle_') >= 0 && node.parent.name === 'bottom') {
            // AudioMgr.play('UI_page_change', null, false, E_Consts.EFFGROUP);
            return false;
        }
        //主界面侧边按钮
        let mainBtnName = [
            'btn_world_map', 'btn_world_task', 'btn_world_shop',
            'btn_world_mail', 'btn_world_sign', 'btn_world_turntable',
            'btn_world_newRecruits'
        ]
        for (let i = 0; i < mainBtnName.length; ++i) {
            if (node.name == mainBtnName[i]) {
                // AudioMgr.play('main_tap', null, false, E_Consts.EFFGROUP);
                return false;
            }
        }
        return true;
    }


    /**
    * 取消监听
    * @param e 
    */
    private static cancelListen(e: EventTouch): void {
        const en = ButtonUtl.getEnterDefName();
        const node: Node = e.currentTarget;
        if (!node) {
            return;
        }

        node[en] = false;
        ButtonUtl.callListener(node, 'cancel', e);
    }


    /**
     * 获取默认回调名
     * @param name 
     * @returns 
     */
    private static getCallbackListDefName(name: string): string {
        const fn = 'mytx_event_' + name + '_cbs';
        return fn;
    }


    /**
     * 获取默认标记
     * @returns 
     */
    private static getEnterDefName(): string {
        const fn = 'mytx_event_touch_enter';
        return fn;
    }


    /**
     * 添加监听
     * @param node 
     * @param name 
     * @param cb 
     */
    private static addListenerByName(node: Node, name: string, cb: Function): void {
        if (!cb) {
            return;
        }

        const fn = ButtonUtl.getCallbackListDefName(name);
        const list = node[fn];

        if (list) {
            if (list instanceof Array) {
                if (list.indexOf(cb) === -1) {
                    list.push(cb);
                }
            }
            else if (list !== cb) {
                node[fn] = [list, cb];
            }
        }
        else {
            node[fn] = cb;
        }
    }


    /**
     * 移除监听
     * @param node 
     * @param name 
     */
    private static removeListenerByName(node: Node, name: string): void {
        const fn = ButtonUtl.getCallbackListDefName(name);
        const list = node[fn];
        if (list) {
            if (list instanceof Array) {
                list.length = 0;
            }
            else {
                node[fn] = null;
            }
        }
    }


    /**
     * 回调
     * @param node 
     * @param name 
     * @param e 
     */
    public static callListener(node: Node, name: string, e: EventTouch): void {
        const fn = ButtonUtl.getCallbackListDefName(name);
        const list = node[fn];
        if (!list) {
            return;
        }

        if (list instanceof Array) {
            for (let i = 0; i < list.length; i++) {
                list[i](e);
            }
        }
        else {
            list(e);
        }
    }
}