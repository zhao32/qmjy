const style = require('./render/style')
// const template = require('./render/template')
const Layout = require('./engine').default;

let __env = GameGlobal.wx || GameGlobal.tt || GameGlobal.swan;
let sharedCanvas = __env.getSharedCanvas();
let sharedContext = sharedCanvas.getContext('2d');
function draw(template) {
    Layout.clear();
    Layout.init(template, style);
    Layout.layout(sharedContext);
}

function updateViewPort(data) {
    Layout.updateViewPort({
        x: data.x,
        y: data.y,
        width: data.width,
        height: data.height,
    });
}

__env.onMessage(data => {
    if (data.type === 'engine' && data.event === 'viewport') {
        updateViewPort(data);
    }

    switch (data.event) {
        case 'level':
            showRankList('level');
            break;
    }
});

function layoutOf(it) {
    var out = '<view class="container" id="main"><view class="rankList"> <scrollview class="list"> '; var arr1 = it.data; if (arr1) { var item, index = -1, l1 = arr1.length - 1; while (index < l1) { item = arr1[index += 1]; out += ' '; if (index % 2 === 1) { out += ' <view class="listItem listItemOld"> '; } out += ' '; if (index % 2 === 0) { out += ' <view class="listItem"> '; } out += ' <view id="listItemUserData"> <text class="listItemNum" value="' + (index + 1) + '"></text> <image class="listHeadImg" src="' + (item.avatarUrl) + '"></image> <text class="listItemName" value="' + (item.nickname) + '"></text> </view> <text class="listItemScore" value="第' + (item.level) + '关"></text> </view> '; } } out += ' </scrollview> <text class="listTips" value="仅展示前 30 位好友排名"></text> </view></view>'; return out;
}

function showRankList(key) {
    console.log('rank key:', key);
    __env.getFriendCloudStorage({
        keyList: [key],
        success: res => {
            if (!res.data) {
                console.log('rank data is empty!');
                return;
            }

            const friendsData = { data: [] };// data: [{level,avatarUrl,nickname}, ...]
            let n = res.data.length;
            if (n >= 30) {
                n = 30;
            }
            for (let i = 0; i < res.data.length; i++) {
                const level = res.data[i].KVDataList.length > 0 ? res.data[i].KVDataList[0].value : '1';
                const item = {};
                item.level = `${level}`;
                item.avatarUrl = res.data[i].avatarUrl; //'openDataContext/avatar.png'
                item.nickname = res.data[i].nickname;
                if (item.nickname.length > 4) {
                    item.nickname = item.nickname.substring(0, 4) + '...';
                }
                friendsData.data.push(item);
            }
            friendsData.data.sort((a, b) => b.level - a.level);
            draw(layoutOf(friendsData));
            console.log('show rank success');
        },

        fail: err => {
            console.log('rank list show err:', err);
        }
    });
}

