package com.pt.ordersystem.ordersystem.utils

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import java.security.SecureRandom

fun genId(size: Int = 7): String {
  return NanoIdUtils.randomNanoId(SecureRandom(), "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray(), size)
}
