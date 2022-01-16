package share;

import share.data.ImageBlock;
import share.data.ImagePanel;
import share.data.ImageSlicer;
import share.message.P2PMessage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ImageServer extends SharingRole {
    public static PeerInfo selfInfo = new PeerInfo("localhost", "Teacher", null, 8001);

    public static void main(String[] args) {
        try {
            ImageServer c = new ImageServer(selfInfo);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changeImage(String ImageName, BufferedImage img, ImageSlicer slicer) {
        BufferedImage[] slice = slicer.slice(slicer.resize(img));

        this.panel.setImageName(ImageName);
        for (int n = 0; n < slicer.getN(); n++) {
            this.panel.setImageBlock(ImageName, n, slice[n]);
        }
    }

    public ImageServer(PeerInfo serverInfo) throws IOException {
        super(serverInfo);
        this.panel.setImageName("testImage.png");

        BufferedImage img = ImageIO.read(ClassLoader.getSystemResourceAsStream("testImage.png"));
        BufferedImage img2 = ImageIO.read(ClassLoader.getSystemResourceAsStream("testImage2.jpg"));
        ImageSlicer slicer = new ImageSlicer(30, 600, 600);
        this.changeImage("testImage.png", img, slicer);

        /* Change Image Button */
        JButton button = new JButton("Swap Image");

        button.addActionListener(e -> {
            if (panel.getImageName().equalsIgnoreCase("testImage.png")) {
                changeImage("testImage2.jpg", img2, slicer);
            } else {
                changeImage("testImage.png", img, slicer);
            }
            synchronized (availablePeer) {
                availablePeer.forEach(client -> new requestImageUpdate(selfInfo, client, availablePeer, this.panel.getImageName()).start());
            }
        });


        JFrame ui = new JFrame();
        ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ui.setSize(630, 680);
        ui.setLayout(new FlowLayout());
        ui.add(this.panel);
        ui.add(button);
        ui.setResizable(true);

        ui.pack();
        ui.setVisible(true);

        new ServerRequestListen(selfInfo, panel, availablePeer).start();
        new RegularActiveCheck(serverInfo, availablePeer).start();

    }


    public class ServerRequestListen extends Thread implements Runnable {
        private final PeerInfo ofPeer;
        private final List<PeerInfo> peers;
        private final ImagePanel ParentPanel;

        public ServerRequestListen(PeerInfo serverInfo, ImagePanel parentPanel, List<PeerInfo> peers) {
            this.ofPeer = serverInfo;
            this.ParentPanel = parentPanel;
            this.peers = peers;
        }

        @Override
        public void run() {
            ServerSocket listener;
            try {
                int tryPort = 8001;
                listener = null;
                while (listener == null && tryPort < 65536) {
                    try {
                        listener = new ServerSocket(tryPort);
                        ofPeer.setRequestListenPort(listener.getLocalPort());
                    } catch (IOException e) {
                        tryPort++;
                    }
                }
                System.out.println("Listening from port" + listener.getLocalPort());

                while (true) {
                    Socket s = listener.accept();
                    new RequestHandler(s).start();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to start the server socket");
            }
        }

        class RequestHandler extends Thread implements Runnable {
            private final Socket s;
            private final ObjectOutputStream w;
            private final ObjectInputStream r;
            private P2PMessage request;


            public RequestHandler(Socket s) throws IOException {
                this.s = s;
                this.r = new ObjectInputStream(s.getInputStream());
                this.w = new ObjectOutputStream(s.getOutputStream());
            }

            private void handlerIncomingMsg() {
                /* Identifies and responds to the incoming message */
                PeerInfo sender = request.getSender();
                switch (this.request.getCmd()) {
                    case requestImgBlk:
                        ImageBlock imgBlk = ParentPanel.blockAvailable(request.getImageBlockN(), request.getImgName());
                        if (imgBlk != null) {
                            P2PMessage Block = P2PMessage.resImageBlock(ofPeer, sender, peers, ParentPanel.getImageName(), request.getImageBlockN(), imgBlk.getImage());
                            this.ResponseToRequest(Block);
                        } else {
                            P2PMessage Block = P2PMessage.resImageBlockNotAvailable(ofPeer, sender, peers, ParentPanel.getImageName());
                            this.ResponseToRequest(Block);
                        }
                        break;
                    case requestLogin:
                        if (!containsPeer(sender)) {
                            P2PMessage res = P2PMessage.resLoginOK(ofPeer, sender, peers, ParentPanel.getImageName());
                            this.ResponseToRequest(res);
                        } else {
                            P2PMessage res = P2PMessage.resLoginFail(ofPeer, sender, peers);
                            this.ResponseToRequest(res);
                        }
                        break;
                    default:
                        throw new RuntimeException("Unknown request message type");
                }

                ImageServer.this.mergePeers(this.request);
            }

            private void ResponseToRequest(P2PMessage RespondMsg) {
                try {
                    this.w.reset();
                    this.w.writeObject(RespondMsg);
                    this.w.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void run() {
                try {
                    this.request = (P2PMessage) this.r.readObject();
                    this.handlerIncomingMsg();
                } catch (Exception e) {
                    try {
                        this.s.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public class RegularActiveCheck extends Thread {
        private final PeerInfo serverInfo;
        private final List<PeerInfo> peers;

        public RegularActiveCheck(PeerInfo serverInfo, List<PeerInfo> peers) {
            this.serverInfo = serverInfo;
            this.peers = peers;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (this.peers) {
                        this.peers.forEach(peer -> {
                            new requestActiveCheck(peer).start();
                        });
                    }
                    printPeers();
                    sleep(5 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        class requestActiveCheck extends RequestAbstract {
            PeerInfo target;

            public requestActiveCheck(PeerInfo requestTarget) {
                super(P2PMessage.reqActiveCheck(RegularActiveCheck.this.serverInfo, requestTarget, RegularActiveCheck.this.peers));
                this.target = requestTarget;
            }

            @Override
            public void HandleException(Exception e) {
                synchronized (RegularActiveCheck.this.peers) {
                    RegularActiveCheck.this.peers.remove(this.target);
                    System.out.println(this.target.getUserName() + " not responding");
                }
            }

            @Override
            public void handleResponse(P2PMessage res) {
                switch (res.getCmd()) {
                    case resActiveOK:
                        return;
                    default:
                        throw new RuntimeException("Unknown response to activeCheckRequest");
                }
            }
        }
    }

    public class requestImageUpdate extends RequestAbstract {
        private final PeerInfo serverInfo;
        private final List<PeerInfo> peers;

        public requestImageUpdate(PeerInfo serverInfo, PeerInfo requestTarget, List<PeerInfo> peers, String newImageName) {
            super(P2PMessage.reqImageUpdate(serverInfo, requestTarget, peers, newImageName));
            this.serverInfo = serverInfo;
            this.peers = peers;
        }

        @Override
        public void handleResponse(P2PMessage res) {
            switch (res.getCmd()) {
                case resImageUpdateOk:
                    break;
                default:
                    throw new RuntimeException("Unknown response to updateImageRequest");
            }
            ImageServer.this.mergePeers(res);
        }
    }
}
