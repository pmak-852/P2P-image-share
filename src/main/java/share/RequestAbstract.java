package share;

import share.message.P2PMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class RequestAbstract extends Thread {
    PeerInfo requestTarget;
    P2PMessage request, response;

    public RequestAbstract(P2PMessage req) {
        this.requestTarget = req.getReceiver();
        this.request = req;
    }

    abstract public void handleResponse(P2PMessage res);

    public P2PMessage getResponse() {
        return this.response;
    }

    public void HandleException(Exception e) {
        e.printStackTrace();
    }

    public void threadCloseOps(){

    }

    @Override
    public void run() {
        Socket s = new Socket();
        try {
            s.connect(new InetSocketAddress(requestTarget.getIP(), requestTarget.getRequestListenPort()), 5000);
            ObjectOutputStream w = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream r = new ObjectInputStream(s.getInputStream());

            w.writeObject(this.request);
            this.response = (P2PMessage) r.readObject();
            handleResponse(this.response);

        } catch (Exception e) {
            HandleException(e);
        } finally {
            try {
                this.threadCloseOps();
                s.close();
            } catch (IOException e) {
                this.threadCloseOps();
                e.printStackTrace();
            }
        }
    }
}
