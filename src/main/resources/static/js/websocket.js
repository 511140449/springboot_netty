// 监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
/*window.onbeforeunload = function() {
    if( wsObj.ws == null ) return;
    wsObj.ws.close();
}*/

/*websocket对象*/
var wsObj = {
    wsUrl:'ws://127.0.0.1:1111/ws',
    lockReconnect : false //避免ws重复连接
    ,ws : null // 判断当前浏览器是否支持WebSocket
    ,number : 0
    ,open: () => {
        var ws = wsObj.ws;
        function sendNumber() {
            if (ws.readyState === ws.OPEN) {
                wsObj.number++;
                ws.send(wsObj.number.toString());
                console.log("发送："+wsObj.number.toString())
                setTimeout(sendNumber, 1000);
            }
        }
        sendNumber();
    }
    ,msg: (data) => {
        console.log(data, "msg")
    }

    //示例**************************************************
    ,initWebsocket(){
        console.log("websockt开始初始化")
        /*
        * 初始化 websock
        * 连接 服务器地址
        * 并绑定 websock 四个事件方法
        */
        this.ws = new WebSocket(this.wsUrl);

        // 接收服务器返回的数据
        this.ws.onmessage = this.websocketonmessage();
        // 连接建立时触发
        this.ws.onopen = this.websocketonopen();
        // 连接中发生异常
        this.ws.onerror = this.websocketonerror();
        // 连接关闭时触发
        this.ws.onclose = this.websocketclose();
    }
    ,websocketsend()
    {
        /*
        * websocket 数据发送 通过 websock.send() 方法向服务器发送数据
        * 注：这里随便发哈，主要作用就是通过这个动作，让客户端与服务端建立联系
        */
        let actions = {
            "test": "12345"
        };
        this.ws.send(JSON.stringify(actions));
        console.log("发送")
    }
    ,websocketonmessage(e)
    {
        /*
        * websocket 数据接收 执行的方法
        * 注：服务器通过 websocke 向客户端发送数据时，这里的方法就会自动触发啦
        */
        const redata = JSON.parse(e.data);
        switch (redata.messageType) {
            case 0: // 售后 提示
                // ... 执行
                break;
            case 1: // 库存 提示
                // ... 执行
                break;
            case 2: // 下架 提示
                // ... 执行
                break;
        }
    }
    ,websocketonopen()
    {
        /*
        * websocket 初始化后 执行的方法
        * 调用 发送数据方法
        */
        this.wsetsend();
    }
    ,websocketonerror()
    {
        /*
        * websocket 连接建立失败 执行的方法
        * 注：我这里加了个判断，如果联系建立失败就在连接几次
        */
        if(this.cishu < 5){
            this.cishu = this.cishu + 1;
            this.initWebsocket();
        }
    }
    ,websocketclose(e)
    {
        /*
        * websocket 连接关闭 执行的方法
        */
        console.log('断开连接', e);
    }


    /*实现 **************************************************/

    ,socketStart() {
        this.createWebSocket(); //连接ws
    }
    ,reconnect() {
        console.log("重连："+ new Date().toLocaleString())
        if (this.lockReconnect) return;
        this.lockReconnect = true;
        setTimeout(function() { //没连接上会一直重连，设置延迟避免请求过多
            wsObj.createWebSocket();
            wsObj.lockReconnect = false;
        }, 2000);
    }

    ,createWebSocket() {
        try {
            if ('WebSocket' in window) {
                this.ws = new WebSocket(this.wsUrl, "WebSocket");
            } else if ('MozWebSocket' in window) {
                this.ws = new MozWebSocket(this.wsUrl, 'WebSocket');
            } else {
                alert("您的浏览器不支持websocket")
            }
            this.initEventHandle();
        } catch (e) {
            this.reconnect();
            console.log(e);
        }
    }

    ,initEventHandle() {
        var _this = this;
        this.ws.onclose = function() {
            console.log("llws连接关闭! " + new Date().toLocaleString());
            wsObj.reconnect();
        };
        this.ws.onerror = function(event) {
            console.log(event)
            console.log("llws连接错误! ");
            wsObj.reconnect();
        };
        this.ws.onopen = function() {
            console.log("llws连接成功! " + new Date().toLocaleString());
            wsObj.open();
            heartCheck.reset().start(); //心跳检测重置
        };
        this.ws.onmessage = function(event) { //如果获取到消息，心跳检测重置
            heartCheck.reset().start(); //拿到任何消息都说明当前连接是正常的
            wsObj.msg(event.data,wsObj.ws)
        };
    }

    ,websocketSend()
    {
        /*
        * websocket 数据发送 通过 websock.send() 方法向服务器发送数据
        * 注：这里随便发哈，主要作用就是通过这个动作，让客户端与服务端建立联系
        */
        let actions = {
            "test": "12345"
        };
        // this.ws.send(JSON.stringify(actions));
        // console.log("发送"+actions)
        console.log("this.ws.readyState = "+this.ws.readyState+" === this.ws.OPEN =" +this.ws.OPEN)
        if (this.ws.readyState === this.ws.OPEN) {
            this.ws.send(JSON.stringify(actions));
            console.log("发送"+actions)
        }else{
            console.log("未发生发送"+actions)
        }
    }

};

//心跳检测
var heartCheck = {
    timeout: 10000, //9分钟发一次心跳
    timeoutObj: null,
    serverTimeoutObj: null,
    reset: function() {
        console.log('heartCheck reset')
        clearTimeout(this.timeoutObj);
        clearTimeout(this.serverTimeoutObj);
        return this;
    },
    start: function() {
        if(wsObj.ws == null) return;
        console.log('heartCheck start')
        var self = this;
        this.timeoutObj = setTimeout(function() {
            //这里发送一个心跳，后端收到后，返回一个心跳消息，
            //onmessage拿到返回的心跳就说明连接正常
            self.serverTimeoutObj = setTimeout(function() { //如果超过一定时间还没重置，说明后端主动断开了
                console.log("try=close")
                wsObj.ws.close(); //这里为什么要在send检测消息后，倒计时执行这个代码呢，因为这个代码的目的时为了触发onclose方法，这样才能实现onclose里面的重连方法
                //所以这个代码也很重要，没有这个方法，有些时候发了定时检测消息给后端，后端超时（我们自己设定的时间）后，不会自动触发onclose方法。我们只有执行ws.close()代码，让ws触发onclose方法
                //的执行。如果没有这个代码，连接没有断线的情况下而后端没有正常检测响应，那么浏览器时不会自动超时关闭的（比如谷歌浏览器）,谷歌浏览器会自动触发onclose
                //是在断网的情况下，在没有断线的情况下，也就是后端响应不正常的情况下，浏览器不会自动触发onclose，所以需要我们自己设定超时自动触发onclose，这也是这个代码的
                //的作用。

            }, self.timeout)
        }, this.timeout)
    }
}

