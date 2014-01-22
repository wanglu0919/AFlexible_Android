package com.intertid.aflexible.exception;
/**
 * 
 * @author wanglu 泰得利通
 *
 */
public class AFlexibleException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AFlexibleException() {
		super();
	}

	public AFlexibleException(String msg) {
		super(msg);
	}

	public AFlexibleException(Throwable ex) {
		super(ex);
	}

	public AFlexibleException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
