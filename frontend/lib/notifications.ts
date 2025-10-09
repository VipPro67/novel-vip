import { Client, IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { Notification } from "./api";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081";

let client: Client | null = null;

export function connectNotifications(
  userId: string,
  onNotification: (notification: Notification) => void,
) {
  const token =
    typeof window !== "undefined" ? localStorage.getItem("token") : null;

  client = new Client({
    webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
    connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
    reconnectDelay: 5000,
  });

  client.onConnect = () => {
    client?.subscribe(`/topic/user.${userId}`, (message: IMessage) => {
	  console.log("connect:/toppic/user.",userId)
      const notification: Notification = JSON.parse(message.body);
      onNotification(notification);
    });
  };

  client.activate();
}

export function disconnectNotifications() {
  if (client) {
    client.deactivate();
    client = null;
  }
}
