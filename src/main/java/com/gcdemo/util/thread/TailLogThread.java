package com.gcdemo.util.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.websocket.Session;

import org.apache.log4j.Logger;

import com.gcdemo.util.JschHandle;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

/**
 * 项目名称：  LogPrintByWebsocket   
 * 类名称：  TailLogThread   
 * 描述：  前台实时推送后台日志线程
 * @author  gaocheng   
 * 创建时间：  2017年11月28日 下午8:41:56 
 * 修改人：gaocheng    修改日期： 2017年11月28日
 * 修改备注：
 *
 */
public class TailLogThread extends Thread {
	private static final Logger logger = Logger.getLogger(TailLogThread.class);
	/**
	 * websocket的session 
	 */
	private Session session;
	/**
	 * 返回的执行通道 
	 */
	private ChannelExec channelExec;
	/**
	 * jcsh
	 */
	private JschHandle jschHandle;
	/**
	 * 服务日志路径 tail -f 拼接 
	 */
	private String serviceLogPath;

	public TailLogThread(Session session, JschHandle jschHandle, String serviceLogPath) throws JSchException {

		this.session = session;
		this.jschHandle = jschHandle;
		this.channelExec = jschHandle.getChannel();
		this.serviceLogPath = serviceLogPath;

	}

	@Override
	public void run() {

		String command = "tail -f " + serviceLogPath + "/logs/catalina.out";

		logger.info("command:" + command);

		InputStream in = null;
		BufferedReader bufferedReader = null;
		try {
			// 执行命令，返回执行结果
			channelExec.setCommand(command);
			channelExec.setInputStream(null);
			channelExec.setErrStream(System.err);
			in = channelExec.getInputStream();

			// 中文乱码 使用utf-8
			bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			channelExec.connect();

			// 循环获取结果
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				// 将实时日志通过WebSocket发送给客户端，给每一行添加一个HTML换行
				if (session.isOpen()) {
					try {
						session.getBasicRemote().sendText(line + "<br>");
					} catch (Exception e) {
						logger.info("session  websocket发送日志错误!");
						logger.info("session状态：" + session.isOpen());
						break;
					}
				} else {
					// session 关闭
					logger.info("session状态：" + session.isOpen());
					break;

				}

			}
			logger.info("readLine执行完毕");

		} catch (Exception e) {
			logger.error("强制跳出tail -f命令", e);
		} finally {
			// 缓冲关闭
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					logger.error("bufferedReader未正确关闭！");
				}
			}
			// 关闭通道
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error("channelExec流未正确关闭！");
				}
			}
			if (channelExec.isConnected()) {
				channelExec.disconnect();
			}
			jschHandle.destory();
			logger.info("已关闭远程连接和readline操作！");
		}

	}

	/** 
	 * 方法名：  closeConnection 
	 * 描述：  关闭channelExec 强制终止输出流 停止tail -f 命令
	 * @author  gaocheng   
	 * 创建时间：2017年11月28日 下午8:43:45
	 *
	 */
	public void closeConnection() {
		if (channelExec.isConnected()) {
			String command = "kill -9 $(ps -ef|grep 'tail -f " + serviceLogPath
					+ "/logs/catalina.out'|grep -v grep|awk '{print $2}')";
			// 执行对应的kill 方法 杀死查看日志的进程
			try {
				jschHandle.execCmdAndWaitRes(command);
			} catch (Exception e) {
				logger.error("杀死进程：" + command + "失败");
			}
		}

		jschHandle.destory();

	}
}
