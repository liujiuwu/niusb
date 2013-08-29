package code.lib

import net.rubyeye.xmemcached.MemcachedClient
import net.rubyeye.xmemcached.XMemcachedClientBuilder
import net.rubyeye.xmemcached.command.BinaryCommandFactory
import net.rubyeye.xmemcached.utils.AddrUtil
import net.liftweb.util.TimeHelpers
import net.liftweb.util.Helpers._

object MemcachedHelper extends App {
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

  def get(key: String): Option[Any] = {
    checkKey(key)
    Option(client.get(key))
  }

  def delete(key: String): Boolean = {
    checkKey(key)
    client.delete(key)
  }
}