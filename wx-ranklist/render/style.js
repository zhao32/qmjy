const transparentBg = '#0000FF00';

module.exports = {
    container: {
        width: '100%',
        height: '100%',
        borderRadius: 12,
    },

    header: {
        width: '100%',
        height: '10%',
        flexDirection: 'column',
        alignItems: 'center',
        backgroundColor: '#fff',
        borderBottomWidth: 0.5,
        borderColor: 'rgba(0, 0, 0, 0.3)',
    },

    title: {
        width: '100%',
        height: '100%',
        fontSize: 35,
        lineHeight: 80,
        textAlign: 'center',
        fontWeight: 'bold',
        borderBottomWidth: 6,
        borderColor: '#000',
    },

    rankList: {
        width: '100%',
        height: '90%',
        backgroundColor: transparentBg,
    },

    list: {
        width: '100%',
        height: '88%',
        backgroundColor: transparentBg,
        marginTop: 30,
    },

    listTips: {
        width: '100%',
        height: '12%',
        lineHeight: 90,
        textAlign: 'center',
        fontSize: 25,
        color: 'rgba(0,0,0,0.5)',
        backgroundColor: transparentBg,
        borderRadius: 10,
        borderWidth: 1,
        borderColor: 'rgba(0, 0, 0, 1)',
    },

    listItem: {
        backgroundColor: transparentBg,
        width: '100%',
        height: '20%',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingLeft: 20,
        paddingRight: 20,
        marginBottom: 10,
    },

    listItemOld: {
        backgroundColor: transparentBg,
    },

    listItemUserData: {
        flex: 1,
        height: '100%',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'flex-start',
        minWidth: 0, // 允许子项收缩
    },

    listItemScore: {
        width: 120,
        height: 100,
        fontSize: 33,
        fontWeight: 'bold',
        lineHeight: 100,
        textAlign: 'right',
        color: '#000',
        marginLeft: 10, // 增加与名字的间距
    },

    listItemNum: {
        width: 60,
        height: 80,
        fontSize: 30,
        fontWeight: 'bold',
        color: '#452E27',
        lineHeight: 80,
        textAlign: 'center',
        marginRight: 10,
    },

    listHeadImg: {
        borderRadius: 8,
        width: 70,
        height: 70,
        marginRight: 16,
        backgroundColor: '#eee',
    },

    listItemName: {
        maxWidth: 160,
        height: 100,
        fontSize: 30,
        lineHeight: 100,
        marginLeft: 0,
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
    },
};