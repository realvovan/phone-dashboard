# PhoneDashboard Module Architecture (`:app`)

This document details the software architecture, grid system engine, widget lifecycle management, and extension guidelines for the `:app` module of **PhoneDashboard**.

---

## 🏛️ Core Architectural Overview

```text
               +----------------------------------+
               |           MainActivity           |
               +----------------------------------+
                                |
                   creates & configures
                                |
                                v
               +----------------------------------+
               |      WidgetContainerLayout       | (ViewGroup)
               +----------------------------------+
                                |
                     manages layout & sizing
                                |
                                v
               +----------------------------------+
               |         WidgetContainer          | (Data Model Grid)
               +----------------------------------+
                                |
                    holds collection of
                                |
                                v
               +----------------------------------+
               |       DashboardWidgetBase        |
               +----------------------------------+
                 /              |               \
                /               |                \
    (implements)          (implements)       (implements)
         v                      v                  v
+------------------+  +-------------------+  +-------------------+
|  LoopingWidget   |  |  ClickableWidget  |  | Custom Views      |
+------------------+  +-------------------+  +-------------------+
         |                      |
    handled by             handled by
         v                      v
+------------------+  +-------------------+
| LoopWidgetsHandler| |ClickWidgetsHandler|
+------------------+  +-------------------+
```

---

## 📐 Grid Layout & Measure Engine

The layout engine uses a decoupled architecture separating the grid data model from the Android view rendering pipeline.

### 1. `WidgetContainer`
- Manages an N*M coordinate grid.
- Enforces boundary limits and performs AABB (Axis-Aligned Bounding Box) collision checks before adding any widget.
- Dispatches state updates (`STARTED`, `PAUSED`) to widget handlers.

### 2. `WidgetContainerLayout`
- A custom `ViewGroup` that measures available space and enforces 1:1 square grid cells.
- **Auto-Scaling Algorithm**:
  - Subtracts padding reserved for `WidgetPixelShifter`.
  - Calculates `mCellSize = Math.min(usableWidth / columns, usableHeight / rows)`.
  - Centers the grid on screen by applying calculated horizontal (`mOffsetX`) and vertical (`mOffsetY`) margins.
- Instantiates widgets reflectively via `addWidget(Class<T> widgetClass, int gridX, int gridY, Object... params)`.

### 3. `WidgetPixelShifter`
- Prevents OLED/AMOLED display burn-in on always-on screens.
- Translates `WidgetContainerLayout` position by small step increments every 30 seconds.
- Cycles through geometric patterns:
  - **Square Pattern**: `(1,0) -> (0,1) -> (-1,0) -> (0,-1)`
  - **Cross Pattern**: `(1,0) -> (-1,1) -> (1,0) -> (-1,-1)`
  - **Diagonal Pattern**: `(0,1) -> (1,-1) -> (0,1) -> (-1,-1)`

---

## 🧩 Widget Lifecycle & Contracts

All dashboard widgets inherit from [`DashboardWidgetBase<TWidgetInfo>`](src/main/java/com/vovan/phonedash/widgets/DashboardWidgetBase.java).

```java
public abstract class DashboardWidgetBase<TWidgetInfo> {
    public abstract int width();                       // Grid width span (cells)
    public abstract int height();                      // Grid height span (cells)
    public abstract View getView();                    // The root View instance
    public abstract void setWidgetInfo(TWidgetInfo info); // Updates view content
    public abstract TWidgetInfo getDefaultWidgetInfo();
}
```

### Optional Widget Capabilities

- **`LoopingWidget`**:
  ```java
  public interface LoopingWidget {
      long onLoop(); // Invoked periodically on UI thread. Returns next delay in milliseconds.
  }
  ```
- **`ClickableWidget`**:
  ```java
  public interface ClickableWidget {
      void onClick(View view);
      boolean onLongClick(View view);
  }
  ```

---

## 💻 Creating a New Custom Widget

To create a new widget, follow these steps:

### Step 1: Define Widget Info Data Model
```java
public class MyWidgetInfo {
    public String title;
    public int value;
    public MyWidgetInfo(String title, int value) {
        this.title = title;
        this.value = value;
    }
}
```

### Step 2: Implement the Widget Class
```java
public class MyWidget extends DashboardWidgetBase<MyWidgetInfo> implements ClickableWidget {
    private final View rootView;
    private final TextView valueTextView;

    public MyWidget(Context context, WidgetContainer container, int gridX, int gridY) {
        super(context, container, gridX, gridY);
        this.rootView = LayoutInflater.from(context).inflate(R.layout.my_widget_layout, null);
        this.valueTextView = rootView.findViewById(R.id.value_text);

        // Required: attach attach/detach lifecycle listeners
        this.addStateChangeListeners();
    }

    @Override public int width() { return 1; }
    @Override public int height() { return 1; }

    @Override
    public void setWidgetInfo(MyWidgetInfo info) {
        this.valueTextView.setText(String.valueOf(info.value));
    }

    @Override
    public MyWidgetInfo getDefaultWidgetInfo() {
        return new MyWidgetInfo("Default", 0);
    }

    @Override public View getView() { return this.rootView; }

    @Override
    public void onClick(View view) {
        // Handle tap interaction
    }
}
```

### Step 3: Add Widget to Layout in `MainActivity`
```java
container.addWidget(MyWidget.class, 2, 0);
```

---

## 🛠️ Data Providers & REST Server

- **`DashboardServerService`**: Foreground service initializing a NanoHTTPD `WebServer` instance on port `8080`.
- **`Router`**: Maps `(HTTP_METHOD, URI_PATH)` to `RouteHandler` lambdas.
- **`HealthRegistry`**: Thread-safe in-memory registry holding activity counts (`stepCount`, `distance`, `activeCalories`). Observers register via `addHealthUpdateCallback(...)`.
