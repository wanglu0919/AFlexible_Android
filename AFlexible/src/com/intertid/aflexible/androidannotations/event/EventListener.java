package com.intertid.aflexible.androidannotations.event;

import java.lang.reflect.Method;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.intertid.aflexible.exception.ViewException;
/**
 * 
 * @author wanglu 泰得利通
 *
 */
public class EventListener implements OnClickListener, OnLongClickListener,
		OnItemClickListener, OnItemLongClickListener {

	private Object handler;
	private String clickMethodName;
	private String longClickMethodName;
	private String itemClickMethodName;
	private String itemLongClickMethodName;

	public EventListener(Object handler) {
		this.handler = handler;
	}

	public EventListener click(String clickMethodName) {
		this.clickMethodName = clickMethodName;
		return this;
	}

	public EventListener longClick(String longClickMethodName) {
		this.longClickMethodName = longClickMethodName;
		return this;
	}

	public EventListener itemClick(String itemClickMethodName) {
		this.itemClickMethodName = itemClickMethodName;
		return this;
	}

	public EventListener itemLongClick(String itemLongClickMethodName) {
		this.itemLongClickMethodName = itemLongClickMethodName;
		return this;
	}

	@Override
	public void onClick(View v) {

		invokeClickMethod(handler, clickMethodName, v);
	}

	@Override
	public boolean onLongClick(View v) {

		return invokeLongClickMethod(handler, longClickMethodName, v);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		invokeItemClickMethod(handler, itemClickMethodName, arg0, arg1, arg2,
				arg3);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {

		return invokeItemLongClickMethod(handler, itemLongClickMethodName,
				arg0, arg1, arg2, arg3);
	}

	private static Object invokeClickMethod(Object handler, String methodName,
			Object... params) {
		if (handler == null)
			return null;
		Method method = null;
		try {
			method = handler.getClass().getDeclaredMethod(methodName,
					View.class);
			if (method != null)
				return method.invoke(handler, params);
			else
				throw new ViewException("no such method:" + methodName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	private static boolean invokeLongClickMethod(Object handler,
			String methodName, Object... params) {
		if (handler == null)
			return false;
		Method method = null;
		try {
			// public boolean onLongClick(View v)
			method = handler.getClass().getDeclaredMethod(methodName,
					View.class);
			if (method != null) {
				Object obj = method.invoke(handler, params);
				return obj == null ? false : Boolean.valueOf(obj.toString());
			} else
				throw new ViewException("no such method:" + methodName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}

	private static Object invokeItemClickMethod(Object handler,
			String methodName, Object... params) {
		if (handler == null)
			return null;
		Method method = null;
		try {
			// /onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			method = handler.getClass().getDeclaredMethod(methodName,
					AdapterView.class, View.class, int.class, long.class);
			if (method != null)
				return method.invoke(handler, params);
			else
				throw new ViewException("no such method:" + methodName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static boolean invokeItemLongClickMethod(Object handler,
			String methodName, Object... params) {
		if (handler == null)
			throw new ViewException(
					"invokeItemLongClickMethod: handler is null :");
		Method method = null;
		try {
			// /onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,long
			// arg3)
			method = handler.getClass().getDeclaredMethod(methodName,
					AdapterView.class, View.class, int.class, long.class);
			if (method != null) {
				Object obj = method.invoke(handler, params);
				return Boolean.valueOf(obj == null ? false : Boolean
						.valueOf(obj.toString()));
			} else
				throw new ViewException("no such method:" + methodName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

}
