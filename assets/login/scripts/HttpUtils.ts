
/**
 * HTTP  网络工具 基于Creator
 */

import { loader, sys } from "cc";

// import PromptFly from "./com/PromptFly";
// import ModelPlayer from "./model/ModelPlayer";

// import PromptFly from "../com/PromptFly";
// import ModelPlayer from "../model/ModelPlayer";

export default class HttpUtils {
    static gameUrl = "https://box.tv888.cn/";
    static token = ''
    static FuncArr = [];  //回调函数数组
    /**
     * get 请求
     * @param {*} url 
     * @param {*} callback 
     */
    static httpGets(urlData, data?, callback?) {
        let urlStr = this.gameUrl + urlData.url;
        let dataStr = '';
        if (data) {
            Object.keys(data).forEach(key => {
                dataStr += key + '=' + encodeURIComponent(data[key]) + '&';
            })
            if (dataStr !== '') {
                dataStr = dataStr.substring(0, dataStr.lastIndexOf('&'));
                urlStr = urlStr + '?' + dataStr;
            }
        }

        let xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4 && (xhr.status >= 200 && xhr.status < 300)) {
                var respone = JSON.parse(xhr.responseText);
                // cc.log(respone)
                if (respone) {
                    // if (respone.code == 0) {
                    //     // PromptFly.Show(respone.msg);
                    // } else if (respone.code == 1) {
                    callback && callback(respone);
                    // }
                }
            } else {
                // callback && callback(-1);
            }
        };

        xhr.open("GET", urlStr, true);
        // if (cc.sys.isNative) {
        //     xhr.setRequestHeader("Accept-Encoding", "gzip,deflate");
        // }

        if (urlData.isToken) {
            xhr.setRequestHeader("ba-user-token", this.token);
        }

        xhr.timeout = 5000;// 5 seconds for timeout

        xhr.send();
    }



    /**
     * post 请求
     * @param {*} url 
     * @param {*} params 
     * @param {*} callback 
     */
    static httpPost(urlData, params, callback) {

        var xhr = loader.getXMLHttpRequest();
        xhr.onreadystatechange = function () {
            // cc.log('xhr.readyState='+xhr.readyState+'  xhr.status='+xhr.status);
            if (xhr.readyState === 4 && (xhr.status >= 200 && xhr.status < 300)) {
                console.log("数据接口返回", xhr.responseText)
                var respone = JSON.parse(xhr.responseText);
                if (respone) {
                    // if (respone.code == 0) {
                    //     // PromptFly.Show(respone.msg);
                    // } else if (respone.code == 1 || respone.code == 401) {
                    callback && callback(respone);
                    // }
                }
            } else {
                // callback && callback(-1);
            }
        };

        let urlStr = this.gameUrl + urlData.url;
        xhr.open("POST", urlStr, true);
        // if (cc.sys.isNative) {
        //     xhr.setRequestHeader("Accept-Encoding", "gzip,deflate");
        // }

        xhr.timeout = 5000;// 5 seconds for timeout

        xhr.setRequestHeader("Content-type", "application/json");
        if (urlData.isToken) {
            xhr.setRequestHeader("ba-user-token", this.token);
        }
        xhr.send(JSON.stringify(params));
    }


    /**
     * 下载
     * @param {*} url 
     * @param {*} path 
     * @param {*} params 
     * @param {*} callback 
     */
    static downLoad(url, path, params, callback) {
        var xhr = loader.getXMLHttpRequest();
        xhr.timeout = 5000;
        var requestURL = url + path;
        if (params) {
            requestURL = requestURL + "?" + params;
        }

        xhr.responseType = "arraybuffer";
        xhr.open("GET", requestURL, true);
        if (sys.isNative) {
            xhr.setRequestHeader("Accept", "text/html");
            xhr.setRequestHeader("Accept-Charset", "utf-8");
            xhr.setRequestHeader("Accept-Encoding", "gzip,deflate");
        }

        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4 && (xhr.status >= 200 && xhr.status < 300)) {
                var buffer = xhr.response;
                var dataview = new DataView(buffer);
                var ints = new Uint8Array(buffer.byteLength);
                for (var i = 0; i < ints.length; i++) {
                    ints[i] = dataview.getUint8(i);
                }
                callback(null, ints);
            }
            else {
                callback(xhr.readyState + ":" + xhr.status, null);
            }
        };
        xhr.send();
        return xhr;
    }

    static setFuncArr(key, target, func) {
        let data = { key: key, target: target, func: func };
        this.FuncArr.push(data);
    }
    static removeFunc(key, target, func) {
        for (let i = this.FuncArr.length - 1; i >= 0; i--) {
            let data = this.FuncArr[i];
            if (data && data.key == key && data.target == target && data.func == func) {
                this.FuncArr.splice(i, 1);
            }
        }
    }
}
