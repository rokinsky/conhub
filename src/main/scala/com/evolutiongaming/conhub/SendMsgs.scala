package com.evolutiongaming.conhub

import akka.actor.Address
import com.evolutiongaming.conhub.transport.SendMsg
import com.evolutiongaming.nel.Nel

trait SendMsgs[Id, T, M] extends ConnTypes[T, M] {

  def apply(msg: M, con: C.Connected): Unit

  def remote(msgs: Nel[M], addresses: Iterable[Address]): Unit

  def local(msg: M, cons: Iterable[C], remote: Boolean): Unit
}

object SendMsgs {

  def apply[Id, T, M](sendMsg: SendMsg[Nel[M]]): SendMsgs[Id, T, M] = {

    new SendMsgs[Id, T, M] {

      def apply(msg: M, con: C.Connected): Unit = {
        con match {
          case con: C.Local  => con.send(MsgAndRemote(msg))
          case con: C.Remote => remote(Nel(msg), List(con.address))
        }
      }

      def remote(msgs: Nel[M], addresses: Iterable[Address]): Unit = {
        sendMsg(msgs, addresses)
      }

      def local(msg: M, cons: Iterable[C], remote: Boolean): Unit = {
        val msgAndRemote = MsgAndRemote(msg, remote)
        for {con <- cons} con match {
          case x: C.Local => x.send(msgAndRemote)
          case _          =>
        }
      }
    }
  }


  def empty[Id, T, M]: SendMsgs[Id, T, M] = new SendMsgs[Id, T, M] {
    def apply(msg: M, con: C.Connected): Unit = {}
    def remote(msgs: Nel[M], addresses: Iterable[Address]): Unit = {}
    def local(msg: M, cons: Iterable[C], remote: Boolean): Unit = {}
  }
}
