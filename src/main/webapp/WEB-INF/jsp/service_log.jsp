<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<html>
<head>
<link href='./assets/stylesheets/bootstrap/bootstrap.css' media='all' rel='stylesheet' type='text/css' />
<link href='./assets/stylesheets/light-theme.css' id='color-settings-body-color' media='all' rel='stylesheet' type='text/css' />

<meta charset="utf-8">
<title>服务器日志</title>
<script src='./assets/javascripts/jquery/jquery.min.js' type='text/javascript'></script>
<script  type="text/javascript">
String.prototype.replaceAll = function (FindText, RepText) {
    regExp = new RegExp(FindText, "g");
    return this.replace(regExp, RepText);
}
</script>
<style type="text/css"> 
html,body{height:100%;margin:0px;}
</style>
</head>
<body>
<input type="hidden" id="wsurllog" value="${ info.wsurllog }"/>
<input type="hidden" id="userType" value="${ info.userType }"/>
<input type="hidden" id="serviceIP" value="${ info.serviceIP }"/>
<input type="hidden" id="projectPath" value="${ info.projectPath }"/>
<div class="group-header">
    <div class="row-fluid">
        <div class="span6 offset2" style="height: 10%;">
            <div class="text-center">
                <h2>IP ： ${ info.serviceIP }</h2>
                <small class="muted">tail -f 日志</small>
            </div>
        </div>
    </div>
</div>
<div class="group-body">
	<div id="log-container" style="height: 75%; overflow-y: scroll; background: #333; color: #aaa; padding: 10px;font-size: 15px">
		<div>
		</div>
	</div>
</div>
	
</body>
<script>
	$(document).ready(function() {
		// 指定websocket路径
		
		//获取ip和服务器id
		var userType = $('#userType').val();
		var ip = $('#serviceIP').val();
		var projectPath = $('#projectPath').val().replaceAll('/', '.');
		var wsurllog = $('#wsurllog').val();

		var websocket = new WebSocket(wsurllog + userType +  "/" + ip + "/" + projectPath);
		websocket.onmessage = function(event) {
			//日志信息过多删除最老的数据
			if($("#log-container div").children('p').length > 100){
				$("#log-container div").children('p').eq(0).remove();
			}
			// 接收服务端的实时日志并添加到HTML页面中
			$("#log-container div").append("<p>"+event.data+"</p>");

			// 滚动条滚动到最低部
			$("#log-container").scrollTop($("#log-container div").height() - $("#log-container").height());
		};
	});
</script>
</body>
</html>