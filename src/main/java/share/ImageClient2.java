package share;

import share.data.ImageBlock;
import share.data.ImagePanel;
import share.message.P2PMessage;
import share.message.P2PMessageType;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class ImageClient2 extends SharingRole {
    private static final Logger logger = Logger.getLogger(ImageClient2.class.getName());

    /* Thread */
    public final static int MAX_REQUEST = 3;

    public static void main(String[] args) {
        int port = 8003;
        boolean validInfo = false;
        P2PMessage loginSetting = null;
        PeerInfo user = new PeerInfo("localhost", "ImageClient2", null, port);

        try {
            while (!validInfo) {
                requestLogin req = new requestLogin(user);
                req.start();
                req.join();

                loginSetting = req.res;
                validInfo = loginSetting.getCmd().equals(P2PMessageType.resLoginOk);
                port++;
            }

            ImageClient2 client = new ImageClient2(user, loginSetting.getImgName());
            client.mergePeers(loginSetting);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ImageClient2(PeerInfo info, String imgName) {
        super(info);

        JFrame ui = new JFrame();
        ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ui.setSize(630, 680);
        ui.setLayout(new FlowLayout());
        ui.add(this.panel);
        ui.setResizable(true);

        ui.pack();
        ui.setVisible(true);

        this.availablePeer.add(ImageServer.selfInfo);
        this.panel.setImageName(imgName);

        new ClientRequestListen(self, panel, availablePeer).start();
        new requestThread(self).start();
    }

    class requestThread extends Thread {
        final Set<RequestAbstract> currentRequest = Collections.synchronizedSet(new HashSet<>());
        final PeerInfo clientInfo;

        requestThread(PeerInfo info) {
            this.clientInfo = info;
        }

        @Override
        public void run() {
            while (true) {
                if (this.currentRequest.size() <= MAX_REQUEST) {
                    ImageBlock nextEmptyBlockN = panel.getNextEmptyBlockN();
                    if (nextEmptyBlockN != null) {
                        PeerInfo requestTarget = getNextPeer();
                        if (requestTarget != null) {
                            P2PMessage request = P2PMessage.reqImageBlock(this.clientInfo, requestTarget, availablePeer, panel.getImageName(), nextEmptyBlockN.getImageBlockIdx());
                            requestImageBlk requestThread = new requestImageBlk(panel, request);

                            logger.info("request " + nextEmptyBlockN.getImageBlockIdx() + " " + requestTarget.getUserName());
                            this.currentRequest.add(requestThread);
                            requestThread.start();
                        }
                    }
                }
            }
        }

        public class requestImageBlk extends RequestAbstract {
            private final ImagePanel clientPanel;
            private final int BlkIdx;

            public requestImageBlk(ImagePanel clientPanel, P2PMessage req) {
                super(req);
                this.clientPanel = clientPanel;
                this.BlkIdx = req.getImageBlockN();

                this.clientPanel.lockImageBlock(this.BlkIdx);
            }

            @Override
            public void HandleException(Exception e) {
                requestThread.this.currentRequest.remove(this);
                this.clientPanel.releaseImageBlock(this.BlkIdx);
            }

            @Override
            public void threadCloseOps() {
                clientPanel.releaseImageBlock(BlkIdx);
                requestThread.this.currentRequest.remove(this);
            }

            @Override
            public void handleResponse(P2PMessage res) {
                switch (this.response.getCmd()) {
                    case resImgBlk:
                        clientPanel.setImageBlock(this.response);
                        logger.info("receive block " + this.response.getImageBlockN() + " from " + this.response.getSender());
                        ImageClient2.this.mergePeers(res);
                        break;
                    case resImgBlkNotAvailable:
                    case resImgNameMismatch:
                        break;
                    default:
                        throw new RuntimeException("Unknown response to Image request");
                }

                requestThread.this.currentRequest.remove(this);
                this.clientPanel.releaseImageBlock(this.BlkIdx);
            }
        }

    }

    class ClientRequestListen extends Thread {
        private final PeerInfo ofPeer;
        private final ImagePanel ParentPanel;
        private final List<PeerInfo> peers;

        public ClientRequestListen(PeerInfo serverInfo, ImagePanel parentPanel, List<PeerInfo> peers) {
            this.ofPeer = serverInfo;
            this.ParentPanel = parentPanel;
            this.peers = peers;
        }

        @Override
        public void run() {
            try {
                ServerSocket listener = new ServerSocket(ofPeer.getRequestListenPort());
                System.out.println("Listening from port " + listener.getLocalPort());

                while (true) {
                    Socket s = listener.accept();
                    logger.info("receive new request!!");
                    new RequestHandler(s, this).start();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to start the server socket");
            }
        }

        class RequestHandler extends Thread {
            private final PeerInfo parentInfo;
            private final ImagePanel parentPanel;
            private final List<PeerInfo> parentPeers;

            private final ObjectOutputStream w;
            private final ObjectInputStream r;
            private P2PMessage request;


            public RequestHandler(Socket s, ClientRequestListen listener) throws IOException {
                this.parentInfo = listener.ofPeer;
                this.parentPanel = listener.ParentPanel;
                this.parentPeers = listener.peers;

                this.r = new ObjectInputStream(s.getInputStream());
                this.w = new ObjectOutputStream(s.getOutputStream());
            }

            private void handlerIncomingMsg() {
                /* Identifies and responds to the incoming message */
                PeerInfo sender = request.getSender();

                switch (this.request.getCmd()) {
                    case requestActiveCheck:
                        P2PMessage Active = P2PMessage.resActiveCheckOK(parentInfo, sender, parentPeers);
                        this.ResponseToRequest(Active);
                        break;
                    case requestImgBlk:
                        ImageBlock imgBlk = parentPanel.blockAvailable(request.getImageBlockN(), request.getImgName());
                        if (imgBlk != null) {
                            P2PMessage Block = P2PMessage.resImageBlock(parentInfo, sender, parentPeers, parentPanel.getImageName(), request.getImageBlockN(), imgBlk.getImage());
                            this.ResponseToRequest(Block);
                        } else {
                            P2PMessage Block = P2PMessage.resImageBlockNotAvailable(parentInfo, sender, parentPeers, panel.getImageName());
                            this.ResponseToRequest(Block);
                        }
                        break;
                    case requestImageUpdate:
                        ParentPanel.setImageName(request.getImgName());
                        ParentPanel.repaint();
                        this.ResponseToRequest(P2PMessage.resImageUpdateOK(parentInfo, sender, parentPeers, panel.getImageName()));
                        break;
                    default:
                        throw new RuntimeException("Unknown request message type");
                }
                ImageClient2.this.mergePeers(this.request);
            }

            private void ResponseToRequest(P2PMessage RespondMsg) {
                try {
                    this.w.reset();
                    this.w.writeObject(RespondMsg);
                    this.w.flush();
                } catch (Exception e) {
                }
            }

            @Override
            public void run() {
                try {
                    this.request = (P2PMessage) this.r.readObject();
                    System.out.println("receive " + this.request.getCmd() + " request from " + this.request.getSender().getUserName());
                    this.handlerIncomingMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class requestLogin extends RequestAbstract {
        P2PMessage res;

        public requestLogin(PeerInfo loginPeer) {
            super(P2PMessage.reqLogin(loginPeer, ImageServer.selfInfo));
        }

        @Override
        public void handleResponse(P2PMessage res) {
            switch (res.getCmd()) {
                case resLoginOk:
                    this.res = res;
                    break;
                case resLoginFail:
                    return;
                default:
                    throw new RuntimeException("Unknown response to requestLogin");
            }
        }
    }

}
