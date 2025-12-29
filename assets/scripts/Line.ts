import { Vec3 } from "cc";

export class Line2D {
    x1: number = 0; // 起点x坐标
    x2: number = 0; // 终点x坐标
    y1: number = 0; // 起点y坐标
    y2: number = 0; // 终点y坐标
    constructor(s1?: Vec3, s2?: Vec3) {
        this.reset(s1, s2); // 初始化线条
    }

    reset(s1?: Vec3, s2?: Vec3) {
        if (s1) {
            this.x1 = s1.x;
            this.y1 = s1.y;
        }
        if (s2) {
            this.x2 = s2.x;
            this.y2 = s2.y;
        }
    }
}