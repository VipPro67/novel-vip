import { Notification } from "../models";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081";

let eventSource: EventSource | null = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 3;
let lastErrorTime = 0;
const ERROR_THROTTLE_MS = 5000; // Only log errors every 5 seconds
let isConnecting = false; // Guard to prevent multiple simultaneous connections
let currentUserId: string | null = null; // Track current user to prevent duplicate connections
let shouldReconnect = true; // Flag to control reconnection behavior

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

  // Prevent duplicate connections for the same user
  if (isConnecting) {
    console.log("SSE connection already in progress, skipping...");
    return false;
  }
  
  if (eventSource && eventSource.readyState !== EventSource.CLOSED && currentUserId === userId) {
    console.log("SSE connection already exists for user:", userId, "state:", eventSource.readyState);
    return false;
  }

  // Force disconnect any existing connection (even if state seems closed)
  if (eventSource) {
    console.log("Force closing existing EventSource before creating new one");
    try {
      eventSource.close();
    } catch (e) {
      console.warn("Error force-closing EventSource:", e);
    }
    eventSource = null;
  }

  // Set connection state
  isConnecting = true;
  currentUserId = userId;

  // Reset reconnect attempts on manual connection
  reconnectAttempts = 0;
  shouldReconnect = true; // Enable reconnection for this new connection attempt

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
      isConnecting = false; // Connection successful
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
      //Check if we should give up on reconnecting
      if (!shouldReconnect) {
        console.log("Reconnection disabled, not attempting reconnect");
        disconnectNotifications();
        return;
      }
      
      // Throttle error logging to prevent console spam
      if (now - lastErrorTime > ERROR_THROTTLE_MS) {
        lastErrorTime = now;
      }
      
      // CRITICAL: EventSource auto-reconnects on error, creating infinite connections.
      // We MUST force close it and stop reconnecting to prevent accumulating dead connections.
      reconnectAttempts++;
      
      if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
        console.error(`SSE reconnect attempts exceeded (${reconnectAttempts}), permanently disabling reconnection`);
        shouldReconnect = false; // Disable future reconnection attempts
        disconnectNotifications();
        return;
      }
      
      // Check connection state
      if (eventSource?.readyState === EventSource.CLOSED) {
        console.log(`SSE connection closed (attempt ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`);
        // Let EventSource handle reconnect naturally, but we'll stop after MAX attempts
      } else if (eventSource?.readyState === EventSource.CONNECTING) {
        console.log(`SSE reconnecting... (attempt ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`);
        // EventSource is already trying to reconnect
      } else if (eventSource?.readyState === EventSource.OPEN) {
        // Connection is open but got an error - this is unusual, force close
        console.warn("SSE error on open connection, force closing");
        shouldReconnect = false; // Disable future reconnection attempts
        console.warn("SSE error on open connection, force closing");
        disconnectNotifications();
      }
    };
    
    return true;
  } catch (error) {
    console.error("Failed to establish SSE connection:", error);
    isConnecting = false; // Reset connection flag on exception
    return false;
  }
}

export function disconnectNotifications() {
  if (eventSource) {
    console.log("Closing SSE connection, readyState:", eventSource.readyState);
    try {
      eventSource.close();
    } catch (error) {
  shouldReconnect = true; // Reset for next connection attempt
      console.error("Error closing EventSource:", error);
    }
    eventSource = null;
  }
  // Reset all connection state
  reconnectAttempts = 0;
  lastErrorTime = 0;
  isConnecting = false;
  currentUserId = null;
}
