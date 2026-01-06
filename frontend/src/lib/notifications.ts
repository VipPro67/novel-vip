import { Notification } from "../models";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081";

let eventSource: EventSource | null = null;

export function connectNotifications(
  userId: string,
  onNotification: (notification: Notification) => void,
) {
  const token =
    typeof window !== "undefined" ? localStorage.getItem("token") : null;

  if (!token) {
    console.error("No authentication token found");
    return;
  }

  // Disconnect any existing connection
  disconnectNotifications();

  // Create SSE connection with authentication token as query parameter
  // EventSource doesn't support custom headers, so we pass the token in the URL
  const url = new URL(`${API_BASE_URL}/api/notifications/stream`);
  url.searchParams.set("token", token);
  
  eventSource = new EventSource(url.toString());

  eventSource.onopen = () => {
    console.log("SSE connection established for user:", userId);
  };

  eventSource.addEventListener("connected", (event) => {
    console.log("SSE connected:", event.data);
  });

  eventSource.addEventListener("notification", (event) => {
    try {
      const notification: Notification = JSON.parse(event.data);
      onNotification(notification);
    } catch (error) {
      console.error("Failed to parse notification:", error);
    }
  });

  eventSource.onerror = (error) => {
    console.error("SSE connection error:", error);
    
    // EventSource will automatically reconnect unless we explicitly close it
    // Only disconnect if the connection is in CLOSED state
    if (eventSource?.readyState === EventSource.CLOSED) {
      console.log("SSE connection closed, cleaning up");
      disconnectNotifications();
    }
  };
}

export function disconnectNotifications() {
  if (eventSource) {
    console.log("Closing SSE connection");
    eventSource.close();
    eventSource = null;
  }
}
