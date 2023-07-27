# wows-real
账号战绩结算推送/对局信息推送

http/ws 端口配置和是否启用在config文件夹下面的config.json

#### 支持使用http/websocket获取数据

## http

基础响应体

code 目前只有200,404,500

200 正常响应

404 没有数据时响应,比如轮询获取个人战绩时没有数据变动则是404

500 参数错误或服务器内部错误,请查看日志

```json
{
	"code": 200,
	"path": "user_real_info",
	"time": 1690469936727,
	"data": ""
}
```

### 上报需要监控的账号ID信息


POST请求 地址=`/user_accountId?botId=2622749113`

参数:`botId` 该参数上报时请使用一个唯一id值可以是QQ号码也可以是其他的

body

server的参数值必须全大写,支持的服务器 ASIA,CN,EU,RU,NA

```json
[
        {"server":"ASIA","accountId":2022515210},
        {"server":"ASIA","accountId":2025801200}
]
```

### 轮询获取账号监控数据

GET 请求 地址=`/user_real_info?botId=2622749113`

参数:`botId` 该参数上报时请使用一个唯一id值可以是QQ号码也可以是其他的

没有数据响应:
```json
{
	"code": 404,
	"path": "user_real_info",
	"time": 1690469936727,
	"data": ""
}
```

有数据时响应:
报文里面的battlesInfo注释参考`https://v3-api.wows.shinoaki.com/v3/swagger-ui/index.html` Schemas 里面的BattleInfoData
```json
{
    "code": 200,
    "path": "user_real_info",
    "time": 1690363448184,
    "data": {
        "userInfo": {
            "server": "cn",
            "serverCn": "国服",
            "clanInfo": {
                "clanId": 7000012708,
                "tag": "AOTD",
                "name": "Mourner",
                "description": "",
                "color": "#b3b3b3",
                "activeLevel": -1
            },
            "accountId": 7049270062,
            "userName": "欧阳景明",
            "accountCreateTime": 1595248572
        },
        "shipInfo": {
            "shipId": 4179506480,
            "nameCn": "哈兰",
            "nameEnglish": "Halland",
            "nameNumbers": "Halland",
            "level": 10,
            "shipType": "Destroyer",
            "country": "Europe",
            "imgSmall": "https://glossary-wows-global.gcdn.co/icons//vehicle/small/PWSD110_f62fe24ced8906191ddfdff91730e3a9ac2daf46b63205d3738bcee9a1e0096c.png",
            "imgLarge": "https://glossary-wows-global.gcdn.co/icons//vehicle/large/PWSD110_292ebab2e5a4e947697be86077329f45f66ee12952ccf05b6efb643c9e6d85d4.png",
            "imgMedium": "https://glossary-wows-global.gcdn.co/icons//vehicle/medium/PWSD110_fb063a0b67bd1b296291c4a2d40311e65b93a4d69eeb62ad087668e32855d143.png",
            "shipIndex": "PWSD110",
            "groupType": "upgradeable"
        },
        "battlesType": "PVP_SOLO",
        "prInfo": {
            "code": 5,
            "value": 1689,
            "nextValue": 61,
            "name": "很好",
            "englishName": "Very Good",
            "color": "#318000"
        },
        "battleInfo":"数据太多就不显示了"
    }
}
```

## websocket协议

地址`/yuyuko`

### 上报监控账号

报文:
```json
{
    "code":200,
    "path":"user_accountId",
    "time":12312312,
    "data":[
        {"server":"ASIA","accountId":2022515210}
    ]
}
```

### 接收个人战绩数据变动推送

报文
```json
{
    "code":200,
    "path":"user_real_info",
    "time":12312312,
    "data":"内容和http接口给的数据一致"
}
```
