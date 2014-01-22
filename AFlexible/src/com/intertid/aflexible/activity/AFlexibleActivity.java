package com.intertid.aflexible.activity;

import java.lang.reflect.Field;

import android.app.Activity;
import android.view.View;
import android.widget.AbsListView;

import com.intertid.aflexible.androidannotations.androidannotations.ViewById;
import com.intertid.aflexible.androidannotations.event.EventListener;

/**
 * 
 * @author wanglu 泰得利通
 *
 */
public abstract class AFlexibleActivity extends Activity {

	@Override
	public void setContentView(int layoutResID) {

		super.setContentView(layoutResID);

		findViews();
	}

	/**
	 * 查找控件并初始化 wanglu 泰得利通
	 */
	private void findViews() {
		Field fields[] = getClass().getDeclaredFields();// 属性

		if (fields != null && fields.length > 0) {

			for (Field field : fields) {

				field.setAccessible(true);

				try {
					if (field.get(this) != null) {
						continue;
					}

					ViewById viewById = field.getAnnotation(ViewById.class);
					if (viewById != null) {
						int viewId = viewById.value();
						field.set(this, findViewById(viewId));// 实例化值

						setListener(field, viewById.click(), Method.Click);
						setListener(field, viewById.longClick(),
								Method.LongClick);
						setListener(field, viewById.itemClick(),
								Method.ItemClick);
						setListener(field, viewById.itemLongClick(),
								Method.ItemLongClick);
					}

				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

			}

		}

	}

	/**
	 * 事件绑定
	 *wanglu 泰得利通 
	 * @param field
	 * @param methodName
	 * @param method
	 */
	private void setListener(Field field, String methodName, Method method) {
		if (methodName == null || (methodName != null && methodName.equals(""))) {
			return;
		}

		try {
			Object obj = field.get(this);

			switch (method) {
			case Click:
				if (obj instanceof View) {
					((View) obj).setOnClickListener(new EventListener(this)
							.click(methodName));
				}
				break;
			case LongClick:

				if (obj instanceof View) {
					((View) obj).setOnLongClickListener(new EventListener(this)
							.longClick(methodName));
				}
				break;
			case ItemClick:

				if (obj instanceof AbsListView) {
					((AbsListView) obj)
							.setOnItemClickListener(new EventListener(this)
									.itemClick(methodName));
				}
				break;
			case ItemLongClick:
				if (obj instanceof AbsListView) {
					((AbsListView) obj)
							.setOnItemLongClickListener(new EventListener(this)
									.itemLongClick(methodName));
				}
				break;

			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	public enum Method {
		Click, LongClick, ItemClick, ItemLongClick
	}

}
