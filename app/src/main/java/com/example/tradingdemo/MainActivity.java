package com.example.tradingdemo;

import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String ACCOUNT_NAME = "Webminix Dev";
    private static final String ACCOUNT_ID = "DEMO-1024";
    private static final String ACCOUNT_SERVER = "Sample Server";
    private static final String ACCOUNT_ACCESS_POINT = "Access Server";
    private static final String ACCOUNT_BALANCE = "5,000.00";
    private static final int BLUE = 0xff087cf1;
    private static final int INK = 0xff222222;
    private static final int MUTED = 0xff666666;
    private static final int ACCENT = 0xff009688;
    private static final String[] PAGES = {"Accounts", "Quotes", "Charts", "Trade", "History", "Messages", "Certificates", "Indicators", "Indicators", "Parameters", "Order", "Account registration", "Authorize", "Brokers", "News", "Mailbox"};
    private static final String[] ICONS = {"", "quotes", "chart", "trade", "history", "messages"};
    private static final int QUOTE_SYMBOL_COUNT = 8;
    private static final String[] SYMBOLS = {"NZDUSD.x","AUDUSD.x","EURUSD.x","GBPUSD.x","USDCAD.x","USDJPY.x","USDCHF.x","XAUUSD.x","XAGUSD.x","XPDUSD.x","XPTUSD.x"};
    private static final double[] BIDS = {0.54980,0.66660,1.09824,1.31042,1.43162,158.81,0.84102,3571.03,50.22,1425.10,1588.20};
    private static final double[] SPREADS = {0.00007,0.00010,0.00008,0.00010,0.00014,0.003,0.00010,0.18,0.02,0.15,0.12};
    private static final String[] CHART_PERIODS = {"M1","M5","M15","M30","H1","H4","D1","W1","MN"};
    private static final int[] CHART_MINUTES = {1,5,15,30,60,240,1440,10080,43200};
    private static final String[] CHART_NAMES = {"New Zealand Dollar vs US Dollar",
            "Australian Dollar vs US Dollar","Euro vs US Dollar","British Pound vs US Dollar",
            "US Dollar vs Canadian Dollar","US Dollar vs Japanese Yen",
            "US Dollar vs Swiss Franc","Gold vs US Dollar","Silver vs US Dollar",
            "Palladium vs US Dollar","Platinum vs US Dollar"};
    private int chartPeriod = 1;
    private int activeWindow;
    private final java.util.ArrayList<Integer> chartWindows = new java.util.ArrayList<>();
    private static final int INDICATOR_MANAGER = 7;
    private static final int INDICATOR_CATALOG = 8;
    private static final int INDICATOR_PARAMETERS = 9;
    private final Bundle indicatorOptions = new Bundle();
    private Bundle indicatorDraft = new Bundle();
    private String editingIndicator = "";
    private int parametersBackPage = INDICATOR_CATALOG;
    private EditText indicatorPeriod;
    private final Spinner[] indicatorWidths = new Spinner[3];
    private final java.util.ArrayList<String> selectedIndicators = new java.util.ArrayList<>();
    private static final int ORDER_TICKET = 10;
    private static final int REGISTRATION = 11;
    private static final int AUTHORIZE = 12;
    private static final int BROKERS = 13;
    private static final int NEWS = 14;
    private static final int MAILBOX = 15;
    private int authorizeBack = 5;
    private static final int REGISTRATION_BLUE = 0xff486fa9;
    private int orderBack = 2;
    private int orderSymbol;
    private Bundle orderDraft = new Bundle();
    private final java.util.ArrayList<Bundle> demoPositions = new java.util.ArrayList<>();
    private EditText orderVolume,orderStop,orderTarget;
    private Spinner orderPolicy;
    private double demoBalance = 5000;
    private TextView tradeTitle;
    private final TextView[] tradeTotals = new TextView[3];
    private final java.util.ArrayList<PositionRow> positionRows = new java.util.ArrayList<>();
    private boolean tradeNewestFirst;
    private TextView orderBid,orderAsk;
    private OrderGraph orderGraph;
    private CandleChart activeChart;
    private final java.util.ArrayList<CandleChart> chartPanes = new java.util.ArrayList<>();
    private ImageView crosshairButton;
    private static final int PRICE_DOWN = 0xffd83a29;
    private final android.os.Handler quoteHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Random quoteRandom = new Random();
    private final long[] bidTicks = new long[SYMBOLS.length];
    private final long[] askTicks = new long[SYMBOLS.length];
    private final long[] lowTicks = new long[SYMBOLS.length];
    private final long[] highTicks = new long[SYMBOLS.length];
    private final int[] bidColors = new int[SYMBOLS.length];
    private final int[] askColors = new int[SYMBOLS.length];
    private final TextView[] bidViews = new TextView[SYMBOLS.length];
    private final TextView[] askViews = new TextView[SYMBOLS.length];
    private final TextView[] lowViews = new TextView[SYMBOLS.length];
    private final TextView[] highViews = new TextView[SYMBOLS.length];
    private final TextView[] changeViews = new TextView[SYMBOLS.length];
    private final TextView[] timeViews = new TextView[SYMBOLS.length];
    private final TextView[] spreadViews = new TextView[SYMBOLS.length];
    private final long[] previousCloseTicks = new long[SYMBOLS.length];
    private final long[] updatedAt = new long[SYMBOLS.length];
    private final java.text.SimpleDateFormat quoteTime =
            new java.text.SimpleDateFormat("HH:mm:ss",Locale.US);
    private final Runnable quoteRefresh = this::refreshQuotes;
    private boolean quoteScreenStarted;

    private FrameLayout root, content, drawer;
    private LinearLayout toolbar, navigation;
    private int page, symbol, historyTab;
    private boolean messageSearch;
    private String messageQuery = "";
    private int historySymbol = -1, historySort = 4, historyRange;
    private boolean historyDescending;
    private long customHistoryStart,customHistoryEnd;
    private long depositTime = System.currentTimeMillis();
    private final java.util.ArrayList<Bundle> historyEvents = new java.util.ArrayList<>();
    private boolean simple;
    private boolean accountDeleted;
    private boolean savedDemoPassword;
    private long lastBack = -2000;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        initializeQuotes();
        root = findViewById(R.id.main);
        toolbar = findViewById(R.id.toolbar);
        navigation = findViewById(R.id.navigation);
        content = findViewById(R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        root.setBackgroundColor(BLUE);
        androidx.core.view.WindowInsetsControllerCompat controller =
                ViewCompat.getWindowInsetsController(root);
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }
        getWindow().setNavigationBarColor(Color.BLACK);
        accountDeleted = getPreferences(MODE_PRIVATE).getBoolean("accountDeleted",false);
        savedDemoPassword = getPreferences(MODE_PRIVATE).getBoolean("savedDemoPassword",false);
        if (state != null) {
            java.util.ArrayList<Integer> windows = state.getIntegerArrayList("chartWindows");
            if (windows != null) chartWindows.addAll(windows);
            activeWindow = state.getInt("activeWindow");
            demoBalance = state.getDouble("demoBalance",5000);
            tradeNewestFirst = state.getBoolean("tradeNewestFirst");
            orderBack = state.getInt("orderBack",2);
            orderSymbol = state.getInt("orderSymbol");
            Bundle restoredOrder = state.getBundle("orderDraft");
            if (restoredOrder != null) orderDraft = restoredOrder;
            java.util.ArrayList<Bundle> positions = state.getParcelableArrayList("demoPositions");
            if (positions != null) demoPositions.addAll(positions);
            chartPeriod = state.getInt("chartPeriod",1);
            java.util.ArrayList<String> restored = state.getStringArrayList("indicators");
            if (restored != null) selectedIndicators.addAll(restored);
            Bundle options = state.getBundle("indicatorOptions");
            if (options != null) indicatorOptions.putAll(options);
            Bundle draft = state.getBundle("indicatorDraft");
            if (draft != null) indicatorDraft = draft;
            editingIndicator = state.getString("editingIndicator","");
            parametersBackPage = state.getInt("parametersBackPage",INDICATOR_CATALOG);
            page = state.getInt("page");
            symbol = state.getInt("symbol");
            authorizeBack = state.getInt("authorizeBack",5);
            messageSearch = state.getBoolean("messageSearch");
            messageQuery = state.getString("messageQuery","");
            historyTab = state.getInt("historyTab");
            historySymbol = state.getInt("historySymbol",-1);
            historySort = state.getInt("historySort",4);
            historyRange = state.getInt("historyRange");
            historyDescending = state.getBoolean("historyDescending");
            customHistoryStart = state.getLong("customHistoryStart");
            customHistoryEnd = state.getLong("customHistoryEnd");
            depositTime = state.getLong("depositTime",depositTime);
            java.util.ArrayList<Bundle> events = state.getParcelableArrayList("historyEvents");
            if (events != null) historyEvents.addAll(events);
            simple = state.getBoolean("simple");
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (drawer != null) {
                    closeDrawer();
                } else if (page == 5 && messageSearch) {
                    messageSearch = false;
                    messageQuery = "";
                    showPage(5);
                } else if (activeChart != null && activeChart.menuOpen) {
                    activeChart.toggleMenu();
                } else if (page == NEWS || page == MAILBOX) {
                    showPage(1);
                } else if (page == BROKERS) {
                    showPage(0);
                } else if (page == AUTHORIZE) {
                    showPage(authorizeBack);
                } else if (page == REGISTRATION) {
                    showPage(5);
                } else if (page == ORDER_TICKET) {
                    showPage(orderBack);
                } else if (page == INDICATOR_PARAMETERS) {
                    showPage(parametersBackPage);
                } else if (page == INDICATOR_CATALOG) {
                    showPage(INDICATOR_MANAGER);
                } else if (page == INDICATOR_MANAGER) {
                    showPage(2);
                } else if (page == 6) {
                    showPage(0);
                } else if (SystemClock.elapsedRealtime() - lastBack < 2000) {
                    finish();
                } else {
                    lastBack = SystemClock.elapsedRealtime();
                    Toast.makeText(MainActivity.this,"Press Back again to exit",Toast.LENGTH_SHORT).show();
                }
            }
        });
        showPage(page);
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (page == INDICATOR_PARAMETERS) captureIndicatorDraft();
        state.putBundle("indicatorDraft",indicatorDraft);
        state.putBundle("indicatorOptions",indicatorOptions);
        state.putString("editingIndicator",editingIndicator);
        state.putInt("parametersBackPage",parametersBackPage);
        state.putStringArrayList("indicators",new java.util.ArrayList<>(selectedIndicators));
        state.putInt("page",page);
        if (page == ORDER_TICKET) captureOrder();
        state.putDouble("demoBalance",demoBalance);
        state.putBoolean("tradeNewestFirst",tradeNewestFirst);
        state.putInt("orderBack",orderBack);
        state.putInt("orderSymbol",orderSymbol);
        state.putBundle("orderDraft",orderDraft);
        state.putParcelableArrayList("demoPositions",new java.util.ArrayList<>(demoPositions));
        syncChartWindow();
        state.putIntegerArrayList("chartWindows",new java.util.ArrayList<>(chartWindows));
        state.putInt("activeWindow",activeWindow);
        state.putInt("chartPeriod",chartPeriod);
        state.putInt("symbol",symbol);
        state.putInt("authorizeBack",authorizeBack);
        state.putBoolean("messageSearch",messageSearch);
        state.putString("messageQuery",messageQuery);
        state.putInt("historyTab",historyTab);
        state.putInt("historySymbol",historySymbol);
        state.putInt("historySort",historySort);
        state.putInt("historyRange",historyRange);
        state.putBoolean("historyDescending",historyDescending);
        state.putLong("customHistoryStart",customHistoryStart);
        state.putLong("customHistoryEnd",customHistoryEnd);
        state.putLong("depositTime",depositTime);
        state.putParcelableArrayList("historyEvents",new java.util.ArrayList<>(historyEvents));
        state.putBoolean("simple",simple);
    }

    private void showPage(int destination) {
        quoteHandler.removeCallbacks(quoteRefresh);
        closeDrawer();
        lastBack = -2000;
        activeChart = null;
        chartPanes.clear();
        crosshairButton = null;
        indicatorPeriod = null;
        java.util.Arrays.fill(indicatorWidths,null);
        page = destination;
        if (page != 5) { messageSearch = false; messageQuery = ""; }
        toolbar.removeAllViews();
        toolbar.setBackgroundColor(BLUE);
        root.setBackgroundColor(BLUE);
        positionRows.clear();
        navigation.removeAllViews();
        content.removeAllViews();
        content.setBackgroundColor(Color.WHITE);

        boolean drawerPage = page == NEWS || page == MAILBOX;
        navigation.setVisibility(page >= 6 && page != BROKERS && !drawerPage ? View.GONE : View.VISIBLE);
        int previousPage = page == AUTHORIZE ? authorizeBack : page == REGISTRATION ? 5 : page == ORDER_TICKET ? orderBack
                                                                                          : page == INDICATOR_PARAMETERS ? parametersBackPage
                                                                                            : page == INDICATOR_CATALOG ? INDICATOR_MANAGER
                                                                                              : page == INDICATOR_MANAGER ? 2 : 0;
        ImageView leading = page >= 6 && !drawerPage
                ? iconAction("back","Back",Color.WHITE,() -> showPage(previousPage))
                : iconAction("menu","Open menu",Color.WHITE,this::openDrawer);
        toolbar.addView(leading,new LinearLayout.LayoutParams(dp(48),-1));
        if (page == REGISTRATION || page == AUTHORIZE) {
            toolbar.setBackgroundColor(REGISTRATION_BLUE);
            root.setBackgroundColor(REGISTRATION_BLUE);
        }
        TextView title = label(PAGES[page],14,Color.WHITE);
        if (page == 2) title.setText("");
        if (page == ORDER_TICKET) {
            title.setText(SYMBOLS[orderSymbol]+"\n"+CHART_NAMES[orderSymbol]);
            title.setMaxLines(2);
            ImageView picker = iconAction("symbols","Choose order symbol",Color.WHITE,() -> {});
            picker.setOnClickListener(view -> new AlertDialog.Builder(this).setTitle("Choose symbol")
                    .setItems(SYMBOLS,(dialog,which) -> {
                        captureOrder();
                        orderDraft.remove("stop");
                        orderDraft.remove("target");
                        orderSymbol = which;
                        showPage(ORDER_TICKET);
                    }).show());
            toolbar.addView(title,new LinearLayout.LayoutParams(0,-1,1));
            toolbar.addView(picker,new LinearLayout.LayoutParams(dp(48),-1));
            title.setGravity(Gravity.CENTER_VERTICAL);
        }
        if (page == INDICATOR_PARAMETERS) {
            title.setText("Parameters: "+editingIndicator);
            title.setSingleLine(true);
            title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        }
        if (page == INDICATOR_MANAGER) {
            title.setText("Indicators on "+SYMBOLS[symbol]+", "+CHART_PERIODS[chartPeriod]);
            title.setSingleLine(true);
        }
        if (page == 4) title.setText("History\n"+(historySymbol < 0 ? "All symbols" : SYMBOLS[historySymbol]));
        title.setGravity(Gravity.CENTER_VERTICAL);
        if (page != ORDER_TICKET) toolbar.addView(title,new LinearLayout.LayoutParams(0,-1,1));
        if (page == 0) {
            toolbarAction("certificate","Certificates",() -> showPage(6));
            toolbarAction("plus","Add account",() -> showPage(BROKERS));
            ImageView more = iconAction("more","Account options",Color.WHITE,() -> {});
            more.setOnClickListener(view -> showAccountOptions(more));
            toolbar.addView(more,new LinearLayout.LayoutParams(dp(48),-1));
        }
        if (page == 6) toolbarAction("plus","Add certificate",() ->
                message("Add certificate","This demo has no certificate files to install."));
        if (page == INDICATOR_PARAMETERS) {
            TextView done = action("DONE","Done",Color.WHITE,this::saveIndicatorParameters);
            done.setTextSize(14);
            done.setTypeface(Typeface.DEFAULT_BOLD);
            toolbar.addView(done,new LinearLayout.LayoutParams(dp(58),-1));
        }
        if (page == INDICATOR_MANAGER) toolbarAction("trash","Delete indicators",this::deleteIndicators);
        if (page == MAILBOX) {
            toolbarAction("compose_mail","Compose mail",this::composeDemoMail);
            toolbarAction("trash","Delete mail",() -> message("Mailbox","No mail to delete."));
        }
        if (page == 5) {
            toolbarAction("mqid","MetaQuotes ID",this::showMessageId);
            toolbarAction("message_search","Search messages",() -> {
                messageSearch = !messageSearch;
                if (!messageSearch) messageQuery = "";
                showPage(5);
            });
        }
        if (page == 4) {
            title.setOnClickListener(this::historySymbols);
            historyToolbarButton("symbols","Filter symbols",this::historySymbols);
            historyToolbarButton("trade_sort","Sort history",this::historySorting);
            historyToolbarButton("calendar","History period",this::historyCalendar);
        }
        if (page == 3) {
            tradeTitle = title;
            toolbarAction("trade_sort","Reverse position order",() -> {
                tradeNewestFirst = !tradeNewestFirst;
                showPage(3);
            });
            toolbarAction("new_order","New order",() -> orderTicket(symbol));
        }
        if (page == 1) toolbarAction("plus","Choose symbol",this::chooseSymbol);
        if (page == 2) {
            crosshairButton = iconAction("crosshair","Enable chart crosshair",Color.WHITE,
                    () -> activeChart.toggleCrosshair());
            toolbar.addView(crosshairButton,new LinearLayout.LayoutParams(dp(48),-1));
            toolbarAction("indicator","Indicators",() -> showPage(INDICATOR_MANAGER));
            ImageView symbols = iconAction("symbols","Choose chart symbol",Color.WHITE,() -> {});
            symbols.setOnClickListener(view -> showChartSymbols(view));
            toolbar.addView(symbols,new LinearLayout.LayoutParams(dp(48),-1));
            ImageView windows = iconAction("windows","Charts",Color.WHITE,() -> {});
            windows.setOnClickListener(this::showChartWindows);
            toolbar.addView(windows,new LinearLayout.LayoutParams(dp(48),-1));
            toolbarAction("new_order","New order",() -> orderTicket(symbol));
        }
        if (page == 1) toolbarAction("edit","Change quote view",() -> {
            simple = !simple;
            showPage(1);
        });

        for (int index = 1; index < ICONS.length; index++) {
            final int tab = index;
            int tint = index == page ? BLUE : 0xff555555;
            LinearLayout tabView = column(0xfff5f5f5);
            tabView.setGravity(Gravity.CENTER);
            ImageView icon = new ImageView(this);
            icon.setScaleType(ImageView.ScaleType.CENTER);
            icon.setImageDrawable(new LineIcon(ICONS[index],tint,22));
            tabView.addView(icon,new LinearLayout.LayoutParams(-1,dp(24)));
            TextView caption = label(PAGES[index],11,tint);
            caption.setGravity(Gravity.CENTER);
            caption.setIncludeFontPadding(false);
            caption.setSingleLine(true);
            if (index == page) caption.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            LinearLayout.LayoutParams captionParams = new LinearLayout.LayoutParams(-1,dp(18));
            captionParams.topMargin = dp(1);
            tabView.addView(caption,captionParams);
            tabView.setContentDescription(PAGES[index]);
            tabView.setFocusable(true);
            tabView.setOnClickListener(view -> showPage(tab));
            tabView.setSelected(index == page);
            navigation.addView(tabView,new LinearLayout.LayoutParams(0,-1,1));
        }

        LinearLayout body = column(Color.WHITE);
        content.addView(body,new FrameLayout.LayoutParams(-1,-1));
        switch (page) {
            case 0: accounts(body); break;
            case 1: quotes(body); break;
            case 2: charts(body); break;
            case 3: tradeScreen(body); break;
            case ORDER_TICKET: orderScreen(body); break;
            case 4: history(body); break;
            case 5: messages(body); break;
            case REGISTRATION: registrationScreen(body,false); break;
            case AUTHORIZE: registrationScreen(body,true); break;
            case BROKERS: brokersScreen(body); break;
            case NEWS: sidebarEmpty(body,"empty_news","No news"); break;
            case MAILBOX: sidebarEmpty(body,"mail","No mail"); break;
            case INDICATOR_MANAGER: indicatorManager(body); break;
            case INDICATOR_CATALOG: indicatorCatalog(body); break;
            case INDICATOR_PARAMETERS: indicatorParameters(body); break;
            case 6:
                body.setPadding(0,dp(20),0,0);
                centered(body,"No installed certificates",14,MUTED);
                break;
        }
    }

    private void accounts(LinearLayout body) {
        if (accountDeleted) {
            centered(body,"No accounts",14,MUTED);
            return;
        }
        FrameLayout area = new FrameLayout(this);
        body.addView(area,new LinearLayout.LayoutParams(-1,-2));
        View blue = new View(this);
        blue.setBackgroundColor(BLUE);
        area.addView(blue,new FrameLayout.LayoutParams(-1,dp(112)));
        LinearLayout card = column(Color.WHITE);
        card.setPadding(dp(12),dp(14),dp(12),dp(14));
        card.setElevation(dp(3));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1,-2);
        params.setMargins(dp(8),dp(10),dp(8),dp(8));
        area.addView(card,params);
        ImageView mark = new ImageView(this);
        mark.setImageDrawable(new LineIcon("brand",0xff168330,46));
        LinearLayout.LayoutParams markParams = new LinearLayout.LayoutParams(dp(46),dp(46));
        markParams.gravity = Gravity.CENTER_HORIZONTAL;
        markParams.bottomMargin = dp(2);
        card.addView(mark,markParams);
        centered(card,ACCOUNT_NAME,15,INK);
        ((TextView) card.getChildAt(1)).setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        View detailsGap = new View(this);
        card.addView(detailsGap,new LinearLayout.LayoutParams(-1,dp(22)));
        centered(card,ACCOUNT_ID + " \u2014 " + ACCOUNT_SERVER + "\n"
                + ACCOUNT_ACCESS_POINT + ", Hedge",13,MUTED);

        FrameLayout balanceRow = new FrameLayout(this);
        TextView balance = label(ACCOUNT_BALANCE + " USD",22,INK);
        balance.setGravity(Gravity.CENTER);
        balance.setPadding(dp(48),0,dp(48),0);
        balanceRow.addView(balance,new FrameLayout.LayoutParams(-1,-1));

        ImageView info = iconAction("about","Account properties",0xff888888,this::showAccountProperties);
        FrameLayout.LayoutParams infoParams = new FrameLayout.LayoutParams(
                dp(48),dp(48),Gravity.END | Gravity.CENTER_VERTICAL);
        balanceRow.addView(info,infoParams);
        card.addView(balanceRow,new LinearLayout.LayoutParams(-1,dp(48)));

    }

    private void brokersScreen(LinearLayout body) {
        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);
        ImageView searchIcon = new ImageView(this);
        searchIcon.setScaleType(ImageView.ScaleType.CENTER);
        searchIcon.setImageDrawable(new LineIcon("message_search",MUTED,22));
        searchRow.addView(searchIcon,new LinearLayout.LayoutParams(dp(48),-1));
        EditText search = new EditText(this);
        search.setHint("Find broker");
        search.setSingleLine(true);
        search.setTextSize(18);
        search.setContentDescription("Find broker");
        searchRow.addView(search,new LinearLayout.LayoutParams(0,-1,1));
        searchRow.setPadding(0,0,dp(8),0);
        body.addView(searchRow,new LinearLayout.LayoutParams(-1,dp(48)));

        LinearLayout results = column(Color.WHITE);
        body.addView(results,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout broker = new LinearLayout(this);
        broker.setGravity(Gravity.CENTER_VERTICAL);
        broker.setPadding(dp(16),0,dp(16),0);
        ImageView logo = new ImageView(this);
        logo.setImageDrawable(new LineIcon("brand",0xff168330,42));
        broker.addView(logo,new LinearLayout.LayoutParams(dp(42),dp(42)));
        LinearLayout caption = column(Color.WHITE);
        caption.setPadding(dp(14),0,0,0);
        TextView name = label("The KF Market",17,0xff444444);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        caption.addView(name);
        caption.addView(label("TheKFMarket",15,0xff4895de));
        broker.addView(caption,new LinearLayout.LayoutParams(0,-2,1));
        broker.addView(iconAction("about","Broker information",0xff999999,() ->
                        message("The KF Market","TheKFMarket\nDemo broker listing")),
                new LinearLayout.LayoutParams(dp(48),-1));
        broker.setFocusable(true);
        broker.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("The KF Market")
                .setMessage("Open the local practice account?")
                .setNegativeButton(android.R.string.cancel,null)
                .setPositiveButton("Open",(dialog,which) -> {
                    if (accountDeleted) addDemoAccount();
                    else showPage(0);
                }).show());
        LinearLayout entry = column(Color.WHITE);
        entry.addView(broker,new LinearLayout.LayoutParams(-1,dp(72)));
        View rule = new View(this);
        rule.setBackgroundColor(0xffe1e1e1);
        LinearLayout.LayoutParams ruleParams = new LinearLayout.LayoutParams(-1,dp(1));
        ruleParams.setMarginStart(dp(76));
        entry.addView(rule,ruleParams);
        results.addView(entry);
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence text,int start,int count,int after) {}
            public void onTextChanged(CharSequence text,int start,int before,int count) {
                String query = text.toString().trim().toLowerCase(Locale.ROOT);
                entry.setVisibility(("the kf market thekfmarket".contains(query)) ? View.VISIBLE : View.GONE);
            }
            public void afterTextChanged(android.text.Editable text) {}
        });
        windowDivider(body);
        TextView help = action("CAN'T FIND YOUR BROKER?","Can't find your broker",0xff444444,
                () -> message("Find broker","Search for The KF Market in this demo."));
        help.setTextSize(16);
        help.setTypeface(Typeface.DEFAULT_BOLD);
        help.setBackgroundColor(0xfff5f5f5);
        body.addView(help,new LinearLayout.LayoutParams(-1,dp(48)));
        body.setFocusableInTouchMode(true);
        body.requestFocus();
    }

    private void addDemoAccount() {
        if (!accountDeleted) {
            message("Accounts","The practice account is already available.");
            return;
        }
        accountDeleted = false;
        getPreferences(MODE_PRIVATE).edit().putBoolean("accountDeleted",false).apply();
        showPage(0);
    }

    private void showAccountOptions(View anchor) {
        PopupMenu menu = new PopupMenu(this,anchor,Gravity.END);
        String[] options = {"Change password","Clear saved password","Delete account"};
        for (int i=0;i<options.length;i++) {
            menu.getMenu().add(0,i,i,options[i]).setEnabled(!accountDeleted);
        }
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    new AlertDialog.Builder(this).setTitle("Change password")
                            .setMessage("Simulate a password change for the practice account?")
                            .setNegativeButton(android.R.string.cancel,null)
                            .setPositiveButton("Simulate",(dialog,which) -> {
                                setSavedDemoPassword(true);
                                message("Change password","Practice password change simulated.");
                            }).show();
                    return true;
                case 1:
                    String result = savedDemoPassword
                            ? "Saved practice password cleared." : "No saved practice password.";
                    setSavedDemoPassword(false);
                    message("Clear saved password",result);
                    return true;
                case 2:
                    new AlertDialog.Builder(this).setTitle("Delete account")
                            .setMessage("Remove this practice account from the app?")
                            .setNegativeButton(android.R.string.cancel,null)
                            .setPositiveButton("Delete",(dialog,which) -> {
                                accountDeleted = true;
                                savedDemoPassword = false;
                                getPreferences(MODE_PRIVATE).edit()
                                        .putBoolean("accountDeleted",true)
                                        .putBoolean("savedDemoPassword",false).apply();
                                showPage(0);
                            }).show();
                    return true;
                default: return false;
            }
        });
        menu.show();
    }

    private void setSavedDemoPassword(boolean saved) {
        savedDemoPassword = saved;
        getPreferences(MODE_PRIVATE).edit().putBoolean("savedDemoPassword",saved).apply();
    }

    private void showAccountProperties() {
        AlertDialog dialog = new AlertDialog.Builder(this,
                androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("Properties")
                .setMessage(ACCOUNT_ID + " - " + ACCOUNT_NAME
                        + "\n\nUSD " + ACCOUNT_BALANCE + ", Hedge"
                        + "\naccess point: " + ACCOUNT_ACCESS_POINT)
                .setPositiveButton(android.R.string.ok,null)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(BLUE);
    }

    private void quotes(LinearLayout body) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        body.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout list = column(Color.WHITE);
        scroll.addView(list,new ScrollView.LayoutParams(-1,-2));
        for (int i=0;i<QUOTE_SYMBOL_COUNT;i++) {
            final int selected = i;
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14),dp(7),dp(14),dp(7));
            LinearLayout info = column(Color.WHITE);
            if (!simple) {
                changeViews[i] = label("",10,MUTED);
                changeViews[i].setSingleLine(true);
                info.addView(changeViews[i]);
            }
            TextView instrument = label(SYMBOLS[i],16,INK);
            instrument.setTypeface(Typeface.create("sans-serif-condensed",Typeface.NORMAL));
            instrument.setIncludeFontPadding(false);
            info.addView(instrument);
            if (!simple) {
                LinearLayout metadata = new LinearLayout(this);
                metadata.setGravity(Gravity.CENTER_VERTICAL);
                timeViews[i] = label("",10,MUTED);
                metadata.addView(timeViews[i]);
                ImageView spreadIcon = new ImageView(this);
                spreadIcon.setImageDrawable(new LineIcon("spread",MUTED,11));
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(11),dp(11));
                iconParams.setMargins(dp(4),0,dp(4),0);
                metadata.addView(spreadIcon,iconParams);
                spreadViews[i] = label("",10,MUTED);
                metadata.addView(spreadViews[i]);
                info.addView(metadata);
                renderQuoteDetails(i);
            }
            row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            quotePrice(row,i,true);
            quotePrice(row,i,false);
            row.setOnClickListener(view -> quoteActions(selected));
            FrameLayout quoteRow = new FrameLayout(this);
            quoteRow.addView(row,new FrameLayout.LayoutParams(-1,-1));
            addPositionBadge(quoteRow,i);
            list.addView(quoteRow,new LinearLayout.LayoutParams(-1,dp(simple ? 54 : 72),1f));
        }
        scheduleQuotes();
    }

    @Override protected void onStart() {
        super.onStart();
        quoteScreenStarted = true;
        scheduleQuotes();
    }

    @Override protected void onStop() {
        quoteScreenStarted = false;
        quoteHandler.removeCallbacks(quoteRefresh);
        super.onStop();
    }

    private void initializeQuotes() {
        double[] initialPercent = {0.23,0.08,-0.35,0.17,0.41,-0.50,-0.50,-0.87,0.12,-0.23,0.18};
        long now = System.currentTimeMillis();
        for (int i=0;i<SYMBOLS.length;i++) {
            double scale = tickScale(i);
            bidTicks[i] = Math.round(BIDS[i] * scale);
            previousCloseTicks[i] = Math.round(bidTicks[i] / (1 + initialPercent[i] / 100));
            updatedAt[i] = now;
            askTicks[i] = Math.round((BIDS[i] + SPREADS[i]) * scale);
            lowTicks[i] = Math.round((BIDS[i] - SPREADS[i] * 8) * scale);
            highTicks[i] = Math.round((BIDS[i] + SPREADS[i] * 8) * scale);
            bidColors[i] = BLUE;
            askColors[i] = BLUE;
        }
    }

    private double tickScale(int selected) {
        return selected >= 7 ? 100 : selected == 5 ? 1000 : 100000;
    }

    private void scheduleQuotes() {
        quoteHandler.removeCallbacks(quoteRefresh);
        if (quoteScreenStarted && (page == 1 || page == 2 || page == 3 || page == ORDER_TICKET)) quoteHandler.postDelayed(quoteRefresh,1500);
    }

    private void refreshQuotes() {
        if (!quoteScreenStarted || (page != 1 && page != 2 && page != 3 && page != ORDER_TICKET)) return;
        long now = System.currentTimeMillis();
        for (int i=0;i<SYMBOLS.length;i++) {
            long previousBid = bidTicks[i];
            long previousAsk = askTicks[i];
            bidTicks[i] = Math.max(1,previousBid + quoteRandom.nextInt(5) - 2);
            askTicks[i] = Math.max(bidTicks[i]+1,previousAsk + quoteRandom.nextInt(5) - 2);
            lowTicks[i] = Math.min(lowTicks[i],bidTicks[i]);
            highTicks[i] = Math.max(highTicks[i],askTicks[i]);
            bidColors[i] = tickColor(previousBid,bidTicks[i],bidColors[i]);
            askColors[i] = tickColor(previousAsk,askTicks[i],askColors[i]);
            if (previousBid != bidTicks[i] || previousAsk != askTicks[i]) updatedAt[i] = now;
            if (page == 1 && i < QUOTE_SYMBOL_COUNT) {
                if (!simple) renderQuoteDetails(i);
                renderQuotePrice(i,true);
                renderQuotePrice(i,false);
            }
        }
        for (CandleChart pane : chartPanes) pane.update();
        if (page == ORDER_TICKET) refreshOrderScreen();
        if (page == 3) refreshTradeScreen();
        scheduleQuotes();
    }

    private void renderQuoteDetails(int selected) {
        long change = bidTicks[selected] - previousCloseTicks[selected];
        double percent = 100.0 * change / previousCloseTicks[selected];
        String points = String.format(Locale.US,"%+d",change);
        String percentage = String.format(Locale.US," %.2f%%",percent);
        SpannableString details = new SpannableString(points + percentage);
        int color = change < 0 ? PRICE_DOWN : change > 0 ? BLUE : MUTED;
        details.setSpan(new android.text.style.ForegroundColorSpan(color),
                points.length()+1,details.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        changeViews[selected].setText(details);
        timeViews[selected].setText(quoteTime.format(new java.util.Date(updatedAt[selected])));
        long spread = askTicks[selected] - bidTicks[selected];
        spreadViews[selected].setText(Long.toString(spread));
        spreadViews[selected].setContentDescription("Spread: "+spread+" points");
    }

    private int tickColor(long previous,long current,int existing) {
        if (current > previous) return BLUE;
        if (current < previous) return PRICE_DOWN;
        return existing;
    }

    private void quotePrice(LinearLayout row,int selected,boolean bid) {
        LinearLayout prices = column(Color.WHITE);
        prices.setGravity(Gravity.END);
        TextView text = label("",16,BLUE);
        text.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
        text.setSingleLine(true);
        text.setGravity(Gravity.END);
        prices.addView(text,new LinearLayout.LayoutParams(-1,-2));
        TextView dayRange = label("",11,0xff888888);
        dayRange.setGravity(Gravity.END);
        dayRange.setSingleLine(true);
        if (!simple) prices.addView(dayRange,new LinearLayout.LayoutParams(-1,-2));
        if (bid) {
            bidViews[selected] = text;
            lowViews[selected] = dayRange;
        } else {
            askViews[selected] = text;
            highViews[selected] = dayRange;
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(74),-2);
        params.setMarginStart(dp(4));
        row.addView(prices,params);
        renderQuotePrice(selected,bid);
    }

    private void renderQuotePrice(int selected,boolean bid) {
        TextView text = bid ? bidViews[selected] : askViews[selected];
        text.setTextColor(bid ? bidColors[selected] : askColors[selected]);
        text.setText(styledPrice(currentPrice(selected,bid)));
        if (!simple) {
            TextView range = bid ? lowViews[selected] : highViews[selected];
            long ticks = bid ? lowTicks[selected] : highTicks[selected];
            range.setText((bid ? "L: " : "H: ") + price(selected,ticks / tickScale(selected)));
        }
    }

    private CharSequence styledPrice(String value) {
        int end = value.length();
        boolean fractionalPip = end - value.indexOf('.') > 3;
        int mainEnd = fractionalPip ? end - 1 : end;
        SpannableString styled = new SpannableString(value);
        styled.setSpan(new RelativeSizeSpan(23f / 16f),mainEnd-2,mainEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (fractionalPip) {
            styled.setSpan(new SuperscriptSpan(),mainEnd,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styled.setSpan(new RelativeSizeSpan(11f / 16f),mainEnd,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return styled;
    }

    private String currentPrice(int selected,boolean bid) {
        return price(selected,(bid ? bidTicks[selected] : askTicks[selected]) / tickScale(selected));
    }

    private void quoteActions(int selected) {
        Dialog dialog = new Dialog(this);
        LinearLayout sheet = column(Color.WHITE);
        sheet.addView(action(SYMBOLS[selected],SYMBOLS[selected],INK,dialog::dismiss),new LinearLayout.LayoutParams(-1,dp(48)));
        sheetRow(sheet,"New Order",() -> {
            dialog.dismiss();
            orderTicket(selected);
        });
        sheetRow(sheet,"Chart",() -> {
            dialog.dismiss();
            symbol = selected;
            showPage(2);
        });
        sheetRow(sheet,"Properties",() -> {
            dialog.dismiss();
            message(SYMBOLS[selected],"Bid  "+currentPrice(selected,true)+"\nAsk  "+currentPrice(selected,false));
        });
        TextView unavailable = label("Depth Of Market",16,0xff999999);
        unavailable.setGravity(Gravity.CENTER_VERTICAL);
        unavailable.setPadding(dp(16),0,0,0);
        sheet.addView(unavailable,new LinearLayout.LayoutParams(-1,dp(48)));
        sheetRow(sheet,"Market Statistics",() -> {
            dialog.dismiss();
            message(SYMBOLS[selected],"Sample spread: "+price(selected,(askTicks[selected] - bidTicks[selected]) / tickScale(selected)));
        });
        sheetRow(sheet,simple ? "Advanced view mode" : "Simple view mode",() -> {
            dialog.dismiss();
            simple = !simple;
            showPage(1);
        });
        dialog.setContentView(sheet);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
        if (window != null) window.setLayout(-1,-2);
    }

    private void orderTicket(int selected) {
        orderSymbol = selected;
        orderBack = page;
        orderDraft = new Bundle();
        showPage(ORDER_TICKET);
    }

    private void orderScreen(LinearLayout body) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout form = column(Color.WHITE);
        scroll.addView(form);
        body.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView execution = label("Market Execution",17,0xff444444);
        execution.setGravity(Gravity.CENTER);
        form.addView(execution,new LinearLayout.LayoutParams(-1,dp(46)));
        windowDivider(form);
        LinearLayout volumes = new LinearLayout(this);
        orderVolume = numericOrderField("Volume",orderDraft.getString("volume","1.00"));
        orderVolume.setTypeface(Typeface.DEFAULT_BOLD);
        orderStep(volumes,"-0.1",orderVolume,-0.1,2,0.01);
        orderStep(volumes,"-0.01",orderVolume,-0.01,2,0.01);
        volumes.addView(orderVolume,new LinearLayout.LayoutParams(0,-1,1));
        orderStep(volumes,"+0.01",orderVolume,0.01,2,0.01);
        orderStep(volumes,"+0.1",orderVolume,0.1,2,0.01);
        form.addView(volumes,new LinearLayout.LayoutParams(-1,dp(49)));
        windowDivider(form);

        LinearLayout prices = new LinearLayout(this);
        prices.setPadding(dp(45),0,dp(45),0);
        orderBid = label("",16,BLUE);
        orderAsk = label("",16,BLUE);
        for (TextView quote : new TextView[]{orderBid,orderAsk}) {
            quote.setGravity(Gravity.CENTER);
            quote.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
            prices.addView(quote,new LinearLayout.LayoutParams(0,-1,1));
        }
        form.addView(prices,new LinearLayout.LayoutParams(-1,dp(55)));

        LinearLayout stops = new LinearLayout(this);
        stops.setPadding(dp(16),0,dp(16),0);
        orderStop = addStop(stops,"SL","stop",0xffe89c9c);
        orderTarget = addStop(stops,"TP","target",0xff9bc126);
        form.addView(stops,new LinearLayout.LayoutParams(-1,dp(52)));
        LinearLayout policy = parameterRow(form);
        parameterLabel(policy,"Fill policy");
        orderPolicy = new Spinner(this);
        ArrayAdapter<String> policies = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,
                new String[]{"Fill or Kill","Immediate or Cancel"});
        policies.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderPolicy.setAdapter(policies);
        orderPolicy.setSelection(orderDraft.getInt("policy"));
        orderPolicy.setContentDescription("Fill policy");
        policy.addView(orderPolicy,new LinearLayout.LayoutParams(-2,-1));
        orderGraph = new OrderGraph();
        form.addView(orderGraph,new LinearLayout.LayoutParams(-1,dp(96),1f));

        TextView note = label("Demo market execution. Orders use the simulated bid or ask shown above.",
                13,MUTED);
        note.setGravity(Gravity.CENTER);
        note.setPadding(dp(12),dp(10),dp(12),dp(10));
        note.setBackgroundColor(0xfff5f5f5);
        body.addView(note,new LinearLayout.LayoutParams(-1,-2));
        windowDivider(body);
        LinearLayout buttons = new LinearLayout(this);
        buttons.setBackgroundColor(0xfff5f5f5);
        for (boolean buy : new boolean[]{false,true}) {
            if (buy) {
                View divider = new View(this);
                divider.setBackgroundColor(0xffdddddd);
                buttons.addView(divider,new LinearLayout.LayoutParams(dp(1),-1));
            }
            String side = buy ? "BUY" : "SELL";
            TextView button = action(side+"\nBY MARKET",side+" by market",buy ? BLUE : PRICE_DOWN,
                    () -> submitDemoOrder(buy));
            button.setTextSize(16);
            button.setTypeface(Typeface.create("sans-serif-condensed",Typeface.NORMAL));
            button.setIncludeFontPadding(false);
            SpannableString caption = new SpannableString(side+"\nBY MARKET");
            caption.setSpan(new android.text.style.StyleSpan(Typeface.BOLD),
                    0,side.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            caption.setSpan(new RelativeSizeSpan(12f/16f),
                    side.length()+1,caption.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            button.setText(caption);
            buttons.addView(button,new LinearLayout.LayoutParams(0,-1,1));
        }
        body.addView(buttons,new LinearLayout.LayoutParams(-1,dp(49)));
        form.setFocusableInTouchMode(true);
        form.requestFocus();
        refreshOrderScreen();
        scheduleQuotes();
    }

    private EditText numericOrderField(String hint,String value) {
        EditText field = new EditText(this);
        field.setSingleLine();
        field.setTextSize(16);
        field.setGravity(Gravity.CENTER);
        field.setHint(hint);
        field.setContentDescription(hint);
        field.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        field.setText(value);
        return field;
    }

    private void orderStep(LinearLayout row,String title,EditText field,double delta,int decimals,double floor) {
        TextView step = action(title,title,BLUE,() -> {
            String text = field.getText().toString().trim();
            double value = text.isEmpty() ? (floor == 0 ? orderPrice(true) : 1) : orderNumber(field);
            if (!Double.isFinite(value)) { field.setError("Enter a valid number"); return; }
            field.setText(String.format(Locale.US,"%."+decimals+"f",Math.max(floor,value+delta)));
        });
        step.setTextSize(15);
        row.addView(step,new LinearLayout.LayoutParams(0,-1,1));
    }

    private EditText addStop(LinearLayout stops,String name,String key,int tint) {
        LinearLayout block = column(Color.WHITE);
        LinearLayout fields = new LinearLayout(this);
        fields.setGravity(Gravity.CENTER_VERTICAL);
        EditText input = numericOrderField(name,orderDraft.getString(key,""));
        input.setBackground(null);
        input.setPadding(0,0,0,0);
        input.setMinWidth(0);
        input.setHintTextColor(0xff999999);
        input.setTextColor(0xff444444);
        input.setTextSize(15);
        int decimals = orderSymbol >= 7 ? 2 : orderSymbol == 5 ? 3 : 5;
        double tick = 1/tickScale(orderSymbol);
        orderStep(fields,"\u2212",input,-tick,decimals,0);
        fields.addView(input,new LinearLayout.LayoutParams(0,-1,1));
        orderStep(fields,"+",input,tick,decimals,0);
        for (int index : new int[]{0,2}) {
            TextView button = (TextView) fields.getChildAt(index);
            button.setTypeface(Typeface.DEFAULT_BOLD);
            button.setTextSize(16);
            button.setLayoutParams(new LinearLayout.LayoutParams(dp(24),-1));
        }
        block.addView(fields,new LinearLayout.LayoutParams(-1,0,1));

        FrameLayout bracket = new FrameLayout(this);
        View base = new View(this);
        base.setBackgroundColor(tint);
        bracket.addView(base,new FrameLayout.LayoutParams(-1,dp(1),Gravity.BOTTOM));
        for (int edge : new int[]{Gravity.START,Gravity.END}) {
            View end = new View(this);
            end.setBackgroundColor(tint);
            bracket.addView(end,new FrameLayout.LayoutParams(dp(1),-1,edge));
        }
        block.addView(bracket,new LinearLayout.LayoutParams(-1,dp(4)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,-1,1);
        if (stops.getChildCount() > 0) params.setMarginStart(dp(16));
        stops.addView(block,params);
        return input;
    }

    private void captureOrder() {
        orderDraft.putString("volume",orderVolume.getText().toString());
        orderDraft.putString("stop",orderStop.getText().toString());
        orderDraft.putString("target",orderTarget.getText().toString());
        orderDraft.putInt("policy",orderPolicy.getSelectedItemPosition());
    }

    private double orderNumber(EditText field) {
        try { return Double.parseDouble(field.getText().toString().trim().replace(',','.')); }
        catch (NumberFormatException invalid) { return Double.NaN; }
    }

    private double orderPrice(boolean bid) {
        return (bid ? bidTicks[orderSymbol] : askTicks[orderSymbol])/tickScale(orderSymbol);
    }

    private void refreshOrderScreen() {
        orderBid.setText(styledPrice(currentPrice(orderSymbol,true)));
        orderBid.setTextColor(bidColors[orderSymbol]);
        orderAsk.setText(styledPrice(currentPrice(orderSymbol,false)));
        orderAsk.setTextColor(askColors[orderSymbol]);
        orderGraph.sample();
    }

    private void submitDemoOrder(boolean buy) {
        double volume = orderNumber(orderVolume);
        if (!Double.isFinite(volume) || volume < 0.01 || volume > 100
                || Math.abs(volume*100-Math.round(volume*100)) > 0.000001) {
            orderVolume.setError("Enter 0.01 to 100.00 in steps of 0.01");
            return;
        }
        double entry = orderPrice(!buy);
        if (!validStop(orderStop,entry,!buy) || !validStop(orderTarget,entry,buy)) return;
        Bundle trade = new Bundle();
        trade.putInt("symbol",orderSymbol);
        trade.putBoolean("buy",buy);
        trade.putDouble("volume",volume);
        trade.putDouble("entry",entry);
        trade.putString("stop",orderStop.getText().toString());
        trade.putString("target",orderTarget.getText().toString());
        trade.putInt("policy",orderPolicy.getSelectedItemPosition());
        demoPositions.add(trade);
        recordHistory(trade,false,entry,0);
        showPage(3);
        message("Demo order filled",String.format(Locale.US,"%s %.2f %s at %s",
                buy ? "Buy" : "Sell",volume,SYMBOLS[trade.getInt("symbol")],
                price(trade.getInt("symbol"),entry)));
    }

    private boolean validStop(EditText field,double entry,boolean above) {
        if (field.getText().toString().trim().isEmpty()) return true;
        double value = orderNumber(field);
        if (Double.isFinite(value) && value > 0 && (above ? value > entry : value < entry)) return true;
        field.setError(above ? "Enter a price above the entry" : "Enter a price below the entry");
        return false;
    }

    private void tradeScreen(LinearLayout body) {
        LinearLayout summary = column(Color.WHITE);
        summary.setPadding(dp(14),dp(8),dp(14),dp(8));
        body.addView(summary);
        String[] names = {"Balance:","Equity:","Free margin:"};
        for (int i=0;i<names.length;i++) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            TextView name = label(names[i],14,0xff444444);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(name);
            row.addView(summaryDots(),new LinearLayout.LayoutParams(0,-1,1));
            TextView value = label("",14,BLUE);
            value.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(value);
            tradeTotals[i] = value;
            summary.addView(row,new LinearLayout.LayoutParams(-1,dp(26)));
        }
        windowDivider(body);
        positionRows.clear();
        if (!demoPositions.isEmpty()) {
            TextView heading = label("Positions",13,MUTED);
            heading.setBackgroundColor(0xfff5f5f5);
            heading.setGravity(Gravity.CENTER_VERTICAL);
            heading.setPadding(dp(14),0,0,0);
            body.addView(heading,new LinearLayout.LayoutParams(-1,dp(24)));
            windowDivider(body);
            ScrollView scroll = new ScrollView(this);
            LinearLayout list = column(Color.WHITE);
            scroll.addView(list);
            body.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
            java.util.ArrayList<Bundle> ordered = new java.util.ArrayList<>(demoPositions);
            if (tradeNewestFirst) java.util.Collections.reverse(ordered);
            for (Bundle trade : ordered) {
                PositionRow row = new PositionRow(trade);
                positionRows.add(row);
                list.addView(row,new LinearLayout.LayoutParams(-1,-2));
                windowDivider(list);
            }
        }
        refreshTradeScreen();
        scheduleQuotes();
    }

    private void refreshTradeScreen() {
        double profit = 0;
        for (PositionRow row : positionRows) profit += row.refresh();
        tradeTotals[0].setText(tradeMoney(demoBalance));
        tradeTotals[1].setText(tradeMoney(demoBalance+profit));
        tradeTotals[2].setText(tradeMoney(demoBalance+profit));
        tradeTitle.setText(demoPositions.isEmpty() ? "Trade" : "Trade\n"+tradeMoney(profit)+" USD");
        toolbar.setBackgroundColor(profit < -0.005 ? 0xffdf5448 : BLUE);
        root.setBackgroundColor(profit < -0.005 ? 0xffdf5448 : BLUE);
    }

    private String tradeMoney(double amount) {
        return String.format(Locale.US,"%,.2f",amount).replace(',',' ');
    }

    private double positionExit(Bundle trade) {
        int instrument = trade.getInt("symbol");
        long ticks = trade.getBoolean("buy") ? bidTicks[instrument] : askTicks[instrument];
        return ticks/tickScale(instrument);
    }

    private double positionProfit(Bundle trade,double exit) {
        int instrument = trade.getInt("symbol");
        double units = instrument < 7 ? 100000 : 100;
        double direction = trade.getBoolean("buy") ? 1 : -1;
        double profit = direction*(exit-trade.getDouble("entry"))*trade.getDouble("volume")*units;
        return instrument == 5 ? profit/exit : profit;
    }

    private final class PositionRow extends LinearLayout {
        private final Bundle trade;
        private final TextView prices;
        private final TextView profit;

        PositionRow(Bundle trade) {
            super(MainActivity.this);
            this.trade = trade;
            setPadding(dp(14),dp(7),dp(14),dp(7));
            setGravity(Gravity.CENTER_VERTICAL);
            setMinimumHeight(dp(48));
            int instrument = trade.getInt("symbol");
            boolean buy = trade.getBoolean("buy");
            LinearLayout details = column(Color.WHITE);
            String symbol = SYMBOLS[instrument]+", ";
            String side = String.format(Locale.US,"%s %.2f",buy ? "buy" : "sell",trade.getDouble("volume"));
            SpannableString title = new SpannableString(symbol+side);
            title.setSpan(new android.text.style.ForegroundColorSpan(buy ? BLUE : PRICE_DOWN),
                    symbol.length(),title.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            TextView name = label("",15,INK);
            name.setTypeface(Typeface.create("sans-serif-condensed",Typeface.NORMAL));
            name.setText(title);
            details.addView(name);
            prices = label("",12,MUTED);
            details.addView(prices);
            addView(details,new LinearLayout.LayoutParams(0,-2,1));
            profit = label("",15,BLUE);
            profit.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
            addView(profit);
            setFocusable(true);
            setOnClickListener(view -> new AlertDialog.Builder(MainActivity.this).setTitle("Demo position")
                    .setMessage(symbol+side).setNegativeButton(android.R.string.cancel,null)
                    .setPositiveButton("Close position",(dialog,which) -> {
                        if (demoPositions.remove(trade)) {
                            double exit = positionExit(trade);
                            double realized = positionProfit(trade,exit);
                            demoBalance += realized;
                            recordHistory(trade,true,exit,realized);
                        }
                        showPage(3);
                    }).show());
        }

        double refresh() {
            int instrument = trade.getInt("symbol");
            double exit = positionExit(trade);
            double value = positionProfit(trade,exit);
            prices.setText(price(instrument,trade.getDouble("entry"))+" \u2192 "+price(instrument,exit));
            profit.setText(tradeMoney(value));
            profit.setTextColor(value < -0.005 ? PRICE_DOWN : BLUE);
            return value;
        }
    }

    private void addPositionBadge(FrameLayout row,int selected) {
        for (int i=demoPositions.size()-1;i>=0;i--) {
            Bundle trade = demoPositions.get(i);
            if (trade.getInt("symbol") != selected) continue;
            ImageView badge = new ImageView(this);
            boolean buy = trade.getBoolean("buy");
            badge.setImageDrawable(new LineIcon("position_badge",buy ? BLUE : PRICE_DOWN,15));
            badge.setContentDescription(buy ? "Open demo buy position" : "Open demo sell position");
            row.addView(badge,new FrameLayout.LayoutParams(dp(15),dp(15),Gravity.START|Gravity.TOP));
            return;
        }
    }

    private final class OrderGraph extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final java.util.ArrayList<Long> bids = new java.util.ArrayList<>();
        private final java.util.ArrayList<Long> asks = new java.util.ArrayList<>();

        OrderGraph() { super(MainActivity.this); }

        void sample() {
            if (bids.size() == 32) { bids.remove(0); asks.remove(0); }
            bids.add(bidTicks[orderSymbol]);
            asks.add(askTicks[orderSymbol]);
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            if (bids.isEmpty()) return;
            long low = java.util.Collections.min(bids)-1;
            long high = java.util.Collections.max(asks)+1;
            float right = getWidth()-dp(65);
            float height = Math.max(1,getHeight()-dp(24));
            if (right <= 0) return;
            drawSeries(canvas,bids,low,high,right,height,BLUE);
            drawSeries(canvas,asks,low,high,right,height,PRICE_DOWN);
        }

        private void drawSeries(Canvas canvas,java.util.ArrayList<Long> values,long low,long high,
                                float right,float height,int color) {
            paint.setColor(color);
            paint.setStrokeWidth(dp(1));
            float step = right/31;
            float y = dp(12);
            for (int i=0;i<values.size();i++) {
                float next = dp(12)+height*(high-values.get(i))/(high-low);
                if (i>0) canvas.drawLine((i-1)*step,y,i*step,next,paint);
                y = next;
            }
            canvas.drawLine((values.size()-1)*step,y,right,y,paint);
            canvas.drawRect(right,y-dp(8),getWidth()-dp(3),y+dp(8),paint);
            paint.setColor(Color.WHITE);
            paint.setTextSize(dp(12));
            canvas.drawText(price(orderSymbol,values.get(values.size()-1)/tickScale(orderSymbol)),
                    right+dp(3),y+dp(4),paint);
        }
    }

    private void indicatorManager(LinearLayout body) {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16),0,dp(8),0);
        TextView title = label("MAIN CHART",16,BLUE);
        title.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
        header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        header.addView(iconAction("add_indicator","Add indicator",BLUE,
                () -> showPage(INDICATOR_CATALOG)),new LinearLayout.LayoutParams(dp(48),-1));
        body.addView(header,new LinearLayout.LayoutParams(-1,dp(50)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = column(Color.WHITE);
        scroll.addView(list);
        body.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        for (String name : selectedIndicators) indicatorRow(list,name,() ->
                openIndicatorParameters(name,INDICATOR_MANAGER));
    }

    private void indicatorCatalog(LinearLayout body) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout list = column(Color.WHITE);
        scroll.addView(list);
        body.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
        indicatorGroup(list,"TREND","Average Directional Movement Index","Bollinger Bands",
                "Envelopes","Ichimoku Kinko Hyo","Moving Average","Parabolic SAR","Standard Deviation");
        indicatorGroup(list,"OSCILLATORS","Average True Range","Bears Power","Bulls Power",
                "Commodity Channel Index","DeMarker","Force Index","MACD","Momentum",
                "Moving Average of Oscillator","Relative Strength Index","Relative Vigor Index",
                "Stochastic Oscillator","Williams' Percent Range");
        indicatorGroup(list,"VOLUMES","Accumulation/Distribution","Money Flow Index",
                "On Balance Volume","Volumes");
        indicatorGroup(list,"BILL WILLIAMS","Accelerator Oscillator","Alligator",
                "Awesome Oscillator","Fractals","Gator Oscillator","Market Facilitation Index");
    }

    private void indicatorGroup(LinearLayout list,String title,String... entries) {
        indicatorHeading(list,title);
        for (String name : entries) {
            indicatorRow(list,name,() -> openIndicatorParameters(name,INDICATOR_CATALOG));
        }
    }

    private void indicatorHeading(LinearLayout list,String title) {
        TextView heading = label(title,16,0xff444444);
        heading.setGravity(Gravity.BOTTOM);
        heading.setPadding(dp(16),0,0,dp(4));
        heading.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
        list.addView(heading,new LinearLayout.LayoutParams(-1,dp(44)));
        View rule = new View(this);
        rule.setBackgroundColor(0xffe1e1e1);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(-1,dp(2));
        lineParams.setMarginStart(dp(16));
        list.addView(rule,lineParams);
    }

    private void indicatorRow(LinearLayout list,String name,Runnable click) {
        TextView row = label(name,16,0xff555555);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(25),dp(8),dp(16),dp(8));
        row.setMinHeight(dp(48));
        row.setFocusable(true);
        row.setOnClickListener(view -> click.run());
        list.addView(row,new LinearLayout.LayoutParams(-1,-2));
    }

    private void deleteIndicators() {
        if (selectedIndicators.isEmpty()) return;
        String[] entries = selectedIndicators.toArray(new String[0]);
        boolean[] remove = new boolean[entries.length];
        new AlertDialog.Builder(this).setTitle("Delete indicators")
                .setMultiChoiceItems(entries,remove,(dialog,which,checked) -> remove[which] = checked)
                .setNegativeButton(android.R.string.cancel,null)
                .setPositiveButton("Delete",(dialog,which) -> {
                    for (int i=0;i<entries.length;i++) {
                        if (remove[i]) {
                            selectedIndicators.remove(entries[i]);
                            indicatorOptions.remove(entries[i]);
                        }
                    }
                    showPage(INDICATOR_MANAGER);
                }).show();
    }

    private void openIndicatorParameters(String name,int returnPage) {
        editingIndicator = name;
        parametersBackPage = returnPage;
        Bundle saved = indicatorOptions.getBundle(name);
        indicatorDraft = saved == null ? new Bundle() : new Bundle(saved);
        showPage(INDICATOR_PARAMETERS);
    }

    private void indicatorParameters(LinearLayout body) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout form = column(Color.WHITE);
        scroll.addView(form);
        body.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        indicatorHeading(form,"PARAMETERS");
        LinearLayout periodRow = parameterRow(form);
        parameterLabel(periodRow,"Period");
        indicatorPeriod = new EditText(this);
        indicatorPeriod.setTextSize(16);
        indicatorPeriod.setSingleLine();
        indicatorPeriod.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
        indicatorPeriod.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        indicatorPeriod.setText(indicatorDraft.getString("period","14"));
        periodRow.addView(indicatorPeriod,new LinearLayout.LayoutParams(dp(72),-1));
        indicatorPeriod.setContentDescription("Period");
        form.setFocusableInTouchMode(true);
        form.requestFocus();

        indicatorHeading(form,"STYLES");
        styleRow(form,"Style",0,0xff008692);
        if ("Average Directional Movement Index".equals(editingIndicator)) {
            styleRow(form,"+DI",1,0xff08bc06);
            styleRow(form,"-DI",2,0xffff9800);
        }
        indicatorHeading(form,"LEVEL");
        editParameterRow(form,"Levels: "+indicatorDraft.getString("levels",""),this::editIndicatorLevels);
        indicatorHeading(form,"TIME FRAMES:");
        editParameterRow(form,"Time frames:  "+indicatorDraft.getString("frames","All"),
                this::editIndicatorTimeframes);

        TextView reset = action("SET TO DEFAULT","Set to default",MUTED,() -> {
            indicatorDraft.clear();
            showPage(INDICATOR_PARAMETERS);
        });
        reset.setTextSize(13);
        body.addView(reset,new LinearLayout.LayoutParams(-1,dp(48)));
    }

    private LinearLayout parameterRow(LinearLayout form) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8),0,0,0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1,dp(48));
        params.setMargins(dp(16),0,dp(16),0);
        form.addView(row,params);
        View divider = new View(this);
        divider.setBackgroundColor(0xffcccccc);
        LinearLayout.LayoutParams rule = new LinearLayout.LayoutParams(-1,dp(1));
        rule.setMargins(dp(16),0,dp(16),0);
        form.addView(divider,rule);
        return row;
    }

    private void parameterLabel(LinearLayout row,String text) {
        TextView name = label(text,16,0xff777777);
        row.addView(name,new LinearLayout.LayoutParams(0,-2,1));
    }

    private void styleRow(LinearLayout form,String title,int index,int defaultColor) {
        LinearLayout row = parameterRow(form);
        parameterLabel(row,title);
        Spinner width = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,
                new String[]{"1 pixel","2 pixel","3 pixel","4 pixel","5 pixel"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        width.setAdapter(adapter);
        String widthKey = "width"+index;
        width.setSelection(indicatorDraft.getInt(widthKey,1));
        width.setContentDescription(title+" width");
        indicatorWidths[index] = width;
        row.addView(width,new LinearLayout.LayoutParams(dp(80),-1));
        String colorKey = "color"+index;
        View swatch = new View(this);
        swatch.setContentDescription(title+" color");
        setIndicatorSwatch(swatch,indicatorDraft.getInt(colorKey,defaultColor));
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(dp(30),dp(30));
        size.setMarginStart(dp(6));
        row.addView(swatch,size);
        swatch.setFocusable(true);
        swatch.setOnClickListener(view -> {
            String[] names = {"Teal","Green","Orange","Blue","Red","Black"};
            int[] colors = {0xff008692,0xff08bc06,0xffff9800,BLUE,PRICE_DOWN,Color.BLACK};
            new AlertDialog.Builder(this).setTitle(title+" color")
                    .setItems(names,(dialog,which) -> {
                        indicatorDraft.putInt(colorKey,colors[which]);
                        setIndicatorSwatch(swatch,colors[which]);
                    }).show();
        });
    }

    private void setIndicatorSwatch(View view,int color) {
        android.graphics.drawable.GradientDrawable circle = new android.graphics.drawable.GradientDrawable();
        circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        circle.setColor(color);
        view.setBackground(circle);
    }

    private void editParameterRow(LinearLayout form,String title,Runnable edit) {
        LinearLayout row = parameterRow(form);
        parameterLabel(row,title);
        row.addView(iconAction("edit",title,BLUE,edit),new LinearLayout.LayoutParams(dp(40),-1));
        row.setOnClickListener(view -> edit.run());
    }

    private void editIndicatorLevels() {
        EditText values = new EditText(this);
        values.setHint("Comma-separated levels");
        values.setText(indicatorDraft.getString("levels",""));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Levels")
                .setView(values).setNegativeButton(android.R.string.cancel,null)
                .setPositiveButton(android.R.string.ok,null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    String text = values.getText().toString().trim();
                    if (!text.isEmpty() && !text.matches("[+-]?[0-9]+(\\.[0-9]+)?(\\s*,\\s*[+-]?[0-9]+(\\.[0-9]+)?)*")) {
                        values.setError("Enter numbers separated by commas");
                        return;
                    }
                    captureIndicatorDraft();
                    indicatorDraft.putString("levels",text);
                    dialog.dismiss();
                    showPage(INDICATOR_PARAMETERS);
                }));
        dialog.show();
    }

    private void editIndicatorTimeframes() {
        String[] stored = indicatorDraft.getStringArray("selectedFrames");
        boolean[] checked = new boolean[CHART_PERIODS.length];
        for (int i=0;i<checked.length;i++) checked[i] = stored == null
                || java.util.Arrays.asList(stored).contains(CHART_PERIODS[i]);
        new AlertDialog.Builder(this).setTitle("Time frames")
                .setMultiChoiceItems(CHART_PERIODS,checked,(dialog,which,enabled) -> checked[which] = enabled)
                .setNegativeButton(android.R.string.cancel,null)
                .setPositiveButton(android.R.string.ok,(dialog,which) -> {
                    java.util.ArrayList<String> frames = new java.util.ArrayList<>();
                    for (int i=0;i<checked.length;i++) if (checked[i]) frames.add(CHART_PERIODS[i]);
                    if (frames.isEmpty()) {
                        Toast.makeText(this,"Select at least one time frame",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    captureIndicatorDraft();
                    indicatorDraft.putStringArray("selectedFrames",frames.toArray(new String[0]));
                    indicatorDraft.putString("frames",frames.size() == checked.length
                            ? "All" : android.text.TextUtils.join(", ",frames));
                    showPage(INDICATOR_PARAMETERS);
                }).show();
    }

    private void captureIndicatorDraft() {
        if (indicatorPeriod == null) return;
        indicatorDraft.putString("period",indicatorPeriod.getText().toString().trim());
        for (int i=0;i<indicatorWidths.length;i++) {
            if (indicatorWidths[i] != null) {
                indicatorDraft.putInt("width"+i,indicatorWidths[i].getSelectedItemPosition());
            }
        }
    }

    private void saveIndicatorParameters() {
        captureIndicatorDraft();
        try {
            if (Integer.parseInt(indicatorDraft.getString("period","")) < 1) throw new NumberFormatException();
        } catch (NumberFormatException invalid) {
            indicatorPeriod.setError("Enter a positive whole number");
            indicatorPeriod.requestFocus();
            return;
        }
        indicatorOptions.putBundle(editingIndicator,new Bundle(indicatorDraft));
        if (!selectedIndicators.contains(editingIndicator)) selectedIndicators.add(editingIndicator);
        showPage(INDICATOR_MANAGER);
    }

    private void charts(LinearLayout body) {
        syncChartWindow();
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout stack = column(Color.WHITE);
        scroll.addView(stack,new ScrollView.LayoutParams(-1,-2));
        body.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
        for (int i=0;i<chartWindows.size();i++) {
            int window = chartWindows.get(i);
            CandleChart pane = new CandleChart(window / CHART_PERIODS.length,
                    window % CHART_PERIODS.length,i);
            pane.setContentDescription("Practice candlestick chart for "+SYMBOLS[pane.selected]);
            chartPanes.add(pane);
            stack.addView(pane,new LinearLayout.LayoutParams(-1,dp(210),1f));
            if (i+1 < chartWindows.size()) windowDivider(stack);
        }
        activeChart = chartPanes.get(activeWindow);
        CandleChart selectedPane = activeChart;
        scroll.post(() -> scroll.scrollTo(0,selectedPane.getTop()));
        scheduleQuotes();
    }

    private void selectChartPane(CandleChart pane) {
        if (activeChart == pane) return;
        if (activeChart != null) {
            activeChart.menuOpen = false;
            activeChart.crosshair = false;
            activeChart.invalidate();
        }
        activeChart = pane;
        activeWindow = pane.windowIndex;
        symbol = pane.selected;
        chartPeriod = pane.timeframe;
        pane.updateCrosshairButton();
    }

    private View summaryDots() {
        return new View(this) {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override protected void onDraw(Canvas canvas) {
                paint.setColor(0xffdddddd);
                for (int x=dp(9);x<getWidth()-dp(6);x+=dp(8)) {
                    canvas.drawCircle(x,getHeight()*0.6f,dp(1),paint);
                }
            }
        };
    }

    private void history(LinearLayout body) {
        LinearLayout tabs = new LinearLayout(this);
        tabs.setBackgroundColor(BLUE);
        String[] labels = {"POSITIONS","ORDERS","DEALS"};
        for (int i=0;i<labels.length;i++) {
            final int index = i;
            LinearLayout cell = column(BLUE);
            TextView tab = action(labels[i],labels[i],Color.WHITE,() -> {
                historyTab = index;
                showPage(4);
            });
            tab.setTextSize(14);
            tab.setTypeface(Typeface.create("sans-serif-condensed",Typeface.NORMAL));
            tab.setSelected(i==historyTab);
            cell.addView(tab,new LinearLayout.LayoutParams(-1,0,1));
            View underline = new View(this);
            underline.setBackgroundColor(i==historyTab ? Color.WHITE : BLUE);
            cell.addView(underline,new LinearLayout.LayoutParams(-1,dp(2)));
            tabs.addView(cell,new LinearLayout.LayoutParams(0,dp(48),1));
        }
        body.addView(tabs);

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = column(Color.WHITE);
        scroll.addView(list);
        body.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        long start = historyStart(historyRange);
        long end = historyEnd();
        java.util.ArrayList<Bundle> visible = new java.util.ArrayList<>();
        for (Bundle event : historyEvents) {
            int symbol = event.getInt("symbol");
            long time = event.getLong("time");
            if (time >= start && time < end && (historySymbol < 0 || symbol == historySymbol)
                    && (historyTab != 0 || event.getBoolean("closed"))) visible.add(event);
        }
        visible.sort(this::compareHistory);
        boolean deposit = historySymbol < 0 && depositTime >= start && depositTime < end;
        if (historyTab == 1) {
            String count = Integer.toString(visible.size());
            historySummary(list,new String[]{"Filled:","Canceled:","Total:"},new String[]{count,"0",count});
        } else {
            double profit = 0;
            for (Bundle event : visible) profit += event.getDouble("profit");
            historySummary(list,new String[]{"Profit:","Deposit","Swap:","Commission:","Balance:"},
                    new String[]{tradeMoney(profit),tradeMoney(deposit ? 5000 : 0),
                            "0.00","0.00",tradeMoney(demoBalance)});
            if (deposit) historyEntry(list,"Balance","DEMO-INITIAL-DEPOSIT",depositTime,
                    tradeMoney(5000),BLUE);
        }
        for (Bundle event : visible) {
            int instrument = event.getInt("symbol");
            boolean closed = event.getBoolean("closed");
            boolean buy = event.getBoolean("buy") ^ (closed && historyTab != 0);
            String name = String.format(Locale.US,"%s, %s %.2f",SYMBOLS[instrument],
                    buy ? "buy" : "sell",event.getDouble("volume"));
            double value = event.getDouble("profit");
            String entry = price(instrument,event.getDouble("entry"));
            String exit = price(instrument,event.getDouble("exit"));
            String details = historyTab == 0 ? entry+" \u2192 "+exit
                    : (closed ? "Out" : "In")+"  "+(closed ? exit : entry);
            String amount = historyTab == 1 ? "Filled" : tradeMoney(value);
            historyEntry(list,name,details,event.getLong("time"),amount,value < 0 ? PRICE_DOWN : BLUE);
        }
    }

    private int compareHistory(Bundle left,Bundle right) {
        int result;
        switch (historySort) {
            case 0: result = SYMBOLS[left.getInt("symbol")].compareTo(SYMBOLS[right.getInt("symbol")]); break;
            case 1: result = Long.compare(left.getLong("id"),right.getLong("id")); break;
            case 2: result = Boolean.compare(left.getBoolean("buy"),right.getBoolean("buy")); break;
            case 3: result = Double.compare(left.getDouble("volume"),right.getDouble("volume")); break;
            case 5: result = Double.compare(left.getDouble("profit"),right.getDouble("profit")); break;
            default: result = Long.compare(left.getLong("time"),right.getLong("time")); break;
        }
        if (result == 0) result = Long.compare(left.getLong("id"),right.getLong("id"));
        return historyDescending ? -result : result;
    }

    private void historySummary(LinearLayout list,String[] names,String[] values) {
        LinearLayout summary = column(Color.WHITE);
        summary.setPadding(dp(14),dp(8),dp(14),dp(8));
        list.addView(summary);
        for (int i=0;i<names.length;i++) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            TextView name = label(names[i],14,0xff444444);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(name);
            row.addView(summaryDots(),new LinearLayout.LayoutParams(0,-1,1));
            TextView value = label(values[i],14,values[i].startsWith("-") ? PRICE_DOWN : BLUE);
            value.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(value);
            summary.addView(row,new LinearLayout.LayoutParams(-1,dp(26)));
        }
        windowDivider(list);
    }

    private void historyEntry(LinearLayout list,String title,String detail,long time,String amount,int tint) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14),dp(6),dp(14),dp(6));
        row.setMinimumHeight(dp(48));
        LinearLayout left = column(Color.WHITE);
        LinearLayout right = column(Color.WHITE);
        right.setGravity(Gravity.END);
        TextView name = label(title,15,0xff444444);
        name.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
        left.addView(name);
        left.addView(label(detail,12,MUTED));
        right.addView(label(historyDate(time,"yyyy.MM.dd HH:mm:ss"),12,MUTED));
        TextView result = label(amount,14,tint);
        result.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
        right.addView(result);
        row.addView(left,new LinearLayout.LayoutParams(0,-2,1));
        row.addView(right,new LinearLayout.LayoutParams(-2,-2));
        list.addView(row);
        windowDivider(list);
    }

    private void historyToolbarButton(String icon,String description,
                                      java.util.function.Consumer<View> click) {
        ImageView button = iconAction(icon,description,Color.WHITE,() -> {});
        button.setOnClickListener(view -> click.accept(view));
        toolbar.addView(button,new LinearLayout.LayoutParams(dp(48),-1));
    }

    private void historySymbols(View anchor) {
        java.util.TreeSet<Integer> used = new java.util.TreeSet<>();
        for (Bundle event : historyEvents) used.add(event.getInt("symbol"));
        String[] names = new String[used.size()+1];
        int[] ids = new int[names.length];
        names[0] = "All symbols";
        ids[0] = -1;
        int index = 1;
        for (int symbol : used) {
            ids[index] = symbol;
            names[index++] = SYMBOLS[symbol];
        }
        historyPopup(anchor,104,names,null,i -> {
            historySymbol = ids[i];
            showPage(4);
        });
    }

    private void historySorting(View anchor) {
        String[] fields = {"Symbol",historyTab == 1 ? "Order" : "Deal","Type","Volume","Time","Profit"};
        fields[historySort] += historyDescending ? "  \u2193" : "  \u2191";
        historyPopup(anchor,92,fields,null,i -> {
            historyDescending = i == historySort && !historyDescending;
            historySort = i;
            showPage(4);
        });
    }

    private void historyCalendar(View anchor) {
        String[] names = {"Today","Last week","Last month","Last 3 months","Custom period"};
        String[] dates = new String[names.length];
        long today = dayStart(System.currentTimeMillis());
        for (int i=0;i<names.length;i++) {
            if (historyRange == i) names[i] += "  \u2713";
            long start = historyStart(i);
            long end = i == 4 && customHistoryEnd != 0 ? customHistoryEnd : today;
            dates[i] = historyDate(start,"dd/MM/yy")+(i == 0 ? "" : " - "+historyDate(end,"dd/MM/yy"));
        }
        historyPopup(anchor,210,names,dates,i -> {
            if (i == 4) chooseHistoryDates();
            else { historyRange = i; showPage(4); }
        });
    }

    private void historyPopup(View anchor,int width,String[] names,String[] details,
                              java.util.function.IntConsumer choose) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout list = column(Color.WHITE);
        scroll.addView(list);
        PopupWindow popup = new PopupWindow(scroll,dp(width),-2,true);
        android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(3));
        popup.setBackgroundDrawable(background);
        popup.setOutsideTouchable(true);
        popup.setElevation(dp(5));
        for (int i=0;i<names.length;i++) {
            final int index = i;
            if (i>0) windowDivider(list);
            Runnable click = () -> { popup.dismiss(); choose.accept(index); };
            TextView row = action(details == null ? names[i] : names[i]+"\n"+details[i],
                    names[i],0xff444444,click);
            row.setTextSize(details == null ? 16 : 14);
            row.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
            row.setPadding(dp(13),0,dp(8),0);
            list.addView(row,new LinearLayout.LayoutParams(-1,dp(48)));
        }
        int offset = -anchor.getHeight()+dp(6);
        popup.setHeight(Math.min(dp(48)*names.length+dp(1)*(names.length-1),
                popup.getMaxAvailableHeight(anchor,offset)));
        popup.showAsDropDown(anchor,0,offset,Gravity.END);
    }

    private void chooseHistoryDates() {
        long now = System.currentTimeMillis();
        historyDatePicker("Start date",customHistoryStart == 0 ? now : customHistoryStart,start ->
                historyDatePicker("End date",Math.max(start,customHistoryEnd == 0 ? now : customHistoryEnd),end -> {
                    if (end < start) {
                        Toast.makeText(this,"End date must be on or after start",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    customHistoryStart = start;
                    customHistoryEnd = end;
                    historyRange = 4;
                    showPage(4);
                }));
    }

    private void historyDatePicker(String title,long time,java.util.function.LongConsumer choose) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.setTimeInMillis(time);
        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(this,(view,y,m,d) -> {
            java.util.Calendar selected = java.util.Calendar.getInstance();
            selected.clear();
            selected.set(y,m,d);
            choose.accept(selected.getTimeInMillis());
        },date.get(java.util.Calendar.YEAR),date.get(java.util.Calendar.MONTH),
                date.get(java.util.Calendar.DAY_OF_MONTH));
        dialog.setTitle(title);
        dialog.show();
    }

    private long historyStart(int range) {
        long today = dayStart(System.currentTimeMillis());
        if (range == 0) return today;
        if (range == 4) return customHistoryStart == 0 ? today : customHistoryStart;
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.setTimeInMillis(today);
        if (range == 1) date.add(java.util.Calendar.DAY_OF_MONTH,-7);
        else date.add(java.util.Calendar.MONTH,range == 2 ? -1 : -3);
        return date.getTimeInMillis();
    }

    private long historyEnd() {
        java.util.Calendar end = java.util.Calendar.getInstance();
        end.setTimeInMillis(historyRange == 4 && customHistoryEnd != 0
                ? customHistoryEnd : dayStart(System.currentTimeMillis()));
        end.add(java.util.Calendar.DAY_OF_MONTH,1);
        return end.getTimeInMillis();
    }

    private long dayStart(long time) {
        java.util.Calendar day = java.util.Calendar.getInstance();
        day.setTimeInMillis(time);
        day.set(java.util.Calendar.HOUR_OF_DAY,0);
        day.set(java.util.Calendar.MINUTE,0);
        day.set(java.util.Calendar.SECOND,0);
        day.set(java.util.Calendar.MILLISECOND,0);
        return day.getTimeInMillis();
    }

    private String historyDate(long time,String format) {
        return new java.text.SimpleDateFormat(format,Locale.US).format(new java.util.Date(time));
    }

    private void recordHistory(Bundle trade,boolean closed,double exit,double profit) {
        Bundle event = new Bundle(trade);
        event.putBoolean("closed",closed);
        event.putDouble("exit",exit);
        event.putDouble("profit",profit);
        event.putLong("time",System.currentTimeMillis());
        event.putLong("id",historyEvents.size()+1L);
        historyEvents.add(event);
    }

    private void messages(LinearLayout body) {
        if (messageSearch) {
            EditText search = new EditText(this);
            search.setSingleLine(true);
            search.setHint("Search");
            search.setText(messageQuery);
            search.setTextSize(16);
            search.setPadding(dp(8),0,dp(8),0);
            search.setBackground(messageSurface(0xfff1f2f3,4));
            search.setContentDescription("Search messages");
            search.addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence text,int start,int count,int after) {}
                public void onTextChanged(CharSequence text,int start,int before,int count) {
                    messageQuery = text.toString();
                }
                public void afterTextChanged(android.text.Editable text) {}
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1,dp(36));
            params.setMargins(dp(24),dp(16),dp(24),0);
            body.setFocusableInTouchMode(true);
            body.requestFocus();
            body.addView(search,params);
            return;
        }

        FrameLayout space = new FrameLayout(this);
        LinearLayout empty = column(Color.WHITE);
        empty.setGravity(Gravity.CENTER_HORIZONTAL);
        ImageView bubble = new ImageView(this);
        bubble.setImageDrawable(new LineIcon("empty_messages",0xffe4e4e4,120));
        empty.addView(bubble,new LinearLayout.LayoutParams(dp(120),dp(120)));
        TextView caption = label("No messages",14,0xffaaaaaa);
        caption.setPadding(0,dp(7),0,0);
        empty.addView(caption);
        space.addView(empty,new FrameLayout.LayoutParams(-2,-2,Gravity.CENTER));
        body.addView(space,new LinearLayout.LayoutParams(-1,0,1));

        windowDivider(body);
        LinearLayout actions = new LinearLayout(this);
        actions.setPadding(dp(8),dp(8),dp(8),dp(16));
        actions.setBackgroundColor(0xfff5f5f5);
        TextView register = action("REGISTER","Register",0xff444444,() ->
                showPage(REGISTRATION));
        TextView signIn = action("SIGN IN","Sign in",BLUE,() -> openAuthorize(5));
        for (TextView button : new TextView[]{register,signIn}) {
            button.setTextSize(14);
            button.setTypeface(Typeface.DEFAULT_BOLD);
            button.setElevation(dp(2));
        }
        register.setBackground(messageSurface(0xfffbc027,2));
        signIn.setBackground(messageSurface(0xfffafafa,2));
        signIn.setLetterSpacing(0.08f);
        actions.addView(register,new LinearLayout.LayoutParams(0,dp(36),2.75f));
        LinearLayout.LayoutParams signInParams = new LinearLayout.LayoutParams(0,dp(36),1);
        signInParams.setMarginStart(dp(10));
        actions.addView(signIn,signInParams);
        body.addView(actions);
    }

    private android.graphics.drawable.GradientDrawable messageSurface(int color,int radius) {
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        return shape;
    }

    private void showMessageId() {
        String demoId = "76992676";
        AlertDialog dialog = new AlertDialog.Builder(this,
                androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("MetaQuotes ID")
                .setMessage("My ID: "+demoId+"\n\nThis sample ID previews the notify service screen.")
                .setNeutralButton("Copy",(window,which) -> {
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    if (clipboard != null) clipboard.setPrimaryClip(
                            android.content.ClipData.newPlainText("Demo MetaQuotes ID",demoId));
                })
                .setPositiveButton(android.R.string.ok,null).show();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(BLUE);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(BLUE);
    }

    private void openAuthorize(int origin) {
        authorizeBack = origin;
        showPage(AUTHORIZE);
    }

    private void registrationScreen(LinearLayout body,boolean authorize) {
        body.setBackgroundColor(REGISTRATION_BLUE);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundResource(R.drawable.registration_background);
        body.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout form = column(Color.TRANSPARENT);
        form.setGravity(Gravity.CENTER_HORIZONTAL);
        form.setFocusableInTouchMode(true);
        scroll.addView(form,new ScrollView.LayoutParams(-1,-2));

        TextView logo = label("",36,Color.WHITE);
        logo.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD_ITALIC));
        logo.setText(registrationAccent("MQL5","5"));
        logo.setGravity(Gravity.CENTER);
        form.addView(logo,new LinearLayout.LayoutParams(-1,dp(59)));
        TextView invite = label("Join trader's community\nwww.mql5.com",25,Color.WHITE);
        invite.setGravity(Gravity.CENTER);
        registrationItem(form,invite,12,12,-2);
        EditText login = registrationField("Login",android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        registrationItem(form,login,44,40,52);
        TextView loginHelp = label("Latin characters and digits without spaces",13,Color.WHITE);
        loginHelp.setGravity(Gravity.CENTER);
        registrationItem(form,loginHelp,12,7,-2);

        EditText identityField = registrationField(authorize ? "Password" : "E-Mail",
                android.text.InputType.TYPE_CLASS_TEXT | (authorize
                        ? android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                        : android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS));
        if (authorize) {
            identityField.setSaveEnabled(false);
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                identityField.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
                login.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            }
            registrationItem(form,passwordWithToggle(identityField),44,18,52);
        } else {
            registrationItem(form,identityField,44,18,52);
        }
        TextView help = label(authorize ? "Authorize and chat with traders right here and now"
                        : "The password for your new MQL5 account will be sent to the specified email address",
                13,Color.WHITE);
        help.setGravity(Gravity.CENTER);
        registrationItem(form,help,authorize ? 24 : 44,authorize ? 16 : 8,-2);

        String submitLabel = authorize ? "Sign In" : "Get an Account";
        TextView submit = action(submitLabel,submitLabel,0xff444444,() -> {
            String name = login.getText().toString();
            if (!name.matches("[A-Za-z0-9]+")) {
                login.setError("Use Latin letters and digits without spaces");
                return;
            }
            if (authorize) {
                if (identityField.length() == 0) {
                    identityField.setError("Enter a sample password");
                    return;
                }
                identityField.setText("");
                message("Authorize","Demo form validated. This screen uses local test input only.");
            } else {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(identityField.getText().toString().trim()).matches()) {
                    identityField.setError("Enter a valid email address");
                    return;
                }
                message("Account registration","Demo form validated. No account was created and no email was sent.");
            }
        });
        submit.setTextSize(15);
        submit.setTypeface(Typeface.DEFAULT_BOLD);
        submit.setBackground(messageSurface(0xfffbc027,2));
        submit.setElevation(dp(2));
        registrationItem(form,submit,41,17,40);
        TextView or = label("or",15,Color.WHITE);
        or.setGravity(Gravity.CENTER);
        registrationItem(form,or,42,14,-2);

        FrameLayout google = new FrameLayout(this);
        google.setBackground(messageSurface(Color.WHITE,2));
        google.setElevation(dp(2));
        TextView googleText = label("Sign in with Google",15,0xff666666);
        googleText.setTypeface(Typeface.DEFAULT_BOLD);
        googleText.setGravity(Gravity.CENTER);
        googleText.setPadding(dp(25),0,0,0);
        google.addView(googleText,new FrameLayout.LayoutParams(-1,-1));
        ImageView googleIcon = new ImageView(this);
        googleIcon.setImageDrawable(new LineIcon("registration_google",BLUE,20));
        FrameLayout.LayoutParams googleIconParams = new FrameLayout.LayoutParams(dp(20),dp(20),
                Gravity.START|Gravity.CENTER_VERTICAL);
        googleIconParams.setMarginStart(dp(10));
        google.addView(googleIcon,googleIconParams);
        google.setContentDescription("Sign in with Google");
        google.setFocusable(true);
        google.setOnClickListener(view -> message("Sign in with Google","Google sign-in is unavailable in this demo."));
        registrationItem(form,google,42,15,40);

        TextView existing = action("",authorize ? "Register an account" : "Sign in with an existing account",
                Color.WHITE,() -> {
                    if (authorize) showPage(REGISTRATION);
                    else openAuthorize(REGISTRATION);
                });
        existing.setTextSize(14);
        existing.setText(authorize ? registrationAccent("If you don't have an account, please register","register")
                : registrationAccent("If you have an account, please login","login"));
        registrationItem(form,existing,12,20,32);
        View space = new View(this);
        form.addView(space,new LinearLayout.LayoutParams(-1,dp(22),1f));
        TextView footer = label("",14,Color.WHITE);
        footer.setGravity(Gravity.CENTER);
        footer.setText(registrationAccent("Visit mql5.com to access all community services.","mql5.com"));
        registrationItem(form,footer,12,4,54);
    }

    private View passwordWithToggle(EditText password) {
        FrameLayout row = new FrameLayout(this);
        password.setPadding(dp(12),0,dp(48),0);
        row.addView(password,new FrameLayout.LayoutParams(-1,-1));
        ImageView eye = iconAction("password_eye","Show password",0xff365f9b,() -> {});
        eye.setOnClickListener(view -> {
            boolean visible = password.getTransformationMethod() == null;
            password.setTransformationMethod(visible
                    ? android.text.method.PasswordTransformationMethod.getInstance() : null);
            password.setSelection(password.length());
            eye.setContentDescription(visible ? "Show password" : "Hide password");
            eye.setImageDrawable(new LineIcon(visible ? "password_eye" : "password_eye_hidden",0xff365f9b,22));
        });
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(48),-1,Gravity.END);
        params.setMarginEnd(dp(8));
        row.addView(eye,params);
        return row;
    }

    private void registrationItem(LinearLayout form,View view,int inset,int top,int height) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1,height < 0 ? -2 : dp(height));
        params.setMargins(dp(inset),dp(top),dp(inset),0);
        form.addView(view,params);
    }

    private EditText registrationField(String hint,int inputType) {
        EditText field = new EditText(this);
        field.setSingleLine(true);
        field.setInputType(inputType);
        field.setTextSize(15);
        field.setTextColor(INK);
        field.setHintTextColor(INK);
        field.setHint(hint);
        field.setContentDescription(hint);
        field.setPadding(dp(12),0,dp(12),0);
        field.setBackground(messageSurface(Color.WHITE,3));
        return field;
    }

    private CharSequence registrationAccent(String text,String accent) {
        SpannableString styled = new SpannableString(text);
        int start = text.lastIndexOf(accent);
        styled.setSpan(new android.text.style.ForegroundColorSpan(0xffffcf28),
                start,start+accent.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return styled;
    }

    private void sidebarEmpty(LinearLayout body,String icon,String title) {
        FrameLayout space = new FrameLayout(this);
        LinearLayout empty = column(Color.WHITE);
        empty.setGravity(Gravity.CENTER_HORIZONTAL);
        ImageView image = new ImageView(this);
        image.setImageDrawable(new LineIcon(icon,0xffb6b6b6,108));
        empty.addView(image,new LinearLayout.LayoutParams(dp(108),dp(108)));
        TextView caption = label(title,14,0xffadadad);
        caption.setGravity(Gravity.CENTER);
        caption.setPadding(0,dp(23),0,0);
        empty.addView(caption);
        space.addView(empty,new FrameLayout.LayoutParams(-2,-2,Gravity.CENTER));
        body.addView(space,new LinearLayout.LayoutParams(-1,0,1));
    }

    private void composeDemoMail() {
        LinearLayout form = column(Color.WHITE);
        form.setPadding(dp(20),0,dp(20),0);
        EditText subject = new EditText(this);
        subject.setHint("Subject");
        subject.setSingleLine(true);
        form.addView(subject);
        EditText body = new EditText(this);
        body.setHint("Message");
        body.setMinLines(3);
        form.addView(body);
        new AlertDialog.Builder(this).setTitle("New mail").setView(form)
                .setNegativeButton(android.R.string.cancel,null).show();
    }

    private void openDrawer() {
        if (drawer != null) return;
        drawer = new FrameLayout(this);
        drawer.setBackgroundColor(0x99000000);
        drawer.setOnClickListener(view -> closeDrawer());
        root.addView(drawer,new FrameLayout.LayoutParams(-1,-1));
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.WHITE);
        scroll.setOnClickListener(view -> {});
        int width = Math.min(dp(320),Math.round(root.getWidth()*0.88f));
        drawer.addView(scroll,new FrameLayout.LayoutParams(width,-1,Gravity.START));
        LinearLayout menu = column(Color.WHITE);
        scroll.addView(menu);
        LinearLayout header = new LinearLayout(this);
        header.setPadding(dp(16),dp(13),dp(12),dp(12));
        ImageView logo = new ImageView(this);
        logo.setImageDrawable(new LineIcon("brand",0xff158831,34));
        header.addView(logo,new LinearLayout.LayoutParams(dp(34),dp(34)));
        LinearLayout details = column(Color.WHITE);
        details.setPadding(dp(17),0,0,0);
        header.addView(details,new LinearLayout.LayoutParams(0,-2,1));
        details.addView(label(ACCOUNT_NAME,14,INK));
        details.addView(label(ACCOUNT_ID + " \u00b7 " + ACCOUNT_SERVER,12,0xff898989));
        TextView manage = action("Manage accounts","Manage accounts",BLUE,() -> showPage(0));
        manage.setTextSize(14);
        manage.setGravity(Gravity.CENTER_VERTICAL);
        details.addView(manage,new LinearLayout.LayoutParams(-1,dp(38)));
        menu.addView(header);
        View rule = new View(this);
        rule.setBackgroundColor(0xffcccccc);
        menu.addView(rule,new LinearLayout.LayoutParams(-1,dp(1)));
        drawerRow(menu,"trade","Trade","",() -> showPage(3));
        drawerRow(menu,"news","News","",() -> showPage(NEWS));
        drawerRow(menu,"mail","Mailbox","",() -> showPage(MAILBOX));
        drawerRow(menu,"journal","Journal","",() -> drawerInfo("Journal"));
        drawerRow(menu,"settings","Settings","",() -> drawerInfo("Settings"));
        drawerRow(menu,"calendar","Economic calendar","Ads",() -> drawerInfo("Economic calendar"));
        drawerRow(menu,"community","Traders Community","",() -> drawerInfo("Traders Community"));
        drawerRow(menu,"about","About","",() -> drawerInfo("About"));
    }

    private void drawerInfo(String title) {
        closeDrawer();
        message(title,"Local practice mode");
    }

    private void drawerRow(LinearLayout menu,String icon,String title,String badge,Runnable operation) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14),0,dp(14),0);
        ImageView image = new ImageView(this);
        image.setImageDrawable(new LineIcon(icon,0xff333333,20));
        row.addView(image,new LinearLayout.LayoutParams(dp(20),dp(20)));
        TextView text = label(title,14,Color.BLACK);
        text.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                badge.equals("Ads") ? -2 : 0,-2,badge.equals("Ads") ? 0 : 1);
        titleParams.setMarginStart(dp(22));
        row.addView(text,titleParams);
        if (!badge.isEmpty()) {
            boolean ads = badge.equals("Ads");
            TextView tag = label(badge,10,ads ? BLUE : Color.WHITE);
            tag.setGravity(Gravity.CENTER);
            android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
            shape.setCornerRadius(dp(10));
            shape.setColor(ads ? Color.WHITE : 0xffff9500);
            if (ads) shape.setStroke(dp(1),BLUE);
            tag.setBackground(shape);
            LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(dp(ads ? 30 : 16),dp(16));
            tagParams.setMarginStart(dp(8));
            row.addView(tag,tagParams);
        }
        row.setContentDescription(badge.isEmpty() ? title : title+" "+badge);
        row.setFocusable(true);
        row.setOnClickListener(view -> operation.run());
        menu.addView(row,new LinearLayout.LayoutParams(-1,dp(40)));
    }

    private void closeDrawer() {
        if (drawer != null) {
            root.removeView(drawer);
            drawer = null;
        }
    }

    private void chooseSymbol() {
        String[] quotes = java.util.Arrays.copyOf(SYMBOLS,QUOTE_SYMBOL_COUNT);
        new AlertDialog.Builder(this).setTitle("Choose symbol")
                .setItems(quotes,(dialog,which) -> quoteActions(which)).show();
    }

    private void showChartWindows(View anchor) {
        syncChartWindow();
        LinearLayout panel = column(Color.WHITE);
        TextView title = label("Charts",18,0xff444444);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(24),0,0,0);
        panel.addView(title,new LinearLayout.LayoutParams(-1,dp(48)));

        PopupWindow popup = new PopupWindow(panel,Math.min(dp(280),root.getWidth()-dp(32)),-2,true);
        android.graphics.drawable.GradientDrawable surface = new android.graphics.drawable.GradientDrawable();
        surface.setColor(Color.WHITE);
        surface.setCornerRadius(dp(3));
        popup.setBackgroundDrawable(surface);
        popup.setElevation(dp(8));
        popup.setOutsideTouchable(true);

        windowDivider(panel);
        LinearLayout newRow = new LinearLayout(this);
        newRow.setGravity(Gravity.CENTER_VERTICAL);
        Runnable addWindow = () -> {
            popup.dismiss();
            new AlertDialog.Builder(this).setTitle("New Window")
                    .setItems(SYMBOLS,(dialog,which) -> {
                        chartWindows.add(which * CHART_PERIODS.length + chartPeriod);
                        openChartWindow(chartWindows.size()-1);
                    }).show();
        };
        newRow.addView(iconAction("plus","New Window",0xff444444,addWindow),
                new LinearLayout.LayoutParams(dp(55),-1));
        TextView newLabel = label("New Window",16,0xff444444);
        newRow.addView(newLabel);
        newRow.setOnClickListener(view -> addWindow.run());
        panel.addView(newRow,new LinearLayout.LayoutParams(-1,dp(48)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout rows = column(Color.WHITE);
        scroll.addView(rows);
        TextView remove = action("REMOVE","Remove",Color.WHITE,() -> {});
        remove.setTextSize(14);
        remove.setTypeface(Typeface.DEFAULT_BOLD);
        remove.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
        remove.setPadding(0,0,dp(16),0);
        remove.setBackgroundColor(BLUE);
        remove.setEnabled(false);
        remove.setAlpha(0.5f);
        boolean[] checked = new boolean[chartWindows.size()];
        for (int i=0;i<chartWindows.size();i++) {
            final int index = i;
            int chart = chartWindows.get(i);
            String name = SYMBOLS[chart / CHART_PERIODS.length]+", "
                    + CHART_PERIODS[chart % CHART_PERIODS.length];
            windowDivider(rows);
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(20),0,dp(7),0);
            CheckBox box = new CheckBox(this);
            box.setContentDescription("Select "+name+" for removal");
            box.setOnCheckedChangeListener((button,isChecked) -> {
                checked[index] = isChecked;
                int count = 0;
                for (boolean selected : checked) if (selected) count++;
                boolean canRemove = count > 0 && count < checked.length;
                remove.setEnabled(canRemove);
                remove.setAlpha(canRemove ? 1f : 0.5f);
            });
            row.addView(box,new LinearLayout.LayoutParams(dp(48),-1));
            TextView caption = label(name,16,0xff444444);
            caption.setTypeface(Typeface.create("sans-serif-condensed",Typeface.NORMAL));
            caption.setGravity(Gravity.CENTER_VERTICAL);
            caption.setOnClickListener(view -> {
                popup.dismiss();
                openChartWindow(index);
            });
            row.addView(caption,new LinearLayout.LayoutParams(0,-1,1));
            row.addView(iconAction("menu","Reorder "+name,0xff777777,() ->
                    reorderChartWindow(anchor,popup,index)),new LinearLayout.LayoutParams(dp(36),-1));
            rows.addView(row,new LinearLayout.LayoutParams(-1,dp(48)));
        }
        panel.addView(scroll,new LinearLayout.LayoutParams(-1,dp(49)*Math.min(5,checked.length)));
        panel.addView(remove,new LinearLayout.LayoutParams(-1,dp(52)));
        remove.setOnClickListener(view -> {
            int previousActive = activeWindow;
            for (int i=checked.length-1;i>=0;i--) {
                if (!checked[i]) continue;
                chartWindows.remove(i);
                if (i < previousActive) activeWindow--;
            }
            popup.dismiss();
            openChartWindow(Math.min(activeWindow,chartWindows.size()-1));
        });
        popup.showAsDropDown(anchor,0,-anchor.getHeight()+dp(14),Gravity.END);
    }

    private void reorderChartWindow(View anchor,PopupWindow popup,int index) {
        if (chartWindows.size() < 2) return;
        String[] actions = index == 0 ? new String[]{"Move down"}
                : index == chartWindows.size()-1 ? new String[]{"Move up"}
                  : new String[]{"Move up","Move down"};
        new AlertDialog.Builder(this).setTitle("Reorder chart")
                .setItems(actions,(dialog,which) -> {
                    int target = index + (actions[which].equals("Move up") ? -1 : 1);
                    java.util.Collections.swap(chartWindows,index,target);
                    if (activeWindow == index) activeWindow = target;
                    else if (activeWindow == target) activeWindow = index;
                    popup.dismiss();
                    showChartWindows(anchor);
                }).show();
    }

    private void windowDivider(LinearLayout panel) {
        View divider = new View(this);
        divider.setBackgroundColor(0xffdddddd);
        panel.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));
    }

    private void syncChartWindow() {
        int current = symbol * CHART_PERIODS.length + chartPeriod;
        if (chartWindows.isEmpty()) chartWindows.add(current);
        else chartWindows.set(activeWindow,current);
    }

    private void openChartWindow(int index) {
        activeWindow = index;
        int chart = chartWindows.get(index);
        symbol = chart / CHART_PERIODS.length;
        chartPeriod = chart % CHART_PERIODS.length;
        showPage(2);
    }

    private void showChartSymbols(View anchor) {
        ListView list = new ListView(this);
        list.setDivider(new android.graphics.drawable.ColorDrawable(0xffdddddd));
        list.setDividerHeight(dp(1));
        list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,SYMBOLS) {
            @Override public View getView(int position,View recycled,android.view.ViewGroup parent) {
                TextView row = recycled == null ? label("",16,Color.BLACK) : (TextView) recycled;
                row.setText(getItem(position));
                row.setTypeface(Typeface.create("sans-serif-condensed",Typeface.NORMAL));
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setSingleLine(true);
                row.setPadding(dp(16),0,dp(10),0);
                row.setLayoutParams(new AbsListView.LayoutParams(-1,dp(48)));
                return row;
            }
        });
        PopupWindow popup = new PopupWindow(list,dp(100),-2,true);
        android.graphics.drawable.GradientDrawable surface = new android.graphics.drawable.GradientDrawable();
        surface.setColor(Color.WHITE);
        surface.setCornerRadius(dp(3));
        popup.setBackgroundDrawable(surface);
        popup.setElevation(dp(5));
        popup.setOutsideTouchable(true);
        int topOffset = -anchor.getHeight()+dp(6);
        int fullHeight = dp(48)*SYMBOLS.length + dp(1)*(SYMBOLS.length-1);
        popup.setHeight(Math.min(fullHeight,popup.getMaxAvailableHeight(anchor,topOffset)));
        list.setOnItemClickListener((parent,view,position,id) -> {
            popup.dismiss();
            symbol = position;
            showPage(2);
        });
        popup.showAsDropDown(anchor,0,topOffset,Gravity.END);
    }

    private void message(String title,String text) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(text).setPositiveButton("OK",null).show();
    }

    private void metrics(LinearLayout body,String[] labels,String[] values) {
        for (int i=0;i<labels.length;i++) {
            LinearLayout row = new LinearLayout(this);
            row.setPadding(dp(12),dp(4),dp(12),dp(4));
            row.addView(label(labels[i],14,INK),new LinearLayout.LayoutParams(0,-2,1));
            row.addView(label(values[i],14,BLUE));
            body.addView(row);
        }
    }

    private void toolbarAction(String icon,String description,Runnable operation) {
        toolbar.addView(iconAction(icon,description,Color.WHITE,operation),new LinearLayout.LayoutParams(dp(48),-1));
    }

    private ImageView iconAction(String icon,String description,int color,Runnable operation) {
        ImageView view = new ImageView(this);
        view.setScaleType(ImageView.ScaleType.CENTER);
        view.setImageDrawable(new LineIcon(icon,color,22));
        view.setContentDescription(description);
        view.setFocusable(true);
        view.setOnClickListener(v -> operation.run());
        return view;
    }

    private void sheetRow(LinearLayout parent,String title,Runnable operation) {
        TextView row = action(title,title,INK,operation);
        row.setTextSize(16);
        row.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16),0,dp(16),0);
        parent.addView(row,new LinearLayout.LayoutParams(-1,dp(48)));
    }

    private TextView action(String text,String description,int color,Runnable operation) {
        TextView view = label(text,20,color);
        view.setGravity(Gravity.CENTER);
        view.setContentDescription(description);
        view.setFocusable(true);
        view.setOnClickListener(v -> operation.run());
        return view;
    }

    private void centered(LinearLayout parent,String value,int size,int color) {
        TextView text = label(value,size,color);
        text.setGravity(Gravity.CENTER);
        text.setPadding(0,dp(5),0,dp(5));
        parent.addView(text,new LinearLayout.LayoutParams(-1,-2));
    }

    private TextView label(String value,int size,int color) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        return text;
    }

    private LinearLayout column(int color) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(color);
        return layout;
    }

    private String price(int selected,double amount) {
        int decimals = selected >= 7 ? 2 : selected == 5 ? 3 : 5;
        return String.format(Locale.US,"%."+decimals+"f",amount);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private android.graphics.Bitmap chartSymbolBitmap;

    private android.graphics.Bitmap chartSymbolBitmap() {
        if (chartSymbolBitmap == null) {
            // White icon silhouette isolated from the supplied toolbar reference.
            String png = "iVBORw0KGgoAAAANSUhEUgAAADEAAAAqCAYAAAAajbNEAAAGFklEQVR42s2Z2W9VVRTGf/d2VOlEC8pQsUINg0WmGi0SVMA4"
                    + "o8YhMcYHY2J88h/w1Wd9Mr6YqHF4UlEjiAGjiDIIBaxlKAgVZJBSKRTocNteX34n2Tne9uK9vdSVnJze273P2Wvvb63vW+sm"
                    + "0uk0BbQSIOE1UKiXFP/H8dXAIqAI2AecG2PsFGAJUA9cBLYCpybSiXKgEWgBmoCjQEeWOXcAL+jI70AKWAekJ8KJcmAV8Dhw"
                    + "F9ANbAFOjDGnDJjtvBqfUQeUFgJW2Zy4DngVWA00A33AF8DOUcYXAcPuerUxUQbMEFZVwNnxdiI5xv8mAQ8Cz+tEubDYM8op"
                    + "zADuBeYB1+tMmUFdBswRipXX8iRagFfEdAL4G/gO+NkFhtYM3AfMB74HrgidsmDMGj/vAg4AbW5KwZyYBTykIwm/2wtsA/6K"
                    + "jV0NPOC9BvjDTFTkFVkdsBZYDhzW2Q06NTgeTixyAYeE2MPA/UCF/08Bv7p78QB+ThjNAi4ZB0PASMyJKGZuBKYC04yTAaA1"
                    + "n6xVLH4fBZZ6zMPu7MJgXA9wPBaURcA9wCMuKCK3weDv0Sxh9qoCjjinLR8nbgCme8wr3cHaDGMr3bnjAeTWBg5g3JwIHEll"
                    + "caYOeNbNOQz055qduoTRZSFVOwr73mneb3Jh0w3oyE4DP5i9ujLgvA3YBJyMfd9oQqjNNyZagWPudNEoY9cCywzaD+SQ6BRG"
                    + "gM+At4RH0lMIE8c64HNggbv/WBBXVabwvJzoBNqNi7HyeJVXmXMjnrmoLjoVOJWIze0CfvM+2ROtB3qBP73n5cQJOaDRfF8a"
                    + "G5cCzsgRG4Bf1EbR/CGvoljwxjNUygV/IxwXmfW+zYfJw+PeCjTIrFNcxLC73ApsFPMHTYdzXBTG0kzgNmB3kL1GAikSSZAB"
                    + "xeNHarDDXuNCdl3ucqXYH1TsbTBeDssDkV1Szd7sQpcrzS+ZwUqD3B/pqWQs0NsYB4sz9gHgHcmoX37oziAzcEdPB58jedKp"
                    + "I8WeRAStfgXkNaknznhls261TzoI4kazz3a/SwbvKStUZZfMY+4p4KeA/EJSLI6dXkK5Uf1/cwKz1BuSWI/q9XJwjwvFZ4Cb"
                    + "JrrGzgSpjeL9tLVEvzxQGsREJNenmTTWAzuuErZZLTGO3Y75SpNzptAngdctruKcsxf40BPcP9EnEdp+A31IThgeRV6XeCqT"
                    + "FJFv51scFY8zPAcCXijNMnae5HoB+FjNlckqTAoV8tGFODkWUxgrDbJSZL0Gf32sk/IycB5414QQLr7JU1ts+sbq8v2QKHN1"
                    + "ojqQJ0cyyIaEC4qenwa+CkTmKoUkOrUK+NFYqVaXrbRF1BKMHfS9fW5KZ65O1Ad9qCbgE68DseANxV9CZ98zK5VbAkfWANxq"
                    + "fb7YDsvTweLDE661C7NXjhrJhSemAi9aljaYhVpi0CkycMPnp20y7LZJELZAa6wwm4HXdLBqjN7WMmE4NVeyqxSvpT7wduBu"
                    + "YZIMpHkyxhO9BnymzkavarlHsXk1/NEAzM0VTiO+MLSnZOjJyut0kK1KJMCU+mmmjbaqmPBsU8qU+vkJYZPJ0jY45gI/5uLE"
                    + "aWvyFUEToMYYKTfoDgmvdACBqEO4wJOL5p61Lj8a1DUdFk0tsUqzz5Kh0s1ckutJHHG3m8VmZLfYcOsIytTigOBWWzQtNIgj"
                    + "22HRFdr5DPI/bWW5y+dV2V0ZzhVO2y0x61x8WIPXBzxRHKs3mmItnKPAlxka1PXyQnlMEXxqKdtvKk7mwxOddjeuAC/ZCBvw"
                    + "4YM6WjJKz6lP1t1nXb9BIRnXYQuDzUi5cVsCiXJyPBh7TyC3l5q/K8R4KsOz+20SnNWBbcBm/v3rUYWwawgcaNPh9kJopw4F"
                    + "XK21dq0pcshFDweEt93mQHuWNLrCbFdhHOwD3pTxCyYAL3idMGt0C6M+r0ned1oJHhujXTnbRnajc9qV7FvH6kuNpwBMBdhO"
                    + "ySW9xsdZT61jlKZDpMfWqJd6DODNwNem1WsmxeNQW29GOmgmGh5j/BxlRDRvk/oo628XhXRit5iuNZ+3Zhl/3oX3y0UXJ6I8"
                    + "zWTlwU6OFOol/wBFZKeyQvftUgAAAABJRU5ErkJggg==";
            byte[] bytes = android.util.Base64.decode(png,android.util.Base64.DEFAULT);
            chartSymbolBitmap = android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }
        return chartSymbolBitmap;
    }

    private final class LineIcon extends Drawable {
        private final String name;
        private final int color;
        private final int size;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        LineIcon(String name,int color,int size) {
            this.name = name;
            this.color = color;
            this.size = dp(size);
        }

        @Override public int getIntrinsicWidth() { return size; }
        @Override public int getIntrinsicHeight() { return size; }
        @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
        @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }

        @Override public void draw(Canvas canvas) {
            canvas.save();
            canvas.translate(getBounds().left,getBounds().top);
            canvas.scale(getBounds().width()/24f,getBounds().height()/24f);
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.7f);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            switch (name) {
                case "back":
                    line(canvas,11,5,4,12,11,19); line(canvas,4,12,21,12); break;
                case "more":
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(12,5,1.65f,paint);
                    canvas.drawCircle(12,12,1.65f,paint);
                    canvas.drawCircle(12,19,1.65f,paint); break;
                case "certificate":
                    line(canvas,9,17,3,17,3,4,21,4,21,11);
                    line(canvas,6,8,18,8); line(canvas,6,11,11,11);
                    canvas.drawCircle(16,15,4,paint);
                    line(canvas,13,18,12,22,16,20,20,22,19,18); break;
                case "menu":
                    line(canvas,3,6,21,6); line(canvas,3,12,21,12); line(canvas,3,18,21,18); break;
                case "plus":
                    line(canvas,12,3,12,21); line(canvas,3,12,21,12); break;
                case "edit":
                    paint.setStrokeWidth(1.8f);
                    paint.setStrokeCap(Paint.Cap.BUTT);
                    line(canvas,4,16,15,5,19,9,8,20,3,21,4,16);
                    line(canvas,4,16,8,20);
                    line(canvas,17,3,21,7);
                    break;
                case "crosshair_active":
                    paint.setStrokeWidth(2.1f);
                    line(canvas,2,12,22,12); line(canvas,12,2,12,22);
                    line(canvas,6,8,2,12,6,16); line(canvas,18,8,22,12,18,16);
                    line(canvas,8,6,12,2,16,6); line(canvas,8,18,12,22,16,18);
                    break;
                case "crosshair":
                    line(canvas,12,2,12,8); line(canvas,12,16,12,22);
                    line(canvas,2,12,8,12); line(canvas,16,12,22,12);
                    canvas.drawCircle(12,12,1,paint); break;
                case "empty_news":
                    paint.setStrokeWidth(2.1f);
                    canvas.drawRoundRect(6.5f,3,22,21,2,2,paint);
                    Path fold = new Path();
                    fold.moveTo(2,12);
                    fold.lineTo(2,18.5f);
                    fold.quadTo(2,21,4.25f,21);
                    fold.quadTo(6.5f,21,6.5f,18.5f);
                    canvas.drawPath(fold,paint);
                    line(canvas,12,10,16.5f,10);
                    line(canvas,12,14.5f,16.5f,14.5f);
                    break;
                case "compose_mail":
                    line(canvas,12,4,3,4,3,22,20,22,20,13);
                    line(canvas,8,14,18,3,22,7,12,17,7,18,8,14);
                    line(canvas,16,5,20,9); break;
                case "trash":
                    line(canvas,5,7,19,7); line(canvas,9,7,9,4,15,4,15,7);
                    line(canvas,6,10,7,21,17,21,18,10); break;
                case "add_indicator":
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTextSize(22);
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    canvas.drawText("\u0192",1,17,paint);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(17,17,5.2f,paint);
                    line(canvas,14.5f,17,19.5f,17); line(canvas,17,14.5f,17,19.5f); break;
                case "indicator":
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTextSize(21);
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    canvas.drawText("\u0192",5,20,paint);
                    break;
                case "symbols":
                    android.graphics.Bitmap icon = chartSymbolBitmap();
                    paint.setFilterBitmap(true);
                    float height = 24f * icon.getHeight() / icon.getWidth();
                    float top = (24f - height) / 2;
                    canvas.drawBitmap(icon,null,new android.graphics.RectF(0,top,24,top+height),paint);
                    break;
                case "windows":
                    canvas.drawRect(3,8,17,21,paint); line(canvas,7,4,21,4,21,17); break;
                case "spread":
                    line(canvas,3,4,3,10,21,10,21,4);
                    line(canvas,3,20,3,14,21,14,21,20); break;
                case "quotes":
                    paint.setStrokeWidth(2.4f);
                    line(canvas,4,9,8,5,12,9); line(canvas,8,5,8,15);
                    line(canvas,12,15,16,19,20,15); line(canvas,16,19,16,9); break;
                case "chart":
                    paint.setStrokeWidth(1.9f);
                    line(canvas,7,4,7,8); line(canvas,7,17,7,21); canvas.drawRect(4.5f,8,9.5f,17,paint);
                    line(canvas,17,2,17,6); line(canvas,17,16,17,22); canvas.drawRect(14.5f,6,19.5f,16,paint); break;
                case "trade":
                    paint.setStrokeWidth(2.3f);
                    line(canvas,4,18,9,11,14,14,20,5);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(4,18,2.9f,paint); canvas.drawCircle(9,11,2.9f,paint);
                    canvas.drawCircle(14,14,2.9f,paint); canvas.drawCircle(20,5,2.9f,paint); break;
                case "history":
                    paint.setStrokeWidth(2.1f);
                    canvas.drawArc(3,3,21,21,218,301,false,paint);
                    line(canvas,2.5f,4,2.5f,9,7.5f,9);
                    line(canvas,12,7,12,12,16,15); break;
                case "password_eye":
                case "password_eye_hidden":
                    Path eye = new Path();
                    eye.moveTo(1,12);
                    eye.cubicTo(6,2,18,2,23,12);
                    eye.cubicTo(18,22,6,22,1,12);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawPath(eye,paint);
                    paint.setColor(Color.WHITE);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(12,12,4.1f,paint);
                    if (name.equals("password_eye_hidden")) line(canvas,4,4,20,20);
                    break;
                case "registration_google":
                    paint.setStrokeWidth(4);
                    paint.setStrokeCap(Paint.Cap.BUTT);
                    paint.setColor(0xffea4335);
                    canvas.drawArc(3,3,21,21,221,95,false,paint);
                    paint.setColor(0xfffbbc05);
                    canvas.drawArc(3,3,21,21,146,75,false,paint);
                    paint.setColor(0xff34a853);
                    canvas.drawArc(3,3,21,21,43,103,false,paint);
                    paint.setColor(0xff4285f4);
                    canvas.drawArc(3,3,21,21,0,43,false,paint);
                    line(canvas,12,12,21,12);
                    break;
                case "mqid":
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawRoundRect(0.5f,6.5f,23.5f,17.5f,1.2f,1.2f,paint);
                    paint.setColor(BLUE);
                    paint.setTextSize(8.3f);
                    paint.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
                    canvas.drawText("MQID",12-paint.measureText("MQID")/2,15,paint);
                    break;
                case "message_search":
                    paint.setStrokeWidth(2);
                    canvas.drawCircle(14,9.5f,7,paint);
                    canvas.drawArc(10,5.5f,18,13.5f,-82,68,false,paint);
                    paint.setStrokeWidth(3);
                    line(canvas,9,15,3,21);
                    break;
                case "empty_messages":
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawOval(0,0,24,22,paint);
                    Path tail = new Path();
                    tail.moveTo(3,17);
                    tail.quadTo(2.7f,21,0.3f,23.5f);
                    tail.quadTo(1,25,10.5f,21);
                    tail.close();
                    canvas.drawPath(tail,paint);
                    paint.setColor(Color.WHITE);
                    for (float x : new float[]{6.2f,12,17.8f}) canvas.drawCircle(x,11.3f,1.9f,paint);
                    break;
                case "messages":
                    paint.setStrokeWidth(2.2f);
                    Path rear = new Path();
                    rear.moveTo(17,8);
                    rear.lineTo(17,5);
                    rear.quadTo(17,3,15,3);
                    rear.lineTo(4,3);
                    rear.quadTo(2,3,2,5);
                    rear.lineTo(2,17);
                    rear.lineTo(6,14);
                    rear.lineTo(10,14);
                    canvas.drawPath(rear,paint);
                    Path front = new Path();
                    front.moveTo(12,9);
                    front.lineTo(20,9);
                    front.quadTo(22,9,22,11);
                    front.lineTo(22,22);
                    front.lineTo(18,19);
                    front.lineTo(12,19);
                    front.quadTo(10,19,10,17);
                    front.lineTo(10,11);
                    front.quadTo(10,9,12,9);
                    canvas.drawPath(front,paint);
                    break;
                case "trade_sort":
                    line(canvas,3,14,8,19,13,14); line(canvas,8,19,8,4);
                    line(canvas,11,9,16,4,21,9); line(canvas,16,4,16,19); break;
                case "position_badge":
                    paint.setStyle(Paint.Style.FILL);
                    line(canvas,0,0,24,0,0,24,0,0);
                    break;
                case "new_order":
                    paint.setStrokeWidth(2);
                    line(canvas,5,2,14,2,20,8,20,22,5,22,5,2);
                    line(canvas,14,2,14,8,20,8);
                    line(canvas,9,14,16,14); line(canvas,12.5f,10.5f,12.5f,17.5f); break;
                case "news":
                case "journal":
                    canvas.drawRoundRect(5,3,20,21,2,2,paint);
                    line(canvas,8,8,17,8); line(canvas,8,12,17,12); line(canvas,8,16,14,16); break;
                case "mail":
                    canvas.drawRoundRect(2,5,22,19,2,2,paint); line(canvas,3,7,12,13,21,7); break;
                case "settings":
                    line(canvas,12,2,14,5,18,4,20,7,18,10,22,12,21,15,17,16,17,20,13,22,10,19,6,20,4,17,6,14,2,12,3,9,7,8,7,4,12,2);
                    canvas.drawCircle(12,12,3,paint); break;
                case "calendar":
                    canvas.drawRoundRect(3,5,21,21,2,2,paint);
                    line(canvas,3,10,21,10); line(canvas,8,2,8,7); line(canvas,16,2,16,7); break;
                case "community":
                    canvas.drawCircle(7,7,3,paint); canvas.drawCircle(17,7,3,paint);
                    canvas.drawRoundRect(2,13,11,21,2,2,paint); canvas.drawRoundRect(13,13,22,21,2,2,paint); break;
                case "about":
                    canvas.drawCircle(12,12,9,paint); line(canvas,12,11,12,17); canvas.drawCircle(12,7,0.45f,paint); break;
                case "brand":
                    paint.setStrokeWidth(2);
                    line(canvas,2,17,6,16,11,13,17,7,20,2);
                    line(canvas,15,5,20,2,21,8);
                    line(canvas,6,18,6,21); line(canvas,10,16,10,21); line(canvas,14,13,14,19);
                    paint.setColor(0xffd9ab00);
                    canvas.drawArc(2,1,23,22,-11,148,false,paint); break;
            }
            canvas.restore();
        }

        private void line(Canvas canvas,float... points) {
            Path path = new Path();
            path.moveTo(points[0],points[1]);
            for (int i=2;i<points.length;i+=2) path.lineTo(points[i],points[i+1]);
            canvas.drawPath(path,paint);
        }
    }

    private final class CandleChart extends View {
        private static final int COUNT = 35;
        private static final int DOWN = 0xffef5350;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final android.graphics.DashPathEffect grid =
                new android.graphics.DashPathEffect(new float[]{dp(3),dp(4)},0);
        private final java.text.SimpleDateFormat dates =
                new java.text.SimpleDateFormat("dd MMM HH:mm",Locale.US);
        private final long[] open = new long[COUNT];
        private final long[] high = new long[COUNT];
        private final long[] low = new long[COUNT];
        private final long[] close = new long[COUNT];
        private final int selected;
        private final int windowIndex;
        private int timeframe;
        private long bucket;
        private boolean menuOpen;
        private boolean crosshair;
        private float rulerX = 0.5f;
        private float rulerY = 0.5f;
        private final java.text.SimpleDateFormat rulerDate =
                new java.text.SimpleDateFormat("yyyy.MM.dd HH:mm",Locale.US);

        CandleChart(int selected,int timeframe,int windowIndex) {
            super(MainActivity.this);
            this.selected = selected;
            this.timeframe = timeframe;
            this.windowIndex = windowIndex;
            setOnClickListener(view -> toggleMenu());
            seed();
        }

        private long period() {
            return CHART_MINUTES[timeframe] * 60000L;
        }

        private void seed() {
            long period = period();
            bucket = System.currentTimeMillis() / period * period;
            Random random = new Random(49L + selected * 31L + timeframe);
            long scale = Math.max(8,Math.round(SPREADS[selected] * tickScale(selected)));
            long next = bidTicks[selected];
            for (int i=COUNT-1;i>=0;i--) {
                close[i] = next;
                open[i] = Math.max(1,next + Math.round((random.nextDouble()-0.5)*scale*5));
                high[i] = Math.max(open[i],close[i]) + 1 + (long)(random.nextDouble()*scale);
                low[i] = Math.max(1,Math.min(open[i],close[i])-1-(long)(random.nextDouble()*scale));
                next = open[i];
            }
        }

        void update() {
            long period = period();
            long currentBucket = System.currentTimeMillis() / period * period;
            if (currentBucket < bucket || currentBucket-bucket >= COUNT*period) seed();
            while (bucket < currentBucket) {
                long previousClose = close[COUNT-1];
                for (long[] series : new long[][]{open,high,low,close}) {
                    System.arraycopy(series,1,series,0,COUNT-1);
                    series[COUNT-1] = previousClose;
                }
                bucket += period;
            }
            int last = COUNT-1;
            close[last] = bidTicks[selected];
            high[last] = Math.max(high[last],close[last]);
            low[last] = Math.min(low[last],close[last]);
            invalidate();
        }

        void toggleMenu() {
            menuOpen = !menuOpen;
            invalidate();
        }

        void toggleCrosshair() {
            crosshair = !crosshair;
            menuOpen = false;
            updateCrosshairButton();
            invalidate();
        }

        private void updateCrosshairButton() {
            crosshairButton.setImageDrawable(new LineIcon(
                    crosshair ? "crosshair_active" : "crosshair",Color.WHITE,22));
            crosshairButton.setContentDescription(
                    crosshair ? "Disable chart crosshair" : "Enable chart crosshair");
            crosshairButton.setSelected(crosshair);
        }

        private float menuRadius() {
            return Math.min(getWidth()*0.335f,getHeight()*0.34f);
        }

        @Override public boolean onTouchEvent(android.view.MotionEvent event) {
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) {
                selectChartPane(this);
                if (crosshair) getParent().requestDisallowInterceptTouchEvent(true);
            }
            if (crosshair && !menuOpen) {
                int action = event.getActionMasked();
                if (action == android.view.MotionEvent.ACTION_DOWN
                        || action == android.view.MotionEvent.ACTION_MOVE
                        || action == android.view.MotionEvent.ACTION_UP) {
                    rulerX = plotFraction(event.getX(),getWidth()-dp(64));
                    rulerY = plotFraction(event.getY(),getHeight()-dp(17));
                    invalidate();
                }
                return true;
            }
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_UP && menuOpen) {
                float x = event.getX()-getWidth()/2f;
                float y = event.getY()-getHeight()*0.42f;
                float radius = menuRadius();
                double distance = Math.hypot(x,y);
                int slot = Math.floorMod((int)Math.round((Math.toDegrees(Math.atan2(y,x))+170)/ (360.0/12)),12);
                menuOpen = false;
                if (distance >= radius*0.58f && distance <= radius) {
                    if (slot < CHART_PERIODS.length) {
                        timeframe = slot;
                        chartPeriod = slot;
                        syncChartWindow();
                        seed();
                    } else if (slot == 10) {
                        toggleCrosshair();
                    } else {
                        if (slot == 9) showPage(INDICATOR_MANAGER);
                        else message("Chart settings","Practice chart");
                    }
                }
                invalidate();
                return true;
            }
            return super.onTouchEvent(event);
        }

        @Override protected void onDraw(Canvas canvas) {
            float right = getWidth()-dp(64);
            float bottom = getHeight()-dp(17);
            if (right <= 0 || bottom <= 0) return;
            long min = low[0];
            long max = Math.max(high[0],askTicks[selected]);
            for (int i=1;i<COUNT;i++) {
                min = Math.min(min,low[i]);
                max = Math.max(max,high[i]);
            }
            long padding = Math.max(3,(max-min)/18);
            min -= padding;
            max += padding;
            double scale = tickScale(selected);
            paint.setTypeface(Typeface.DEFAULT);
            paint.setTextSize(dp(12));
            paint.setStrokeWidth(dp(1));
            int lines = Math.max(3,Math.min(18,(int)(bottom/dp(30))));
            for (int i=0;i<lines;i++) {
                float y = (i+0.5f)*bottom/lines;
                gridLine(canvas,0,y,right,y);
                paint.setColor(INK);
                canvas.drawText(price(selected,(max-(max-min)*y/bottom)/scale),
                        right+dp(3),y+dp(4),paint);
            }
            for (int i=1;i<9;i++) gridLine(canvas,right*i/9f,0,right*i/9f,bottom);
            paint.setColor(INK);
            canvas.drawLine(0,0,right,0,paint);
            canvas.drawLine(0,0,0,bottom,paint);
            canvas.drawLine(right,0,right,bottom,paint);
            canvas.drawLine(0,bottom,right,bottom,paint);

            float step = right/(COUNT+2f);
            for (int i=0;i<COUNT;i++) {
                float x = (i+0.5f)*step;
                float first = position(open[i],min,max,bottom);
                float last = position(close[i],min,max,bottom);
                paint.setColor(close[i]>=open[i] ? ACCENT : DOWN);
                paint.setStrokeWidth(Math.max(1,step*0.12f));
                canvas.drawLine(x,position(high[i],min,max,bottom),x,
                        position(low[i],min,max,bottom),paint);
                canvas.drawRect(x-step*0.31f,Math.min(first,last),x+step*0.31f,
                        Math.max(Math.min(first,last)+1,Math.max(first,last)),paint);
            }
            float bid = position(bidTicks[selected],min,max,bottom);
            float ask = position(askTicks[selected],min,max,bottom);
            priceMarker(canvas,right,ask,Math.min(ask,bid-dp(14)),bottom,currentPrice(selected,false),DOWN);
            priceMarker(canvas,right,bid,bid,bottom,currentPrice(selected,true),ACCENT);

            paint.setColor(INK);
            for (int i=0;i<3;i++) {
                int index = i * 12;
                canvas.drawText(dates.format(new java.util.Date(bucket-(COUNT-1-index)*period())),
                        index*step,bottom+dp(13),paint);
            }
            canvas.drawText(SYMBOLS[selected]+", "+CHART_PERIODS[timeframe],dp(4),dp(16),paint);
            canvas.drawText(CHART_NAMES[selected],dp(4),dp(30),paint);
            if (crosshair) drawRuler(canvas,right,bottom,step,min,max);
            if (menuOpen) drawMenu(canvas);
        }

        private float plotFraction(float coordinate,float extent) {
            return extent <= 0 ? 0.5f : Math.max(0,Math.min(1,coordinate/extent));
        }

        private void drawRuler(Canvas canvas,float right,float bottom,float step,long min,long max) {
            float x = right*rulerX;
            float y = bottom*rulerY;
            float gap = dp(3);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(dp(1));
            if (y > gap) canvas.drawLine(x,0,x,y-gap,paint);
            if (y+gap < bottom) canvas.drawLine(x,y+gap,x,bottom,paint);
            if (x > gap) canvas.drawLine(0,y,x-gap,y,paint);
            if (x+gap < right) canvas.drawLine(x+gap,y,right,y,paint);

            int index = Math.min(COUNT-1,(int)(x/step));
            long time = bucket-(COUNT-1-index)*period();
            String date = rulerDate.format(new java.util.Date(time));
            float width = paint.measureText(date)+dp(6);
            float left = Math.max(0,Math.min(right-width,x-width/2));
            canvas.drawRect(left,bottom+dp(1),left+width,bottom+dp(16),paint);
            paint.setColor(Color.WHITE);
            canvas.drawText(date,left+dp(3),bottom+dp(13),paint);
            double selectedPrice = (max-(max-min)*(double)rulerY)/tickScale(selected);
            float markerY = Math.max(dp(8),Math.min(bottom-dp(8),y));
            paint.setColor(Color.BLACK);
            canvas.drawRect(right+dp(2),markerY-dp(7),getWidth()-dp(3),markerY+dp(7),paint);
            paint.setColor(Color.WHITE);
            canvas.drawText(price(selected,selectedPrice),right+dp(5),markerY+dp(4),paint);
        }

        private float position(long value,long min,long max,float bottom) {
            return (float)((max-value)*bottom/(max-min));
        }

        private void gridLine(Canvas canvas,float x1,float y1,float x2,float y2) {
            paint.setColor(0xffeeeeee);
            paint.setStrokeWidth(1);
            paint.setPathEffect(grid);
            canvas.drawLine(x1,y1,x2,y2,paint);
            paint.setPathEffect(null);
        }

        private void priceMarker(Canvas canvas,float x,float y,float markerY,float bottom,
                                 String value,int color) {
            float center = Math.max(dp(8),Math.min(bottom-dp(8),markerY));
            paint.setColor(color);
            paint.setStrokeWidth(1);
            canvas.drawLine(0,y,x,y,paint);
            canvas.drawRect(x+dp(2),center-dp(7),getWidth()-dp(3),center+dp(7),paint);
            paint.setColor(Color.WHITE);
            canvas.drawText(value,x+dp(5),center+dp(4),paint);
        }

        private void drawMenu(Canvas canvas) {
            float x = getWidth()/2f;
            float y = getHeight()*0.42f;
            float radius = menuRadius();
            paint.setColor(0xef087cf1);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(radius*0.42f);
            canvas.drawCircle(x,y,radius*0.79f,paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextSize(Math.min(dp(15),radius*0.14f));
            String[] items = {"M1","M5","M15","M30","H1","H4","D1","W1","MN","\u0192","+","\u2699"};
            for (int i=0;i<items.length;i++) {
                double angle = Math.toRadians(-170+i*30);
                float tx = x+(float)Math.cos(angle)*radius*0.79f;
                float ty = y+(float)Math.sin(angle)*radius*0.79f;
                paint.setColor(i == timeframe ? 0xffa9d6ff : Color.WHITE);
                canvas.save();
                float rotation = -80+i*30;
                if (rotation > 90) rotation -= 180;
                canvas.rotate(rotation,tx,ty);
                canvas.drawText(items[i],tx-paint.measureText(items[i])/2,ty+paint.getTextSize()*0.34f,paint);
                canvas.restore();
            }
        }
    }
}
