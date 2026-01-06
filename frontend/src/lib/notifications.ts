import { Notification } from "../models";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081";

let eventSource: EventSource | null = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 3;
let lastErrorTime = 0;
const ERROR_THROTTLE_MS = 5000; // Only log errors every 5 seconds

export function connectNotifications(
  userId: string,
  onNotification: (notification: Notification) => void,
): boolean {
  // Only run in browser environment
  if (typeof window === "undefined") {
    console.warn("SSE connection can only be established in browser environment");
    return false;
  }

  const token = localStorage.getItem("token");

  if (!token) {
    console.error("No authentication token found for SSE connection");
    return false;
  }

  // Disconnect any existing connection
  disconnectNotifications();

  // Reset reconnect attempts on manual connection
  reconnectAttempts = 0;

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
      reconnectAttempts = 0; // Reset on successful connection
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
      const now = Date.now();
      
      // Throttle error logging to prevent console spam
      if (now - lastErrorTime > ERROR_THROTTLE_MS) {
        console.error("SSE connection error:", error);
        lastErrorTime = now;
      }
      
      // Check connection state
      if (eventSource?.readyState === EventSource.CLOSED) {
        reconnectAttempts++;
        console.log(`SSE connection closed (attempt ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`);
        
        // Only fully disconnect after max reconnect attempts
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
          console.warn("Max SSE reconnection attempts reached, giving up");
          disconnectNotifications();
        }
      } else if (eventSource?.readyState === EventSource.CONNECTING) {
        // Connection is attempting to reconnect, this is normal behavior
        if (now - lastErrorTime > ERROR_THROTTLE_MS) {
          console.log("SSE reconnecting...");
        }
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
  // Reset reconnect state
  reconnectAttempts = 0;
  lastErrorTime = 0;
}
