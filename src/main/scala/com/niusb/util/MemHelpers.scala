package com.niusb.util

import net.rubyeye.xmemcached.XMemcachedClientBuilder
import net.rubyeye.xmemcached.command.BinaryCommandFactory
import net.rubyeye.xmemcached.utils.AddrUtil
import net.liftweb.util.Helpers._
import java.util.Date

object MemHelpers extends MemHelpers

trait MemHelpers {
  lazy val client = initMemcachedClient()

  private def initMemcachedClient(servers: String = "42.120.5.18:11211") = {
    val builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(servers))
    //builder.setConnectionPoolSize(5)
    builder.setCommandFactory(new BinaryCommandFactory())
    //builder.getConfiguration().setStatisticsServer(false)
    val memcachedClient = builder.build()
    memcachedClient.setEnableHeartBeat(false)
    memcachedClient
  }

  private def checkKey(key: String) {
    require(!key.isEmpty(), "key不能为空")
  }

  def set(key: String, value: Any, exp: TimeSpan = 0 seconds): Boolean = {
    checkKey(key)
    client.set(key, (exp.millis / 1000L).toInt, value)
  }

  def incr(key: String, setp: Long = 1, default: Int = 1): Long = {
    checkKey(key)
    client.incr(key, setp, default)
  }

  def decr(key: String, setp: Long = 1, default: Int = 1): Long = {
    checkKey(key)
    client.decr(key, setp, default)
  }

  def get(key: String): Option[Any] = {
    checkKey(key)
    Option(client.get(key))
  }

  def delete(key: String): Boolean = {
    checkKey(key)
    client.delete(key)
  }
}

object MyTest extends App {
  val key = "13826526941_count_"+WebHelpers.ndf.format(new Date)
  MemHelpers.delete(key)
  /*for (i <- 1 to 10) {
    MemHelpers.incr(key, 1)
    println(MemHelpers.get(key).get)
  }*/
}