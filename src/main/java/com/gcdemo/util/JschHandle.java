package com.gcdemo.util;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * 项目名称：  LogPrintByWebsocket   
 * 类名称：  JschHandle   
 * 描述：  使用jsch ssh免密登录 进行远程操作服务器命令
 * @author  gaocheng   
 * 创建时间：  2017年11月28日 下午4:46:17 
 * 修改人：gaocheng    修改日期： 2017年11月28日
 * 修改备注：
 *
 */
public class JschHandle {

	private static final Logger log = Logger.getLogger(JschHandle.class);

	// 用户名
	private String username;

	// 私钥密码
	private String passphrase;

	// 本地测试私钥地址 这里是在Windows下进行测试使用路径 自己设置 如果放到服务器上 设置为对应私钥的地址
	private String priKey = "D://JSchTest//id_rsa";

	// 远程主机
	private String host;

	// 端口号 不同主机端口号不同，自己需要想办法处理
	private int port = 22;

	private JSch jsch;

	protected Session session = null;

	// 首次初始化标识
	private boolean firstInit = false;

	public JschHandle(String username, String passphrase, String host, String port, String userType)
			throws JSchException {
		// 设置属性

		this.username = "root";

		this.host = host;

		// 实例对象
		init();
	}

	/** 
	 * 方法名：  init 
	 * 描述：  初始化
	 * @author  gaocheng   
	 * 创建时间：2017年11月28日 下午4:57:26
	 * @throws JSchException
	 *
	 */
	protected void init() throws JSchException {
		try {

			log.info(username);
			log.info(priKey);
			validate();
			// 加载配置

			JSch.setConfig("StrictHostKeyChecking", "no");
			jsch = new JSch();
			// 加载证书
			jsch.addIdentity(priKey, passphrase);
			// 获取session
			session = jsch.getSession(username, host, port);

			// 连接
			session.connect();
			log.info("JSCH session connect success.");

		} catch (JSchException e) {
			e.printStackTrace();
			log.error(e.getMessage());
			throw e;
		}
	}

	/** 
	 * 方法名：  validate 
	 * 描述：  验证数据完整性
	 * @author  gaocheng   
	 * 创建时间：2017年11月28日 下午5:04:11
	 * @throws JSchException
	 *
	 */
	private void validate() throws JSchException {
		if (firstInit) {
			return;
		}
		if (username == null || username.isEmpty()) {
			throw new JSchException("Parameter:username is empty.");
		}
		// 验证host
		if (host == null || host.isEmpty()) {
			throw new JSchException("Parameter:host is empty.");
		} else {
			// 验证地址
			try {
				InetAddress inet = InetAddress.getByName(host);
				host = inet.getHostAddress();
				log.info("JSCH connection address:" + host + ":" + port);
			} catch (UnknownHostException e) {
				throw new JSchException(e.getMessage(), e);
			}
		}

		firstInit = true;
	}

	/** 
	 * 方法名：  destory 
	 * 描述：  销毁操作
	 * @author  gaocheng   
	 * 创建时间：2017年11月28日 下午4:59:51
	 *
	 */
	public void destory() {
		if (session != null) {
			session.disconnect();
		}
	}

	/** 
	 * 方法名：  execCmdAndWaitRes 
	 * 描述：  exec方式执行并等待返回结果
	 * @author  gaocheng   
	 * 创建时间：2017年11月28日 下午5:00:09
	 * @param command
	 * @return
	 * @throws Exception
	 *
	 */
	public String execCmdAndWaitRes(String command) throws Exception {
		log.info("执行handle命令" + command);
		// 获得执行通道
		ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
		int res = -1;
		StringBuffer buf = new StringBuffer(1024);
		try {
			// 执行命令，返回执行结
			channelExec.setCommand(command);
			channelExec.setInputStream(null);
			channelExec.setErrStream(System.err);
			InputStream in = channelExec.getInputStream();
			channelExec.connect();
			// 设置缓冲区
			byte[] tmp = new byte[1024];
			// 循环获取结果
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0) {
						break;
					}

					buf.append(new String(tmp, 0, i));
				}
				if (channelExec.isClosed()) {
					res = channelExec.getExitStatus();
					log.info(String.valueOf(res) + "," + buf.toString());
					break;
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			// 关闭通道
			channelExec.disconnect();
		}
		if (res != 0) {
			log.info("执行命令出错：" + command);
		}
		return String.valueOf(res) + "," + buf.toString();
	}

	/** 
	 * 方法名：  getChannel 
	 * 描述：  返回对应的服务器命令执行通道
	 * @author  gaocheng   
	 * 创建时间：2017年11月28日 下午8:46:10
	 * @return
	 * @throws JSchException
	 *
	 */
	public ChannelExec getChannel() throws JSchException {

		return (ChannelExec) session.openChannel("exec");

	}

	/** 
	 * 方法名：  getExitCodeMean 
	 * 描述：    翻译命令执行结果
	 * @author  gaocheng   
	 * 创建时间：2017年11月28日 下午5:02:20
	 * @param exitCode
	 * @return
	 *
	 */
	public String getExitCodeMean(String exitCode) {
		String res = "";
		switch (exitCode) {
			case "1":
				res = "命令错误！";
				break;
			case "2":
				res = "shell内建的错误！";
				break;
			case "126":
				res = "调用的命令不可执行！";
				break;
			case "127":
				res = "找不到命令！";
				break;
			case "128":
				res = "无效的退出码！";
				break;
			default:
				break;
		}
		return res;

	}

}