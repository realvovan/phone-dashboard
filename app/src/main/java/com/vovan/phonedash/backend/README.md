## REST API Specification

The app runs an internal HTTP server via [`DashboardServerService`](app/src/main/java/com/vovan/phonedash/backend/DashboardServerService.java) on port **8080**.

### `POST /health`

Receives live health and activity data and updates the internal [`HealthRegistry`](app/src/main/java/com/vovan/phonedash/dataProviders/HealthRegistry.java).

#### Request Headers
```http
Content-Type: application/x-www-form-urlencoded
```

#### Request Body
The server accepts `postData` containing a JSON object:
```json
{
  "steps": 6500,
  "distance": 4800,
  "active_energy": 320,
  "resting_energy": 1450
}
```

#### Response
```json
{
  "ok": true
}
```

> [!TIP]
> **iOS Shortcut Integration**: You can set up an Automation on an iPhone using iOS Shortcuts to send health data via an HTTP POST request to `http://<phone-ip>:8080/health` whenever a specific app opens or on a recurring schedule.