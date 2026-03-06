
package rich.util.modules.autobuy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import rich.util.modules.autobuy.BuyRequest;
import rich.util.string.chat.ChatMessage;

public class NetworkManager {
    private static final int PORT = 25566;
    private volatile ServerSocket serverSocket;
    private volatile Socket clientSocket;
    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList();
    private volatile PrintWriter out;
    private volatile BufferedReader in;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    private volatile ExecutorService executor;
    private final ConcurrentLinkedQueue<BuyRequest> buyQueue = new ConcurrentLinkedQueue();
    private final ConcurrentLinkedQueue<String> serverSwitchQueue = new ConcurrentLinkedQueue();
    private final ConcurrentLinkedQueue<Boolean> pauseQueue = new ConcurrentLinkedQueue();
    private final ConcurrentLinkedQueue<Boolean> updateQueue = new ConcurrentLinkedQueue();
    private final AtomicInteger clientsInAuction = new AtomicInteger(0);

    public void startAsServer() {
        this.stop();
        this.sleep(300L);
        this.running.set(true);
        this.stopping.set(false);
        this.clients.clear();
        this.clientsInAuction.set(0);
        this.executor = Executors.newCachedThreadPool();
        this.executor.execute(() -> {
            ServerSocket ss;
            int attempts = 0;
            while (this.running.get() && this.serverSocket == null && attempts < 5) {
                try {
                    ss = new ServerSocket();
                    ss.setReuseAddress(true);
                    ss.bind(new InetSocketAddress(25566));
                    ss.setSoTimeout(1000);
                    this.serverSocket = ss;
                    this.msg("\u00a7a[BUYER] Server started on port 25566");
                }
                catch (IOException e) {
                    if (++attempts < 5) {
                        this.msg("\u00a7e[BUYER] Port busy, attempt " + attempts + "/5...");
                        this.sleep(1000L);
                        continue;
                    }
                    this.msg("\u00a7c[BUYER] Failed to start server");
                    return;
                }
            }
            while (this.running.get() && !this.stopping.get() && (ss = this.serverSocket) != null && !ss.isClosed()) {
                try {
                    Socket client = ss.accept();
                    client.setTcpNoDelay(true);
                    client.setKeepAlive(true);
                    client.setSoTimeout(5000);
                    ClientHandler handler = new ClientHandler(client);
                    this.clients.add(handler);
                    this.connected.set(true);
                    this.msg("\u00a7a[BUYER] Checker #" + this.clients.size() + " connected!");
                    this.executor.execute(() -> this.handleClient(handler));
                }
                catch (SocketTimeoutException client) {
                }
                catch (IOException e) {
                    if (!this.running.get() || this.stopping.get()) continue;
                    this.sleep(100L);
                }
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleClient(ClientHandler handler) {
        try {
            while (this.running.get() && !this.stopping.get() && !handler.closed) {
                String line;
                try {
                    line = handler.in.readLine();
                }
                catch (SocketTimeoutException e) {
                    continue;
                }
                if (line == null) {
                    break;
                }
                this.processServerMessage(line, handler);
            }
        }
        catch (IOException iOException) {
        }
        finally {
            if (handler.inAuction) {
                this.clientsInAuction.decrementAndGet();
            }
            handler.close();
            this.clients.remove(handler);
            this.updateConnectedState();
            if (this.running.get() && !this.stopping.get()) {
                this.msg("\u00a7c[BUYER] Checker disconnected. Remaining: " + this.clients.size());
            }
        }
    }

    private void updateConnectedState() {
        this.connected.set(!this.clients.isEmpty());
    }

    public void startAsClient() {
        this.stop();
        this.sleep(300L);
        this.running.set(true);
        this.stopping.set(false);
        this.executor = Executors.newCachedThreadPool();
        this.executor.execute(() -> {
            while (this.running.get() && !this.stopping.get()) {
                if (!this.connected.get()) {
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress("localhost", 25566), 2000);
                        socket.setTcpNoDelay(true);
                        socket.setKeepAlive(true);
                        socket.setSoTimeout(5000);
                        this.clientSocket = socket;
                        this.out = new PrintWriter(socket.getOutputStream(), true);
                        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        this.connected.set(true);
                        this.msg("\u00a7a[CHECKER] Connected to buyer!");
                        this.clientReadLoop();
                    }
                    catch (IOException e) {
                        this.connected.set(false);
                    }
                }
                this.sleep(2000L);
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void clientReadLoop() {
        try {
            while (this.running.get() && this.connected.get() && !this.stopping.get()) {
                String line;
                BufferedReader reader = this.in;
                if (reader == null) {
                    break;
                }
                try {
                    line = reader.readLine();
                }
                catch (SocketTimeoutException e) {
                    continue;
                }
                if (line == null) {
                    break;
                }
                this.processClientMessage(line);
            }
        }
        catch (IOException iOException) {
        }
        finally {
            this.connected.set(false);
            if (this.running.get() && !this.stopping.get()) {
                this.msg("\u00a7c[CHECKER] Connection lost");
            }
            this.closeClientSocket();
        }
    }

    private void processServerMessage(String line, ClientHandler handler) {
        if (line.startsWith("BUY:")) {
            try {
                String data = line.substring(4);
                String[] parts = data.split("\\|\\|\\|");
                if (parts.length == 7) {
                    int price = Integer.parseInt(parts[0]);
                    String itemId = parts[1];
                    String displayName = parts[2];
                    int count = Integer.parseInt(parts[3]);
                    String loreHash = parts[4];
                    int maxPrice = Integer.parseInt(parts[5]);
                    int minQuantity = Integer.parseInt(parts[6]);
                    this.buyQueue.add(new BuyRequest(price, itemId, displayName, count, loreHash, maxPrice, minQuantity));
                }
            }
            catch (Exception exception) {}
        } else if (line.equals("ENTER_AUCTION")) {
            if (!handler.inAuction) {
                handler.inAuction = true;
                this.clientsInAuction.incrementAndGet();
            }
        } else if (line.equals("LEAVE_AUCTION")) {
            if (handler.inAuction) {
                handler.inAuction = false;
                this.clientsInAuction.decrementAndGet();
            }
        } else if (line.equals("PAUSE:true")) {
            this.pauseQueue.add(true);
        } else if (line.equals("PAUSE:false")) {
            this.pauseQueue.add(false);
        }
    }

    private void processClientMessage(String line) {
        if (line.startsWith("SWITCH:")) {
            String server = line.substring(7);
            this.serverSwitchQueue.add(server);
        } else if (line.equals("PAUSE:true")) {
            this.pauseQueue.add(true);
        } else if (line.equals("PAUSE:false")) {
            this.pauseQueue.add(false);
        } else if (line.equals("UPDATE")) {
            this.updateQueue.add(true);
        }
    }

    public void sendUpdateCommand() {
        for (ClientHandler handler : this.clients) {
            if (!handler.inAuction || handler.closed) continue;
            handler.send("UPDATE");
        }
    }

    public boolean pollUpdateCommand() {
        return this.updateQueue.poll() != null;
    }

    public int getClientsInAuctionCount() {
        return this.clientsInAuction.get();
    }

    public void sendBuyCommand(int price, String itemId, String displayName, int count, String loreHash, int maxPrice, int minQuantity) {
        if (this.connected.get() && this.out != null) {
            try {
                this.out.println("BUY:" + price + "|||" + itemId + "|||" + displayName + "|||" + count + "|||" + loreHash + "|||" + maxPrice + "|||" + minQuantity);
                this.out.flush();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void sendServerSwitch(String server) {
        for (ClientHandler handler : this.clients) {
            handler.send("SWITCH:" + server);
        }
    }

    public void sendPauseState(boolean paused) {
        String msg = "PAUSE:" + paused;
        if (this.out != null) {
            try {
                this.out.println(msg);
                this.out.flush();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        for (ClientHandler handler : this.clients) {
            handler.send(msg);
        }
    }

    public void sendEnterAuction() {
        if (this.connected.get() && this.out != null) {
            try {
                this.out.println("ENTER_AUCTION");
                this.out.flush();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void sendLeaveAuction() {
        if (this.connected.get() && this.out != null) {
            try {
                this.out.println("LEAVE_AUCTION");
                this.out.flush();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public BuyRequest pollBuyRequest() {
        return this.buyQueue.poll();
    }

    public String pollServerSwitch() {
        return this.serverSwitchQueue.poll();
    }

    public Boolean pollPauseState() {
        return this.pauseQueue.poll();
    }

    public boolean isConnected() {
        return this.connected.get();
    }

    public int getConnectedClientCount() {
        return this.clients.size();
    }

    public boolean isConnectedToServer() {
        return this.connected.get() && this.clientSocket != null;
    }

    public boolean isServerRunning() {
        return this.serverSocket != null && !this.serverSocket.isClosed();
    }

    private void closeClientSocket() {
        PrintWriter tempOut = this.out;
        BufferedReader tempIn = this.in;
        Socket tempClient = this.clientSocket;
        this.out = null;
        this.in = null;
        this.clientSocket = null;
        try {
            if (tempIn != null) {
                tempIn.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if (tempOut != null) {
                tempOut.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if (tempClient != null) {
                tempClient.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void closeServerSocket() {
        ServerSocket temp = this.serverSocket;
        this.serverSocket = null;
        if (temp != null) {
            try {
                temp.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void stop() {
        this.stopping.set(true);
        this.running.set(false);
        this.connected.set(false);
        this.clientsInAuction.set(0);
        this.buyQueue.clear();
        this.serverSwitchQueue.clear();
        this.pauseQueue.clear();
        this.updateQueue.clear();
        for (ClientHandler handler : this.clients) {
            handler.close();
        }
        this.clients.clear();
        this.closeClientSocket();
        this.closeServerSocket();
        ExecutorService temp = this.executor;
        this.executor = null;
        if (temp != null) {
            temp.shutdownNow();
            try {
                temp.awaitTermination(500L, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
    }

    private void msg(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            mc.execute(() -> ChatMessage.autobuymessage(text));
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    private static class ClientHandler {
        final Socket socket;
        final PrintWriter out;
        final BufferedReader in;
        volatile boolean inAuction = false;
        volatile boolean closed = false;

        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        void close() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            try {
                this.in.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                this.out.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                this.socket.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }

        void send(String message) {
            if (!this.closed && this.out != null) {
                try {
                    this.out.println(message);
                    this.out.flush();
                }
                catch (Exception ignored) {
                    this.closed = true;
                }
            }
        }
    }
}

