package com.gcdemo.util.thread;

import org.springframework.core.task.TaskExecutor;

/**
 * 项目名称：  LogPrintByWebsocket   
 * 类名称：  ThreadTaskPool   
 * 描述：  推送任务的线程池
 * @author  gaocheng   
 * 创建时间：  2017年11月28日 下午7:23:20 
 * 修改人：gaocheng    修改日期： 2017年11月28日
 * 修改备注：
 *
 */
public class ThreadTaskPool {

	private static TaskExecutor taskExecutor;

	/** 
	 * 方法名：  push 
	 * 描述：  将任务推送到线程中，进行执行
	 * @author  gaocheng   
	 * 创建时间：2017年11月28日 下午7:22:58
	 * @param task
	 *
	 */
	public static void push(Runnable task) {
		ThreadTaskPool.taskExecutor.execute(task);
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		ThreadTaskPool.taskExecutor = taskExecutor;
	}

}
