package pl.setblack.mb137.data


class ClientState {
   var sysState : SysState = new SysState(1)
   var connections : Seq[Connection] = Seq()

}
