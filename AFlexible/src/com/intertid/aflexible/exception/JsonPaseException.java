package com.intertid.aflexible.exception;


public class JsonPaseException extends AFlexibleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6251732319441400084L;

	private String strMsg = null;

	public JsonPaseException(String strExce) {
		strMsg = strExce;
	}

	public void printStackTrace() {
		if (strMsg != null)
			System.err.println(strMsg);

		super.printStackTrace();
	}

}
