package com.gcdemo.util.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.gcdemo.util.JschHandle;
import com.gcdemo.util.thread.TailLogThread;
import com.gcdemo.util.thread.ThreadTaskPool;

/**
 * 项目名称：  LogPrintByWebsocket   
 * 类名称：  WsLogHandle   
 * 描述：  日志处理websocket
 * @author  gaocheng   
 * 创建时间：  2017年11月28日 下午7:24:24 
 * 修改人：gaocheng    修改日期： 2017年11月28日
 * 修改备注：
 *
 */
@ServerEndpoint("/log/{userType}/{ip}/{projectPath}")
public class WsLogHandle {
	private static final Logger logger = Logger.getLogger(WsLogHandle.class);

	/**
	 * 存放sessionID对应的tailLogThread
	 */
	private static Map<String, TailLogThread> sessionMap = new ConcurrentHashMap<String, TailLogThread>();

	@OnOpen
	public void onOpen(@PathParam(value = "ip") String ip, @PathParam(value = "projectPath") String projectPath,
			@PathParam(value = "userType") String userType, Session session) {

		logger.info("WSLogHandle加入新的websocket连接，标识：" + session.getId());
		logger.info("ip：" + ip + "  path:" + projectPath);
		if (StringUtils.isNoneBlank(ip, projectPath)) {
			// 根据传入的参数ip port进行对应的远程调用linux命令
			try {
				JschHandle js = new JschHandle("", "", ip, "", userType);
				// 线程池放入线程
				TailLogThread tailLogThread = new TailLogThread(session, js, StringUtils.replace(projectPath, "@", "/"));
				sessionMap.put(session.getId(), tailLogThread);
				ThreadTaskPool.push(tailLogThread);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@OnMessage
	public void onMessage(String message, Session session) {

	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		logger.info("WSLogHandle关闭已有websocket连接，标识：" + session.getId());
		// 关闭对应的session的日志输出命令
		TailLogThread tailLogThread = sessionMap.get(session.getId());
		// 关闭channel连接 停止readline操作
		tailLogThread.closeConnection();
		// 清除
		sessionMap.remove(session.getId());
	}

	@OnError
	public void error(Session session, java.lang.Throwable throwable) {
		logger.error("WSLogHandle因存在异常，关闭已有websocket连接，标识：" + session.getId(), throwable);
		// 关闭对应的session的日志输出命令
		TailLogThread tailLogThread = sessionMap.get(session.getId());
		// 关闭channel连接 停止readline操作
		tailLogThread.closeConnection();
		// 清除
		sessionMap.remove(session.getId());
	}

}
