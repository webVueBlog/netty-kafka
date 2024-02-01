package com.lx.cp.utils;

import javax.xml.bind.DatatypeConverter;

/**
 * 消息工具类
 */
public class MsgUtil {

	// 消息分隔符
	public static final byte DELIMITER = 0x7e;

	/**
	 * 将字节数组转换为十六进制字符串
	 *
	 * @param v 字节数组
	 * @return 十六进制字符串
	 */
	public static String hexBinary2str(byte[] v) {
		return DatatypeConverter.printHexBinary(v);
	}

	/**
	 * 将十六进制字符串转换为字节数组
	 *
	 * @param str 十六进制字符串
	 * @return 字节数组
	 */
	public static byte[] str2hexBinary(String str) {
		return DatatypeConverter.parseHexBinary(str);
	}
}
