/**
 * @file SqlUtil.ts
 * @author dream
 * @description 本地化存储方案
 *
 */
import { sys } from "cc";

export module SqlUtil {
    let _key: string | null = null;
    let _iv: string | null = null;

    /**
     * 存储
     * @param key 存储key
     * @param value 存储值
     * @returns 
     */
    export function set(key: string, value: any) {
        if (null == key) {
            console.error("存储的key不能为空");
            return;
        }
        if (null == value) {
            console.warn("存储的值为空，则直接移除该存储");
            remove(key);
            return;
        }
        if (typeof value === 'function') {
            console.error("储存的值不能为方法");
            return;
        }

        if (value instanceof Map) {
            let obj = Object.create(null);
            for (let [k, v] of value) {
                obj[k] = v;
            }
            value = obj;
        }

        if (typeof value === 'object') {
            try {
                value = JSON.stringify(value);
            }
            catch (e) {
                console.error(`解析失败，str=${value}`);
                return;
            }
        }
        else if (typeof value === 'number') {
            value = value + "";
        }
       
        sys.localStorage.setItem(key, value);
    }

    /**
     * 获取
     * @param key 获取的key
     * @param defaultValue 获取的默认值
     * @returns 
     */
    export function get(key: string, defaultValue?: any) {
        if (null == key) {
            console.error("存储的key不能为空");
            return;
        }
       
        let str: string | null = sys.localStorage.getItem(key);
        
        if (null == defaultValue || str == "") {
            return defaultValue;
        }
        // if (null == defaultValue || typeof defaultValue === 'string') {
        //     return str;
        // }
        if (null === str) {
            return defaultValue;
        }
        if (typeof defaultValue === 'number') {
            return Number(str) || 0;
        }
        if (typeof defaultValue === 'boolean') {
            return "true" == str;
        }
        if (str == "") {
            return defaultValue;
        }

        if (defaultValue instanceof Map) {
            let obj = JSON.parse(str);
            let map = new Map();
            for (let k of Object.keys(obj)) {
                map.set(k, obj[k]);
            }
            return map;
        }


        if (typeof defaultValue === 'object') {
            try {
                return JSON.parse(str);
            }
            catch (e) {
                console.error("解析数据失败,str=" + str);
                return defaultValue;
            }
        }
        return str;

    }

    /**
     * 移除某个值
     * @param key 需要移除的key 
     * @returns 
     */
    export function remove(key: string) {
        if (null == key) {
            console.error("存储的key不能为空");
            return;
        }
        
        sys.localStorage.removeItem(key);
    }

    /**
     * 清空整个本地存储
     */
    export function clear() {
        sys.localStorage.clear();
    }

}