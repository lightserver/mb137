package pl.setblack.lsa.events

class NodeConnection(
                       val targetId:Long,
                       val protocol:Protocol) {
                          def send[T] ( msg:Message[T]): Unit = {
                            protocol.send(msg)
                          }

                          def knows (id: Long) : Boolean = {
                            id == targetId
                          }
                        }
