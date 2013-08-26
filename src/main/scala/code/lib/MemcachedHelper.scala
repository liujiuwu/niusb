package code.lib

import net.rubyeye.xmemcached.MemcachedClient
import net.rubyeye.xmemcached.XMemcachedClientBuilder
import net.rubyeye.xmemcached.command.BinaryCommandFactory
import net.rubyeye.xmemcached.utils.AddrUtil

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

  def set(key: String, value: Any, exp: Int = 0): Boolean = {
    checkKey(key)
    client.set(key, exp, value)
  }

  def get(key: String): Option[Any] = {
    checkKey(key)
    Option(client.get(key))
  }

  def delete(key: String): Boolean = {
    checkKey(key)
    client.delete(key)
  }

  set("name", List(1, 2, 3, 4))
  println(get("name"))
}