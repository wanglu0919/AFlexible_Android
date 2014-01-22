package com.intertid.aflexible.exception;

/**
 * 
 * @author wanglu 泰得利通
 *
 */
public class ViewException extends AFlexibleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String strMsg = null;

	public ViewException(String strExce) {
		strMsg = strExce;
	}

	public void printStackTrace() {
		if (strMsg != null)
			System.err.println(strMsg);

		super.printStackTrace();
	}
}
