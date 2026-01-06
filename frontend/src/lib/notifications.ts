import { Notification } from "../models";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081";

let eventSource: EventSource | null = null;

export function connectNotifications(
  userId: string,
  onNotification: (notification: Notification) => void,
): boolean {
  const token =
    typeof window !== "undefined" ? localStorage.getItem("token") : null;

  if (!token) {
    console.error("No authentication token found for SSE connection");
    return false;
  }

  // Disconnect any existing connection
  disconnectNotifications();

  // Create SSE connection with authentication token as query parameter
  // NOTE: Passing JWT in URL is not ideal as it may be logged in server logs,
  // browser history, and proxy logs. This is a limitation of the EventSource API
  // which doesn't support custom headers. Consider these alternatives for production:
  // 1. Use a short-lived token specifically for SSE
  // 2. Use session cookies instead of JWT for SSE authentication
  // 3. Implement a separate handshake endpoint to exchange JWT for a secure session
  const url = new URL(`${API_BASE_URL}/api/notifications/stream`);
  url.searchParams.set("token", token);
  
  try {
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
    
    return true;
  } catch (error) {
    console.error("Failed to establish SSE connection:", error);
    return false;
  }
}

export function disconnectNotifications() {
  if (eventSource) {
    console.log("Closing SSE connection");
    eventSource.close();
    eventSource = null;
  }
}
