# TradingDemo

TradingDemo is a native Android app built for the Webminix assessment. It demonstrates a trading interface using sample market data, a demo account, and simulated buy and sell orders.

## Features

- **Quotes** — Browse trading symbols and view sample bid and ask prices.
- **Charts** — View candlestick charts, switch symbols and timeframes, and use the crosshair.
- **Trade** — Place simulated buy and sell orders and view demo positions.
- **History** — Browse the Positions, Orders, and Deals tabs.
- **Other screens** — Explore Accounts, Brokers, Messages, News, and Mailbox.

## Technology

- **Java** handles navigation, user interactions, and simulated trading.
- **Android XML** defines layouts and app resources.
- **Android Canvas** draws the candlestick charts and crosshair.
- **Gradle** builds the Android app.

## Run the app

1. Clone or download this repository.
2. Open the project root in Android Studio and let Gradle sync.
3. Connect an Android phone with USB debugging enabled or start an emulator.
4. Select the device and click **Run**.

## Build the APK

From the project root, run this command in PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.