import { Vec3, UITransform, Node, math, BoxCollider2D, Intersection2D, Vec2 } from 'cc';
import { Arrow } from './Arrow';
import { Line2D } from './Line';
import { Obstacle } from './Obstacle';

export class Utils {

    /**
     * Converts a rotation angle (in degrees) to a direction vector.
     * @param angle The rotation angle in degrees.
     * @returns A Vec2 representing the direction.
     */
    static getDirectionFromRotation(angle: number): Vec3 {
        const radians = angle * (Math.PI / 180);
        return new Vec3(Math.cos(radians), Math.sin(radians));
    }

    /**
     * 计算触摸点相对于节点中心的角度
     * @param touchPos 触摸点的屏幕坐标
     * @param nodePos 节点的世界坐标
     * @returns 角度（单位：度）
     */
    static calculateTouchAngle(touchPos: Vec3 | Vec2, nodePos: Vec3 | Vec2): number {
        return Math.atan2(touchPos.y - nodePos.y, touchPos.x - nodePos.x) * (180 / Math.PI);
    }

    /**
     * 计算触摸点相对于节点中心的角度0-360度
     * @param touchPos 触摸点的屏幕坐标
     * @param nodePos 节点的世界坐标
     * @returns 距离
     */
    static calculateTouchAngle360(touchPos: Vec3 | Vec2, nodePos: Vec3 | Vec2): number {
        let angle = Math.atan2(touchPos.y - nodePos.y, touchPos.x - nodePos.x) * (180 / Math.PI);
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }


    /**
    * Calculates the rotation angle (in degrees) from one position to another.
    * @param targetPosition The target position as a Vec3.
    * @param sourcePosition The source position as a Vec3.
    * @returns The rotation angle in degrees.
    */
    static getRotationFromDirection(targetPosition: Vec3, sourcePosition: Vec3): number {
        const direction = targetPosition.subtract(sourcePosition);
        const angle = Math.atan2(direction.y, direction.x) * (180 / Math.PI);
        return angle;
    }

    static getRotatedVecticesArrow(arrow: Arrow | Obstacle): math.Vec2[] {
        const corners = [];
        corners.push(new math.Vec2(arrow.points[0].worldPosition.x, arrow.points[0].worldPosition.y));
        corners.push(new math.Vec2(arrow.points[1].worldPosition.x, arrow.points[1].worldPosition.y));
        corners.push(new math.Vec2(arrow.points[2].worldPosition.x, arrow.points[2].worldPosition.y));
        corners.push(new math.Vec2(arrow.points[3].worldPosition.x, arrow.points[3].worldPosition.y));
        return corners;
    }



    static isPolygonCollision(vertices1: Vec3[], vertices2: Vec3[]): boolean {
        // 获取所有的分离轴（两个多边形的边法线）
        const axes = Utils.getAxes(vertices1).concat(Utils.getAxes(vertices2));

        for (const axis of axes) {
            const projection1 = Utils.projectVertices(vertices1, axis);
            const projection2 = Utils.projectVertices(vertices2, axis);

            console.log(`Axis: ${axis}, Projection1: ${projection1}, Projection2: ${projection2}`);

            // 如果在某个轴上没有重叠，则没有碰撞
            if (projection1.max < projection2.min || projection2.max < projection1.min) {
                return false;
            }
        }

        return true; // 所有轴上都有重叠，则发生碰撞
    }
    /**
     * 投影顶点到轴上
     * @param vertices 顶点数组
     * @param axis 投影轴
     * @returns 投影的最小值和最大值
     */
    static projectVertices(vertices: Vec3[], axis: Vec3): { min: number; max: number } {
        let min = Infinity;
        let max = -Infinity;

        for (const vertex of vertices) {
            // 投影点到轴上
            const projection = vertex.x * axis.x + vertex.y * axis.y;
            min = Math.min(min, projection);
            max = Math.max(max, projection);
        }

        return { min, max };
    }

    /**
     * 获取多边形的分离轴
     * @param vertices 多边形的顶点数组
     * @returns 分离轴数组
     */
    static getAxes(vertices: Vec3[]): Vec3[] {
        const axes: Vec3[] = [];
        for (let i = 0; i < vertices.length; i++) {
            const p1 = vertices[i];
            const p2 = vertices[(i + 1) % vertices.length]; // 下一个顶点（循环）

            // 计算边向量
            const edge = new Vec3(p2.x - p1.x, p2.y - p1.y, 0);

            // 计算法线（分离轴）
            const normal = new Vec3(-edge.y, edge.x, 0).normalize();
            axes.push(normal);
        }
        return axes;
    }


    static checkEdgeCollision(line1: Line2D, line2: Line2D): boolean {
        const p1 = { x: line1.x1, y: line1.y1 };
        const p2 = { x: line1.x2, y: line1.y2 };
        const q1 = { x: line2.x1, y: line2.y1 };
        const q2 = { x: line2.x2, y: line2.y2 };

        // // 计算向量
        // const r = new Vec3(p2.x - p1.x, p2.y - p1.y, 0);
        // const s = new Vec3(q2.x - q1.x, q2.y - q1.y, 0);
        // const qp = new Vec3(q1.x - p1.x, q1.y - p1.y, 0);

        // // 计算叉积
        // const rxs = r.x * s.y - r.y * s.x;
        // const qpxr = qp.x * r.y - qp.y * r.x;

        // const epsilon = 1e-6; // 容差值

        // // 平行且不重叠
        // if (Math.abs(rxs) < epsilon && Math.abs(qpxr) >= epsilon) {
        //     return false;
        // }

        // // 平行且重叠
        // if (Math.abs(rxs) < epsilon && Math.abs(qpxr) < epsilon) {
        //     // 检查是否在同一直线上
        //     const t0 = ((q1.x - p1.x) * r.x + (q1.y - p1.y) * r.y) / (r.x * r.x + r.y * r.y);
        //     const t1 = ((q2.x - p1.x) * r.x + (q2.y - p1.y) * r.y) / (r.x * r.x + r.y * r.y);
        //     return !(t1 < 0 || t0 > 1); // 检查是否有重叠部分
        // }

        // // 计算参数 t 和 u
        // const t = (qp.x * s.y - qp.y * s.x) / (rxs + epsilon);
        // const u = (qp.x * r.y - qp.y * r.x) / (rxs + epsilon);

        // // 检查是否相交
        // return t >= 0 && t <= 1 && u >= 0 && u <= 1;

        return Intersection2D.lineLine(new Vec2(line1.x1, line1.y1), new Vec2(line1.x2, line1.y2),
            new Vec2(line2.x1, line2.y1), new Vec2(line2.x2, line2.y2));
    }

    /**
     * 检测矩形与点的碰撞
     * @param rectVertices 矩形的四个顶点
     * @param point 
     * @returns 
     */
    static isRectangleCollidingWithPoint(rectVertices: Vec3[], point: Vec3): boolean {
        // 使用点在多边形内的算法
        let inside = false;
        for (let i = 0, j = rectVertices.length - 1; i < rectVertices.length; j = i++) {
            const xi = rectVertices[i].x, yi = rectVertices[i].y;
            const xj = rectVertices[j].x, yj = rectVertices[j].y;

            const intersect = ((yi > point.y) !== (yj > point.y)) &&
                (point.x < (xj - xi) * (point.y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    }

    static lineCircle(lineStart: math.Vec2, lineEnd: math.Vec2, circleCenter: math.Vec2, circleRadius: number): boolean {
        // 计算线段的方向向量
        const lineDir = new math.Vec2(lineEnd.x - lineStart.x, lineEnd.y - lineStart.y);

        // 计算圆心到线段起点的向量
        const startToCircle = new math.Vec2(circleCenter.x - lineStart.x, circleCenter.y - lineStart.y);

        // 计算投影比例 t
        const lineLengthSquared = lineDir.x * lineDir.x + lineDir.y * lineDir.y;
        const t = Math.max(0, Math.min(1, (startToCircle.x * lineDir.x + startToCircle.y * lineDir.y) / lineLengthSquared));

        // 计算投影点的坐标
        const closestPoint = new math.Vec2(
            lineStart.x + t * lineDir.x,
            lineStart.y + t * lineDir.y
        );

        // 计算投影点到圆心的距离
        const distanceSquared = (closestPoint.x - circleCenter.x) * (closestPoint.x - circleCenter.x) +
            (closestPoint.y - circleCenter.y) * (closestPoint.y - circleCenter.y);

        // 如果距离小于等于圆的半径，则发生碰撞
        return distanceSquared <= circleRadius * circleRadius;
    }

    /**
     * 写一个从当前数字移动到目标数字的函数，使用线性插值
     * * @param start 起始数字
     * * @param end 目标数字
     * * @param t 插值因子（0 到 1 之间的值）
     */
    static lerp(start: number, end: number, t: number): number {
        return start + (end - start) * t;
    }

    //写一个从当前数字移动到目标数字的函数，固定步长
    static moveToTarget(start: number, end: number, step: number): number {
        if (Math.abs(end - start) <= step) {
            return end; // 如果距离小于等于步长，直接返回目标值
        } else {
            return start + Math.sign(end - start) * step; // 根据方向移动
        }
    }

    //写一个检测当前数组是否在from到to之间的函数
    /**
 * 判断一个角度是否在两个角度范围内（顺时针方向）
 * @param angle 要判断的角度
 * @param start 起始角度
 * @param end 结束角度
 * @returns 是否在范围内
 */
    static isAngleInRange(angle: number, start: number, end: number): boolean {
        // 将角度标准化到 0 ~ 360 范围
        angle = (angle + 360) % 360;
        start = (start + 360) % 360;
        end = (end + 360) % 360;

        // 如果起点小于终点，直接判断
        if (start <= end) {
            return angle >= start && angle <= end;
        }

        // 如果起点大于终点，表示跨越了 0 度
        return angle >= start || angle <= end;
    }

    /**
     * 标准化角度到 0 ~ 360 范围
     */
    static normalizeAngle360(angle: number): number {
        return (angle % 360 + 360) % 360;
    }

    /**
     * 标准化角度到 -180 ~ 180 范围
     */
    static normalizeAngle180(angle: number): number {
        angle = (angle % 360 + 360) % 360;
        if (angle > 180) angle -= 360;
        return angle;
    }

    /**
     * 计算两个角度之间的最小差值（-180 ~ 180）
     */
    static calculateAngleDifference(angle1: number, angle2: number): number {
        const diff = Utils.normalizeAngle180(angle2 - angle1);
        return diff;
    }


}