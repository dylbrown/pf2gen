package tools.nethys;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

class ProxyPool {
    private final int max;
    private final Semaphore available;

    static{
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);
    }

    static WebClient makeClient() {
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setTimeout(0);
        webClient.getOptions().setConnectionTimeToLive(0);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.waitForBackgroundJavaScript(100000);
        return webClient;
    }

    public WebClient getItem() throws InterruptedException {
        available.acquire();
        return getNextAvailableItem();
    }

    public void putItem(WebClient x) {
        if (markAsUnused(x))
            available.release();
    }

    protected List<WebClient> items = new ArrayList<>();
    protected List<Boolean> used = new ArrayList<>();

    public ProxyPool(int max) {
        this.max = max;
        this.available = new Semaphore(max, true);
        for(int i = 0; i < max; i++) {
            items.add(makeClient());
            used.add(false);
        }
    }

    private synchronized WebClient getNextAvailableItem() {
        for (int i = 0; i < max; ++i) {
            if (!used.get(i)) {
                used.set(i, true);
                return items.get(i);
            }
        }
        return null; // not reached
    }

    private synchronized boolean markAsUnused(WebClient item) {
        for (int i = 0; i < max; ++i) {
            if (item == items.get(i)) {
                if (used.get(i)) {
                    used.set(i, false);
                    return true;
                } else
                    return false;
            }
        }
        return false;
    }
}
