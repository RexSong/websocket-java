package org.rex.example.websocketjava;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@ServerEndpoint(value = "/ws/server")
@Component
public class WebSocketServer {
  private static final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();
  private static final AtomicInteger onlineCount = new AtomicInteger(0);

  @OnOpen
  public void onOpen(Session session) {
    sessions.add(session);
    int count = onlineCount.incrementAndGet();
    log.info("new connection join, current connection number is : " + count);
    this.sendMessage(session, "connected successful");
  }

  @OnClose
  public void onClose(Session session) {
    sessions.remove(session);
    int count = onlineCount.decrementAndGet();
    log.info("1 connection closed, current connection number is : " + count);
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    log.info("message from client: " + message);
    this.sendMessage(session, "received message, content is : " + message);
  }

  @OnError
  public void onError(Session session, Throwable error) {
    log.error("error occur: Session ID: " + error.getMessage() + session.getId());
  }

  public void sendMessage(Session session, String message) {
    try{
      session.getBasicRemote().sendText("SID:::" + session.getId() + ":::" + message);
    } catch (IOException ex) {
      log.error("send message error: " + ex.getMessage());
    }
  }

  public void sendMessageToAll(String message) {
    for(Session s : sessions) {
      if(s.isOpen()) {
        this.sendMessage(s, message);
      }
    }
  }

  public void sendMessage(String sessionId, String message) {
    Optional<Session> session = sessions.stream().filter(s -> s.getId().equals(sessionId)).findFirst();
    if(session.isPresent()) {
      this.sendMessage(session.get(), message);
    } else {
      log.warn("can't find session: " + sessionId);
    }
  }
}
